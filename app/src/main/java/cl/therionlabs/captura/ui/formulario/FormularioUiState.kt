package cl.therionlabs.captura.ui.formulario

import cl.therionlabs.captura.data.local.AreaEntity
import cl.therionlabs.captura.data.local.CursoEntity
import cl.therionlabs.captura.domain.model.CanalContacto
import cl.therionlabs.captura.domain.model.Modalidad
import cl.therionlabs.captura.domain.model.NivelEducativo
import cl.therionlabs.captura.domain.validation.ProspectoValidator

enum class Campo {
    NOMBRE, EMAIL, TELEFONO, NIVEL_EDUCATIVO, AREA, CURSO, MODALIDAD, CANAL_CONTACTO, OBSERVACIONES,
}

data class FormularioUiState(
    val nombre: String = "",
    val email: String = "",
    val telefono: String = "",
    val nivelEducativo: NivelEducativo? = null,
    val areaId: Int? = null,
    val cursoId: Int? = null,
    val modalidad: Modalidad? = null,
    val canalContacto: CanalContacto? = null,
    val observaciones: String = "",
    val tocados: Set<Campo> = emptySet(),
    val areas: List<AreaEntity> = emptyList(),
    val cursos: List<CursoEntity> = emptyList(),
    val guardando: Boolean = false,
) {
    val errores: Map<Campo, String> by lazy(LazyThreadSafetyMode.PUBLICATION) {
        buildMap {
            ProspectoValidator.validarNombre(nombre)?.let { put(Campo.NOMBRE, it) }
            ProspectoValidator.validarEmail(email)?.let { put(Campo.EMAIL, it) }
            ProspectoValidator.validarTelefono(telefono)?.let { put(Campo.TELEFONO, it) }
            ProspectoValidator.validarSeleccion(nivelEducativo)?.let { put(Campo.NIVEL_EDUCATIVO, it) }
            ProspectoValidator.validarSeleccion(areaId)?.let { put(Campo.AREA, it) }
            ProspectoValidator.validarSeleccion(cursoId)?.let { put(Campo.CURSO, it) }
            ProspectoValidator.validarSeleccion(modalidad)?.let { put(Campo.MODALIDAD, it) }
            ProspectoValidator.validarSeleccion(canalContacto)?.let { put(Campo.CANAL_CONTACTO, it) }
            ProspectoValidator.validarObservaciones(observaciones)?.let { put(Campo.OBSERVACIONES, it) }
        }
    }

    val esValido: Boolean get() = errores.isEmpty()

    fun errorVisible(campo: Campo): String? = if (campo in tocados) errores[campo] else null

    val areaSeleccionada: AreaEntity? get() = areas.find { it.id == areaId }
    val cursoSeleccionado: CursoEntity? get() = cursos.find { it.id == cursoId }
}

data class ResumenSync(
    val pendientes: Int = 0,
    val sinConexion: Boolean = false,
    val cargando: Boolean = true,
)

sealed interface FormularioEvento {
    data class Guardado(val sinConexion: Boolean) : FormularioEvento
    data class ErrorValidacion(val primerCampo: Campo) : FormularioEvento
    data class DatasetCargado(val cantidad: Int) : FormularioEvento
}
