package com.po4yka.trailglass.ui.screens

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GpsOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.po4yka.trailglass.feature.tracking.LocationTrackingController
import com.po4yka.trailglass.location.tracking.TrackingMode
import com.po4yka.trailglass.ui.permissions.rememberLocationPermissionLauncher
import com.po4yka.trailglass.ui.permissions.rememberLocationPermissionState

/** Settings screen for app configuration. */
@Composable
fun SettingsScreen(
    trackingController: LocationTrackingController,
    onNavigateToDeviceManagement: () -> Unit = {},
    onNavigateToAlgorithmSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by trackingController.uiState.collectAsState()

    // Android permission launcher
    val permissionLauncher =
        rememberLocationPermissionLauncher { granted ->
            if (granted) {
                trackingController.checkPermissions()
            }
        }

    // Permission state for background location
    val permissionState =
        rememberLocationPermissionState { granted ->
            if (granted) {
                trackingController.checkPermissions()
            }
        }

    // Lifecycle observer to refresh permissions when returning from settings
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    permissionState.refresh()
                    trackingController.checkPermissions()
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Tracking section
        item {
            Text(
                text = "Location Tracking",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        item {
            TrackingStatusCard(
                isTracking = uiState.trackingState.isTracking,
                mode = uiState.trackingState.mode,
                samplesRecorded = uiState.trackingState.samplesRecordedToday,
                hasPermissions = uiState.hasPermissions,
                onStartTracking = { trackingController.startTracking(TrackingMode.PASSIVE) },
                onStopTracking = { trackingController.stopTracking() },
                onRequestPermissions = { permissionLauncher.launch() }
            )
        }

        // Permissions section
        item {
            Text(
                text = "Permissions",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        item {
            PermissionsCard(
                hasPermissions = uiState.hasPermissions,
                hasBackgroundPermission = permissionState.backgroundPermissionGranted,
                onRequestPermissions = { permissionLauncher.launch() },
                onRequestBackgroundPermission = {
                    permissionState.requestBackgroundPermission()
                }
            )
        }

        // Advanced section
        item {
            Text(
                text = "Advanced",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = onNavigateToAlgorithmSettings
            ) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Algorithm Settings",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Configure distance, bearing, and interpolation",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Account section
        item {
            Text(
                text = "Account",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = onNavigateToDeviceManagement
            ) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Devices,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Device Management",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Manage your connected devices",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // About section
        item {
            Text(
                text = "About",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        item {
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "TrailGlass",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Version 1.0.0-alpha",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun TrackingStatusCard(
    isTracking: Boolean,
    mode: TrackingMode,
    samplesRecorded: Int,
    hasPermissions: Boolean,
    onStartTracking: () -> Unit,
    onStopTracking: () -> Unit,
    onRequestPermissions: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (isTracking) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (isTracking) Icons.Default.GpsFixed else Icons.Default.GpsOff,
                    contentDescription = null,
                    tint =
                        if (isTracking) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                )

                Column {
                    Text(
                        text = if (isTracking) "Tracking Active" else "Tracking Inactive",
                        style = MaterialTheme.typography.titleMedium
                    )

                    if (isTracking) {
                        Text(
                            text = "Mode: ${mode.name} • $samplesRecorded samples today",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (!hasPermissions) {
                Button(
                    onClick = onRequestPermissions,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Grant Permissions")
                }
            } else if (isTracking) {
                OutlinedButton(
                    onClick = onStopTracking,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Stop Tracking")
                }
            } else {
                Button(
                    onClick = onStartTracking,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Start Tracking")
                }
            }
        }
    }
}

@Composable
private fun PermissionsCard(
    hasPermissions: Boolean,
    hasBackgroundPermission: Boolean,
    onRequestPermissions: () -> Unit,
    onRequestBackgroundPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Foreground permission status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (hasPermissions) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    tint =
                        if (hasPermissions) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                )

                Column {
                    Text(
                        text = "Location Permission",
                        style = MaterialTheme.typography.titleSmall
                    )

                    Text(
                        text = if (hasPermissions) "Granted" else "Not granted",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (!hasPermissions) {
                Spacer(modifier = Modifier.height(12.dp))

                TextButton(
                    onClick = onRequestPermissions,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Request Permission")
                }
            }

            // Background permission status (Android 10+)
            if (hasPermissions && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (hasBackgroundPermission) Icons.Default.CheckCircle else Icons.Default.LocationOn,
                        contentDescription = null,
                        tint =
                            if (hasBackgroundPermission) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                    )

                    Column {
                        Text(
                            text = "Background Location",
                            style = MaterialTheme.typography.titleSmall
                        )

                        Text(
                            text =
                                if (hasBackgroundPermission) {
                                    "Always allowed"
                                } else {
                                    "Only while using app"
                                },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (!hasBackgroundPermission) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Enable background location to automatically track trips and visits even when the app is closed.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onRequestBackgroundPermission,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Enable Background Tracking")
                    }
                }
            }
        }
    }
}
