package cl.therionlabs.captura.ui.login

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.therionlabs.captura.R
import cl.therionlabs.captura.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val cargando: Boolean = false,
    @param:StringRes val error: Int? = null,
) {
    val puedeIngresar: Boolean get() = username.isNotBlank() && password.isNotEmpty() && !cargando
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onUsernameChange(valor: String) = _uiState.update { it.copy(username = valor.trim(), error = null) }

    fun onPasswordChange(valor: String) = _uiState.update { it.copy(password = valor, error = null) }

    fun ingresar() {
        val actual = _uiState.value
        if (!actual.puedeIngresar) return

        _uiState.update { it.copy(cargando = true, error = null) }
        viewModelScope.launch {
            // Si sale bien, AppNavHost reacciona a la nueva sesión y cambia de pantalla.
            val error = try {
                authRepository.iniciarSesion(actual.username, actual.password)
                null
            } catch (e: CancellationException) {
                throw e
            } catch (e: HttpException) {
                if (e.code() == 401) R.string.login_error_credenciales else R.string.login_error_servidor
            } catch (_: IOException) {
                R.string.login_error_conexion
            } catch (_: Exception) {
                R.string.login_error_servidor
            }
            _uiState.update { it.copy(cargando = false, error = error, password = if (error != null) "" else it.password) }
        }
    }
}
