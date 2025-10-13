package com.po4yka.trailglass.feature.tracking

import com.po4yka.trailglass.location.tracking.LocationTracker
import com.po4yka.trailglass.location.tracking.TrackingMode
import com.po4yka.trailglass.location.tracking.TrackingState
import com.po4yka.trailglass.logging.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Controller for location tracking feature.
 * Manages tracking state and user actions.
 */
@Inject
class LocationTrackingController(
    private val locationTracker: LocationTracker,
    private val startTrackingUseCase: StartTrackingUseCase,
    private val stopTrackingUseCase: StopTrackingUseCase,
    private val coroutineScope: CoroutineScope
) {

    private val logger = logger()

    /**
     * Location tracking UI state.
     */
    data class LocationTrackingUIState(
        val trackingState: TrackingState = TrackingState(),
        val hasPermissions: Boolean = false,
        val isProcessing: Boolean = false,
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(LocationTrackingUIState())
    val uiState: StateFlow<LocationTrackingUIState> = _uiState.asStateFlow()

    init {
        // Observe tracking state from tracker
        coroutineScope.launch {
            locationTracker.trackingState.collect { trackingState ->
                _uiState.update { it.copy(trackingState = trackingState) }
            }
        }

        // Check permissions
        checkPermissions()
    }

    /**
     * Start tracking with the specified mode.
     */
    fun startTracking(mode: TrackingMode) {
        logger.info { "User requested to start tracking: $mode" }

        _uiState.update { it.copy(isProcessing = true, error = null) }

        coroutineScope.launch {
            when (val result = startTrackingUseCase.execute(mode)) {
                is StartTrackingUseCase.Result.Success -> {
                    logger.info { "Tracking started successfully" }
                    _uiState.update { it.copy(isProcessing = false) }
                }
                is StartTrackingUseCase.Result.PermissionDenied -> {
                    logger.warn { "Permission denied" }
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

        coroutineScope.launch {
            stopTrackingUseCase.execute()
            _uiState.update { it.copy(isProcessing = false) }
        }
    }

    /**
     * Check if permissions are granted.
     */
    fun checkPermissions() {
        coroutineScope.launch {
            val hasPermissions = locationTracker.hasPermissions()
            _uiState.update { it.copy(hasPermissions = hasPermissions) }
            logger.debug { "Permissions check: $hasPermissions" }
        }
    }

    /**
     * Request permissions (platform-specific).
     */
    fun requestPermissions() {
        coroutineScope.launch {
            val granted = locationTracker.requestPermissions()
            _uiState.update { it.copy(hasPermissions = granted) }
        }
    }

    /**
     * Clear error state.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
