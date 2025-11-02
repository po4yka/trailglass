package com.po4yka.trailglass.domain.model

import kotlinx.datetime.Instant

/**
 * Raw GPS/network location point recorded by the app.
 * All higher-level timelines are derived from this data.
 */
data class LocationSample(
    val id: String,
    val timestamp: Instant,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Double,
    val speed: Double? = null,
    val bearing: Double? = null,
    val source: LocationSource,
    val tripId: String? = null,
    val uploadedAt: Instant? = null,
    val deviceId: String,
    val userId: String
)

enum class LocationSource {
    GPS,
    NETWORK,
    VISIT,
    SIGNIFICANT_CHANGE
}
