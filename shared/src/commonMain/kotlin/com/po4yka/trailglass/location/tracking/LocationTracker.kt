package com.po4yka.trailglass.location.tracking

import com.po4yka.trailglass.domain.model.LocationSample
import kotlinx.coroutines.flow.Flow

/**
 * Tracking mode for location updates.
 */
enum class TrackingMode {
    /**
     * No location tracking.
     */
    IDLE,

    /**
     * Passive location tracking (significant location changes only).
     * Best for battery efficiency, updates every ~500m or 5 minutes.
     */
    PASSIVE,

    /**
     * Active location tracking (continuous updates).
     * Higher accuracy and frequency, but more battery usage.
     * Typically used during trips or when user explicitly enables tracking.
     */
    ACTIVE
}

/**
 * Location tracking state.
 */
data class TrackingState(
    val mode: TrackingMode = TrackingMode.IDLE,
    val isTracking: Boolean = false,
    val lastLocation: LocationSample? = null,
    val samplesRecordedToday: Int = 0,
    val batteryOptimized: Boolean = true
)

/**
 * Platform-agnostic interface for location tracking.
 * Implementations handle platform-specific location APIs.
 */
interface LocationTracker {
    /**
     * Flow of tracking state updates.
     */
    val trackingState: Flow<TrackingState>

    /**
     * Flow of location samples as they are received.
     */
    val locationUpdates: Flow<LocationSample>

    /**
     * Start location tracking with the specified mode.
     *
     * @param mode The tracking mode to use
     */
    suspend fun startTracking(mode: TrackingMode)

    /**
     * Stop location tracking.
     */
    suspend fun stopTracking()

    /**
     * Check if location permissions are granted.
     *
     * @return true if all required permissions are granted
     */
    suspend fun hasPermissions(): Boolean

    /**
     * Request location permissions from the user.
     * Platform-specific implementations should show permission dialogs.
     *
     * @return true if permissions were granted
     */
    suspend fun requestPermissions(): Boolean

    /**
     * Get the current tracking state.
     */
    suspend fun getCurrentState(): TrackingState
}
