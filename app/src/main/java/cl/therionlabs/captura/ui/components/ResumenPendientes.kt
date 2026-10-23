package cl.therionlabs.captura.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cl.therionlabs.captura.R

/** Indica cuántos registros del dispositivo faltan por sincronizar. */
@Composable
fun ResumenPendientes(pendientes: Int, modifier: Modifier = Modifier) {
    AnimatedContent(
        targetState = pendientes == 0,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        label = "resumen",
    ) { todoSincronizado ->
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                if (todoSincronizado) Icons.Outlined.CloudDone else Icons.Outlined.CloudUpload,
                contentDescription = null,
                tint = if (todoSincronizado) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.tertiary,
            )
            Text(
                text = if (todoSincronizado) stringResource(R.string.todo_sincronizado)
                else pluralStringResource(R.plurals.pendientes, pendientes, pendientes),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
