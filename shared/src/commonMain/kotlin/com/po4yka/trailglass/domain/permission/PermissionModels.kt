package com.po4yka.trailglass.domain.permission

/**
 * Represents the state of a permission request.
 */
sealed class PermissionState {
    /**
     * Permission has been granted.
     */
    data object Granted : PermissionState()

    /**
     * Permission has been denied, but can still be requested.
     */
    data object Denied : PermissionState()

    /**
     * Permission has been permanently denied (user selected "Don't ask again" on Android,
     * or denied multiple times on iOS).
     * The user must manually enable it in system settings.
     */
    data object PermanentlyDenied : PermissionState()

    /**
     * Permission status is not yet determined.
     */
    data object NotDetermined : PermissionState()

    /**
     * Permission is restricted (e.g., by parental controls or enterprise policy).
     */
    data object Restricted : PermissionState()
}

/**
 * Types of permissions used in the app.
 */
enum class PermissionType {
    /**
     * Fine location permission (GPS).
     * Required for: Location tracking, map follow mode, place visit detection.
     */
    LOCATION_FINE,

    /**
     * Coarse location permission (network-based).
     * Required for: Approximate location tracking.
     */
    LOCATION_COARSE,

    /**
     * Background location permission.
     * Required for: Tracking location when app is in background.
     */
    LOCATION_BACKGROUND,

    /**
     * Camera permission.
     * Required for: Taking photos for place visits.
     */
    CAMERA,

    /**
     * Photo library permission.
     * Required for: Accessing and attaching existing photos.
     */
    PHOTO_LIBRARY,

    /**
     * Notification permission.
     * Required for: Sync notifications, tracking reminders.
     */
    NOTIFICATIONS
}

/**
 * Information about why a permission is needed.
 */
data class PermissionRationale(
    val permissionType: PermissionType,
    val title: String,
    val description: String,
    val features: List<String>,
    val isRequired: Boolean
)

/**
 * Complete permission request state for UI.
 */
data class PermissionRequestState(
    val permissionType: PermissionType,
    val state: PermissionState,
    val rationale: PermissionRationale,
    val shouldShowRationale: Boolean = false,
    val canRequest: Boolean = true
)

/**
 * Result of a permission request.
 */
sealed class PermissionResult {
    /**
     * Permission was granted.
     */
    data object Granted : PermissionResult()

    /**
     * Permission was denied by user.
     */
    data object Denied : PermissionResult()

    /**
     * Permission was permanently denied (requires settings navigation).
     */
    data object PermanentlyDenied : PermissionResult()

    /**
     * Permission request was cancelled by user.
     */
    data object Cancelled : PermissionResult()

    /**
     * An error occurred during permission request.
     */
    data class Error(
        val message: String
    ) : PermissionResult()
}

/**
 * Action to take when permission is denied.
 */
sealed class PermissionAction {
    /**
     * Retry the permission request.
     */
    data object Retry : PermissionAction()

    /**
     * Open system settings to manually enable permission.
     */
    data object OpenSettings : PermissionAction()

    /**
     * Continue without the permission (if optional).
     */
    data object ContinueWithout : PermissionAction()

    /**
     * Cancel the operation requiring permission.
     */
    data object Cancel : PermissionAction()
}

/**
 * Extension to check if permission state allows usage.
 */
val PermissionState.isGranted: Boolean
    get() = this is PermissionState.Granted

/**
 * Extension to check if permission can be requested.
 */
val PermissionState.canRequest: Boolean
    get() = this is PermissionState.NotDetermined || this is PermissionState.Denied

/**
 * Extension to check if user must go to settings.
 */
val PermissionState.requiresSettings: Boolean
    get() = this is PermissionState.PermanentlyDenied
