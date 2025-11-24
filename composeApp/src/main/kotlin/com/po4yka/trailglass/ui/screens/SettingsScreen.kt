package com.po4yka.trailglass.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.po4yka.trailglass.feature.tracking.LocationTrackingController
import com.po4yka.trailglass.ui.permissions.rememberLocationPermissionLauncher
import com.po4yka.trailglass.ui.permissions.rememberLocationPermissionState
import com.po4yka.trailglass.ui.screens.settings.AboutCard
import com.po4yka.trailglass.ui.screens.settings.PermissionsCard
import com.po4yka.trailglass.ui.screens.settings.SettingsNavigationCard
import com.po4yka.trailglass.ui.screens.settings.SettingsSectionHeader
import com.po4yka.trailglass.ui.screens.settings.TrackingModeCard
import com.po4yka.trailglass.ui.screens.settings.TrackingStatusCard

/** Settings screen for app configuration. */
@Composable
fun SettingsScreen(
    trackingController: LocationTrackingController,
    onNavigateToDeviceManagement: () -> Unit = {},
    onNavigateToAlgorithmSettings: () -> Unit = {},
    onNavigateToLogs: () -> Unit = {},
    onNavigateToDiagnostics: () -> Unit = {},
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
            SettingsSectionHeader(text = "Location Tracking")
        }

        item {
            TrackingStatusCard(
                isTracking = uiState.trackingState.isTracking,
                mode = uiState.trackingState.mode,
                samplesRecorded = uiState.trackingState.samplesRecordedToday,
                hasPermissions = uiState.hasPermissions,
                onStartTracking = { trackingController.startTracking(uiState.trackingState.mode) },
                onStopTracking = { trackingController.stopTracking() },
                onRequestPermissions = { permissionLauncher.launch() }
            )
        }

        item {
            TrackingModeCard(
                currentMode = uiState.trackingState.mode,
                isTracking = uiState.trackingState.isTracking,
                onModeSelected = { mode ->
                    if (!uiState.trackingState.isTracking) {
                        trackingController.startTracking(mode)
                    }
                }
            )
        }

        // Permissions section
        item {
            SettingsSectionHeader(text = "Permissions")
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
            SettingsSectionHeader(text = "Advanced")
        }

        item {
            SettingsNavigationCard(
                icon = Icons.Default.Settings,
                title = "Algorithm Settings",
                description = "Configure distance, bearing, and interpolation",
                onClick = onNavigateToAlgorithmSettings
            )
        }

        // Account section
        item {
            SettingsSectionHeader(text = "Account")
        }

        item {
            SettingsNavigationCard(
                icon = Icons.Default.Devices,
                title = "Device Management",
                description = "Manage your connected devices",
                onClick = onNavigateToDeviceManagement
            )
        }

        // Developer section
        item {
            SettingsSectionHeader(text = "Developer")
        }

        item {
            SettingsNavigationCard(
                icon = Icons.Default.Description,
                title = "Logs",
                description = "View application logs",
                onClick = onNavigateToLogs
            )
        }

        item {
            SettingsNavigationCard(
                icon = Icons.Default.BugReport,
                title = "Diagnostics",
                description = "View system diagnostics and status",
                onClick = onNavigateToDiagnostics
            )
        }

        // About section
        item {
            SettingsSectionHeader(text = "About")
        }

        item {
            AboutCard()
        }
    }
}
