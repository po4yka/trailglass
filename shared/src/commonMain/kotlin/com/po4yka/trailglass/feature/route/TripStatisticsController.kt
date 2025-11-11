package com.po4yka.trailglass.feature.route

import com.po4yka.trailglass.domain.model.Trip
import com.po4yka.trailglass.domain.model.TripRoute
import com.po4yka.trailglass.logging.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

/**
 * Controller for Trip Statistics screen.
 * Manages statistics data and display state.
 */
@Inject
class TripStatisticsController(
    private val getTripRouteUseCase: GetTripRouteUseCase,
    private val coroutineScope: CoroutineScope
) {

    private val logger = logger()

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
        coroutineScope.launch {
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
                .onFailure { error ->
                    logger.error(error) { "Failed to load statistics for trip $tripId" }
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load statistics"
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
}
