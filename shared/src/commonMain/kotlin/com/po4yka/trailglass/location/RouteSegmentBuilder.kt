package com.po4yka.trailglass.location

import com.po4yka.trailglass.domain.model.Coordinate
import com.po4yka.trailglass.domain.model.LocationSample
import com.po4yka.trailglass.domain.model.PlaceVisit
import com.po4yka.trailglass.domain.model.RouteSegment
import com.po4yka.trailglass.domain.model.TransportType
import com.po4yka.trailglass.logging.logger
import kotlinx.datetime.Instant
import kotlin.math.*

/**
 * Builds route segments from location samples.
 * Identifies movement between places and infers transport type.
 */
class RouteSegmentBuilder(
    private val pathSimplifier: PathSimplifier = PathSimplifier()
) {

    private val logger = logger()

    /**
     * Build route segments from location samples and place visits.
     *
     * @param samples All location samples sorted by time
     * @param visits Detected place visits
     * @return List of route segments representing movement
     */
    fun buildSegments(
        samples: List<LocationSample>,
        visits: List<PlaceVisit>
    ): List<RouteSegment> {
        if (samples.isEmpty()) {
            logger.debug { "No samples to build route segments" }
            return emptyList()
        }

        logger.info { "Building route segments from ${samples.size} samples and ${visits.size} visits" }

        val sortedSamples = samples.sortedBy { it.timestamp }
        val sortedVisits = visits.sortedBy { it.startTime }

        val segments = mutableListOf<RouteSegment>()

        // Create a map of sample IDs that are part of visits
        val visitSampleIds = visits.flatMap { it.locationSampleIds }.toSet()

        // Group samples into movement segments (samples not in visits)
        val movementGroups = groupMovementSamples(sortedSamples, visitSampleIds, sortedVisits)

        // Build route segment for each movement group
        movementGroups.forEach { (fromVisit, toVisit, movementSamples) ->
            if (movementSamples.size >= 2) {
                val segment = createRouteSegment(movementSamples, fromVisit, toVisit)
                segments.add(segment)
            }
        }

        logger.info { "Built ${segments.size} route segments" }
        return segments
    }

    /**
     * Group samples that represent movement (between visits).
     */
    private fun groupMovementSamples(
        samples: List<LocationSample>,
        visitSampleIds: Set<String>,
        visits: List<PlaceVisit>
    ): List<Triple<PlaceVisit?, PlaceVisit?, List<LocationSample>>> {
        val groups = mutableListOf<Triple<PlaceVisit?, PlaceVisit?, List<LocationSample>>>()
        val movementSamples = mutableListOf<LocationSample>()
        var fromVisit: PlaceVisit? = null
        var toVisit: PlaceVisit? = null

        for (sample in samples) {
            if (visitSampleIds.contains(sample.id)) {
                // This sample is part of a visit
                if (movementSamples.isNotEmpty()) {
                    // End current movement segment
                    toVisit = findVisitContainingSample(sample.id, visits)
                    groups.add(Triple(fromVisit, toVisit, movementSamples.toList()))
                    movementSamples.clear()
                }
                // Update fromVisit for next segment
                fromVisit = findVisitContainingSample(sample.id, visits)
            } else {
                // This sample is movement between visits
                movementSamples.add(sample)
            }
        }

        // Add final movement group if any
        if (movementSamples.isNotEmpty()) {
            groups.add(Triple(fromVisit, null, movementSamples.toList()))
        }

        return groups
    }

    /**
     * Find which visit contains a given sample ID.
     */
    private fun findVisitContainingSample(sampleId: String, visits: List<PlaceVisit>): PlaceVisit? {
        return visits.find { it.locationSampleIds.contains(sampleId) }
    }

    /**
     * Create a route segment from movement samples.
     */
    private fun createRouteSegment(
        samples: List<LocationSample>,
        fromVisit: PlaceVisit?,
        toVisit: PlaceVisit?
    ): RouteSegment {
        val startTime = samples.first().timestamp
        val endTime = samples.last().timestamp

        // Calculate distance and speed
        val distance = calculateTotalDistance(samples)
        val durationSeconds = (endTime - startTime).inWholeSeconds.toDouble()
        val averageSpeed = if (durationSeconds > 0) distance / durationSeconds else null

        // Infer transport type from speed
        val transportType = TransportType.inferFromSpeed(averageSpeed)

        // Simplify path
        val simplifiedPath = pathSimplifier.simplify(samples)

        val segmentId = generateSegmentId(startTime, fromVisit?.id, toVisit?.id)

        logger.debug {
            "Created route segment: ${fromVisit?.city ?: "?"} → ${toVisit?.city ?: "?"}, " +
            "distance: ${distance.toInt()}m, transport: $transportType"
        }

        return RouteSegment(
            id = segmentId,
            startTime = startTime,
            endTime = endTime,
            fromPlaceVisitId = fromVisit?.id,
            toPlaceVisitId = toVisit?.id,
            locationSampleIds = samples.map { it.id },
            simplifiedPath = simplifiedPath,
            transportType = transportType,
            distanceMeters = distance,
            averageSpeedMps = averageSpeed
        )
    }

    /**
     * Calculate total distance traveled along a path.
     */
    private fun calculateTotalDistance(samples: List<LocationSample>): Double {
        var totalDistance = 0.0
        for (i in 0 until samples.lastIndex) {
            val distance = haversineDistance(
                samples[i].latitude, samples[i].longitude,
                samples[i + 1].latitude, samples[i + 1].longitude
            )
            totalDistance += distance
        }
        return totalDistance
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

    /**
     * Generate a deterministic ID for a route segment.
     */
    private fun generateSegmentId(time: Instant, fromId: String?, toId: String?): String {
        return "route_${time.toEpochMilliseconds()}_${fromId?.hashCode() ?: 0}_${toId?.hashCode() ?: 0}"
    }
}
