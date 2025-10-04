package com.po4yka.trailglass.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.po4yka.trailglass.feature.tracking.LocationTrackingController
import com.po4yka.trailglass.location.tracking.TrackingMode

/**
 * Settings screen for app configuration.
 */
@Composable
fun SettingsScreen(
    trackingController: LocationTrackingController,
    modifier: Modifier = Modifier
) {
    val uiState by trackingController.uiState.collectAsState()

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
                onRequestPermissions = { trackingController.requestPermissions() }
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
                onRequestPermissions = { trackingController.requestPermissions() }
            )
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
        colors = CardDefaults.cardColors(
            containerColor = if (isTracking)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
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
                    tint = if (isTracking)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
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
    onRequestPermissions: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (hasPermissions) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    tint = if (hasPermissions)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error
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
        }
    }
}
