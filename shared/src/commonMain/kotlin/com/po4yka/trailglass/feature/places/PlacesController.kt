package com.po4yka.trailglass.feature.places

import com.po4yka.trailglass.data.repository.FrequentPlaceRepository
import com.po4yka.trailglass.domain.model.FrequentPlace
import com.po4yka.trailglass.domain.model.PlaceCategory
import com.po4yka.trailglass.domain.model.PlaceSignificance
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
 * Sort options for places list.
 */
enum class PlaceSortOption {
    MOST_VISITED,
    RECENTLY_VISITED,
    ALPHABETICAL,
    BY_SIGNIFICANCE
}

/**
 * Controller for places feature.
 * Manages frequent places state and user actions.
 *
 * IMPORTANT: Call [cleanup] when this controller is no longer needed to prevent memory leaks.
 */
@Inject
class PlacesController(
    private val getFrequentPlacesUseCase: GetFrequentPlacesUseCase,
    private val frequentPlaceRepository: FrequentPlaceRepository,
    coroutineScope: CoroutineScope,
    private val userId: String
) : Lifecycle {

    private val logger = logger()

    // Create a child scope that can be cancelled independently
    private val controllerScope = CoroutineScope(
        coroutineScope.coroutineContext + SupervisorJob()
    )

    /**
     * Places UI state.
     */
    data class PlacesState(
        val allPlaces: List<FrequentPlace> = emptyList(),
        val places: List<FrequentPlace> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val minSignificance: PlaceSignificance = PlaceSignificance.RARE,
        val searchQuery: String = "",
        val selectedCategories: Set<PlaceCategory> = emptySet(),
        val sortOption: PlaceSortOption = PlaceSortOption.BY_SIGNIFICANCE
    )

    private val _state = MutableStateFlow(PlacesState())
    val state: StateFlow<PlacesState> = _state.asStateFlow()

    init {
        // Load places on initialization
        loadPlaces()
    }

    /**
     * Filter and sort places based on current state.
     */
    private fun filterAndSortPlaces(
        allPlaces: List<FrequentPlace>,
        query: String,
        categories: Set<PlaceCategory>,
        sortOption: PlaceSortOption
    ): List<FrequentPlace> {
        // Filter by search query
        var filtered = if (query.isBlank()) {
            allPlaces
        } else {
            val searchTerm = query.trim().lowercase()
            allPlaces.filter { place ->
                place.displayName.lowercase().contains(searchTerm) ||
                place.name?.lowercase()?.contains(searchTerm) == true ||
                place.address?.lowercase()?.contains(searchTerm) == true ||
                place.city?.lowercase()?.contains(searchTerm) == true ||
                place.userLabel?.lowercase()?.contains(searchTerm) == true
            }
        }

        // Filter by categories
        if (categories.isNotEmpty()) {
            filtered = filtered.filter { it.category in categories }
        }

        // Sort
        return when (sortOption) {
            PlaceSortOption.MOST_VISITED -> filtered.sortedByDescending { it.visitCount }
            PlaceSortOption.RECENTLY_VISITED -> filtered.sortedByDescending { it.lastVisitTime }
            PlaceSortOption.ALPHABETICAL -> filtered.sortedBy { it.displayName.lowercase() }
            PlaceSortOption.BY_SIGNIFICANCE -> filtered.sortedWith(
                compareByDescending<FrequentPlace> { it.significance.ordinal }
                    .thenByDescending { it.visitCount }
            )
        }
    }

    /**
     * Load frequent places for the user.
     */
    fun loadPlaces() {
        logger.debug { "Loading frequent places for user $userId" }

        _state.update { it.copy(isLoading = true, error = null) }

        controllerScope.launch {
            getFrequentPlacesUseCase.execute(userId, _state.value.minSignificance)
                .onSuccess { allPlaces ->
                    logger.info { "Loaded ${allPlaces.size} frequent places" }
                    val currentState = _state.value
                    val filteredPlaces = filterAndSortPlaces(
                        allPlaces,
                        currentState.searchQuery,
                        currentState.selectedCategories,
                        currentState.sortOption
                    )
                    _state.update {
                        it.copy(
                            allPlaces = allPlaces,
                            places = filteredPlaces,
                            isLoading = false
                        )
                    }
                }
                .onFailure { error ->
                    logger.error(error) { "Failed to load frequent places" }
                    _state.update {
                        it.copy(
                            error = error.message ?: "Failed to load places",
                            isLoading = false
                        )
                    }
                }
        }
    }

    /**
     * Refresh the frequent places list.
     */
    fun refresh() {
        logger.info { "Refreshing frequent places" }

        _state.update { it.copy(isLoading = true, error = null) }

        controllerScope.launch {
            getFrequentPlacesUseCase.refresh(userId)
                .onSuccess { allPlaces ->
                    logger.info { "Refreshed ${allPlaces.size} frequent places" }
                    val currentState = _state.value
                    val filteredPlaces = filterAndSortPlaces(
                        allPlaces,
                        currentState.searchQuery,
                        currentState.selectedCategories,
                        currentState.sortOption
                    )
                    _state.update {
                        it.copy(
                            allPlaces = allPlaces,
                            places = filteredPlaces,
                            isLoading = false
                        )
                    }
                }
                .onFailure { error ->
                    logger.error(error) { "Failed to refresh frequent places" }
                    _state.update {
                        it.copy(
                            error = error.message ?: "Failed to refresh places",
                            isLoading = false
                        )
                    }
                }
        }
    }

    /**
     * Toggle favorite status for a place.
     */
    fun toggleFavorite(placeId: String) {
        controllerScope.launch {
            try {
                val place = frequentPlaceRepository.getPlaceById(placeId)
                if (place != null) {
                    val updatedPlace = place.copy(isFavorite = !place.isFavorite)
                    frequentPlaceRepository.updatePlace(updatedPlace)

                    // Update local state immediately for better UX
                    _state.update { state ->
                        val updatedAllPlaces = state.allPlaces.map { p ->
                            if (p.id == placeId) updatedPlace else p
                        }
                        val updatedFilteredPlaces = state.places.map { p ->
                            if (p.id == placeId) updatedPlace else p
                        }
                        state.copy(
                            allPlaces = updatedAllPlaces,
                            places = updatedFilteredPlaces
                        )
                    }

                    logger.info { "Toggled favorite for place $placeId to ${updatedPlace.isFavorite}" }
                } else {
                    logger.warn { "Place not found: $placeId" }
                }
            } catch (e: Exception) {
                logger.error(e) { "Failed to toggle favorite for place $placeId" }
            }
        }
    }

    /**
     * Get a specific place by ID.
     */
    suspend fun getPlaceById(placeId: String): FrequentPlace? {
        return try {
            frequentPlaceRepository.getPlaceById(placeId)
        } catch (e: Exception) {
            logger.error(e) { "Failed to get place $placeId" }
            null
        }
    }

    /**
     * Filter places by minimum significance.
     */
    fun setMinSignificance(significance: PlaceSignificance) {
        _state.update { it.copy(minSignificance = significance) }
        loadPlaces()
    }

    /**
     * Search places by query.
     * Filters places by name, address, city, or user label.
     */
    fun search(query: String) {
        logger.debug { "Searching places with query: $query" }

        _state.update { state ->
            val filteredPlaces = filterAndSortPlaces(
                state.allPlaces,
                query,
                state.selectedCategories,
                state.sortOption
            )
            state.copy(
                searchQuery = query,
                places = filteredPlaces
            )
        }
    }

    /**
     * Clear search query and show all places.
     */
    fun clearSearch() {
        logger.debug { "Clearing search" }

        _state.update { state ->
            val filteredPlaces = filterAndSortPlaces(
                state.allPlaces,
                "",
                state.selectedCategories,
                state.sortOption
            )
            state.copy(
                searchQuery = "",
                places = filteredPlaces
            )
        }
    }

    /**
     * Toggle category filter.
     */
    fun toggleCategoryFilter(category: PlaceCategory) {
        logger.debug { "Toggling category filter: $category" }

        _state.update { state ->
            val newCategories = if (category in state.selectedCategories) {
                state.selectedCategories - category
            } else {
                state.selectedCategories + category
            }

            val filteredPlaces = filterAndSortPlaces(
                state.allPlaces,
                state.searchQuery,
                newCategories,
                state.sortOption
            )

            state.copy(
                selectedCategories = newCategories,
                places = filteredPlaces
            )
        }
    }

    /**
     * Clear all category filters.
     */
    fun clearCategoryFilters() {
        logger.debug { "Clearing category filters" }

        _state.update { state ->
            val filteredPlaces = filterAndSortPlaces(
                state.allPlaces,
                state.searchQuery,
                emptySet(),
                state.sortOption
            )

            state.copy(
                selectedCategories = emptySet(),
                places = filteredPlaces
            )
        }
    }

    /**
     * Set sort option.
     */
    fun setSortOption(option: PlaceSortOption) {
        logger.debug { "Setting sort option: $option" }

        _state.update { state ->
            val filteredPlaces = filterAndSortPlaces(
                state.allPlaces,
                state.searchQuery,
                state.selectedCategories,
                option
            )

            state.copy(
                sortOption = option,
                places = filteredPlaces
            )
        }
    }

    /**
     * Update place category manually.
     */
    fun updatePlaceCategory(placeId: String, newCategory: PlaceCategory) {
        controllerScope.launch {
            try {
                val place = frequentPlaceRepository.getPlaceById(placeId)
                if (place != null) {
                    val updatedPlace = place.copy(category = newCategory)
                    frequentPlaceRepository.updatePlace(updatedPlace)

                    // Update local state
                    _state.update { state ->
                        val updatedAllPlaces = state.allPlaces.map { p ->
                            if (p.id == placeId) updatedPlace else p
                        }
                        val filteredPlaces = filterAndSortPlaces(
                            updatedAllPlaces,
                            state.searchQuery,
                            state.selectedCategories,
                            state.sortOption
                        )
                        state.copy(
                            allPlaces = updatedAllPlaces,
                            places = filteredPlaces
                        )
                    }

                    logger.info { "Updated category for place $placeId to $newCategory" }
                } else {
                    logger.warn { "Place not found: $placeId" }
                }
            } catch (e: Exception) {
                logger.error(e) { "Failed to update category for place $placeId" }
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
        logger.info { "Cleaning up PlacesController" }
        controllerScope.cancel()
        logger.debug { "PlacesController cleanup complete" }
    }
}
