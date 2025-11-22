package com.po4yka.trailglass.feature.permission

import com.po4yka.trailglass.domain.permission.*
import com.po4yka.trailglass.feature.common.Lifecycle
import com.po4yka.trailglass.logging.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

/**
 * Controller for managing permission request flows with user-friendly explanations.
 * Orchestrates the complete permission request experience:
 * 1. Check current permission state
 * 2. Show rationale if needed
 * 3. Request permission
 * 4. Handle denial (temporary or permanent)
 * 5. Navigate to settings if needed
 *
 * IMPORTANT: Call [cleanup] when this controller is no longer needed to prevent memory leaks.
 */
@Inject
class PermissionFlowController(
    private val permissionManager: PermissionManager,
    private val rationaleProvider: PermissionRationaleProvider,
    coroutineScope: CoroutineScope
) : Lifecycle {
    private val logger = logger()

    // Create a child scope that can be cancelled independently
    private val controllerScope =
        CoroutineScope(
            coroutineScope.coroutineContext + SupervisorJob()
        )

    /**
     * UI state for permission flow.
     */
    data class PermissionFlowState(
        val currentRequest: PermissionRequestState? = null,
        val isRequesting: Boolean = false,
        val showRationaleDialog: Boolean = false,
        val showDeniedDialog: Boolean = false,
        val showPermanentlyDeniedDialog: Boolean = false,
        val settingsInstructions: SettingsInstructions? = null,
        val lastResult: PermissionResult? = null,
        val error: String? = null
    )

    private val _state = MutableStateFlow(PermissionFlowState())
    val state: StateFlow<PermissionFlowState> = _state.asStateFlow()

    /**
     * Start a permission request flow.
     * This checks the current state and shows appropriate UI.
     */
    fun startPermissionFlow(permissionType: PermissionType) {
        logger.info { "Starting permission flow for $permissionType" }

        controllerScope.launch {
            try {
                val currentState = permissionManager.checkPermission(permissionType)
                val rationale = rationaleProvider.getRationale(permissionType)
                val shouldShowRationale = permissionManager.shouldShowRationale(permissionType)

                logger.debug { "Permission state: $currentState, shouldShowRationale: $shouldShowRationale" }

                when {
                    currentState.isGranted -> {
                        // Already granted, nothing to do
                        logger.info { "Permission already granted" }
                        _state.update {
                            it.copy(
                                lastResult = PermissionResult.Granted,
                                currentRequest = null
                            )
                        }
                    }

                    currentState.requiresSettings -> {
                        // Permanently denied, show settings dialog
                        logger.info { "Permission permanently denied, showing settings dialog" }
                        val instructions = rationaleProvider.getSettingsInstructions(permissionType)
                        _state.update {
                            it.copy(
                                currentRequest =
                                    PermissionRequestState(
                                        permissionType = permissionType,
                                        state = currentState,
                                        rationale = rationale,
                                        shouldShowRationale = false,
                                        canRequest = false
                                    ),
                                showPermanentlyDeniedDialog = true,
                                settingsInstructions = instructions
                            )
                        }
                    }

                    shouldShowRationale -> {
                        // Should show rationale before requesting
                        logger.info { "Showing permission rationale" }
                        _state.update {
                            it.copy(
                                currentRequest =
                                    PermissionRequestState(
                                        permissionType = permissionType,
                                        state = currentState,
                                        rationale = rationale,
                                        shouldShowRationale = true,
                                        canRequest = true
                                    ),
                                showRationaleDialog = true
                            )
                        }
                    }

                    else -> {
                        // Can request directly
                        logger.info { "Requesting permission directly" }
                        requestPermissionInternal(permissionType, rationale)
                    }
                }
            } catch (e: Exception) {
                logger.error(e) { "Error starting permission flow" }
                _state.update {
                    it.copy(
                        error = "Failed to check permission: ${e.message}",
                        isRequesting = false
                    )
                }
            }
        }
    }

    /**
     * User confirmed they want to grant the permission after seeing rationale.
     */
    fun onRationaleAccepted() {
        logger.debug { "User accepted rationale" }

        val currentRequest = _state.value.currentRequest
        if (currentRequest != null) {
            _state.update {
                it.copy(showRationaleDialog = false)
            }
            requestPermissionInternal(currentRequest.permissionType, currentRequest.rationale)
        } else {
            logger.warn { "No current request when rationale accepted" }
        }
    }

    /**
     * User declined to grant permission after seeing rationale.
     */
    fun onRationaleDenied() {
        logger.debug { "User declined rationale" }

        _state.update {
            it.copy(
                showRationaleDialog = false,
                lastResult = PermissionResult.Cancelled,
                currentRequest = null
            )
        }
    }

    /**
     * User chose to open settings.
     */
    fun onOpenSettingsClicked() {
        logger.info { "User chose to open settings" }

        controllerScope.launch {
            try {
                permissionManager.openAppSettings()
                _state.update {
                    it.copy(
                        showPermanentlyDeniedDialog = false,
                        currentRequest = null
                    )
                }
            } catch (e: Exception) {
                logger.error(e) { "Failed to open settings" }
                _state.update {
                    it.copy(error = "Failed to open settings: ${e.message}")
                }
            }
        }
    }

    /**
     * User chose to continue without the permission.
     */
    fun onContinueWithoutPermission() {
        logger.info { "User chose to continue without permission" }

        _state.update {
            it.copy(
                showRationaleDialog = false,
                showDeniedDialog = false,
                showPermanentlyDeniedDialog = false,
                lastResult = PermissionResult.Denied,
                currentRequest = null
            )
        }
    }

    /**
     * Retry permission request after denial.
     */
    fun onRetryPermission() {
        logger.info { "User chose to retry permission" }

        val currentRequest = _state.value.currentRequest
        if (currentRequest != null) {
            _state.update {
                it.copy(showDeniedDialog = false)
            }
            startPermissionFlow(currentRequest.permissionType)
        }
    }

    /**
     * Dismiss any dialog.
     */
    fun dismissDialogs() {
        _state.update {
            it.copy(
                showRationaleDialog = false,
                showDeniedDialog = false,
                showPermanentlyDeniedDialog = false
            )
        }
    }

    /**
     * Clear error state.
     */
    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    /**
     * Clear the last result.
     */
    fun clearResult() {
        _state.update { it.copy(lastResult = null) }
    }

    /**
     * Internal function to request permission.
     */
    private fun requestPermissionInternal(
        permissionType: PermissionType,
        rationale: PermissionRationale
    ) {
        _state.update {
            it.copy(
                isRequesting = true,
                currentRequest =
                    PermissionRequestState(
                        permissionType = permissionType,
                        state = PermissionState.NotDetermined,
                        rationale = rationale,
                        shouldShowRationale = false,
                        canRequest = true
                    )
            )
        }

        controllerScope.launch {
            try {
                val result = permissionManager.requestPermission(permissionType)
                logger.info { "Permission request result: $result" }

                when (result) {
                    is PermissionResult.Granted -> {
                        _state.update {
                            it.copy(
                                isRequesting = false,
                                lastResult = result,
                                currentRequest = null
                            )
                        }
                    }

                    is PermissionResult.Denied -> {
                        _state.update {
                            it.copy(
                                isRequesting = false,
                                showDeniedDialog = true,
                                lastResult = result
                            )
                        }
                    }

                    is PermissionResult.PermanentlyDenied -> {
                        val instructions = rationaleProvider.getSettingsInstructions(permissionType)
                        _state.update {
                            it.copy(
                                isRequesting = false,
                                showPermanentlyDeniedDialog = true,
                                settingsInstructions = instructions,
                                lastResult = result
                            )
                        }
                    }

                    is PermissionResult.Cancelled -> {
                        _state.update {
                            it.copy(
                                isRequesting = false,
                                lastResult = result,
                                currentRequest = null
                            )
                        }
                    }

                    is PermissionResult.Error -> {
                        _state.update {
                            it.copy(
                                isRequesting = false,
                                error = result.message,
                                lastResult = result
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                logger.error(e) { "Error requesting permission" }
                _state.update {
                    it.copy(
                        isRequesting = false,
                        error = "Failed to request permission: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Check if a specific permission is granted.
     */
    suspend fun isPermissionGranted(permissionType: PermissionType): Boolean =
        try {
            permissionManager.checkPermission(permissionType).isGranted
        } catch (e: Exception) {
            logger.error(e) { "Error checking permission" }
            false
        }

    /**
     * Get the current state of a permission without starting a flow.
     */
    suspend fun getPermissionState(permissionType: PermissionType): PermissionState =
        try {
            permissionManager.checkPermission(permissionType)
        } catch (e: Exception) {
            logger.error(e) { "Error getting permission state" }
            PermissionState.NotDetermined
        }

    /**
     * Cleanup method to release resources and prevent memory leaks.
     * MUST be called when this controller is no longer needed.
     *
     * Cancels all running coroutines including flow collectors.
     */
    override fun cleanup() {
        logger.info { "Cleaning up PermissionFlowController" }
        controllerScope.cancel()
        logger.debug { "PermissionFlowController cleanup complete" }
    }
}
