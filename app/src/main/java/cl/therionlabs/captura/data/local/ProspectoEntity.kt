package cl.therionlabs.captura.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import cl.therionlabs.captura.domain.model.CanalContacto
import cl.therionlabs.captura.domain.model.EstadoSync
import cl.therionlabs.captura.domain.model.Modalidad
import cl.therionlabs.captura.domain.model.NivelEducativo

@Entity(
    tableName = "prospectos",
    indices = [Index("estado"), Index("capturadoEn")],
)
data class ProspectoEntity(
    @PrimaryKey val id: String,
    val nombre: String,
    val email: String,
    val telefono: String,
    val nivelEducativo: NivelEducativo,
    val cursoId: Int,
    val modalidad: Modalidad,
    val canalContacto: CanalContacto,
    val observaciones: String?,
    val capturadoEn: Long,
    val estado: EstadoSync,
)
