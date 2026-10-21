package cl.therionlabs.captura.domain.validation

object ProspectoValidator {

    const val NOMBRE_MIN = 2
    const val NOMBRE_MAX = 100
    const val EMAIL_MAX = 150
    const val TELEFONO_DIGITOS = 8
    const val OBSERVACIONES_MAX = 500

    const val MSG_NOMBRE_VACIO = "Ingresa el nombre"
    const val MSG_NOMBRE_CORTO = "Mínimo 2 caracteres"
    const val MSG_NOMBRE_LARGO = "Máximo 100 caracteres"
    const val MSG_EMAIL = "Correo inválido"
    const val MSG_TELEFONO = "Debe tener 8 dígitos"
    const val MSG_SELECCION = "Selecciona una opción"
    const val MSG_OBSERVACIONES = "Máximo 500 caracteres"

    private val EMAIL_REGEX =
        Regex("^[A-Za-z0-9_%+-]+(\\.[A-Za-z0-9_%+-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$")

    private val TELEFONO_REGEX = Regex("^\\d{$TELEFONO_DIGITOS}$")

    fun validarNombre(valor: String): String? {
        val nombre = valor.trim()
        return when {
            nombre.isEmpty() -> MSG_NOMBRE_VACIO
            nombre.length < NOMBRE_MIN -> MSG_NOMBRE_CORTO
            nombre.length > NOMBRE_MAX -> MSG_NOMBRE_LARGO
            else -> null
        }
    }

    fun validarEmail(valor: String): String? {
        val email = valor.trim()
        return if (email.length in 1..EMAIL_MAX && EMAIL_REGEX.matches(email)) null else MSG_EMAIL
    }

    fun validarTelefono(valor: String): String? =
        if (TELEFONO_REGEX.matches(valor)) null else MSG_TELEFONO

    fun validarSeleccion(valor: Any?): String? = if (valor == null) MSG_SELECCION else null

    fun validarObservaciones(valor: String): String? =
        if (valor.trim().length > OBSERVACIONES_MAX) MSG_OBSERVACIONES else null
}
