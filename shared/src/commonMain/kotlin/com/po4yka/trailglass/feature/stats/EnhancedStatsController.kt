package com.po4yka.trailglass.feature.stats

import com.po4yka.trailglass.feature.stats.models.ComprehensiveStatistics
import com.po4yka.trailglass.logging.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

/**
 * Controller for enhanced stats feature with comprehensive analytics.
 */
@Inject
class EnhancedStatsController(
    private val getComprehensiveStatsUseCase: GetComprehensiveStatsUseCase,
    private val coroutineScope: CoroutineScope,
    private val userId: String
) {

    private val logger = logger()

    /**
     * Enhanced stats UI state.
     */
    data class EnhancedStatsState(
        val period: GetStatsUseCase.Period? = null,
        val stats: ComprehensiveStatistics? = null,
        val isLoading: Boolean = false,
        val error: String? = null
    )

    private val _state = MutableStateFlow(EnhancedStatsState())
    val state: StateFlow<EnhancedStatsState> = _state.asStateFlow()

    /**
     * Load comprehensive stats for a specific period.
     */
    fun loadPeriod(period: GetStatsUseCase.Period) {
        logger.debug { "Loading comprehensive stats for period: $period" }

        _state.update { it.copy(isLoading = true, period = period, error = null) }

        coroutineScope.launch {
            try {
                val stats = getComprehensiveStatsUseCase.execute(period, userId)
                _state.update { it.copy(stats = stats, isLoading = false) }
                logger.info {
                    "Loaded comprehensive stats for $period: ${stats.distanceStats.totalDistanceKm.toInt()} km, " +
                    "${stats.geographicStats.countries.size} countries"
                }
            } catch (e: Exception) {
                logger.error(e) { "Failed to load comprehensive stats for $period" }
                _state.update {
                    it.copy(
                        error = e.message ?: "Unknown error",
                        isLoading = false
                    )
                }
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
