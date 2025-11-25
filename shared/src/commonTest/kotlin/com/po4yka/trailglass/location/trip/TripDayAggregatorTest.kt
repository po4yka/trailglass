package com.po4yka.trailglass.location.trip

import com.po4yka.trailglass.domain.model.PlaceVisit
import com.po4yka.trailglass.domain.model.RouteSegment
import com.po4yka.trailglass.domain.model.TimelineItem
import com.po4yka.trailglass.domain.model.Trip
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TripDayAggregatorTest {

    private val aggregator = TripDayAggregator(TimeZone.UTC)
    private val timeZone = TimeZone.UTC

    @Test
    fun testAggregateTripDays_singleDay() {
        val date = LocalDate(2023, 10, 1)
        val start = date.atStartOfDayIn(timeZone)
        // End at 23:00 to stay within the same day
        val end = start.plus(23, DateTimeUnit.HOUR)

        val trip = createTrip(start, end)
        val visit = createVisit(start.plus(1, DateTimeUnit.HOUR))
        val route = createRoute(start.plus(2, DateTimeUnit.HOUR))

        val days = aggregator.aggregateTripDays(trip, listOf(visit), listOf(route))

        assertEquals(1, days.size)
        val day = days[0]
        assertEquals(date, day.date)
        assertEquals(4, day.items.size) // Start + Visit + Route + End
        assertTrue(day.items[0] is TimelineItem.DayStart)
        assertTrue(day.items[1] is TimelineItem.Visit)
        assertTrue(day.items[2] is TimelineItem.Route)
        assertTrue(day.items[3] is TimelineItem.DayEnd)
    }

    @Test
    fun testAggregateTripDays_multiDay() {
        val startDate = LocalDate(2023, 10, 1)
        val endDate = LocalDate(2023, 10, 3)
        val start = startDate.atStartOfDayIn(timeZone)
        val end = endDate.plus(1, DateTimeUnit.DAY).atStartOfDayIn(timeZone)

        val trip = createTrip(start, end)
        // Add a visit to avoid empty trip check
        val visit = createVisit(start.plus(1, DateTimeUnit.HOUR))

        val days = aggregator.aggregateTripDays(trip, listOf(visit), emptyList())

        assertEquals(4, days.size) // Oct 1, 2, 3, 4 (since end is Oct 4 00:00)
        assertEquals(startDate, days[0].date)
    }

    private fun createTrip(start: kotlinx.datetime.Instant, end: kotlinx.datetime.Instant): Trip {
        return Trip(
            id = "trip1",
            startTime = start,
            endTime = end,
            userId = "user1"
        )
    }

    private fun createVisit(time: kotlinx.datetime.Instant): PlaceVisit {
        return PlaceVisit(
            id = "visit1",
            startTime = time,
            endTime = time.plus(1, DateTimeUnit.HOUR),
            centerLatitude = 0.0,
            centerLongitude = 0.0
        )
    }

    private fun createRoute(time: kotlinx.datetime.Instant): RouteSegment {
        return RouteSegment(
            id = "route1",
            startTime = time,
            endTime = time.plus(1, DateTimeUnit.HOUR),
            fromPlaceVisitId = null,
            toPlaceVisitId = null
        )
    }
}
