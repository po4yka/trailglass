package com.po4yka.trailglass.feature.stats

import com.po4yka.trailglass.data.repository.PlaceVisitRepository
import com.po4yka.trailglass.data.repository.RouteSegmentRepository
import com.po4yka.trailglass.data.repository.TripRepository
import com.po4yka.trailglass.feature.stats.calculators.*
import com.po4yka.trailglass.feature.stats.models.ComprehensiveStatistics
import com.po4yka.trailglass.logging.logger
import kotlinx.datetime.*
import me.tatarka.inject.annotations.Inject
import kotlin.time.Duration

/**
 * Use case for getting comprehensive statistics for a time period.
 */
@Inject
class GetComprehensiveStatsUseCase(
    private val tripRepository: TripRepository,
    private val placeVisitRepository: PlaceVisitRepository,
    private val routeSegmentRepository: RouteSegmentRepository,
    private val distanceCalculator: DistanceStatisticsCalculator,
    private val placeCalculator: PlaceStatisticsCalculator,
    private val patternAnalyzer: TravelPatternAnalyzer,
    private val geoCalculator: GeographicStatisticsCalculator,
    private val timeZone: TimeZone = TimeZone.currentSystemDefault()
) {

    private val logger = logger()

    /**
     * Get comprehensive statistics for a time period.
     */
    suspend fun execute(period: GetStatsUseCase.Period, userId: String): ComprehensiveStatistics {
        logger.info { "Getting comprehensive stats for period: $period, user: $userId" }

        val (startTime, endTime) = getTimeRange(period)

        // Get all data in parallel (conceptually - would use async in real code)
        val trips = tripRepository.getTripsInRange(userId, startTime, endTime)
        val visits = placeVisitRepository.getVisits(userId, startTime, endTime)
        val routes = routeSegmentRepository.getRouteSegmentsInRange(userId, startTime, endTime)

        logger.debug {
            "Loaded data: ${trips.size} trips, ${visits.size} visits, ${routes.size} routes"
        }

        // Calculate statistics using specialized calculators
        val distanceStats = distanceCalculator.calculate(routes)
        val placeStats = placeCalculator.calculate(visits)
        val travelPatterns = patternAnalyzer.analyze(visits, routes)
        val geoStats = geoCalculator.calculate(visits)

        // Calculate active days
        val activeDays = calculateActiveDays(visits, routes)

        // Calculate total tracking time
        val totalTrackingTime = calculateTotalTrackingTime(visits, routes)

        val stats = ComprehensiveStatistics(
            period = formatPeriod(period),
            distanceStats = distanceStats,
            placeStats = placeStats,
            travelPatterns = travelPatterns,
            geographicStats = geoStats,
            totalTrips = trips.size,
            activeDays = activeDays,
            totalTimeTracking = totalTrackingTime
        )

        logger.info {
            "Comprehensive stats for $period: ${stats.distanceStats.totalDistanceKm.toInt()} km, " +
            "${stats.geographicStats.countries.size} countries, ${stats.activeDays} active days"
        }

        return stats
    }

    /**
     * Get time range for a period.
     */
    private fun getTimeRange(period: GetStatsUseCase.Period): Pair<Instant, Instant> {
        return when (period) {
            is GetStatsUseCase.Period.Year -> {
                val start = LocalDate(period.year, 1, 1).atStartOfDayIn(timeZone)
                val end = LocalDate(period.year, 12, 31).atTime(23, 59, 59).toInstant(timeZone)
                start to end
            }
            is GetStatsUseCase.Period.Month -> {
                val start = LocalDate(period.year, period.month, 1).atStartOfDayIn(timeZone)
                val lastDay = start.toLocalDateTime(timeZone).date
                    .plus(1, DateTimeUnit.MONTH)
                    .minus(1, DateTimeUnit.DAY)
                val end = lastDay.atTime(23, 59, 59).toInstant(timeZone)
                start to end
            }
            is GetStatsUseCase.Period.Custom -> {
                val start = period.startDate.atStartOfDayIn(timeZone)
                val end = period.endDate.atTime(23, 59, 59).toInstant(timeZone)
                start to end
            }
        }
    }

    /**
     * Calculate number of unique active days.
     */
    private fun calculateActiveDays(
        visits: List<com.po4yka.trailglass.domain.model.PlaceVisit>,
        routes: List<com.po4yka.trailglass.domain.model.RouteSegment>
    ): Int {
        val activeDates = mutableSetOf<LocalDate>()

        visits.forEach { visit ->
            activeDates.add(visit.startTime.toLocalDateTime(timeZone).date)
        }

        routes.forEach { route ->
            activeDates.add(route.startTime.toLocalDateTime(timeZone).date)
        }

        return activeDates.size
    }

    /**
     * Calculate total time spent tracking (visits + routes).
     */
    private fun calculateTotalTrackingTime(
        visits: List<com.po4yka.trailglass.domain.model.PlaceVisit>,
        routes: List<com.po4yka.trailglass.domain.model.RouteSegment>
    ): Duration {
        val visitTime = visits.fold(Duration.ZERO) { acc, visit -> acc + visit.duration }
        val routeTime = routes.fold(Duration.ZERO) { acc, route ->
            acc + (route.endTime - route.startTime)
        }
        return visitTime + routeTime
    }

    /**
     * Format period as string.
     */
    private fun formatPeriod(period: GetStatsUseCase.Period): String {
        return when (period) {
            is GetStatsUseCase.Period.Year -> "${period.year}"
            is GetStatsUseCase.Period.Month -> {
                val month = Month(period.month).name.lowercase()
                    .replaceFirstChar { it.uppercase() }
                "$month ${period.year}"
            }
            is GetStatsUseCase.Period.Custom -> {
                "${period.startDate} to ${period.endDate}"
            }
        }
    }
}
