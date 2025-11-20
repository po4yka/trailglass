package com.po4yka.trailglass.domain.model

import kotlinx.serialization.Serializable

/**
 * Represents a geographic location with optional altitude and accuracy.
 */
import kotlinx.datetime.Instant

/**
 * Represents a geographic location with optional altitude and accuracy.
 */
@Serializable
data class Location(
    val coordinate: Coordinate,
    val timestamp: Instant,
    val altitude: Double? = null,
    val accuracy: Double? = null,
    val speed: Double? = null,
    val bearing: Double? = null
) {
    // Convenience properties for backward compatibility or ease of use
    val latitude: Double get() = coordinate.latitude
    val longitude: Double get() = coordinate.longitude
    val time: Long get() = timestamp.toEpochMilliseconds()

    constructor(
        latitude: Double,
        longitude: Double,
        time: Long? = null,
        altitude: Double? = null,
        accuracy: Double? = null,
        speed: Double? = null,
        bearing: Double? = null
    ) : this(
        coordinate = Coordinate(latitude, longitude),
        timestamp = time?.let { Instant.fromEpochMilliseconds(it) } ?: kotlinx.datetime.Clock.System.now(),
        altitude = altitude,
        accuracy = accuracy,
        speed = speed,
        bearing = bearing
    )
}
