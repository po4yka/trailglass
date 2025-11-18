package com.po4yka.trailglass.feature.places

import com.po4yka.trailglass.data.repository.FrequentPlaceRepository
import com.po4yka.trailglass.domain.model.FrequentPlace
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
        val searchQuery: String = ""
    )

    private val _state = MutableStateFlow(PlacesState())
    val state: StateFlow<PlacesState> = _state.asStateFlow()

    init {
        // Load places on initialization
        loadPlaces()
    }

    /**
     * Filter places based on current search query.
     */
    private fun filterPlaces(allPlaces: List<FrequentPlace>, query: String): List<FrequentPlace> {
        if (query.isBlank()) return allPlaces

        val searchTerm = query.trim().lowercase()
        return allPlaces.filter { place ->
            place.displayName.lowercase().contains(searchTerm) ||
            place.name?.lowercase()?.contains(searchTerm) == true ||
            place.address?.lowercase()?.contains(searchTerm) == true ||
            place.city?.lowercase()?.contains(searchTerm) == true ||
            place.userLabel?.lowercase()?.contains(searchTerm) == true
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
                    val filteredPlaces = filterPlaces(allPlaces, _state.value.searchQuery)
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
                    val filteredPlaces = filterPlaces(allPlaces, _state.value.searchQuery)
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
            val filteredPlaces = filterPlaces(state.allPlaces, query)
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
            state.copy(
                searchQuery = "",
                places = state.allPlaces
            )
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
