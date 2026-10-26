package cl.therionlabs.captura.ui.components

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun <T> Selector(
    titulo: String,
    opciones: List<T>,
    seleccion: T?,
    etiqueta: (T) -> String,
    onSeleccion: (T) -> Unit,
    error: String?,
    focusRequester: FocusRequester = remember { FocusRequester() },
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .focusable(),
    ) {
        Text(
            titulo,
            style = MaterialTheme.typography.labelLarge,
            color = if (error != null) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            opciones.forEachIndexed { index, opcion ->
                SegmentedButton(
                    selected = opcion == seleccion,
                    onClick = { onSeleccion(opcion) },
                    shape = SegmentedButtonDefaults.itemShape(index, opciones.size),
                    label = { Text(etiqueta(opcion), maxLines = 1) },
                )
            }
        }
        ErrorAnimado(error, Modifier.padding(start = 16.dp, top = 4.dp))
    }
}

@Preview
@Composable
private fun SelectorPreview() {
    Selector(
        titulo = "Modalidad",
        opciones = listOf("Presencial", "Online", "Híbrida"),
        seleccion = "Online",
        etiqueta = { it },
        onSeleccion = {},
        error = null,
    )
}

@Preview
@Composable
private fun SelectorErrorPreview() {
    Selector(
        titulo = "Modalidad",
        opciones = listOf("Presencial", "Online", "Híbrida"),
        seleccion = null,
        etiqueta = { it },
        onSeleccion = {},
        error = "Selecciona una opción",
    )
}
