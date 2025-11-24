package com.po4yka.trailglass.feature.regions

import com.po4yka.trailglass.data.repository.RegionRepository
import com.po4yka.trailglass.domain.model.Coordinate
import com.po4yka.trailglass.domain.model.Region
import com.po4yka.trailglass.domain.model.RegionTransition
import com.po4yka.trailglass.domain.model.TransitionType
import com.po4yka.trailglass.feature.common.Lifecycle
import com.po4yka.trailglass.location.CurrentLocationProvider
import com.po4yka.trailglass.logging.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import me.tatarka.inject.annotations.Inject
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Sort options for regions list.
 */
enum class RegionSortOption {
    NAME,
    DISTANCE,
    MOST_VISITED,
    LAST_ENTERED
}

/**
 * Controller for regions (geofences) feature.
 * Manages user-defined regions and their state.
 *
 * IMPORTANT: Call [cleanup] when this controller is no longer needed to prevent memory leaks.
 */
@Inject
class RegionsController(
    private val regionRepository: RegionRepository,
    private val currentLocationProvider: CurrentLocationProvider,
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
     * Regions UI state.
     */
    data class RegionsState(
        val allRegions: List<Region> = emptyList(),
        val regions: List<Region> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val searchQuery: String = "",
        val sortOption: RegionSortOption = RegionSortOption.NAME,
        val currentLocation: Coordinate? = null
    )

    private val _state = MutableStateFlow(RegionsState())
    val state: StateFlow<RegionsState> = _state.asStateFlow()

    init {
        // Load regions on initialization
        loadRegions()
    }

    /**
     * Filter and sort regions based on current state.
     */
    private fun filterAndSortRegions(
        allRegions: List<Region>,
        query: String,
        sortOption: RegionSortOption,
        currentLocation: Coordinate?
    ): List<Region> {
        // Filter by search query
        val filtered =
            if (query.isBlank()) {
                allRegions
            } else {
                val searchTerm = query.trim().lowercase()
                allRegions.filter { region ->
                    region.name.lowercase().contains(searchTerm) ||
                        region.description?.lowercase()?.contains(searchTerm) == true
                }
            }

        // Sort
        return when (sortOption) {
            RegionSortOption.NAME -> filtered.sortedBy { it.name.lowercase() }
            RegionSortOption.DISTANCE -> {
                if (currentLocation != null) {
                    filtered.sortedBy { region ->
                        calculateDistance(
                            currentLocation.latitude,
                            currentLocation.longitude,
                            region.latitude,
                            region.longitude
                        )
                    }
                } else {
                    filtered.sortedBy { it.name.lowercase() }
                }
            }
            RegionSortOption.MOST_VISITED -> filtered.sortedByDescending { it.enterCount }
            RegionSortOption.LAST_ENTERED -> filtered.sortedByDescending { it.lastEnterTime }
        }
    }

    /**
     * Calculate distance between two coordinates in meters using Haversine formula.
     */
    private fun calculateDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val earthRadiusKm = 6371.0
        val dLat = (lat2 - lat1).toRadians()
        val dLon = (lon2 - lon1).toRadians()
        val a =
            sin(dLat / 2) * sin(dLat / 2) +
                cos(lat1.toRadians()) * cos(lat2.toRadians()) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadiusKm * c * 1000 // Convert to meters
    }

    /**
     * Load all regions for the user.
     */
    fun loadRegions() {
        logger.debug { "Loading regions for user $userId" }

        _state.update { it.copy(isLoading = true, error = null) }

        controllerScope.launch {
            try {
                regionRepository.getAllRegions(userId).collect { allRegions ->
                    logger.info { "Loaded ${allRegions.size} regions" }

                    val currentState = _state.value
                    val filteredRegions =
                        filterAndSortRegions(
                            allRegions,
                            currentState.searchQuery,
                            currentState.sortOption,
                            currentState.currentLocation
                        )

                    _state.update {
                        it.copy(
                            allRegions = allRegions,
                            regions = filteredRegions,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                logger.error(e) { "Failed to load regions" }
                _state.update {
                    it.copy(
                        error = e.message ?: "Failed to load regions",
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * Get a specific region by ID.
     */
    suspend fun getRegionById(regionId: String): Region? =
        try {
            regionRepository.getRegionById(regionId)
        } catch (e: Exception) {
            logger.error(e) { "Failed to get region $regionId" }
            null
        }

    /**
     * Create a new region.
     */
    @OptIn(ExperimentalUuidApi::class)
    fun createRegion(
        name: String,
        description: String?,
        latitude: Double,
        longitude: Double,
        radiusMeters: Int,
        notificationsEnabled: Boolean = true
    ) {
        logger.info { "Creating new region: $name" }

        controllerScope.launch {
            try {
                val now = Clock.System.now()
                val region =
                    Region(
                        id = Uuid.random().toString(),
                        userId = userId,
                        name = name,
                        description = description,
                        latitude = latitude,
                        longitude = longitude,
                        radiusMeters = radiusMeters,
                        notificationsEnabled = notificationsEnabled,
                        createdAt = now,
                        updatedAt = now,
                        enterCount = 0,
                        lastEnterTime = null,
                        lastExitTime = null
                    )

                regionRepository.insertRegion(region)
                logger.info { "Created region: ${region.id}" }

                // Reload regions
                loadRegions()
            } catch (e: Exception) {
                logger.error(e) { "Failed to create region" }
                _state.update {
                    it.copy(error = e.message ?: "Failed to create region")
                }
            }
        }
    }

    /**
     * Update an existing region.
     */
    fun updateRegion(
        regionId: String,
        name: String,
        description: String?,
        latitude: Double,
        longitude: Double,
        radiusMeters: Int,
        notificationsEnabled: Boolean
    ) {
        logger.info { "Updating region: $regionId" }

        controllerScope.launch {
            try {
                val existingRegion = regionRepository.getRegionById(regionId)
                if (existingRegion != null) {
                    val updatedRegion =
                        existingRegion.copy(
                            name = name,
                            description = description,
                            latitude = latitude,
                            longitude = longitude,
                            radiusMeters = radiusMeters,
                            notificationsEnabled = notificationsEnabled,
                            updatedAt = Clock.System.now()
                        )

                    regionRepository.updateRegion(updatedRegion)
                    logger.info { "Updated region: $regionId" }

                    // Reload regions
                    loadRegions()
                } else {
                    logger.warn { "Region not found: $regionId" }
                    _state.update {
                        it.copy(error = "Region not found")
                    }
                }
            } catch (e: Exception) {
                logger.error(e) { "Failed to update region" }
                _state.update {
                    it.copy(error = e.message ?: "Failed to update region")
                }
            }
        }
    }

    /**
     * Delete a region.
     */
    fun deleteRegion(regionId: String) {
        logger.info { "Deleting region: $regionId" }

        controllerScope.launch {
            try {
                regionRepository.deleteRegion(regionId)
                logger.info { "Deleted region: $regionId" }

                // Update local state immediately
                _state.update { state ->
                    val updatedAllRegions = state.allRegions.filter { it.id != regionId }
                    val updatedRegions = state.regions.filter { it.id != regionId }
                    state.copy(
                        allRegions = updatedAllRegions,
                        regions = updatedRegions
                    )
                }
            } catch (e: Exception) {
                logger.error(e) { "Failed to delete region" }
                _state.update {
                    it.copy(error = e.message ?: "Failed to delete region")
                }
            }
        }
    }

    /**
     * Search regions by query.
     */
    fun search(query: String) {
        logger.debug { "Searching regions with query: $query" }

        _state.update { state ->
            val filteredRegions =
                filterAndSortRegions(
                    state.allRegions,
                    query,
                    state.sortOption,
                    state.currentLocation
                )
            state.copy(
                searchQuery = query,
                regions = filteredRegions
            )
        }
    }

    /**
     * Clear search query.
     */
    fun clearSearch() {
        logger.debug { "Clearing search" }

        _state.update { state ->
            val filteredRegions =
                filterAndSortRegions(
                    state.allRegions,
                    "",
                    state.sortOption,
                    state.currentLocation
                )
            state.copy(
                searchQuery = "",
                regions = filteredRegions
            )
        }
    }

    /**
     * Set sort option.
     */
    fun setSortOption(option: RegionSortOption) {
        logger.debug { "Setting sort option: $option" }

        _state.update { state ->
            val filteredRegions =
                filterAndSortRegions(
                    state.allRegions,
                    state.searchQuery,
                    option,
                    state.currentLocation
                )
            state.copy(
                sortOption = option,
                regions = filteredRegions
            )
        }
    }

    /**
     * Set current location for distance-based sorting.
     */
    fun setCurrentLocation(location: Coordinate?) {
        logger.debug { "Setting current location: $location" }

        _state.update { state ->
            val filteredRegions =
                filterAndSortRegions(
                    state.allRegions,
                    state.searchQuery,
                    state.sortOption,
                    location
                )
            state.copy(
                currentLocation = location,
                regions = filteredRegions
            )
        }
    }

    /**
     * Update current location from location provider.
     * Useful for triggering distance-based sorting.
     */
    fun updateCurrentLocation() {
        logger.debug { "Updating current location from provider" }

        controllerScope.launch {
            currentLocationProvider.getCurrentLocation()
                .onSuccess { locationData ->
                    logger.debug { "Got current location: ${locationData.coordinate}" }
                    setCurrentLocation(locationData.coordinate)
                }
                .onFailure { error ->
                    logger.warn { "Failed to get current location: ${error.message}" }
                    // Don't show error to user, just log it
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
     */
    override fun cleanup() {
        logger.info { "Cleaning up RegionsController" }
        controllerScope.cancel()
        logger.debug { "RegionsController cleanup complete" }
    }

    private fun Double.toRadians(): Double = this * PI / 180.0
}
