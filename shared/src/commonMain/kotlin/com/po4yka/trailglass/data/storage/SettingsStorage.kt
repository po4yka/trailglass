package com.po4yka.trailglass.data.storage

import com.po4yka.trailglass.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

/** Platform-specific settings storage. */
interface SettingsStorage {
    /** Get settings as a Flow. */
    fun getSettingsFlow(): Flow<AppSettings>

    /** Get current settings value. */
    suspend fun getSettings(): AppSettings

    /** Save settings. */
    suspend fun saveSettings(settings: AppSettings)

    /** Clear all settings. */
    suspend fun clearSettings()
}
