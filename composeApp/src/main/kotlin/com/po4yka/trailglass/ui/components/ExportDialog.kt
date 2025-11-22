package com.po4yka.trailglass.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.po4yka.trailglass.feature.export.ExportDataUseCase

/**
 * Dialog for configuring and initiating data export.
 * Supports multiple export formats and data type selection.
 */
@Composable
fun ExportDialog(
    isVisible: Boolean,
    selectedFormat: ExportDataUseCase.Format,
    onFormatSelected: (ExportDataUseCase.Format) -> Unit,
    isExporting: Boolean,
    progress: Float,
    currentOperation: String?,
    error: String?,
    onExport: () -> Unit,
    onDismiss: () -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isVisible) {
        AlertDialog(
            onDismissRequest = { if (!isExporting) onDismiss() },
            modifier = modifier,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Download,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Export Data",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Format selection
                    Text(
                        text = "Export Format",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    FormatSelector(
                        selectedFormat = selectedFormat,
                        onFormatSelected = onFormatSelected,
                        enabled = !isExporting
                    )

                    // Progress indicator
                    AnimatedVisibility(visible = isExporting) {
                        ExportProgressSection(
                            progress = progress,
                            currentOperation = currentOperation
                        )
                    }

                    // Error message
                    AnimatedVisibility(visible = error != null) {
                        ErrorSection(
                            error = error ?: "",
                            onClearError = onClearError
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = onExport,
                    enabled = !isExporting,
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                ) {
                    if (isExporting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(if (isExporting) "Exporting..." else "Export")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                    enabled = !isExporting
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun FormatSelector(
    selectedFormat: ExportDataUseCase.Format,
    onFormatSelected: (ExportDataUseCase.Format) -> Unit,
    enabled: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FormatOption(
            format = ExportDataUseCase.Format.CSV,
            icon = Icons.Default.TableChart,
            title = "CSV",
            description = "Spreadsheet format, compatible with Excel and Google Sheets",
            isSelected = selectedFormat == ExportDataUseCase.Format.CSV,
            onClick = { onFormatSelected(ExportDataUseCase.Format.CSV) },
            enabled = enabled
        )

        FormatOption(
            format = ExportDataUseCase.Format.GPX,
            icon = Icons.Default.Map,
            title = "GPX",
            description = "GPS format for navigation apps and mapping tools",
            isSelected = selectedFormat == ExportDataUseCase.Format.GPX,
            onClick = { onFormatSelected(ExportDataUseCase.Format.GPX) },
            enabled = enabled
        )

        FormatOption(
            format = ExportDataUseCase.Format.JSON,
            icon = Icons.Default.Code,
            title = "JSON",
            description = "Structured data format for technical use and integration",
            isSelected = selectedFormat == ExportDataUseCase.Format.JSON,
            onClick = { onFormatSelected(ExportDataUseCase.Format.JSON) },
            enabled = enabled
        )
    }
}

@Composable
private fun FormatOption(
    format: ExportDataUseCase.Format,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean
) {
    Card(
        onClick = onClick,
        enabled = enabled,
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
            ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint =
                    if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color =
                        if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color =
                        if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        }
                )
            }

            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun ExportProgressSection(
    progress: Float,
    currentOperation: String?
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        if (currentOperation != null) {
            Text(
                text = currentOperation,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorSection(
    error: String,
    onClearError: () -> Unit
) {
    Card(
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onClearError) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Dismiss error",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}
