package com.po4yka.trailglass.location

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

/** Helper class for managing location permissions. */
class LocationPermissions(
    private val activity: ComponentActivity
) {
    private var onPermissionsResult: ((Boolean) -> Unit)? = null

    private val locationPermissionLauncher =
        activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val allGranted = permissions.entries.all { it.value }
            onPermissionsResult?.invoke(allGranted)
            onPermissionsResult = null
        }

    /** Check if all required location permissions are granted. */
    fun hasLocationPermissions(): Boolean {
        val fineLocation =
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        val coarseLocation =
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        return fineLocation && coarseLocation
    }

    /** Check if background location permission is granted (Android 10+). */
    fun hasBackgroundLocationPermission(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Not required on older versions
        }

    /** Request foreground location permissions (fine and coarse). */
    fun requestLocationPermissions(onResult: (Boolean) -> Unit) {
        onPermissionsResult = onResult

        val permissions =
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )

        locationPermissionLauncher.launch(permissions)
    }

    /**
     * Request background location permission (Android 10+). Should be called after foreground permissions are granted.
     */
    fun requestBackgroundLocationPermission(onResult: (Boolean) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            onPermissionsResult = onResult

            locationPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            )
        } else {
            onResult(true) // Not applicable
        }
    }

    /** Check if we should show rationale for location permissions. */
    fun shouldShowLocationRationale(): Boolean =
        activity.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) ||
            activity.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)

    /** Check if we should show rationale for background location permission. */
    fun shouldShowBackgroundLocationRationale(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            activity.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        } else {
            false
        }

    /** Open app settings to allow user to manually grant permissions. */
    fun openAppSettings() {
        val intent =
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", activity.packageName, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        activity.startActivity(intent)
    }

    companion object {
        /** Get required permissions for location tracking based on Android version. */
        fun getRequiredPermissions(includeBackground: Boolean = true): List<String> {
            val permissions =
                mutableListOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )

            if (includeBackground && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }

            return permissions
        }
    }
}
