package com.po4yka.trailglass.data.repository

import com.po4yka.trailglass.data.storage.SettingsStorage
import com.po4yka.trailglass.domain.error.Result
import com.po4yka.trailglass.domain.model.AppSettings
import com.po4yka.trailglass.logging.logger
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Inject

/**
 * Implementation of SettingsRepository using platform-specific storage.
 */
@Inject
class SettingsRepositoryImpl(
    private val storage: SettingsStorage,
    private val json: Json
) : SettingsRepository {

    private val logger = logger()

    override fun getSettings(): Flow<AppSettings> {
        return storage.getSettingsFlow()
    }

    override suspend fun getCurrentSettings(): AppSettings {
        return storage.getSettings()
    }

    override suspend fun updateSettings(settings: AppSettings) {
        logger.info { "Updating settings" }
        storage.saveSettings(settings)
    }

    override suspend fun resetToDefaults() {
        logger.info { "Resetting settings to defaults" }
        storage.clearSettings()
        // Storage will return default settings after clear
    }

    override suspend fun exportSettings(): String {
        val settings = storage.getSettings()
        return json.encodeToString(settings)
    }

    override suspend fun importSettings(jsonString: String): Result<Unit> {
        return try {
            val settings = json.decodeFromString<AppSettings>(jsonString)
            storage.saveSettings(settings)
            Result.Success(Unit)
        } catch (e: Exception) {
            logger.error(e) { "Failed to import settings" }
            Result.Error(
                com.po4yka.trailglass.domain.error.TrailGlassError.ValidationError.InvalidInput(
                    fieldName = "settings",
                    technicalMessage = "Invalid settings format: ${e.message}"
                )
            )
        }
    }
}
