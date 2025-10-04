package com.po4yka.trailglass.location.tracking

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.po4yka.trailglass.data.repository.LocationRepository
import com.po4yka.trailglass.domain.model.LocationSample
import com.po4yka.trailglass.domain.model.LocationSource
import com.po4yka.trailglass.logging.logger
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Clock
import java.util.UUID

/**
 * Android implementation of LocationTracker using FusedLocationProviderClient.
 */
class AndroidLocationTracker(
    private val context: Context,
    private val repository: LocationRepository,
    private val configuration: TrackingConfiguration,
    private val deviceId: String,
    private val userId: String
) : LocationTracker {

    private val logger = logger()
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val _trackingState = MutableStateFlow(TrackingState())
    override val trackingState: Flow<TrackingState> = _trackingState.asStateFlow()

    private val _locationUpdates = MutableSharedFlow<LocationSample>(replay = 0)
    override val locationUpdates: Flow<LocationSample> = _locationUpdates.asSharedFlow()

    private var locationCallback: LocationCallback? = null

    override suspend fun startTracking(mode: TrackingMode) {
        logger.info { "Starting location tracking in $mode mode" }

        if (!hasPermissions()) {
            logger.warn { "Cannot start tracking - missing location permissions" }
            return
        }

        // Stop any existing tracking
        stopTracking()

        val locationRequest = createLocationRequest(mode)
        locationCallback = createLocationCallback()

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                context.mainLooper
            )

            _trackingState.update { state ->
                state.copy(
                    mode = mode,
                    isTracking = true
                )
            }

            logger.info { "Location tracking started successfully" }
        } catch (e: SecurityException) {
            logger.error(e) { "Security exception when starting location tracking" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to start location tracking" }
        }
    }

    override suspend fun stopTracking() {
        logger.info { "Stopping location tracking" }

        locationCallback?.let { callback ->
            try {
                fusedLocationClient.removeLocationUpdates(callback)
                logger.debug { "Location updates removed" }
            } catch (e: Exception) {
                logger.error(e) { "Error removing location updates" }
            }
        }

        locationCallback = null

        _trackingState.update { state ->
            state.copy(
                mode = TrackingMode.IDLE,
                isTracking = false
            )
        }

        logger.info { "Location tracking stopped" }
    }

    override suspend fun hasPermissions(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        // Check background location permission for Android 10+
        val backgroundLocation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && configuration.requireBackgroundLocation) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Not required or not applicable
        }

        val hasAll = fineLocation && coarseLocation && backgroundLocation
        logger.debug { "Permission check: fine=$fineLocation, coarse=$coarseLocation, background=$backgroundLocation" }

        return hasAll
    }

    override suspend fun requestPermissions(): Boolean {
        // Note: This method should be called from an Activity context
        // The actual permission request needs to be handled by the UI layer
        logger.warn { "requestPermissions() called - this should be handled by the UI layer" }
        return false
    }

    override suspend fun getCurrentState(): TrackingState {
        return _trackingState.value
    }

    /**
     * Create a LocationRequest based on the tracking mode.
     */
    private fun createLocationRequest(mode: TrackingMode): LocationRequest {
        val (interval, distance, priority) = when (mode) {
            TrackingMode.PASSIVE -> Triple(
                configuration.passiveInterval.inWholeMilliseconds,
                configuration.passiveDistance.toFloat(),
                Priority.PRIORITY_BALANCED_POWER_ACCURACY
            )
            TrackingMode.ACTIVE -> Triple(
                configuration.activeInterval.inWholeMilliseconds,
                configuration.activeDistance.toFloat(),
                Priority.PRIORITY_HIGH_ACCURACY
            )
            TrackingMode.IDLE -> Triple(
                Long.MAX_VALUE,
                Float.MAX_VALUE,
                Priority.PRIORITY_PASSIVE
            )
        }

        return LocationRequest.Builder(priority, interval)
            .setMinUpdateDistanceMeters(distance)
            .setWaitForAccurateLocation(mode == TrackingMode.ACTIVE)
            .build()
    }

    /**
     * Create a LocationCallback that processes location updates.
     */
    private fun createLocationCallback(): LocationCallback {
        return object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.locations.forEach { location ->
                    processLocation(location)
                }
            }

            override fun onLocationAvailability(availability: LocationAvailability) {
                logger.debug { "Location availability changed: ${availability.isLocationAvailable}" }
            }
        }
    }

    /**
     * Process a location update and save it.
     */
    private fun processLocation(location: Location) {
        logger.debug {
            "Received location: (${location.latitude}, ${location.longitude}), " +
            "accuracy: ${location.accuracy}m"
        }

        val sample = LocationSample(
            id = "sample_${UUID.randomUUID()}",
            timestamp = Clock.System.now(),
            latitude = location.latitude,
            longitude = location.longitude,
            accuracy = location.accuracy.toDouble(),
            speed = if (location.hasSpeed()) location.speed.toDouble() else null,
            bearing = if (location.hasBearing()) location.bearing.toDouble() else null,
            source = when {
                location.provider == "gps" -> LocationSource.GPS
                location.provider == "network" -> LocationSource.NETWORK
                else -> LocationSource.GPS
            },
            deviceId = deviceId,
            userId = userId
        )

        // Emit to flow
        _locationUpdates.tryEmit(sample)

        // Update state
        _trackingState.update { state ->
            state.copy(
                lastLocation = sample,
                samplesRecordedToday = state.samplesRecordedToday + 1
            )
        }

        // Save to repository (fire and forget)
        kotlinx.coroutines.GlobalScope.launch {
            try {
                repository.insertSample(sample)
                logger.trace { "Location sample saved: ${sample.id}" }
            } catch (e: Exception) {
                logger.error(e) { "Failed to save location sample ${sample.id}" }
            }
        }
    }
}

// Suppress the deprecated GlobalScope warning - we're using it intentionally for fire-and-forget
@Suppress("DEPRECATION")
private fun <T> kotlinx.coroutines.GlobalScope.launch(block: suspend kotlinx.coroutines.CoroutineScope.() -> T) =
    kotlinx.coroutines.GlobalScope.launch { block() }
