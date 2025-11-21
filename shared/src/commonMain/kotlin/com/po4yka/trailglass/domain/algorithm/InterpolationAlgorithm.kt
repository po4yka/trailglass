package com.po4yka.trailglass.domain.algorithm

import com.po4yka.trailglass.domain.model.Coordinate
import kotlin.math.*

/**
 * Algorithm for interpolating between two geographic coordinates.
 */
interface InterpolationAlgorithm {
    /**
     * Interpolate between two coordinates.
     * @param from Starting coordinate
     * @param to Ending coordinate
     * @param fraction Interpolation fraction (0.0 to 1.0)
     * @return Interpolated coordinate
     */
    fun interpolate(from: Coordinate, to: Coordinate, fraction: Double): Coordinate

    /**
     * Generate multiple intermediate points along the path.
     * @param from Starting coordinate
     * @param to Ending coordinate
     * @param steps Number of intermediate steps (excluding start and end)
     * @return List of coordinates including start, intermediates, and end
     */
    fun generatePath(from: Coordinate, to: Coordinate, steps: Int): List<Coordinate> {
        if (steps <= 0) return listOf(from, to)

        return buildList {
            add(from)
            for (i in 1..steps) {
                val fraction = i.toDouble() / (steps + 1)
                add(interpolate(from, to, fraction))
            }
            add(to)
        }
    }
}

/**
 * Available interpolation algorithms.
 */
enum class InterpolationAlgorithmType {
    /**
     * Linear interpolation - straight line in lat/lon space.
     * Fast but not geographically accurate, especially over long distances.
     * Good for very short distances or simple animations.
     */
    LINEAR,

    /**
     * Spherical Linear Interpolation (SLERP) - great circle path.
     * Follows the shortest path on a sphere between two points.
     * Most accurate for geographic paths, used for smooth camera animations.
     */
    SLERP,

    /**
     * Cubic Hermite interpolation - smooth curved path with control points.
     * Creates smooth, aesthetically pleasing curves.
     * Useful for animated transitions with easing.
     */
    CUBIC
}

/**
 * Linear interpolation - simple straight line in coordinate space.
 * Fast but not geographically accurate over long distances.
 */
class LinearInterpolation : InterpolationAlgorithm {
    override fun interpolate(from: Coordinate, to: Coordinate, fraction: Double): Coordinate {
        val clampedFraction = fraction.coerceIn(0.0, 1.0)
        return Coordinate(
            latitude = from.latitude + (to.latitude - from.latitude) * clampedFraction,
            longitude = from.longitude + (to.longitude - from.longitude) * clampedFraction
        )
    }
}

/**
 * Spherical Linear Interpolation (SLERP) - great circle path on a sphere.
 * Most accurate for geographic interpolation, follows shortest path.
 */
class SphericalInterpolation : InterpolationAlgorithm {
    override fun interpolate(from: Coordinate, to: Coordinate, fraction: Double): Coordinate {
        val clampedFraction = fraction.coerceIn(0.0, 1.0)

        // Convert to radians
        val lat1 = Math.toRadians(from.latitude)
        val lon1 = Math.toRadians(from.longitude)
        val lat2 = Math.toRadians(to.latitude)
        val lon2 = Math.toRadians(to.longitude)

        // Convert to Cartesian coordinates
        val x1 = cos(lat1) * cos(lon1)
        val y1 = cos(lat1) * sin(lon1)
        val z1 = sin(lat1)

        val x2 = cos(lat2) * cos(lon2)
        val y2 = cos(lat2) * sin(lon2)
        val z2 = sin(lat2)

        // Calculate angle between vectors
        val dot = x1 * x2 + y1 * y2 + z1 * z2
        val theta = acos(dot.coerceIn(-1.0, 1.0))

        // Handle co-incident points
        if (abs(theta) < 1e-10) {
            return from
        }

        // SLERP formula
        val sinTheta = sin(theta)
        val a = sin((1.0 - clampedFraction) * theta) / sinTheta
        val b = sin(clampedFraction * theta) / sinTheta

        // Interpolated Cartesian coordinates
        val x = a * x1 + b * x2
        val y = a * y1 + b * y2
        val z = a * z1 + b * z2

        // Convert back to geographic coordinates
        val lat = atan2(z, sqrt(x * x + y * y))
        val lon = atan2(y, x)

        return Coordinate(
            latitude = Math.toDegrees(lat),
            longitude = Math.toDegrees(lon)
        )
    }
}

/**
 * Cubic Hermite interpolation - smooth curve with easing.
 * Creates aesthetically pleasing curves for animations.
 */
class CubicInterpolation : InterpolationAlgorithm {
    override fun interpolate(from: Coordinate, to: Coordinate, fraction: Double): Coordinate {
        val clampedFraction = fraction.coerceIn(0.0, 1.0)

        // Hermite basis functions for smooth interpolation
        val t = clampedFraction
        val t2 = t * t
        val t3 = t2 * t

        // Cubic Hermite curve: smooth acceleration and deceleration
        val h00 = 2 * t3 - 3 * t2 + 1  // Starting point weight
        val h10 = t3 - 2 * t2 + t      // Starting tangent weight
        val h01 = -2 * t3 + 3 * t2     // Ending point weight
        val h11 = t3 - t2              // Ending tangent weight

        // Calculate tangent vectors (velocity at endpoints)
        // Use direction to next/previous point scaled by distance
        val deltaLat = to.latitude - from.latitude
        val deltaLon = to.longitude - from.longitude

        // Tangent at start: direction of movement
        val m0Lat = deltaLat
        val m0Lon = deltaLon

        // Tangent at end: direction of movement
        val m1Lat = deltaLat
        val m1Lon = deltaLon

        // Apply Hermite interpolation
        val lat = h00 * from.latitude + h10 * m0Lat + h01 * to.latitude + h11 * m1Lat
        val lon = h00 * from.longitude + h10 * m0Lon + h01 * to.longitude + h11 * m1Lon

        return Coordinate(
            latitude = lat,
            longitude = lon
        )
    }
}

/**
 * Factory for creating interpolation algorithm instances.
 */
object InterpolationAlgorithmFactory {
    fun create(type: InterpolationAlgorithmType): InterpolationAlgorithm {
        return when (type) {
            InterpolationAlgorithmType.LINEAR -> LinearInterpolation()
            InterpolationAlgorithmType.SLERP -> SphericalInterpolation()
            InterpolationAlgorithmType.CUBIC -> CubicInterpolation()
        }
    }
}
