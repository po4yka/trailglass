package com.po4yka.trailglass.domain.model

import kotlinx.datetime.Instant
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * A geographic region (geofence) that triggers notifications when the user enters or exits.
 * Regions are user-defined circular areas of interest.
 */
data class Region(
    val id: String,
    val userId: String,
    val name: String,
    val description: String? = null,
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Int,
    val notificationsEnabled: Boolean = true,
    val createdAt: Instant,
    val updatedAt: Instant,
    val enterCount: Int = 0,
    val lastEnterTime: Instant? = null,
    val lastExitTime: Instant? = null
) {
    val coordinate: Coordinate
        get() = Coordinate(latitude = latitude, longitude = longitude)

    /**
     * Check if a given coordinate is within this region.
     * Uses the Haversine formula to calculate distance.
     *
     * @param lat The latitude to check
     * @param lon The longitude to check
     * @return true if the coordinate is within the region
     */
    fun contains(lat: Double, lon: Double): Boolean {
        val distance = distanceFrom(lat, lon)
        return distance <= radiusMeters
    }

    /**
     * Calculate the distance in meters from this region's center to a given coordinate.
     * Uses the Haversine formula.
     *
     * @param lat The latitude to measure to
     * @param lon The longitude to measure to
     * @return Distance in meters
     */
    fun distanceFrom(lat: Double, lon: Double): Double {
        val earthRadiusMeters = 6371000.0

        val lat1Rad = Math.toRadians(latitude)
        val lat2Rad = Math.toRadians(lat)
        val deltaLatRad = Math.toRadians(lat - latitude)
        val deltaLonRad = Math.toRadians(lon - longitude)

        val a = sin(deltaLatRad / 2).pow(2) +
            cos(lat1Rad) * cos(lat2Rad) *
            sin(deltaLonRad / 2).pow(2)

        val c = 2 * asin(sqrt(a))

        return earthRadiusMeters * c
    }

    /**
     * Whether this region has been entered at least once.
     */
    val hasBeenEntered: Boolean
        get() = enterCount > 0 && lastEnterTime != null

    companion object {
        const val MIN_RADIUS_METERS = 50
        const val MAX_RADIUS_METERS = 2000
        const val DEFAULT_RADIUS_METERS = 200
    }
}

/**
 * Represents a region transition event (enter or exit).
 * Used to track when a user enters or exits a region.
 */
data class RegionTransition(
    val regionId: String,
    val regionName: String,
    val transitionType: TransitionType,
    val timestamp: Long,
    val latitude: Double,
    val longitude: Double
)

/**
 * Type of region transition.
 */
enum class TransitionType {
    ENTER,
    EXIT
}
