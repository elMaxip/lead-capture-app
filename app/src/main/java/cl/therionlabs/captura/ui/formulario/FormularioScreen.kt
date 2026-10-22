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
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.StickyNote2
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Interests
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Schedule
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.therionlabs.captura.R
import cl.therionlabs.captura.domain.model.CanalContacto
import cl.therionlabs.captura.domain.model.Modalidad
import cl.therionlabs.captura.domain.model.NivelEducativo
import cl.therionlabs.captura.domain.validation.ProspectoValidator
import cl.therionlabs.captura.ui.components.DropdownField
import cl.therionlabs.captura.ui.components.ErrorAnimado
import cl.therionlabs.captura.ui.lista.formatearFecha
import cl.therionlabs.captura.util.rememberHaptics
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioScreen(
    onVolver: () -> Unit,
    viewModel: FormularioViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
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
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.formulario_titulo)) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.volver),
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
                .consumeWindowInsets(padding)
                .imePadding()
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                SeccionDatos(state, viewModel, focus, onSiguiente = { focusManager.clearFocus() })
                SeccionIntereses(state, viewModel, focus)
                SeccionOperativa(state, viewModel, focus)
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

@Composable
private fun SeccionDatos(
    state: FormularioUiState,
    viewModel: FormularioViewModel,
    focus: Map<Campo, FocusRequester>,
    onSiguiente: () -> Unit,
) {
    Seccion(stringResource(R.string.seccion_datos), Icons.Outlined.Badge) {
        OutlinedTextField(
            value = state.nombre,
            onValueChange = viewModel::onNombreChange,
            label = { Text(stringResource(R.string.campo_nombre)) },
            singleLine = true,
            isError = state.errorVisible(Campo.NOMBRE) != null,
            supportingText = { ErrorAnimado(state.errorVisible(Campo.NOMBRE)) },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .campo(Campo.NOMBRE, focus, viewModel::marcarTocado),
        )
        OutlinedTextField(
            value = state.email,
            onValueChange = viewModel::onEmailChange,
            label = { Text(stringResource(R.string.campo_email)) },
            singleLine = true,
            isError = state.errorVisible(Campo.EMAIL) != null,
            supportingText = { ErrorAnimado(state.errorVisible(Campo.EMAIL)) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .campo(Campo.EMAIL, focus, viewModel::marcarTocado),
        )
        OutlinedTextField(
            value = state.telefono,
            onValueChange = viewModel::onTelefonoChange,
            label = { Text(stringResource(R.string.campo_telefono)) },
            prefix = { Text(stringResource(R.string.campo_telefono_prefijo)) },
            singleLine = true,
            isError = state.errorVisible(Campo.TELEFONO) != null,
            supportingText = { ErrorAnimado(state.errorVisible(Campo.TELEFONO)) },
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
            seleccion = state.nivelEducativo,
            etiqueta = { it.etiqueta },
            onSeleccion = viewModel::onNivelEducativo,
            onCerrar = { viewModel.marcarTocado(Campo.NIVEL_EDUCATIVO) },
            error = state.errorVisible(Campo.NIVEL_EDUCATIVO),
            focusRequester = focus.getValue(Campo.NIVEL_EDUCATIVO),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun SeccionIntereses(
    state: FormularioUiState,
    viewModel: FormularioViewModel,
    focus: Map<Campo, FocusRequester>,
) {
    Seccion(stringResource(R.string.seccion_intereses), Icons.Outlined.Interests) {
        DropdownField(
            label = stringResource(R.string.campo_area),
            opciones = state.areas,
            seleccion = state.areaSeleccionada,
            etiqueta = { it.nombre },
            onSeleccion = viewModel::onArea,
            onCerrar = { viewModel.marcarTocado(Campo.AREA) },
            error = state.errorVisible(Campo.AREA),
            focusRequester = focus.getValue(Campo.AREA),
            modifier = Modifier.fillMaxWidth(),
        )
        DropdownField(
            label = stringResource(R.string.campo_curso),
            opciones = state.cursos,
            seleccion = state.cursoSeleccionado,
            etiqueta = { it.nombre },
            onSeleccion = viewModel::onCurso,
            onCerrar = { viewModel.marcarTocado(Campo.CURSO) },
            error = state.errorVisible(Campo.CURSO),
            enabled = state.areaId != null,
            placeholder = if (state.areaId == null) stringResource(R.string.campo_curso_sin_area) else null,
            focusRequester = focus.getValue(Campo.CURSO),
            modifier = Modifier.fillMaxWidth(),
        )
        Selector(
            titulo = stringResource(R.string.campo_modalidad),
            opciones = Modalidad.entries,
            seleccion = state.modalidad,
            etiqueta = { it.etiqueta },
            onSeleccion = viewModel::onModalidad,
            error = state.errorVisible(Campo.MODALIDAD),
            focusRequester = focus.getValue(Campo.MODALIDAD),
        )
        Spacer(Modifier.height(4.dp))
        Selector(
            titulo = stringResource(R.string.campo_canal),
            opciones = CanalContacto.entries,
            seleccion = state.canalContacto,
            etiqueta = { it.etiqueta },
            onSeleccion = viewModel::onCanalContacto,
            error = state.errorVisible(Campo.CANAL_CONTACTO),
            focusRequester = focus.getValue(Campo.CANAL_CONTACTO),
        )
    }
}

@Composable
private fun SeccionOperativa(
    state: FormularioUiState,
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
            value = state.observaciones,
            onValueChange = viewModel::onObservacionesChange,
            label = { Text(stringResource(R.string.campo_observaciones)) },
            minLines = 3,
            maxLines = 6,
            isError = state.errorVisible(Campo.OBSERVACIONES) != null,
            supportingText = {
                Row {
                    ErrorAnimado(state.errorVisible(Campo.OBSERVACIONES), Modifier.weight(1f))
                    Spacer(Modifier.size(8.dp))
                    Text(
                        stringResource(
                            R.string.campo_contador,
                            state.observaciones.trim().length,
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
): Modifier = composed {
    var tuvoFoco by remember { mutableStateOf(false) }
    focusRequester(focus.getValue(campo)).onFocusChanged {
        if (tuvoFoco && !it.isFocused) onTocado(campo)
        tuvoFoco = it.isFocused
    }
}
