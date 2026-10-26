package cl.therionlabs.captura.ui.formulario

import cl.therionlabs.captura.data.local.AreaEntity
import cl.therionlabs.captura.data.local.CursoEntity
import cl.therionlabs.captura.domain.model.CanalContacto
import cl.therionlabs.captura.domain.model.Modalidad
import cl.therionlabs.captura.domain.model.NivelEducativo

sealed interface FormularioAccion {
    data class OnNameChange(val value: String) : FormularioAccion
    data class OnEmailChange(val value: String) : FormularioAccion
    data class OnPhoneChange(val value: String) : FormularioAccion
    data class OnEducationChange(val value: NivelEducativo) : FormularioAccion
    data class OnAreaChange(val value: AreaEntity) : FormularioAccion
    data class OnCourseChange(val value: CursoEntity) : FormularioAccion
    data class OnModeChange(val value: Modalidad) : FormularioAccion
    data class OnContactChange(val value: CanalContacto) : FormularioAccion
    data class OnObservationsChange(val value: String) : FormularioAccion
    data class FieldTouched(val value: Campo) : FormularioAccion
}