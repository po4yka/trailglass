package com.po4yka.trailglass.feature.timeline

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
import kotlinx.datetime.LocalDate
import me.tatarka.inject.annotations.Inject

/**
 * Controller for timeline feature.
 * Manages timeline state and user actions.
 *
 * IMPORTANT: Call [cleanup] when this controller is no longer needed to prevent memory leaks.
 */
@Inject
class TimelineController(
    private val getTimelineUseCase: GetTimelineForDayUseCase,
    coroutineScope: CoroutineScope,
    private val userId: String
) : Lifecycle {
    private val logger = logger()

    // Create a child scope that can be cancelled independently
    private val controllerScope =
        CoroutineScope(
            coroutineScope.coroutineContext + SupervisorJob()
        )

    /**
     * Timeline UI state.
     */
    data class TimelineState(
        val selectedDate: LocalDate? = null,
        val items: List<GetTimelineForDayUseCase.TimelineItemUI> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null
    )

    private val _state = MutableStateFlow(TimelineState())
    val state: StateFlow<TimelineState> = _state.asStateFlow()

    /**
     * Load timeline for a specific day.
     */
    fun loadDay(date: LocalDate) {
        logger.debug { "Loading timeline for $date" }

        _state.update { it.copy(isLoading = true, selectedDate = date, error = null) }

        controllerScope.launch {
            try {
                val items = getTimelineUseCase.execute(date, userId)
                _state.update { it.copy(items = items, isLoading = false) }
                logger.info { "Loaded ${items.size} timeline items for $date" }
            } catch (e: Exception) {
                logger.error(e) { "Failed to load timeline for $date" }
                _state.update { it.copy(error = e.message ?: "Unknown error", isLoading = false) }
            }
        }
    }

    /**
     * Refresh the current day.
     */
    fun refresh() {
        _state.value.selectedDate?.let { date ->
            loadDay(date)
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
        logger.info { "Cleaning up TimelineController" }
        controllerScope.cancel()
        logger.debug { "TimelineController cleanup complete" }
    }
}
