package com.po4yka.trailglass.ui.screens.algorithmsettings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Displays a clickable card showing an algorithm setting with icon, title, current value, and description.
 *
 * @param icon The icon to display on the left side of the card
 * @param title The title of the algorithm setting
 * @param currentValue The currently selected algorithm value
 * @param description A description of the current algorithm
 * @param onClick Callback invoked when the card is clicked
 * @param modifier Optional modifier for the card
 */
@Composable
internal fun AlgorithmCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    currentValue: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = currentValue,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Dialog for selecting an algorithm from a list of options.
 *
 * @param T The type of algorithm option
 * @param title The title of the dialog
 * @param options List of available algorithm options
 * @param selectedOption The currently selected option
 * @param getOptionLabel Function to get the display label for an option
 * @param getOptionDescription Function to get the description for an option
 * @param onDismiss Callback invoked when the dialog is dismissed
 * @param onSelect Callback invoked when an option is selected
 * @param modifier Optional modifier for the dialog
 */
@Composable
internal fun <T> AlgorithmSelectionDialog(
    title: String,
    options: List<T>,
    selectedOption: T,
    getOptionLabel: (T) -> String,
    getOptionDescription: (T) -> String,
    onDismiss: () -> Unit,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(options.size) { index ->
                    val option = options[index]
                    Card(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(option) },
                        colors =
                            CardDefaults.cardColors(
                                containerColor =
                                    if (selectedOption == option) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    }
                            )
                    ) {
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            RadioButton(
                                selected = selectedOption == option,
                                onClick = { onSelect(option) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = getOptionLabel(option),
                                    style = MaterialTheme.typography.titleSmall,
                                    color =
                                        if (selectedOption == option) {
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = getOptionDescription(option),
                                    style = MaterialTheme.typography.bodySmall,
                                    color =
                                        if (selectedOption == option) {
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
