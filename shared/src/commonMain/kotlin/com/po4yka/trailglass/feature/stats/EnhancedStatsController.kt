package com.po4yka.trailglass.feature.stats

import com.po4yka.trailglass.feature.common.Lifecycle
import com.po4yka.trailglass.feature.stats.models.ComprehensiveStatistics
import com.po4yka.trailglass.logging.logger
import kotlinx.coroutines.CancellationException
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
 * Controller for enhanced stats feature with comprehensive analytics.
 *
 * IMPORTANT: Call [cleanup] when this controller is no longer needed to prevent memory leaks.
 */
@Inject
class EnhancedStatsController(
    private val getComprehensiveStatsUseCase: GetComprehensiveStatsUseCase,
    coroutineScope: CoroutineScope,
    private val userId: String
) : Lifecycle {
    private val logger = logger()

    // Create a child scope that can be cancelled independently
    private val controllerScope =
        CoroutineScope(
            coroutineScope.coroutineContext + SupervisorJob()
        )

    /** Enhanced stats UI state. */
    data class EnhancedStatsState(
        val period: GetStatsUseCase.Period? = null,
        val stats: ComprehensiveStatistics? = null,
        val isLoading: Boolean = false,
        val error: String? = null
    )

    private val _state = MutableStateFlow(EnhancedStatsState())
    val state: StateFlow<EnhancedStatsState> = _state.asStateFlow()

    /** Load comprehensive stats for a specific period. */
    fun loadPeriod(period: GetStatsUseCase.Period) {
        logger.debug { "Loading comprehensive stats for period: $period" }

        _state.update { it.copy(isLoading = true, period = period, error = null) }

        controllerScope.launch {
            try {
                val stats = getComprehensiveStatsUseCase.execute(period, userId)
                _state.update { it.copy(stats = stats, isLoading = false) }
                logger.info {
                    "Loaded comprehensive stats for $period: ${stats.distanceStats.totalDistanceKm.toInt()} km, " +
                        "${stats.geographicStats.countries.size} countries"
                }
            } catch (e: CancellationException) {
                throw e
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

    /** Refresh the current period. */
    fun refresh() {
        _state.value.period?.let { period ->
            loadPeriod(period)
        }
    }

    /** Clear error state. */
    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    /**
     * Cleanup method to release resources and prevent memory leaks. MUST be called when this controller is no longer
     * needed.
     *
     * Cancels all running coroutines including flow collectors.
     */
    override fun cleanup() {
        logger.info { "Cleaning up EnhancedStatsController" }
        controllerScope.cancel()
        logger.debug { "EnhancedStatsController cleanup complete" }
    }
}
