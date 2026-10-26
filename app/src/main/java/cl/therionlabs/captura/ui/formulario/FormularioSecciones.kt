package cl.therionlabs.captura.ui.formulario

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.StickyNote2
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Interests
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.therionlabs.captura.R
import cl.therionlabs.captura.data.local.AreaEntity
import cl.therionlabs.captura.data.local.CursoEntity
import cl.therionlabs.captura.domain.model.CanalContacto
import cl.therionlabs.captura.domain.model.Modalidad
import cl.therionlabs.captura.domain.model.NivelEducativo
import cl.therionlabs.captura.domain.validation.ProspectoValidator
import cl.therionlabs.captura.ui.components.DropdownField
import cl.therionlabs.captura.ui.components.ErrorAnimado
import cl.therionlabs.captura.ui.components.Selector
import cl.therionlabs.captura.ui.lista.formatearFecha
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
internal fun Seccion(
    titulo: String,
    icono: ImageVector,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp),
            ) {
                Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    titulo,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            content()
        }
    }
}

@Preview
@Composable
private fun SeccionPreview() {
    Seccion(titulo = "Seccion", icono = Icons.Outlined.Badge) {
        Text("Contenido")
    }
}

@Composable
internal fun SeccionDatos(
    nombre: String,
    email: String,
    telefono: String,
    nivelEducativo: NivelEducativo?,
    errorNombre: String?,
    errorEmail: String?,
    errorTelefono: String?,
    errorNivel: String?,
    onAction: (FormularioAccion) -> Unit,
    focus: Map<Campo, FocusRequester>,
    onSiguiente: () -> Unit,
) {
    Seccion(stringResource(R.string.seccion_datos), Icons.Outlined.Badge) {
        OutlinedTextField(
            value = nombre,
            onValueChange = { onAction(FormularioAccion.OnNameChange(it)) },
            label = { Text(stringResource(R.string.campo_nombre)) },
            singleLine = true,
            isError = errorNombre != null,
            supportingText = { ErrorAnimado(errorNombre) },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .campo(Campo.NOMBRE, focus) { onAction(FormularioAccion.FieldTouched(it)) },
        )
        OutlinedTextField(
            value = email,
            onValueChange = { onAction(FormularioAccion.OnEmailChange(it)) },
            label = { Text(stringResource(R.string.campo_email)) },
            singleLine = true,
            isError = errorEmail != null,
            supportingText = { ErrorAnimado(errorEmail) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .campo(Campo.EMAIL, focus, { onAction(FormularioAccion.FieldTouched(it)) }),
        )
        OutlinedTextField(
            value = telefono,
            onValueChange = { onAction(FormularioAccion.OnPhoneChange(it)) },
            label = { Text(stringResource(R.string.campo_telefono)) },
            prefix = { Text(stringResource(R.string.campo_telefono_prefijo)) },
            singleLine = true,
            isError = errorTelefono != null,
            supportingText = { ErrorAnimado(errorTelefono) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { onSiguiente() }),
            modifier = Modifier
                .fillMaxWidth()
                .campo(Campo.TELEFONO, focus, { onAction(FormularioAccion.FieldTouched(it)) }),
        )
        DropdownField(
            label = stringResource(R.string.campo_nivel),
            opciones = NivelEducativo.entries,
            seleccion = nivelEducativo,
            etiqueta = { it.etiqueta },
            onSeleccion = { onAction(FormularioAccion.OnEducationChange(it)) },
            onCerrar = { onAction(FormularioAccion.FieldTouched(Campo.NIVEL_EDUCATIVO)) },
            error = errorNivel,
            focusRequester = focus.getValue(Campo.NIVEL_EDUCATIVO),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview
@Composable
private fun SeccionDatosPreview() {
    val focus = remember { Campo.entries.associateWith { FocusRequester() } }
    SeccionDatos(
        nombre = "John Doe",
        email = "johndoe@gmail.com",
        telefono = "12345678",
        nivelEducativo = NivelEducativo.UNIVERSITARIA,
        errorNombre = null,
        errorEmail = null,
        errorTelefono = null,
        errorNivel = null,
        onAction = {},
        focus = focus,
        onSiguiente = {},
    )
}

@Composable
internal fun SeccionIntereses(
    areas: List<AreaEntity>,
    areaSeleccionada: AreaEntity?,
    cursos: List<CursoEntity>,
    cursoSeleccionado: CursoEntity?,
    hayArea: Boolean,
    modalidad: Modalidad?,
    canalContacto: CanalContacto?,
    errorArea: String?,
    errorCurso: String?,
    errorModalidad: String?,
    errorCanal: String?,
    onAction: (FormularioAccion) -> Unit,
    focus: Map<Campo, FocusRequester>,
) {
    Seccion(stringResource(R.string.seccion_intereses), Icons.Outlined.Interests) {
        DropdownField(
            label = stringResource(R.string.campo_area),
            opciones = areas,
            seleccion = areaSeleccionada,
            etiqueta = { it.nombre },
            onSeleccion = { onAction(FormularioAccion.OnAreaChange(it)) },
            onCerrar = { onAction(FormularioAccion.FieldTouched(Campo.AREA)) },
            error = errorArea,
            focusRequester = focus.getValue(Campo.AREA),
            modifier = Modifier.fillMaxWidth(),
        )
        DropdownField(
            label = stringResource(R.string.campo_curso),
            opciones = cursos,
            seleccion = cursoSeleccionado,
            etiqueta = { it.nombre },
            onSeleccion = { onAction(FormularioAccion.OnCourseChange(it)) },
            onCerrar = { onAction(FormularioAccion.FieldTouched(Campo.CURSO)) },
            error = errorCurso,
            enabled = hayArea,
            placeholder = if (!hayArea) stringResource(R.string.campo_curso_sin_area) else null,
            focusRequester = focus.getValue(Campo.CURSO),
            modifier = Modifier.fillMaxWidth(),
        )
        Selector(
            titulo = stringResource(R.string.campo_modalidad),
            opciones = Modalidad.entries,
            seleccion = modalidad,
            etiqueta = { it.etiqueta },
            onSeleccion = { onAction(FormularioAccion.OnModeChange(it)) },
            error = errorModalidad,
            focusRequester = focus.getValue(Campo.MODALIDAD),
        )
        Spacer(Modifier.height(4.dp))
        Selector(
            titulo = stringResource(R.string.campo_canal),
            opciones = CanalContacto.entries,
            seleccion = canalContacto,
            etiqueta = { it.etiqueta },
            onSeleccion = { onAction(FormularioAccion.OnContactChange(it)) },
            error = errorCanal,
            focusRequester = focus.getValue(Campo.CANAL_CONTACTO),
        )
    }
}

@Preview
@Composable
private fun SeccionInteresesPreview() {
    val focus = remember { Campo.entries.associateWith { FocusRequester() } }
    val area = AreaEntity(id = 1, nombre = "Tecnología")
    val curso = CursoEntity(id = 1, areaId = area.id, nombre = "Desarrollo Android")
    SeccionIntereses(
        areas = listOf(area),
        areaSeleccionada = area,
        cursos = listOf(curso),
        cursoSeleccionado = curso,
        hayArea = true,
        modalidad = Modalidad.entries.first(),
        canalContacto = null,
        errorArea = null,
        errorCurso = null,
        errorModalidad = null,
        errorCanal = "Selecciona un canal",
        onAction = {},
        focus = focus,
    )
}

@Composable
internal fun SeccionOperativa(
    observaciones: String,
    errorObservaciones: String?,
    onAction: (FormularioAccion) -> Unit,
    focus: Map<Campo, FocusRequester>,
) {
    val ahora by produceState(System.currentTimeMillis()) {
        while (true) {
            delay(15_000.milliseconds)
            value = System.currentTimeMillis()
        }
    }

    Seccion(stringResource(R.string.seccion_operativa), Icons.AutoMirrored.Outlined.StickyNote2) {
        OutlinedTextField(
            value = observaciones,
            onValueChange = { onAction(FormularioAccion.OnObservationsChange(it)) },
            label = { Text(stringResource(R.string.campo_observaciones)) },
            minLines = 3,
            maxLines = 6,
            isError = errorObservaciones != null,
            supportingText = {
                Row {
                    ErrorAnimado(errorObservaciones, Modifier.weight(1f))
                    Spacer(Modifier.size(8.dp))
                    Text(
                        stringResource(
                            R.string.campo_contador,
                            observaciones.trim().length,
                            ProspectoValidator.OBSERVACIONES_MAX,
                        )
                    )
                }
            },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier = Modifier
                .fillMaxWidth()
                .campo(Campo.OBSERVACIONES, focus) { onAction(FormularioAccion.FieldTouched(it)) },
        )
        OutlinedTextField(
            value = formatearFecha(ahora),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.campo_fecha_captura)) },
            leadingIcon = { Icon(Icons.Outlined.Schedule, contentDescription = null) },
            supportingText = { Text(stringResource(R.string.campo_fecha_captura_ayuda)) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview
@Composable
private fun SeccionOperativaPreview() {
    val focus = remember { Campo.entries.associateWith { FocusRequester() } }
    SeccionOperativa(
        observaciones = "Interesado en horario vespertino",
        errorObservaciones = null,
        onAction = {},
        focus = focus,
    )
}
