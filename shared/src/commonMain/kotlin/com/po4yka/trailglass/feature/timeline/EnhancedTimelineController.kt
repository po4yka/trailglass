package com.po4yka.trailglass.feature.timeline

import com.po4yka.trailglass.feature.common.Lifecycle
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
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import me.tatarka.inject.annotations.Inject

/**
 * Enhanced controller for timeline feature with zoom levels and filtering.
 *
 * IMPORTANT: Call [cleanup] when this controller is no longer needed to prevent memory leaks.
 */
@Inject
class EnhancedTimelineController(
    private val getTimelineUseCase: GetTimelineUseCase,
    coroutineScope: CoroutineScope,
    private val userId: String
) : Lifecycle {
    private val logger = logger()

    // Create a child scope that can be cancelled independently
    private val controllerScope =
        CoroutineScope(
            coroutineScope.coroutineContext + SupervisorJob()
        )

    /** Enhanced timeline UI state. */
    data class EnhancedTimelineState(
        val selectedDate: LocalDate =
            Clock.System
                .now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date,
        val zoomLevel: TimelineZoomLevel = TimelineZoomLevel.DAY,
        val items: List<GetTimelineUseCase.TimelineItemUI> = emptyList(),
        val filter: TimelineFilter = TimelineFilter(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val searchQuery: String? = null
    )

    private val _state = MutableStateFlow(EnhancedTimelineState())
    val state: StateFlow<EnhancedTimelineState> = _state.asStateFlow()

    init {
        // Load today's timeline on initialization
        loadTimeline()
    }

    /** Load timeline for the current state. */
    fun loadTimeline() {
        val currentState = _state.value

        logger.debug { "Loading timeline: date=${currentState.selectedDate}, zoom=${currentState.zoomLevel}" }

        _state.update { it.copy(isLoading = true, error = null) }

        controllerScope.launch {
            try {
                val items =
                    getTimelineUseCase.execute(
                        zoomLevel = currentState.zoomLevel,
                        referenceDate = currentState.selectedDate,
                        userId = userId,
                        filter = currentState.filter
                    )

                _state.update { it.copy(items = items, isLoading = false) }
                logger.info { "Loaded ${items.size} timeline items" }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logger.error(e) { "Failed to load timeline" }
                _state.update { it.copy(error = e.message ?: "Unknown error", isLoading = false) }
            }
        }
    }

    /** Change the selected date. */
    fun selectDate(date: LocalDate) {
        logger.debug { "Selecting date: $date" }
        _state.update { it.copy(selectedDate = date) }
        loadTimeline()
    }

    /** Navigate to next period (day/week/month/year depending on zoom level). */
    fun navigateNext() {
        val currentState = _state.value
        val nextDate =
            when (currentState.zoomLevel) {
                TimelineZoomLevel.DAY ->
                    kotlinx.datetime.LocalDate.Companion.fromEpochDays(
                        currentState.selectedDate.toEpochDays() + 1
                    )

                TimelineZoomLevel.WEEK ->
                    kotlinx.datetime.LocalDate.Companion.fromEpochDays(
                        currentState.selectedDate.toEpochDays() + 7
                    )

                TimelineZoomLevel.MONTH -> {
                    val year =
                        if (currentState.selectedDate.monthNumber ==
                            12
                        ) {
                            currentState.selectedDate.year + 1
                        } else {
                            currentState.selectedDate.year
                        }
                    val month =
                        if (currentState.selectedDate.monthNumber ==
                            12
                        ) {
                            1
                        } else {
                            currentState.selectedDate.monthNumber + 1
                        }
                    kotlinx.datetime.LocalDate(year, month, 1)
                }

                TimelineZoomLevel.YEAR ->
                    kotlinx.datetime.LocalDate(
                        currentState.selectedDate.year + 1,
                        currentState.selectedDate.monthNumber,
                        1
                    )
            }
        selectDate(nextDate)
    }

    /** Navigate to previous period (day/week/month/year depending on zoom level). */
    fun navigatePrevious() {
        val currentState = _state.value
        val previousDate =
            when (currentState.zoomLevel) {
                TimelineZoomLevel.DAY ->
                    kotlinx.datetime.LocalDate.Companion.fromEpochDays(
                        currentState.selectedDate.toEpochDays() - 1
                    )

                TimelineZoomLevel.WEEK ->
                    kotlinx.datetime.LocalDate.Companion.fromEpochDays(
                        currentState.selectedDate.toEpochDays() - 7
                    )

                TimelineZoomLevel.MONTH -> {
                    val year =
                        if (currentState.selectedDate.monthNumber ==
                            1
                        ) {
                            currentState.selectedDate.year - 1
                        } else {
                            currentState.selectedDate.year
                        }
                    val month =
                        if (currentState.selectedDate.monthNumber ==
                            1
                        ) {
                            12
                        } else {
                            currentState.selectedDate.monthNumber - 1
                        }
                    kotlinx.datetime.LocalDate(year, month, 1)
                }

                TimelineZoomLevel.YEAR ->
                    kotlinx.datetime.LocalDate(
                        currentState.selectedDate.year - 1,
                        currentState.selectedDate.monthNumber,
                        1
                    )
            }
        selectDate(previousDate)
    }

    /** Jump to today. */
    fun jumpToToday() {
        val today =
            Clock.System
                .now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date
        selectDate(today)
    }

    /** Change zoom level. */
    fun setZoomLevel(zoomLevel: TimelineZoomLevel) {
        logger.debug { "Changing zoom level to: $zoomLevel" }
        _state.update { it.copy(zoomLevel = zoomLevel) }
        loadTimeline()
    }

    /** Update filter. */
    fun updateFilter(filter: TimelineFilter) {
        logger.debug { "Updating filter: ${filter.activeFilterCount} active filters" }
        _state.update { it.copy(filter = filter) }
        loadTimeline()
    }

    /** Clear all filters. */
    fun clearFilters() {
        logger.debug { "Clearing all filters" }
        _state.update { it.copy(filter = TimelineFilter()) }
        loadTimeline()
    }

    /** Search timeline. */
    fun search(query: String?) {
        logger.debug { "Searching timeline: query=$query" }
        val currentFilter = _state.value.filter
        val newFilter = currentFilter.copy(searchQuery = query)
        _state.update { it.copy(searchQuery = query, filter = newFilter) }
        loadTimeline()
    }

    /** Clear search. */
    fun clearSearch() {
        search(null)
    }

    /** Refresh the timeline. */
    fun refresh() {
        logger.debug { "Refreshing timeline" }
        loadTimeline()
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
        logger.info { "Cleaning up EnhancedTimelineController" }
        controllerScope.cancel()
        logger.debug { "EnhancedTimelineController cleanup complete" }
    }
}
