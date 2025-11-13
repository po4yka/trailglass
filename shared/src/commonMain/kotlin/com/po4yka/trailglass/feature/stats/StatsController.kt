package com.po4yka.trailglass.feature.stats

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
 * Controller for stats feature.
 * Manages statistics state and period selection.
 *
 * IMPORTANT: Call [cleanup] when this controller is no longer needed to prevent memory leaks.
 */
@Inject
class StatsController(
    private val getStatsUseCase: GetStatsUseCase,
    coroutineScope: CoroutineScope,
    private val userId: String
) : Lifecycle {

    private val logger = logger()

    // Create a child scope that can be cancelled independently
    private val controllerScope = CoroutineScope(
        coroutineScope.coroutineContext + SupervisorJob()
    )

    /**
     * Stats UI state.
     */
    data class StatsState(
        val period: GetStatsUseCase.Period? = null,
        val stats: GetStatsUseCase.Stats? = null,
        val isLoading: Boolean = false,
        val error: String? = null
    )

    private val _state = MutableStateFlow(StatsState())
    val state: StateFlow<StatsState> = _state.asStateFlow()

    /**
     * Load stats for a specific period.
     */
    fun loadPeriod(period: GetStatsUseCase.Period) {
        logger.debug { "Loading stats for period: $period" }

        _state.update { it.copy(isLoading = true, period = period, error = null) }

        controllerScope.launch {
            try {
                val stats = getStatsUseCase.execute(period, userId)
                _state.update { it.copy(stats = stats, isLoading = false) }
                logger.info {
                    "Loaded stats for $period: ${stats.countriesVisited.size} countries, " +
                    "${stats.totalTrips} trips"
                }
            } catch (e: Exception) {
                logger.error(e) { "Failed to load stats for $period" }
                _state.update { it.copy(error = e.message ?: "Unknown error", isLoading = false) }
            }
        }
    }

    /**
     * Refresh the current period.
     */
    fun refresh() {
        _state.value.period?.let { period ->
            loadPeriod(period)
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
        logger.info { "Cleaning up StatsController" }
        controllerScope.cancel()
        logger.debug { "StatsController cleanup complete" }
    }
}
