package com.po4yka.trailglass.location

import com.po4yka.trailglass.domain.model.Coordinate
import com.po4yka.trailglass.domain.model.LocationSample
import com.po4yka.trailglass.logging.logger
import kotlin.math.*

/**
 * Simplifies paths using the Douglas-Peucker algorithm.
 * Reduces the number of points while preserving the shape of the path.
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
        logger.debug { "Simplified path from ${points.size} to ${simplified.size} points (${reduction}% reduction)" }

        return simplified
    }

    /**
     * Douglas-Peucker algorithm implementation.
     * Recursively simplifies a polyline by removing points that deviate
     * less than epsilon from the line between endpoints.
     */
    private fun douglasPeucker(points: List<Coordinate>, epsilon: Double): List<Coordinate> {
        if (points.size < 3) return points

        // Find the point with the maximum distance from the line segment
        var maxDistance = 0.0
        var maxIndex = 0

        val start = points.first()
        val end = points.last()

        for (i in 1 until points.lastIndex) {
            val distance = perpendicularDistance(points[i], start, end)
            if (distance > maxDistance) {
                maxDistance = distance
                maxIndex = i
            }
        }

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
     * Calculate perpendicular distance from a point to a line segment.
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
        // Convert to Cartesian coordinates for easier calculation
        val x0 = point.latitude
        val y0 = point.longitude
        val x1 = lineStart.latitude
        val y1 = lineStart.longitude
        val x2 = lineEnd.latitude
        val y2 = lineEnd.longitude

        // Handle case where start and end are the same point
        if (x1 == x2 && y1 == y2) {
            return haversineDistance(point, lineStart)
        }

        // Calculate perpendicular distance using cross product
        val numerator = abs((y2 - y1) * x0 - (x2 - x1) * y0 + x2 * y1 - y2 * x1)
        val denominator = sqrt((y2 - y1).pow(2) + (x2 - x1).pow(2))

        // Convert to meters (approximate)
        val distanceDegrees = numerator / denominator
        return distanceDegrees * 111320.0 // degrees to meters at equator
    }

    /**
     * Calculate Haversine distance between two coordinates.
     *
     * @return Distance in meters
     */
    private fun haversineDistance(c1: Coordinate, c2: Coordinate): Double {
        val earthRadiusMeters = 6371000.0

        val dLat = Math.toRadians(c2.latitude - c1.latitude)
        val dLon = Math.toRadians(c2.longitude - c1.longitude)

        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(c1.latitude)) * cos(Math.toRadians(c2.latitude)) *
                sin(dLon / 2).pow(2)

        val c = 2 * asin(sqrt(a))

        return earthRadiusMeters * c
    }
}
