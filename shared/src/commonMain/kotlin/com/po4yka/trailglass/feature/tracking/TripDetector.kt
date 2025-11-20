package com.po4yka.trailglass.feature.tracking

import com.po4yka.trailglass.domain.model.Coordinate
import com.po4yka.trailglass.domain.model.Location
import com.po4yka.trailglass.logging.logger
import kotlinx.datetime.Instant
import kotlin.math.*

/**
 * Detects trip start and end based on location patterns.
 *
 * A trip is considered started when:
 * - User moves more than MIN_TRIP_DISTANCE from a stationary point
 * - Movement continues for MIN_TRIP_DURATION
 *
 * A trip is considered ended when:
 * - User stays within STATIONARY_RADIUS for STATIONARY_DURATION
 */
class TripDetector {
    private val logger = logger()

    companion object {
        // Minimum distance to consider trip started (50 meters)
        private const val MIN_TRIP_DISTANCE_METERS = 50.0

        // Minimum duration for a valid trip (2 minutes)
        private const val MIN_TRIP_DURATION_MS = 2 * 60 * 1000L

        // Radius within which user is considered stationary (20 meters)
        private const val STATIONARY_RADIUS_METERS = 20.0

        // Duration of stationary period to end trip (3 minutes)
        private const val STATIONARY_DURATION_MS = 3 * 60 * 1000L
    }

    private var lastTripStartLocation: Coordinate? = null
    private var lastTripStartTime: Instant? = null
    private var stationaryStartLocation: Coordinate? = null
    private var stationaryStartTime: Instant? = null
    private var currentTripActive = false

    /**
     * Process a new location and determine if trip state changed.
     *
     * @param location New location to process
     * @return TripEvent if state changed, null otherwise
     */
    fun processLocation(location: Location): TripEvent? {
        val now = location.timestamp

        if (!currentTripActive) {
            // Check if trip should start
            return checkTripStart(location, now)
        } else {
            // Check if trip should end
            return checkTripEnd(location, now)
        }
    }

    /**
     * Check if a trip should start based on movement.
     */
    private fun checkTripStart(location: Location, now: Instant): TripEvent? {
        if (lastTripStartLocation == null) {
            // First location, set as potential trip start
            lastTripStartLocation = location.coordinate
            lastTripStartTime = now
            return null
        }

        val distance = calculateDistance(
            lastTripStartLocation!!,
            location.coordinate
        )

        if (distance > MIN_TRIP_DISTANCE_METERS) {
            val duration = now.toEpochMilliseconds() - lastTripStartTime!!.toEpochMilliseconds()

            if (duration >= MIN_TRIP_DURATION_MS) {
                // Trip started!
                currentTripActive = true
                stationaryStartLocation = null
                stationaryStartTime = null

                logger.info { "Trip started at ${location.coordinate}" }
                return TripEvent.TripStarted(
                    startLocation = lastTripStartLocation!!,
                    startTime = lastTripStartTime!!
                )
            }
        } else {
            // Reset trip start tracking if user moved back
            lastTripStartLocation = location.coordinate
            lastTripStartTime = now
        }

        return null
    }

    /**
     * Check if a trip should end based on stationary period.
     */
    private fun checkTripEnd(location: Location, now: Instant): TripEvent? {
        if (stationaryStartLocation == null) {
            // Start tracking stationary period
            stationaryStartLocation = location.coordinate
            stationaryStartTime = now
            return null
        }

        val distance = calculateDistance(
            stationaryStartLocation!!,
            location.coordinate
        )

        if (distance <= STATIONARY_RADIUS_METERS) {
            // User is still stationary
            val stationaryDuration = now.toEpochMilliseconds() - stationaryStartTime!!.toEpochMilliseconds()

            if (stationaryDuration >= STATIONARY_DURATION_MS) {
                // Trip ended!
                currentTripActive = false
                lastTripStartLocation = location.coordinate
                lastTripStartTime = now

                logger.info { "Trip ended at ${location.coordinate}" }
                return TripEvent.TripEnded(
                    endLocation = location.coordinate,
                    endTime = now
                )
            }
        } else {
            // User moved, reset stationary tracking
            stationaryStartLocation = null
            stationaryStartTime = null
        }

        return null
    }

    /**
     * Reset detector state (e.g., when manually stopping tracking).
     */
    fun reset() {
        lastTripStartLocation = null
        lastTripStartTime = null
        stationaryStartLocation = null
        stationaryStartTime = null
        currentTripActive = false
        logger.debug { "Trip detector reset" }
    }

    /**
     * Calculate distance between two coordinates in meters.
     * Uses Haversine formula.
     */
    private fun calculateDistance(start: Coordinate, end: Coordinate): Double {
        val earthRadiusMeters = 6371000.0

        val lat1 = (start.latitude * PI / 180.0)
        val lat2 = (end.latitude * PI / 180.0)
        val dLat = (end.latitude - start.latitude * PI / 180.0)
        val dLon = (end.longitude - start.longitude * PI / 180.0)

        val a = sin(dLat / 2).pow(2) +
                cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadiusMeters * c
    }
}

/**
 * Events emitted by trip detector.
 */
sealed class TripEvent {
    /**
     * Trip has started.
     */
    data class TripStarted(
        val startLocation: Coordinate,
        val startTime: Instant
    ) : TripEvent()

    /**
     * Trip has ended.
     */
    data class TripEnded(
        val endLocation: Coordinate,
        val endTime: Instant
    ) : TripEvent()
}
