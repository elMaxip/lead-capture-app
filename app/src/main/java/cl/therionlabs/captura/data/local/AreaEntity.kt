package cl.therionlabs.captura.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "areas")
data class AreaEntity(
    @PrimaryKey val id: Int,
    val nombre: String,
)
