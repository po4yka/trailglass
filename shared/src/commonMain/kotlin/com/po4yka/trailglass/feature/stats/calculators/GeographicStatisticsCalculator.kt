package com.po4yka.trailglass.feature.stats.calculators

import com.po4yka.trailglass.domain.model.PlaceVisit
import com.po4yka.trailglass.feature.stats.models.CityStats
import com.po4yka.trailglass.feature.stats.models.CountryStats
import com.po4yka.trailglass.feature.stats.models.GeographicStatistics
import com.po4yka.trailglass.feature.stats.models.LocationRecord
import me.tatarka.inject.annotations.Inject
import kotlin.math.*
import kotlin.time.Duration

/**
 * Calculator for geographic statistics.
 */
@Inject
class GeographicStatisticsCalculator {
    /**
     * Calculate comprehensive geographic statistics.
     */
    fun calculate(visits: List<PlaceVisit>): GeographicStatistics {
        if (visits.isEmpty()) {
            return GeographicStatistics(
                countries = emptySet(),
                cities = emptySet(),
                topCountries = emptyList(),
                topCities = emptyList(),
                furthestLocation = null,
                homeBase = null
            )
        }

        val countries = visits.mapNotNull { it.countryCode }.toSet()
        val cities = visits.mapNotNull { it.city }.toSet()

        val topCountries = calculateTopCountries(visits)
        val topCities = calculateTopCities(visits)

        val homeBase = findHomeBase(visits)
        val furthestLocation = findFurthestLocation(visits, homeBase)

        return GeographicStatistics(
            countries = countries,
            cities = cities,
            topCountries = topCountries,
            topCities = topCities,
            furthestLocation = furthestLocation,
            homeBase = homeBase
        )
    }

    /**
     * Calculate top countries by visit count.
     */
    private fun calculateTopCountries(visits: List<PlaceVisit>): List<CountryStats> =
        visits
            .filter { it.countryCode != null }
            .groupBy { it.countryCode!! }
            .map { (country, countryVisits) ->
                CountryStats(
                    countryCode = country,
                    visitCount = countryVisits.size,
                    totalDuration =
                        countryVisits.fold(Duration.ZERO) { acc, visit ->
                            acc + visit.duration
                        },
                    cities = countryVisits.mapNotNull { it.city }.toSet()
                )
            }.sortedByDescending { it.visitCount }
            .take(10)

    /**
     * Calculate top cities by visit count.
     */
    private fun calculateTopCities(visits: List<PlaceVisit>): List<CityStats> =
        visits
            .filter { it.city != null }
            .groupBy { it.city!! }
            .map { (city, cityVisits) ->
                CityStats(
                    city = city,
                    countryCode = cityVisits.firstOrNull()?.countryCode,
                    visitCount = cityVisits.size,
                    totalDuration =
                        cityVisits.fold(Duration.ZERO) { acc, visit ->
                            acc + visit.duration
                        }
                )
            }.sortedByDescending { it.visitCount }
            .take(10)

    /**
     * Find home base (most visited location).
     */
    private fun findHomeBase(visits: List<PlaceVisit>): LocationRecord? {
        if (visits.isEmpty()) return null

        // Group by approximate location (100m radius)
        val locationClusters = mutableMapOf<String, MutableList<PlaceVisit>>()

        visits.forEach { visit ->
            val key = "${(visit.centerLatitude * 1000).toInt()},${(visit.centerLongitude * 1000).toInt()}"
            locationClusters.getOrPut(key) { mutableListOf() }.add(visit)
        }

        // Find cluster with most visits
        val mostVisitedCluster = locationClusters.values.maxByOrNull { it.size } ?: return null
        val representative = mostVisitedCluster.first()

        return LocationRecord(
            name = representative.displayName,
            city = representative.city,
            country = representative.countryCode,
            latitude = representative.centerLatitude,
            longitude = representative.centerLongitude
        )
    }

    /**
     * Find furthest location from home base using Haversine formula.
     */
    private fun findFurthestLocation(
        visits: List<PlaceVisit>,
        homeBase: LocationRecord?
    ): LocationRecord? {
        if (visits.isEmpty() || homeBase == null) return null

        val furthest =
            visits.maxByOrNull { visit ->
                calculateDistance(
                    homeBase.latitude,
                    homeBase.longitude,
                    visit.centerLatitude,
                    visit.centerLongitude
                )
            } ?: return null

        return LocationRecord(
            name = furthest.displayName,
            city = furthest.city,
            country = furthest.countryCode,
            latitude = furthest.centerLatitude,
            longitude = furthest.centerLongitude
        )
    }

    /**
     * Calculate distance between two coordinates using Haversine formula (in meters).
     */
    private fun calculateDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val earthRadiusKm = 6371.0

        val dLat = (lat2 - lat1) * PI / 180.0
        val dLon = (lon2 - lon1) * PI / 180.0

        val a =
            sin(dLat / 2) * sin(dLat / 2) +
                cos(lat1 * PI / 180.0) * cos(lat2 * PI / 180.0) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadiusKm * c * 1000.0 // Convert to meters
    }

    /**
     * Get distance distribution by country.
     */
    fun getDistanceByCountry(visits: List<PlaceVisit>): Map<String, Double> {
        // This would require route segments, returning empty for now
        // In a real implementation, you'd correlate routes with countries
        return emptyMap()
    }
}
