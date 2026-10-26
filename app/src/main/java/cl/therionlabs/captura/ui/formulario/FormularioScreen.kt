package cl.therionlabs.captura.ui.formulario

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
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
import cl.therionlabs.captura.ui.components.OfflineBanner
import cl.therionlabs.captura.ui.components.ResumenPendientes
import cl.therionlabs.captura.ui.theme.CapturaTheme
import cl.therionlabs.captura.util.rememberHaptics


@Composable
fun FormularioScreen(viewModel: FormularioViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val resumen by viewModel.resumen.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current
    val haptics = rememberHaptics()
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

    FormularioScreenContent(
        state = state,
        resumen = resumen,
        onAction = viewModel::onAction,
        onGuardar = viewModel::guardar,
        onCargarDataset = viewModel::cargarDatasetFicticio,
        onCerrarSesion = viewModel::cerrarSesion,
        snackbarHostState = snackbarHostState,
        scrollState = scrollState,
        focus = focus,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioScreenContent(
    state: FormularioUiState,
    resumen: ResumenSync,
    onAction: (FormularioAccion) -> Unit,
    onGuardar: () -> Unit,
    onCargarDataset: () -> Unit,
    onCerrarSesion: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    scrollState: ScrollState = rememberScrollState(),
    focus: Map<Campo, FocusRequester> = remember { Campo.entries.associateWith { FocusRequester() } },
) {
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.formulario_titulo)) },
                actions = {
                    if (BuildConfig.DEBUG) {
                        IconButton(onClick = onCargarDataset) {
                            Icon(
                                Icons.Outlined.Science,
                                contentDescription = stringResource(R.string.cargar_dataset),
                            )
                        }
                    }
                    IconButton(onClick = onCerrarSesion) {
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
                        focus = focus,
                        onSiguiente = { focusManager.clearFocus() },
                        onAction = onAction,
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
                        focus = focus,
                        onAction = onAction,
                    )
                    SeccionOperativa(
                        observaciones = state.observaciones,
                        errorObservaciones = state.errorVisible(Campo.OBSERVACIONES),
                        focus = focus,
                        onAction = onAction,
                    )
                    BotonGuardar(
                        habilitado = state.esValido && !state.guardando,
                        guardando = state.guardando,
                        onGuardar = {
                            focusManager.clearFocus()
                            onGuardar()
                        },
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
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

@Preview(showBackground = true)
@Composable
private fun FormularioScreenPreview() {
    val area = AreaEntity(id = 1, nombre = "Tecnología")
    val curso = CursoEntity(id = 1, areaId = area.id, nombre = "Desarrollo Android")
    CapturaTheme {
        FormularioScreenContent(
            state = FormularioUiState(
                nombre = "John Doe",
                email = "johndoe@gmail.com",
                telefono = "12345678",
                nivelEducativo = NivelEducativo.UNIVERSITARIA,
                areaId = area.id,
                cursoId = curso.id,
                modalidad = Modalidad.ONLINE,
                canalContacto = CanalContacto.WHATSAPP,
                areas = listOf(area),
                cursos = listOf(curso),
            ),
            resumen = ResumenSync(pendientes = 3, cargando = false),
            onAction = {},
            onGuardar = {},
            onCargarDataset = {},
            onCerrarSesion = {},
        )
    }
}
