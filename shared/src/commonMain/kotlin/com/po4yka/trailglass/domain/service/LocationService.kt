package com.po4yka.trailglass.domain.service

import com.po4yka.trailglass.domain.model.Coordinate
import kotlinx.coroutines.flow.Flow

/**
 * Service for tracking user location.
 *
 * This is a platform-specific service that provides real-time location updates.
 * Platform implementations should use native location APIs:
 * - Android: FusedLocationProviderClient
 * - iOS: CoreLocation
 */
interface LocationService {
    /**
     * Flow of location updates.
     *
     * Emits new coordinates as the user's location changes.
     * The flow is hot and will continue emitting until cancelled.
     */
    val locationUpdates: Flow<Coordinate>

    /**
     * Get the last known location.
     *
     * @return The last known coordinate, or null if not available
     */
    suspend fun getLastKnownLocation(): Coordinate?

    /**
     * Start tracking location updates.
     *
     * @param intervalMs Update interval in milliseconds (default: 5000ms)
     * @param fastestIntervalMs Fastest update interval in milliseconds (default: 2000ms)
     */
    suspend fun startTracking(
        intervalMs: Long = 5000,
        fastestIntervalMs: Long = 2000
    )

    /**
     * Stop tracking location updates.
     */
    suspend fun stopTracking()

    /**
     * Check if location permissions are granted.
     *
     * @return true if permissions are granted, false otherwise
     */
    suspend fun hasLocationPermission(): Boolean

    /**
     * Request location permissions from the user.
     *
     * Platform-specific behavior:
     * - Android: Returns false - permissions must be requested via Activity using Compose or Activity APIs
     * - iOS: Triggers system permission dialog and returns result
     *
     * @param background Whether to request background location permission (always-on)
     * @return true if permissions were granted, false otherwise or if platform requires UI-based request
     */
    suspend fun requestLocationPermission(background: Boolean = false): Boolean

    /**
     * Check if location tracking is currently active.
     *
     * @return true if tracking, false otherwise
     */
    fun isTracking(): Boolean
}
