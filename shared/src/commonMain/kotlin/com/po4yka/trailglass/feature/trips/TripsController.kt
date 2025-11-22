package com.po4yka.trailglass.feature.trips

import com.po4yka.trailglass.domain.model.Trip
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
import com.po4yka.trailglass.domain.error.Result as TrailGlassResult

/**
 * Controller for trips list view.
 * Handles loading and managing user's trips.
 *
 * IMPORTANT: Call [cleanup] when this controller is no longer needed to prevent memory leaks.
 */
@Inject
class TripsController(
    private val getTripsUseCase: GetTripsUseCase,
    private val createTripUseCase: CreateTripUseCase,
    private val updateTripUseCase: UpdateTripUseCase,
    private val deleteTripUseCase: DeleteTripUseCase,
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
     * Sort option for trips list.
     */
    enum class SortOption {
        DATE_DESC, // Most recent first (default)
        DATE_ASC, // Oldest first
        NAME_ASC, // Alphabetical by name
        DURATION_DESC, // Longest trips first
        DISTANCE_DESC // Farthest trips first
    }

    /**
     * Filter option for trips list.
     */
    data class FilterOptions(
        val showOngoing: Boolean = true,
        val showCompleted: Boolean = true,
        val searchQuery: String = ""
    ) {
        val isFiltered: Boolean
            get() = !showOngoing || !showCompleted || searchQuery.isNotEmpty()
    }

    /**
     * Trips UI state.
     */
    data class TripsState(
        val trips: List<Trip> = emptyList(),
        val ongoingTrips: List<Trip> = emptyList(),
        val completedTrips: List<Trip> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val sortOption: SortOption = SortOption.DATE_DESC,
        val filterOptions: FilterOptions = FilterOptions(),
        val showCreateDialog: Boolean = false
    ) {
        /**
         * Get filtered and sorted trips based on current options.
         */
        val filteredTrips: List<Trip>
            get() {
                var result = trips

                // Apply filter
                if (!filterOptions.showOngoing || !filterOptions.showCompleted) {
                    result =
                        result.filter {
                            (filterOptions.showOngoing && it.isOngoing) ||
                                (filterOptions.showCompleted && !it.isOngoing)
                        }
                }

                // Apply search query
                if (filterOptions.searchQuery.isNotEmpty()) {
                    val query = filterOptions.searchQuery.lowercase()
                    result =
                        result.filter {
                            it.displayName.lowercase().contains(query) ||
                                it.description?.lowercase()?.contains(query) == true ||
                                it.primaryCountry?.lowercase()?.contains(query) == true ||
                                it.countriesVisited.any { country -> country.lowercase().contains(query) } ||
                                it.citiesVisited.any { city -> city.lowercase().contains(query) }
                        }
                }

                // Apply sort
                return when (sortOption) {
                    SortOption.DATE_DESC -> result.sortedByDescending { it.startTime }
                    SortOption.DATE_ASC -> result.sortedBy { it.startTime }
                    SortOption.NAME_ASC -> result.sortedBy { it.displayName }
                    SortOption.DURATION_DESC -> result.sortedByDescending { it.duration?.inWholeSeconds ?: 0 }
                    SortOption.DISTANCE_DESC -> result.sortedByDescending { it.totalDistanceMeters }
                }
            }
    }

    private val _state = MutableStateFlow(TripsState())
    val state: StateFlow<TripsState> = _state.asStateFlow()

    /**
     * Load all trips for the current user.
     */
    fun loadTrips() {
        logger.debug { "Loading trips for user $userId" }

        _state.update { it.copy(isLoading = true, error = null) }

        controllerScope.launch {
            when (val result = getTripsUseCase.execute(userId)) {
                is TrailGlassResult.Success -> {
                    val trips = result.data
                    val ongoing = trips.filter { it.isOngoing }
                    val completed = trips.filter { !it.isOngoing }

                    _state.update {
                        it.copy(
                            trips = trips,
                            ongoingTrips = ongoing,
                            completedTrips = completed,
                            isLoading = false
                        )
                    }

                    logger.info { "Loaded ${trips.size} trips (${ongoing.size} ongoing, ${completed.size} completed)" }
                }
                is TrailGlassResult.Error -> {
                    val error = result.error.userMessage
                    logger.error { "Failed to load trips: $error" }
                    _state.update {
                        it.copy(
                            error = error,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    /**
     * Refresh trips list.
     */
    fun refresh() {
        logger.debug { "Refreshing trips" }
        loadTrips()
    }

    /**
     * Update sort option.
     */
    fun setSortOption(option: SortOption) {
        logger.debug { "Setting sort option to $option" }
        _state.update { it.copy(sortOption = option) }
    }

    /**
     * Update filter options.
     */
    fun setFilterOptions(options: FilterOptions) {
        logger.debug { "Updating filter options: $options" }
        _state.update { it.copy(filterOptions = options) }
    }

    /**
     * Update search query.
     */
    fun setSearchQuery(query: String) {
        _state.update {
            it.copy(filterOptions = it.filterOptions.copy(searchQuery = query))
        }
    }

    /**
     * Show create trip dialog.
     */
    fun showCreateDialog() {
        logger.info { "Showing create trip dialog" }
        _state.update { it.copy(showCreateDialog = true) }
    }

    /**
     * Hide create trip dialog.
     */
    fun dismissCreateDialog() {
        _state.update { it.copy(showCreateDialog = false) }
    }

    /**
     * Create a new trip.
     */
    fun createTrip(trip: Trip) {
        logger.debug { "Creating new trip: ${trip.displayName}" }

        _state.update { it.copy(isLoading = true, error = null, showCreateDialog = false) }

        controllerScope.launch {
            val result =
                createTripUseCase.execute(
                    userId = userId,
                    name = trip.name,
                    startTime = trip.startTime,
                    endTime = trip.endTime,
                    description = trip.description,
                    isAutoDetected = trip.isAutoDetected,
                    detectionConfidence = trip.detectionConfidence
                )

            when (result) {
                is TrailGlassResult.Success -> {
                    logger.info { "Created trip: ${result.data.id}" }

                    // Reload trips to include the new one
                    loadTrips()
                }
                is TrailGlassResult.Error -> {
                    val error = result.error.userMessage
                    logger.error { "Failed to create trip: $error" }
                    _state.update {
                        it.copy(
                            error = error,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    fun updateTrip(trip: Trip) {
        logger.debug { "Updating trip: ${trip.id}" }

        _state.update { it.copy(isLoading = true, error = null) }

        controllerScope.launch {
            val result =
                updateTripUseCase.execute(
                    tripId = trip.id,
                    name = trip.name,
                    description = trip.description,
                    endTime = trip.endTime,
                    coverPhotoUri = trip.coverPhotoUri,
                    tags = trip.tags,
                    isPublic = trip.isPublic
                )

            when (result) {
                is TrailGlassResult.Success -> {
                    logger.info { "Updated trip: ${result.data.id}" }

                    // Reload trips to reflect the update
                    loadTrips()
                }
                is TrailGlassResult.Error -> {
                    val error = result.error.userMessage
                    logger.error { "Failed to update trip: $error" }
                    _state.update {
                        it.copy(
                            error = error,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    /**
     * Delete a trip.
     */
    fun deleteTrip(
        tripId: String,
        onSuccess: () -> Unit = {}
    ) {
        logger.debug { "Deleting trip: $tripId" }

        _state.update { it.copy(isLoading = true, error = null) }

        controllerScope.launch {
            when (val result = deleteTripUseCase.execute(tripId)) {
                is TrailGlassResult.Success -> {
                    logger.info { "Deleted trip: $tripId" }

                    // Reload trips to reflect the deletion
                    loadTrips()

                    // Notify caller of successful deletion
                    onSuccess()
                }
                is TrailGlassResult.Error -> {
                    val error = result.error.userMessage
                    logger.error { "Failed to delete trip: $error" }
                    _state.update {
                        it.copy(
                            error = error,
                            isLoading = false
                        )
                    }
                }
            }
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
        logger.info { "Cleaning up TripsController" }
        controllerScope.cancel()
        logger.debug { "TripsController cleanup complete" }
    }
}
