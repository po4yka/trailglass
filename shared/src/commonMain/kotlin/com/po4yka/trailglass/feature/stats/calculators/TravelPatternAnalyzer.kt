package com.po4yka.trailglass.feature.stats.calculators

import com.po4yka.trailglass.domain.model.PlaceVisit
import com.po4yka.trailglass.domain.model.RouteSegment
import com.po4yka.trailglass.feature.stats.models.ActivityCount
import com.po4yka.trailglass.feature.stats.models.TravelPatterns
import com.po4yka.trailglass.feature.stats.models.WeekdaySplit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Analyzer for travel patterns and temporal statistics.
 */
class TravelPatternAnalyzer(
    private val timeZone: TimeZone = TimeZone.currentSystemDefault()
) {

    /**
     * Analyze travel patterns from visits and routes.
     */
    fun analyze(
        visits: List<PlaceVisit>,
        routes: List<RouteSegment>
    ): TravelPatterns {
        // Analyze hourly activity
        val hourlyActivity = analyzeHourlyActivity(visits, routes)

        // Analyze weekday activity
        val weekdayActivity = analyzeWeekdayActivity(visits, routes)

        // Find peak hours and days
        val peakTravelHour = hourlyActivity.maxByOrNull { it.value.totalEvents }?.key
        val peakTravelDay = weekdayActivity.maxByOrNull { it.value.totalEvents }?.key

        // Analyze weekday vs weekend
        val weekdaySplit = analyzeWeekdaySplit(visits, routes)

        return TravelPatterns(
            hourlyActivity = hourlyActivity,
            weekdayActivity = weekdayActivity,
            peakTravelHour = peakTravelHour,
            peakTravelDay = peakTravelDay,
            weekdayVsWeekend = weekdaySplit
        )
    }

    /**
     * Analyze activity by hour of day (0-23).
     */
    private fun analyzeHourlyActivity(
        visits: List<PlaceVisit>,
        routes: List<RouteSegment>
    ): Map<Int, ActivityCount> {
        val activityByHour = mutableMapOf<Int, MutableList<ActivityEvent>>()

        // Add visit starts
        visits.forEach { visit ->
            val hour = visit.startTime.toLocalDateTime(timeZone).hour
            activityByHour.getOrPut(hour) { mutableListOf() }
                .add(ActivityEvent.Visit)
        }

        // Add route starts
        routes.forEach { route ->
            val hour = route.startTime.toLocalDateTime(timeZone).hour
            activityByHour.getOrPut(hour) { mutableListOf() }
                .add(ActivityEvent.Route(route.distanceMeters))
        }

        // Convert to ActivityCount
        return (0..23).associateWith { hour ->
            val events = activityByHour[hour] ?: emptyList()
            ActivityCount(
                totalEvents = events.size,
                visits = events.count { it is ActivityEvent.Visit },
                routes = events.count { it is ActivityEvent.Route },
                totalDistance = events.filterIsInstance<ActivityEvent.Route>()
                    .sumOf { it.distance }
            )
        }
    }

    /**
     * Analyze activity by day of week.
     */
    private fun analyzeWeekdayActivity(
        visits: List<PlaceVisit>,
        routes: List<RouteSegment>
    ): Map<DayOfWeek, ActivityCount> {
        val activityByDay = mutableMapOf<DayOfWeek, MutableList<ActivityEvent>>()

        // Add visit starts
        visits.forEach { visit ->
            val dayOfWeek = visit.startTime.toLocalDateTime(timeZone).dayOfWeek
            activityByDay.getOrPut(dayOfWeek) { mutableListOf() }
                .add(ActivityEvent.Visit)
        }

        // Add route starts
        routes.forEach { route ->
            val dayOfWeek = route.startTime.toLocalDateTime(timeZone).dayOfWeek
            activityByDay.getOrPut(dayOfWeek) { mutableListOf() }
                .add(ActivityEvent.Route(route.distanceMeters))
        }

        // Convert to ActivityCount
        return DayOfWeek.entries.associateWith { day ->
            val events = activityByDay[day] ?: emptyList()
            ActivityCount(
                totalEvents = events.size,
                visits = events.count { it is ActivityEvent.Visit },
                routes = events.count { it is ActivityEvent.Route },
                totalDistance = events.filterIsInstance<ActivityEvent.Route>()
                    .sumOf { it.distance }
            )
        }
    }

    /**
     * Analyze weekday vs weekend split.
     */
    private fun analyzeWeekdaySplit(
        visits: List<PlaceVisit>,
        routes: List<RouteSegment>
    ): WeekdaySplit {
        val weekdays = setOf(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY
        )

        // Count trips (visits + routes)
        var weekdayTrips = 0
        var weekendTrips = 0
        var weekdayDistance = 0.0
        var weekendDistance = 0.0

        visits.forEach { visit ->
            val dayOfWeek = visit.startTime.toLocalDateTime(timeZone).dayOfWeek
            if (dayOfWeek in weekdays) {
                weekdayTrips++
            } else {
                weekendTrips++
            }
        }

        routes.forEach { route ->
            val dayOfWeek = route.startTime.toLocalDateTime(timeZone).dayOfWeek
            if (dayOfWeek in weekdays) {
                weekdayTrips++
                weekdayDistance += route.distanceMeters
            } else {
                weekendTrips++
                weekendDistance += route.distanceMeters
            }
        }

        return WeekdaySplit(
            weekdayTrips = weekdayTrips,
            weekendTrips = weekendTrips,
            weekdayDistance = weekdayDistance,
            weekendDistance = weekendDistance
        )
    }

    /**
     * Get most active time range.
     */
    fun getMostActiveTimeRange(visits: List<PlaceVisit>, routes: List<RouteSegment>): String {
        val hourlyActivity = analyzeHourlyActivity(visits, routes)
        val peakHour = hourlyActivity.maxByOrNull { it.value.totalEvents }?.key ?: return "Unknown"

        return when (peakHour) {
            in 0..5 -> "Night Owl (12 AM - 6 AM)"
            in 6..11 -> "Morning (6 AM - 12 PM)"
            in 12..17 -> "Afternoon (12 PM - 6 PM)"
            else -> "Evening (6 PM - 12 AM)"
        }
    }

    /**
     * Helper class for tracking activity events.
     */
    private sealed class ActivityEvent {
        object Visit : ActivityEvent()
        data class Route(val distance: Double) : ActivityEvent()
    }
}
