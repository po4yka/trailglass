package com.po4yka.trailglass.data.repository.impl

import com.po4yka.trailglass.data.repository.LocationRepository
import com.po4yka.trailglass.data.repository.PlaceVisitRepository
import com.po4yka.trailglass.data.repository.WidgetStateRepository
import com.po4yka.trailglass.data.repository.WidgetStats
import com.po4yka.trailglass.location.tracking.LocationTracker
import com.po4yka.trailglass.logging.logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
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
    private val placeVisitRepository: PlaceVisitRepository,
    private val locationTracker: LocationTracker
) : WidgetStateRepository {
    private val logger = logger()

    override fun getTodayStats(): Flow<WidgetStats> {
        // Combine location/visit data with tracking status
        return combine(
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
                    Pair(
                        distanceKm,
                        placeVisits.size
                    )
                )
            },
            locationTracker.trackingState.map { it.isTracking }
        ) { (distanceAndPlaces, isTracking) ->
            WidgetStats(
                distanceKm = distanceAndPlaces.first,
                placesVisited = distanceAndPlaces.second,
                isTracking = isTracking
            )
        }
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

        // Get current tracking status
        val isTracking = locationTracker.getCurrentState().isTracking

        logger.debug { "Getting widget stats snapshot for today: $today, distance=$distanceKm km, places=${placeVisits.size}, tracking=$isTracking" }

        return WidgetStats(
            distanceKm = distanceKm,
            placesVisited = placeVisits.size,
            isTracking = isTracking
        )
    }

    override suspend fun isTrackingActive(): Boolean {
        return locationTracker.getCurrentState().isTracking
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
        val dLat = (lat2 - lat1).toRadians()
        val dLon = (lon2 - lon1).toRadians()
        val a =
            sin(dLat / 2) * sin(dLat / 2) +
                cos(lat1.toRadians()) * cos(lat2.toRadians()) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    private fun Double.toRadians(): Double = this * PI / 180.0
}
