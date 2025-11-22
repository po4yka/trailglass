package com.po4yka.trailglass.domain.service

import com.po4yka.trailglass.domain.model.Coordinate
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import platform.CoreLocation.*
import platform.Foundation.NSError
import platform.darwin.NSObject
import kotlin.concurrent.Volatile
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * iOS implementation of LocationService using CoreLocation framework.
 *
 * This service provides real-time location updates for follow mode and location tracking.
 */
@OptIn(ExperimentalForeignApi::class)
class IosLocationService : LocationService {
    private val locationManager = CLLocationManager()

    @Volatile
    private var isCurrentlyTracking = false

    override val locationUpdates: Flow<Coordinate> =
        callbackFlow {
            val delegate =
                object : NSObject(), CLLocationManagerDelegateProtocol {
                    override fun locationManager(
                        manager: CLLocationManager,
                        didUpdateLocations: List<*>
                    ) {
                        @Suppress("UNCHECKED_CAST")
                        val locations = didUpdateLocations as List<CLLocation>
                        locations.lastOrNull()?.let { location ->
                            val coordinate =
                                location.coordinate.useContents {
                                    Coordinate(
                                        latitude = latitude,
                                        longitude = longitude
                                    )
                                }
                            trySend(coordinate)
                        }
                    }

                    override fun locationManager(
                        manager: CLLocationManager,
                        didFailWithError: NSError
                    ) {
                        close(Exception("Location update failed: ${didFailWithError.localizedDescription}"))
                    }

                    override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
                        val status = manager.authorizationStatus()
                        if (status == kCLAuthorizationStatusDenied ||
                            status == kCLAuthorizationStatusRestricted
                        ) {
                            close(IllegalStateException("Location permission denied"))
                        }
                    }
                }

            // Check authorization before starting
            val authStatus = locationManager.authorizationStatus()
            if (authStatus == kCLAuthorizationStatusDenied ||
                authStatus == kCLAuthorizationStatusRestricted
            ) {
                close(IllegalStateException("Location permission not granted"))
                return@callbackFlow
            }

            // Request authorization if not determined
            if (authStatus == kCLAuthorizationStatusNotDetermined) {
                locationManager.requestWhenInUseAuthorization()
            }

            // Configure location manager
            locationManager.delegate = delegate
            locationManager.desiredAccuracy = kCLLocationAccuracyBest
            locationManager.distanceFilter = 10.0 // Update every 10 meters

            // Enable background location updates
            // This requires "location" in UIBackgroundModes in Info.plist
            locationManager.allowsBackgroundLocationUpdates = true
            locationManager.pausesLocationUpdatesAutomatically = false

            // Start location updates
            locationManager.startUpdatingLocation()
            isCurrentlyTracking = true

            // Clean up when flow is cancelled
            awaitClose {
                locationManager.stopUpdatingLocation()
                locationManager.delegate = null
                isCurrentlyTracking = false
            }
        }

    override suspend fun getLastKnownLocation(): Coordinate? =
        suspendCoroutine { continuation ->
            locationManager.location?.let { location ->
                val coordinate =
                    location.coordinate.useContents {
                        Coordinate(
                            latitude = latitude,
                            longitude = longitude
                        )
                    }
                continuation.resume(coordinate)
            } ?: continuation.resume(null)
        }

    override suspend fun startTracking(
        intervalMs: Long,
        fastestIntervalMs: Long
    ) {
        // Check authorization
        val authStatus = locationManager.authorizationStatus()
        if (authStatus == kCLAuthorizationStatusDenied ||
            authStatus == kCLAuthorizationStatusRestricted
        ) {
            throw IllegalStateException("Location permission not granted")
        }

        if (authStatus == kCLAuthorizationStatusNotDetermined) {
            locationManager.requestWhenInUseAuthorization()
        }

        // Location updates are handled by the locationUpdates Flow
        // This method is primarily for explicit tracking control
        isCurrentlyTracking = true
    }

    override suspend fun stopTracking() {
        locationManager.stopUpdatingLocation()
        isCurrentlyTracking = false
    }

    override suspend fun hasLocationPermission(): Boolean =
        when (locationManager.authorizationStatus()) {
            kCLAuthorizationStatusAuthorizedAlways,
            kCLAuthorizationStatusAuthorizedWhenInUse -> true
            else -> false
        }

    override suspend fun requestLocationPermission(background: Boolean): Boolean {
        // Check current authorization status
        val currentStatus = locationManager.authorizationStatus()

        // If already authorized, return true
        if (currentStatus == kCLAuthorizationStatusAuthorizedAlways ||
            (currentStatus == kCLAuthorizationStatusAuthorizedWhenInUse && !background)
        ) {
            return true
        }

        // Request appropriate permission level
        if (background) {
            // Request always authorization for background tracking
            locationManager.requestAlwaysAuthorization()
        } else {
            // Request when-in-use authorization for foreground tracking
            locationManager.requestWhenInUseAuthorization()
        }

        // Note: The actual permission result comes asynchronously via the delegate
        // callback locationManagerDidChangeAuthorization. For immediate return,
        // we check if we already had permission or if the request was initiated.
        // The app should observe permission changes through the delegate.

        // Return current status - the actual grant/deny will be reflected in subsequent checks
        return hasLocationPermission()
    }

    override fun isTracking(): Boolean = isCurrentlyTracking
}
