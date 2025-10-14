package com.po4yka.trailglass.domain.service

import com.po4yka.trailglass.domain.model.Coordinate
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import platform.CoreLocation.*
import platform.Foundation.NSError
import platform.darwin.NSObject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * iOS implementation of LocationService using CoreLocation framework.
 *
 * This service provides real-time location updates for follow mode and location tracking.
 */
class IosLocationService : LocationService {

    private val locationManager = CLLocationManager()

    @Volatile
    private var isCurrentlyTracking = false

    override val locationUpdates: Flow<Coordinate> = callbackFlow {
        val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
            override fun locationManager(
                manager: CLLocationManager,
                didUpdateLocations: List<*>
            ) {
                @Suppress("UNCHECKED_CAST")
                val locations = didUpdateLocations as List<CLLocation>
                locations.lastOrNull()?.let { location ->
                    val coordinate = Coordinate(
                        latitude = location.coordinate.useContents { latitude },
                        longitude = location.coordinate.useContents { longitude }
                    )
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
                val status = manager.authorizationStatus
                if (status == kCLAuthorizationStatusDenied ||
                    status == kCLAuthorizationStatusRestricted) {
                    close(SecurityException("Location permission denied"))
                }
            }
        }

        // Check authorization before starting
        val authStatus = locationManager.authorizationStatus
        if (authStatus == kCLAuthorizationStatusDenied ||
            authStatus == kCLAuthorizationStatusRestricted) {
            close(SecurityException("Location permission not granted"))
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
        locationManager.allowsBackgroundLocationUpdates = false

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

    override suspend fun getLastKnownLocation(): Coordinate? = suspendCoroutine { continuation ->
        locationManager.location?.let { location ->
            val coordinate = Coordinate(
                latitude = location.coordinate.useContents { latitude },
                longitude = location.coordinate.useContents { longitude }
            )
            continuation.resume(coordinate)
        } ?: continuation.resume(null)
    }

    override suspend fun startTracking(intervalMs: Long, fastestIntervalMs: Long) {
        // Check authorization
        val authStatus = locationManager.authorizationStatus
        if (authStatus == kCLAuthorizationStatusDenied ||
            authStatus == kCLAuthorizationStatusRestricted) {
            throw SecurityException("Location permission not granted")
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

    override suspend fun hasLocationPermission(): Boolean {
        return when (locationManager.authorizationStatus) {
            kCLAuthorizationStatusAuthorizedAlways,
            kCLAuthorizationStatusAuthorizedWhenInUse -> true
            else -> false
        }
    }

    override fun isTracking(): Boolean = isCurrentlyTracking
}
