package com.po4yka.trailglass.location.trip

import com.po4yka.trailglass.domain.model.Coordinate
import com.po4yka.trailglass.domain.model.PlaceVisit
import com.po4yka.trailglass.logging.logger
import kotlinx.datetime.Instant
import kotlin.math.*

/**
 * Detects the user's home location based on place visit patterns.
 * Home is typically where the user spends most nights and weekends.
 */
class HomeLocationDetector(
    private val homeRadiusMeters: Double = 500.0,
    private val minNightsForHome: Int = 3
) {

    private val logger = logger()

    /**
     * Home location candidate with scoring.
     */
    data class HomeCandidate(
        val location: Coordinate,
        val nightsSpent: Int,
        val totalHoursSpent: Double,
        val lastVisitTime: Instant,
        val placeVisitIds: List<String>
    )

    /**
     * Detect the user's home location from a set of place visits.
     *
     * @param visits All place visits to analyze
     * @return The detected home location, or null if unable to determine
     */
    fun detectHome(visits: List<PlaceVisit>): Coordinate? {
        if (visits.isEmpty()) {
            logger.debug { "No visits to analyze for home detection" }
            return null
        }

        logger.info { "Detecting home location from ${visits.size} place visits" }

        // Group visits by proximity
        val clusters = clusterVisitsByProximity(visits)
        logger.debug { "Grouped visits into ${clusters.size} location clusters" }

        // Score each cluster
        val candidates = clusters.map { cluster ->
            scoreHomeCandidate(cluster)
        }

        // Find the best candidate (most nights spent)
        val homeCandidate = candidates
            .filter { it.nightsSpent >= minNightsForHome }
            .maxByOrNull { it.nightsSpent }

        if (homeCandidate != null) {
            logger.info {
                "Detected home location at (${homeCandidate.location.latitude}, ${homeCandidate.location.longitude}), " +
                "${homeCandidate.nightsSpent} nights spent, ${homeCandidate.totalHoursSpent.toInt()} hours total"
            }
            return homeCandidate.location
        }

        logger.warn { "Unable to detect home location - no cluster meets minimum criteria" }
        return null
    }

    /**
     * Cluster visits by spatial proximity.
     * Visits within homeRadiusMeters are grouped together.
     */
    private fun clusterVisitsByProximity(visits: List<PlaceVisit>): List<List<PlaceVisit>> {
        val clusters = mutableListOf<MutableList<PlaceVisit>>()
        val visited = mutableSetOf<String>()

        for (visit in visits) {
            if (visit.id in visited) continue

            // Start a new cluster
            val cluster = mutableListOf(visit)
            visited.add(visit.id)

            // Find all visits within radius
            for (other in visits) {
                if (other.id in visited) continue

                val distance = haversineDistance(
                    visit.centerLatitude, visit.centerLongitude,
                    other.centerLatitude, other.centerLongitude
                )

                if (distance <= homeRadiusMeters) {
                    cluster.add(other)
                    visited.add(other.id)
                }
            }

            clusters.add(cluster)
        }

        return clusters
    }

    /**
     * Score a cluster of visits as a potential home location.
     */
    private fun scoreHomeCandidate(cluster: List<PlaceVisit>): HomeCandidate {
        // Calculate centroid
        val avgLat = cluster.map { it.centerLatitude }.average()
        val avgLon = cluster.map { it.centerLongitude }.average()

        // Count nights spent (visits that include nighttime hours)
        var nightsSpent = 0
        var totalHours = 0.0

        for (visit in cluster) {
            val durationHours = (visit.endTime - visit.startTime).inWholeHours.toDouble()
            totalHours += durationHours

            // Check if visit includes nighttime (simplified: check if duration > 6 hours)
            if (durationHours >= 6.0) {
                nightsSpent++
            }
        }

        val lastVisit = cluster.maxByOrNull { it.endTime }?.endTime
            ?: cluster.first().endTime

        return HomeCandidate(
            location = Coordinate(avgLat, avgLon),
            nightsSpent = nightsSpent,
            totalHoursSpent = totalHours,
            lastVisitTime = lastVisit,
            placeVisitIds = cluster.map { it.id }
        )
    }

    /**
     * Calculate Haversine distance between two coordinates.
     */
    private fun haversineDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val earthRadiusMeters = 6371000.0

        val dLat = (lat2 - lat1) * PI / 180.0
        val dLon = (lon2 - lon1) * PI / 180.0

        val a = sin(dLat / 2).pow(2) +
                cos(lat1 * PI / 180.0) * cos(lat2 * PI / 180.0) *
                sin(dLon / 2).pow(2)

        val c = 2 * asin(sqrt(a))

        return earthRadiusMeters * c
    }
}
