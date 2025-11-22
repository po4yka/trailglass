package com.po4yka.trailglass.feature.tracking

import com.po4yka.trailglass.domain.model.Coordinate
import com.po4yka.trailglass.domain.model.Location
import com.po4yka.trailglass.logging.logger
import kotlinx.datetime.Instant
import kotlin.math.*

/**
 * Detects when user visits and stays at a place.
 *
 * A place visit is detected when:
 * - User stays within VISIT_RADIUS for MIN_VISIT_DURATION
 * - User has moved at least MIN_DISTANCE_FROM_LAST_VISIT from previous visit
 */
class PlaceVisitDetector {
    private val logger = logger()

    companion object {
        // Radius defining a "place" (30 meters)
        private const val VISIT_RADIUS_METERS = 30.0

        // Minimum duration to count as a visit (5 minutes)
        private const val MIN_VISIT_DURATION_MS = 5 * 60 * 1000L

        // Minimum distance from last visit to create new visit (50 meters)
        private const val MIN_DISTANCE_FROM_LAST_VISIT = 50.0
    }

    private var potentialVisitLocation: Coordinate? = null
    private var potentialVisitStartTime: Instant? = null
    private var lastConfirmedVisitLocation: Coordinate? = null

    /**
     * Process a new location and detect place visits.
     *
     * @param location New location to process
     * @return PlaceVisitEvent if visit detected, null otherwise
     */
    fun processLocation(location: Location): PlaceVisitEvent? {
        val now = location.timestamp

        if (potentialVisitLocation == null) {
            // Start tracking potential visit
            potentialVisitLocation = location.coordinate
            potentialVisitStartTime = now
            return null
        }

        val distance = calculateDistance(potentialVisitLocation!!, location.coordinate)

        if (distance <= VISIT_RADIUS_METERS) {
            // User is still at the same place
            val duration = now.toEpochMilliseconds() - potentialVisitStartTime!!.toEpochMilliseconds()

            if (duration >= MIN_VISIT_DURATION_MS) {
                // Check if this is far enough from last visit
                if (lastConfirmedVisitLocation == null ||
                    calculateDistance(lastConfirmedVisitLocation!!, potentialVisitLocation!!) >=
                    MIN_DISTANCE_FROM_LAST_VISIT
                ) {
                    // Place visit detected!
                    lastConfirmedVisitLocation = potentialVisitLocation
                    val visitLocation = potentialVisitLocation!!
                    val visitStartTime = potentialVisitStartTime!!

                    // Reset for next visit
                    potentialVisitLocation = location.coordinate
                    potentialVisitStartTime = now

                    logger.info { "Place visit detected at $visitLocation" }
                    return PlaceVisitEvent.VisitDetected(
                        location = visitLocation,
                        arrivalTime = visitStartTime,
                        departureTime = now,
                        duration = duration
                    )
                }
            }
        } else {
            // User moved, reset potential visit
            potentialVisitLocation = location.coordinate
            potentialVisitStartTime = now
        }

        return null
    }

    /**
     * Reset detector state.
     */
    fun reset() {
        potentialVisitLocation = null
        potentialVisitStartTime = null
        lastConfirmedVisitLocation = null
        logger.debug { "Place visit detector reset" }
    }

    /**
     * Calculate distance between two coordinates in meters.
     */
    private fun calculateDistance(
        start: Coordinate,
        end: Coordinate
    ): Double {
        val earthRadiusMeters = 6371000.0

        val lat1 = (start.latitude * PI / 180.0)
        val lat2 = (end.latitude * PI / 180.0)
        val dLat = (end.latitude - start.latitude * PI / 180.0)
        val dLon = (end.longitude - start.longitude * PI / 180.0)

        val a =
            sin(dLat / 2).pow(2) +
                cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadiusMeters * c
    }
}

/**
 * Events emitted by place visit detector.
 */
sealed class PlaceVisitEvent {
    /**
     * Place visit was detected.
     */
    data class VisitDetected(
        val location: Coordinate,
        val arrivalTime: Instant,
        val departureTime: Instant,
        val duration: Long
    ) : PlaceVisitEvent()
}
