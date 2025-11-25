package com.po4yka.trailglass.feature.map

import com.po4yka.trailglass.domain.model.EnhancedMapDisplayData
import com.po4yka.trailglass.domain.model.EnhancedMapMarker
import com.po4yka.trailglass.domain.model.MapRoute
import com.po4yka.trailglass.domain.model.MapVisualizationMode
import com.po4yka.trailglass.domain.model.MarkerCluster
import com.po4yka.trailglass.domain.model.PlaceCategory
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
import kotlinx.datetime.Instant
import me.tatarka.inject.annotations.Inject

/**
 * Enhanced map controller with clustering and heatmap support.
 *
 * IMPORTANT: Call [cleanup] when this controller is no longer needed to prevent memory leaks.
 */
@Inject
class EnhancedMapController(
    private val getMapDataUseCase: GetMapDataUseCase,
    private val markerClusterer: MarkerClusterer,
    private val heatmapGenerator: HeatmapGenerator,
    coroutineScope: CoroutineScope
) : Lifecycle {
    private val logger = logger()

    // Create a child scope that can be cancelled independently
    private val controllerScope =
        CoroutineScope(
            coroutineScope.coroutineContext + SupervisorJob()
        )

    /** Enhanced map UI state. */
    data class EnhancedMapState(
        val mapData: EnhancedMapDisplayData = EnhancedMapDisplayData(),
        val visualizationMode: MapVisualizationMode = MapVisualizationMode.HYBRID,
        val selectedMarker: EnhancedMapMarker? = null,
        val selectedRoute: MapRoute? = null,
        val selectedCluster: MarkerCluster? = null,
        val zoomLevel: Float = 15f,
        val clusteringEnabled: Boolean = true,
        val heatmapEnabled: Boolean = false,
        val isLoading: Boolean = false,
        val error: String? = null
    )

    private val _state = MutableStateFlow(EnhancedMapState())
    val state: StateFlow<EnhancedMapState> = _state.asStateFlow()

    /** Load enhanced map data with clustering and heatmap. */
    fun loadMapData(
        userId: String,
        startTime: Instant,
        endTime: Instant,
        enableClustering: Boolean = true,
        enableHeatmap: Boolean = false
    ) {
        logger.debug { "Loading enhanced map data" }

        _state.update { it.copy(isLoading = true, error = null) }

        controllerScope.launch {
            val result = getMapDataUseCase.execute(userId, startTime, endTime)

            result
                .onSuccess { mapData ->
                    // Convert to enhanced markers
                    val enhancedMarkers =
                        mapData.markers.map { marker ->
                            // Would need to load additional data like category, visit count
                            // For now, use placeholder values
                            EnhancedMapMarker(
                                id = marker.id,
                                coordinate = marker.coordinate,
                                title = marker.title,
                                snippet = marker.snippet,
                                placeVisitId = marker.placeVisitId,
                                category = PlaceCategory.OTHER, // Would load from place visit
                                isFavorite = false,
                                visitCount = 1
                            )
                        }

                    // Apply clustering if enabled
                    val (clusters, singleMarkers) =
                        if (enableClustering) {
                            markerClusterer.cluster(
                                markers = enhancedMarkers,
                                zoomLevel = _state.value.zoomLevel
                            )
                        } else {
                            Pair(emptyList(), enhancedMarkers)
                        }

                    // Generate heatmap if enabled
                    val heatmap =
                        if (enableHeatmap) {
                            heatmapGenerator.generate(
                                markers = enhancedMarkers,
                                intensityMode = HeatmapGenerator.IntensityMode.VISIT_COUNT
                            )
                        } else {
                            null
                        }

                    // Update state
                    _state.update {
                        it.copy(
                            mapData =
                                EnhancedMapDisplayData(
                                    markers = singleMarkers,
                                    clusters = clusters,
                                    routes = mapData.routes,
                                    heatmapData = heatmap,
                                    region = mapData.region,
                                    clusteringEnabled = enableClustering,
                                    heatmapEnabled = enableHeatmap
                                ),
                            clusteringEnabled = enableClustering,
                            heatmapEnabled = enableHeatmap,
                            isLoading = false
                        )
                    }

                    logger.info {
                        "Loaded enhanced map data: ${enhancedMarkers.size} markers, " +
                            "${clusters.size} clusters, ${mapData.routes.size} routes"
                    }
                }.onError { error ->
                    logger.error { "Failed to load enhanced map data: ${error.getTechnicalDetails()}" }
                    _state.update {
                        it.copy(
                            error = error.getUserFriendlyMessage(),
                            isLoading = false
                        )
                    }
                }
        }
    }

    /** Update zoom level and re-cluster markers. */
    fun updateZoom(zoomLevel: Float) {
        val currentState = _state.value
        _state.update { it.copy(zoomLevel = zoomLevel) }

        // Re-cluster if clustering is enabled
        if (currentState.clusteringEnabled) {
            controllerScope.launch {
                val allMarkers =
                    currentState.mapData.markers +
                        currentState.mapData.clusters.flatMap { it.markers }

                val (clusters, singleMarkers) =
                    markerClusterer.cluster(
                        markers = allMarkers,
                        zoomLevel = zoomLevel
                    )

                _state.update {
                    it.copy(
                        mapData =
                            it.mapData.copy(
                                markers = singleMarkers,
                                clusters = clusters
                            )
                    )
                }
            }
        }
    }

    /** Toggle clustering on/off. */
    fun toggleClustering() {
        val newValue = !_state.value.clusteringEnabled
        _state.update { it.copy(clusteringEnabled = newValue) }

        if (newValue) {
            // Re-cluster
            updateZoom(_state.value.zoomLevel)
        } else {
            // Flatten all clusters back to markers
            val allMarkers =
                _state.value.mapData.markers +
                    _state.value.mapData.clusters
                        .flatMap { it.markers }

            _state.update {
                it.copy(
                    mapData =
                        it.mapData.copy(
                            markers = allMarkers,
                            clusters = emptyList()
                        )
                )
            }
        }
    }

    /** Toggle heatmap on/off. */
    fun toggleHeatmap() {
        val newValue = !_state.value.heatmapEnabled

        _state.update { it.copy(heatmapEnabled = newValue) }

        if (newValue) {
            // Generate heatmap
            controllerScope.launch {
                val allMarkers =
                    _state.value.mapData.markers +
                        _state.value.mapData.clusters
                            .flatMap { it.markers }

                val heatmap =
                    heatmapGenerator.generate(
                        markers = allMarkers,
                        intensityMode = HeatmapGenerator.IntensityMode.VISIT_COUNT
                    )

                _state.update {
                    it.copy(
                        mapData = it.mapData.copy(heatmapData = heatmap)
                    )
                }
            }
        } else {
            // Remove heatmap
            _state.update {
                it.copy(
                    mapData = it.mapData.copy(heatmapData = null)
                )
            }
        }
    }

    /** Change visualization mode. */
    fun setVisualizationMode(mode: MapVisualizationMode) {
        _state.update { it.copy(visualizationMode = mode) }

        when (mode) {
            MapVisualizationMode.MARKERS -> {
                // Show only markers, no clustering or heatmap
                _state.update {
                    it.copy(
                        clusteringEnabled = false,
                        heatmapEnabled = false
                    )
                }
            }

            MapVisualizationMode.CLUSTERS -> {
                // Enable clustering, disable heatmap
                _state.update {
                    it.copy(
                        clusteringEnabled = true,
                        heatmapEnabled = false
                    )
                }
                toggleClustering()
            }

            MapVisualizationMode.HEATMAP -> {
                // Enable heatmap, disable markers
                _state.update {
                    it.copy(
                        clusteringEnabled = false,
                        heatmapEnabled = true
                    )
                }
                toggleHeatmap()
            }

            MapVisualizationMode.HYBRID -> {
                // Enable both clustering and routes
                _state.update {
                    it.copy(
                        clusteringEnabled = true,
                        heatmapEnabled = false
                    )
                }
                toggleClustering()
            }
        }
    }

    /** Select a marker. */
    fun selectMarker(marker: EnhancedMapMarker) {
        logger.debug { "Marker selected: ${marker.title}" }
        _state.update {
            it.copy(
                selectedMarker = marker,
                selectedCluster = null,
                selectedRoute = null
            )
        }
    }

    /** Select a cluster (expands to show markers). */
    fun selectCluster(cluster: MarkerCluster) {
        logger.debug { "Cluster selected: ${cluster.count} markers" }
        _state.update {
            it.copy(
                selectedCluster = cluster,
                selectedMarker = null,
                selectedRoute = null
            )
        }
    }

    /** Select a route. */
    fun selectRoute(route: MapRoute) {
        logger.debug { "Route selected: ${route.transportType}" }
        _state.update {
            it.copy(
                selectedRoute = route,
                selectedMarker = null,
                selectedCluster = null
            )
        }
    }

    /** Clear all selections. */
    fun clearSelection() {
        _state.update {
            it.copy(
                selectedMarker = null,
                selectedCluster = null,
                selectedRoute = null
            )
        }
    }

    /** Clear error. */
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
        logger.info { "Cleaning up EnhancedMapController" }
        controllerScope.cancel()
        logger.debug { "EnhancedMapController cleanup complete" }
    }
}
