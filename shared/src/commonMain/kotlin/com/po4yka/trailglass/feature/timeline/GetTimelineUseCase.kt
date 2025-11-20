package com.po4yka.trailglass.feature.timeline

import com.po4yka.trailglass.data.repository.PlaceVisitRepository
import com.po4yka.trailglass.data.repository.RouteSegmentRepository
import com.po4yka.trailglass.domain.model.*
import com.po4yka.trailglass.logging.logger
import kotlinx.datetime.*
import me.tatarka.inject.annotations.Inject

/**
 * Enhanced use case for getting timeline with zoom levels and filtering.
 */
@Inject
class GetTimelineUseCase(
    private val placeVisitRepository: PlaceVisitRepository,
    private val routeSegmentRepository: RouteSegmentRepository,
    private val timeZone: TimeZone = TimeZone.currentSystemDefault()
) {
    private val logger = logger()

    /**
     * Timeline item for UI display with enhanced metadata.
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

        data class DaySummaryUI(
            override val id: String,
            override val timestamp: Instant,
            val date: LocalDate,
            val visitCount: Int,
            val routeCount: Int,
            val totalDistance: Double,
            val primaryCountry: String?
        ) : TimelineItemUI()

        data class WeekSummaryUI(
            override val id: String,
            override val timestamp: Instant,
            val weekStart: LocalDate,
            val weekEnd: LocalDate,
            val visitCount: Int,
            val routeCount: Int,
            val totalDistance: Double,
            val countriesVisited: List<String>
        ) : TimelineItemUI()

        data class MonthSummaryUI(
            override val id: String,
            override val timestamp: Instant,
            val month: kotlinx.datetime.Month,
            val year: Int,
            val visitCount: Int,
            val routeCount: Int,
            val totalDistance: Double,
            val countriesVisited: List<String>
        ) : TimelineItemUI()
    }

    /**
     * Get timeline for the specified zoom level and date range.
     *
     * @param zoomLevel Timeline zoom level
     * @param referenceDate Reference date for the timeline
     * @param userId User ID
     * @param filter Optional filter criteria
     * @return List of timeline items
     */
    suspend fun execute(
        zoomLevel: TimelineZoomLevel,
        referenceDate: LocalDate,
        userId: String,
        filter: TimelineFilter = TimelineFilter()
    ): List<TimelineItemUI> {
        logger.debug { "Getting timeline: zoom=$zoomLevel, date=$referenceDate, filter=${filter.activeFilterCount} active" }

        return when (zoomLevel) {
            TimelineZoomLevel.DAY -> getDayTimeline(referenceDate, userId, filter)
            TimelineZoomLevel.WEEK -> getWeekTimeline(referenceDate, userId, filter)
            TimelineZoomLevel.MONTH -> getMonthTimeline(referenceDate, userId, filter)
            TimelineZoomLevel.YEAR -> getYearTimeline(referenceDate, userId, filter)
        }
    }

    /**
     * Get detailed timeline for a single day.
     */
    private suspend fun getDayTimeline(
        date: LocalDate,
        userId: String,
        filter: TimelineFilter
    ): List<TimelineItemUI> {
        val dayStart = date.atStartOfDayIn(timeZone)
        val dayEnd = date.plus(1, DateTimeUnit.DAY).atStartOfDayIn(timeZone)

        val visits = placeVisitRepository.getVisits(userId, dayStart, dayEnd)
            .filter { visit -> matchesFilter(visit, filter) }

        val routes = routeSegmentRepository.getRouteSegmentsInRange(userId, dayStart, dayEnd)
            .filter { route -> matchesFilter(route, filter) }

        val items = mutableListOf<TimelineItemUI>()

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

        return items.sortedBy { it.timestamp }
    }

    /**
     * Get weekly summary timeline.
     */
    private suspend fun getWeekTimeline(
        referenceDate: LocalDate,
        userId: String,
        filter: TimelineFilter
    ): List<TimelineItemUI> {
        // Calculate week boundaries (Monday to Sunday)
        val dayOfWeek = referenceDate.dayOfWeek.ordinal
        val weekStart = referenceDate.minus(dayOfWeek, DateTimeUnit.DAY)
        val weekEnd = weekStart.plus(6, DateTimeUnit.DAY)

        val items = mutableListOf<TimelineItemUI>()

        // Generate summaries for each day in the week
        for (day in 0..6) {
            val currentDate = weekStart.plus(day, DateTimeUnit.DAY)
            val dayStart = currentDate.atStartOfDayIn(timeZone)
            val dayEnd = currentDate.plus(1, DateTimeUnit.DAY).atStartOfDayIn(timeZone)

            val visits = placeVisitRepository.getVisits(userId, dayStart, dayEnd)
                .filter { matchesFilter(it, filter) }

            val routes = routeSegmentRepository.getRouteSegmentsInRange(userId, dayStart, dayEnd)
                .filter { matchesFilter(it, filter) }

            if (visits.isNotEmpty() || routes.isNotEmpty()) {
                items.add(
                    TimelineItemUI.DaySummaryUI(
                        id = "day_summary_$currentDate",
                        timestamp = dayStart,
                        date = currentDate,
                        visitCount = visits.size,
                        routeCount = routes.size,
                        totalDistance = routes.sumOf { it.distanceMeters },
                        primaryCountry = visits.mapNotNull { it.countryCode }.firstOrNull()
                    )
                )
            }
        }

        return items.sortedBy { it.timestamp }
    }

    /**
     * Get monthly summary timeline.
     */
    private suspend fun getMonthTimeline(
        referenceDate: LocalDate,
        userId: String,
        filter: TimelineFilter
    ): List<TimelineItemUI> {
        val monthStart = LocalDate(referenceDate.year, referenceDate.month, 1)
        val monthEnd = monthStart.plus(1, DateTimeUnit.MONTH)

        val items = mutableListOf<TimelineItemUI>()

        // Generate weekly summaries for the month
        var currentWeekStart = monthStart
        while (currentWeekStart < monthEnd) {
            val weekEndDate = minOf(
                currentWeekStart.plus(6, DateTimeUnit.DAY),
                monthEnd.minus(1, DateTimeUnit.DAY)
            )

            val weekStart = currentWeekStart.atStartOfDayIn(timeZone)
            val weekEndTime = weekEndDate.plus(1, DateTimeUnit.DAY).atStartOfDayIn(timeZone)

            val visits = placeVisitRepository.getVisits(userId, weekStart, weekEndTime)
                .filter { matchesFilter(it, filter) }

            val routes = routeSegmentRepository.getRouteSegmentsInRange(userId, weekStart, weekEndTime)
                .filter { matchesFilter(it, filter) }

            if (visits.isNotEmpty() || routes.isNotEmpty()) {
                items.add(
                    TimelineItemUI.WeekSummaryUI(
                        id = "week_summary_$currentWeekStart",
                        timestamp = weekStart,
                        weekStart = currentWeekStart,
                        weekEnd = weekEndDate,
                        visitCount = visits.size,
                        routeCount = routes.size,
                        totalDistance = routes.sumOf { it.distanceMeters },
                        countriesVisited = visits.mapNotNull { it.countryCode }.distinct()
                    )
                )
            }

            currentWeekStart = currentWeekStart.plus(7, DateTimeUnit.DAY)
        }

        return items.sortedBy { it.timestamp }
    }

    /**
     * Get yearly summary timeline.
     */
    private suspend fun getYearTimeline(
        referenceDate: LocalDate,
        userId: String,
        filter: TimelineFilter
    ): List<TimelineItemUI> {
        val items = mutableListOf<TimelineItemUI>()

        // Generate monthly summaries for the year
        for (monthNum in 1..12) {
            val monthStart = LocalDate(referenceDate.year, monthNum, 1)
            val monthEnd = monthStart.plus(1, DateTimeUnit.MONTH)

            val monthStartTime = monthStart.atStartOfDayIn(timeZone)
            val monthEndTime = monthEnd.atStartOfDayIn(timeZone)

            val visits = placeVisitRepository.getVisits(userId, monthStartTime, monthEndTime)
                .filter { matchesFilter(it, filter) }

            val routes = routeSegmentRepository.getRouteSegmentsInRange(userId, monthStartTime, monthEndTime)
                .filter { matchesFilter(it, filter) }

            if (visits.isNotEmpty() || routes.isNotEmpty()) {
                items.add(
                    TimelineItemUI.MonthSummaryUI(
                        id = "month_summary_${monthStart.year}_${monthStart.monthNumber}",
                        timestamp = monthStartTime,
                        month = monthStart.month,
                        year = monthStart.year,
                        visitCount = visits.size,
                        routeCount = routes.size,
                        totalDistance = routes.sumOf { it.distanceMeters },
                        countriesVisited = visits.mapNotNull { it.countryCode }.distinct()
                    )
                )
            }
        }

        return items.sortedBy { it.timestamp }
    }

    /**
     * Check if a place visit matches the filter criteria.
     */
    private fun matchesFilter(visit: PlaceVisit, filter: TimelineFilter): Boolean {
        // Category filter
        if (filter.placeCategories.isNotEmpty() && visit.category !in filter.placeCategories) {
            return false
        }

        // Country filter
        if (filter.countries.isNotEmpty() && visit.countryCode !in filter.countries) {
            return false
        }

        // City filter
        if (filter.cities.isNotEmpty() && visit.city !in filter.cities) {
            return false
        }

        // Favorites filter
        if (filter.showOnlyFavorites && !visit.isFavorite) {
            return false
        }

        // Duration filter
        if (filter.minDuration != null && visit.duration < filter.minDuration) {
            return false
        }
        if (filter.maxDuration != null && visit.duration > filter.maxDuration) {
            return false
        }

        // Search query
        if (filter.searchQuery != null) {
            val query = filter.searchQuery.lowercase()
            val matchesName = visit.displayName.lowercase().contains(query)
            val matchesAddress = visit.approximateAddress?.lowercase()?.contains(query) == true
            val matchesCity = visit.city?.lowercase()?.contains(query) == true
            val matchesNotes = visit.userNotes?.lowercase()?.contains(query) == true

            if (!matchesName && !matchesAddress && !matchesCity && !matchesNotes) {
                return false
            }
        }

        return true
    }

    /**
     * Check if a route segment matches the filter criteria.
     */
    private fun matchesFilter(route: RouteSegment, filter: TimelineFilter): Boolean {
        // Transport type filter
        if (filter.transportTypes.isNotEmpty() && route.transportType !in filter.transportTypes) {
            return false
        }

        return true
    }
}
