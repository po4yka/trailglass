package com.po4yka.trailglass.location.tracking

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/** Configuration for location tracking behavior. */
data class TrackingConfiguration(
    /** Minimum time interval between location updates in SIGNIFICANT mode. */
    val significantInterval: Duration = 5.minutes,
    /** Minimum distance between location updates in SIGNIFICANT mode (meters). */
    val significantDistance: Double = 500.0,
    /** Desired accuracy for location in SIGNIFICANT mode (meters). */
    val significantAccuracy: Double = 500.0,
    /** Minimum time interval between location updates in PASSIVE mode. */
    val passiveInterval: Duration = 5.minutes,
    /** Minimum distance between location updates in PASSIVE mode (meters). */
    val passiveDistance: Double = 500.0,
    /** Minimum time interval between location updates in ACTIVE mode. */
    val activeInterval: Duration = 30.seconds,
    /** Minimum distance between location updates in ACTIVE mode (meters). */
    val activeDistance: Double = 10.0,
    /**
     * Desired accuracy for location in ACTIVE mode (meters). Platform-specific implementations will use their best
     * approximation.
     */
    val activeAccuracy: Double = 10.0,
    /** Desired accuracy for location in PASSIVE mode (meters). */
    val passiveAccuracy: Double = 100.0,
    /** Whether to request background location permission. */
    val requireBackgroundLocation: Boolean = true,
    /** Whether to show a notification during active tracking (Android). */
    val showNotification: Boolean = true,
    /** Notification title for active tracking (Android). */
    val notificationTitle: String = "TrailGlass",
    /** Notification message for active tracking (Android). */
    val notificationMessage: String = "Recording your location"
)

/** Default tracking configurations for common scenarios. */
object TrackingConfigurations {
    /** Battery-optimized configuration. Suitable for all-day background tracking. */
    val BATTERY_OPTIMIZED =
        TrackingConfiguration(
            significantInterval = 5.minutes,
            significantDistance = 500.0,
            significantAccuracy = 500.0,
            passiveInterval = 10.minutes,
            passiveDistance = 1000.0,
            activeInterval = 1.minutes,
            activeDistance = 50.0,
            activeAccuracy = 50.0,
            passiveAccuracy = 200.0
        )

    /** Balanced configuration. Good compromise between accuracy and battery life. */
    val BALANCED =
        TrackingConfiguration(
            significantInterval = 5.minutes,
            significantDistance = 500.0,
            significantAccuracy = 500.0,
            passiveInterval = 5.minutes,
            passiveDistance = 500.0,
            activeInterval = 30.seconds,
            activeDistance = 10.0,
            activeAccuracy = 10.0,
            passiveAccuracy = 100.0
        )

    /** High accuracy configuration. Best for active trip recording, higher battery usage. */
    val HIGH_ACCURACY =
        TrackingConfiguration(
            significantInterval = 5.minutes,
            significantDistance = 500.0,
            significantAccuracy = 500.0,
            passiveInterval = 2.minutes,
            passiveDistance = 100.0,
            activeInterval = 10.seconds,
            activeDistance = 5.0,
            activeAccuracy = 5.0,
            passiveAccuracy = 50.0
        )
}
