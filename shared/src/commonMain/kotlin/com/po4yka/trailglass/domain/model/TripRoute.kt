package com.po4yka.trailglass.domain.model

import kotlinx.datetime.Instant

/**
 * Complete route for a trip, combining all route segments and place visits into a unified structure optimized for map
 * visualization and replay.
 */
data class TripRoute(
    val tripId: String,
    val startTime: Instant,
    val endTime: Instant,
    // Unified path combining all segments
    val fullPath: List<RoutePoint>,
    // Individual route segments for detailed analysis
    val segments: List<RouteSegment>,
    // Place visits along the route
    val visits: List<PlaceVisit>,
    // Photos associated with this route
    val photoMarkers: List<PhotoMarker>,
    // Bounding box for camera fitting
    val bounds: RouteBounds,
    // Aggregated statistics
    val statistics: RouteStatistics
)

/** A point along the route with timing information for replay animation. */
data class RoutePoint(
    val latitude: Double,
    val longitude: Double,
    val timestamp: Instant,
    val bearing: Double? = null, // Direction of movement in degrees (0-360)
    val speed: Double? = null, // Speed in m/s at this point
    val accuracy: Double? = null, // GPS accuracy in meters
    val segmentId: String? = null, // Which route segment this belongs to
    val transportType: TransportType = TransportType.UNKNOWN
)

/** Photo marker on the map with location and metadata. */
data class PhotoMarker(
    val photoId: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Instant,
    val thumbnailUri: String? = null,
    val placeVisitId: String? = null, // Associated place visit if any
    val caption: String? = null
)

/** Geographic bounding box for a route. */
data class RouteBounds(
    val minLatitude: Double,
    val maxLatitude: Double,
    val minLongitude: Double,
    val maxLongitude: Double
) {
    val centerLatitude: Double
        get() = (minLatitude + maxLatitude) / 2.0

    val centerLongitude: Double
        get() = (minLongitude + maxLongitude) / 2.0

    val latitudeSpan: Double
        get() = maxLatitude - minLatitude

    val longitudeSpan: Double
        get() = maxLongitude - minLongitude

    companion object {
        /** Calculate bounding box from a list of coordinates. */
        fun fromCoordinates(coordinates: List<Coordinate>): RouteBounds? {
            if (coordinates.isEmpty()) return null

            var minLat = Double.MAX_VALUE
            var maxLat = -Double.MAX_VALUE
            var minLon = Double.MAX_VALUE
            var maxLon = -Double.MAX_VALUE

            coordinates.forEach { coord ->
                if (coord.latitude < minLat) minLat = coord.latitude
                if (coord.latitude > maxLat) maxLat = coord.latitude
                if (coord.longitude < minLon) minLon = coord.longitude
                if (coord.longitude > maxLon) maxLon = coord.longitude
            }

            return RouteBounds(minLat, maxLat, minLon, maxLon)
        }

        /** Calculate bounding box from route points. */
        fun fromRoutePoints(points: List<RoutePoint>): RouteBounds? {
            if (points.isEmpty()) return null

            var minLat = Double.MAX_VALUE
            var maxLat = -Double.MAX_VALUE
            var minLon = Double.MAX_VALUE
            var maxLon = -Double.MAX_VALUE

            points.forEach { point ->
                if (point.latitude < minLat) minLat = point.latitude
                if (point.latitude > maxLat) maxLat = point.latitude
                if (point.longitude < minLon) minLon = point.longitude
                if (point.longitude > maxLon) maxLon = point.longitude
            }

            return RouteBounds(minLat, maxLat, minLon, maxLon)
        }
    }
}

/** Aggregated statistics for a trip route. */
data class RouteStatistics(
    val totalDistanceMeters: Double,
    val totalDurationSeconds: Long,
    val numberOfLocations: Int,
    val numberOfPhotos: Int,
    val numberOfVideos: Int = 0,
    val countriesVisited: List<String> = emptyList(),
    val citiesVisited: List<String> = emptyList(),
    // Transport breakdown
    val distanceByTransport: Map<TransportType, Double> = emptyMap(),
    val durationByTransport: Map<TransportType, Long> = emptyMap(),
    // Speed metrics
    val averageSpeedMps: Double? = null,
    val maxSpeedMps: Double? = null
) {
    val totalDistanceKilometers: Double
        get() = totalDistanceMeters / 1000.0

    val totalDurationDays: Int
        get() = (totalDurationSeconds / 86400).toInt()

    val totalDurationHours: Int
        get() = ((totalDurationSeconds % 86400) / 3600).toInt()

    val totalDurationMinutes: Int
        get() = ((totalDurationSeconds % 3600) / 60).toInt()

    val remainingSeconds: Int
        get() = (totalDurationSeconds % 60).toInt()
}
