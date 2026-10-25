package cl.therionlabs.captura.data.sync

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import cl.therionlabs.captura.data.repository.CatalogoRepository
import cl.therionlabs.captura.data.repository.ProspectoRepository
import cl.therionlabs.captura.data.session.SessionManager
import cl.therionlabs.captura.domain.model.Rol
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val prospectoRepository: ProspectoRepository,
    private val catalogoRepository: CatalogoRepository,
    private val sessionManager: SessionManager,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (sessionManager.sesion.value?.rol != Rol.CAPTADOR) return Result.success()

        try {
            catalogoRepository.refrescar()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo refrescar el catálogo: ${e.javaClass.simpleName}")
        }

        return try {
            prospectoRepository.pendientes().chunked(TAMANO_LOTE).forEach { lote ->
                val respuesta = prospectoRepository.enviar(lote)
                prospectoRepository.marcarSincronizados(respuesta.sincronizados)
                if (respuesta.rechazados.isNotEmpty()) {
                    Log.w(TAG, "${respuesta.rechazados.size} registros rechazados quedan pendientes")
                }
            }
            Result.success()
        } catch (e: IOException) {
            Result.retry()
        } catch (e: HttpException) {
            if (e.code() >= 500) Result.retry() else Result.failure()
        }
    }

    private companion object {
        const val TAG = "SyncWorker"
        const val TAMANO_LOTE = 50
    }
}
