package com.po4yka.trailglass.location.trip

import com.po4yka.trailglass.domain.model.*
import com.po4yka.trailglass.logging.logger
import kotlinx.datetime.*

/**
 * Aggregates trips into daily timelines.
 * Builds TripDay objects containing ordered timeline items (visits and routes).
 */
class TripDayAggregator(
    private val timeZone: TimeZone = TimeZone.UTC
) {

    private val logger = logger()

    /**
     * Build daily timelines for a trip.
     *
     * @param trip The trip to aggregate
     * @param visits Place visits within the trip
     * @param routes Route segments within the trip
     * @return List of TripDay objects, one per day
     */
    fun aggregateTripDays(
        trip: Trip,
        visits: List<PlaceVisit>,
        routes: List<RouteSegment>
    ): List<TripDay> {
        logger.info { "Aggregating trip ${trip.id} into daily timelines" }

        // Filter visits and routes to only those in the trip
        val tripVisits = visits.filter { visit ->
            visit.startTime >= trip.startTime &&
            (trip.endTime == null || visit.startTime <= trip.endTime)
        }.sortedBy { it.startTime }

        val tripRoutes = routes.filter { route ->
            route.startTime >= trip.startTime &&
            (trip.endTime == null || route.startTime <= trip.endTime)
        }.sortedBy { it.startTime }

        if (tripVisits.isEmpty() && tripRoutes.isEmpty()) {
            logger.warn { "No visits or routes found for trip ${trip.id}" }
            return emptyList()
        }

        logger.debug { "Building timeline from ${tripVisits.size} visits and ${tripRoutes.size} routes" }

        // Determine all days covered by the trip
        val startDate = trip.startTime.toLocalDateTime(timeZone).date
        val endDate = (trip.endTime ?: trip.startTime).toLocalDateTime(timeZone).date
        val days = generateDayRange(startDate, endDate)

        logger.debug { "Trip spans ${days.size} days from $startDate to $endDate" }

        // Build a TripDay for each day
        val tripDays = days.map { date ->
            buildTripDay(trip.id, date, tripVisits, tripRoutes)
        }

        logger.info { "Built ${tripDays.size} trip days for trip ${trip.id}" }
        return tripDays
    }

    /**
     * Build a single TripDay for a specific date.
     */
    private fun buildTripDay(
        tripId: String,
        date: LocalDate,
        allVisits: List<PlaceVisit>,
        allRoutes: List<RouteSegment>
    ): TripDay {
        val dayStart = date.atStartOfDayIn(timeZone)
        val dayEnd = date.plus(1, DateTimeUnit.DAY).atStartOfDayIn(timeZone)

        // Find visits and routes that fall on this day
        val dayVisits = allVisits.filter { visit ->
            visit.startTime >= dayStart && visit.startTime < dayEnd
        }

        val dayRoutes = allRoutes.filter { route ->
            route.startTime >= dayStart && route.startTime < dayEnd
        }

        // Build timeline items
        val items = buildTimelineItems(date, dayVisits, dayRoutes, dayStart, dayEnd)

        logger.trace {
            "Day $date: ${items.size} timeline items (${dayVisits.size} visits, ${dayRoutes.size} routes)"
        }

        return TripDay(
            id = generateTripDayId(tripId, date),
            tripId = tripId,
            date = date,
            items = items
        )
    }

    /**
     * Build ordered timeline items for a day.
     */
    private fun buildTimelineItems(
        date: LocalDate,
        visits: List<PlaceVisit>,
        routes: List<RouteSegment>,
        dayStart: Instant,
        dayEnd: Instant
    ): List<TimelineItem> {
        val items = mutableListOf<TimelineItem>()

        // Add day start marker
        items.add(
            TimelineItem.DayStart(
                id = generateTimelineItemId("day_start", date),
                timestamp = dayStart
            )
        )

        // Merge visits and routes into a single sorted timeline
        val visitItems = visits.map { visit ->
            TimelineItem.Visit(
                id = generateTimelineItemId("visit", visit.id),
                timestamp = visit.startTime,
                placeVisit = visit
            )
        }

        val routeItems = routes.map { route ->
            TimelineItem.Route(
                id = generateTimelineItemId("route", route.id),
                timestamp = route.startTime,
                routeSegment = route
            )
        }

        // Combine and sort by timestamp
        val allItems = (visitItems + routeItems).sortedBy { it.timestamp }
        items.addAll(allItems)

        // Add day end marker
        items.add(
            TimelineItem.DayEnd(
                id = generateTimelineItemId("day_end", date),
                timestamp = dayEnd
            )
        )

        return items
    }

    /**
     * Generate a range of dates from start to end (inclusive).
     */
    private fun generateDayRange(start: LocalDate, end: LocalDate): List<LocalDate> {
        val days = mutableListOf<LocalDate>()
        var current = start

        while (current <= end) {
            days.add(current)
            current = current.plus(1, DateTimeUnit.DAY)
        }

        return days
    }

    /**
     * Generate a deterministic ID for a TripDay.
     */
    private fun generateTripDayId(tripId: String, date: LocalDate): String {
        return "trip_day_${tripId}_${date}"
    }

    /**
     * Generate a deterministic ID for a timeline item.
     */
    private fun generateTimelineItemId(type: String, identifier: Any): String {
        return "timeline_${type}_${identifier.hashCode()}"
    }
}
