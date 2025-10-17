package com.po4yka.trailglass.domain.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlin.time.Duration

/**
 * A trip or vacation - a logical grouping of movement and visits.
 * Can be auto-detected or manually created.
 */
data class Trip(
    val id: String,
    val name: String? = null,
    val startTime: Instant,
    val endTime: Instant?,
    val primaryCountry: String? = null,
    val isOngoing: Boolean = false,
    val userId: String,

    // Trip statistics
    val totalDistanceMeters: Double = 0.0,
    val visitedPlaceCount: Int = 0,
    val countriesVisited: List<String> = emptyList(),
    val citiesVisited: List<String> = emptyList(),

    // User customization
    val description: String? = null,
    val coverPhotoUri: String? = null,
    val isPublic: Boolean = false,
    val tags: List<String> = emptyList(),

    // Auto-detection metadata
    val isAutoDetected: Boolean = false,
    val detectionConfidence: Float = 0f, // 0.0 to 1.0

    // Metadata
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null
) {
    /**
     * Calculate trip duration.
     */
    val duration: Duration?
        get() = if (endTime != null) endTime - startTime else null

    /**
     * Get display name for the trip.
     * Priority: name > primaryCountry > "Unnamed Trip"
     */
    val displayName: String
        get() = name
            ?: primaryCountry?.let { "$it Trip" }
            ?: "Unnamed Trip"

    /**
     * Get a summary of the trip.
     */
    val summary: String
        get() = buildString {
            if (countriesVisited.isNotEmpty()) {
                append("${countriesVisited.size} ${if (countriesVisited.size == 1) "country" else "countries"}")
            }
            if (visitedPlaceCount > 0) {
                if (isNotEmpty()) append(" • ")
                append("$visitedPlaceCount ${if (visitedPlaceCount == 1) "place" else "places"}")
            }
            if (totalDistanceMeters > 0) {
                if (isNotEmpty()) append(" • ")
                val km = (totalDistanceMeters / 1000).toInt()
                append("$km km")
            }
        }
}

/**
 * A single day within a trip.
 * Contains all timeline items (visits, routes, etc.) for that day.
 */
data class TripDay(
    val id: String,
    val tripId: String,
    val date: LocalDate,
    val items: List<TimelineItem> = emptyList()
)

/**
 * Timeline item - represents an event in a day's timeline.
 */
sealed class TimelineItem {
    abstract val id: String
    abstract val timestamp: Instant

    data class Visit(
        override val id: String,
        override val timestamp: Instant,
        val placeVisit: PlaceVisit
    ) : TimelineItem()

    data class Route(
        override val id: String,
        override val timestamp: Instant,
        val routeSegment: RouteSegment
    ) : TimelineItem()

    data class DayStart(
        override val id: String,
        override val timestamp: Instant
    ) : TimelineItem()

    data class DayEnd(
        override val id: String,
        override val timestamp: Instant
    ) : TimelineItem()
}
