package com.po4yka.trailglass.ui.screens

import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.ViewCompact
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.po4yka.trailglass.domain.model.*
import com.po4yka.trailglass.feature.settings.SettingsController

/**
 * Enhanced settings screen with all preference categories.
 */
@Composable
fun EnhancedSettingsScreen(
    controller: SettingsController,
    onNavigateToAccount: () -> Unit = {},
    onNavigateToDataManagement: () -> Unit = {},
    onNavigateToAlgorithmSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by controller.state.collectAsState()
    val settings = state.settings
    var showClearDataDialog by remember { mutableStateOf(false) }

    if (state.isLoading || settings == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
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
            TrackingPreferencesCard(
                preferences = settings.trackingPreferences,
                onUpdate = { controller.updateTrackingPreferences(it) }
            )
        }

        // Privacy section
        item {
            Text(
                text = "Privacy",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        item {
            PrivacySettingsCard(
                privacy = settings.privacySettings,
                onUpdate = { controller.updatePrivacySettings(it) }
            )
        }

        // Units section
        item {
            Text(
                text = "Units & Format",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        item {
            UnitPreferencesCard(
                units = settings.unitPreferences,
                onUpdate = { controller.updateUnitPreferences(it) }
            )
        }

        // Appearance section
        item {
            Text(
                text = "Appearance",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        item {
            AppearanceSettingsCard(
                appearance = settings.appearanceSettings,
                onUpdate = { controller.updateAppearanceSettings(it) }
            )
        }

        // Algorithm section
        item {
            Text(
                text = "Advanced",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        item {
            AlgorithmSettingsNavigationCard(
                onClick = onNavigateToAlgorithmSettings
            )
        }

        // Account section
        item {
            Text(
                text = "Account & Sync",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        item {
            AccountSettingsCard(
                account = settings.accountSettings,
                onClick = onNavigateToAccount
            )
        }

        // Data Management section
        item {
            Text(
                text = "Data Management",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        item {
            DataManagementCard(
                data = settings.dataManagement,
                onClick = onNavigateToDataManagement,
                onClearData = { showClearDataDialog = true }
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
            AboutCard()
        }

        // Reset button
        item {
            OutlinedButton(
                onClick = { controller.resetToDefaults() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Icon(Icons.Default.RestartAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reset All Settings")
            }
        }
    }

    // Clear data confirmation dialog
    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("Clear All Data") },
            text = {
                Text(
                    "This will permanently delete all your data including trips, locations, photos, and settings. This action cannot be undone.\n\nAre you sure you want to continue?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearDataDialog = false
                        controller.clearAllData()
                    }
                ) {
                    Text("Clear All Data", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Error snackbar
    state.error?.let { error ->
        Snackbar(
            action = {
                TextButton(onClick = { controller.clearError() }) {
                    Text("Dismiss")
                }
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text(error)
        }
    }
}

@Composable
private fun TrackingPreferencesCard(
    preferences: TrackingPreferences,
    onUpdate: (TrackingPreferences) -> Unit
) {
    var showAccuracyDialog by remember { mutableStateOf(false) }
    var showIntervalDialog by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Accuracy
            ListItem(
                headlineContent = { Text("Tracking Accuracy") },
                supportingContent = { Text(preferences.accuracy.name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }) },
                leadingContent = { Icon(Icons.Default.MyLocation, contentDescription = null) },
                modifier = Modifier.clickable { showAccuracyDialog = true }
            )

            HorizontalDivider()

            // Update Interval
            ListItem(
                headlineContent = { Text("Update Interval") },
                supportingContent = { Text(preferences.updateInterval.name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }) },
                leadingContent = { Icon(Icons.Default.Update, contentDescription = null) },
                modifier = Modifier.clickable { showIntervalDialog = true }
            )

            HorizontalDivider()

            // Battery Optimization
            ListItem(
                headlineContent = { Text("Battery Optimization") },
                supportingContent = { Text("Reduce battery usage") },
                leadingContent = { Icon(Icons.Default.BatteryChargingFull, contentDescription = null) },
                trailingContent = {
                    Switch(
                        checked = preferences.batteryOptimization,
                        onCheckedChange = {
                            onUpdate(preferences.copy(batteryOptimization = it))
                        }
                    )
                }
            )

            HorizontalDivider()

            // Track When Stationary
            ListItem(
                headlineContent = { Text("Track When Stationary") },
                supportingContent = { Text("Continue tracking while not moving") },
                leadingContent = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                trailingContent = {
                    Switch(
                        checked = preferences.trackWhenStationary,
                        onCheckedChange = {
                            onUpdate(preferences.copy(trackWhenStationary = it))
                        }
                    )
                }
            )
        }
    }

    // Accuracy Dialog
    if (showAccuracyDialog) {
        AlertDialog(
            onDismissRequest = { showAccuracyDialog = false },
            title = { Text("Tracking Accuracy") },
            text = {
                Column {
                    TrackingAccuracy.values().forEach { accuracy ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onUpdate(preferences.copy(accuracy = accuracy))
                                    showAccuracyDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = preferences.accuracy == accuracy,
                                onClick = {
                                    onUpdate(preferences.copy(accuracy = accuracy))
                                    showAccuracyDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(accuracy.name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() })
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAccuracyDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Interval Dialog
    if (showIntervalDialog) {
        AlertDialog(
            onDismissRequest = { showIntervalDialog = false },
            title = { Text("Update Interval") },
            text = {
                Column {
                    UpdateInterval.values().forEach { interval ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onUpdate(preferences.copy(updateInterval = interval))
                                    showIntervalDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = preferences.updateInterval == interval,
                                onClick = {
                                    onUpdate(preferences.copy(updateInterval = interval))
                                    showIntervalDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(interval.name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() })
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showIntervalDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun PrivacySettingsCard(
    privacy: PrivacySettings,
    onUpdate: (PrivacySettings) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            ListItem(
                headlineContent = { Text("Share Analytics") },
                supportingContent = { Text("Help improve the app") },
                leadingContent = { Icon(Icons.Default.Analytics, contentDescription = null) },
                trailingContent = {
                    Switch(
                        checked = privacy.shareAnalytics,
                        onCheckedChange = {
                            onUpdate(privacy.copy(shareAnalytics = it))
                        }
                    )
                }
            )

            HorizontalDivider()

            ListItem(
                headlineContent = { Text("Share Crash Reports") },
                supportingContent = { Text("Help fix bugs") },
                leadingContent = { Icon(Icons.Default.BugReport, contentDescription = null) },
                trailingContent = {
                    Switch(
                        checked = privacy.shareCrashReports,
                        onCheckedChange = {
                            onUpdate(privacy.copy(shareCrashReports = it))
                        }
                    )
                }
            )

            HorizontalDivider()

            ListItem(
                headlineContent = { Text("Auto Backup") },
                supportingContent = { Text("Automatically backup data") },
                leadingContent = { Icon(Icons.Default.Backup, contentDescription = null) },
                trailingContent = {
                    Switch(
                        checked = privacy.autoBackup,
                        onCheckedChange = {
                            onUpdate(privacy.copy(autoBackup = it))
                        }
                    )
                }
            )

            HorizontalDivider()

            ListItem(
                headlineContent = { Text("Encrypt Backups") },
                supportingContent = { Text("Secure your backup data") },
                leadingContent = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingContent = {
                    Switch(
                        checked = privacy.encryptBackups,
                        onCheckedChange = {
                            onUpdate(privacy.copy(encryptBackups = it))
                        }
                    )
                }
            )

            HorizontalDivider()

            ListItem(
                headlineContent = { Text("End-to-End Encryption") },
                supportingContent = { Text("Encrypt data before sync (requires key backup)") },
                leadingContent = { Icon(Icons.Default.Security, contentDescription = null) },
                trailingContent = {
                    Switch(
                        checked = privacy.enableE2EEncryption,
                        onCheckedChange = {
                            onUpdate(privacy.copy(enableE2EEncryption = it))
                        }
                    )
                }
            )
        }
    }
}

@Composable
private fun UnitPreferencesCard(
    units: UnitPreferences,
    onUpdate: (UnitPreferences) -> Unit
) {
    var showDistanceDialog by remember { mutableStateOf(false) }
    var showTempDialog by remember { mutableStateOf(false) }
    var showTimeDialog by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            ListItem(
                headlineContent = { Text("Distance Units") },
                supportingContent = { Text(units.distanceUnit.name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }) },
                leadingContent = { Icon(Icons.Default.Straighten, contentDescription = null) },
                modifier = Modifier.clickable { showDistanceDialog = true }
            )

            HorizontalDivider()

            ListItem(
                headlineContent = { Text("Temperature Units") },
                supportingContent = { Text(units.temperatureUnit.name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }) },
                leadingContent = { Icon(Icons.Default.Thermostat, contentDescription = null) },
                modifier = Modifier.clickable { showTempDialog = true }
            )

            HorizontalDivider()

            ListItem(
                headlineContent = { Text("Time Format") },
                supportingContent = { Text(if (units.timeFormat == TimeFormat.TWELVE_HOUR) "12-hour" else "24-hour") },
                leadingContent = { Icon(Icons.Default.Schedule, contentDescription = null) },
                modifier = Modifier.clickable { showTimeDialog = true }
            )
        }
    }

    // Distance Dialog
    if (showDistanceDialog) {
        AlertDialog(
            onDismissRequest = { showDistanceDialog = false },
            title = { Text("Distance Units") },
            text = {
                Column {
                    DistanceUnit.values().forEach { unit ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onUpdate(units.copy(distanceUnit = unit))
                                    showDistanceDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = units.distanceUnit == unit,
                                onClick = {
                                    onUpdate(units.copy(distanceUnit = unit))
                                    showDistanceDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(unit.name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() })
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDistanceDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Temperature Dialog
    if (showTempDialog) {
        AlertDialog(
            onDismissRequest = { showTempDialog = false },
            title = { Text("Temperature Units") },
            text = {
                Column {
                    TemperatureUnit.values().forEach { unit ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onUpdate(units.copy(temperatureUnit = unit))
                                    showTempDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = units.temperatureUnit == unit,
                                onClick = {
                                    onUpdate(units.copy(temperatureUnit = unit))
                                    showTempDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(unit.name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() })
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTempDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Time Format Dialog
    if (showTimeDialog) {
        AlertDialog(
            onDismissRequest = { showTimeDialog = false },
            title = { Text("Time Format") },
            text = {
                Column {
                    TimeFormat.values().forEach { format ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onUpdate(units.copy(timeFormat = format))
                                    showTimeDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = units.timeFormat == format,
                                onClick = {
                                    onUpdate(units.copy(timeFormat = format))
                                    showTimeDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (format == TimeFormat.TWELVE_HOUR) "12-hour" else "24-hour")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTimeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun AppearanceSettingsCard(
    appearance: AppearanceSettings,
    onUpdate: (AppearanceSettings) -> Unit
) {
    var showThemeDialog by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            ListItem(
                headlineContent = { Text("Theme") },
                supportingContent = { Text(appearance.theme.name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }) },
                leadingContent = { Icon(Icons.Default.Palette, contentDescription = null) },
                modifier = Modifier.clickable { showThemeDialog = true }
            )

            HorizontalDivider()

            ListItem(
                headlineContent = { Text("Show Map in Timeline") },
                supportingContent = { Text("Display map view") },
                leadingContent = { Icon(Icons.Default.Map, contentDescription = null) },
                trailingContent = {
                    Switch(
                        checked = appearance.showMapInTimeline,
                        onCheckedChange = {
                            onUpdate(appearance.copy(showMapInTimeline = it))
                        }
                    )
                }
            )

            HorizontalDivider()

            ListItem(
                headlineContent = { Text("Compact View") },
                supportingContent = { Text("Condensed UI") },
                leadingContent = { Icon(Icons.Default.ViewCompact, contentDescription = null) },
                trailingContent = {
                    Switch(
                        checked = appearance.compactView,
                        onCheckedChange = {
                            onUpdate(appearance.copy(compactView = it))
                        }
                    )
                }
            )
        }
    }

    // Theme Dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Theme") },
            text = {
                Column {
                    AppTheme.values().forEach { theme ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onUpdate(appearance.copy(theme = theme))
                                    showThemeDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = appearance.theme == theme,
                                onClick = {
                                    onUpdate(appearance.copy(theme = theme))
                                    showThemeDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(theme.name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() })
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun AccountSettingsCard(
    account: AccountSettings,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            ListItem(
                headlineContent = { Text("Account") },
                supportingContent = { Text(account.email ?: "Not signed in") },
                leadingContent = { Icon(Icons.Default.AccountCircle, contentDescription = null) },
                trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) }
            )

            if (account.email != null) {
                HorizontalDivider()

                ListItem(
                    headlineContent = { Text("Last Sync") },
                    supportingContent = {
                        Text(
                            account.lastSyncTime?.toString() ?: "Never"
                        )
                    },
                    leadingContent = { Icon(Icons.Default.Sync, contentDescription = null) }
                )
            }
        }
    }
}

@Composable
private fun DataManagementCard(
    data: DataManagement,
    onClick: () -> Unit,
    onClearData: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            ListItem(
                headlineContent = { Text("Data Management") },
                supportingContent = { Text("Export, import, and backup") },
                leadingContent = { Icon(Icons.Default.Storage, contentDescription = null) },
                trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                modifier = Modifier.clickable(onClick = onClick)
            )

            HorizontalDivider()

            ListItem(
                headlineContent = { Text("Storage Used") },
                supportingContent = { Text("%.2f MB".format(data.storageUsedMb)) },
                leadingContent = { Icon(Icons.Default.Storage, contentDescription = null) }
            )

            HorizontalDivider()

            ListItem(
                headlineContent = { Text("Clear All Data") },
                supportingContent = { Text("Delete all trips, locations, photos, and settings") },
                leadingContent = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                modifier = Modifier.clickable(onClick = onClearData)
            )
        }
    }
}

@Composable
private fun AlgorithmSettingsNavigationCard(
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.GraphicEq,
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
                    text = "Configure distance, bearing, and interpolation algorithms",
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

@Composable
private fun AboutCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "TrailGlass",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Version 1.0.0-alpha",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "A comprehensive location tracking and travel management app",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
