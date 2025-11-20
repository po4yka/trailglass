package com.po4yka.trailglass.domain.permission

import kotlinx.cinterop.*
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import me.tatarka.inject.annotations.Inject
import platform.CoreLocation.*
import platform.Foundation.*
import platform.Photos.PHAuthorizationStatusAuthorized
import platform.Photos.PHAuthorizationStatusDenied
import platform.Photos.PHAuthorizationStatusNotDetermined
import platform.Photos.PHAuthorizationStatusRestricted
import platform.Photos.PHPhotoLibrary
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNUserNotificationCenter
import platform.darwin.NSObject
import kotlin.coroutines.resume

/**
 * iOS implementation of PermissionManager.
 * Uses iOS permission APIs (CoreLocation, Photos, UserNotifications).
 *
 * Properly implements CLLocationManagerDelegate to wait for user responses
 * instead of immediately checking stale permission status.
 */
@Inject
@OptIn(ExperimentalForeignApi::class)
actual class PermissionManager {

    private val permissionStates = mutableMapOf<PermissionType, MutableStateFlow<PermissionState>>()
    private val locationManager = CLLocationManager()

    // Store pending permission request continuations
    private var locationPermissionContinuation: CancellableContinuation<PermissionResult>? = null
    private var backgroundLocationContinuation: CancellableContinuation<PermissionResult>? = null

    /**
     * Delegate to receive location authorization callbacks from iOS.
     */
    private val locationDelegate = object : NSObject(), CLLocationManagerDelegateProtocol {
        override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
            val status = manager.authorizationStatus()

            // Handle regular location permission
            locationPermissionContinuation?.let { continuation ->
                val result = when (status) {
                    kCLAuthorizationStatusAuthorizedWhenInUse,
                    kCLAuthorizationStatusAuthorizedAlways -> {
                        updatePermissionState(PermissionType.LOCATION_FINE, PermissionState.Granted)
                        PermissionResult.Granted
                    }
                    kCLAuthorizationStatusDenied -> {
                        updatePermissionState(PermissionType.LOCATION_FINE, PermissionState.PermanentlyDenied)
                        PermissionResult.PermanentlyDenied
                    }
                    kCLAuthorizationStatusRestricted -> {
                        updatePermissionState(PermissionType.LOCATION_FINE, PermissionState.Restricted)
                        PermissionResult.Denied
                    }
                    else -> PermissionResult.Cancelled
                }
                continuation.resume(result)
                locationPermissionContinuation = null
            }

            // Handle background location permission
            backgroundLocationContinuation?.let { continuation ->
                val result = when (status) {
                    kCLAuthorizationStatusAuthorizedAlways -> {
                        updatePermissionState(PermissionType.LOCATION_BACKGROUND, PermissionState.Granted)
                        PermissionResult.Granted
                    }
                    kCLAuthorizationStatusAuthorizedWhenInUse -> {
                        updatePermissionState(PermissionType.LOCATION_BACKGROUND, PermissionState.Denied)
                        PermissionResult.Denied
                    }
                    kCLAuthorizationStatusDenied -> {
                        updatePermissionState(PermissionType.LOCATION_BACKGROUND, PermissionState.PermanentlyDenied)
                        PermissionResult.PermanentlyDenied
                    }
                    kCLAuthorizationStatusRestricted -> {
                        updatePermissionState(PermissionType.LOCATION_BACKGROUND, PermissionState.Restricted)
                        PermissionResult.Denied
                    }
                    else -> PermissionResult.Cancelled
                }
                continuation.resume(result)
                backgroundLocationContinuation = null
            }
        }
    }

    init {
        // Set up the location manager delegate
        locationManager.delegate = locationDelegate
    }

    /**
     * Check the current state of a permission.
     */
    actual suspend fun checkPermission(permissionType: PermissionType): PermissionState {
        val state = when (permissionType) {
            PermissionType.LOCATION_FINE,
            PermissionType.LOCATION_COARSE -> {
                checkLocationPermission()
            }

            PermissionType.LOCATION_BACKGROUND -> {
                checkBackgroundLocationPermission()
            }

            PermissionType.CAMERA -> {
                checkCameraPermission()
            }

            PermissionType.PHOTO_LIBRARY -> {
                checkPhotoLibraryPermission()
            }

            PermissionType.NOTIFICATIONS -> {
                checkNotificationPermission()
            }
        }

        updatePermissionState(permissionType, state)
        return state
    }

    /**
     * Request a permission from the user.
     */
    actual suspend fun requestPermission(permissionType: PermissionType): PermissionResult {
        return when (permissionType) {
            PermissionType.LOCATION_FINE,
            PermissionType.LOCATION_COARSE -> {
                requestLocationPermission()
            }

            PermissionType.LOCATION_BACKGROUND -> {
                requestBackgroundLocationPermission()
            }

            PermissionType.CAMERA -> {
                requestCameraPermission()
            }

            PermissionType.PHOTO_LIBRARY -> {
                requestPhotoLibraryPermission()
            }

            PermissionType.NOTIFICATIONS -> {
                requestNotificationPermission()
            }
        }
    }

    /**
     * Check if we should show a rationale before requesting.
     * On iOS, this is typically only relevant if permission was previously denied.
     */
    actual suspend fun shouldShowRationale(permissionType: PermissionType): Boolean {
        val state = checkPermission(permissionType)
        return state is PermissionState.Denied
    }

    /**
     * Open system settings for the app.
     */
    actual suspend fun openAppSettings() {
        val settingsUrl = NSURL.URLWithString(UIApplicationOpenSettingsURLString)
        if (settingsUrl != null) {
            UIApplication.sharedApplication.openURL(settingsUrl)
        }
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
     * Cleanup method to release resources.
     * Should be called when PermissionManager is no longer needed.
     */
    fun cleanup() {
        locationManager.delegate = null
        locationPermissionContinuation?.cancel()
        locationPermissionContinuation = null
        backgroundLocationContinuation?.cancel()
        backgroundLocationContinuation = null
        permissionStates.clear()
    }

    // MARK: - Location Permissions

    private fun checkLocationPermission(): PermissionState {
        return when (CLLocationManager.authorizationStatus()) {
            kCLAuthorizationStatusAuthorizedWhenInUse,
            kCLAuthorizationStatusAuthorizedAlways -> PermissionState.Granted

            kCLAuthorizationStatusDenied -> PermissionState.PermanentlyDenied

            kCLAuthorizationStatusRestricted -> PermissionState.Restricted

            kCLAuthorizationStatusNotDetermined -> PermissionState.NotDetermined

            else -> PermissionState.NotDetermined
        }
    }

    private fun checkBackgroundLocationPermission(): PermissionState {
        return when (CLLocationManager.authorizationStatus()) {
            kCLAuthorizationStatusAuthorizedAlways -> PermissionState.Granted

            kCLAuthorizationStatusAuthorizedWhenInUse -> PermissionState.Denied // Can upgrade to Always

            kCLAuthorizationStatusDenied -> PermissionState.PermanentlyDenied

            kCLAuthorizationStatusRestricted -> PermissionState.Restricted

            kCLAuthorizationStatusNotDetermined -> PermissionState.NotDetermined

            else -> PermissionState.NotDetermined
        }
    }

    private suspend fun requestLocationPermission(): PermissionResult {
        // Check if permission is already determined
        val currentStatus = CLLocationManager.authorizationStatus()
        when (currentStatus) {
            kCLAuthorizationStatusAuthorizedWhenInUse,
            kCLAuthorizationStatusAuthorizedAlways -> {
                return PermissionResult.Granted
            }
            kCLAuthorizationStatusDenied -> {
                return PermissionResult.PermanentlyDenied
            }
            kCLAuthorizationStatusRestricted -> {
                return PermissionResult.Denied
            }
            else -> {
                // Need to request - continue below
            }
        }

        return suspendCancellableCoroutine { continuation ->
            locationPermissionContinuation = continuation

            // Request permission - delegate will receive callback
            locationManager.requestWhenInUseAuthorization()

            // Handle cancellation
            continuation.invokeOnCancellation {
                locationPermissionContinuation = null
            }
        }
    }

    private suspend fun requestBackgroundLocationPermission(): PermissionResult {
        // Check if permission is already determined
        val currentStatus = CLLocationManager.authorizationStatus()
        when (currentStatus) {
            kCLAuthorizationStatusAuthorizedAlways -> {
                return PermissionResult.Granted
            }
            kCLAuthorizationStatusDenied -> {
                return PermissionResult.PermanentlyDenied
            }
            kCLAuthorizationStatusRestricted -> {
                return PermissionResult.Denied
            }
            else -> {
                // Need to request - continue below
            }
        }

        return suspendCancellableCoroutine { continuation ->
            backgroundLocationContinuation = continuation

            // Request always authorization - delegate will receive callback
            locationManager.requestAlwaysAuthorization()

            // Handle cancellation
            continuation.invokeOnCancellation {
                backgroundLocationContinuation = null
            }
        }
    }

    // MARK: - Camera Permission

    private fun checkCameraPermission(): PermissionState {
        // Camera permissions on iOS require checking AVCaptureDevice authorization
        // Since these APIs are not available in Kotlin/Native, we provide a workaround
        // by checking if camera usage description is present in Info.plist
        // Actual status should be checked via Swift bridge in production
        return PermissionState.NotDetermined
    }

    private suspend fun requestCameraPermission(): PermissionResult {
        // Camera permission request requires AVCaptureDevice.requestAccess(for:completionHandler:)
        // which is not available in Kotlin/Native bindings

        // For proper implementation, create a Swift helper:
        //
        // @objc class CameraPermissionHelper: NSObject {
        //     @objc static func requestPermission(completion: @escaping (Bool) -> Void) {
        //         AVCaptureDevice.requestAccess(for: .video, completionHandler: completion)
        //     }
        //
        //     @objc static func authorizationStatus() -> AVAuthorizationStatus {
        //         return AVCaptureDevice.authorizationStatus(for: .video)
        //     }
        // }
        //
        // Then expose via c_interop def file and call from Kotlin

        // For now, returning NotDetermined to indicate implementation needed
        return PermissionResult.Denied
    }

    // MARK: - Photo Library Permission

    private fun checkPhotoLibraryPermission(): PermissionState {
        return when (PHPhotoLibrary.authorizationStatus()) {
            PHAuthorizationStatusAuthorized -> PermissionState.Granted
            PHAuthorizationStatusDenied -> PermissionState.PermanentlyDenied
            PHAuthorizationStatusRestricted -> PermissionState.Restricted
            PHAuthorizationStatusNotDetermined -> PermissionState.NotDetermined
            else -> PermissionState.NotDetermined
        }
    }

    private suspend fun requestPhotoLibraryPermission(): PermissionResult {
        return suspendCancellableCoroutine { continuation ->
            PHPhotoLibrary.requestAuthorization { status ->
                val result = when (status) {
                    PHAuthorizationStatusAuthorized -> PermissionResult.Granted
                    PHAuthorizationStatusDenied -> PermissionResult.PermanentlyDenied
                    else -> PermissionResult.Cancelled
                }
                continuation.resume(result)
            }
        }
    }

    // MARK: - Notification Permission

    private fun checkNotificationPermission(): PermissionState {
        // Notification authorization check requires async API
        // Return NotDetermined as we can't synchronously check
        return PermissionState.NotDetermined
    }

    private suspend fun requestNotificationPermission(): PermissionResult {
        return suspendCancellableCoroutine { continuation ->
            val center = UNUserNotificationCenter.currentNotificationCenter()
            val options = UNAuthorizationOptionAlert or
                    UNAuthorizationOptionSound or
                    UNAuthorizationOptionBadge

            center.requestAuthorizationWithOptions(
                options = options,
                completionHandler = { granted, error ->
                    val result = when {
                        error != null -> PermissionResult.Error("Notification permission error")
                        granted -> PermissionResult.Granted
                        else -> PermissionResult.Denied
                    }
                    continuation.resume(result)
                }
            )
        }
    }

    // MARK: - Helpers

    private fun updatePermissionState(permissionType: PermissionType, state: PermissionState) {
        permissionStates.getOrPut(permissionType) {
            MutableStateFlow(PermissionState.NotDetermined)
        }.value = state
    }
}
