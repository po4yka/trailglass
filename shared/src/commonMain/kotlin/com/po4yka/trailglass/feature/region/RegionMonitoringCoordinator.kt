package com.po4yka.trailglass.feature.region

import com.po4yka.trailglass.data.repository.RegionRepository
import com.po4yka.trailglass.domain.model.Coordinate
import com.po4yka.trailglass.domain.model.Region
import com.po4yka.trailglass.domain.model.RegionTransition
import com.po4yka.trailglass.domain.model.TransitionType
import com.po4yka.trailglass.domain.service.LocationService
import com.po4yka.trailglass.logging.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import me.tatarka.inject.annotations.Inject

/**
 * Coordinator for monitoring region transitions (enter/exit events).
 * Observes location updates and tracks which regions the user enters or exits.
 *
 * IMPORTANT: Call [start] to begin monitoring and [stop] when monitoring is no longer needed.
 */
@Inject
class RegionMonitoringCoordinator(
    private val locationService: LocationService,
    private val regionRepository: RegionRepository,
    coroutineScope: CoroutineScope,
    private val userId: String
) {
    private val logger = logger()

    // Create a child scope that can be cancelled independently
    private val coordinatorScope = CoroutineScope(
        coroutineScope.coroutineContext + SupervisorJob()
    )

    // Track which regions the user is currently in
    private val currentRegions = mutableSetOf<String>()

    // Emit region transition events
    private val _transitions = MutableSharedFlow<RegionTransition>(replay = 0)
    val transitions: SharedFlow<RegionTransition> = _transitions.asSharedFlow()

    private var isMonitoring = false

    /**
     * Start monitoring region transitions.
     * Combines location updates with region data to detect enter/exit events.
     */
    fun start() {
        if (isMonitoring) {
            logger.warn { "RegionMonitoringCoordinator is already monitoring" }
            return
        }

        logger.info { "Starting region monitoring for user $userId" }
        isMonitoring = true

        coordinatorScope.launch {
            combine(
                locationService.locationUpdates,
                regionRepository.getAllRegions(userId)
            ) { coordinate, regions ->
                Pair(coordinate, regions)
            }
                .catch { e ->
                    logger.error(e) { "Error in region monitoring" }
                }
                .collect { (coordinate, regions) ->
                    processLocation(coordinate, regions)
                }
        }
    }

    /**
     * Stop monitoring region transitions.
     */
    fun stop() {
        if (!isMonitoring) {
            logger.warn { "RegionMonitoringCoordinator is not monitoring" }
            return
        }

        logger.info { "Stopping region monitoring" }
        isMonitoring = false
        coordinatorScope.cancel()
    }

    /**
     * Process a location update and check for region transitions.
     */
    private suspend fun processLocation(coordinate: Coordinate, regions: List<Region>) {
        val lat = coordinate.latitude
        val lon = coordinate.longitude

        // Find regions that contain the current location
        val regionsContainingLocation = regions.filter { region ->
            region.contains(lat, lon)
        }

        val currentRegionIds = regionsContainingLocation.map { it.id }.toSet()

        // Check for ENTER events (regions now in but weren't before)
        val enteredRegions = currentRegionIds - currentRegions
        for (regionId in enteredRegions) {
            val region = regionsContainingLocation.find { it.id == regionId }
            if (region != null) {
                handleEnterEvent(region, lat, lon)
            }
        }

        // Check for EXIT events (regions were in before but aren't now)
        val exitedRegionIds = currentRegions - currentRegionIds
        for (regionId in exitedRegionIds) {
            val region = regions.find { it.id == regionId }
            if (region != null) {
                handleExitEvent(region, lat, lon)
            }
        }

        // Update current regions
        currentRegions.clear()
        currentRegions.addAll(currentRegionIds)
    }

    /**
     * Handle a region enter event.
     */
    private suspend fun handleEnterEvent(region: Region, lat: Double, lon: Double) {
        logger.info { "Entered region: ${region.name}" }

        val now = Clock.System.now()

        // Update region statistics
        regionRepository.updateEnterStats(region.id, now)

        // Emit transition event
        val transition = RegionTransition(
            regionId = region.id,
            regionName = region.name,
            transitionType = TransitionType.ENTER,
            timestamp = now.toEpochMilliseconds(),
            latitude = lat,
            longitude = lon
        )

        _transitions.emit(transition)
    }

    /**
     * Handle a region exit event.
     */
    private suspend fun handleExitEvent(region: Region, lat: Double, lon: Double) {
        logger.info { "Exited region: ${region.name}" }

        val now = Clock.System.now()

        // Update region statistics
        regionRepository.updateExitStats(region.id, now)

        // Emit transition event
        val transition = RegionTransition(
            regionId = region.id,
            regionName = region.name,
            transitionType = TransitionType.EXIT,
            timestamp = now.toEpochMilliseconds(),
            latitude = lat,
            longitude = lon
        )

        _transitions.emit(transition)
    }
}
