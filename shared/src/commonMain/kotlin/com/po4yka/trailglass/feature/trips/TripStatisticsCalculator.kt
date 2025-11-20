package com.po4yka.trailglass.feature.trips

import com.po4yka.trailglass.domain.model.LocationSample
import com.po4yka.trailglass.domain.model.PlaceVisit
import com.po4yka.trailglass.domain.model.RouteSegment
import com.po4yka.trailglass.domain.model.Trip
import kotlinx.datetime.Clock
import kotlin.math.*

/**
 * Calculates statistics for trips based on location data, visits, and routes.
 */
class TripStatisticsCalculator {

    /**
     * Trip statistics result.
     */
    data class TripStatistics(
        val totalDistanceMeters: Double,
        val visitedPlaceCount: Int,
        val countriesVisited: List<String>,
        val citiesVisited: List<String>,
        val routeSegmentCount: Int,
        val averageSpeedKmh: Double
    )

    /**
     * Calculate comprehensive statistics for a trip.
     *
     * @param placeVisits All place visits during the trip
     * @param routeSegments All route segments during the trip
     * @param locationSamples Optional: raw location samples for more accurate distance
     * @return Calculated statistics
     */
    fun calculateStatistics(
        placeVisits: List<PlaceVisit>,
        routeSegments: List<RouteSegment>,
        locationSamples: List<LocationSample> = emptyList()
    ): TripStatistics {
        // Calculate total distance
        val totalDistance = if (locationSamples.isNotEmpty()) {
            calculateDistanceFromSamples(locationSamples)
        } else {
            routeSegments.sumOf { it.distanceMeters }
        }

        // Extract countries and cities
        val countries = placeVisits
            .mapNotNull { it.countryCode }
            .distinct()
            .sorted()

        val cities = placeVisits
            .mapNotNull { it.city }
            .distinct()
            .sorted()

        // Calculate average speed
        val totalDurationSeconds = routeSegments.sumOf { (it.endTime - it.startTime).inWholeSeconds }
        val averageSpeed = if (totalDurationSeconds > 0) {
            (totalDistance / 1000.0) / (totalDurationSeconds / 3600.0) // km/h
        } else {
            0.0
        }

        return TripStatistics(
            totalDistanceMeters = totalDistance,
            visitedPlaceCount = placeVisits.size,
            countriesVisited = countries,
            citiesVisited = cities,
            routeSegmentCount = routeSegments.size,
            averageSpeedKmh = averageSpeed
        )
    }

    /**
     * Calculate total distance from location samples.
     * Uses Haversine formula for accuracy.
     */
    private fun calculateDistanceFromSamples(samples: List<LocationSample>): Double {
        if (samples.size < 2) return 0.0

        val sorted = samples.sortedBy { it.timestamp }
        var totalDistance = 0.0

        for (i in 0 until sorted.size - 1) {
            val current = sorted[i]
            val next = sorted[i + 1]

            totalDistance += calculateDistance(
                current.latitude, current.longitude,
                next.latitude, next.longitude
            )
        }

        return totalDistance
    }

    /**
     * Calculate distance between two coordinates using Haversine formula.
     *
     * @return Distance in meters
     */
    private fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val earthRadiusMeters = 6371000.0

        val dLat = (lat2 - lat1) * PI / 180.0
        val dLon = (lon2 - lon1) * PI / 180.0

        val a = sin(dLat / 2).pow(2) +
                cos(lat1 * PI / 180.0) * cos(lat2 * PI / 180.0) *
                sin(dLon / 2).pow(2)

        val c = 2 * asin(sqrt(a))

        return earthRadiusMeters * c
    }

    /**
     * Update an existing trip with calculated statistics.
     */
    fun updateTripWithStatistics(
        trip: Trip,
        statistics: TripStatistics
    ): Trip {
        return trip.copy(
            totalDistanceMeters = statistics.totalDistanceMeters,
            visitedPlaceCount = statistics.visitedPlaceCount,
            countriesVisited = statistics.countriesVisited,
            citiesVisited = statistics.citiesVisited,
            updatedAt = Clock.System.now()
        )
    }
}
