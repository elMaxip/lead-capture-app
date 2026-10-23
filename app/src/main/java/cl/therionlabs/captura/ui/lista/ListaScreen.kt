package cl.therionlabs.captura.ui.lista

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.automirrored.outlined.StickyNote2
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.ContactPhone
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Laptop
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.therionlabs.captura.R
import cl.therionlabs.captura.domain.model.Modalidad
import cl.therionlabs.captura.ui.components.OfflineBanner
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val FORMATO_FECHA: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm", Locale.forLanguageTag("es-CL"))

fun formatearFecha(epochMs: Long): String =
    FORMATO_FECHA.format(Instant.ofEpochMilli(epochMs).atZone(ZoneId.systemDefault()))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaScreen(viewModel: ListaViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    // Si ya hay datos en pantalla, el error de una actualización se informa con un snackbar.
    LaunchedEffect(state.error, state.prospectos.isEmpty()) {
        val error = state.error
        if (error != null && state.prospectos.isNotEmpty()) {
            snackbarHostState.showSnackbar(resources.getString(error))
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.lista_titulo)) },
                actions = {
                    IconButton(onClick = viewModel::actualizar, enabled = !state.actualizando) {
                        Icon(Icons.Outlined.Refresh, contentDescription = stringResource(R.string.lista_actualizar))
                    }
                    IconButton(onClick = viewModel::cerrarSesion) {
                        Icon(
                            Icons.AutoMirrored.Outlined.Logout,
                            contentDescription = stringResource(R.string.cerrar_sesion),
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            OfflineBanner(
                visible = state.sinConexion,
                mensaje = stringResource(R.string.lista_sin_conexion),
            )

            PullToRefreshBox(
                isRefreshing = state.actualizando && !state.cargando,
                onRefresh = viewModel::actualizar,
                modifier = Modifier.fillMaxSize(),
            ) {
                when {
                    state.cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                    state.prospectos.isEmpty() && state.error != null -> EstadoVacio(
                        icono = Icons.Outlined.CloudOff,
                        titulo = stringResource(state.error!!),
                        detalle = stringResource(R.string.lista_error_detalle),
                        onReintentar = viewModel::actualizar,
                    )
                    state.prospectos.isEmpty() -> EstadoVacio(
                        icono = Icons.Outlined.Inbox,
                        titulo = stringResource(R.string.lista_vacia),
                        detalle = stringResource(R.string.lista_vacia_detalle),
                        onReintentar = viewModel::actualizar,
                    )
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        item(key = "total") {
                            Text(
                                pluralStringResource(
                                    R.plurals.lista_total,
                                    state.prospectos.size,
                                    state.prospectos.size,
                                ),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .widthIn(max = 600.dp)
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp, vertical = 8.dp),
                            )
                        }
                        items(state.prospectos, key = { it.id }) { item ->
                            ProspectoCard(
                                item = item,
                                modifier = Modifier
                                    .animateItem()
                                    .widthIn(max = 600.dp)
                                    .fillMaxWidth(),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProspectoCard(item: ProspectoItem, modifier: Modifier = Modifier) {
    var expandido by rememberSaveable(item.id) { mutableStateOf(false) }
    val giro by animateFloatAsState(if (expandido) 180f else 0f, label = "giroFlecha")

    Card(
        onClick = { expandido = !expandido },
        modifier = modifier.animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Iniciales(item.nombre)
                Spacer(Modifier.size(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        item.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        item.curso,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Icon(
                    Icons.Outlined.ExpandMore,
                    contentDescription = stringResource(
                        if (expandido) R.string.lista_ocultar_detalle else R.string.lista_ver_detalle
                    ),
                    modifier = Modifier.rotate(giro),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.size(4.dp))
            Row(
                modifier = Modifier.padding(start = 52.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Dato(
                    icono = if (item.modalidad == Modalidad.ONLINE) Icons.Outlined.Laptop else Icons.Outlined.Groups,
                    texto = item.modalidad.etiqueta,
                )
                Dato(icono = Icons.Outlined.Schedule, texto = formatearFecha(item.capturadoEn))
            }

            AnimatedVisibility(visible = expandido) {
                Column(
                    modifier = Modifier.padding(start = 52.dp, top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    HorizontalDivider()
                    Dato(icono = Icons.Outlined.Email, texto = item.email)
                    Dato(icono = Icons.Outlined.Phone, texto = item.telefono)
                    Dato(
                        icono = Icons.Outlined.ContactPhone,
                        texto = stringResource(R.string.lista_canal, item.canalContacto),
                    )
                    Dato(icono = Icons.Outlined.School, texto = item.nivelEducativo)
                    Dato(icono = Icons.Outlined.Category, texto = item.area)
                    item.observaciones?.let {
                        Dato(icono = Icons.AutoMirrored.Outlined.StickyNote2, texto = it)
                    }
                }
            }
        }
    }
}

@Composable
private fun Iniciales(nombre: String) {
    val iniciales = nombre.split(" ").filter { it.isNotBlank() }.take(2)
        .joinToString("") { it.first().uppercase() }
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            iniciales,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

@Composable
private fun Dato(icono: ImageVector, texto: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            icono,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            texto,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun EstadoVacio(
    icono: ImageVector,
    titulo: String,
    detalle: String,
    onReintentar: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            icono,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.outline,
        )
        Spacer(Modifier.size(16.dp))
        Text(titulo, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Spacer(Modifier.size(4.dp))
        Text(
            detalle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.size(16.dp))
        FilledTonalButton(onClick = onReintentar) {
            Text(stringResource(R.string.lista_actualizar))
        }
    }
}
