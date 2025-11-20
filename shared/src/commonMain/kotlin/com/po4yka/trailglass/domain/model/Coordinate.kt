package com.po4yka.trailglass.domain.model

import kotlinx.serialization.Serializable

/**
 * Represents a geographic coordinate (latitude, longitude).
 */
@Serializable
data class Coordinate(
    val latitude: Double,
    val longitude: Double
)
