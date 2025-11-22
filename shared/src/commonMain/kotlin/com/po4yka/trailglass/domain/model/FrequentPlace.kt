package com.po4yka.trailglass.domain.model

import kotlinx.datetime.Instant
import kotlin.time.Duration

/**
 * Represents a frequently visited place, aggregating multiple PlaceVisits.
 * This is used to identify and track places like home, work, favorite cafe, etc.
 */
data class FrequentPlace(
    val id: String,
    val centerLatitude: Double,
    val centerLongitude: Double,
    val radiusMeters: Double = 50.0,
    // Place information
    val name: String? = null,
    val address: String? = null,
    val city: String? = null,
    val countryCode: String? = null,
    // Categorization
    val category: PlaceCategory = PlaceCategory.OTHER,
    val categoryConfidence: CategoryConfidence = CategoryConfidence.LOW,
    val significance: PlaceSignificance = PlaceSignificance.RARE,
    // Visit statistics
    val visitCount: Int = 0,
    val totalDuration: Duration = Duration.ZERO,
    val firstVisitTime: Instant? = null,
    val lastVisitTime: Instant? = null,
    // User customization
    val userLabel: String? = null,
    val userNotes: String? = null,
    val isFavorite: Boolean = false,
    // Metadata
    val userId: String,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    /**
     * Average duration per visit.
     */
    val averageDuration: Duration
        get() = if (visitCount > 0) totalDuration / visitCount else Duration.ZERO

    /**
     * Display name for this frequent place.
     * Priority: userLabel > name > address > coordinates
     */
    val displayName: String
        get() =
            userLabel
                ?: name
                ?: address
                ?: "${centerLatitude.roundToDecimals(4)}, ${centerLongitude.roundToDecimals(4)}"

    /**
     * Whether this place is significant (visited frequently).
     */
    val isSignificant: Boolean
        get() =
            significance == PlaceSignificance.PRIMARY ||
                significance == PlaceSignificance.FREQUENT

    private fun Double.roundToDecimals(decimals: Int): String {
        val multiplier =
            when (decimals) {
                1 -> 10.0
                2 -> 100.0
                3 -> 1000.0
                4 -> 10000.0
                else -> 10000.0
            }
        val rounded = kotlin.math.round(this * multiplier) / multiplier
        return rounded.toString()
    }
}
