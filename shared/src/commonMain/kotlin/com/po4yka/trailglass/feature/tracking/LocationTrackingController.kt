package com.po4yka.trailglass.feature.tracking

import com.po4yka.trailglass.domain.permission.PermissionResult
import com.po4yka.trailglass.domain.permission.PermissionType
import com.po4yka.trailglass.feature.common.Lifecycle
import com.po4yka.trailglass.feature.permission.PermissionFlowController
import com.po4yka.trailglass.location.tracking.LocationTracker
import com.po4yka.trailglass.location.tracking.TrackingMode
import com.po4yka.trailglass.location.tracking.TrackingState
import com.po4yka.trailglass.logging.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

/**
 * Controller for location tracking feature.
 * Manages tracking state, user actions, and permission requests.
 *
 * IMPORTANT: Call [cleanup] when this controller is no longer needed to prevent memory leaks.
 */
@Inject
class LocationTrackingController(
    private val locationTracker: LocationTracker,
    private val startTrackingUseCase: StartTrackingUseCase,
    private val stopTrackingUseCase: StopTrackingUseCase,
    private val permissionFlow: PermissionFlowController,
    coroutineScope: CoroutineScope
) : Lifecycle {
    private val logger = logger()

    // Create a child scope that can be cancelled independently
    private val controllerScope =
        CoroutineScope(
            coroutineScope.coroutineContext + SupervisorJob()
        )

    /**
     * Location tracking UI state.
     */
    data class LocationTrackingUIState(
        val trackingState: TrackingState = TrackingState(),
        val hasPermissions: Boolean = false,
        val isProcessing: Boolean = false,
        val error: String? = null,
        val pendingTrackingMode: TrackingMode? = null
    )

    private val _uiState = MutableStateFlow(LocationTrackingUIState())
    val uiState: StateFlow<LocationTrackingUIState> = _uiState.asStateFlow()

    init {
        // Observe tracking state from tracker
        controllerScope.launch {
            locationTracker.trackingState.collect { trackingState ->
                _uiState.update { it.copy(trackingState = trackingState) }
            }
        }

        // Observe permission results
        controllerScope.launch {
            permissionFlow.state.collect { permState ->
                when (permState.lastResult) {
                    is PermissionResult.Granted -> {
                        logger.info { "Permission granted, starting tracking" }
                        // Permission granted, start tracking with pending mode
                        val pendingMode = _uiState.value.pendingTrackingMode
                        if (pendingMode != null) {
                            _uiState.update { it.copy(hasPermissions = true, pendingTrackingMode = null) }
                            startTrackingInternal(pendingMode)
                        } else {
                            _uiState.update { it.copy(hasPermissions = true) }
                        }
                    }
                    is PermissionResult.Denied,
                    is PermissionResult.PermanentlyDenied -> {
                        logger.warn { "Permission denied" }
                        _uiState.update {
                            it.copy(
                                hasPermissions = false,
                                isProcessing = false,
                                pendingTrackingMode = null,
                                error = "Location permission is required to track your trips"
                            )
                        }
                    }
                    is PermissionResult.Cancelled -> {
                        logger.info { "Permission request cancelled" }
                        _uiState.update {
                            it.copy(
                                isProcessing = false,
                                pendingTrackingMode = null
                            )
                        }
                    }
                    is PermissionResult.Error -> {
                        logger.error { "Permission error: ${permState.lastResult.message}" }
                        _uiState.update {
                            it.copy(
                                isProcessing = false,
                                pendingTrackingMode = null,
                                error = permState.lastResult.message
                            )
                        }
                    }
                    null -> {
                        // No result yet
                    }
                }
            }
        }

        // Check permissions on init
        checkPermissions()
    }

    /**
     * Start tracking with the specified mode.
     * This will request permissions if needed before starting.
     */
    fun startTracking(mode: TrackingMode) {
        logger.info { "User requested to start tracking: $mode" }

        controllerScope.launch {
            // Check if we have permissions
            val hasPermissions = permissionFlow.isPermissionGranted(PermissionType.LOCATION_FINE)

            if (hasPermissions) {
                // Already have permissions, start tracking directly
                startTrackingInternal(mode)
            } else {
                // Need to request permissions
                logger.info { "Location permission not granted, requesting..." }
                _uiState.update {
                    it.copy(
                        isProcessing = true,
                        error = null,
                        pendingTrackingMode = mode
                    )
                }

                // Start permission flow
                permissionFlow.startPermissionFlow(PermissionType.LOCATION_FINE)
            }
        }
    }

    /**
     * Start tracking for background mode.
     * This requires both foreground and background location permissions.
     */
    fun startBackgroundTracking(mode: TrackingMode) {
        logger.info { "User requested to start background tracking: $mode" }

        controllerScope.launch {
            // Check if we have foreground permissions first
            val hasFinePermission = permissionFlow.isPermissionGranted(PermissionType.LOCATION_FINE)

            if (!hasFinePermission) {
                // Need foreground permission first
                logger.info { "Foreground location permission not granted, requesting..." }
                _uiState.update {
                    it.copy(
                        isProcessing = true,
                        error = null,
                        pendingTrackingMode = mode
                    )
                }
                permissionFlow.startPermissionFlow(PermissionType.LOCATION_FINE)
                return@launch
            }

            // Check background permission
            val hasBackgroundPermission = permissionFlow.isPermissionGranted(PermissionType.LOCATION_BACKGROUND)

            if (hasBackgroundPermission) {
                // Have both permissions, start tracking
                startTrackingInternal(mode)
            } else {
                // Need background permission
                logger.info { "Background location permission not granted, requesting..." }
                _uiState.update {
                    it.copy(
                        isProcessing = true,
                        error = null,
                        pendingTrackingMode = mode
                    )
                }
                permissionFlow.startPermissionFlow(PermissionType.LOCATION_BACKGROUND)
            }
        }
    }

    /**
     * Internal method to actually start tracking (after permissions are granted).
     */
    private fun startTrackingInternal(mode: TrackingMode) {
        _uiState.update { it.copy(isProcessing = true, error = null) }

        controllerScope.launch {
            when (val result = startTrackingUseCase.execute(mode)) {
                is StartTrackingUseCase.Result.Success -> {
                    logger.info { "Tracking started successfully" }
                    _uiState.update { it.copy(isProcessing = false) }
                }
                is StartTrackingUseCase.Result.PermissionDenied -> {
                    logger.warn { "Permission denied during tracking start" }
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            error = "Location permission required",
                            hasPermissions = false
                        )
                    }
                }
                is StartTrackingUseCase.Result.Error -> {
                    logger.error { "Failed to start tracking: ${result.message}" }
                    _uiState.update { it.copy(isProcessing = false, error = result.message) }
                }
            }
        }
    }

    /**
     * Stop tracking.
     */
    fun stopTracking() {
        logger.info { "User requested to stop tracking" }

        _uiState.update { it.copy(isProcessing = true, error = null) }

        controllerScope.launch {
            stopTrackingUseCase.execute()
            _uiState.update { it.copy(isProcessing = false) }
        }
    }

    /**
     * Check if permissions are granted.
     */
    fun checkPermissions() {
        controllerScope.launch {
            val hasFinePermission = permissionFlow.isPermissionGranted(PermissionType.LOCATION_FINE)
            _uiState.update { it.copy(hasPermissions = hasFinePermission) }
            logger.debug { "Permissions check: $hasFinePermission" }
        }
    }

    /**
     * Request permissions using the permission flow.
     * This shows the user-friendly permission dialogs.
     */
    fun requestPermissions() {
        logger.info { "Requesting location permissions" }
        permissionFlow.startPermissionFlow(PermissionType.LOCATION_FINE)
    }

    /**
     * Clear error state.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
        permissionFlow.clearError()
    }

    /**
     * Cleanup method to release resources and prevent memory leaks.
     * MUST be called when this controller is no longer needed.
     *
     * Cancels all running coroutines including flow collectors.
     */
    override fun cleanup() {
        logger.info { "Cleaning up LocationTrackingController" }
        controllerScope.cancel()
        logger.debug { "LocationTrackingController cleanup complete" }
    }
}
