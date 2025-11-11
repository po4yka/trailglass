package com.po4yka.trailglass.feature.stats.calculators

import com.po4yka.trailglass.domain.model.PlaceCategory
import com.po4yka.trailglass.domain.model.PlaceVisit
import com.po4yka.trailglass.feature.stats.models.PlaceStatistics
import com.po4yka.trailglass.feature.stats.models.PlaceVisitCount
import com.po4yka.trailglass.feature.stats.models.VisitRecord
import kotlin.time.Duration

/**
 * Calculator for place visit statistics.
 */
class PlaceStatisticsCalculator {

    /**
     * Calculate comprehensive place statistics from visits.
     */
    fun calculate(visits: List<PlaceVisit>): PlaceStatistics {
        if (visits.isEmpty()) {
            return PlaceStatistics(
                totalPlaces = 0,
                totalVisits = 0,
                mostVisitedPlaces = emptyList(),
                visitsByCategory = emptyMap(),
                averageVisitDuration = Duration.ZERO,
                longestVisit = null,
                shortestVisit = null
            )
        }

        // Group visits by place identifier (using coordinates as fallback)
        val placeGroups = visits.groupBy { visit ->
            visit.frequentPlaceId ?: visit.userLabel ?: visit.poiName
            ?: "${visit.location.latitude},${visit.location.longitude}"
        }

        val totalPlaces = placeGroups.size

        // Calculate most visited places
        val mostVisitedPlaces = placeGroups
            .map { (placeId, placeVisits) ->
                val representative = placeVisits.first()
                PlaceVisitCount(
                    placeId = representative.frequentPlaceId,
                    placeName = representative.displayName,
                    visitCount = placeVisits.size,
                    totalDuration = placeVisits.fold(Duration.ZERO) { acc, visit ->
                        acc + visit.duration
                    },
                    category = representative.category,
                    city = representative.city,
                    country = representative.country
                )
            }
            .sortedByDescending { it.visitCount }
            .take(10)

        // Count visits by category
        val visitsByCategory = visits
            .groupBy { it.category }
            .mapValues { (_, visits) -> visits.size }

        // Calculate average visit duration
        val totalDuration = visits.fold(Duration.ZERO) { acc, visit -> acc + visit.duration }
        val averageVisitDuration = totalDuration / visits.size

        // Find longest and shortest visits
        val longestVisit = visits.maxByOrNull { it.duration }?.let {
            VisitRecord(
                placeName = it.displayName,
                duration = it.duration,
                category = it.category
            )
        }

        val shortestVisit = visits.minByOrNull { it.duration }?.let {
            VisitRecord(
                placeName = it.displayName,
                duration = it.duration,
                category = it.category
            )
        }

        return PlaceStatistics(
            totalPlaces = totalPlaces,
            totalVisits = visits.size,
            mostVisitedPlaces = mostVisitedPlaces,
            visitsByCategory = visitsByCategory,
            averageVisitDuration = averageVisitDuration,
            longestVisit = longestVisit,
            shortestVisit = shortestVisit
        )
    }

    /**
     * Get category distribution as percentages.
     */
    fun getCategoryDistribution(visits: List<PlaceVisit>): Map<PlaceCategory, Double> {
        if (visits.isEmpty()) return emptyMap()

        val totalVisits = visits.size.toDouble()
        return visits
            .groupBy { it.category }
            .mapValues { (_, categoryVisits) ->
                (categoryVisits.size / totalVisits) * 100.0
            }
    }

    /**
     * Calculate time spent by category.
     */
    fun getTimeByCategory(visits: List<PlaceVisit>): Map<PlaceCategory, Duration> {
        return visits
            .groupBy { it.category }
            .mapValues { (_, categoryVisits) ->
                categoryVisits.fold(Duration.ZERO) { acc, visit -> acc + visit.duration }
            }
    }

    /**
     * Get top places by total time spent.
     */
    fun getTopPlacesByDuration(visits: List<PlaceVisit>, limit: Int = 5): List<PlaceVisitCount> {
        val placeGroups = visits.groupBy { visit ->
            visit.frequentPlaceId ?: visit.userLabel ?: visit.poiName
            ?: "${visit.location.latitude},${visit.location.longitude}"
        }

        return placeGroups
            .map { (placeId, placeVisits) ->
                val representative = placeVisits.first()
                PlaceVisitCount(
                    placeId = representative.frequentPlaceId,
                    placeName = representative.displayName,
                    visitCount = placeVisits.size,
                    totalDuration = placeVisits.fold(Duration.ZERO) { acc, visit ->
                        acc + visit.duration
                    },
                    category = representative.category,
                    city = representative.city,
                    country = representative.country
                )
            }
            .sortedByDescending { it.totalDuration }
            .take(limit)
    }
}
