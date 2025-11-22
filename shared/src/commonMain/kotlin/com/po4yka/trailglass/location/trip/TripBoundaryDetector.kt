package com.po4yka.trailglass.location.trip

import com.po4yka.trailglass.domain.model.Coordinate
import com.po4yka.trailglass.domain.model.PlaceVisit
import com.po4yka.trailglass.logging.logger
import kotlinx.datetime.Instant
import kotlin.math.*

/**
 * Detects trip boundaries by identifying when the user travels
 * far from their home location.
 */
class TripBoundaryDetector(
    private val tripDistanceThresholdMeters: Double = 100_000.0, // 100km from home
    private val minTripDurationHours: Int = 4
) {
    private val logger = logger()

    /**
     * A potential trip segment.
     */
    data class TripSegment(
        val startTime: Instant,
        val endTime: Instant,
        val visitIds: List<String>,
        val primaryCountry: String?
    )

    /**
     * Detect trip segments from place visits and home location.
     *
     * @param visits All place visits sorted by time
     * @param homeLocation The user's detected home location (null if unknown)
     * @return List of detected trip segments
     */
    fun detectTrips(
        visits: List<PlaceVisit>,
        homeLocation: Coordinate?
    ): List<TripSegment> {
        if (visits.isEmpty()) {
            logger.debug { "No visits to analyze for trip detection" }
            return emptyList()
        }

        if (homeLocation == null) {
            logger.warn { "No home location available - treating all visits as potential trips" }
            // Without home location, we can't reliably detect trips
            // Return a single segment spanning all visits
            return listOf(
                TripSegment(
                    startTime = visits.first().startTime,
                    endTime = visits.last().endTime,
                    visitIds = visits.map { it.id },
                    primaryCountry =
                        visits
                            .mapNotNull { it.countryCode }
                            .groupingBy { it }
                            .eachCount()
                            .maxByOrNull { it.value }
                            ?.key
                )
            )
        }

        logger.info {
            "Detecting trips from ${visits.size} visits with home at (${homeLocation.latitude}, ${homeLocation.longitude})"
        }

        val sortedVisits = visits.sortedBy { it.startTime }
        val trips = mutableListOf<TripSegment>()

        var currentTripVisits = mutableListOf<PlaceVisit>()
        var tripStartTime: Instant? = null

        for (visit in sortedVisits) {
            val distanceFromHome =
                haversineDistance(
                    homeLocation.latitude,
                    homeLocation.longitude,
                    visit.centerLatitude,
                    visit.centerLongitude
                )

            val isAwayFromHome = distanceFromHome > tripDistanceThresholdMeters

            if (isAwayFromHome) {
                // User is away from home - part of a trip
                if (tripStartTime == null) {
                    // Start of a new trip
                    tripStartTime = visit.startTime
                    logger.debug { "Trip started at ${visit.city ?: "unknown location"}" }
                }
                currentTripVisits.add(visit)
            } else {
                // User is at/near home
                if (tripStartTime != null && currentTripVisits.isNotEmpty()) {
                    // End of current trip
                    val tripEndTime = currentTripVisits.last().endTime
                    val durationHours = (tripEndTime - tripStartTime).inWholeHours

                    if (durationHours >= minTripDurationHours) {
                        // Valid trip - add it
                        val primaryCountry =
                            currentTripVisits
                                .mapNotNull { it.countryCode }
                                .groupingBy { it }
                                .eachCount()
                                .maxByOrNull { it.value }
                                ?.key

                        trips.add(
                            TripSegment(
                                startTime = tripStartTime,
                                endTime = tripEndTime,
                                visitIds = currentTripVisits.map { it.id },
                                primaryCountry = primaryCountry
                            )
                        )

                        logger.debug {
                            "Trip ended at ${currentTripVisits.last().city ?: "unknown"}, " +
                                "duration: ${durationHours}h, visits: ${currentTripVisits.size}, country: $primaryCountry"
                        }
                    } else {
                        logger.trace { "Discarding short trip segment (${durationHours}h < ${minTripDurationHours}h)" }
                    }

                    // Reset for next trip
                    currentTripVisits.clear()
                    tripStartTime = null
                }
            }
        }

        // Handle ongoing trip (user still away from home)
        if (tripStartTime != null && currentTripVisits.isNotEmpty()) {
            val tripEndTime = currentTripVisits.last().endTime
            val durationHours = (tripEndTime - tripStartTime).inWholeHours

            if (durationHours >= minTripDurationHours) {
                val primaryCountry =
                    currentTripVisits
                        .mapNotNull { it.countryCode }
                        .groupingBy { it }
                        .eachCount()
                        .maxByOrNull { it.value }
                        ?.key

                trips.add(
                    TripSegment(
                        startTime = tripStartTime,
                        endTime = tripEndTime,
                        visitIds = currentTripVisits.map { it.id },
                        primaryCountry = primaryCountry
                    )
                )

                logger.debug { "Ongoing trip detected, ${currentTripVisits.size} visits so far" }
            }
        }

        logger.info { "Detected ${trips.size} trips" }
        return trips
    }

    /**
     * Calculate Haversine distance between two coordinates.
     */
    private fun haversineDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val earthRadiusMeters = 6371000.0

        val dLat = (lat2 - lat1) * PI / 180.0
        val dLon = (lon2 - lon1) * PI / 180.0

        val a =
            sin(dLat / 2).pow(2) +
                cos(lat1 * PI / 180.0) * cos(lat2 * PI / 180.0) *
                sin(dLon / 2).pow(2)

        val c = 2 * asin(sqrt(a))

        return earthRadiusMeters * c
    }
}
