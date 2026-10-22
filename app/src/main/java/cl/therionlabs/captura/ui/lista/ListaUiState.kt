package cl.therionlabs.captura.ui.lista

import cl.therionlabs.captura.domain.model.EstadoSync
import cl.therionlabs.captura.domain.model.Modalidad

data class ProspectoItem(
    val id: String,
    val nombre: String,
    val curso: String,
    val modalidad: Modalidad,
    val capturadoEn: Long,
    val estado: EstadoSync,
)

data class ListaUiState(
    val prospectos: List<ProspectoItem> = emptyList(),
    val pendientes: Int = 0,
    val sinConexion: Boolean = false,
    val cargando: Boolean = true,
)

sealed interface ListaEvento {
    data class DatasetCargado(val cantidad: Int) : ListaEvento
}
