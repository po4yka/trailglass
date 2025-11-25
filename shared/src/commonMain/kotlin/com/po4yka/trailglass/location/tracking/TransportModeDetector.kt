package com.po4yka.trailglass.location.tracking

import com.po4yka.trailglass.domain.model.Location
import com.po4yka.trailglass.domain.model.TransportType
import com.po4yka.trailglass.logging.logger
import com.po4yka.trailglass.util.distanceMetersTo
import kotlin.math.pow

/**
 * Detects mode of transport based on location patterns.
 *
 * Uses speed and distance patterns to infer transport mode:
 * - WALK: 0-7 km/h
 * - BIKE: 7-25 km/h
 * - CAR: 25-120 km/h
 * - TRAIN: 40-200 km/h (consistent speed)
 * - PLANE: >200 km/h or high altitude changes
 */
class TransportModeDetector {
    private val logger = logger()

    companion object {
        // Speed thresholds in km/h
        private const val WALK_MAX_SPEED = 7.0
        private const val BIKE_MAX_SPEED = 25.0
        private const val CAR_MAX_SPEED = 120.0
        private const val TRAIN_MIN_SPEED = 40.0
        private const val TRAIN_MAX_SPEED = 200.0
        private const val PLANE_MIN_SPEED = 200.0

        // Minimum samples for reliable detection
        private const val MIN_SAMPLES_FOR_DETECTION = 5

        // Altitude change threshold for plane detection (meters)
        private const val PLANE_ALTITUDE_THRESHOLD = 1000.0
    }

    private val recentLocations = mutableListOf<Location>()

    /**
     * Process a new location and detect transport mode.
     *
     * @param location New location to process
     * @return Detected transport mode, or null if not enough data
     */
    fun detectTransportMode(location: Location): TransportType? {
        recentLocations.add(location)

        // Keep only recent samples
        if (recentLocations.size > MIN_SAMPLES_FOR_DETECTION) {
            recentLocations.removeAt(0)
        }

        if (recentLocations.size < MIN_SAMPLES_FOR_DETECTION) {
            return null // Not enough data
        }

        // Calculate speeds between consecutive points
        val speeds = mutableListOf<Double>()
        for (i in 1 until recentLocations.size) {
            val prev = recentLocations[i - 1]
            val curr = recentLocations[i]

            val distance = prev.coordinate.distanceMetersTo(curr.coordinate)
            val timeDiffSeconds =
                (
                    curr.timestamp.toEpochMilliseconds() -
                        prev.timestamp.toEpochMilliseconds()
                ) / 1000.0

            if (timeDiffSeconds > 0) {
                val speedKmh = (distance / timeDiffSeconds) * 3.6 // m/s to km/h
                speeds.add(speedKmh)
            }
        }

        if (speeds.isEmpty()) {
            return null
        }

        val avgSpeed = speeds.average()
        val maxSpeed = speeds.maxOrNull() ?: 0.0
        val speedVariance = calculateVariance(speeds)

        // Check for altitude changes (plane detection)
        val altitudeChange = checkAltitudeChange()

        // Detect transport mode
        val mode =
            when {
                // Plane: High speed or significant altitude change
                avgSpeed >= PLANE_MIN_SPEED || altitudeChange >= PLANE_ALTITUDE_THRESHOLD -> {
                    TransportType.PLANE
                }

                // Train: High consistent speed with low variance
                avgSpeed >= TRAIN_MIN_SPEED && avgSpeed <= TRAIN_MAX_SPEED && speedVariance < 100 -> {
                    TransportType.TRAIN
                }

                // Car: Medium to high speed
                avgSpeed > BIKE_MAX_SPEED && avgSpeed <= CAR_MAX_SPEED -> {
                    TransportType.CAR
                }

                // Bike: Low to medium speed
                avgSpeed > WALK_MAX_SPEED && avgSpeed <= BIKE_MAX_SPEED -> {
                    TransportType.BIKE
                }

                // Walk: Low speed
                avgSpeed <= WALK_MAX_SPEED -> {
                    TransportType.WALK
                }

                else -> TransportType.UNKNOWN
            }

        logger.debug { "Transport mode detected: $mode (avg speed: ${avgSpeed.toInt()} km/h)" }
        return mode
    }

    /** Reset detector state. */
    fun reset() {
        recentLocations.clear()
        logger.debug { "Transport mode detector reset" }
    }

    /** Calculate variance of speeds. */
    private fun calculateVariance(speeds: List<Double>): Double {
        val mean = speeds.average()
        val squaredDiffs = speeds.map { (it - mean).pow(2) }
        return squaredDiffs.average()
    }

    /** Check for significant altitude changes (plane detection). */
    private fun checkAltitudeChange(): Double {
        if (recentLocations.size < 2) return 0.0

        val altitudes = recentLocations.mapNotNull { it.altitude }
        if (altitudes.size < 2) return 0.0

        val maxAltitude = altitudes.maxOrNull() ?: 0.0
        val minAltitude = altitudes.minOrNull() ?: 0.0

        return maxAltitude - minAltitude
    }
}
