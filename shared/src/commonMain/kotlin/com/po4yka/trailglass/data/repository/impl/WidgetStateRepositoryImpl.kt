package com.po4yka.trailglass.data.repository.impl

import com.po4yka.trailglass.data.repository.LocationRepository
import com.po4yka.trailglass.data.repository.PlaceVisitRepository
import com.po4yka.trailglass.data.repository.WidgetStateRepository
import com.po4yka.trailglass.data.repository.WidgetStats
import com.po4yka.trailglass.logging.logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import me.tatarka.inject.annotations.Inject

/**
 * Implementation of WidgetStateRepository.
 *
 * Aggregates data from location and place visit repositories to provide
 * widget-ready statistics.
 */
@Inject
class WidgetStateRepositoryImpl(
    private val locationRepository: LocationRepository,
    private val placeVisitRepository: PlaceVisitRepository
) : WidgetStateRepository {
    private val logger = logger()

    override fun getTodayStats(): Flow<WidgetStats> =
        kotlinx.coroutines.flow.flow {
            // Calculate today's stats
            val now = Clock.System.now()
            val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
            val startOfDay = today.atStartOfDayIn(TimeZone.currentSystemDefault())

            // Get locations for today
            val locations = when (val result = locationRepository.getSamples("", startOfDay, now)) {
                is com.po4yka.trailglass.domain.error.Result.Success -> result.data
                is com.po4yka.trailglass.domain.error.Result.Error -> emptyList()
            }

            // Get place visits for today
            val placeVisits = try {
                placeVisitRepository.getVisits("", startOfDay, now)
            } catch (e: Exception) {
                emptyList()
            }

            // Calculate total distance (rough approximation)
            val distanceKm = calculateDistance(locations)

            logger.debug { "Widget stats: distance=$distanceKm km, places=${placeVisits.size}" }

            emit(
                WidgetStats(
                    distanceKm = distanceKm,
                    placesVisited = placeVisits.size,
                    isTracking = false // TODO: Get actual tracking status
                )
            )
        }

    override suspend fun getTodayStatsSnapshot(): WidgetStats {
        // Get snapshot of current data
        val now = Clock.System.now()
        val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
        val startOfDay = today.atStartOfDayIn(TimeZone.currentSystemDefault())

        // Get locations for today
        val locations = when (val result = locationRepository.getSamples("", startOfDay, now)) {
            is com.po4yka.trailglass.domain.error.Result.Success -> result.data
            is com.po4yka.trailglass.domain.error.Result.Error -> emptyList()
        }

        // Get place visits for today
        val placeVisits = try {
            placeVisitRepository.getVisits("", startOfDay, now)
        } catch (e: Exception) {
            emptyList()
        }

        // Calculate total distance
        val distanceKm = calculateDistance(locations)

        logger.debug { "Getting widget stats snapshot for today: $today, distance=$distanceKm km, places=${placeVisits.size}" }

        return WidgetStats(
            distanceKm = distanceKm,
            placesVisited = placeVisits.size,
            isTracking = false
        )
    }

    override suspend fun isTrackingActive(): Boolean {
        // TODO: Get actual tracking status from tracking service
        return false
    }

    /**
     * Calculate total distance from location points.
     *
     * Simple approximation using Haversine formula.
     */
    private fun calculateDistance(locations: List<com.po4yka.trailglass.domain.model.LocationSample>): Double {
        if (locations.size < 2) return 0.0

        var totalDistance = 0.0
        for (i in 0 until locations.size - 1) {
            val loc1 = locations[i]
            val loc2 = locations[i + 1]
            totalDistance += haversineDistance(loc1.latitude, loc1.longitude, loc2.latitude, loc2.longitude)
        }

        return totalDistance
    }

    /**
     * Calculate distance between two points using Haversine formula.
     *
     * @return distance in kilometers
     */
    private fun haversineDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val r = 6371.0 // Earth radius in kilometers
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a =
            Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return r * c
    }
}
