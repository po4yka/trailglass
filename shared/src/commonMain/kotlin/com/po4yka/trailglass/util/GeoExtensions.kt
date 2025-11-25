package com.po4yka.trailglass.util

import com.po4yka.trailglass.domain.model.Coordinate
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/** Earth's radius in meters for geographic calculations. */
private const val EARTH_RADIUS_METERS = 6371000.0

/**
 * Calculate the Haversine distance between this coordinate and another.
 *
 * @param other The destination coordinate
 * @return Distance in meters
 */
fun Coordinate.distanceMetersTo(other: Coordinate): Double {
    val dLat = (other.latitude - latitude).toRadians()
    val dLon = (other.longitude - longitude).toRadians()

    val a = sin(dLat / 2).pow(2) +
        cos(latitude.toRadians()) * cos(other.latitude.toRadians()) *
        sin(dLon / 2).pow(2)

    val c = 2 * asin(sqrt(a))
    return EARTH_RADIUS_METERS * c
}

/**
 * Calculate the initial bearing from this coordinate to another.
 *
 * @param other The destination coordinate
 * @return Bearing in degrees (0-360)
 */
fun Coordinate.bearingTo(other: Coordinate): Double {
    val lat1 = latitude.toRadians()
    val lat2 = other.latitude.toRadians()
    val dLon = (other.longitude - longitude).toRadians()

    val y = sin(dLon) * cos(lat2)
    val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)

    val bearing = atan2(y, x).toDegrees()
    return (bearing + 360) % 360
}

/**
 * Check if this coordinate is within a given distance of another.
 *
 * @param other The coordinate to check against
 * @param radiusMeters The maximum distance in meters
 * @return True if within radius
 */
fun Coordinate.isWithinRadius(other: Coordinate, radiusMeters: Double): Boolean =
    distanceMetersTo(other) <= radiusMeters

/**
 * Calculate the center point of multiple coordinates.
 *
 * @return The geographic center coordinate
 */
fun List<Coordinate>.center(): Coordinate? {
    if (isEmpty()) return null
    if (size == 1) return first()

    var x = 0.0
    var y = 0.0
    var z = 0.0

    forEach { coord ->
        val lat = coord.latitude.toRadians()
        val lon = coord.longitude.toRadians()

        x += cos(lat) * cos(lon)
        y += cos(lat) * sin(lon)
        z += sin(lat)
    }

    val count = size.toDouble()
    x /= count
    y /= count
    z /= count

    val lon = atan2(y, x)
    val hyp = sqrt(x * x + y * y)
    val lat = atan2(z, hyp)

    return Coordinate(
        latitude = lat.toDegrees(),
        longitude = lon.toDegrees()
    )
}

/**
 * Calculate the bounding box that contains all coordinates.
 *
 * @return Pair of (southwest, northeast) corners, or null if empty
 */
fun List<Coordinate>.boundingBox(): Pair<Coordinate, Coordinate>? {
    if (isEmpty()) return null

    var minLat = Double.MAX_VALUE
    var maxLat = Double.MIN_VALUE
    var minLon = Double.MAX_VALUE
    var maxLon = Double.MIN_VALUE

    forEach { coord ->
        if (coord.latitude < minLat) minLat = coord.latitude
        if (coord.latitude > maxLat) maxLat = coord.latitude
        if (coord.longitude < minLon) minLon = coord.longitude
        if (coord.longitude > maxLon) maxLon = coord.longitude
    }

    return Coordinate(minLat, minLon) to Coordinate(maxLat, maxLon)
}

/** Convert degrees to radians. */
private fun Double.toRadians(): Double = this * PI / 180.0

/** Convert radians to degrees. */
private fun Double.toDegrees(): Double = this * 180.0 / PI
