package com.po4yka.trailglass.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.po4yka.trailglass.feature.diagnostics.DiagnosticsController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticsScreen(
    controller: DiagnosticsController,
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    val state by controller.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Diagnostics") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val exported = controller.exportDiagnostics()
                            shareText(context, exported, "TrailGlass Diagnostics")
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Export")
                    }
                    IconButton(onClick = { controller.refreshAll() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (state.isLoading) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
                Text(
                    text = "Loading diagnostics...",
                    modifier = Modifier.padding(top = 16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    LocationStatusCard(
                        trackingMode = state.locationStatus.trackingMode.toString(),
                        lastUpdate = state.locationStatus.lastLocationUpdate?.toString() ?: "Never",
                        accuracy = state.locationStatus.locationAccuracy?.let { "%.2f m".format(it) } ?: "N/A",
                        satellites = state.locationStatus.gpsSatellites?.toString() ?: "N/A",
                        locationPermission = state.locationStatus.locationPermissionGranted,
                        backgroundPermission = state.locationStatus.backgroundLocationPermissionGranted
                    )
                }

                item {
                    DatabaseStatusCard(
                        locationSamples = state.databaseStatus.locationSamplesCount,
                        placeVisits = state.databaseStatus.placeVisitsCount,
                        routeSegments = state.databaseStatus.routeSegmentsCount,
                        trips = state.databaseStatus.tripsCount,
                        photos = state.databaseStatus.photosCount,
                        regions = state.databaseStatus.regionsCount,
                        databaseSize = "%.2f MB".format(state.databaseStatus.databaseSizeMB)
                    )
                }

                item {
                    SyncStatusCard(
                        lastSync = state.syncStatus.lastSyncTimestamp?.toString() ?: "Never",
                        syncEnabled = state.syncStatus.syncEnabled,
                        pendingItems = state.syncStatus.pendingSyncItems,
                        errors = state.syncStatus.syncErrorsCount
                    )
                }

                item {
                    SystemStatusCard(
                        appVersion = state.systemStatus.appVersion,
                        buildNumber = state.systemStatus.buildNumber,
                        osVersion = state.systemStatus.osVersion,
                        deviceModel = state.systemStatus.deviceModel,
                        network = state.systemStatus.networkConnectivity.toString(),
                        batteryLevel = state.systemStatus.batteryLevel?.let { "${(it * 100).toInt()}%" } ?: "N/A",
                        batteryOptimization =
                            state.systemStatus.batteryOptimizationDisabled?.let {
                                if (it) "Disabled" else "Enabled"
                            } ?: "N/A",
                        lowPowerMode =
                            state.systemStatus.lowPowerMode?.let {
                                if (it) "Yes" else "No"
                            } ?: "N/A"
                    )
                }

                item {
                    PermissionsCard(
                        location = state.permissionsStatus.locationPermissionGranted,
                        backgroundLocation = state.permissionsStatus.backgroundLocationPermissionGranted,
                        notifications = state.permissionsStatus.notificationsPermissionGranted,
                        photoLibrary = state.permissionsStatus.photoLibraryPermissionGranted
                    )
                }
            }
        }
    }
}

@Composable
private fun LocationStatusCard(
    trackingMode: String,
    lastUpdate: String,
    accuracy: String,
    satellites: String,
    locationPermission: Boolean,
    backgroundPermission: Boolean
) {
    DiagnosticCard(title = "Location Status") {
        DiagnosticRow("Tracking Mode", trackingMode)
        DiagnosticRow("Last Update", lastUpdate)
        DiagnosticRow("Accuracy", accuracy)
        DiagnosticRow("GPS Satellites", satellites)
        DiagnosticRow("Location Permission", if (locationPermission) "✓ Granted" else "✗ Denied", locationPermission)
        DiagnosticRow("Background Permission", if (backgroundPermission) "✓ Granted" else "✗ Denied", backgroundPermission)
    }
}

@Composable
private fun DatabaseStatusCard(
    locationSamples: Long,
    placeVisits: Long,
    routeSegments: Long,
    trips: Long,
    photos: Long,
    regions: Long,
    databaseSize: String
) {
    DiagnosticCard(title = "Database Status") {
        DiagnosticRow("Location Samples", locationSamples.toString())
        DiagnosticRow("Place Visits", placeVisits.toString())
        DiagnosticRow("Route Segments", routeSegments.toString())
        DiagnosticRow("Trips", trips.toString())
        DiagnosticRow("Photos", photos.toString())
        DiagnosticRow("Regions", regions.toString())
        DiagnosticRow("Database Size", databaseSize)
    }
}

@Composable
private fun SyncStatusCard(
    lastSync: String,
    syncEnabled: Boolean,
    pendingItems: Int,
    errors: Int
) {
    DiagnosticCard(title = "Sync Status") {
        DiagnosticRow("Last Sync", lastSync)
        DiagnosticRow("Sync Enabled", if (syncEnabled) "Yes" else "No")
        DiagnosticRow("Pending Items", pendingItems.toString())
        DiagnosticRow("Errors", errors.toString())
    }
}

@Composable
private fun SystemStatusCard(
    appVersion: String,
    buildNumber: String,
    osVersion: String,
    deviceModel: String,
    network: String,
    batteryLevel: String,
    batteryOptimization: String,
    lowPowerMode: String
) {
    DiagnosticCard(title = "System Status") {
        DiagnosticRow("App Version", appVersion)
        DiagnosticRow("Build Number", buildNumber)
        DiagnosticRow("OS Version", osVersion)
        DiagnosticRow("Device Model", deviceModel)
        DiagnosticRow("Network", network)
        DiagnosticRow("Battery Level", batteryLevel)
        DiagnosticRow("Battery Optimization", batteryOptimization)
        DiagnosticRow("Low Power Mode", lowPowerMode)
    }
}

@Composable
private fun PermissionsCard(
    location: Boolean,
    backgroundLocation: Boolean,
    notifications: Boolean,
    photoLibrary: Boolean
) {
    DiagnosticCard(title = "Permissions") {
        DiagnosticRow("Location", if (location) "✓ Granted" else "✗ Denied", location)
        DiagnosticRow("Background Location", if (backgroundLocation) "✓ Granted" else "✗ Denied", backgroundLocation)
        DiagnosticRow("Notifications", if (notifications) "✓ Granted" else "✗ Denied", notifications)
        DiagnosticRow("Photo Library", if (photoLibrary) "✓ Granted" else "✗ Denied", photoLibrary)
    }
}

@Composable
private fun DiagnosticCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            content()
        }
    }
}

@Composable
private fun DiagnosticRow(
    label: String,
    value: String,
    isSuccess: Boolean? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = FontFamily.Monospace,
            color =
                when (isSuccess) {
                    true -> Color(0xFF4CAF50)
                    false -> Color(0xFFF44336)
                    null -> MaterialTheme.colorScheme.onSurface
                }
        )
    }
}

private fun shareText(
    context: android.content.Context,
    text: String,
    title: String
) {
    val sendIntent =
        android.content.Intent().apply {
            action = android.content.Intent.ACTION_SEND
            putExtra(android.content.Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
    val shareIntent = android.content.Intent.createChooser(sendIntent, title)
    context.startActivity(shareIntent)
}
