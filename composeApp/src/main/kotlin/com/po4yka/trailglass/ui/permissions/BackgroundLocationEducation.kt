package com.po4yka.trailglass.ui.permissions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/** Dialog explaining how to enable background location in system settings. Shows platform-specific instructions. */
@Composable
fun BackgroundLocationInstructionsDialog(
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null
            )
        },
        title = {
            Text("Enable Background Location")
        },
        text = {
            Column {
                Text(
                    text = "To enable background location tracking:",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Android instructions
                InstructionStep(1, "Tap 'Open Settings' below")
                InstructionStep(2, "Find 'Permissions' or 'Location'")
                InstructionStep(3, "Select 'Allow all the time'")

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Note: You may need to navigate through app settings to find the location permission option.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(onClick = onOpenSettings) {
                Text("Open Settings")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun InstructionStep(
    number: Int,
    text: String
) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = number.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/** Bottom sheet explaining background location with platform-specific guidance. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackgroundLocationBottomSheet(
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    val navigationBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 24.dp)
                    .padding(bottom = navigationBarPadding + 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Enable \"Always Allow\" Location",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "For automatic trip tracking, please allow location access all the time.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Instructions card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Steps:",
                        style = MaterialTheme.typography.titleSmall
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    InstructionStep(1, "Tap 'Open Settings' below")
                    InstructionStep(2, "Select 'Permissions' or 'Location'")
                    InstructionStep(3, "Choose 'Allow all the time'")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onOpenSettings,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open Settings")
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Maybe Later")
            }
        }
    }
}
