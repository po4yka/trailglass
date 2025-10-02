package com.po4yka.trailglass.domain.model

import kotlinx.datetime.Instant

/**
 * A cluster of location points where the user stayed for a noticeable time.
 * Used in timelines and inside Memories.
 */
data class PlaceVisit(
    val id: String,
    val startTime: Instant,
    val endTime: Instant,
    val centerLatitude: Double,
    val centerLongitude: Double,
    val approximateAddress: String? = null,
    val poiName: String? = null,
    val city: String? = null,
    val countryCode: String? = null,
    val locationSampleIds: List<String> = emptyList()
)
