package com.po4yka.trailglass.feature.tracking

import com.po4yka.trailglass.location.tracking.LocationTracker
import com.po4yka.trailglass.logging.logger
import me.tatarka.inject.annotations.Inject

/**
 * Use case for stopping location tracking.
 */
@Inject
class StopTrackingUseCase(
    private val locationTracker: LocationTracker
) {

    private val logger = logger()

    /**
     * Stop location tracking.
     */
    suspend fun execute() {
        logger.info { "Stopping tracking" }

        try {
            locationTracker.stopTracking()
            logger.info { "Tracking stopped successfully" }
        } catch (e: Exception) {
            logger.error(e) { "Error stopping tracking" }
        }
    }
}
