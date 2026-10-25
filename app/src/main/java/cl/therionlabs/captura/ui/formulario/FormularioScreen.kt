package cl.therionlabs.captura.ui.formulario

import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.automirrored.outlined.StickyNote2
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Interests
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusEventModifierNode
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.therionlabs.captura.BuildConfig
import cl.therionlabs.captura.R
import cl.therionlabs.captura.data.local.AreaEntity
import cl.therionlabs.captura.data.local.CursoEntity
import cl.therionlabs.captura.domain.model.CanalContacto
import cl.therionlabs.captura.domain.model.Modalidad
import cl.therionlabs.captura.domain.model.NivelEducativo
import cl.therionlabs.captura.domain.validation.ProspectoValidator
import cl.therionlabs.captura.ui.components.DropdownField
import cl.therionlabs.captura.ui.components.ErrorAnimado
import cl.therionlabs.captura.ui.components.OfflineBanner
import cl.therionlabs.captura.ui.components.ResumenPendientes
import cl.therionlabs.captura.ui.lista.formatearFecha
import cl.therionlabs.captura.util.rememberHaptics
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioScreen(viewModel: FormularioViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val resumen by viewModel.resumen.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current
    val haptics = rememberHaptics()
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    val focus = remember { Campo.entries.associateWith { FocusRequester() } }

    LaunchedEffect(viewModel) {
        viewModel.eventosFlow.collect { evento ->
            when (evento) {
                is FormularioEvento.Guardado -> {
                    haptics.exito()
                    scrollState.animateScrollTo(0)
                    snackbarHostState.showSnackbar(
                        resources.getString(
                            if (evento.sinConexion) R.string.guardado_sin_conexion else R.string.guardado
                        )
                    )
                }
                is FormularioEvento.ErrorValidacion -> {
                    haptics.error()
                    focus.getValue(evento.primerCampo).requestFocus()
                }
                is FormularioEvento.DatasetCargado -> snackbarHostState.showSnackbar(
                    if (evento.cantidad > 0) {
                        resources.getQuantityString(R.plurals.dataset_cargado, evento.cantidad, evento.cantidad)
                    } else {
                        resources.getString(R.string.dataset_no_disponible)
                    }
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.formulario_titulo)) },
                actions = {
                    if (BuildConfig.DEBUG) {
                        IconButton(onClick = viewModel::cargarDatasetFicticio) {
                            Icon(
                                Icons.Outlined.Science,
                                contentDescription = stringResource(R.string.cargar_dataset),
                            )
                        }
                    }
                    IconButton(onClick = viewModel::cerrarSesion) {
                        Icon(
                            Icons.AutoMirrored.Outlined.Logout,
                            contentDescription = stringResource(R.string.cerrar_sesion),
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding),
        ) {
            OfflineBanner(visible = resumen.sinConexion)
            if (!resumen.cargando) ResumenPendientes(pendientes = resumen.pendientes)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .verticalScroll(scrollState)
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 600.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    SeccionDatos(
                        nombre = state.nombre,
                        email = state.email,
                        telefono = state.telefono,
                        nivelEducativo = state.nivelEducativo,
                        errorNombre = state.errorVisible(Campo.NOMBRE),
                        errorEmail = state.errorVisible(Campo.EMAIL),
                        errorTelefono = state.errorVisible(Campo.TELEFONO),
                        errorNivel = state.errorVisible(Campo.NIVEL_EDUCATIVO),
                        viewModel = viewModel,
                        focus = focus,
                        onSiguiente = { focusManager.clearFocus() },
                    )
                    SeccionIntereses(
                        areas = state.areas,
                        areaSeleccionada = state.areaSeleccionada,
                        cursos = state.cursos,
                        cursoSeleccionado = state.cursoSeleccionado,
                        hayArea = state.areaId != null,
                        modalidad = state.modalidad,
                        canalContacto = state.canalContacto,
                        errorArea = state.errorVisible(Campo.AREA),
                        errorCurso = state.errorVisible(Campo.CURSO),
                        errorModalidad = state.errorVisible(Campo.MODALIDAD),
                        errorCanal = state.errorVisible(Campo.CANAL_CONTACTO),
                        viewModel = viewModel,
                        focus = focus,
                    )
                    SeccionOperativa(
                        observaciones = state.observaciones,
                        errorObservaciones = state.errorVisible(Campo.OBSERVACIONES),
                        viewModel = viewModel,
                        focus = focus,
                    )
                    BotonGuardar(
                        habilitado = state.esValido && !state.guardando,
                        guardando = state.guardando,
                        onGuardar = {
                            focusManager.clearFocus()
                            viewModel.guardar()
                        },
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun SeccionDatos(
    nombre: String,
    email: String,
    telefono: String,
    nivelEducativo: NivelEducativo?,
    errorNombre: String?,
    errorEmail: String?,
    errorTelefono: String?,
    errorNivel: String?,
    viewModel: FormularioViewModel,
    focus: Map<Campo, FocusRequester>,
    onSiguiente: () -> Unit,
) {
    Seccion(stringResource(R.string.seccion_datos), Icons.Outlined.Badge) {
        OutlinedTextField(
            value = nombre,
            onValueChange = viewModel::onNombreChange,
            label = { Text(stringResource(R.string.campo_nombre)) },
            singleLine = true,
            isError = errorNombre != null,
            supportingText = { ErrorAnimado(errorNombre) },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .campo(Campo.NOMBRE, focus, viewModel::marcarTocado),
        )
        OutlinedTextField(
            value = email,
            onValueChange = viewModel::onEmailChange,
            label = { Text(stringResource(R.string.campo_email)) },
            singleLine = true,
            isError = errorEmail != null,
            supportingText = { ErrorAnimado(errorEmail) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .campo(Campo.EMAIL, focus, viewModel::marcarTocado),
        )
        OutlinedTextField(
            value = telefono,
            onValueChange = viewModel::onTelefonoChange,
            label = { Text(stringResource(R.string.campo_telefono)) },
            prefix = { Text(stringResource(R.string.campo_telefono_prefijo)) },
            singleLine = true,
            isError = errorTelefono != null,
            supportingText = { ErrorAnimado(errorTelefono) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { onSiguiente() }),
            modifier = Modifier
                .fillMaxWidth()
                .campo(Campo.TELEFONO, focus, viewModel::marcarTocado),
        )
        DropdownField(
            label = stringResource(R.string.campo_nivel),
            opciones = NivelEducativo.entries,
            seleccion = nivelEducativo,
            etiqueta = { it.etiqueta },
            onSeleccion = viewModel::onNivelEducativo,
            onCerrar = { viewModel.marcarTocado(Campo.NIVEL_EDUCATIVO) },
            error = errorNivel,
            focusRequester = focus.getValue(Campo.NIVEL_EDUCATIVO),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun SeccionIntereses(
    areas: List<AreaEntity>,
    areaSeleccionada: AreaEntity?,
    cursos: List<CursoEntity>,
    cursoSeleccionado: CursoEntity?,
    hayArea: Boolean,
    modalidad: Modalidad?,
    canalContacto: CanalContacto?,
    errorArea: String?,
    errorCurso: String?,
    errorModalidad: String?,
    errorCanal: String?,
    viewModel: FormularioViewModel,
    focus: Map<Campo, FocusRequester>,
) {
    Seccion(stringResource(R.string.seccion_intereses), Icons.Outlined.Interests) {
        DropdownField(
            label = stringResource(R.string.campo_area),
            opciones = areas,
            seleccion = areaSeleccionada,
            etiqueta = { it.nombre },
            onSeleccion = viewModel::onArea,
            onCerrar = { viewModel.marcarTocado(Campo.AREA) },
            error = errorArea,
            focusRequester = focus.getValue(Campo.AREA),
            modifier = Modifier.fillMaxWidth(),
        )
        DropdownField(
            label = stringResource(R.string.campo_curso),
            opciones = cursos,
            seleccion = cursoSeleccionado,
            etiqueta = { it.nombre },
            onSeleccion = viewModel::onCurso,
            onCerrar = { viewModel.marcarTocado(Campo.CURSO) },
            error = errorCurso,
            enabled = hayArea,
            placeholder = if (!hayArea) stringResource(R.string.campo_curso_sin_area) else null,
            focusRequester = focus.getValue(Campo.CURSO),
            modifier = Modifier.fillMaxWidth(),
        )
        Selector(
            titulo = stringResource(R.string.campo_modalidad),
            opciones = Modalidad.entries,
            seleccion = modalidad,
            etiqueta = { it.etiqueta },
            onSeleccion = viewModel::onModalidad,
            error = errorModalidad,
            focusRequester = focus.getValue(Campo.MODALIDAD),
        )
        Spacer(Modifier.height(4.dp))
        Selector(
            titulo = stringResource(R.string.campo_canal),
            opciones = CanalContacto.entries,
            seleccion = canalContacto,
            etiqueta = { it.etiqueta },
            onSeleccion = viewModel::onCanalContacto,
            error = errorCanal,
            focusRequester = focus.getValue(Campo.CANAL_CONTACTO),
        )
    }
}

@Composable
private fun SeccionOperativa(
    observaciones: String,
    errorObservaciones: String?,
    viewModel: FormularioViewModel,
    focus: Map<Campo, FocusRequester>,
) {
    val ahora by produceState(System.currentTimeMillis()) {
        while (true) {
            delay(15_000)
            value = System.currentTimeMillis()
        }
    }

    Seccion(stringResource(R.string.seccion_operativa), Icons.AutoMirrored.Outlined.StickyNote2) {
        OutlinedTextField(
            value = observaciones,
            onValueChange = viewModel::onObservacionesChange,
            label = { Text(stringResource(R.string.campo_observaciones)) },
            minLines = 3,
            maxLines = 6,
            isError = errorObservaciones != null,
            supportingText = {
                Row {
                    ErrorAnimado(errorObservaciones, Modifier.weight(1f))
                    Spacer(Modifier.size(8.dp))
                    Text(
                        stringResource(
                            R.string.campo_contador,
                            observaciones.trim().length,
                            ProspectoValidator.OBSERVACIONES_MAX,
                        )
                    )
                }
            },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier = Modifier
                .fillMaxWidth()
                .campo(Campo.OBSERVACIONES, focus, viewModel::marcarTocado),
        )
        OutlinedTextField(
            value = formatearFecha(ahora),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.campo_fecha_captura)) },
            leadingIcon = { Icon(Icons.Outlined.Schedule, contentDescription = null) },
            supportingText = { Text(stringResource(R.string.campo_fecha_captura_ayuda)) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun <T> Selector(
    titulo: String,
    opciones: List<T>,
    seleccion: T?,
    etiqueta: (T) -> String,
    onSeleccion: (T) -> Unit,
    error: String?,
    focusRequester: FocusRequester,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .focusable(),
    ) {
        Text(
            titulo,
            style = MaterialTheme.typography.labelLarge,
            color = if (error != null) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            opciones.forEachIndexed { index, opcion ->
                SegmentedButton(
                    selected = opcion == seleccion,
                    onClick = { onSeleccion(opcion) },
                    shape = SegmentedButtonDefaults.itemShape(index, opciones.size),
                    label = { Text(etiqueta(opcion), maxLines = 1) },
                )
            }
        }
        ErrorAnimado(error, Modifier.padding(start = 16.dp, top = 4.dp))
    }
}

@Composable
private fun BotonGuardar(habilitado: Boolean, guardando: Boolean, onGuardar: () -> Unit) {
    Box {
        Button(
            onClick = onGuardar,
            enabled = habilitado,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
        ) {
            if (guardando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                Icon(Icons.Outlined.Save, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text(stringResource(R.string.guardar))
            }
        }
        if (!habilitado && !guardando) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(interactionSource = null, indication = null, onClick = onGuardar),
            )
        }
    }
}

@Composable
private fun Seccion(titulo: String, icono: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp),
            ) {
                Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    titulo,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            content()
        }
    }
}

private fun Modifier.campo(
    campo: Campo,
    focus: Map<Campo, FocusRequester>,
    onTocado: (Campo) -> Unit,
): Modifier = focusRequester(focus.getValue(campo)) then MarcarTocadoElement(campo, onTocado)

/** Marca el campo como tocado cuando pierde el foco. Modifier.Node en vez de `composed` para no recrearlo en cada recomposición. */
private data class MarcarTocadoElement(
    val campo: Campo,
    val onTocado: (Campo) -> Unit,
) : ModifierNodeElement<MarcarTocadoNode>() {
    override fun create() = MarcarTocadoNode(campo, onTocado)

    override fun update(node: MarcarTocadoNode) {
        node.campo = campo
        node.onTocado = onTocado
    }
}

private class MarcarTocadoNode(
    var campo: Campo,
    var onTocado: (Campo) -> Unit,
) : Modifier.Node(), FocusEventModifierNode {
    private var tuvoFoco = false

    override fun onFocusEvent(focusState: FocusState) {
        if (tuvoFoco && !focusState.isFocused) onTocado(campo)
        tuvoFoco = focusState.isFocused
    }
}
