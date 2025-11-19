package com.po4yka.trailglass.feature.stats.calculators

import com.po4yka.trailglass.domain.model.RouteSegment
import com.po4yka.trailglass.domain.model.TransportType
import com.po4yka.trailglass.feature.stats.models.DistanceStatistics
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import me.tatarka.inject.annotations.Inject

/**
 * Calculator for distance-related statistics.
 */
@Inject
class DistanceStatisticsCalculator {

    /**
     * Calculate distance statistics from route segments.
     */
    fun calculate(routes: List<RouteSegment>): DistanceStatistics {
        if (routes.isEmpty()) {
            return DistanceStatistics(
                totalDistanceMeters = 0.0,
                byTransportType = emptyMap(),
                totalDuration = Duration.ZERO,
                averageSpeed = 0.0
            )
        }

        // Calculate total distance
        val totalDistance = routes.sumOf { it.distanceMeters }

        // Group by transport type
        val distanceByType = routes
            .groupBy { it.transportType }
            .mapValues { (_, segments) -> segments.sumOf { it.distanceMeters } }

        // Calculate total duration
        val totalDuration = routes.fold(Duration.ZERO) { acc, segment ->
            acc + (segment.endTime - segment.startTime)
        }

        // Calculate average speed (km/h)
        val averageSpeed = if (totalDuration.inWholeSeconds > 0) {
            val totalHours = totalDuration.inWholeSeconds / 3600.0
            (totalDistance / 1000.0) / totalHours
        } else {
            0.0
        }

        return DistanceStatistics(
            totalDistanceMeters = totalDistance,
            byTransportType = distanceByType,
            totalDuration = totalDuration,
            averageSpeed = averageSpeed
        )
    }

    /**
     * Calculate distance statistics for a specific transport type.
     */
    fun calculateForType(routes: List<RouteSegment>, type: TransportType): DistanceStatistics {
        val filteredRoutes = routes.filter { it.transportType == type }
        return calculate(filteredRoutes)
    }

    /**
     * Get transport type distribution as percentages.
     */
    fun getTransportTypeDistribution(routes: List<RouteSegment>): Map<TransportType, Double> {
        if (routes.isEmpty()) return emptyMap()

        val totalDistance = routes.sumOf { it.distanceMeters }
        if (totalDistance == 0.0) return emptyMap()

        return routes
            .groupBy { it.transportType }
            .mapValues { (_, segments) ->
                val typeDistance = segments.sumOf { it.distanceMeters }
                (typeDistance / totalDistance) * 100.0
            }
    }

    /**
     * Calculate average speed by transport type.
     */
    fun getAverageSpeedByType(routes: List<RouteSegment>): Map<TransportType, Double> {
        return routes
            .groupBy { it.transportType }
            .mapValues { (_, segments) ->
                val totalDistance = segments.sumOf { it.distanceMeters }
                val totalDuration = segments.fold(Duration.ZERO) { acc, segment ->
                    acc + (segment.endTime - segment.startTime)
                }

                if (totalDuration.inWholeSeconds > 0) {
                    val totalHours = totalDuration.inWholeSeconds / 3600.0
                    (totalDistance / 1000.0) / totalHours
                } else {
                    0.0
                }
            }
    }
}
