package com.po4yka.trailglass.ui.screens.settings

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GpsOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.po4yka.trailglass.location.tracking.TrackingMode

@Composable
internal fun TrackingStatusCard(
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
                        val modeDescription =
                            when (mode) {
                                TrackingMode.IDLE -> "Off"
                                TrackingMode.SIGNIFICANT -> "Efficient"
                                TrackingMode.PASSIVE -> "Balanced"
                                TrackingMode.ACTIVE -> "High"
                            }
                        Text(
                            text = "Mode: $modeDescription • $samplesRecorded samples today",
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
internal fun PermissionsCard(
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

@Composable
internal fun TrackingModeCard(
    currentMode: TrackingMode,
    isTracking: Boolean,
    onModeSelected: (TrackingMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier =
                Modifier
                    .padding(16.dp)
                    .selectableGroup()
        ) {
            Text(
                text = "Tracking Mode",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            TrackingModeOption(
                mode = TrackingMode.IDLE,
                title = "Off",
                description = "No tracking, GPS off",
                batteryImpact = "No battery impact",
                selected = currentMode == TrackingMode.IDLE,
                enabled = !isTracking,
                onSelect = { onModeSelected(TrackingMode.IDLE) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            TrackingModeOption(
                mode = TrackingMode.SIGNIFICANT,
                title = "Efficient",
                description = "Major movements only (WiFi/Cell changes)",
                batteryImpact = "Minimal (1-2% per day)",
                selected = currentMode == TrackingMode.SIGNIFICANT,
                enabled = !isTracking,
                onSelect = { onModeSelected(TrackingMode.SIGNIFICANT) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            TrackingModeOption(
                mode = TrackingMode.PASSIVE,
                title = "Balanced",
                description = "Periodic updates (5min/500m)",
                batteryImpact = "Low (3-5% per day)",
                selected = currentMode == TrackingMode.PASSIVE,
                enabled = !isTracking,
                onSelect = { onModeSelected(TrackingMode.PASSIVE) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            TrackingModeOption(
                mode = TrackingMode.ACTIVE,
                title = "High",
                description = "Continuous tracking (30sec/10m)",
                batteryImpact = "Moderate (10-15% per day)",
                selected = currentMode == TrackingMode.ACTIVE,
                enabled = !isTracking,
                onSelect = { onModeSelected(TrackingMode.ACTIVE) }
            )

            if (isTracking) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Stop tracking to change mode",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TrackingModeOption(
    mode: TrackingMode,
    title: String,
    description: String,
    batteryImpact: String,
    selected: Boolean,
    enabled: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .selectable(
                    selected = selected,
                    enabled = enabled,
                    role = Role.RadioButton,
                    onClick = onSelect
                ).padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
            enabled = enabled
        )

        Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.BatteryChargingFull,
                    contentDescription = null,
                    modifier = Modifier.height(12.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = batteryImpact,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
internal fun AboutCard(modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
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
