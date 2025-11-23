package com.po4yka.trailglass.domain.algorithm

import com.po4yka.trailglass.domain.model.Coordinate
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.sin
import kotlin.math.tan

// Extension functions for degrees/radians conversion
private fun Double.toRadians(): Double = this * PI / 180.0

private fun Double.toDegrees(): Double = this * 180.0 / PI

/** Algorithm for calculating bearing (direction) between two geographic coordinates. */
interface BearingAlgorithm {
    /**
     * Calculate bearing from one coordinate to another in degrees (0-360). 0° = North, 90° = East, 180° = South, 270° =
     * West
     */
    fun calculate(
        from: Coordinate,
        to: Coordinate
    ): Double

    /** Calculate bearing between two lat/lon pairs in degrees (0-360). */
    fun calculate(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double
}

/** Available bearing calculation algorithms. */
enum class BearingAlgorithmType {
    /**
     * Initial bearing (forward azimuth) - direction at start point. Most commonly used, represents compass heading at
     * departure.
     */
    INITIAL,

    /**
     * Final bearing (reverse azimuth) - direction at end point. Useful for arrival headings, can differ significantly
     * from initial bearing.
     */
    FINAL,

    /**
     * Rhumb line (constant bearing) - maintains same compass heading. Not the shortest path but simpler navigation.
     * Spirals toward poles on long distances.
     */
    RHUMB_LINE
}

/**
 * Initial bearing calculation - direction at the start point along great circle. Most commonly used for navigation and
 * visualization.
 */
class InitialBearing : BearingAlgorithm {
    override fun calculate(
        from: Coordinate,
        to: Coordinate
    ): Double = calculate(from.latitude, from.longitude, to.latitude, to.longitude)

    override fun calculate(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val phi1 = lat1.toRadians()
        val phi2 = lat2.toRadians()
        val deltaLambda = (lon2 - lon1).toRadians()

        val y = sin(deltaLambda) * cos(phi2)
        val x =
            cos(phi1) * sin(phi2) -
                sin(phi1) * cos(phi2) * cos(deltaLambda)

        val theta = atan2(y, x)
        return (theta.toDegrees() + 360) % 360
    }
}

/**
 * Final bearing calculation - direction at the end point along great circle. Useful for determining arrival heading.
 */
class FinalBearing : BearingAlgorithm {
    override fun calculate(
        from: Coordinate,
        to: Coordinate
    ): Double = calculate(from.latitude, from.longitude, to.latitude, to.longitude)

    override fun calculate(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        // Final bearing from A to B is the reverse of initial bearing from B to A
        val initialBearing = InitialBearing().calculate(lat2, lon2, lat1, lon1)
        return (initialBearing + 180) % 360
    }
}

/**
 * Rhumb line bearing - constant compass bearing throughout the journey. Follows a line of constant bearing (loxodrome),
 * not the shortest path. Easier for navigation but longer distance than great circle.
 */
class RhumbLineBearing : BearingAlgorithm {
    override fun calculate(
        from: Coordinate,
        to: Coordinate
    ): Double = calculate(from.latitude, from.longitude, to.latitude, to.longitude)

    override fun calculate(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val phi1 = (lat1).toRadians()
        val phi2 = (lat2).toRadians()
        var deltaLambda = (lon2 - lon1).toRadians()

        // Normalize longitude difference
        if (abs(deltaLambda) > PI) {
            deltaLambda =
                if (deltaLambda > 0) {
                    -(2 * PI - deltaLambda)
                } else {
                    (2 * PI + deltaLambda)
                }
        }

        val deltaPsi = ln(tan(phi2 / 2 + PI / 4) / tan(phi1 / 2 + PI / 4))
        val theta = atan2(deltaLambda, deltaPsi)

        return ((theta).toDegrees() + 360) % 360
    }
}

/** Factory for creating bearing algorithm instances. */
object BearingAlgorithmFactory {
    fun create(type: BearingAlgorithmType): BearingAlgorithm =
        when (type) {
            BearingAlgorithmType.INITIAL -> InitialBearing()
            BearingAlgorithmType.FINAL -> FinalBearing()
            BearingAlgorithmType.RHUMB_LINE -> RhumbLineBearing()
        }
}
