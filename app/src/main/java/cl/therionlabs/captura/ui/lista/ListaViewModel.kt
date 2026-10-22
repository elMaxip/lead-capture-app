package cl.therionlabs.captura.ui.lista

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.therionlabs.captura.data.network.ConnectivityObserver
import cl.therionlabs.captura.data.repository.ProspectoRepository
import cl.therionlabs.captura.data.sync.SyncScheduler
import cl.therionlabs.captura.domain.model.EstadoSync
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListaViewModel @Inject constructor(
    private val prospectoRepository: ProspectoRepository,
    private val syncScheduler: SyncScheduler,
    connectivityObserver: ConnectivityObserver,
) : ViewModel() {

    val uiState: StateFlow<ListaUiState> = combine(
        prospectoRepository.observarTodos(),
        connectivityObserver.conectado,
    ) { registros, conectado ->
        ListaUiState(
            prospectos = registros.map { (p, cursoNombre) ->
                ProspectoItem(
                    id = p.id,
                    nombre = p.nombre,
                    curso = cursoNombre.orEmpty(),
                    modalidad = p.modalidad,
                    capturadoEn = p.capturadoEn,
                    estado = p.estado,
                )
            },
            pendientes = registros.count { it.prospecto.estado == EstadoSync.PENDIENTE },
            sinConexion = !conectado,
            cargando = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ListaUiState(sinConexion = !connectivityObserver.estaConectadoAhora()),
    )

    private val eventos = Channel<ListaEvento>(Channel.BUFFERED)
    val eventosFlow: Flow<ListaEvento> = eventos.receiveAsFlow()

    fun cargarDatasetFicticio() {
        viewModelScope.launch {
            val cantidad = prospectoRepository.cargarDatasetFicticio()
            if (cantidad > 0) syncScheduler.programar()
            eventos.send(ListaEvento.DatasetCargado(cantidad))
        }
    }
}
