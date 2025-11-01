package com.po4yka.trailglass.domain.permission

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import me.tatarka.inject.annotations.Inject

/**
 * Android implementation of PermissionManager.
 * Uses Android's permission system (Manifest.permission) and ActivityCompat.
 */
@Inject
actual class PermissionManager(
    private val context: Context
) {

    private val permissionStates = mutableMapOf<PermissionType, MutableStateFlow<PermissionState>>()

    /**
     * Check the current state of a permission.
     */
    actual suspend fun checkPermission(permissionType: PermissionType): PermissionState {
        val androidPermission = getAndroidPermission(permissionType)

        return when {
            androidPermission == null -> PermissionState.NotDetermined

            ContextCompat.checkSelfPermission(
                context,
                androidPermission
            ) == PackageManager.PERMISSION_GRANTED -> {
                updatePermissionState(permissionType, PermissionState.Granted)
                PermissionState.Granted
            }

            shouldShowRequestPermissionRationale(androidPermission) -> {
                updatePermissionState(permissionType, PermissionState.Denied)
                PermissionState.Denied
            }

            else -> {
                // First time asking or permanently denied
                // We can't distinguish without tracking, so default to NotDetermined
                updatePermissionState(permissionType, PermissionState.NotDetermined)
                PermissionState.NotDetermined
            }
        }
    }

    /**
     * Request a permission from the user.
     *
     * Note: This implementation checks permission status and provides guidance.
     * The actual permission request dialog must be triggered from UI layer using
     * Activity Result API (recommended) or via Activity.requestPermissions().
     *
     * Integration pattern:
     * 1. Call this method to get current status
     * 2. If Denied/NotDetermined, use shouldShowRationale() to decide if rationale needed
     * 3. Show rationale UI if needed
     * 4. Trigger permission request via Activity Result API
     * 5. Update permission state via updatePermissionState() after result
     */
    actual suspend fun requestPermission(permissionType: PermissionType): PermissionResult {
        val androidPermission = getAndroidPermission(permissionType)
            ?: return PermissionResult.Error("Unknown permission type: $permissionType")

        // Check if already granted
        val currentStatus = ContextCompat.checkSelfPermission(context, androidPermission)
        if (currentStatus == PackageManager.PERMISSION_GRANTED) {
            updatePermissionState(permissionType, PermissionState.Granted)
            return PermissionResult.Granted
        }

        // Permission not granted - check if we should show rationale
        val shouldShowRationale = shouldShowRequestPermissionRationale(androidPermission)

        return if (shouldShowRationale) {
            // User has denied before but not permanently
            updatePermissionState(permissionType, PermissionState.Denied)
            PermissionResult.Denied
        } else {
            // Either first request or permanently denied
            // UI layer should call Activity Result API to show permission dialog
            updatePermissionState(permissionType, PermissionState.NotDetermined)
            PermissionResult.Cancelled // Indicates permission flow should be initiated from UI
        }
    }

    /**
     * Check if we should show a rationale before requesting.
     */
    actual suspend fun shouldShowRationale(permissionType: PermissionType): Boolean {
        val androidPermission = getAndroidPermission(permissionType) ?: return false

        return shouldShowRequestPermissionRationale(androidPermission)
    }

    /**
     * Open system settings for the app.
     */
    actual suspend fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    /**
     * Observable state for permission changes.
     */
    actual fun observePermission(permissionType: PermissionType): StateFlow<PermissionState> {
        return permissionStates.getOrPut(permissionType) {
            MutableStateFlow(PermissionState.NotDetermined)
        }
    }

    /**
     * Map PermissionType to Android permission string.
     */
    private fun getAndroidPermission(permissionType: PermissionType): String? {
        return when (permissionType) {
            PermissionType.LOCATION_FINE -> Manifest.permission.ACCESS_FINE_LOCATION

            PermissionType.LOCATION_COARSE -> Manifest.permission.ACCESS_COARSE_LOCATION

            PermissionType.LOCATION_BACKGROUND -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                } else {
                    null // Background location permission only exists on Android 10+
                }
            }

            PermissionType.CAMERA -> Manifest.permission.CAMERA

            PermissionType.PHOTO_LIBRARY -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Manifest.permission.READ_MEDIA_IMAGES
                } else {
                    Manifest.permission.READ_EXTERNAL_STORAGE
                }
            }

            PermissionType.NOTIFICATIONS -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Manifest.permission.POST_NOTIFICATIONS
                } else {
                    null // Notifications don't require permission before Android 13
                }
            }
        }
    }

    /**
     * Check if should show rationale using Activity.
     * This is a helper that tries to get the Activity from context.
     */
    private fun shouldShowRequestPermissionRationale(permission: String): Boolean {
        val activity = context as? Activity ?: return false
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    }

    /**
     * Update the cached permission state.
     */
    private fun updatePermissionState(permissionType: PermissionType, state: PermissionState) {
        permissionStates.getOrPut(permissionType) {
            MutableStateFlow(PermissionState.NotDetermined)
        }.value = state
    }
}
