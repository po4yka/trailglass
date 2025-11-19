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
 * - VISIT: Available on both platforms (native on iOS, emulated on Android)
 * - SIGNIFICANT_CHANGE: Available on both platforms but primarily used on iOS
 *
 * @see com.po4yka.trailglass.location.tracking.IOSLocationTracker for iOS visit monitoring
 * @see com.po4yka.trailglass.location.tracking.AndroidVisitDetector for Android visit detection
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
     * Visit detection - significant location stays.
     *
     * Detects when a user arrives at and departs from significant locations
     * with low battery impact. Both platforms now support this feature with
     * different underlying implementations.
     *
     * **Platform Implementations:**
     *
     * **iOS:** Native CLLocationManager visit monitoring
     * - Uses iOS's built-in machine learning and low-power sensors
     * - `IOSLocationTracker.startMonitoringVisits()`
     * - Extremely low battery impact
     * - Automatic with no configuration needed
     *
     * **Android:** Emulated visit detection via AndroidVisitDetector
     * - Analyzes GPS/NETWORK location sample patterns
     * - Uses DBSCAN clustering algorithm to detect stationary periods
     * - `AndroidVisitDetector.startMonitoring()`
     * - Provides semantic parity with iOS CLVisit behavior
     * - Creates VISIT-type LocationSamples for consistency
     *
     * **Benefits:**
     * - Very low battery impact (especially on iOS)
     * - Automatic place detection
     * - Works even when app is not actively tracking (iOS)
     * - Consistent visit-based features across platforms
     *
     * @see com.po4yka.trailglass.location.tracking.IOSLocationTracker.startMonitoringVisits
     * @see com.po4yka.trailglass.location.tracking.AndroidVisitDetector
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
