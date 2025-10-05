package com.po4yka.trailglass.feature.map

import com.po4yka.trailglass.domain.model.*
import com.po4yka.trailglass.logging.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant

/**
 * Controller for map visualization.
 */
class MapController(
    private val getMapDataUseCase: GetMapDataUseCase,
    private val coroutineScope: CoroutineScope,
    private val userId: String
) {

    private val logger = logger()

    /**
     * Map UI state.
     */
    data class MapState(
        val mapData: MapDisplayData = MapDisplayData(),
        val cameraPosition: CameraPosition? = null,
        val selectedMarker: MapMarker? = null,
        val selectedRoute: MapRoute? = null,
        val isLoading: Boolean = false,
        val error: String? = null
    )

    private val _state = MutableStateFlow(MapState())
    val state: StateFlow<MapState> = _state.asStateFlow()

    /**
     * Load map data for a time range.
     */
    fun loadMapData(startTime: Instant, endTime: Instant) {
        logger.debug { "Loading map data from $startTime to $endTime" }

        _state.update { it.copy(isLoading = true, error = null) }

        coroutineScope.launch {
            try {
                val mapData = getMapDataUseCase.execute(userId, startTime, endTime)

                // Set camera position to region center if available
                val cameraPosition = mapData.region?.let { region ->
                    CameraPosition(
                        target = region.center,
                        zoom = calculateZoomLevel(region)
                    )
                }

                _state.update {
                    it.copy(
                        mapData = mapData,
                        cameraPosition = cameraPosition,
                        isLoading = false
                    )
                }

                logger.info {
                    "Loaded map data: ${mapData.markers.size} markers, " +
                    "${mapData.routes.size} routes"
                }
            } catch (e: Exception) {
                logger.error(e) { "Failed to load map data" }
                _state.update { it.copy(error = e.message ?: "Unknown error", isLoading = false) }
            }
        }
    }

    /**
     * Select a marker on the map.
     */
    fun selectMarker(marker: MapMarker) {
        logger.debug { "Marker selected: ${marker.title}" }
        _state.update { it.copy(selectedMarker = marker) }
    }

    /**
     * Deselect the current marker.
     */
    fun deselectMarker() {
        _state.update { it.copy(selectedMarker = null) }
    }

    /**
     * Select a route on the map.
     */
    fun selectRoute(route: MapRoute) {
        logger.debug { "Route selected: ${route.transportType}" }
        _state.update { it.copy(selectedRoute = route) }
    }

    /**
     * Deselect the current route.
     */
    fun deselectRoute() {
        _state.update { it.copy(selectedRoute = null) }
    }

    /**
     * Update camera position.
     */
    fun updateCameraPosition(position: CameraPosition) {
        _state.update { it.copy(cameraPosition = position) }
    }

    /**
     * Move camera to coordinate.
     */
    fun moveCameraTo(coordinate: Coordinate, zoom: Float = 15f) {
        val position = CameraPosition(target = coordinate, zoom = zoom)
        updateCameraPosition(position)
    }

    /**
     * Fit map to show all markers and routes.
     */
    fun fitToData() {
        val region = _state.value.mapData.region
        if (region != null) {
            val position = CameraPosition(
                target = region.center,
                zoom = calculateZoomLevel(region)
            )
            updateCameraPosition(position)
        }
    }

    /**
     * Refresh map data.
     */
    fun refresh() {
        // Re-load with current time range (would need to store it)
        // For now, just trigger a refresh signal
        logger.debug { "Map refresh requested" }
    }

    /**
     * Clear error state.
     */
    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    /**
     * Calculate appropriate zoom level for a region.
     * Higher latitudeDelta = lower zoom.
     */
    private fun calculateZoomLevel(region: MapRegion): Float {
        // Rough approximation: zoom levels from 1 (world) to 20 (building)
        val delta = maxOf(region.latitudeDelta, region.longitudeDelta)

        return when {
            delta > 10.0 -> 5f    // Country level
            delta > 5.0 -> 7f     // Large region
            delta > 1.0 -> 9f     // City level
            delta > 0.5 -> 11f    // District
            delta > 0.1 -> 13f    // Neighborhood
            delta > 0.05 -> 15f   // Street level
            else -> 17f           // Building level
        }
    }
}
