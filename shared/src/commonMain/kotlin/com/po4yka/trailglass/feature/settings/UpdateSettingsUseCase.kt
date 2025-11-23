package com.po4yka.trailglass.feature.settings

import com.po4yka.trailglass.data.repository.SettingsRepository
import com.po4yka.trailglass.domain.model.AppSettings
import com.po4yka.trailglass.logging.logger
import me.tatarka.inject.annotations.Inject

/** Use case for updating app settings. */
@Inject
class UpdateSettingsUseCase(
    private val settingsRepository: SettingsRepository
) {
    private val logger = logger()

    /** Update app settings. */
    suspend fun execute(settings: AppSettings) {
        logger.info { "Updating settings" }
        settingsRepository.updateSettings(settings)
    }

    /** Reset settings to defaults. */
    suspend fun resetToDefaults() {
        logger.info { "Resetting settings to defaults" }
        settingsRepository.resetToDefaults()
    }
}
