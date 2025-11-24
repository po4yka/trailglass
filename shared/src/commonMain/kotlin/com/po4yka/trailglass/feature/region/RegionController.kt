package com.po4yka.trailglass.feature.region

import com.po4yka.trailglass.domain.model.Region
import com.po4yka.trailglass.domain.usecase.region.CheckIfInRegionUseCase
import com.po4yka.trailglass.domain.usecase.region.CreateRegionUseCase
import com.po4yka.trailglass.domain.usecase.region.DeleteRegionUseCase
import com.po4yka.trailglass.domain.usecase.region.GetAllRegionsUseCase
import com.po4yka.trailglass.domain.usecase.region.UpdateRegionUseCase
import com.po4yka.trailglass.feature.common.Lifecycle
import com.po4yka.trailglass.logging.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

/**
 * Controller for region/geofencing feature. Manages region state and user actions.
 *
 * IMPORTANT: Call [cleanup] when this controller is no longer needed to prevent memory leaks.
 */
@Inject
class RegionController(
    private val getAllRegionsUseCase: GetAllRegionsUseCase,
    private val createRegionUseCase: CreateRegionUseCase,
    private val updateRegionUseCase: UpdateRegionUseCase,
    private val deleteRegionUseCase: DeleteRegionUseCase,
    private val checkIfInRegionUseCase: CheckIfInRegionUseCase,
    coroutineScope: CoroutineScope,
    private val userId: String
) : Lifecycle {
    private val logger = logger()

    // Create a child scope that can be cancelled independently
    private val controllerScope = CoroutineScope(
        coroutineScope.coroutineContext + SupervisorJob()
    )

    /** Region UI state. */
    data class RegionState(
        val regions: List<Region> = emptyList(),
        val selectedRegion: Region? = null,
        val isLoading: Boolean = false,
        val error: String? = null
    )

    private val _state = MutableStateFlow(RegionState())
    val state: StateFlow<RegionState> = _state.asStateFlow()

    init {
        // Start observing regions on initialization
        observeRegions()
    }

    /**
     * Observe regions for the current user.
     * This will automatically update the state when regions change.
     */
    private fun observeRegions() {
        logger.debug { "Starting to observe regions for user $userId" }

        controllerScope.launch {
            getAllRegionsUseCase.execute(userId)
                .catch { e ->
                    logger.error(e) { "Error observing regions" }
                    _state.update { it.copy(error = e.message ?: "Failed to load regions") }
                }
                .collect { regions ->
                    logger.debug { "Received ${regions.size} regions" }
                    _state.update { it.copy(regions = regions, isLoading = false) }
                }
        }
    }

    /**
     * Create a new region.
     *
     * @param name Region name
     * @param description Optional description
     * @param lat Center latitude
     * @param lon Center longitude
     * @param radius Radius in meters
     * @param notificationsEnabled Whether notifications are enabled
     */
    suspend fun createRegion(
        name: String,
        description: String?,
        lat: Double,
        lon: Double,
        radius: Int,
        notificationsEnabled: Boolean
    ) {
        logger.debug { "Creating region: $name" }

        _state.update { it.copy(isLoading = true, error = null) }

        val id = generateRegionId()
        val result = createRegionUseCase.execute(
            id = id,
            userId = userId,
            name = name,
            description = description,
            lat = lat,
            lon = lon,
            radius = radius,
            notificationsEnabled = notificationsEnabled
        )

        result.fold(
            onSuccess = { region ->
                logger.info { "Created region: ${region.name}" }
                _state.update { it.copy(isLoading = false) }
            },
            onFailure = { error ->
                logger.error(error) { "Failed to create region" }
                _state.update {
                    it.copy(
                        error = error.message ?: "Failed to create region",
                        isLoading = false
                    )
                }
            }
        )
    }

    /**
     * Update an existing region.
     *
     * @param region The region to update
     */
    suspend fun updateRegion(region: Region) {
        logger.debug { "Updating region: ${region.name}" }

        _state.update { it.copy(isLoading = true, error = null) }

        val result = updateRegionUseCase.execute(region)

        result.fold(
            onSuccess = { updatedRegion ->
                logger.info { "Updated region: ${updatedRegion.name}" }
                _state.update { it.copy(isLoading = false) }
            },
            onFailure = { error ->
                logger.error(error) { "Failed to update region" }
                _state.update {
                    it.copy(
                        error = error.message ?: "Failed to update region",
                        isLoading = false
                    )
                }
            }
        )
    }

    /**
     * Delete a region.
     *
     * @param regionId The region ID to delete
     */
    suspend fun deleteRegion(regionId: String) {
        logger.debug { "Deleting region: $regionId" }

        _state.update { it.copy(isLoading = true, error = null) }

        val result = deleteRegionUseCase.execute(regionId)

        result.fold(
            onSuccess = {
                logger.info { "Deleted region: $regionId" }
                _state.update { it.copy(isLoading = false, selectedRegion = null) }
            },
            onFailure = { error ->
                logger.error(error) { "Failed to delete region" }
                _state.update {
                    it.copy(
                        error = error.message ?: "Failed to delete region",
                        isLoading = false
                    )
                }
            }
        )
    }

    /**
     * Select a region by ID.
     *
     * @param regionId The region ID to select
     */
    suspend fun selectRegion(regionId: String) {
        logger.debug { "Selecting region: $regionId" }

        val region = _state.value.regions.find { it.id == regionId }
        if (region != null) {
            _state.update { it.copy(selectedRegion = region) }
        } else {
            logger.warn { "Region not found: $regionId" }
        }
    }

    /**
     * Check which regions contain the current location.
     *
     * @param lat Current latitude
     * @param lon Current longitude
     * @return List of regions that contain the location
     */
    suspend fun checkCurrentLocationInRegions(lat: Double, lon: Double): List<Region> {
        logger.debug { "Checking location ($lat, $lon) in regions" }

        return checkIfInRegionUseCase.execute(userId, lat, lon)
    }

    /**
     * Clear error state.
     */
    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    /**
     * Clear selected region.
     */
    fun clearSelection() {
        _state.update { it.copy(selectedRegion = null) }
    }

    /**
     * Cleanup method to release resources and prevent memory leaks.
     * MUST be called when this controller is no longer needed.
     *
     * Cancels all running coroutines including flow collectors.
     */
    override fun cleanup() {
        logger.info { "Cleaning up RegionController" }
        controllerScope.cancel()
        logger.debug { "RegionController cleanup complete" }
    }

    private fun generateRegionId(): String {
        return "region_${System.currentTimeMillis()}_${(0..999).random()}"
    }
}
