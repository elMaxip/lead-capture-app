package cl.therionlabs.captura.ui.lista

import androidx.annotation.StringRes
import cl.therionlabs.captura.domain.model.Modalidad

data class ProspectoItem(
    val id: String,
    val nombre: String,
    val email: String,
    val telefono: String,
    val nivelEducativo: String,
    val curso: String,
    val area: String,
    val modalidad: Modalidad,
    val canalContacto: String,
    val observaciones: String?,
    val capturadoEn: Long,
)

data class ListaUiState(
    val prospectos: List<ProspectoItem> = emptyList(),
    val sinConexion: Boolean = false,
    val cargando: Boolean = true,
    val actualizando: Boolean = false,
    @param:StringRes val error: Int? = null,
)
