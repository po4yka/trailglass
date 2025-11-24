package com.po4yka.trailglass.ui.screens.routeview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.po4yka.trailglass.feature.route.MapStyle

/** Map style selector bottom sheet. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MapStyleSelectorSheet(
    currentStyle: MapStyle,
    onStyleSelected: (MapStyle) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
        ) {
            Text(
                text = "Choose Map Style",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            MapStyle.entries.forEach { style ->
                MapStyleOption(
                    style = style,
                    isSelected = style == currentStyle,
                    onClick = { onStyleSelected(style) }
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

/** Privacy warning dialog shown before sharing location data. */
@Composable
internal fun PrivacyWarningDialog(
    exportResult: com.po4yka.trailglass.feature.route.export.ExportResult,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text("Privacy Warning")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = exportResult.privacyInfo.warningMessage,
                    style = MaterialTheme.typography.bodyMedium
                )

                HorizontalDivider()

                // Show detailed privacy info
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "File details:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "• ${exportResult.privacyInfo.numberOfPoints} GPS points",
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (exportResult.privacyInfo.numberOfPhotos > 0) {
                        Text(
                            text = "• ${exportResult.privacyInfo.numberOfPhotos} photo locations",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Text(
                        text = "• Timestamps included",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Text(
                    text = "Please ensure you trust the recipient before sharing.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Share Anyway")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
