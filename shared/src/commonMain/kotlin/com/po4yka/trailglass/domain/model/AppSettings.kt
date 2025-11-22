package com.po4yka.trailglass.domain.model

import com.po4yka.trailglass.domain.algorithm.BearingAlgorithmType
import com.po4yka.trailglass.domain.algorithm.DistanceAlgorithmType
import com.po4yka.trailglass.domain.algorithm.InterpolationAlgorithmType
import kotlinx.serialization.Serializable

/**
 * Application settings and user preferences.
 */
@Serializable
data class AppSettings(
    // Tracking preferences
    val trackingPreferences: TrackingPreferences = TrackingPreferences(),
    // Privacy settings
    val privacySettings: PrivacySettings = PrivacySettings(),
    // Unit preferences
    val unitPreferences: UnitPreferences = UnitPreferences(),
    // Appearance settings
    val appearanceSettings: AppearanceSettings = AppearanceSettings(),
    // Account settings
    val accountSettings: AccountSettings = AccountSettings(),
    // Data management
    val dataManagement: DataManagement = DataManagement(),
    // Algorithm preferences
    val algorithmPreferences: AlgorithmPreferences = AlgorithmPreferences()
)

/**
 * Tracking preferences for location accuracy and frequency.
 */
@Serializable
data class TrackingPreferences(
    val accuracy: TrackingAccuracy = TrackingAccuracy.BALANCED,
    val updateInterval: UpdateInterval = UpdateInterval.NORMAL,
    val batteryOptimization: Boolean = true,
    val trackWhenStationary: Boolean = false,
    val minimumDistance: Int = 10 // meters
)

@Serializable
enum class TrackingAccuracy {
    HIGH, // GPS only, most accurate
    BALANCED, // GPS + Network, good balance
    LOW // Network only, battery saver
}

@Serializable
enum class UpdateInterval {
    FREQUENT, // Every 30 seconds
    NORMAL, // Every 2 minutes
    BATTERY_SAVER // Every 10 minutes
}

/**
 * Privacy settings for data retention and sharing.
 */
@Serializable
data class PrivacySettings(
    val dataRetentionDays: Int = 365, // 0 = forever
    val shareAnalytics: Boolean = false,
    val shareCrashReports: Boolean = true,
    val autoBackup: Boolean = true,
    val encryptBackups: Boolean = true,
    val enableE2EEncryption: Boolean = false // End-to-end encryption for synced data
)

/**
 * Unit preferences for displaying measurements.
 */
@Serializable
data class UnitPreferences(
    val distanceUnit: DistanceUnit = DistanceUnit.METRIC,
    val temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    val timeFormat: TimeFormat = TimeFormat.TWENTY_FOUR_HOUR
)

@Serializable
enum class DistanceUnit {
    METRIC, // km, m
    IMPERIAL // mi, ft
}

@Serializable
enum class TemperatureUnit {
    CELSIUS,
    FAHRENHEIT
}

@Serializable
enum class TimeFormat {
    TWELVE_HOUR,
    TWENTY_FOUR_HOUR
}

/**
 * Appearance settings for theme and display.
 */
@Serializable
data class AppearanceSettings(
    val theme: AppTheme = AppTheme.SYSTEM,
    val useDeviceWallpaper: Boolean = false,
    val showMapInTimeline: Boolean = true,
    val compactView: Boolean = false
)

@Serializable
enum class AppTheme {
    LIGHT,
    DARK,
    SYSTEM
}

/**
 * Account settings and sync preferences.
 */
@Serializable
data class AccountSettings(
    val email: String? = null,
    val autoSync: Boolean = true,
    val syncOnWifiOnly: Boolean = true,
    val lastSyncTime: kotlinx.datetime.Instant? = null
)

/**
 * Data management settings for export and import.
 */
@Serializable
data class DataManagement(
    val lastExportTime: kotlinx.datetime.Instant? = null,
    val lastBackupTime: kotlinx.datetime.Instant? = null,
    val storageUsedMb: Double = 0.0
)

/**
 * Algorithm preferences for geographic calculations.
 * Allows users to choose different calculation methods based on their needs.
 */
@Serializable
data class AlgorithmPreferences(
    /**
     * Distance calculation algorithm.
     * - HAVERSINE: Fast and accurate (~0.5% error), assumes spherical Earth
     * - VINCENTY: Most accurate (~0.5mm error), accounts for ellipsoidal Earth, slower
     * - SIMPLE: Fastest but only accurate for short distances (<1km)
     */
    val distanceAlgorithm: DistanceAlgorithmType = DistanceAlgorithmType.HAVERSINE,
    /**
     * Bearing calculation algorithm.
     * - INITIAL: Direction at start point (most common for navigation)
     * - FINAL: Direction at end point (useful for arrival headings)
     * - RHUMB_LINE: Constant compass bearing (loxodrome, not shortest path)
     */
    val bearingAlgorithm: BearingAlgorithmType = BearingAlgorithmType.INITIAL,
    /**
     * Interpolation algorithm for paths and animations.
     * - LINEAR: Simple straight line, fast but not geographically accurate
     * - SLERP: Spherical interpolation, follows great circle (most accurate)
     * - CUBIC: Smooth curved path with easing (aesthetically pleasing)
     */
    val interpolationAlgorithm: InterpolationAlgorithmType = InterpolationAlgorithmType.SLERP
)
