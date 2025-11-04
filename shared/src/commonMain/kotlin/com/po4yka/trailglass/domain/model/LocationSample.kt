package com.po4yka.trailglass.domain.model

import kotlinx.datetime.Instant

/**
 * Raw GPS/network location point recorded by the app.
 * All higher-level timelines are derived from this data.
 */
data class LocationSample(
    val id: String,
    val timestamp: Instant,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Double,
    val speed: Double? = null,
    val bearing: Double? = null,
    val source: LocationSource,
    val tripId: String? = null,
    val uploadedAt: Instant? = null,
    val deviceId: String,
    val userId: String
)

/**
 * Location source types indicating where the location data originated.
 *
 * Platform-specific availability:
 * - GPS: Available on both Android and iOS
 * - NETWORK: Primarily used on Android (cell tower/WiFi positioning)
 * - VISIT: **iOS-ONLY** - Uses CLLocationManager's visit monitoring feature
 * - SIGNIFICANT_CHANGE: Available on both platforms but primarily used on iOS
 *
 * @see com.po4yka.trailglass.location.tracking.IOSLocationTracker for iOS visit monitoring
 * @see com.po4yka.trailglass.location.tracking.AndroidLocationTracker for Android location sources
 */
enum class LocationSource {
    /**
     * GPS satellite positioning - available on both platforms.
     * Provides high accuracy but higher battery consumption.
     */
    GPS,

    /**
     * Network-based positioning (cell towers, WiFi) - primarily Android.
     * Android's FusedLocationProviderClient distinguishes between GPS and network.
     * iOS CoreLocation doesn't provide this distinction.
     */
    NETWORK,

    /**
     * **iOS-ONLY** - CLLocationManager visit monitoring.
     *
     * iOS-specific feature that detects when a user arrives at and departs from
     * significant locations using low-power sensors and machine learning.
     *
     * Benefits:
     * - Very low battery impact
     * - Automatic place detection
     * - Works even when app is not actively tracking
     *
     * Android alternative: Manual clustering of GPS/NETWORK samples using DBSCAN.
     *
     * Platform Implementation:
     * - iOS: `IOSLocationTracker.startMonitoringVisits()`
     * - Android: Not available - use location clustering instead
     */
    VISIT,

    /**
     * Significant location change monitoring.
     *
     * Platform Implementation:
     * - iOS: `startMonitoringSignificantLocationChanges()` - updates only when
     *   device moves a significant distance (typically 500+ meters)
     * - Android: Can be simulated with low-frequency location updates
     *
     * Used for passive tracking with minimal battery impact.
     */
    SIGNIFICANT_CHANGE
}
