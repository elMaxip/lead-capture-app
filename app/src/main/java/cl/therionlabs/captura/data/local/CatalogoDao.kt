package cl.therionlabs.captura.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface CatalogoDao {

    @Query("SELECT * FROM areas ORDER BY id")
    fun observarAreas(): Flow<List<AreaEntity>>

    @Query("SELECT * FROM cursos WHERE areaId = :areaId ORDER BY id")
    fun observarCursos(areaId: Int): Flow<List<CursoEntity>>

    @Upsert
    suspend fun upsertAreas(areas: List<AreaEntity>)

    @Upsert
    suspend fun upsertCursos(cursos: List<CursoEntity>)

    @Transaction
    suspend fun reemplazarCatalogo(areas: List<AreaEntity>, cursos: List<CursoEntity>) {
        upsertAreas(areas)
        upsertCursos(cursos)
    }
}
