package com.po4yka.trailglass.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.po4yka.trailglass.domain.model.Trip
import com.po4yka.trailglass.feature.export.ExportController
import com.po4yka.trailglass.ui.components.ExportDialog
import com.po4yka.trailglass.ui.util.FilePickerHelper

/** Wrapper screen that integrates TripsScreen with export functionality. Adds export capabilities to the trips list. */
@Composable
fun TripsScreenWrapper(
    trips: List<Trip>,
    exportController: ExportController,
    userId: String,
    onTripClick: (Trip) -> Unit = {},
    onCreateTrip: () -> Unit = {},
    onRefresh: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity =
        context as? ComponentActivity
            ?: error("TripsScreenWrapper must be used within a ComponentActivity")

    val filePickerHelper = remember { FilePickerHelper(activity) }
    val exportState by exportController.state.collectAsState()

    // Show export dialog
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
                    dataTypeName = "all_trips",
                    format = exportState.selectedFormat
                )

            // Use default export directory
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

    // Scaffold with export FAB
    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Export button
                if (trips.isNotEmpty()) {
                    SmallFloatingActionButton(
                        onClick = {
                            exportController.setDataType(ExportController.DataTypeSelection.AllTrips)
                            exportController.showDialog()
                        },
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Export All Trips")
                    }
                }

                // Create trip button (main FAB)
                FloatingActionButton(
                    onClick = onCreateTrip,
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Create Trip"
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            TripsScreen(
                trips = trips,
                onTripClick = onTripClick,
                onCreateTrip = onCreateTrip,
                onRefresh = onRefresh
            )
        }
    }
}
