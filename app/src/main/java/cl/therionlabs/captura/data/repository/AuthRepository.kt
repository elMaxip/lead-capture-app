package cl.therionlabs.captura.data.repository

import cl.therionlabs.captura.data.remote.ApiService
import cl.therionlabs.captura.data.remote.dto.LoginRequestDto
import cl.therionlabs.captura.data.session.Sesion
import cl.therionlabs.captura.data.session.SessionManager
import cl.therionlabs.captura.data.sync.SyncScheduler
import cl.therionlabs.captura.domain.model.Rol
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: ApiService,
    private val sessionManager: SessionManager,
    private val syncScheduler: SyncScheduler,
) {
    // El aviso de cierre al servidor no debe cancelarse cuando la pantalla desaparece.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** Lanza IOException sin red y HttpException (401) si las credenciales no son válidas. */
    suspend fun iniciarSesion(username: String, password: String): Sesion {
        val respuesta = api.iniciarSesion(LoginRequestDto(username.trim(), password))
        val rol = Rol.entries.find { it.name == respuesta.usuario.rol }
            ?: error("Rol desconocido: ${respuesta.usuario.rol}")
        val sesion = Sesion(
            token = respuesta.token,
            username = respuesta.usuario.username,
            nombre = respuesta.usuario.nombre,
            rol = rol,
        )
        sessionManager.guardar(sesion)
        // Los registros que quedaron pendientes de una sesión anterior se envían ahora.
        if (rol == Rol.CAPTADOR) syncScheduler.programar()
        return sesion
    }

    /**
     * Cierra la sesión en el dispositivo de inmediato y avisa al servidor en segundo plano.
     * Los prospectos pendientes se conservan y se sincronizan en el próximo inicio de sesión.
     */
    fun cerrarSesion() {
        val token = sessionManager.token ?: return
        sessionManager.limpiar()
        scope.launch {
            runCatching { api.cerrarSesion("Bearer $token") }
        }
    }
}
