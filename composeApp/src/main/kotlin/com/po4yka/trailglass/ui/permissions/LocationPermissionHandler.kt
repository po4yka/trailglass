package com.po4yka.trailglass.ui.permissions

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/** Location permission state holder. */
open class LocationPermissionState(
    private val context: Context,
    private val onPermissionResult: (Boolean) -> Unit
) {
    var showRationaleDialog by mutableStateOf(false)
        private set

    var showBackgroundLocationEducation by mutableStateOf(false)
        private set

    var showBackgroundLocationInstructions by mutableStateOf(false)
        private set

    var permissionsGranted by mutableStateOf(checkPermissions())
        private set

    var backgroundPermissionGranted by mutableStateOf(checkBackgroundPermission())
        private set

    /** Check if all required location permissions are granted. */
    fun checkPermissions(): Boolean {
        val fineLocation =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        val coarseLocation =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        return fineLocation || coarseLocation
    }

    /** Check if background location permission is granted (Android 10+). */
    fun checkBackgroundPermission(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Not required on older versions
        }

    fun onPermissionGranted() {
        permissionsGranted = true
        backgroundPermissionGranted = checkBackgroundPermission()
        onPermissionResult(true)
    }

    fun onPermissionDenied() {
        permissionsGranted = false
        onPermissionResult(false)
    }

    fun onBackgroundPermissionGranted() {
        backgroundPermissionGranted = true
    }

    fun showRationale() {
        showRationaleDialog = true
    }

    fun dismissRationale() {
        showRationaleDialog = false
    }

    fun showBackgroundEducation() {
        showBackgroundLocationEducation = true
    }

    fun dismissBackgroundEducation() {
        showBackgroundLocationEducation = false
    }

    fun showBackgroundInstructions() {
        showBackgroundLocationInstructions = true
    }

    fun dismissBackgroundInstructions() {
        showBackgroundLocationInstructions = false
    }

    fun openAppSettings() {
        val intent =
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
        context.startActivity(intent)
    }

    /**
     * Request background location permission with proper educational flow. This follows Android best practices by
     * showing education first, then redirecting to settings (required on Android 11+).
     */
    fun requestBackgroundPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Show educational content first
            showBackgroundEducation()
        }
    }

    fun refresh() {
        permissionsGranted = checkPermissions()
        backgroundPermissionGranted = checkBackgroundPermission()
    }
}

/**
 * Composable that handles location permission requests.
 *
 * @param onPermissionResult Callback with permission grant result
 * @return LocationPermissionState that can be used to request permissions
 */
@Composable
fun rememberLocationPermissionState(onPermissionResult: (Boolean) -> Unit = {}): LocationPermissionState {
    val context = LocalContext.current
    val state = remember { LocationPermissionState(context, onPermissionResult) }

    // Launcher for basic location permissions
    val locationPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val granted = permissions.values.any { it }
            if (granted) {
                state.onPermissionGranted()
            } else {
                state.onPermissionDenied()
            }
        }

    // Launcher for background location permission (Android 10+)
    val backgroundPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                state.onPermissionGranted()
            }
        }

    // Update state when composition is active
    LaunchedEffect(Unit) {
        state.refresh()
    }

    // Show rationale dialog if needed
    if (state.showRationaleDialog) {
        LocationPermissionRationaleDialog(
            onDismiss = { state.dismissRationale() },
            onConfirm = {
                state.dismissRationale()
                state.openAppSettings()
            }
        )
    }

    // Show background location education
    if (state.showBackgroundLocationEducation) {
        BackgroundLocationBottomSheet(
            onOpenSettings = {
                state.dismissBackgroundEducation()
                state.openAppSettings()
            },
            onDismiss = {
                state.dismissBackgroundEducation()
            }
        )
    }

    // Show background location instructions
    if (state.showBackgroundLocationInstructions) {
        BackgroundLocationInstructionsDialog(
            onOpenSettings = {
                state.dismissBackgroundInstructions()
                state.openAppSettings()
            },
            onDismiss = {
                state.dismissBackgroundInstructions()
            }
        )
    }

    // Expose request function
    DisposableEffect(state) {
        // Store launcher reference in state for external access
        val requestPermissions: () -> Unit = {
            if (!state.checkPermissions()) {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                !state.checkBackgroundPermission()
            ) {
                // Request background permission separately
                backgroundPermissionLauncher.launch(
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            }
        }

        // Make request function accessible
        (state as? LocationPermissionStateWithRequest)?.requestFunction = requestPermissions

        onDispose { }
    }

    return state
}

/** Request location permissions. */
fun LocationPermissionState.requestPermissions() {
    (this as? LocationPermissionStateWithRequest)?.requestFunction?.invoke()
}

private class LocationPermissionStateWithRequest(
    context: Context,
    onPermissionResult: (Boolean) -> Unit
) : LocationPermissionState(context, onPermissionResult) {
    var requestFunction: (() -> Unit)? = null
}

@Composable
private fun LocationPermissionRationaleDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Location Permission Required") },
        text = {
            Text(
                "TrailGlass needs location permission to track your travels and show your position on the map. " +
                    "Please grant location permission in app settings."
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Open Settings")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Simple launcher-based approach for requesting location permissions. Use this as a helper in your Composable screens.
 *
 * Example:
 * ```
 * val permissionLauncher = rememberLocationPermissionLauncher { granted ->
 *     if (granted) {
 *         // Permission granted
 *     }
 * }
 *
 * Button(onClick = { permissionLauncher.launch() }) {
 *     Text("Request Permission")
 * }
 * ```
 */
@Composable
fun rememberLocationPermissionLauncher(onResult: (Boolean) -> Unit): LocationPermissionLauncher {
    val launcher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val granted = permissions.values.any { it }
            onResult(granted)
        }

    return remember {
        LocationPermissionLauncher {
            launcher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }
}

class LocationPermissionLauncher(
    private val launchFn: () -> Unit
) {
    fun launch() = launchFn()
}
