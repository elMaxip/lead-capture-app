package cl.therionlabs.captura.ui.formulario

import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusEventModifierNode
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.node.ModifierNodeElement

internal fun Modifier.campo(
    campo: Campo,
    focus: Map<Campo, FocusRequester>,
    onTocado: (Campo) -> Unit,
): Modifier = focusRequester(focus.getValue(campo)) then MarcarTocadoElement(campo, onTocado)

private data class MarcarTocadoElement(
    val campo: Campo,
    val onTocado: (Campo) -> Unit,
) : ModifierNodeElement<MarcarTocadoNode>() {
    override fun create() = MarcarTocadoNode(campo, onTocado)

    override fun update(node: MarcarTocadoNode) {
        node.campo = campo
        node.onTocado = onTocado
    }
}

private class MarcarTocadoNode(
    var campo: Campo,
    var onTocado: (Campo) -> Unit,
) : Modifier.Node(), FocusEventModifierNode {
    private var tuvoFoco = false

    override fun onFocusEvent(focusState: FocusState) {
        if (tuvoFoco && !focusState.isFocused) onTocado(campo)
        tuvoFoco = focusState.isFocused
    }
}
