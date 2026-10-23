package cl.therionlabs.captura.ui.lista

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.therionlabs.captura.R
import cl.therionlabs.captura.data.network.ConnectivityObserver
import cl.therionlabs.captura.data.remote.dto.ProspectoConsolidadoDto
import cl.therionlabs.captura.data.repository.AuthRepository
import cl.therionlabs.captura.data.repository.ProspectoRepository
import cl.therionlabs.captura.domain.model.CanalContacto
import cl.therionlabs.captura.domain.model.Modalidad
import cl.therionlabs.captura.domain.model.NivelEducativo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.time.Instant
import javax.inject.Inject

/** Lista los prospectos consolidados en el servidor (perfil Administrativo). */
@HiltViewModel
class ListaViewModel @Inject constructor(
    private val prospectoRepository: ProspectoRepository,
    private val authRepository: AuthRepository,
    connectivityObserver: ConnectivityObserver,
) : ViewModel() {

    private val estado = MutableStateFlow(ListaUiState())

    val uiState: StateFlow<ListaUiState> = combine(
        estado,
        connectivityObserver.conectado,
    ) { estado, conectado ->
        estado.copy(sinConexion = !conectado)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ListaUiState(sinConexion = !connectivityObserver.estaConectadoAhora()),
    )

    init {
        cargar()
    }

    fun actualizar() = cargar()

    fun cerrarSesion() = authRepository.cerrarSesion()

    private fun cargar() {
        if (estado.value.actualizando) return
        estado.update { it.copy(actualizando = true) }
        viewModelScope.launch {
            val error = try {
                val prospectos = prospectoRepository.listarConsolidados().map { it.toItem() }
                estado.update { it.copy(prospectos = prospectos) }
                null
            } catch (e: CancellationException) {
                throw e
            } catch (_: IOException) {
                R.string.lista_error_conexion
            } catch (_: HttpException) {
                R.string.lista_error_servidor
            }
            estado.update { it.copy(cargando = false, actualizando = false, error = error) }
        }
    }

    private fun ProspectoConsolidadoDto.toItem() = ProspectoItem(
        id = id,
        nombre = nombre,
        email = email,
        telefono = telefono,
        nivelEducativo = NivelEducativo.entries.find { it.name == nivelEducativo }?.etiqueta ?: nivelEducativo,
        curso = cursoNombre,
        area = areaNombre,
        modalidad = Modalidad.entries.find { it.name == modalidad } ?: Modalidad.PRESENCIAL,
        canalContacto = CanalContacto.entries.find { it.name == canalContacto }?.etiqueta ?: canalContacto,
        observaciones = observaciones,
        capturadoEn = Instant.parse(capturadoEn).toEpochMilli(),
    )
}
