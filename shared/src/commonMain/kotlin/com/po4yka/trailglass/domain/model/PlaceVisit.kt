package com.po4yka.trailglass.domain.model

import kotlinx.datetime.Instant
import kotlin.time.Duration

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
    val locationSampleIds: List<String> = emptyList(),

    // Enhanced fields for categorization
    val category: PlaceCategory = PlaceCategory.OTHER,
    val categoryConfidence: CategoryConfidence = CategoryConfidence.LOW,
    val significance: PlaceSignificance = PlaceSignificance.RARE,

    // User customization
    val userLabel: String? = null,
    val userNotes: String? = null,
    val isFavorite: Boolean = false,

    // Place clustering - links to frequently visited place
    val frequentPlaceId: String? = null,

    // Metadata
    val userId: String? = null,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null
) {
    /**
     * Calculate the duration of this visit.
     */
    val duration: Duration
        get() = endTime - startTime

    /**
     * Convenience property for accessing location as a Coordinate.
     */
    val location: Coordinate
        get() = Coordinate(centerLatitude, centerLongitude)

    /**
     * Get a display name for this place.
     * Priority: userLabel > poiName > approximateAddress > coordinates
     */
    val displayName: String
        get() = userLabel
            ?: poiName
            ?: approximateAddress
            ?: "${centerLatitude.format(4)}, ${centerLongitude.format(4)}"

    /**
     * Get a short description of the visit.
     */
    val shortDescription: String
        get() = buildString {
            if (city != null) {
                append(city)
            } else if (approximateAddress != null) {
                append(approximateAddress)
            } else {
                append("${centerLatitude.format(4)}, ${centerLongitude.format(4)}")
            }
        }

    private fun Double.format(decimals: Int): String {
        // Simple truncation for KMP compatibility
        val str = this.toString()
        val dotIndex = str.indexOf('.')
        if (dotIndex >= 0 && dotIndex + decimals + 1 < str.length) {
            return str.substring(0, dotIndex + decimals + 1)
        }
        return str
    }
}
