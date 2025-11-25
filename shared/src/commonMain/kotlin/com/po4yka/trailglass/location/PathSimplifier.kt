package com.po4yka.trailglass.location

import com.po4yka.trailglass.domain.model.Coordinate
import com.po4yka.trailglass.domain.model.LocationSample
import com.po4yka.trailglass.logging.logger
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Simplifies paths using the Douglas-Peucker algorithm. Reduces the number of points while preserving the shape of the
 * path.
 */
class PathSimplifier(
    private val epsilonMeters: Double = 50.0
) {
    private val logger = logger()

    /**
     * Simplify a list of location samples to a reduced set of points.
     *
     * @param samples Original location samples
     * @return Simplified list of coordinates
     */
    fun simplify(samples: List<LocationSample>): List<Coordinate> {
        if (samples.size < 3) {
            logger.trace { "Path too short to simplify (${samples.size} points)" }
            return samples.map { Coordinate(it.latitude, it.longitude) }
        }

        val points = samples.map { Coordinate(it.latitude, it.longitude) }
        val simplified = douglasPeucker(points, epsilonMeters)

        val reduction = ((1.0 - simplified.size.toDouble() / points.size) * 100).toInt()
        logger.debug { "Simplified path from ${points.size} to ${simplified.size} points ($reduction% reduction)" }

        return simplified
    }

    /**
     * Douglas-Peucker algorithm implementation. Recursively simplifies a polyline by removing points that deviate less
     * than epsilon from the line between endpoints.
     */
    private fun douglasPeucker(
        points: List<Coordinate>,
        epsilon: Double
    ): List<Coordinate> {
        if (points.size < 3) return points

        val start = points.first()
        val end = points.last()

        // Find the point with the maximum distance from the line segment
        val (maxIndex, maxDistance) =
            (1 until points.lastIndex)
                .asSequence()
                .map { index ->
                    val distance = perpendicularDistance(points[index], start, end)
                    index to distance
                }.maxByOrNull { it.second }
                ?: (0 to 0.0)

        // If max distance is greater than epsilon, recursively simplify
        return if (maxDistance > epsilon) {
            // Split at the point of maximum distance
            val left = douglasPeucker(points.subList(0, maxIndex + 1), epsilon)
            val right = douglasPeucker(points.subList(maxIndex, points.size), epsilon)

            // Combine results (remove duplicate middle point)
            left.dropLast(1) + right
        } else {
            // All points are within epsilon, return only endpoints
            listOf(start, end)
        }
    }

    /**
     * Calculate perpendicular distance from a point to a line segment. Uses proper spherical geometry for accuracy at
     * all latitudes.
     *
     * @param point The point to measure from
     * @param lineStart Start of the line segment
     * @param lineEnd End of the line segment
     * @return Distance in meters
     */
    private fun perpendicularDistance(
        point: Coordinate,
        lineStart: Coordinate,
        lineEnd: Coordinate
    ): Double {
        // Handle case where start and end are the same point
        if (lineStart.latitude == lineEnd.latitude && lineStart.longitude == lineEnd.longitude) {
            return haversineDistance(point, lineStart)
        }

        // Use cross-track distance formula for spherical geometry
        // This is accurate at all latitudes unlike the previous degree-based approximation

        val R = 6371000.0 // Earth radius in meters

        val lat1 = (lineStart.latitude * PI / 180.0)
        val lon1 = (lineStart.longitude * PI / 180.0)
        val lat2 = (lineEnd.latitude * PI / 180.0)
        val lon2 = (lineEnd.longitude * PI / 180.0)
        val lat3 = (point.latitude * PI / 180.0)
        val lon3 = (point.longitude * PI / 180.0)

        // Distance from start to point
        val dist13 = haversineDistance(lineStart, point) / R

        // Bearing from start to end
        val bearing12 =
            atan2(
                sin(lon2 - lon1) * cos(lat2),
                cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(lon2 - lon1)
            )

        // Bearing from start to point
        val bearing13 =
            atan2(
                sin(lon3 - lon1) * cos(lat3),
                cos(lat1) * sin(lat3) - sin(lat1) * cos(lat3) * cos(lon3 - lon1)
            )

        // Cross-track distance (perpendicular distance to great circle)
        val crossTrackDistance = abs(asin(sin(dist13) * sin(bearing13 - bearing12))) * R

        return crossTrackDistance
    }

    /**
     * Calculate Haversine distance between two coordinates.
     *
     * @return Distance in meters
     */
    private fun haversineDistance(
        c1: Coordinate,
        c2: Coordinate
    ): Double {
        val earthRadiusMeters = 6371000.0

        val dLat = (c2.latitude - c1.latitude * PI / 180.0)
        val dLon = (c2.longitude - c1.longitude * PI / 180.0)

        val a =
            sin(dLat / 2).pow(2) +
                cos((c1.latitude * PI / 180.0)) * cos((c2.latitude * PI / 180.0)) *
                sin(dLon / 2).pow(2)

        val c = 2 * asin(sqrt(a))

        return earthRadiusMeters * c
    }
}
