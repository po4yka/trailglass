package com.po4yka.trailglass.location

import com.po4yka.trailglass.domain.model.Coordinate
import kotlinx.coroutines.flow.Flow

/**
 * Provides access to the device's current location.
 * Separated from LocationTracker to provide lightweight location access
 * without the overhead of continuous tracking.
 */
interface CurrentLocationProvider {
    /**
     * Get the current location.
     * Requests a fresh location update if needed.
     *
     * @return Result containing the current location or an error
     */
    suspend fun getCurrentLocation(): Result<LocationData>

    /**
     * Observe location updates.
     * Useful for real-time location monitoring without full tracking.
     *
     * @return Flow of location updates
     */
    fun observeLocation(): Flow<LocationData>

    /**
     * Get the last known location without requesting a fresh update.
     * This is cached and may be stale.
     *
     * @return Last known location or null if not available
     */
    val lastKnownLocation: LocationData?
}

/**
 * Lightweight location data for current location queries.
 * Simpler than LocationSample which includes tracking metadata.
 */
data class LocationData(
    val coordinate: Coordinate,
    val accuracy: Float,
    val timestamp: Long
) {
    constructor(
        latitude: Double,
        longitude: Double,
        accuracy: Float,
        timestamp: Long
    ) : this(
        coordinate = Coordinate(latitude, longitude),
        accuracy = accuracy,
        timestamp = timestamp
    )

    val latitude: Double get() = coordinate.latitude
    val longitude: Double get() = coordinate.longitude
}

/**
 * Factory function to create platform-specific CurrentLocationProvider.
 * This is implemented using expect/actual pattern.
 */
expect fun createCurrentLocationProvider(): CurrentLocationProvider
