package com.po4yka.trailglass.feature.map

import com.po4yka.trailglass.domain.model.CameraMove
import com.po4yka.trailglass.domain.model.CameraPosition
import com.po4yka.trailglass.domain.model.Coordinate
import com.po4yka.trailglass.domain.model.MapEvent
import com.po4yka.trailglass.domain.model.MapEventSink
import com.po4yka.trailglass.domain.model.MapMarker
import com.po4yka.trailglass.domain.model.MapRoute
import com.po4yka.trailglass.domain.permission.PermissionResult
import com.po4yka.trailglass.domain.permission.PermissionType
import com.po4yka.trailglass.domain.service.LocationService
import com.po4yka.trailglass.feature.common.Lifecycle
import com.po4yka.trailglass.feature.permission.PermissionFlowController
import com.po4yka.trailglass.logging.logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Instant
import me.tatarka.inject.annotations.Inject

/**
 * Controller for map visualization.
 *
 * Implements [MapEventSink] to handle events from the map UI in a decoupled, testable manner.
 *
 * IMPORTANT: Call [cleanup] when this controller is no longer needed to prevent memory leaks.
 */
@Inject
class MapController(
    private val getMapDataUseCase: GetMapDataUseCase,
    private val locationService: LocationService,
    private val permissionFlow: PermissionFlowController,
    coroutineScope: CoroutineScope,
    private val userId: String
) : MapEventSink,
    Lifecycle {
    private val logger = logger()

    // Create a child scope that can be cancelled independently
    private val controllerScope =
        CoroutineScope(
            coroutineScope.coroutineContext + SupervisorJob()
        )

    // Mutex protects locationTrackingJob access for thread safety during concurrent toggle calls
    private val locationJobMutex = Mutex()
    private var locationTrackingJob: Job? = null
    private var pendingFollowModeParams: FollowModeParams? = null

    private val _state = MutableStateFlow(MapState())
    val state: StateFlow<MapState> = _state.asStateFlow()

    init {
        // Observe permission results for follow mode
        controllerScope.launch {
            permissionFlow.state.collect { permState ->
                when (permState.lastResult) {
                    is PermissionResult.Granted -> {
                        logger.info { "Location permission granted for follow mode" }
                        val params = pendingFollowModeParams
                        if (params != null) {
                            pendingFollowModeParams = null
                            _state.update { it.copy(hasLocationPermission = true) }
                            // Enable follow mode with pending params
                            enableFollowModeInternal(params.zoom, params.tilt, params.bearing)
                        } else {
                            _state.update { it.copy(hasLocationPermission = true) }
                        }
                    }

                    is PermissionResult.Denied,
                    is PermissionResult.PermanentlyDenied -> {
                        logger.warn { "Location permission denied for follow mode" }
                        pendingFollowModeParams = null
                        _state.update {
                            it.copy(
                                hasLocationPermission = false,
                                error = "Location permission is required for follow mode"
                            )
                        }
                    }

                    is PermissionResult.Cancelled -> {
                        logger.info { "Location permission request cancelled" }
                        pendingFollowModeParams = null
                    }

                    is PermissionResult.Error -> {
                        logger.error { "Permission error: ${permState.lastResult.message}" }
                        pendingFollowModeParams = null
                        _state.update { it.copy(error = permState.lastResult.message) }
                    }

                    null -> {
                        // No result yet
                    }
                }
            }
        }

        // Check permissions on init
        checkPermissions()
    }

    /** Load map data for a time range. */
    fun loadMapData(
        startTime: Instant,
        endTime: Instant
    ) {
        logger.debug { "Loading map data from $startTime to $endTime" }

        _state.update { it.copy(isLoading = true, error = null) }

        controllerScope.launch {
            val result = getMapDataUseCase.execute(userId, startTime, endTime)

            result
                .onSuccess { mapData ->
                    // Set camera to region center with smooth animation if available
                    val cameraMove =
                        mapData.region?.let { region ->
                            CameraMove.Ease(
                                position =
                                    CameraPosition(
                                        target = region.center,
                                        zoom = MapOperations.calculateZoomLevel(region)
                                    ),
                                durationMs = 1500
                            )
                        }

                    _state.update {
                        it.copy(
                            mapData = mapData,
                            cameraMove = cameraMove,
                            isLoading = false
                        )
                    }

                    logger.info {
                        "Loaded map data: ${mapData.markers.size} markers, " +
                            "${mapData.routes.size} routes"
                    }
                }.onError { error ->
                    logger.error { "Failed to load map data: ${error.getTechnicalDetails()}" }
                    _state.update {
                        it.copy(
                            error = error.getUserFriendlyMessage(),
                            isLoading = false
                        )
                    }
                }
        }
    }

    /** Select a marker on the map. */
    fun selectMarker(marker: MapMarker) {
        logger.debug { "Marker selected: ${marker.title}" }
        _state.update { it.copy(selectedMarker = marker) }
    }

    /** Deselect the current marker. */
    fun deselectMarker() {
        _state.update { it.copy(selectedMarker = null) }
    }

    /** Select a route on the map. */
    fun selectRoute(route: MapRoute) {
        logger.debug { "Route selected: ${route.transportType}" }
        _state.update { it.copy(selectedRoute = route) }
    }

    /** Deselect the current route. */
    fun deselectRoute() {
        _state.update { it.copy(selectedRoute = null) }
    }

    /**
     * Apply a camera movement command.
     *
     * @param cameraMove The camera movement command to apply
     */
    fun applyCameraMove(cameraMove: CameraMove) {
        _state.update { it.copy(cameraMove = cameraMove) }
    }

    /**
     * Move camera to coordinate with smooth animation.
     *
     * @param coordinate Target coordinate
     * @param zoom Zoom level (default: 15f - street level)
     * @param animated If true, uses smooth easing animation, otherwise instant (default: true)
     * @param durationMs Animation duration in milliseconds (default: 1000ms)
     */
    fun moveCameraTo(
        coordinate: Coordinate,
        zoom: Float = 15f,
        animated: Boolean = true,
        durationMs: Int = 1000
    ) {
        val position = CameraPosition(target = coordinate, zoom = zoom)
        val cameraMove =
            if (animated) {
                CameraMove.Ease(position, durationMs)
            } else {
                CameraMove.Instant(position)
            }
        applyCameraMove(cameraMove)
    }

    /**
     * Fit map to show all markers and routes with smooth animation.
     *
     * @param animated If true, uses smooth easing animation, otherwise instant (default: true)
     * @param durationMs Animation duration in milliseconds (default: 1200ms)
     */
    fun fitToData(
        animated: Boolean = true,
        durationMs: Int = 1200
    ) {
        val region = _state.value.mapData.region
        if (region != null) {
            val position =
                CameraPosition(
                    target = region.center,
                    zoom = MapOperations.calculateZoomLevel(region)
                )
            val cameraMove =
                if (animated) {
                    CameraMove.Ease(position, durationMs)
                } else {
                    CameraMove.Instant(position)
                }
            applyCameraMove(cameraMove)
        }
    }

    /** Refresh map data. */
    fun refresh() {
        // Re-load with current time range (would need to store it)
        // For now, just trigger a refresh signal
        logger.debug { "Map refresh requested" }
    }

    /** Clear error state. */
    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    /**
     * Toggle follow mode on/off.
     *
     * When enabled, the camera will track the user's current location. When disabled, location tracking stops. This
     * will request location permission if not already granted.
     *
     * @param zoom Zoom level to use when following (default: 15f - street level)
     * @param tilt Camera tilt angle in degrees (default: 45f - perspective view)
     * @param bearing Camera bearing in degrees (default: 0f - north up)
     */
    suspend fun toggleFollowMode(
        zoom: Float = 15f,
        tilt: Float = 45f,
        bearing: Float = 0f
    ) {
        val isCurrentlyEnabled = _state.value.isFollowModeEnabled

        if (isCurrentlyEnabled) {
            // Disable follow mode - use mutex for thread-safe job access
            logger.info { "Disabling follow mode" }
            locationJobMutex.withLock {
                locationTrackingJob?.cancel()
                locationTrackingJob = null
            }
            _state.update { it.copy(isFollowModeEnabled = false) }
        } else {
            // Enable follow mode - check permission first
            logger.info { "User requested to enable follow mode" }

            val hasPermission = permissionFlow.isPermissionGranted(PermissionType.LOCATION_FINE)

            if (hasPermission) {
                enableFollowModeInternal(zoom, tilt, bearing)
            } else {
                // Request permission
                logger.info { "Location permission not granted, requesting..." }
                pendingFollowModeParams = FollowModeParams(zoom, tilt, bearing)
                permissionFlow.startPermissionFlow(PermissionType.LOCATION_FINE)
            }
        }
    }

    /** Enable follow mode after permission is confirmed granted. */
    private fun enableFollowModeInternal(
        zoom: Float,
        tilt: Float,
        bearing: Float
    ) {
        logger.info { "Enabling follow mode" }

        controllerScope.launch {
            // Get last known location and move camera there first
            locationService.getLastKnownLocation()?.let { coordinate ->
                val position =
                    CameraPosition(
                        target = coordinate,
                        zoom = zoom,
                        tilt = tilt,
                        bearing = bearing
                    )
                applyCameraMove(CameraMove.Ease(position, durationMs = 800))
            }

            // Start tracking location updates with thread-safe job assignment
            locationJobMutex.withLock {
                // Cancel any existing tracking job before starting new one
                locationTrackingJob?.cancel()
                locationTrackingJob =
                    locationService.locationUpdates
                        .onEach { coordinate ->
                            logger.debug { "Location update: ${coordinate.latitude}, ${coordinate.longitude}" }

                            // Update camera to follow user
                            val position =
                                CameraPosition(
                                    target = coordinate,
                                    zoom = zoom,
                                    tilt = tilt,
                                    bearing = bearing
                                )
                            applyCameraMove(CameraMove.Ease(position, durationMs = 500))
                        }.launchIn(controllerScope)
            }

            _state.update { it.copy(isFollowModeEnabled = true) }
        }
    }

    /**
     * Check if location permission is granted.
     *
     * @return true if permission is granted, false otherwise
     */
    suspend fun hasLocationPermission(): Boolean = permissionFlow.isPermissionGranted(PermissionType.LOCATION_FINE)

    /** Check permissions. */
    fun checkPermissions() {
        controllerScope.launch {
            val hasPermission = permissionFlow.isPermissionGranted(PermissionType.LOCATION_FINE)
            _state.update { it.copy(hasLocationPermission = hasPermission) }
            logger.debug { "Location permission check: $hasPermission" }
        }
    }

    /**
     * Handle map events from the UI layer.
     *
     * This implements the [MapEventSink] interface, providing a clean event-driven API for map interactions.
     */
    override fun send(event: MapEvent) {
        logger.debug { "Map event received: ${event::class.simpleName}" }

        when (event) {
            is MapEvent.MarkerTapped -> {
                // Find and select the marker
                val marker =
                    _state.value.mapData.markers
                        .find { it.id == event.markerId }
                if (marker != null) {
                    selectMarker(marker)
                } else {
                    logger.warn { "Marker not found: ${event.markerId}" }
                }
            }

            is MapEvent.RouteTapped -> {
                // Find and select the route
                val route =
                    _state.value.mapData.routes
                        .find { it.id == event.routeId }
                if (route != null) {
                    selectRoute(route)
                } else {
                    logger.warn { "Route not found: ${event.routeId}" }
                }
            }

            is MapEvent.MapTapped -> {
                // Deselect any selected markers or routes
                deselectMarker()
                deselectRoute()
            }

            is MapEvent.CameraMoved -> {
                // Update camera position in state for tracking
                // Note: This is informational only, we don't update cameraMove
                // to avoid feedback loops
                logger.debug {
                    "Camera moved to: ${event.position.target.latitude}, " +
                        "${event.position.target.longitude}"
                }
            }

            is MapEvent.MapReady -> {
                logger.info { "Map is ready for interaction" }
                // Could trigger initial data load or other setup here
            }
        }
    }

    /**
     * Cleanup method to release resources and prevent memory leaks. MUST be called when this controller is no longer
     * needed.
     *
     * Cancels the controllerScope which automatically cancels all child jobs (including locationTrackingJob).
     */
    override fun cleanup() {
        logger.info { "Cleaning up MapController" }
        // Cancelling controllerScope automatically cancels all child jobs (including locationTrackingJob)
        // No need to cancel locationTrackingJob separately since it's launched in controllerScope
        controllerScope.cancel()
        logger.debug { "MapController cleanup complete" }
    }
}
