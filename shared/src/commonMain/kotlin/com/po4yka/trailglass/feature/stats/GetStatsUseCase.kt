package com.po4yka.trailglass.feature.stats

import com.po4yka.trailglass.data.repository.PlaceVisitRepository
import com.po4yka.trailglass.data.repository.TripRepository
import com.po4yka.trailglass.logging.logger
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.atTime
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import me.tatarka.inject.annotations.Inject

/** Use case for getting statistics for a time period. */
@Inject
class GetStatsUseCase(
    private val tripRepository: TripRepository,
    private val placeVisitRepository: PlaceVisitRepository,
    private val timeZone: TimeZone = TimeZone.currentSystemDefault()
) {
    private val logger = logger()

    /** Statistics for a time period. */
    data class Stats(
        val period: Period,
        val countriesVisited: Set<String>,
        val citiesVisited: Set<String>,
        val totalTrips: Int,
        val totalDays: Int,
        val totalVisits: Int,
        val topCountries: List<Pair<String, Int>>, // country to visit count
        val topCities: List<Pair<String, Int>> // city to visit count
    )

    /** Time period for statistics. */
    sealed class Period {
        data class Year(
            val year: Int
        ) : Period()

        data class Month(
            val year: Int,
            val month: Int
        ) : Period()

        data class Custom(
            val startDate: LocalDate,
            val endDate: LocalDate
        ) : Period()
    }

    /** Get statistics for a time period. */
    suspend fun execute(
        period: Period,
        userId: String
    ): Stats {
        logger.info { "Getting stats for period: $period, user: $userId" }

        val (startTime, endTime) = getTimeRange(period)

        // Get trips in range
        val trips = tripRepository.getTripsInRange(userId, startTime, endTime)
        logger.debug { "Found ${trips.size} trips" }

        // Get visits in range
        val visits = placeVisitRepository.getVisits(userId, startTime, endTime)
        logger.debug { "Found ${visits.size} visits" }

        // Aggregate visit statistics in single pass for efficiency
        val visitStats = aggregateVisitStatistics(visits)

        // Calculate total days
        val totalDays = calculateTotalDays(trips, period)

        val stats =
            Stats(
                period = period,
                countriesVisited = visitStats.countries,
                citiesVisited = visitStats.cities,
                totalTrips = trips.size,
                totalDays = totalDays,
                totalVisits = visits.size,
                topCountries = visitStats.topCountries,
                topCities = visitStats.topCities
            )

        logger.info {
            "Stats for $period: ${stats.countriesVisited.size} countries, " +
                "${stats.citiesVisited.size} cities, ${stats.totalTrips} trips"
        }

        return stats
    }

    /** Get time range for a period. */
    private fun getTimeRange(period: Period): Pair<Instant, Instant> =
        when (period) {
            is Period.Year -> {
                val start = LocalDate(period.year, 1, 1).atStartOfDayIn(timeZone)
                val end = LocalDate(period.year, 12, 31).atTime(23, 59, 59).toInstant(timeZone)
                start to end
            }

            is Period.Month -> {
                val start = LocalDate(period.year, period.month, 1).atStartOfDayIn(timeZone)
                val lastDay =
                    start
                        .toLocalDateTime(timeZone)
                        .date
                        .plus(1, DateTimeUnit.MONTH)
                        .minus(1, DateTimeUnit.DAY)
                val end = lastDay.atTime(23, 59, 59).toInstant(timeZone)
                start to end
            }

            is Period.Custom -> {
                val start = period.startDate.atStartOfDayIn(timeZone)
                val end = period.endDate.atTime(23, 59, 59).toInstant(timeZone)
                start to end
            }
        }

    /** Calculate total days covered by trips. */
    private fun calculateTotalDays(
        trips: List<com.po4yka.trailglass.domain.model.Trip>,
        period: Period
    ): Int {
        if (trips.isEmpty()) return 0

        // Sum up all unique days covered by trips
        val allDays = mutableSetOf<LocalDate>()

        trips.forEach { trip ->
            val startDate = trip.startTime.toLocalDateTime(timeZone).date
            val endDate = (trip.endTime ?: Clock.System.now()).toLocalDateTime(timeZone).date

            var currentDate = startDate
            while (currentDate <= endDate) {
                allDays.add(currentDate)
                currentDate = currentDate.plus(1, DateTimeUnit.DAY)
            }
        }

        return allDays.size
    }

    /** Aggregated visit statistics from single-pass processing. */
    private data class VisitStatistics(
        val countries: Set<String>,
        val cities: Set<String>,
        val topCountries: List<Pair<String, Int>>,
        val topCities: List<Pair<String, Int>>
    )

    /**
     * Aggregate visit statistics in a single pass through the data.
     * More efficient than multiple iterations for large datasets.
     */
    private fun aggregateVisitStatistics(
        visits: List<com.po4yka.trailglass.domain.model.PlaceVisit>
    ): VisitStatistics {
        val countryMap = mutableMapOf<String, Int>()
        val cityMap = mutableMapOf<String, Int>()

        // Single pass through visits
        visits.forEach { visit ->
            visit.countryCode?.let { country ->
                countryMap[country] = (countryMap[country] ?: 0) + 1
            }
            visit.city?.let { city ->
                cityMap[city] = (cityMap[city] ?: 0) + 1
            }
        }

        return VisitStatistics(
            countries = countryMap.keys.toSet(),
            cities = cityMap.keys.toSet(),
            topCountries = countryMap.entries
                .sortedByDescending { it.value }
                .take(5)
                .map { it.key to it.value },
            topCities = cityMap.entries
                .sortedByDescending { it.value }
                .take(5)
                .map { it.key to it.value }
        )
    }
}
