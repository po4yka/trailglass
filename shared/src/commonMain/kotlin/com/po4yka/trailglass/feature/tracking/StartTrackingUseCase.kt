package com.po4yka.trailglass.feature.tracking

import com.po4yka.trailglass.location.tracking.LocationTracker
import com.po4yka.trailglass.location.tracking.TrackingMode
import com.po4yka.trailglass.logging.logger

/**
 * Use case for starting location tracking.
 */
class StartTrackingUseCase(
    private val locationTracker: LocationTracker
) {

    private val logger = logger()

    /**
     * Result of starting tracking.
     */
    sealed class Result {
        object Success : Result()
        object PermissionDenied : Result()
        data class Error(val message: String) : Result()
    }

    /**
     * Start location tracking with the specified mode.
     *
     * @param mode Tracking mode to use
     * @return Result of the operation
     */
    suspend fun execute(mode: TrackingMode): Result {
        logger.info { "Starting tracking with mode: $mode" }

        // Check permissions
        if (!locationTracker.hasPermissions()) {
            logger.warn { "Cannot start tracking - missing permissions" }
            return Result.PermissionDenied
        }

        return try {
            locationTracker.startTracking(mode)
            logger.info { "Tracking started successfully" }
            Result.Success
        } catch (e: Exception) {
            logger.error(e) { "Failed to start tracking" }
            Result.Error(e.message ?: "Unknown error")
        }
    }
}
