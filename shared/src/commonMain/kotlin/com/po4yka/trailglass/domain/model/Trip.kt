package com.po4yka.trailglass.domain.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

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
    val userId: String
)

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
