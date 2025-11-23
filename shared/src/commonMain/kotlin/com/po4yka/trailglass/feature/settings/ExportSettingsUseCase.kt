package com.po4yka.trailglass.feature.settings

import com.po4yka.trailglass.data.repository.SettingsRepository
import com.po4yka.trailglass.domain.error.Result
import com.po4yka.trailglass.logging.logger
import me.tatarka.inject.annotations.Inject

/** Use case for exporting and importing settings. */
@Inject
class ExportSettingsUseCase(
    private val settingsRepository: SettingsRepository
) {
    private val logger = logger()

    /** Export settings as JSON string. */
    suspend fun exportSettings(): String {
        logger.info { "Exporting settings" }
        return settingsRepository.exportSettings()
    }

    /** Import settings from JSON string. */
    suspend fun importSettings(json: String): Result<Unit> {
        logger.info { "Importing settings" }
        return settingsRepository.importSettings(json)
    }
}
