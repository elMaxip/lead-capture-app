package cl.therionlabs.captura.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CatalogoDto(
    val areas: List<AreaDto>,
)

@Serializable
data class AreaDto(
    val id: Int,
    val nombre: String,
    val cursos: List<CursoDto>,
)

@Serializable
data class CursoDto(
    val id: Int,
    val nombre: String,
)
