package com.po4yka.trailglass.feature.route

import com.po4yka.trailglass.domain.model.Trip
import com.po4yka.trailglass.domain.model.TripRoute
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
 * Controller for Trip Statistics screen.
 * Manages statistics data and display state.
 *
 * IMPORTANT: Call [cleanup] when this controller is no longer needed to prevent memory leaks.
 */
@Inject
class TripStatisticsController(
    private val getTripRouteUseCase: GetTripRouteUseCase,
    coroutineScope: CoroutineScope
) : Lifecycle {

    private val logger = logger()

    // Create a child scope that can be cancelled independently
    private val controllerScope = CoroutineScope(
        coroutineScope.coroutineContext + SupervisorJob()
    )

    /**
     * State for Trip Statistics screen.
     */
    data class StatisticsState(
        val tripRoute: TripRoute? = null,
        val isLoading: Boolean = false,
        val error: String? = null,

        // UI state
        val showTransportBreakdown: Boolean = true
    )

    private val _state = MutableStateFlow(StatisticsState())
    val state: StateFlow<StatisticsState> = _state.asStateFlow()

    /**
     * Load trip statistics.
     */
    fun loadStatistics(tripId: String) {
        controllerScope.launch {
            logger.info { "Loading statistics for trip $tripId" }
            _state.value = _state.value.copy(isLoading = true, error = null)

            getTripRouteUseCase.execute(tripId)
                .onSuccess { tripRoute ->
                    logger.info { "Statistics loaded successfully" }
                    _state.value = _state.value.copy(
                        tripRoute = tripRoute,
                        isLoading = false
                    )
                }
                .onError { error ->
                    logger.error { "Failed to load statistics for trip $tripId - ${error.getTechnicalDetails()}" }
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error.getUserFriendlyMessage()
                    )
                }
        }
    }

    /**
     * Toggle transport breakdown visibility.
     */
    fun toggleTransportBreakdown() {
        _state.value = _state.value.copy(
            showTransportBreakdown = !_state.value.showTransportBreakdown
        )
    }

    /**
     * Clear error state.
     */
    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    /**
     * Cleanup method to release resources and prevent memory leaks.
     * MUST be called when this controller is no longer needed.
     *
     * Cancels all running coroutines including flow collectors.
     */
    override fun cleanup() {
        logger.info { "Cleaning up TripStatisticsController" }
        controllerScope.cancel()
        logger.debug { "TripStatisticsController cleanup complete" }
    }
}
