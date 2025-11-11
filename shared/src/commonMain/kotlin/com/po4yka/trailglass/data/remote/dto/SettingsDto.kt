package com.po4yka.trailglass.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Tracking preferences DTO.
 */
@Serializable
data class TrackingPreferencesDto(
    val accuracy: String,
    val updateInterval: String,
    val batteryOptimization: Boolean,
    val trackWhenStationary: Boolean,
    val minimumDistance: Int
)

/**
 * Privacy settings DTO.
 */
@Serializable
data class PrivacySettingsDto(
    val dataRetentionDays: Int,
    val allowAnonymousAnalytics: Boolean,
    val shareLocation: Boolean,
    val requireAuthentication: Boolean,
    val autoBackup: Boolean
)

/**
 * Unit preferences DTO.
 */
@Serializable
data class UnitPreferencesDto(
    val distanceUnit: String,
    val temperatureUnit: String,
    val timeFormat: String,
    val firstDayOfWeek: String
)

/**
 * Appearance settings DTO.
 */
@Serializable
data class AppearanceSettingsDto(
    val theme: String,
    val accentColor: String,
    val mapStyle: String,
    val enableAnimations: Boolean
)

/**
 * Settings DTO (complete settings object).
 */
@Serializable
data class SettingsDto(
    val trackingPreferences: TrackingPreferencesDto? = null,
    val privacySettings: PrivacySettingsDto? = null,
    val unitPreferences: UnitPreferencesDto? = null,
    val appearanceSettings: AppearanceSettingsDto? = null,
    val serverVersion: Long? = null,
    val lastModified: String? = null
)

/**
 * Update settings request (partial update supported).
 */
@Serializable
data class UpdateSettingsRequest(
    val trackingPreferences: TrackingPreferencesDto? = null,
    val privacySettings: PrivacySettingsDto? = null,
    val unitPreferences: UnitPreferencesDto? = null,
    val appearanceSettings: AppearanceSettingsDto? = null,
    val expectedVersion: Long
)

/**
 * Settings response (for updates).
 */
@Serializable
data class SettingsResponse(
    val serverVersion: Long,
    val syncTimestamp: String
)
