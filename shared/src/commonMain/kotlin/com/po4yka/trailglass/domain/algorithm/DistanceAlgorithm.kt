package com.po4yka.trailglass.domain.algorithm

import com.po4yka.trailglass.domain.model.Coordinate
import kotlin.math.*

/**
 * Algorithm for calculating distance between two geographic coordinates.
 */
interface DistanceAlgorithm {
    /**
     * Calculate distance between two coordinates in meters.
     */
    fun calculate(from: Coordinate, to: Coordinate): Double

    /**
     * Calculate distance between two lat/lon pairs in meters.
     */
    fun calculate(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double
}

/**
 * Available distance calculation algorithms.
 */
enum class DistanceAlgorithmType {
    /**
     * Haversine formula - accurate for most purposes, assumes spherical Earth.
     * Fast and accurate to within 0.5% for most distances.
     */
    HAVERSINE,

    /**
     * Vincenty formula - most accurate, accounts for Earth's ellipsoidal shape.
     * Slower but accurate to within 0.5mm for any distance.
     */
    VINCENTY,

    /**
     * Simple Euclidean approximation - fastest but least accurate.
     * Only suitable for very short distances (<1km).
     * Error increases significantly with distance and latitude.
     */
    SIMPLE
}

/**
 * Haversine distance calculation - accurate to ~0.5% for most distances.
 * Assumes Earth is a perfect sphere with radius 6371km.
 */
class HaversineDistance : DistanceAlgorithm {
    companion object {
        private const val EARTH_RADIUS_METERS = 6371000.0
    }

    override fun calculate(from: Coordinate, to: Coordinate): Double {
        return calculate(from.latitude, from.longitude, to.latitude, to.longitude)
    }

    override fun calculate(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return EARTH_RADIUS_METERS * c
    }
}

/**
 * Vincenty distance calculation - most accurate, accounts for ellipsoidal Earth.
 * Accurate to within 0.5mm using WGS-84 ellipsoid parameters.
 */
class VincentyDistance : DistanceAlgorithm {
    companion object {
        // WGS-84 ellipsoid parameters
        private const val SEMI_MAJOR_AXIS = 6378137.0  // a (equatorial radius)
        private const val SEMI_MINOR_AXIS = 6356752.314245  // b (polar radius)
        private const val FLATTENING = 1.0 / 298.257223563  // f
        private const val MAX_ITERATIONS = 200
        private const val CONVERGENCE_THRESHOLD = 1e-12
    }

    override fun calculate(from: Coordinate, to: Coordinate): Double {
        return calculate(from.latitude, from.longitude, to.latitude, to.longitude)
    }

    override fun calculate(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        // Handle identical points
        if (lat1 == lat2 && lon1 == lon2) return 0.0

        val phi1 = Math.toRadians(lat1)
        val phi2 = Math.toRadians(lat2)
        val lambda1 = Math.toRadians(lon1)
        val lambda2 = Math.toRadians(lon2)

        val U1 = atan((1 - FLATTENING) * tan(phi1))
        val U2 = atan((1 - FLATTENING) * tan(phi2))
        val L = lambda2 - lambda1

        val sinU1 = sin(U1)
        val cosU1 = cos(U1)
        val sinU2 = sin(U2)
        val cosU2 = cos(U2)

        var lambda = L
        var lambdaP: Double
        var iterLimit = MAX_ITERATIONS
        var cosSqAlpha: Double
        var sinSigma: Double
        var cos2SigmaM: Double
        var cosSigma: Double
        var sigma: Double

        do {
            val sinLambda = sin(lambda)
            val cosLambda = cos(lambda)
            sinSigma = sqrt(
                (cosU2 * sinLambda).pow(2) +
                (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda).pow(2)
            )

            if (sinSigma == 0.0) return 0.0  // Co-incident points

            cosSigma = sinU1 * sinU2 + cosU1 * cosU2 * cosLambda
            sigma = atan2(sinSigma, cosSigma)
            val sinAlpha = cosU1 * cosU2 * sinLambda / sinSigma
            cosSqAlpha = 1 - sinAlpha.pow(2)
            cos2SigmaM = if (cosSqAlpha != 0.0) {
                cosSigma - 2 * sinU1 * sinU2 / cosSqAlpha
            } else {
                0.0  // Equatorial line
            }

            val C = FLATTENING / 16 * cosSqAlpha * (4 + FLATTENING * (4 - 3 * cosSqAlpha))
            lambdaP = lambda
            lambda = L + (1 - C) * FLATTENING * sinAlpha * (
                sigma + C * sinSigma * (
                    cos2SigmaM + C * cosSigma * (-1 + 2 * cos2SigmaM.pow(2))
                )
            )
        } while (abs(lambda - lambdaP) > CONVERGENCE_THRESHOLD && --iterLimit > 0)

        if (iterLimit == 0) {
            // Formula failed to converge, fall back to Haversine
            return HaversineDistance().calculate(lat1, lon1, lat2, lon2)
        }

        val uSq = cosSqAlpha * (SEMI_MAJOR_AXIS.pow(2) - SEMI_MINOR_AXIS.pow(2)) / SEMI_MINOR_AXIS.pow(2)
        val A = 1 + uSq / 16384 * (4096 + uSq * (-768 + uSq * (320 - 175 * uSq)))
        val B = uSq / 1024 * (256 + uSq * (-128 + uSq * (74 - 47 * uSq)))
        val deltaSigma = B * sinSigma * (
            cos2SigmaM + B / 4 * (
                cosSigma * (-1 + 2 * cos2SigmaM.pow(2)) -
                B / 6 * cos2SigmaM * (-3 + 4 * sinSigma.pow(2)) * (-3 + 4 * cos2SigmaM.pow(2))
            )
        )

        return SEMI_MINOR_AXIS * A * (sigma - deltaSigma)
    }
}

/**
 * Simple Euclidean distance approximation.
 * Fast but only accurate for very short distances (<1km).
 * Error increases with distance and latitude.
 */
class SimpleDistance : DistanceAlgorithm {
    companion object {
        private const val METERS_PER_DEGREE_LAT = 111000.0  // Approximate
    }

    override fun calculate(from: Coordinate, to: Coordinate): Double {
        return calculate(from.latitude, from.longitude, to.latitude, to.longitude)
    }

    override fun calculate(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val avgLat = (lat1 + lat2) / 2.0
        val metersPerDegreeLon = METERS_PER_DEGREE_LAT * cos(Math.toRadians(avgLat))

        val dLat = (lat2 - lat1) * METERS_PER_DEGREE_LAT
        val dLon = (lon2 - lon1) * metersPerDegreeLon

        return sqrt(dLat.pow(2) + dLon.pow(2))
    }
}

/**
 * Factory for creating distance algorithm instances.
 */
object DistanceAlgorithmFactory {
    fun create(type: DistanceAlgorithmType): DistanceAlgorithm {
        return when (type) {
            DistanceAlgorithmType.HAVERSINE -> HaversineDistance()
            DistanceAlgorithmType.VINCENTY -> VincentyDistance()
            DistanceAlgorithmType.SIMPLE -> SimpleDistance()
        }
    }
}
