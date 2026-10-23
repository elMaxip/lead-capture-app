package cl.therionlabs.captura.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProspectoDao {

    @Query("SELECT COUNT(*) FROM prospectos WHERE estado = 'PENDIENTE'")
    fun observarPendientes(): Flow<Int>

    @Insert
    suspend fun insertar(prospecto: ProspectoEntity)

    @Insert
    suspend fun insertar(prospectos: List<ProspectoEntity>)

    @Query("SELECT * FROM prospectos WHERE estado = 'PENDIENTE' ORDER BY capturadoEn")
    suspend fun pendientes(): List<ProspectoEntity>

    @Query("UPDATE prospectos SET estado = 'SINCRONIZADO' WHERE id IN (:ids)")
    suspend fun marcarSincronizados(ids: List<String>)
}
