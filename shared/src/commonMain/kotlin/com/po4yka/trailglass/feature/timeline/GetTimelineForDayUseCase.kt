package com.po4yka.trailglass.feature.timeline

import com.po4yka.trailglass.data.repository.PlaceVisitRepository
import com.po4yka.trailglass.data.repository.RouteSegmentRepository
import com.po4yka.trailglass.domain.model.*
import com.po4yka.trailglass.logging.logger
import kotlinx.datetime.*
import me.tatarka.inject.annotations.Inject

/**
 * Use case for getting the timeline for a specific day.
 * Combines place visits and route segments into a chronological timeline.
 */
@Inject
class GetTimelineForDayUseCase(
    private val placeVisitRepository: PlaceVisitRepository,
    private val routeSegmentRepository: RouteSegmentRepository,
    private val timeZone: TimeZone = TimeZone.currentSystemDefault()
) {

    private val logger = logger()

    /**
     * Timeline item for UI display.
     */
    sealed class TimelineItemUI {
        abstract val timestamp: Instant
        abstract val id: String

        data class VisitUI(
            override val id: String,
            override val timestamp: Instant,
            val placeVisit: PlaceVisit
        ) : TimelineItemUI()

        data class RouteUI(
            override val id: String,
            override val timestamp: Instant,
            val routeSegment: RouteSegment
        ) : TimelineItemUI()

        data class DayStartUI(
            override val id: String,
            override val timestamp: Instant
        ) : TimelineItemUI()

        data class DayEndUI(
            override val id: String,
            override val timestamp: Instant
        ) : TimelineItemUI()
    }

    /**
     * Get timeline for a specific day.
     *
     * @param date The date to get timeline for
     * @param userId User ID
     * @return List of timeline items sorted chronologically
     */
    suspend fun execute(date: LocalDate, userId: String): List<TimelineItemUI> {
        logger.debug { "Getting timeline for $date, user: $userId" }

        val dayStart = date.atStartOfDayIn(timeZone)
        val dayEnd = date.plus(1, DateTimeUnit.DAY).atStartOfDayIn(timeZone)

        // Get visits for the day
        val visits = placeVisitRepository.getVisits(userId, dayStart, dayEnd)
        logger.debug { "Found ${visits.size} visits for $date" }

        // Get routes for the day
        val routes = routeSegmentRepository.getRouteSegmentsInRange(userId, dayStart, dayEnd)
        logger.debug { "Found ${routes.size} routes for $date" }

        // Build timeline items
        val items = mutableListOf<TimelineItemUI>()

        // Add day start
        items.add(
            TimelineItemUI.DayStartUI(
                id = "day_start_$date",
                timestamp = dayStart
            )
        )

        // Add visits
        visits.forEach { visit ->
            items.add(
                TimelineItemUI.VisitUI(
                    id = "visit_${visit.id}",
                    timestamp = visit.startTime,
                    placeVisit = visit
                )
            )
        }

        // Add routes
        routes.forEach { route ->
            items.add(
                TimelineItemUI.RouteUI(
                    id = "route_${route.id}",
                    timestamp = route.startTime,
                    routeSegment = route
                )
            )
        }

        // Add day end
        items.add(
            TimelineItemUI.DayEndUI(
                id = "day_end_$date",
                timestamp = dayEnd
            )
        )

        // Sort by timestamp
        val sortedItems = items.sortedBy { it.timestamp }

        logger.info { "Built timeline for $date with ${sortedItems.size} items" }
        return sortedItems
    }
}
