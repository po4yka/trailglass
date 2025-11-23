package com.po4yka.trailglass.feature.settings

import com.po4yka.trailglass.data.repository.SettingsRepository
import com.po4yka.trailglass.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow
import me.tatarka.inject.annotations.Inject

/** Use case for getting app settings. */
@Inject
class GetSettingsUseCase(
    private val settingsRepository: SettingsRepository
) {
    /** Get settings as a Flow. */
    fun execute(): Flow<AppSettings> = settingsRepository.getSettings()

    /** Get current settings value. */
    suspend fun getCurrentSettings(): AppSettings = settingsRepository.getCurrentSettings()
}
