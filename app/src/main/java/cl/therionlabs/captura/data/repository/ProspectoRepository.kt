package cl.therionlabs.captura.data.repository

import android.content.Context
import cl.therionlabs.captura.data.local.ProspectoDao
import cl.therionlabs.captura.data.local.ProspectoEntity
import cl.therionlabs.captura.data.remote.ApiService
import cl.therionlabs.captura.data.remote.dto.ProspectoConsolidadoDto
import cl.therionlabs.captura.data.remote.dto.SyncRequestDto
import cl.therionlabs.captura.data.remote.dto.SyncResponseDto
import cl.therionlabs.captura.data.remote.dto.toDto
import cl.therionlabs.captura.domain.model.CanalContacto
import cl.therionlabs.captura.domain.model.EstadoSync
import cl.therionlabs.captura.domain.model.Modalidad
import cl.therionlabs.captura.domain.model.NivelEducativo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.FileNotFoundException
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProspectoRepository @Inject constructor(
    private val dao: ProspectoDao,
    private val api: ApiService,
    private val json: Json,
    @param:ApplicationContext private val context: Context,
) {

    fun observarPendientes(): Flow<Int> = dao.observarPendientes()

    suspend fun guardar(prospecto: ProspectoEntity) = dao.insertar(prospecto)

    suspend fun pendientes(): List<ProspectoEntity> = dao.pendientes()

    suspend fun marcarSincronizados(ids: List<String>) {
        if (ids.isNotEmpty()) dao.marcarSincronizados(ids)
    }

    suspend fun enviar(lote: List<ProspectoEntity>): SyncResponseDto =
        api.sincronizar(SyncRequestDto(lote.map { it.toDto() }))

    /** Prospectos ya consolidados en el servidor (perfil Administrativo). */
    suspend fun listarConsolidados(): List<ProspectoConsolidadoDto> = api.listarProspectos()

    suspend fun cargarDatasetFicticio(): Int = withContext(Dispatchers.IO) {
        val texto = try {
            context.assets.open(DATASET_ASSET).bufferedReader().use { it.readText() }
        } catch (_: FileNotFoundException) {
            return@withContext 0
        }
        val ahora = System.currentTimeMillis()
        val prospectos = json.decodeFromString<List<ProspectoFicticio>>(texto)
            .mapIndexed { i, p ->
                ProspectoEntity(
                    id = UUID.randomUUID().toString(),
                    nombre = p.nombre,
                    email = p.email,
                    telefono = p.telefono,
                    nivelEducativo = p.nivelEducativo,
                    cursoId = p.cursoId,
                    modalidad = p.modalidad,
                    canalContacto = p.canalContacto,
                    observaciones = p.observaciones,
                    // Escalonados para que se vean en orden en la lista.
                    capturadoEn = ahora - TimeUnit.MINUTES.toMillis(i.toLong()),
                    estado = EstadoSync.PENDIENTE,
                )
            }
        dao.insertar(prospectos)
        prospectos.size
    }

    @Serializable
    private data class ProspectoFicticio(
        val nombre: String,
        val email: String,
        val telefono: String,
        val nivelEducativo: NivelEducativo,
        val cursoId: Int,
        val modalidad: Modalidad,
        val canalContacto: CanalContacto,
        val observaciones: String? = null,
    )

    private companion object {
        const val DATASET_ASSET = "prospectos_ficticios.json"
    }
}
