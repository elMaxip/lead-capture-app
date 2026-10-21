package cl.therionlabs.captura.data.remote.dto

import cl.therionlabs.captura.data.local.ProspectoEntity
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class ProspectoDto(
    val id: String,
    val nombre: String,
    val email: String,
    val telefono: String,
    val nivelEducativo: String,
    val cursoId: Int,
    val modalidad: String,
    val canalContacto: String,
    val observaciones: String?,
    val capturadoEn: String,
)

fun ProspectoEntity.toDto() = ProspectoDto(
    id = id,
    nombre = nombre,
    email = email,
    telefono = telefono,
    nivelEducativo = nivelEducativo.name,
    cursoId = cursoId,
    modalidad = modalidad.name,
    canalContacto = canalContacto.name,
    observaciones = observaciones,
    capturadoEn = Instant.ofEpochMilli(capturadoEn).toString(),
)
