package com.po4yka.trailglass.domain.permission

import kotlinx.coroutines.flow.StateFlow

/**
 * Platform-agnostic permission manager.
 * Handles permission checking, requesting, and providing rationales.
 */
expect class PermissionManager {
    /**
     * Check the current state of a permission.
     */
    suspend fun checkPermission(permissionType: PermissionType): PermissionState

    /**
     * Request a permission from the user.
     * May show system permission dialog.
     */
    suspend fun requestPermission(permissionType: PermissionType): PermissionResult

    /**
     * Check if we should show a rationale before requesting.
     * Returns true if the user has denied the permission before.
     */
    suspend fun shouldShowRationale(permissionType: PermissionType): Boolean

    /**
     * Open system settings for the app.
     * Used when permission is permanently denied.
     */
    suspend fun openAppSettings()

    /**
     * Observable state for permission changes.
     */
    fun observePermission(permissionType: PermissionType): StateFlow<PermissionState>
}

/**
 * Provides user-friendly permission rationales.
 */
class PermissionRationaleProvider {

    /**
     * Get rationale for a specific permission type.
     */
    fun getRationale(permissionType: PermissionType): PermissionRationale {
        return when (permissionType) {
            PermissionType.LOCATION_FINE -> PermissionRationale(
                permissionType = PermissionType.LOCATION_FINE,
                title = "Precise Location Access",
                description = "TrailGlass needs precise location access to automatically record your visits and track your journeys.",
                features = listOf(
                    "Automatically detect when you arrive at and leave places",
                    "Accurately map your routes and paths",
                    "Calculate distances and durations of your trips",
                    "Show your real-time location on the map"
                ),
                isRequired = true
            )

            PermissionType.LOCATION_COARSE -> PermissionRationale(
                permissionType = PermissionType.LOCATION_COARSE,
                title = "Approximate Location Access",
                description = "TrailGlass can use approximate location for basic tracking features.",
                features = listOf(
                    "Detect general area visits",
                    "Provide approximate trip information",
                    "Lower battery usage than precise location"
                ),
                isRequired = false
            )

            PermissionType.LOCATION_BACKGROUND -> PermissionRationale(
                permissionType = PermissionType.LOCATION_BACKGROUND,
                title = "Background Location Access",
                description = "To automatically track your trips even when the app is closed, TrailGlass needs background location access.",
                features = listOf(
                    "Track your location continuously without keeping the app open",
                    "Never miss a place visit or route segment",
                    "Seamlessly record multi-day trips",
                    "Automatic tracking start/stop based on movement"
                ),
                isRequired = false
            )

            PermissionType.CAMERA -> PermissionRationale(
                permissionType = PermissionType.CAMERA,
                title = "Camera Access",
                description = "Take photos to remember your visits and journeys.",
                features = listOf(
                    "Capture moments during your trips",
                    "Attach photos to specific place visits",
                    "Build a visual timeline of your travels"
                ),
                isRequired = false
            )

            PermissionType.PHOTO_LIBRARY -> PermissionRationale(
                permissionType = PermissionType.PHOTO_LIBRARY,
                title = "Photo Library Access",
                description = "Access your existing photos to attach to your visits.",
                features = listOf(
                    "Select photos from your library",
                    "Attach existing photos to place visits",
                    "Automatically suggest photos based on time and location"
                ),
                isRequired = false
            )

            PermissionType.NOTIFICATIONS -> PermissionRationale(
                permissionType = PermissionType.NOTIFICATIONS,
                title = "Notifications",
                description = "Stay informed about your trips and sync status.",
                features = listOf(
                    "Get notified when tracking starts or stops",
                    "Receive sync completion notifications",
                    "Stay updated on trip milestones",
                    "Get reminders to review your visits"
                ),
                isRequired = false
            )
        }
    }

    /**
     * Get a user-friendly explanation for why permission was denied.
     */
    fun getDeniedExplanation(permissionType: PermissionType, isPermanent: Boolean): String {
        val permissionName = when (permissionType) {
            PermissionType.LOCATION_FINE, PermissionType.LOCATION_COARSE -> "location"
            PermissionType.LOCATION_BACKGROUND -> "background location"
            PermissionType.CAMERA -> "camera"
            PermissionType.PHOTO_LIBRARY -> "photo library"
            PermissionType.NOTIFICATIONS -> "notifications"
        }

        return if (isPermanent) {
            "You've previously denied $permissionName access. To enable it, please go to Settings > TrailGlass and turn on $permissionName permission."
        } else {
            "TrailGlass needs $permissionName access to work properly. You can grant permission in the next dialog."
        }
    }

    /**
     * Get settings navigation instructions for the platform.
     */
    fun getSettingsInstructions(permissionType: PermissionType): SettingsInstructions {
        val permissionName = when (permissionType) {
            PermissionType.LOCATION_FINE, PermissionType.LOCATION_COARSE -> "Location"
            PermissionType.LOCATION_BACKGROUND -> "Location (Allow all the time)"
            PermissionType.CAMERA -> "Camera"
            PermissionType.PHOTO_LIBRARY -> "Photos"
            PermissionType.NOTIFICATIONS -> "Notifications"
        }

        return SettingsInstructions(
            permissionType = permissionType,
            steps = listOf(
                "Open Settings app",
                "Find and tap TrailGlass",
                "Tap $permissionName",
                "Select the appropriate permission level"
            ),
            quickAction = "Open Settings"
        )
    }
}

/**
 * Instructions for enabling permission in system settings.
 */
data class SettingsInstructions(
    val permissionType: PermissionType,
    val steps: List<String>,
    val quickAction: String
)
