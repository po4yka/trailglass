package com.po4yka.trailglass.feature.stats

import com.po4yka.trailglass.logging.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Controller for stats feature.
 * Manages statistics state and period selection.
 */
@Inject
class StatsController(
    private val getStatsUseCase: GetStatsUseCase,
    private val coroutineScope: CoroutineScope,
    private val userId: String
) {

    private val logger = logger()

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

        coroutineScope.launch {
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
}
