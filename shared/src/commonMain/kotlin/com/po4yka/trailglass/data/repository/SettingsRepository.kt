package com.po4yka.trailglass.data.repository

import com.po4yka.trailglass.domain.error.Result
import com.po4yka.trailglass.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

/** Repository for managing app settings and user preferences. */
interface SettingsRepository {
    /** Get current app settings as a Flow. */
    fun getSettings(): Flow<AppSettings>

    /** Get current app settings value (non-reactive). */
    suspend fun getCurrentSettings(): AppSettings

    /** Update app settings. */
    suspend fun updateSettings(settings: AppSettings)

    /** Reset all settings to defaults. */
    suspend fun resetToDefaults()

    /** Export settings as JSON string. */
    suspend fun exportSettings(): String

    /** Import settings from JSON string. */
    suspend fun importSettings(json: String): Result<Unit>
}
