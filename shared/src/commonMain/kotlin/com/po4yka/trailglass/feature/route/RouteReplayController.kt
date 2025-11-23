package com.po4yka.trailglass.feature.route

import com.po4yka.trailglass.domain.model.Coordinate
import com.po4yka.trailglass.domain.model.RoutePoint
import com.po4yka.trailglass.domain.model.TripRoute
import com.po4yka.trailglass.feature.common.Lifecycle
import com.po4yka.trailglass.logging.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import me.tatarka.inject.annotations.Inject
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/** Playback speed multiplier options. */
enum class PlaybackSpeed(
    val multiplier: Float,
    val displayName: String
) {
    HALF(0.5f, "0.5×"),
    NORMAL(1.0f, "1×"),
    DOUBLE(2.0f, "2×"),
    QUAD(4.0f, "4×");

    fun next(): PlaybackSpeed =
        when (this) {
            HALF -> NORMAL
            NORMAL -> DOUBLE
            DOUBLE -> QUAD
            QUAD -> HALF
        }
}

/** Current vehicle state during replay. */
data class VehicleState(
    val position: Coordinate,
    val bearing: Double,
    val currentPoint: RoutePoint,
    val timestamp: Instant
)

/**
 * Controller for Route Replay Mode. Manages animated playback of route with vehicle movement and camera following.
 *
 * IMPORTANT: Call [cleanup] when this controller is no longer needed to prevent memory leaks.
 */
@Inject
class RouteReplayController(
    private val getTripRouteUseCase: GetTripRouteUseCase,
    coroutineScope: CoroutineScope
) : Lifecycle {
    private val logger = logger()

    // Create a child scope that can be cancelled independently
    private val controllerScope =
        CoroutineScope(
            coroutineScope.coroutineContext + SupervisorJob()
        )

    /** State for Route Replay screen. */
    data class ReplayState(
        val tripRoute: TripRoute? = null,
        val isLoading: Boolean = false,
        val error: String? = null,
        // Playback state
        val isPlaying: Boolean = false,
        val progress: Float = 0f, // 0.0 to 1.0
        val playbackSpeed: PlaybackSpeed = PlaybackSpeed.NORMAL,
        // Vehicle state
        val vehicleState: VehicleState? = null,
        // Camera state
        val cameraPosition: Coordinate? = null,
        val cameraBearing: Double = 0.0,
        val cameraTilt: Double = 45.0, // 3D perspective angle
        val cameraZoom: Float = 16f,
        // UI state
        val showControls: Boolean = true
    )

    private val _state = MutableStateFlow(ReplayState())
    val state: StateFlow<ReplayState> = _state.asStateFlow()

    private var animationJob: Job? = null
    private val animationFrameMs = 16L // ~60 FPS

    /** Load route data for replay. */
    fun loadRoute(tripId: String) {
        controllerScope.launch {
            logger.info { "Loading route for replay: $tripId" }
            _state.value = _state.value.copy(isLoading = true, error = null)

            getTripRouteUseCase
                .execute(tripId)
                .onSuccess { tripRoute ->
                    logger.info { "Route loaded for replay: ${tripRoute.fullPath.size} points" }
                    _state.value =
                        _state.value.copy(
                            tripRoute = tripRoute,
                            isLoading = false,
                            vehicleState = createInitialVehicleState(tripRoute),
                            cameraPosition =
                                tripRoute.fullPath.firstOrNull()?.let {
                                    Coordinate(it.latitude, it.longitude)
                                }
                        )
                }.onError { error ->
                    logger.error { "Failed to load route for replay: $tripId - ${error.getTechnicalDetails()}" }
                    _state.value =
                        _state.value.copy(
                            isLoading = false,
                            error = error.getUserFriendlyMessage()
                        )
                }
        }
    }

    /** Start or resume playback. */
    fun play() {
        val route = _state.value.tripRoute ?: return
        if (route.fullPath.isEmpty()) return

        logger.debug { "Starting playback at ${_state.value.progress * 100}%" }
        _state.value = _state.value.copy(isPlaying = true)

        // Cancel existing animation if any
        animationJob?.cancel()

        // Start animation loop
        animationJob =
            controllerScope.launch {
                animateRoute()
            }
    }

    /** Pause playback. */
    fun pause() {
        logger.debug { "Pausing playback" }
        _state.value = _state.value.copy(isPlaying = false)
        animationJob?.cancel()
    }

    /** Toggle play/pause. */
    fun togglePlayPause() {
        if (_state.value.isPlaying) {
            pause()
        } else {
            play()
        }
    }

    /** Seek to a specific position (0.0 to 1.0). */
    fun seekTo(progress: Float) {
        val clampedProgress = progress.coerceIn(0f, 1f)
        logger.debug { "Seeking to ${clampedProgress * 100}%" }

        val route = _state.value.tripRoute ?: return
        if (route.fullPath.isEmpty()) return

        // Calculate new vehicle state at this position
        val newVehicleState = calculateVehicleStateAtProgress(route, clampedProgress)

        _state.value =
            _state.value.copy(
                progress = clampedProgress,
                vehicleState = newVehicleState,
                cameraPosition = newVehicleState.position,
                cameraBearing = newVehicleState.bearing
            )
    }

    /** Cycle through playback speeds. */
    fun cyclePlaybackSpeed() {
        val newSpeed = _state.value.playbackSpeed.next()
        logger.debug { "Changing playback speed to ${newSpeed.displayName}" }
        _state.value = _state.value.copy(playbackSpeed = newSpeed)
    }

    /** Restart playback from the beginning. */
    fun restart() {
        logger.debug { "Restarting playback" }
        seekTo(0f)
        play()
    }

    /** Toggle controls visibility. */
    fun toggleControls() {
        _state.value =
            _state.value.copy(
                showControls = !_state.value.showControls
            )
    }

    /** Clear error state. */
    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    /** Stop and cleanup. */
    fun stop() {
        pause()
        _state.value = ReplayState()
    }

    /** Animation loop that updates vehicle position. */
    private suspend fun animateRoute() {
        val route = _state.value.tripRoute ?: return
        if (route.fullPath.isEmpty()) return

        val totalDuration = (route.endTime - route.startTime).inWholeMilliseconds.toDouble()
        val baseDurationMs = 60000.0 // Base duration: 1 minute for the entire route

        while (_state.value.isPlaying && _state.value.progress < 1f) {
            val currentProgress = _state.value.progress
            val speed = _state.value.playbackSpeed.multiplier

            // Calculate progress increment based on speed and frame time
            val progressIncrement = (animationFrameMs.toFloat() / baseDurationMs.toFloat()) * speed

            val newProgress = (currentProgress + progressIncrement).coerceAtMost(1f)

            // Update vehicle state
            val newVehicleState = calculateVehicleStateAtProgress(route, newProgress)

            _state.value =
                _state.value.copy(
                    progress = newProgress,
                    vehicleState = newVehicleState,
                    cameraPosition = newVehicleState.position,
                    cameraBearing = newVehicleState.bearing
                )

            // Stop if we reached the end
            if (newProgress >= 1f) {
                logger.info { "Playback completed" }
                _state.value = _state.value.copy(isPlaying = false)
                break
            }

            delay(animationFrameMs)
        }
    }

    /**
     * Calculate vehicle state at a given progress (0.0 to 1.0). Uses linear interpolation for smooth animation between
     * route points.
     */
    private fun calculateVehicleStateAtProgress(
        route: TripRoute,
        progress: Float
    ): VehicleState {
        val path = route.fullPath
        if (path.isEmpty()) {
            return VehicleState(
                position = Coordinate(0.0, 0.0),
                bearing = 0.0,
                currentPoint = RoutePoint(0.0, 0.0, Instant.DISTANT_PAST),
                timestamp = Instant.DISTANT_PAST
            )
        }

        if (path.size == 1) {
            val point = path[0]
            return VehicleState(
                position = Coordinate(point.latitude, point.longitude),
                bearing = 0.0,
                currentPoint = point,
                timestamp = point.timestamp
            )
        }

        // Find the exact position along the path with interpolation
        val totalPoints = path.size
        val exactPosition = progress * (totalPoints - 1)
        val baseIndex = exactPosition.toInt().coerceIn(0, totalPoints - 2)
        val fraction = exactPosition - baseIndex

        val currentPoint = path[baseIndex]
        val nextPoint = path[baseIndex + 1]

        // Linear interpolation (lerp) between current and next point for smooth animation
        val interpolatedLat = lerp(currentPoint.latitude, nextPoint.latitude, fraction.toDouble())
        val interpolatedLon = lerp(currentPoint.longitude, nextPoint.longitude, fraction.toDouble())

        // Calculate bearing between current and next point
        val bearing =
            calculateBearing(
                currentPoint.latitude,
                currentPoint.longitude,
                nextPoint.latitude,
                nextPoint.longitude
            )

        // Interpolate timestamp
        val currentMillis = currentPoint.timestamp.toEpochMilliseconds()
        val nextMillis = nextPoint.timestamp.toEpochMilliseconds()
        val interpolatedMillis = lerp(currentMillis.toDouble(), nextMillis.toDouble(), fraction.toDouble())
        val interpolatedTimestamp = Instant.fromEpochMilliseconds(interpolatedMillis.toLong())

        return VehicleState(
            position = Coordinate(interpolatedLat, interpolatedLon),
            bearing = bearing,
            currentPoint = currentPoint, // Reference point for metadata (transport type, segment)
            timestamp = interpolatedTimestamp
        )
    }

    /**
     * Linear interpolation between two values.
     *
     * @param a Start value
     * @param b End value
     * @param t Interpolation factor (0.0 to 1.0)
     * @return Interpolated value
     */
    private fun lerp(
        a: Double,
        b: Double,
        t: Double
    ): Double = a + (b - a) * t

    /** Create initial vehicle state at the start of the route. */
    private fun createInitialVehicleState(route: TripRoute): VehicleState? {
        if (route.fullPath.isEmpty()) return null
        return calculateVehicleStateAtProgress(route, 0f)
    }

    /** Calculate bearing (direction) between two coordinates. Returns bearing in degrees (0-360). */
    private fun calculateBearing(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val lat1Rad = (lat1 * PI / 180.0)
        val lat2Rad = (lat2 * PI / 180.0)
        val dLon = (lon2 - lon1 * PI / 180.0)

        val y = sin(dLon) * cos(lat2Rad)
        val x = cos(lat1Rad) * sin(lat2Rad) - sin(lat1Rad) * cos(lat2Rad) * cos(dLon)

        val bearingRad = atan2(y, x)
        val bearingDeg = (bearingRad * 180.0 / PI)

        // Normalize to 0-360
        return (bearingDeg + 360) % 360
    }

    /**
     * Cleanup method to release resources and prevent memory leaks. MUST be called when this controller is no longer
     * needed.
     *
     * Cancels all running coroutines including the animation job and flow collectors.
     */
    override fun cleanup() {
        logger.info { "Cleaning up RouteReplayController" }
        animationJob?.cancel()
        animationJob = null
        controllerScope.cancel()
        logger.debug { "RouteReplayController cleanup complete" }
    }
}
