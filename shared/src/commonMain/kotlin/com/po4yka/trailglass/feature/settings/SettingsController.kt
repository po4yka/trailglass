package com.po4yka.trailglass.feature.settings

import com.po4yka.trailglass.domain.model.*
import com.po4yka.trailglass.logging.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

/**
 * Controller for managing app settings.
 */
@Inject
class SettingsController(
    private val getSettingsUseCase: GetSettingsUseCase,
    private val updateSettingsUseCase: UpdateSettingsUseCase,
    private val exportSettingsUseCase: ExportSettingsUseCase,
    private val scope: CoroutineScope
) {
    private val logger = logger()

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        scope.launch {
            getSettingsUseCase.execute().collect { settings ->
                _state.value = _state.value.copy(
                    settings = settings,
                    isLoading = false
                )
            }
        }
    }

    fun updateTrackingPreferences(preferences: TrackingPreferences) {
        scope.launch {
            val currentSettings = _state.value.settings ?: return@launch
            val updated = currentSettings.copy(trackingPreferences = preferences)
            updateSettingsUseCase.execute(updated)
        }
    }

    fun updatePrivacySettings(privacy: PrivacySettings) {
        scope.launch {
            val currentSettings = _state.value.settings ?: return@launch
            val updated = currentSettings.copy(privacySettings = privacy)
            updateSettingsUseCase.execute(updated)
        }
    }

    fun updateUnitPreferences(units: UnitPreferences) {
        scope.launch {
            val currentSettings = _state.value.settings ?: return@launch
            val updated = currentSettings.copy(unitPreferences = units)
            updateSettingsUseCase.execute(updated)
        }
    }

    fun updateAppearanceSettings(appearance: AppearanceSettings) {
        scope.launch {
            val currentSettings = _state.value.settings ?: return@launch
            val updated = currentSettings.copy(appearanceSettings = appearance)
            updateSettingsUseCase.execute(updated)
        }
    }

    fun updateAccountSettings(account: AccountSettings) {
        scope.launch {
            val currentSettings = _state.value.settings ?: return@launch
            val updated = currentSettings.copy(accountSettings = account)
            updateSettingsUseCase.execute(updated)
        }
    }

    fun resetToDefaults() {
        scope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)
                updateSettingsUseCase.resetToDefaults()
                _state.value = _state.value.copy(isLoading = false)
            } catch (e: Exception) {
                logger.error(e) { "Failed to reset settings" }
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Failed to reset settings: ${e.message}"
                )
            }
        }
    }

    fun exportSettings(): String? {
        var result: String? = null
        scope.launch {
            try {
                result = exportSettingsUseCase.exportSettings()
            } catch (e: Exception) {
                logger.error(e) { "Failed to export settings" }
                _state.value = _state.value.copy(error = "Failed to export settings: ${e.message}")
            }
        }
        return result
    }

    fun importSettings(json: String) {
        scope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)
                when (val result = exportSettingsUseCase.importSettings(json)) {
                    is com.po4yka.trailglass.domain.error.Result.Success -> {
                        _state.value = _state.value.copy(isLoading = false)
                    }
                    is com.po4yka.trailglass.domain.error.Result.Error -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = result.error.userMessage
                        )
                    }
                }
            } catch (e: Exception) {
                logger.error(e) { "Failed to import settings" }
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Failed to import settings: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}

/**
 * State for settings screen.
 */
data class SettingsState(
    val settings: AppSettings? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)
