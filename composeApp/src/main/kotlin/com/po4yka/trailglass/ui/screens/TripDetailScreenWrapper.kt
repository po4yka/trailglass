package com.po4yka.trailglass.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.po4yka.trailglass.domain.model.Trip
import com.po4yka.trailglass.feature.export.ExportController
import com.po4yka.trailglass.feature.export.ExportDataUseCase
import com.po4yka.trailglass.ui.components.ExportDialog
import com.po4yka.trailglass.ui.util.FilePickerHelper

/** Wrapper screen that integrates TripDetailScreen with export functionality. Manages export state and file picking. */
@Composable
fun TripDetailScreenWrapper(
    trip: Trip,
    exportController: ExportController,
    userId: String,
    onBack: () -> Unit = {},
    onViewRoute: () -> Unit = {},
    onEdit: () -> Unit = {},
    onShare: () -> Unit = {},
    onDelete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity =
        context as? ComponentActivity
            ?: error("TripDetailScreenWrapper must be used within a ComponentActivity")

    val filePickerHelper = remember { FilePickerHelper(activity) }
    val exportState by exportController.state.collectAsState()

    // Configure export for trips/visits when export is triggered
    LaunchedEffect(Unit) {
        exportController.setDataType(
            ExportController.DataTypeSelection.SingleTrip(
                tripId = trip.id,
                tripName = trip.displayName
            )
        )
    }

    // Show export dialog when requested
    ExportDialog(
        isVisible = exportState.showDialog,
        selectedFormat = exportState.selectedFormat,
        onFormatSelected = { format ->
            exportController.setFormat(format)
        },
        isExporting = exportState.isExporting,
        progress = exportState.progress,
        currentOperation = exportState.currentOperation,
        error = exportState.error,
        onExport = {
            // Request file location from user
            val fileName =
                filePickerHelper.generateDefaultFileName(
                    dataTypeName = trip.displayName,
                    format = exportState.selectedFormat
                )

            // For simplicity, use default export directory
            // In a real app, you might want to use the file picker
            val exportDir = filePickerHelper.getDefaultExportDirectory()
            val outputPath = "${exportDir.absolutePath}/$fileName"

            exportController.exportData(userId, outputPath)
        },
        onDismiss = {
            exportController.dismissDialog()
        },
        onClearError = {
            exportController.clearError()
        }
    )

    // Main trip detail screen
    TripDetailScreen(
        trip = trip,
        onBack = onBack,
        onViewRoute = onViewRoute,
        onEdit = onEdit,
        onShare = onShare,
        onExport = { format ->
            // Set format based on legacy ExportFormat enum
            val exportFormat =
                when (format) {
                    ExportFormat.GPX -> ExportDataUseCase.Format.GPX
                    ExportFormat.KML -> {
                        // KML not supported by ExportManager yet, use GPX as fallback
                        ExportDataUseCase.Format.GPX
                    }
                }
            exportController.setFormat(exportFormat)

            // Set data type to trip visits for GPX/KML exports
            exportController.setDataType(
                ExportController.DataTypeSelection.TripVisits(
                    tripId = trip.id,
                    tripName = trip.displayName
                )
            )

            exportController.showDialog()
        },
        onDelete = onDelete,
        modifier = modifier
    )

    // Show success snackbar when export completes
    LaunchedEffect(exportState.lastExportPath) {
        exportState.lastExportPath?.let { path ->
            // You could show a snackbar here
            // For now, we just log success
        }
    }
}
