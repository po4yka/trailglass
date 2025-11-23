package com.po4yka.trailglass.feature.route

import com.po4yka.trailglass.domain.model.Coordinate
import com.po4yka.trailglass.domain.model.PhotoMarker
import com.po4yka.trailglass.domain.model.TripRoute
import com.po4yka.trailglass.feature.common.Lifecycle
import com.po4yka.trailglass.feature.route.export.ExportFormat
import com.po4yka.trailglass.feature.route.export.ExportResult
import com.po4yka.trailglass.feature.route.export.ExportRouteUseCase
import com.po4yka.trailglass.logging.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

/** Map style options for route visualization. */
enum class MapStyle {
    STANDARD,
    SATELLITE,
    TERRAIN,
    DARK
}

/**
 * Controller for the Route View screen. Manages route data loading, map state, and replay controls.
 *
 * IMPORTANT: Call [cleanup] when this controller is no longer needed to prevent memory leaks.
 */
@Inject
class RouteViewController(
    private val getTripRouteUseCase: GetTripRouteUseCase,
    private val exportRouteUseCase: ExportRouteUseCase,
    coroutineScope: CoroutineScope
) : Lifecycle {
    private val logger = logger()

    // Create a child scope that can be cancelled independently
    private val controllerScope =
        CoroutineScope(
            coroutineScope.coroutineContext + SupervisorJob()
        )

    /** State for Route View screen. */
    data class RouteViewState(
        val tripRoute: TripRoute? = null,
        val isLoading: Boolean = false,
        val error: String? = null,
        // Map state
        val mapStyle: MapStyle = MapStyle.STANDARD,
        val shouldRecenterCamera: Boolean = false,
        val selectedPhotoMarker: PhotoMarker? = null,
        // UI state
        val showMapStyleSelector: Boolean = false,
        val showStatistics: Boolean = false,
        // Export state
        val isExporting: Boolean = false,
        val exportResult: ExportResult? = null
    )

    private val _state = MutableStateFlow(RouteViewState())
    val state: StateFlow<RouteViewState> = _state.asStateFlow()

    /** Load route data for a trip. */
    fun loadRoute(tripId: String) {
        controllerScope.launch {
            logger.info { "Loading route for trip $tripId" }
            _state.value = _state.value.copy(isLoading = true, error = null)

            getTripRouteUseCase
                .execute(tripId)
                .onSuccess { tripRoute ->
                    logger.info { "Route loaded successfully: ${tripRoute.fullPath.size} points" }
                    _state.value =
                        _state.value.copy(
                            tripRoute = tripRoute,
                            isLoading = false,
                            shouldRecenterCamera = true // Trigger initial camera fit
                        )
                }.onError { error ->
                    logger.error { "Failed to load route for trip $tripId - ${error.getTechnicalDetails()}" }
                    _state.value =
                        _state.value.copy(
                            isLoading = false,
                            error = error.getUserFriendlyMessage()
                        )
                }
        }
    }

    /** Change map style. */
    fun setMapStyle(style: MapStyle) {
        logger.debug { "Changing map style to $style" }
        _state.value = _state.value.copy(mapStyle = style)
    }

    /** Show/hide map style selector. */
    fun toggleMapStyleSelector() {
        _state.value =
            _state.value.copy(
                showMapStyleSelector = !_state.value.showMapStyleSelector
            )
    }

    /** Show/hide statistics screen. */
    fun toggleStatistics() {
        _state.value =
            _state.value.copy(
                showStatistics = !_state.value.showStatistics
            )
    }

    /** Recenter camera to fit entire route. */
    fun recenterCamera() {
        logger.debug { "Recenter camera requested" }
        _state.value = _state.value.copy(shouldRecenterCamera = true)
    }

    /** Acknowledge camera recentering (called after UI applies camera move). */
    fun acknowledgeRecenter() {
        _state.value = _state.value.copy(shouldRecenterCamera = false)
    }

    /** Select a photo marker on the map. */
    fun selectPhotoMarker(marker: PhotoMarker?) {
        logger.debug { "Photo marker selected: ${marker?.photoId}" }
        _state.value = _state.value.copy(selectedPhotoMarker = marker)
    }

    /** Clear error state. */
    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    /** Get route bounds for camera positioning. */
    fun getRouteBounds(): Pair<Coordinate, Coordinate>? {
        val bounds = _state.value.tripRoute?.bounds ?: return null
        return Pair(
            Coordinate(bounds.minLatitude, bounds.minLongitude),
            Coordinate(bounds.maxLatitude, bounds.maxLongitude)
        )
    }

    /** Export route to specified format. */
    fun exportRoute(
        tripName: String,
        format: ExportFormat
    ) {
        val route = _state.value.tripRoute ?: return

        controllerScope.launch {
            logger.info { "Exporting route to $format" }
            _state.value = _state.value.copy(isExporting = true)

            exportRouteUseCase
                .execute(route, tripName, format)
                .onSuccess { result ->
                    logger.info { "Export successful: ${result.fileName}" }
                    _state.value =
                        _state.value.copy(
                            isExporting = false,
                            exportResult = result
                        )
                }.onFailure { error ->
                    logger.error(error) { "Export failed" }
                    _state.value =
                        _state.value.copy(
                            isExporting = false,
                            error = "Export failed: ${error.message}"
                        )
                }
        }
    }

    /** Clear export result after handling. */
    fun clearExportResult() {
        _state.value = _state.value.copy(exportResult = null)
    }

    /**
     * Cleanup method to release resources and prevent memory leaks. MUST be called when this controller is no longer
     * needed.
     *
     * Cancels all running coroutines including flow collectors.
     */
    override fun cleanup() {
        logger.info { "Cleaning up RouteViewController" }
        controllerScope.cancel()
        logger.debug { "RouteViewController cleanup complete" }
    }
}
