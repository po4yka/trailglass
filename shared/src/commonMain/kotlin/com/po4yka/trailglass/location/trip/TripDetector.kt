package com.po4yka.trailglass.location.trip

import com.po4yka.trailglass.domain.model.PlaceVisit
import com.po4yka.trailglass.domain.model.Trip
import com.po4yka.trailglass.logging.logger
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.hours

/**
 * Detects trips from place visit data.
 * Coordinates home location detection and trip boundary detection.
 */
class TripDetector(
    private val homeLocationDetector: HomeLocationDetector = HomeLocationDetector(),
    private val tripBoundaryDetector: TripBoundaryDetector = TripBoundaryDetector()
) {

    private val logger = logger()

    /**
     * Detect trips from a set of place visits.
     *
     * @param visits All place visits for the user
     * @param userId The user ID
     * @return List of detected trips
     */
    fun detectTrips(visits: List<PlaceVisit>, userId: String): List<Trip> {
        if (visits.isEmpty()) {
            logger.debug { "No visits provided for trip detection" }
            return emptyList()
        }

        logger.info { "Detecting trips for user $userId from ${visits.size} place visits" }

        // Step 1: Detect home location
        val homeLocation = homeLocationDetector.detectHome(visits)
        if (homeLocation != null) {
            logger.info {
                "Home location detected at (${homeLocation.latitude}, ${homeLocation.longitude})"
            }
        } else {
            logger.warn { "Unable to detect home location for user $userId" }
        }

        // Step 2: Detect trip boundaries
        val tripSegments = tripBoundaryDetector.detectTrips(visits, homeLocation)
        logger.debug { "Detected ${tripSegments.size} trip segments" }

        // Step 3: Convert to Trip domain objects
        val trips = tripSegments.mapIndexed { index, segment ->
            val tripId = generateTripId(userId, segment.startTime, index)

            // Determine if trip is ongoing (last trip and recent)
            val isOngoing = index == tripSegments.lastIndex &&
                    isRecentTrip(segment.endTime)

            Trip(
                id = tripId,
                name = null, // Auto-generated name can be added later
                startTime = segment.startTime,
                endTime = if (isOngoing) null else segment.endTime,
                primaryCountry = segment.primaryCountry,
                isOngoing = isOngoing,
                userId = userId
            )
        }

        logger.info { "Detected ${trips.size} trips (${trips.count { it.isOngoing }} ongoing)" }
        return trips
    }

    /**
     * Generate a deterministic trip ID.
     */
    private fun generateTripId(userId: String, startTime: Instant, index: Int): String {
        return "trip_${userId.hashCode()}_${startTime.toEpochMilliseconds()}_$index"
    }

    /**
     * Check if a trip is recent (within last 24 hours).
     */
    private fun isRecentTrip(endTime: Instant): Boolean {
        // Simple heuristic: if the trip ended within the last 24 hours, consider it ongoing
        val now = Clock.System.now()
        val timeSinceEnd = now - endTime
        return timeSinceEnd < 24.hours
    }
}
