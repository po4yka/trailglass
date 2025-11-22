package com.po4yka.trailglass.data.storage

import com.po4yka.trailglass.domain.model.*
import com.po4yka.trailglass.logging.logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Instant
import platform.Foundation.NSUserDefaults

/**
 * iOS implementation of SettingsStorage using UserDefaults.
 */
actual class SettingsStorage {
    private val logger = logger()
    private val userDefaults = NSUserDefaults.standardUserDefaults

    // MutableStateFlow to provide reactive updates
    private val _settingsFlow = MutableStateFlow(loadSettings())

    // Preference keys
    private object Keys {
        // Tracking
        const val TRACKING_ACCURACY = "tracking_accuracy"
        const val TRACKING_UPDATE_INTERVAL = "tracking_update_interval"
        const val TRACKING_BATTERY_OPT = "tracking_battery_opt"
        const val TRACKING_WHEN_STATIONARY = "tracking_when_stationary"
        const val TRACKING_MIN_DISTANCE = "tracking_min_distance"

        // Privacy
        const val PRIVACY_RETENTION_DAYS = "privacy_retention_days"
        const val PRIVACY_SHARE_ANALYTICS = "privacy_share_analytics"
        const val PRIVACY_SHARE_CRASH = "privacy_share_crash"
        const val PRIVACY_AUTO_BACKUP = "privacy_auto_backup"
        const val PRIVACY_ENCRYPT_BACKUPS = "privacy_encrypt_backups"

        // Units
        const val UNIT_DISTANCE = "unit_distance"
        const val UNIT_TEMPERATURE = "unit_temperature"
        const val UNIT_TIME_FORMAT = "unit_time_format"

        // Appearance
        const val APPEARANCE_THEME = "appearance_theme"
        const val APPEARANCE_DEVICE_WALLPAPER = "appearance_device_wallpaper"
        const val APPEARANCE_MAP_IN_TIMELINE = "appearance_map_in_timeline"
        const val APPEARANCE_COMPACT_VIEW = "appearance_compact_view"

        // Account
        const val ACCOUNT_EMAIL = "account_email"
        const val ACCOUNT_AUTO_SYNC = "account_auto_sync"
        const val ACCOUNT_SYNC_WIFI_ONLY = "account_sync_wifi_only"
        const val ACCOUNT_LAST_SYNC = "account_last_sync"

        // Data Management
        const val DATA_LAST_EXPORT = "data_last_export"
        const val DATA_LAST_BACKUP = "data_last_backup"
        const val DATA_STORAGE_MB = "data_storage_mb"
    }

    actual fun getSettingsFlow(): Flow<AppSettings> = _settingsFlow.asStateFlow()

    actual suspend fun getSettings(): AppSettings = _settingsFlow.value

    actual suspend fun saveSettings(settings: AppSettings) {
        // Tracking
        userDefaults.setObject(settings.trackingPreferences.accuracy.name, Keys.TRACKING_ACCURACY)
        userDefaults.setObject(settings.trackingPreferences.updateInterval.name, Keys.TRACKING_UPDATE_INTERVAL)
        userDefaults.setBool(settings.trackingPreferences.batteryOptimization, Keys.TRACKING_BATTERY_OPT)
        userDefaults.setBool(settings.trackingPreferences.trackWhenStationary, Keys.TRACKING_WHEN_STATIONARY)
        userDefaults.setInteger(settings.trackingPreferences.minimumDistance.toLong(), Keys.TRACKING_MIN_DISTANCE)

        // Privacy
        userDefaults.setInteger(settings.privacySettings.dataRetentionDays.toLong(), Keys.PRIVACY_RETENTION_DAYS)
        userDefaults.setBool(settings.privacySettings.shareAnalytics, Keys.PRIVACY_SHARE_ANALYTICS)
        userDefaults.setBool(settings.privacySettings.shareCrashReports, Keys.PRIVACY_SHARE_CRASH)
        userDefaults.setBool(settings.privacySettings.autoBackup, Keys.PRIVACY_AUTO_BACKUP)
        userDefaults.setBool(settings.privacySettings.encryptBackups, Keys.PRIVACY_ENCRYPT_BACKUPS)

        // Units
        userDefaults.setObject(settings.unitPreferences.distanceUnit.name, Keys.UNIT_DISTANCE)
        userDefaults.setObject(settings.unitPreferences.temperatureUnit.name, Keys.UNIT_TEMPERATURE)
        userDefaults.setObject(settings.unitPreferences.timeFormat.name, Keys.UNIT_TIME_FORMAT)

        // Appearance
        userDefaults.setObject(settings.appearanceSettings.theme.name, Keys.APPEARANCE_THEME)
        userDefaults.setBool(settings.appearanceSettings.useDeviceWallpaper, Keys.APPEARANCE_DEVICE_WALLPAPER)
        userDefaults.setBool(settings.appearanceSettings.showMapInTimeline, Keys.APPEARANCE_MAP_IN_TIMELINE)
        userDefaults.setBool(settings.appearanceSettings.compactView, Keys.APPEARANCE_COMPACT_VIEW)

        // Account
        settings.accountSettings.email?.let { userDefaults.setObject(it, Keys.ACCOUNT_EMAIL) }
        userDefaults.setBool(settings.accountSettings.autoSync, Keys.ACCOUNT_AUTO_SYNC)
        userDefaults.setBool(settings.accountSettings.syncOnWifiOnly, Keys.ACCOUNT_SYNC_WIFI_ONLY)
        settings.accountSettings.lastSyncTime?.let {
            userDefaults.setDouble(it.epochSeconds.toDouble(), Keys.ACCOUNT_LAST_SYNC)
        }

        // Data Management
        settings.dataManagement.lastExportTime?.let {
            userDefaults.setDouble(it.epochSeconds.toDouble(), Keys.DATA_LAST_EXPORT)
        }
        settings.dataManagement.lastBackupTime?.let {
            userDefaults.setDouble(it.epochSeconds.toDouble(), Keys.DATA_LAST_BACKUP)
        }
        userDefaults.setDouble(settings.dataManagement.storageUsedMb, Keys.DATA_STORAGE_MB)

        userDefaults.synchronize()

        // Update flow
        _settingsFlow.value = settings
    }

    actual suspend fun clearSettings() {
        // Remove all settings keys
        listOf(
            Keys.TRACKING_ACCURACY,
            Keys.TRACKING_UPDATE_INTERVAL,
            Keys.TRACKING_BATTERY_OPT,
            Keys.TRACKING_WHEN_STATIONARY,
            Keys.TRACKING_MIN_DISTANCE,
            Keys.PRIVACY_RETENTION_DAYS,
            Keys.PRIVACY_SHARE_ANALYTICS,
            Keys.PRIVACY_SHARE_CRASH,
            Keys.PRIVACY_AUTO_BACKUP,
            Keys.PRIVACY_ENCRYPT_BACKUPS,
            Keys.UNIT_DISTANCE,
            Keys.UNIT_TEMPERATURE,
            Keys.UNIT_TIME_FORMAT,
            Keys.APPEARANCE_THEME,
            Keys.APPEARANCE_DEVICE_WALLPAPER,
            Keys.APPEARANCE_MAP_IN_TIMELINE,
            Keys.APPEARANCE_COMPACT_VIEW,
            Keys.ACCOUNT_EMAIL,
            Keys.ACCOUNT_AUTO_SYNC,
            Keys.ACCOUNT_SYNC_WIFI_ONLY,
            Keys.ACCOUNT_LAST_SYNC,
            Keys.DATA_LAST_EXPORT,
            Keys.DATA_LAST_BACKUP,
            Keys.DATA_STORAGE_MB
        ).forEach { key ->
            userDefaults.removeObjectForKey(key)
        }

        userDefaults.synchronize()

        // Reset to defaults
        _settingsFlow.value = AppSettings()
    }

    private fun loadSettings(): AppSettings =
        AppSettings(
            trackingPreferences =
                TrackingPreferences(
                    accuracy =
                        (userDefaults.stringForKey(Keys.TRACKING_ACCURACY) as? String)?.let {
                            TrackingAccuracy.valueOf(it)
                        } ?: TrackingAccuracy.BALANCED,
                    updateInterval =
                        (userDefaults.stringForKey(Keys.TRACKING_UPDATE_INTERVAL) as? String)?.let {
                            UpdateInterval.valueOf(it)
                        } ?: UpdateInterval.NORMAL,
                    batteryOptimization =
                        if (userDefaults.objectForKey(Keys.TRACKING_BATTERY_OPT) != null) {
                            userDefaults.boolForKey(Keys.TRACKING_BATTERY_OPT)
                        } else {
                            true
                        },
                    trackWhenStationary = userDefaults.boolForKey(Keys.TRACKING_WHEN_STATIONARY),
                    minimumDistance =
                        if (userDefaults.objectForKey(Keys.TRACKING_MIN_DISTANCE) != null) {
                            userDefaults.integerForKey(Keys.TRACKING_MIN_DISTANCE).toInt()
                        } else {
                            10
                        }
                ),
            privacySettings =
                PrivacySettings(
                    dataRetentionDays =
                        if (userDefaults.objectForKey(Keys.PRIVACY_RETENTION_DAYS) != null) {
                            userDefaults.integerForKey(Keys.PRIVACY_RETENTION_DAYS).toInt()
                        } else {
                            365
                        },
                    shareAnalytics = userDefaults.boolForKey(Keys.PRIVACY_SHARE_ANALYTICS),
                    shareCrashReports =
                        if (userDefaults.objectForKey(Keys.PRIVACY_SHARE_CRASH) != null) {
                            userDefaults.boolForKey(Keys.PRIVACY_SHARE_CRASH)
                        } else {
                            true
                        },
                    autoBackup =
                        if (userDefaults.objectForKey(Keys.PRIVACY_AUTO_BACKUP) != null) {
                            userDefaults.boolForKey(Keys.PRIVACY_AUTO_BACKUP)
                        } else {
                            true
                        },
                    encryptBackups =
                        if (userDefaults.objectForKey(Keys.PRIVACY_ENCRYPT_BACKUPS) != null) {
                            userDefaults.boolForKey(Keys.PRIVACY_ENCRYPT_BACKUPS)
                        } else {
                            true
                        }
                ),
            unitPreferences =
                UnitPreferences(
                    distanceUnit =
                        (userDefaults.stringForKey(Keys.UNIT_DISTANCE) as? String)?.let {
                            DistanceUnit.valueOf(it)
                        } ?: DistanceUnit.METRIC,
                    temperatureUnit =
                        (userDefaults.stringForKey(Keys.UNIT_TEMPERATURE) as? String)?.let {
                            TemperatureUnit.valueOf(it)
                        } ?: TemperatureUnit.CELSIUS,
                    timeFormat =
                        (userDefaults.stringForKey(Keys.UNIT_TIME_FORMAT) as? String)?.let {
                            TimeFormat.valueOf(it)
                        } ?: TimeFormat.TWENTY_FOUR_HOUR
                ),
            appearanceSettings =
                AppearanceSettings(
                    theme =
                        (userDefaults.stringForKey(Keys.APPEARANCE_THEME) as? String)?.let {
                            AppTheme.valueOf(it)
                        } ?: AppTheme.SYSTEM,
                    useDeviceWallpaper = userDefaults.boolForKey(Keys.APPEARANCE_DEVICE_WALLPAPER),
                    showMapInTimeline =
                        if (userDefaults.objectForKey(Keys.APPEARANCE_MAP_IN_TIMELINE) != null) {
                            userDefaults.boolForKey(Keys.APPEARANCE_MAP_IN_TIMELINE)
                        } else {
                            true
                        },
                    compactView = userDefaults.boolForKey(Keys.APPEARANCE_COMPACT_VIEW)
                ),
            accountSettings =
                AccountSettings(
                    email = userDefaults.stringForKey(Keys.ACCOUNT_EMAIL) as? String,
                    autoSync =
                        if (userDefaults.objectForKey(Keys.ACCOUNT_AUTO_SYNC) != null) {
                            userDefaults.boolForKey(Keys.ACCOUNT_AUTO_SYNC)
                        } else {
                            true
                        },
                    syncOnWifiOnly =
                        if (userDefaults.objectForKey(Keys.ACCOUNT_SYNC_WIFI_ONLY) != null) {
                            userDefaults.boolForKey(Keys.ACCOUNT_SYNC_WIFI_ONLY)
                        } else {
                            true
                        },
                    lastSyncTime =
                        if (userDefaults.objectForKey(Keys.ACCOUNT_LAST_SYNC) != null) {
                            Instant.fromEpochSeconds(userDefaults.doubleForKey(Keys.ACCOUNT_LAST_SYNC).toLong())
                        } else {
                            null
                        }
                ),
            dataManagement =
                DataManagement(
                    lastExportTime =
                        if (userDefaults.objectForKey(Keys.DATA_LAST_EXPORT) != null) {
                            Instant.fromEpochSeconds(userDefaults.doubleForKey(Keys.DATA_LAST_EXPORT).toLong())
                        } else {
                            null
                        },
                    lastBackupTime =
                        if (userDefaults.objectForKey(Keys.DATA_LAST_BACKUP) != null) {
                            Instant.fromEpochSeconds(userDefaults.doubleForKey(Keys.DATA_LAST_BACKUP).toLong())
                        } else {
                            null
                        },
                    storageUsedMb =
                        if (userDefaults.objectForKey(Keys.DATA_STORAGE_MB) != null) {
                            userDefaults.doubleForKey(Keys.DATA_STORAGE_MB)
                        } else {
                            0.0
                        }
                )
        )
}
