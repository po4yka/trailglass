package com.po4yka.trailglass.feature.timeline

import com.po4yka.trailglass.logging.logger
import kotlinx.coroutines.CoroutineScope
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
 */
@Inject
class TimelineController(
    private val getTimelineUseCase: GetTimelineForDayUseCase,
    private val coroutineScope: CoroutineScope,
    private val userId: String
) {

    private val logger = logger()

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

        coroutineScope.launch {
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
}
