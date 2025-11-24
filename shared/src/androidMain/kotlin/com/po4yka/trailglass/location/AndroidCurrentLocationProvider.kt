package com.po4yka.trailglass.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.po4yka.trailglass.logging.logger
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume

/**
 * Factory function to create Android CurrentLocationProvider.
 */
actual fun createCurrentLocationProvider(): CurrentLocationProvider {
    // This will be resolved by DI
    throw NotImplementedError("Use DI to create AndroidCurrentLocationProvider")
}

/**
 * Android implementation of CurrentLocationProvider using FusedLocationProviderClient.
 */
class AndroidCurrentLocationProvider(
    private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
) : CurrentLocationProvider {
    private val logger = logger()

    override var lastKnownLocation: LocationData? = null
        private set

    override suspend fun getCurrentLocation(): Result<LocationData> {
        logger.debug { "Getting current location" }

        // Check permissions
        if (!hasLocationPermissions()) {
            logger.warn { "Location permissions not granted" }
            return Result.failure(
                SecurityException("Location permissions not granted")
            )
        }

        return try {
            // Try to get last known location first (fast)
            val lastLocation = fusedLocationClient.lastLocation.await()
            if (lastLocation != null && isLocationFresh(lastLocation.time)) {
                logger.debug { "Using cached location: (${lastLocation.latitude}, ${lastLocation.longitude})" }
                val locationData = LocationData(
                    latitude = lastLocation.latitude,
                    longitude = lastLocation.longitude,
                    accuracy = lastLocation.accuracy,
                    timestamp = lastLocation.time
                )
                lastKnownLocation = locationData
                return Result.success(locationData)
            }

            // Request fresh location if cached is stale or not available
            logger.debug { "Requesting fresh location" }
            val freshLocation = requestFreshLocation()
            lastKnownLocation = freshLocation
            Result.success(freshLocation)
        } catch (e: SecurityException) {
            logger.error(e) { "Security exception getting location" }
            Result.failure(e)
        } catch (e: Exception) {
            logger.error(e) { "Failed to get current location" }
            Result.failure(e)
        }
    }

    override fun observeLocation(): Flow<LocationData> = callbackFlow {
        logger.debug { "Starting location observation" }

        if (!hasLocationPermissions()) {
            logger.warn { "Location permissions not granted for observation" }
            close(SecurityException("Location permissions not granted"))
            return@callbackFlow
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10000L // 10 seconds
        ).setMinUpdateIntervalMillis(5000L) // 5 seconds minimum
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    val locationData = LocationData(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        accuracy = location.accuracy,
                        timestamp = location.time
                    )
                    lastKnownLocation = locationData
                    trySend(locationData)
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                context.mainLooper
            )

            logger.debug { "Location observation started" }
        } catch (e: SecurityException) {
            logger.error(e) { "Security exception during location observation" }
            close(e)
        }

        awaitClose {
            logger.debug { "Stopping location observation" }
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    /**
     * Request a fresh location update.
     * Uses high accuracy and waits for result.
     */
    private suspend fun requestFreshLocation(): LocationData = suspendCancellableCoroutine { continuation ->
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            0L // Immediate
        ).setMaxUpdates(1) // Only one update
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    val locationData = LocationData(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        accuracy = location.accuracy,
                        timestamp = location.time
                    )
                    fusedLocationClient.removeLocationUpdates(this)
                    continuation.resume(locationData)
                } ?: run {
                    fusedLocationClient.removeLocationUpdates(this)
                    continuation.cancel(Exception("No location available"))
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                context.mainLooper
            )

            continuation.invokeOnCancellation {
                fusedLocationClient.removeLocationUpdates(locationCallback)
            }
        } catch (e: SecurityException) {
            continuation.cancel(e)
        }
    }

    /**
     * Check if location is fresh (less than 2 minutes old).
     */
    private fun isLocationFresh(timestamp: Long): Boolean {
        val ageMs = System.currentTimeMillis() - timestamp
        return ageMs < FRESH_LOCATION_MAX_AGE_MS
    }

    /**
     * Check if location permissions are granted.
     */
    private fun hasLocationPermissions(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineLocation && coarseLocation
    }

    companion object {
        private const val FRESH_LOCATION_MAX_AGE_MS = 2 * 60 * 1000L // 2 minutes
    }
}
