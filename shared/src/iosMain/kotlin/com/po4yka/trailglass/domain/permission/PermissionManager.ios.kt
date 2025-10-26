package com.po4yka.trailglass.domain.permission

import kotlinx.cinterop.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import me.tatarka.inject.annotations.Inject
import platform.CoreLocation.*
import platform.Foundation.*
import platform.Photos.PHPhotoLibrary
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume

/**
 * iOS implementation of PermissionManager.
 * Uses iOS permission APIs (CoreLocation, Photos, UserNotifications).
 */
@Inject
@OptIn(ExperimentalForeignApi::class)
actual class PermissionManager {

    private val permissionStates = mutableMapOf<PermissionType, MutableStateFlow<PermissionState>>()
    private val locationManager = CLLocationManager()

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
                PermissionResult.Error("Camera permission must be requested via AVCaptureDevice")
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
        return suspendCancellableCoroutine { continuation ->
            // Note: In a real implementation, you would set up a delegate
            // to receive the authorization status callback
            locationManager.requestWhenInUseAuthorization()

            // For this example, we immediately check the status
            // In production, you'd wait for the delegate callback
            val status = CLLocationManager.authorizationStatus()
            val result = when (status) {
                kCLAuthorizationStatusAuthorizedWhenInUse,
                kCLAuthorizationStatusAuthorizedAlways -> PermissionResult.Granted

                kCLAuthorizationStatusDenied -> PermissionResult.PermanentlyDenied

                else -> PermissionResult.Cancelled
            }

            continuation.resume(result)
        }
    }

    private suspend fun requestBackgroundLocationPermission(): PermissionResult {
        return suspendCancellableCoroutine { continuation ->
            locationManager.requestAlwaysAuthorization()

            val status = CLLocationManager.authorizationStatus()
            val result = when (status) {
                kCLAuthorizationStatusAuthorizedAlways -> PermissionResult.Granted

                kCLAuthorizationStatusDenied -> PermissionResult.PermanentlyDenied

                else -> PermissionResult.Cancelled
            }

            continuation.resume(result)
        }
    }

    // MARK: - Camera Permission

    private fun checkCameraPermission(): PermissionState {
        // Camera permission check requires AVCaptureDevice
        // For this stub implementation, return NotDetermined
        return PermissionState.NotDetermined
    }

    // MARK: - Photo Library Permission

    private fun checkPhotoLibraryPermission(): PermissionState {
        return when (PHPhotoLibrary.authorizationStatus()) {
            3L -> PermissionState.Granted // PHAuthorizationStatusAuthorized

            2L -> PermissionState.PermanentlyDenied // PHAuthorizationStatusDenied

            1L -> PermissionState.Restricted // PHAuthorizationStatusRestricted

            0L -> PermissionState.NotDetermined // PHAuthorizationStatusNotDetermined

            else -> PermissionState.NotDetermined
        }
    }

    private suspend fun requestPhotoLibraryPermission(): PermissionResult {
        return suspendCancellableCoroutine { continuation ->
            PHPhotoLibrary.requestAuthorization { status ->
                val result = when (status) {
                    3L -> PermissionResult.Granted // Authorized
                    2L -> PermissionResult.PermanentlyDenied // Denied
                    else -> PermissionResult.Cancelled
                }
                continuation.resume(result)
            }
        }
    }

    // MARK: - Notification Permission

    private fun checkNotificationPermission(): PermissionState {
        // Notification authorization check requires async API
        // For this stub, return NotDetermined
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
