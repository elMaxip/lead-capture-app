package cl.therionlabs.captura.ui.formulario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.therionlabs.captura.data.local.AreaEntity
import cl.therionlabs.captura.data.local.CursoEntity
import cl.therionlabs.captura.data.local.ProspectoEntity
import cl.therionlabs.captura.data.network.ConnectivityObserver
import cl.therionlabs.captura.data.repository.AuthRepository
import cl.therionlabs.captura.data.repository.CatalogoRepository
import cl.therionlabs.captura.data.repository.ProspectoRepository
import cl.therionlabs.captura.data.sync.SyncScheduler
import cl.therionlabs.captura.domain.model.CanalContacto
import cl.therionlabs.captura.domain.model.EstadoSync
import cl.therionlabs.captura.domain.model.Modalidad
import cl.therionlabs.captura.domain.model.NivelEducativo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class FormularioViewModel @Inject constructor(
    private val prospectoRepository: ProspectoRepository,
    private val syncScheduler: SyncScheduler,
    private val authRepository: AuthRepository,
    catalogoRepository: CatalogoRepository,
    connectivityObserver: ConnectivityObserver,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FormularioUiState())
    val uiState: StateFlow<FormularioUiState> = _uiState.asStateFlow()

    private val eventos = Channel<FormularioEvento>(Channel.BUFFERED)
    val eventosFlow: Flow<FormularioEvento> = eventos.receiveAsFlow()

    private val conectado: StateFlow<Boolean> = connectivityObserver.conectado.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        connectivityObserver.estaConectadoAhora(),
    )

    val resumen: StateFlow<ResumenSync> = combine(
        prospectoRepository.observarPendientes(),
        conectado,
    ) { pendientes, conectado ->
        ResumenSync(pendientes = pendientes, sinConexion = !conectado, cargando = false)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ResumenSync(sinConexion = !connectivityObserver.estaConectadoAhora()),
    )

    init {
        viewModelScope.launch {
            catalogoRepository.observarAreas().collect { areas ->
                _uiState.update { it.copy(areas = areas) }
            }
        }
        viewModelScope.launch {
            _uiState.map { it.areaId }
                .distinctUntilChanged()
                .flatMapLatest { areaId ->
                    if (areaId == null) flowOf(emptyList()) else catalogoRepository.observarCursos(areaId)
                }
                .collect { cursos -> _uiState.update { it.copy(cursos = cursos) } }
        }
    }

    fun onAction(action: FormularioAccion) {
        when (action) {
            is FormularioAccion.OnNameChange -> onNameChange(action.value)
            is FormularioAccion.OnEmailChange -> onEmailChange(action.value)
            is FormularioAccion.OnPhoneChange -> onPhoneChange(action.value)
            is FormularioAccion.OnEducationChange -> onEducationChange(action.value)
            is FormularioAccion.OnAreaChange -> onAreaChange(action.value)
            is FormularioAccion.OnCourseChange -> onCourseChange(action.value)
            is FormularioAccion.OnModeChange -> onModeChange(action.value)
            is FormularioAccion.OnContactChange -> onContactChange(action.value)
            is FormularioAccion.OnObservationsChange -> onObservationChange(action.value)
            is FormularioAccion.FieldTouched -> touchField(action.value)
        }
    }

    private fun onNameChange(value: String) {
        editar(Campo.NOMBRE) { copy(nombre = value) }
    }

    private fun onEmailChange(value: String) = editar(Campo.EMAIL) { copy(email = value.trim()) }

    private fun onPhoneChange(valor: String) = editar(Campo.TELEFONO) {
        copy(telefono = valor.filter(Char::isDigit).take(TELEFONO_DIGITOS))
    }

    private fun onEducationChange(value: NivelEducativo) = editar(Campo.NIVEL_EDUCATIVO) { copy(nivelEducativo = value) }

    private fun onAreaChange(value: AreaEntity) = editar(Campo.AREA) {
        if (areaId == value.id) this else copy(areaId = value.id, cursoId = null, cursos = emptyList())
    }

    private fun onCourseChange(value: CursoEntity) = editar(Campo.CURSO) { copy(cursoId = value.id) }

    private fun onModeChange(value: Modalidad) = editar(Campo.MODALIDAD) { copy(modalidad = value) }

    private fun onContactChange(value: CanalContacto) = editar(Campo.CANAL_CONTACTO) { copy(canalContacto = value) }

    private fun onObservationChange(value: String) = editar(Campo.OBSERVACIONES) { copy(observaciones = value) }

    fun touchField(campo: Campo) {
        _uiState.update { if (campo in it.tocados) it else it.copy(tocados = it.tocados + campo) }
    }

    fun guardar() {
        val actual = _uiState.value
        if (actual.guardando) return

        if (!actual.esValido) {
            _uiState.update { it.copy(tocados = Campo.entries.toSet()) }
            val primero = Campo.entries.first { it in actual.errores }
            eventos.trySend(FormularioEvento.ErrorValidacion(primero))
            return
        }

        _uiState.update { it.copy(guardando = true) }
        viewModelScope.launch {
            val prospecto = ProspectoEntity(
                id = UUID.randomUUID().toString(),
                nombre = actual.nombre.trim(),
                email = actual.email.trim(),
                telefono = PREFIJO_MOVIL + actual.telefono,
                nivelEducativo = requireNotNull(actual.nivelEducativo),
                cursoId = requireNotNull(actual.cursoId),
                modalidad = requireNotNull(actual.modalidad),
                canalContacto = requireNotNull(actual.canalContacto),
                observaciones = actual.observaciones.trim().ifEmpty { null },
                capturadoEn = System.currentTimeMillis(),
                estado = EstadoSync.PENDIENTE,
            )
            prospectoRepository.guardar(prospecto)
            syncScheduler.programar()

            _uiState.update { FormularioUiState(areas = it.areas) }
            eventos.send(FormularioEvento.Guardado(sinConexion = !conectado.value))
        }
    }

    fun cargarDatasetFicticio() {
        viewModelScope.launch {
            val cantidad = prospectoRepository.cargarDatasetFicticio()
            if (cantidad > 0) syncScheduler.programar()
            eventos.send(FormularioEvento.DatasetCargado(cantidad))
        }
    }

    fun cerrarSesion() = authRepository.cerrarSesion()

    private inline fun editar(campo: Campo, crossinline cambio: FormularioUiState.() -> FormularioUiState) {
        _uiState.update { it.cambio().copy(tocados = it.tocados + campo) }
    }

    private companion object {
        const val PREFIJO_MOVIL = "+569"
        const val TELEFONO_DIGITOS = 8
    }
}
