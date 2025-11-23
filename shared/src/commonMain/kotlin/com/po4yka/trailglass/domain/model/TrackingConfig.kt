package com.po4yka.trailglass.domain.model

/**
 * Configuration for location tracking.
 *
 * Defines how frequently and accurately location should be tracked.
 */
data class TrackingConfig(
    /** Tracking accuracy mode. */
    val accuracy: TrackingAccuracy = TrackingAccuracy.BALANCED,
    /** Minimum time interval between location updates in milliseconds. */
    val updateIntervalMs: Long = 30000, // 30 seconds default
    /** Minimum distance between location updates in meters. */
    val distanceFilterMeters: Float = 10f,
    /** Whether to track in background. */
    val backgroundTracking: Boolean = true,
    /** Whether to detect trips automatically. */
    val autoTripDetection: Boolean = true,
    /** Whether to detect place visits automatically. */
    val autoPlaceVisitDetection: Boolean = true,
    /** Whether to detect transport mode automatically. */
    val autoTransportDetection: Boolean = true
)

/** Current tracking status. */
sealed class TrackingStatus {
    /** Tracking is stopped. */
    data object Stopped : TrackingStatus()

    /**
     * Tracking is active.
     *
     * @param config Current tracking configuration
     * @param lastUpdateTime Timestamp of last location update
     * @param locationsRecorded Number of locations recorded in current session
     */
    data class Active(
        val config: TrackingConfig,
        val lastUpdateTime: kotlinx.datetime.Instant?,
        val locationsRecorded: Int
    ) : TrackingStatus()

    /**
     * Tracking is paused (e.g., low battery, no permission).
     *
     * @param reason Reason for pause
     */
    data class Paused(
        val reason: String
    ) : TrackingStatus()
}
