package cl.therionlabs.captura.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SyncRequestDto(
    val prospectos: List<ProspectoDto>,
)

@Serializable
data class SyncResponseDto(
    val sincronizados: List<String>,
    val rechazados: List<RechazadoDto>,
)

@Serializable
data class RechazadoDto(
    val id: String,
    val errores: Map<String, String>,
)
