package cl.therionlabs.captura.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cl.therionlabs.captura.R
import cl.therionlabs.captura.domain.model.EstadoSync

@Composable
fun EstadoChip(estado: EstadoSync, modifier: Modifier = Modifier) {
    val sincronizado = estado == EstadoSync.SINCRONIZADO
    val contenedor by animateColorAsState(
        if (sincronizado) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.tertiaryContainer,
        label = "colorChip",
    )
    val contenido by animateColorAsState(
        if (sincronizado) MaterialTheme.colorScheme.onPrimaryContainer
        else MaterialTheme.colorScheme.onTertiaryContainer,
        label = "colorTextoChip",
    )

    AssistChip(
        onClick = {},
        modifier = modifier,
        colors = AssistChipDefaults.assistChipColors(
            containerColor = contenedor,
            labelColor = contenido,
        ),
        border = null,
        label = {
            AnimatedContent(
                targetState = estado,
                transitionSpec = { (fadeIn() + scaleIn(initialScale = 0.8f)) togetherWith fadeOut() },
                label = "estadoChip",
            ) { actual ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = if (actual == EstadoSync.SINCRONIZADO) Icons.Outlined.CloudDone
                        else Icons.Outlined.CloudUpload,
                        contentDescription = null,
                        tint = contenido,
                        modifier = Modifier.size(AssistChipDefaults.IconSize),
                    )
                    Text(
                        maxLines = 1,
                        softWrap = false,
                        text = stringResource(
                            if (actual == EstadoSync.SINCRONIZADO) R.string.estado_sincronizado
                            else R.string.estado_pendiente
                        )
                    )
                }
            }
        },
    )
}
