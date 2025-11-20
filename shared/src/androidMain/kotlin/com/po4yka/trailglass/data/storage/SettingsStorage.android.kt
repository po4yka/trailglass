package com.po4yka.trailglass.data.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.po4yka.trailglass.domain.model.*
import com.po4yka.trailglass.logging.logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * Android implementation of SettingsStorage using DataStore.
 */
actual class SettingsStorage(private val context: Context) {

    private val logger = logger()

    // Preference keys
    private object Keys {
        // Tracking
        val TRACKING_ACCURACY = stringPreferencesKey("tracking_accuracy")
        val TRACKING_UPDATE_INTERVAL = stringPreferencesKey("tracking_update_interval")
        val TRACKING_BATTERY_OPT = booleanPreferencesKey("tracking_battery_opt")
        val TRACKING_WHEN_STATIONARY = booleanPreferencesKey("tracking_when_stationary")
        val TRACKING_MIN_DISTANCE = intPreferencesKey("tracking_min_distance")

        // Privacy
        val PRIVACY_RETENTION_DAYS = intPreferencesKey("privacy_retention_days")
        val PRIVACY_SHARE_ANALYTICS = booleanPreferencesKey("privacy_share_analytics")
        val PRIVACY_SHARE_CRASH = booleanPreferencesKey("privacy_share_crash")
        val PRIVACY_AUTO_BACKUP = booleanPreferencesKey("privacy_auto_backup")
        val PRIVACY_ENCRYPT_BACKUPS = booleanPreferencesKey("privacy_encrypt_backups")

        // Units
        val UNIT_DISTANCE = stringPreferencesKey("unit_distance")
        val UNIT_TEMPERATURE = stringPreferencesKey("unit_temperature")
        val UNIT_TIME_FORMAT = stringPreferencesKey("unit_time_format")

        // Appearance
        val APPEARANCE_THEME = stringPreferencesKey("appearance_theme")
        val APPEARANCE_DEVICE_WALLPAPER = booleanPreferencesKey("appearance_device_wallpaper")
        val APPEARANCE_MAP_IN_TIMELINE = booleanPreferencesKey("appearance_map_in_timeline")
        val APPEARANCE_COMPACT_VIEW = booleanPreferencesKey("appearance_compact_view")

        // Account
        val ACCOUNT_EMAIL = stringPreferencesKey("account_email")
        val ACCOUNT_AUTO_SYNC = booleanPreferencesKey("account_auto_sync")
        val ACCOUNT_SYNC_WIFI_ONLY = booleanPreferencesKey("account_sync_wifi_only")
        val ACCOUNT_LAST_SYNC = longPreferencesKey("account_last_sync")

        // Data Management
        val DATA_LAST_EXPORT = longPreferencesKey("data_last_export")
        val DATA_LAST_BACKUP = longPreferencesKey("data_last_backup")
        val DATA_STORAGE_MB = doublePreferencesKey("data_storage_mb")
    }

    actual fun getSettingsFlow(): Flow<AppSettings> {
        return context.dataStore.data
            .catch { exception ->
                logger.error(exception) { "Error reading settings" }
                emit(emptyPreferences())
            }
            .map { preferences ->
                preferences.toAppSettings()
            }
    }

    actual suspend fun getSettings(): AppSettings {
        return context.dataStore.data
            .catch { exception ->
                logger.error(exception) { "Error reading settings" }
                emit(emptyPreferences())
            }
            .map { it.toAppSettings() }
            .first()
    }

    actual suspend fun saveSettings(settings: AppSettings) {
        context.dataStore.edit { preferences ->
            // Tracking
            preferences[Keys.TRACKING_ACCURACY] = settings.trackingPreferences.accuracy.name
            preferences[Keys.TRACKING_UPDATE_INTERVAL] = settings.trackingPreferences.updateInterval.name
            preferences[Keys.TRACKING_BATTERY_OPT] = settings.trackingPreferences.batteryOptimization
            preferences[Keys.TRACKING_WHEN_STATIONARY] = settings.trackingPreferences.trackWhenStationary
            preferences[Keys.TRACKING_MIN_DISTANCE] = settings.trackingPreferences.minimumDistance

            // Privacy
            preferences[Keys.PRIVACY_RETENTION_DAYS] = settings.privacySettings.dataRetentionDays
            preferences[Keys.PRIVACY_SHARE_ANALYTICS] = settings.privacySettings.shareAnalytics
            preferences[Keys.PRIVACY_SHARE_CRASH] = settings.privacySettings.shareCrashReports
            preferences[Keys.PRIVACY_AUTO_BACKUP] = settings.privacySettings.autoBackup
            preferences[Keys.PRIVACY_ENCRYPT_BACKUPS] = settings.privacySettings.encryptBackups

            // Units
            preferences[Keys.UNIT_DISTANCE] = settings.unitPreferences.distanceUnit.name
            preferences[Keys.UNIT_TEMPERATURE] = settings.unitPreferences.temperatureUnit.name
            preferences[Keys.UNIT_TIME_FORMAT] = settings.unitPreferences.timeFormat.name

            // Appearance
            preferences[Keys.APPEARANCE_THEME] = settings.appearanceSettings.theme.name
            preferences[Keys.APPEARANCE_DEVICE_WALLPAPER] = settings.appearanceSettings.useDeviceWallpaper
            preferences[Keys.APPEARANCE_MAP_IN_TIMELINE] = settings.appearanceSettings.showMapInTimeline
            preferences[Keys.APPEARANCE_COMPACT_VIEW] = settings.appearanceSettings.compactView

            // Account
            settings.accountSettings.email?.let { preferences[Keys.ACCOUNT_EMAIL] = it }
            preferences[Keys.ACCOUNT_AUTO_SYNC] = settings.accountSettings.autoSync
            preferences[Keys.ACCOUNT_SYNC_WIFI_ONLY] = settings.accountSettings.syncOnWifiOnly
            settings.accountSettings.lastSyncTime?.let {
                preferences[Keys.ACCOUNT_LAST_SYNC] = it.epochSeconds
            }

            // Data Management
            settings.dataManagement.lastExportTime?.let {
                preferences[Keys.DATA_LAST_EXPORT] = it.epochSeconds
            }
            settings.dataManagement.lastBackupTime?.let {
                preferences[Keys.DATA_LAST_BACKUP] = it.epochSeconds
            }
            preferences[Keys.DATA_STORAGE_MB] = settings.dataManagement.storageUsedMb
        }
    }

    actual suspend fun clearSettings() {
        context.dataStore.edit { it.clear() }
    }

    private fun Preferences.toAppSettings(): AppSettings {
        return AppSettings(
            trackingPreferences = TrackingPreferences(
                accuracy = this[Keys.TRACKING_ACCURACY]?.let {
                    TrackingAccuracy.valueOf(it)
                } ?: TrackingAccuracy.BALANCED,
                updateInterval = this[Keys.TRACKING_UPDATE_INTERVAL]?.let {
                    UpdateInterval.valueOf(it)
                } ?: UpdateInterval.NORMAL,
                batteryOptimization = this[Keys.TRACKING_BATTERY_OPT] ?: true,
                trackWhenStationary = this[Keys.TRACKING_WHEN_STATIONARY] ?: false,
                minimumDistance = this[Keys.TRACKING_MIN_DISTANCE] ?: 10
            ),
            privacySettings = PrivacySettings(
                dataRetentionDays = this[Keys.PRIVACY_RETENTION_DAYS] ?: 365,
                shareAnalytics = this[Keys.PRIVACY_SHARE_ANALYTICS] ?: false,
                shareCrashReports = this[Keys.PRIVACY_SHARE_CRASH] ?: true,
                autoBackup = this[Keys.PRIVACY_AUTO_BACKUP] ?: true,
                encryptBackups = this[Keys.PRIVACY_ENCRYPT_BACKUPS] ?: true
            ),
            unitPreferences = UnitPreferences(
                distanceUnit = this[Keys.UNIT_DISTANCE]?.let {
                    DistanceUnit.valueOf(it)
                } ?: DistanceUnit.METRIC,
                temperatureUnit = this[Keys.UNIT_TEMPERATURE]?.let {
                    TemperatureUnit.valueOf(it)
                } ?: TemperatureUnit.CELSIUS,
                timeFormat = this[Keys.UNIT_TIME_FORMAT]?.let {
                    TimeFormat.valueOf(it)
                } ?: TimeFormat.TWENTY_FOUR_HOUR
            ),
            appearanceSettings = AppearanceSettings(
                theme = this[Keys.APPEARANCE_THEME]?.let {
                    AppTheme.valueOf(it)
                } ?: AppTheme.SYSTEM,
                useDeviceWallpaper = this[Keys.APPEARANCE_DEVICE_WALLPAPER] ?: false,
                showMapInTimeline = this[Keys.APPEARANCE_MAP_IN_TIMELINE] ?: true,
                compactView = this[Keys.APPEARANCE_COMPACT_VIEW] ?: false
            ),
            accountSettings = AccountSettings(
                email = this[Keys.ACCOUNT_EMAIL],
                autoSync = this[Keys.ACCOUNT_AUTO_SYNC] ?: true,
                syncOnWifiOnly = this[Keys.ACCOUNT_SYNC_WIFI_ONLY] ?: true,
                lastSyncTime = this[Keys.ACCOUNT_LAST_SYNC]?.let {
                    Instant.fromEpochSeconds(it)
                }
            ),
            dataManagement = DataManagement(
                lastExportTime = this[Keys.DATA_LAST_EXPORT]?.let {
                    Instant.fromEpochSeconds(it)
                },
                lastBackupTime = this[Keys.DATA_LAST_BACKUP]?.let {
                    Instant.fromEpochSeconds(it)
                },
                storageUsedMb = this[Keys.DATA_STORAGE_MB] ?: 0.0
            )
        )
    }
}

// Extension to get first value from Flow
private suspend fun <T> Flow<T>.first(): T {
    var result: T? = null
    collect {
        result = it
        return@collect
    }
    return result!!
}
