package com.po4yka.trailglass.domain.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.po4yka.trailglass.domain.model.Coordinate
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import me.tatarka.inject.annotations.Inject

/**
 * Android implementation of LocationService using Google Play Services FusedLocationProviderClient.
 *
 * This service provides real-time location updates for follow mode and location tracking.
 */
@Inject
class AndroidLocationService(
    private val context: Context
) : LocationService {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    @Volatile
    private var isCurrentlyTracking = false

    override val locationUpdates: Flow<Coordinate> = callbackFlow {
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    val coordinate = Coordinate(
                        latitude = location.latitude,
                        longitude = location.longitude
                    )
                    trySend(coordinate)
                }
            }
        }

        // Check permissions before starting
        if (!hasLocationPermission()) {
            close(SecurityException("Location permission not granted"))
            return@callbackFlow
        }

        // Create location request
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000L // 5 seconds default interval
        ).apply {
            setMinUpdateIntervalMillis(2000L) // 2 seconds fastest interval
            setMaxUpdateDelayMillis(10000L) // 10 seconds max delay
        }.build()

        // Request location updates
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            isCurrentlyTracking = true
        } catch (e: SecurityException) {
            close(e)
            return@callbackFlow
        }

        // Clean up when flow is cancelled
        awaitClose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
            isCurrentlyTracking = false
        }
    }

    override suspend fun getLastKnownLocation(): Coordinate? {
        if (!hasLocationPermission()) {
            return null
        }

        return try {
            val location = fusedLocationClient.lastLocation.await()
            location?.let {
                Coordinate(
                    latitude = it.latitude,
                    longitude = it.longitude
                )
            }
        } catch (e: SecurityException) {
            null
        }
    }

    override suspend fun startTracking(intervalMs: Long, fastestIntervalMs: Long) {
        if (!hasLocationPermission()) {
            throw SecurityException("Location permission not granted")
        }

        // Location updates are handled by the locationUpdates Flow
        // This method is primarily for explicit tracking control
        isCurrentlyTracking = true
    }

    override suspend fun stopTracking() {
        isCurrentlyTracking = false
        // The Flow will handle cleanup when collectors are cancelled
    }

    override suspend fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun isTracking(): Boolean = isCurrentlyTracking
}
