package com.po4yka.trailglass.location

import com.po4yka.trailglass.logging.logger
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.CoreLocation.*
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970
import platform.darwin.NSObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Factory function to create iOS CurrentLocationProvider.
 */
actual fun createCurrentLocationProvider(): CurrentLocationProvider {
    return IOSCurrentLocationProvider()
}

/**
 * iOS implementation of CurrentLocationProvider using CLLocationManager.
 */
@OptIn(ExperimentalForeignApi::class)
class IOSCurrentLocationProvider : CurrentLocationProvider {
    private val logger = logger()
    private val locationManager = CLLocationManager()
    private val delegate = LocationDelegate()

    override var lastKnownLocation: LocationData? = null
        private set

    init {
        locationManager.delegate = delegate
        delegate.provider = this
    }

    override suspend fun getCurrentLocation(): Result<LocationData> {
        logger.debug { "Getting current location" }

        // Check authorization
        val authStatus = locationManager.authorizationStatus
        if (authStatus != kCLAuthorizationStatusAuthorizedWhenInUse &&
            authStatus != kCLAuthorizationStatusAuthorizedAlways
        ) {
            logger.warn { "Location authorization not granted: $authStatus" }
            return Result.failure(
                IllegalStateException("Location authorization not granted")
            )
        }

        // Try last known location first
        locationManager.location?.let { location ->
            val timestamp = (location.timestamp.timeIntervalSince1970 * 1000).toLong()
            if (isLocationFresh(timestamp)) {
                logger.debug {
                    "Using cached location: (${location.coordinate.useContents { latitude }}, ${location.coordinate.useContents { longitude }})"
                }
                val locationData = LocationData(
                    latitude = location.coordinate.useContents { latitude },
                    longitude = location.coordinate.useContents { longitude },
                    accuracy = location.horizontalAccuracy.toFloat(),
                    timestamp = timestamp
                )
                lastKnownLocation = locationData
                return Result.success(locationData)
            }
        }

        // Request fresh location
        logger.debug { "Requesting fresh location" }
        return try {
            val freshLocation = requestFreshLocation()
            lastKnownLocation = freshLocation
            Result.success(freshLocation)
        } catch (e: Exception) {
            logger.error(e) { "Failed to get current location" }
            Result.failure(e)
        }
    }

    override fun observeLocation(): Flow<LocationData> = callbackFlow {
        logger.debug { "Starting location observation" }

        val authStatus = locationManager.authorizationStatus
        if (authStatus != kCLAuthorizationStatusAuthorizedWhenInUse &&
            authStatus != kCLAuthorizationStatusAuthorizedAlways
        ) {
            logger.warn { "Location authorization not granted for observation" }
            close(IllegalStateException("Location authorization not granted"))
            return@callbackFlow
        }

        delegate.onLocationUpdate = { location ->
            val locationData = LocationData(
                latitude = location.coordinate.useContents { latitude },
                longitude = location.coordinate.useContents { longitude },
                accuracy = location.horizontalAccuracy.toFloat(),
                timestamp = (location.timestamp.timeIntervalSince1970 * 1000).toLong()
            )
            lastKnownLocation = locationData
            trySend(locationData)
        }

        delegate.onError = { error ->
            logger.error { "Location observation error: ${error.localizedDescription}" }
        }

        locationManager.desiredAccuracy = kCLLocationAccuracyBest
        locationManager.distanceFilter = 10.0 // 10 meters
        locationManager.startUpdatingLocation()

        logger.debug { "Location observation started" }

        awaitClose {
            logger.debug { "Stopping location observation" }
            locationManager.stopUpdatingLocation()
            delegate.onLocationUpdate = null
            delegate.onError = null
        }
    }

    /**
     * Request a fresh location update.
     */
    private suspend fun requestFreshLocation(): LocationData = suspendCancellableCoroutine { continuation ->
        var resumed = false

        delegate.onLocationUpdate = { location ->
            if (!resumed) {
                resumed = true
                locationManager.stopUpdatingLocation()
                delegate.onLocationUpdate = null
                delegate.onError = null

                val locationData = LocationData(
                    latitude = location.coordinate.useContents { latitude },
                    longitude = location.coordinate.useContents { longitude },
                    accuracy = location.horizontalAccuracy.toFloat(),
                    timestamp = (location.timestamp.timeIntervalSince1970 * 1000).toLong()
                )
                continuation.resume(locationData)
            }
        }

        delegate.onError = { error ->
            if (!resumed) {
                resumed = true
                locationManager.stopUpdatingLocation()
                delegate.onLocationUpdate = null
                delegate.onError = null

                continuation.resumeWithException(
                    Exception("Location error: ${error.localizedDescription}")
                )
            }
        }

        locationManager.desiredAccuracy = kCLLocationAccuracyBest
        locationManager.startUpdatingLocation()

        continuation.invokeOnCancellation {
            if (!resumed) {
                locationManager.stopUpdatingLocation()
                delegate.onLocationUpdate = null
                delegate.onError = null
            }
        }
    }

    /**
     * Check if location is fresh (less than 2 minutes old).
     */
    private fun isLocationFresh(timestamp: Long): Boolean {
        val currentTime = (NSDate().timeIntervalSince1970 * 1000).toLong()
        val ageMs = currentTime - timestamp
        return ageMs < FRESH_LOCATION_MAX_AGE_MS
    }

    /**
     * CLLocationManagerDelegate implementation.
     */
    private class LocationDelegate : NSObject(), CLLocationManagerDelegateProtocol {
        var provider: IOSCurrentLocationProvider? = null
        var onLocationUpdate: ((CLLocation) -> Unit)? = null
        var onError: ((platform.Foundation.NSError) -> Unit)? = null

        override fun locationManager(
            manager: CLLocationManager,
            didUpdateLocations: List<*>
        ) {
            // Safely filter to CLLocation instances to handle Kotlin/Objective-C interop
            val locations = didUpdateLocations.filterIsInstance<CLLocation>()
            locations.lastOrNull()?.let { location ->
                onLocationUpdate?.invoke(location)
            }
        }

        override fun locationManager(
            manager: CLLocationManager,
            didFailWithError: platform.Foundation.NSError
        ) {
            onError?.invoke(didFailWithError)
        }

        override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
            val status = manager.authorizationStatus
            provider?.logger?.info { "Authorization changed: $status" }
        }
    }

    companion object {
        private const val FRESH_LOCATION_MAX_AGE_MS = 2 * 60 * 1000L // 2 minutes
    }
}
