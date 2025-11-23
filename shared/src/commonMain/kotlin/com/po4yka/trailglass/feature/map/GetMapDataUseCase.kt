package com.po4yka.trailglass.feature.map

import com.po4yka.trailglass.data.repository.PlaceVisitRepository
import com.po4yka.trailglass.data.repository.RouteSegmentRepository
import com.po4yka.trailglass.domain.model.Coordinate
import com.po4yka.trailglass.domain.model.MapDisplayData
import com.po4yka.trailglass.domain.model.MapMarker
import com.po4yka.trailglass.domain.model.MapRegion
import com.po4yka.trailglass.domain.model.MapRoute
import com.po4yka.trailglass.domain.model.TransportType
import com.po4yka.trailglass.logging.logger
import kotlinx.datetime.Instant
import me.tatarka.inject.annotations.Inject

/** Use case for getting map data (markers and routes) for display. */
@Inject
class GetMapDataUseCase(
    private val placeVisitRepository: PlaceVisitRepository,
    private val routeSegmentRepository: RouteSegmentRepository
) {
    private val logger = logger()

    /**
     * Get map data for a time range.
     *
     * @param userId User ID
     * @param startTime Start of time range
     * @param endTime End of time range
     * @return Map display data with markers and routes
     */
    suspend fun execute(
        userId: String,
        startTime: Instant,
        endTime: Instant
    ): MapDisplayData {
        logger.debug { "Getting map data for $userId from $startTime to $endTime" }

        // Get place visits
        // Get place visits
        val visits = placeVisitRepository.getVisits(userId, startTime, endTime)
        logger.debug { "Found ${visits.size} visits" }

        // Get route segments
        val routes = routeSegmentRepository.getRouteSegmentsInRange(userId, startTime, endTime)
        logger.debug { "Found ${routes.size} routes" }

        // Convert to map markers
        val markers =
            visits.map { visit ->
                MapMarker(
                    id = "marker_${visit.id}",
                    coordinate = Coordinate(visit.centerLatitude, visit.centerLongitude),
                    title = visit.city ?: visit.poiName ?: "Unknown location",
                    snippet = visit.approximateAddress,
                    placeVisitId = visit.id
                )
            }

        // Convert to map routes
        val mapRoutes =
            routes.map { route ->
                MapRoute(
                    id = "route_${route.id}",
                    coordinates = route.simplifiedPath,
                    transportType = route.transportType,
                    color = getTransportColor(route.transportType),
                    routeSegmentId = route.id
                )
            }

        // Calculate bounding region
        val region = calculateBoundingRegion(markers, mapRoutes)

        val mapData =
            MapDisplayData(
                markers = markers,
                routes = mapRoutes,
                region = region
            )

        logger.info {
            "Map data prepared: ${markers.size} markers, ${mapRoutes.size} routes"
        }

        return mapData
    }

    /** Get color for transport type (ARGB format). */
    private fun getTransportColor(transportType: TransportType): Int =
        when (transportType) {
            TransportType.WALK -> 0xFF4CAF50.toInt() // Green
            TransportType.BIKE -> 0xFF2196F3.toInt() // Blue
            TransportType.CAR -> 0xFFF44336.toInt() // Red
            TransportType.TRAIN -> 0xFF9C27B0.toInt() // Purple
            TransportType.PLANE -> 0xFFFF9800.toInt() // Orange
            TransportType.BOAT -> 0xFF00BCD4.toInt() // Cyan
            TransportType.UNKNOWN -> 0xFF9E9E9E.toInt() // Gray
        }

    /** Calculate bounding region that contains all markers and routes. */
    private fun calculateBoundingRegion(
        markers: List<MapMarker>,
        routes: List<MapRoute>
    ): MapRegion? {
        val allCoordinates = mutableListOf<Coordinate>()

        // Add marker coordinates
        allCoordinates.addAll(markers.map { it.coordinate })

        // Add route coordinates
        routes.forEach { route ->
            allCoordinates.addAll(route.coordinates)
        }

        if (allCoordinates.isEmpty()) {
            return null
        }

        // Find bounds
        val minLat = allCoordinates.minOf { it.latitude }
        val maxLat = allCoordinates.maxOf { it.latitude }
        val minLon = allCoordinates.minOf { it.longitude }
        val maxLon = allCoordinates.maxOf { it.longitude }

        // Calculate center and deltas with padding
        val centerLat = (minLat + maxLat) / 2
        val centerLon = (minLon + maxLon) / 2
        val latDelta = (maxLat - minLat) * 1.2 // 20% padding
        val lonDelta = (maxLon - minLon) * 1.2

        return MapRegion(
            center = Coordinate(centerLat, centerLon),
            latitudeDelta = latDelta.coerceAtLeast(0.01), // Minimum delta
            longitudeDelta = lonDelta.coerceAtLeast(0.01)
        )
    }
}
