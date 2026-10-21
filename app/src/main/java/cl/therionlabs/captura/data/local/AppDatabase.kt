package cl.therionlabs.captura.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import cl.therionlabs.captura.data.remote.dto.CatalogoDto
import kotlinx.serialization.json.Json

@Database(
    entities = [ProspectoEntity::class, AreaEntity::class, CursoEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun prospectoDao(): ProspectoDao
    abstract fun catalogoDao(): CatalogoDao

    class SembrarCatalogo(
        private val context: Context,
        private val json: Json,
    ) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            val texto = context.assets.open(CATALOGO_ASSET).bufferedReader().use { it.readText() }
            val catalogo = json.decodeFromString<CatalogoDto>(texto)

            catalogo.areas.forEach { area ->
                db.insert("areas", SQLiteDatabase.CONFLICT_REPLACE, ContentValues().apply {
                    put("id", area.id)
                    put("nombre", area.nombre)
                })
                area.cursos.forEach { curso ->
                    db.insert("cursos", SQLiteDatabase.CONFLICT_REPLACE, ContentValues().apply {
                        put("id", curso.id)
                        put("areaId", area.id)
                        put("nombre", curso.nombre)
                    })
                }
            }
        }
    }

    companion object {
        const val NOMBRE = "captura.db"
        private const val CATALOGO_ASSET = "catalogo.json"
    }
}
