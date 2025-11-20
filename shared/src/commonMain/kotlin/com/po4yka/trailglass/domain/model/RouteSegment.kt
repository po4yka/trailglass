package com.po4yka.trailglass.domain.model

import kotlinx.datetime.Instant

/**
 * Transport type inferred from speed and movement patterns.
 */
enum class TransportType {
    WALK,
    BIKE,
    CAR,
    TRAIN,
    PLANE,
    BOAT,
    UNKNOWN;

    companion object {
        /**
         * Infer transport type from average speed.
         * @param speedMps Speed in meters per second
         */
        fun inferFromSpeed(speedMps: Double?): TransportType {
            if (speedMps == null || speedMps < 0) return UNKNOWN

            return when {
                speedMps < 2.0 -> WALK          // < 7.2 km/h
                speedMps < 7.0 -> BIKE          // 7.2 - 25 km/h
                speedMps < 50.0 -> CAR          // 25 - 180 km/h
                speedMps < 100.0 -> TRAIN       // 180 - 360 km/h
                else -> PLANE                    // > 360 km/h
            }
        }
    }
}

/**
 * A route segment between two locations (places or day boundaries).
 * Represents movement from point A to point B.
 */
data class RouteSegment(
    val id: String,
    val startTime: Instant,
    val endTime: Instant,
    val fromPlaceVisitId: String?,
    val toPlaceVisitId: String?,
    val locationSampleIds: List<String> = emptyList(),
    val simplifiedPath: List<Coordinate> = emptyList(),
    val transportType: TransportType = TransportType.UNKNOWN,
    val distanceMeters: Double = 0.0,
    val averageSpeedMps: Double? = null
)
