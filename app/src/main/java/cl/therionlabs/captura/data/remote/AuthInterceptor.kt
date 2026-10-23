package cl.therionlabs.captura.data.remote

import cl.therionlabs.captura.data.session.SessionManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/** Agrega el token de la sesión a cada petición y cierra la sesión si el servidor la rechaza. */
@Singleton
class AuthInterceptor @Inject constructor(
    private val sessionManager: SessionManager,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = sessionManager.token
        val request = if (token == null || original.header(AUTHORIZATION) != null) {
            original
        } else {
            original.newBuilder().header(AUTHORIZATION, "Bearer $token").build()
        }

        val response = chain.proceed(request)
        // La sesión se revocó o ya no existe en el servidor: se vuelve al login.
        if (response.code == 401 && token != null && sessionManager.token == token) {
            sessionManager.limpiar()
        }
        return response
    }

    private companion object {
        const val AUTHORIZATION = "Authorization"
    }
}
