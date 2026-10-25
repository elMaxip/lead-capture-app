package cl.therionlabs.captura.data.session

import android.content.Context
import androidx.core.content.edit
import cl.therionlabs.captura.domain.model.Rol
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class Sesion(
    val token: String,
    val username: String,
    val nombre: String,
    val rol: Rol,
)

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs = context.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE)

    private val _sesion = MutableStateFlow(leer())
    val sesion: StateFlow<Sesion?> = _sesion.asStateFlow()

    val token: String? get() = _sesion.value?.token

    fun guardar(sesion: Sesion) {
        prefs.edit {
            putString(TOKEN, sesion.token)
            putString(USERNAME, sesion.username)
            putString(NOMBRE, sesion.nombre)
            putString(ROL, sesion.rol.name)
        }
        _sesion.value = sesion
    }

    fun limpiar() {
        prefs.edit { clear() }
        _sesion.value = null
    }

    private fun leer(): Sesion? {
        val token = prefs.getString(TOKEN, null) ?: return null
        val rol = Rol.entries.find { it.name == prefs.getString(ROL, null) } ?: return null
        return Sesion(
            token = token,
            username = prefs.getString(USERNAME, null).orEmpty(),
            nombre = prefs.getString(NOMBRE, null).orEmpty(),
            rol = rol,
        )
    }

    private companion object {
        const val ARCHIVO = "sesion"
        const val TOKEN = "token"
        const val USERNAME = "username"
        const val NOMBRE = "nombre"
        const val ROL = "rol"
    }
}
