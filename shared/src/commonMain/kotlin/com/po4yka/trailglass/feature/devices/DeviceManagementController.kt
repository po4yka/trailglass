package com.po4yka.trailglass.feature.devices

import com.po4yka.trailglass.data.remote.TrailGlassApiClient
import com.po4yka.trailglass.data.remote.dto.DeviceDto
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
 * Controller for Device Management screen.
 * Manages list of user's devices and allows device removal.
 *
 * IMPORTANT: Call [cleanup] when this controller is no longer needed to prevent memory leaks.
 */
@Inject
class DeviceManagementController(
    private val apiClient: TrailGlassApiClient,
    coroutineScope: CoroutineScope
) : Lifecycle {

    private val logger = logger()

    // Create a child scope that can be cancelled independently
    private val controllerScope = CoroutineScope(
        coroutineScope.coroutineContext + SupervisorJob()
    )

    /**
     * Device Management UI state.
     */
    data class DeviceManagementState(
        val devices: List<DeviceDto> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val deletingDeviceId: String? = null
    )

    private val _state = MutableStateFlow(DeviceManagementState())
    val state: StateFlow<DeviceManagementState> = _state.asStateFlow()

    /**
     * Load all devices for the current user.
     */
    fun loadDevices() {
        logger.debug { "Loading user devices" }

        _state.update { it.copy(isLoading = true, error = null) }

        controllerScope.launch {
            when (val result = apiClient.getUserDevices()) {
                is Result.Success -> {
                    val devicesResponse = result.getOrThrow()
                    logger.info { "Loaded ${devicesResponse.devices.size} devices" }

                    _state.update {
                        it.copy(
                            devices = devicesResponse.devices,
                            isLoading = false
                        )
                    }
                }
                is Result.Failure -> {
                    val error = result.exceptionOrNull()?.message ?: "Failed to load devices"
                    logger.error { "Failed to load devices: $error" }
                    _state.update {
                        it.copy(
                            error = error,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    /**
     * Refresh devices list.
     */
    fun refresh() {
        logger.debug { "Refreshing devices" }
        loadDevices()
    }

    /**
     * Delete a device.
     */
    fun deleteDevice(deviceId: String, onSuccess: () -> Unit = {}) {
        logger.debug { "Deleting device: $deviceId" }

        _state.update { it.copy(deletingDeviceId = deviceId, error = null) }

        controllerScope.launch {
            when (val result = apiClient.deleteUserDevice(deviceId)) {
                is Result.Success -> {
                    logger.info { "Deleted device: $deviceId" }

                    // Remove device from local list
                    _state.update {
                        it.copy(
                            devices = it.devices.filter { device -> device.deviceId != deviceId },
                            deletingDeviceId = null
                        )
                    }

                    // Notify caller of successful deletion
                    onSuccess()
                }
                is Result.Failure -> {
                    val error = result.exceptionOrNull()?.message ?: "Failed to delete device"
                    logger.error { "Failed to delete device: $error" }
                    _state.update {
                        it.copy(
                            error = error,
                            deletingDeviceId = null
                        )
                    }
                }
            }
        }
    }

    /**
     * Clear error state.
     */
    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    /**
     * Cleanup method to release resources and prevent memory leaks.
     * MUST be called when this controller is no longer needed.
     *
     * Cancels all running coroutines including flow collectors.
     */
    override fun cleanup() {
        logger.info { "Cleaning up DeviceManagementController" }
        controllerScope.cancel()
        logger.debug { "DeviceManagementController cleanup complete" }
    }
}
