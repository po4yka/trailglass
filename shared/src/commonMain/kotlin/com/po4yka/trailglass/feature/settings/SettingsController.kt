package com.po4yka.trailglass.feature.settings

import com.po4yka.trailglass.data.security.SyncDataEncryption
import com.po4yka.trailglass.domain.model.AccountSettings
import com.po4yka.trailglass.domain.model.AlgorithmPreferences
import com.po4yka.trailglass.domain.model.AppearanceSettings
import com.po4yka.trailglass.domain.model.AppSettings
import com.po4yka.trailglass.domain.model.PrivacySettings
import com.po4yka.trailglass.domain.model.TrackingPreferences
import com.po4yka.trailglass.domain.model.UnitPreferences
import com.po4yka.trailglass.feature.common.Lifecycle
import com.po4yka.trailglass.logging.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

/**
 * Controller for managing app settings.
 *
 * IMPORTANT: Call [cleanup] when this controller is no longer needed to prevent memory leaks.
 */
@Inject
class SettingsController(
    private val getSettingsUseCase: GetSettingsUseCase,
    private val updateSettingsUseCase: UpdateSettingsUseCase,
    private val exportSettingsUseCase: ExportSettingsUseCase,
    private val clearDataUseCase: ClearDataUseCase,
    private val syncDataEncryption: SyncDataEncryption,
    coroutineScope: CoroutineScope
) : Lifecycle {
    private val logger = logger()

    // Create a child scope that can be cancelled independently
    private val controllerScope = CoroutineScope(
        coroutineScope.coroutineContext + SupervisorJob()
    )

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        controllerScope.launch {
            getSettingsUseCase.execute().collect { settings ->
                _state.value = _state.value.copy(
                    settings = settings,
                    isLoading = false
                )
            }
        }
    }

    fun updateTrackingPreferences(preferences: TrackingPreferences) {
        controllerScope.launch {
            val currentSettings = _state.value.settings ?: return@launch
            val updated = currentSettings.copy(trackingPreferences = preferences)
            updateSettingsUseCase.execute(updated)
        }
    }

    fun updatePrivacySettings(privacy: PrivacySettings) {
        controllerScope.launch {
            val currentSettings = _state.value.settings ?: return@launch

            // If E2E encryption is being enabled, initialize it
            if (privacy.enableE2EEncryption && !currentSettings.privacySettings.enableE2EEncryption) {
                logger.info { "Initializing E2E encryption" }
                syncDataEncryption.initializeEncryption()
                    .onSuccess {
                        logger.info { "E2E encryption initialized successfully" }
                    }
                    .onFailure { e ->
                        logger.error(e) { "Failed to initialize E2E encryption" }
                        _state.value = _state.value.copy(
                            error = "Failed to initialize encryption: ${e.message}"
                        )
                        return@launch
                    }
            }

            val updated = currentSettings.copy(privacySettings = privacy)
            updateSettingsUseCase.execute(updated)
        }
    }

    fun updateUnitPreferences(units: UnitPreferences) {
        controllerScope.launch {
            val currentSettings = _state.value.settings ?: return@launch
            val updated = currentSettings.copy(unitPreferences = units)
            updateSettingsUseCase.execute(updated)
        }
    }

    fun updateAppearanceSettings(appearance: AppearanceSettings) {
        controllerScope.launch {
            val currentSettings = _state.value.settings ?: return@launch
            val updated = currentSettings.copy(appearanceSettings = appearance)
            updateSettingsUseCase.execute(updated)
        }
    }

    fun updateAccountSettings(account: AccountSettings) {
        controllerScope.launch {
            val currentSettings = _state.value.settings ?: return@launch
            val updated = currentSettings.copy(accountSettings = account)
            updateSettingsUseCase.execute(updated)
        }
    }

    fun updateAlgorithmPreferences(preferences: AlgorithmPreferences) {
        controllerScope.launch {
            val currentSettings = _state.value.settings ?: return@launch
            val updated = currentSettings.copy(algorithmPreferences = preferences)
            updateSettingsUseCase.execute(updated)
        }
    }

    fun resetToDefaults() {
        controllerScope.launch {
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
        controllerScope.launch {
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
        controllerScope.launch {
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

    fun clearAllData() {
        controllerScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)
                clearDataUseCase.execute()
                _state.value = _state.value.copy(isLoading = false)
            } catch (e: Exception) {
                logger.error(e) { "Failed to clear all data" }
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Failed to clear all data: ${e.message}"
                )
            }
        }
    }

    /**
     * Cleanup method to release resources and prevent memory leaks.
     * MUST be called when this controller is no longer needed.
     *
     * Cancels all running coroutines including flow collectors.
     */
    override fun cleanup() {
        logger.info { "Cleaning up SettingsController" }
        controllerScope.cancel()
        logger.debug { "SettingsController cleanup complete" }
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
