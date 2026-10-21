package cl.therionlabs.captura.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cursos",
    foreignKeys = [
        ForeignKey(
            entity = AreaEntity::class,
            parentColumns = ["id"],
            childColumns = ["areaId"],
        ),
    ],
    indices = [Index("areaId")],
)
data class CursoEntity(
    @PrimaryKey val id: Int,
    val areaId: Int,
    val nombre: String,
)
