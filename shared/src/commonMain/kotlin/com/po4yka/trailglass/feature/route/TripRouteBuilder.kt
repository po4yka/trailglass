package com.po4yka.trailglass.feature.route

import com.po4yka.trailglass.domain.model.LocationSample
import com.po4yka.trailglass.domain.model.Photo
import com.po4yka.trailglass.domain.model.PhotoMarker
import com.po4yka.trailglass.domain.model.PlaceVisit
import com.po4yka.trailglass.domain.model.RouteBounds
import com.po4yka.trailglass.domain.model.RoutePoint
import com.po4yka.trailglass.domain.model.RouteSegment
import com.po4yka.trailglass.domain.model.RouteStatistics
import com.po4yka.trailglass.domain.model.TransportType
import com.po4yka.trailglass.domain.model.Trip
import com.po4yka.trailglass.domain.model.TripRoute
import com.po4yka.trailglass.logging.logger
import kotlinx.datetime.Instant
import kotlin.math.PI

/**
 * Builds complete TripRoute from location samples, route segments, place visits, and photos. This is the main service
 * for constructing route data for visualization and replay.
 *
 * Includes performance optimizations for large routes:
 * - Adaptive point limiting based on route size
 * - Pre-simplified path for initial display
 */
class TripRouteBuilder(
    private val maxFullPathPoints: Int = 50000, // Limit for very large routes
    private val maxDisplayPoints: Int = 5000 // Optimal for smooth rendering
) {
    private val logger = logger()

    /**
     * Build a complete trip route from existing data.
     *
     * @param trip The trip to build route for
     * @param locationSamples All location samples for the trip (filtered and validated)
     * @param routeSegments All route segments for the trip
     * @param placeVisits All place visits during the trip
     * @param photos Photos taken during the trip with location data
     * @return Complete TripRoute ready for visualization
     */
    fun buildTripRoute(
        trip: Trip,
        locationSamples: List<LocationSample>,
        routeSegments: List<RouteSegment>,
        placeVisits: List<PlaceVisit>,
        photos: List<Photo>
    ): TripRoute {
        logger.info { "Building trip route for trip ${trip.id} with ${locationSamples.size} samples" }

        // Performance optimization: Limit points for very large routes
        val optimizedSamples =
            if (locationSamples.size > maxFullPathPoints) {
                logger.warn {
                    "Route has ${locationSamples.size} points, downsampling to $maxFullPathPoints for performance"
                }
                downsampleLocations(locationSamples, maxFullPathPoints)
            } else {
                locationSamples
            }

        // Sort data chronologically
        val sortedSamples = optimizedSamples.sortedBy { it.timestamp }
        val sortedSegments = routeSegments.sortedBy { it.startTime }
        val sortedVisits = placeVisits.sortedBy { it.startTime }
        val sortedPhotos = photos.sortedBy { it.timestamp }

        // Build unified route path from location samples
        val fullPath = buildFullPath(sortedSamples, sortedSegments)

        // Create photo markers
        val photoMarkers = buildPhotoMarkers(sortedPhotos, placeVisits)

        // Calculate bounding box
        val bounds =
            calculateBounds(fullPath, placeVisits, photoMarkers)
                ?: RouteBounds(-90.0, 90.0, -180.0, 180.0) // Fallback to world bounds

        // Calculate statistics
        val statistics =
            calculateStatistics(
                trip = trip,
                segments = sortedSegments,
                visits = sortedVisits,
                photos = sortedPhotos,
                samples = sortedSamples
            )

        logger.info {
            "Built trip route: ${fullPath.size} points, ${photoMarkers.size} photos, " +
                "${statistics.totalDistanceKilometers.toInt()}km"
        }

        return TripRoute(
            tripId = trip.id,
            startTime = trip.startTime,
            endTime = trip.endTime ?: Instant.DISTANT_FUTURE,
            fullPath = fullPath,
            segments = sortedSegments,
            visits = sortedVisits,
            photoMarkers = photoMarkers,
            bounds = bounds,
            statistics = statistics
        )
    }

    /** Build unified path from location samples with metadata from route segments. */
    private fun buildFullPath(
        samples: List<LocationSample>,
        segments: List<RouteSegment>
    ): List<RoutePoint> {
        if (samples.isEmpty()) {
            logger.warn { "No location samples to build path" }
            return emptyList()
        }

        // Create map of sample ID to segment
        val sampleToSegment = mutableMapOf<String, RouteSegment>()
        segments.forEach { segment ->
            segment.locationSampleIds.forEach { sampleId ->
                sampleToSegment[sampleId] = segment
            }
        }

        // Convert samples to route points
        val routePoints =
            samples.map { sample ->
                val segment = sampleToSegment[sample.id]
                RoutePoint(
                    latitude = sample.latitude,
                    longitude = sample.longitude,
                    timestamp = sample.timestamp,
                    bearing = sample.bearing,
                    speed = sample.speed,
                    accuracy = sample.accuracy,
                    segmentId = segment?.id,
                    transportType = segment?.transportType ?: TransportType.UNKNOWN
                )
            }

        logger.debug { "Built full path with ${routePoints.size} route points" }
        return routePoints
    }

    /** Build photo markers from photos, associating them with place visits and route. */
    private fun buildPhotoMarkers(
        photos: List<Photo>,
        placeVisits: List<PlaceVisit>
    ): List<PhotoMarker> {
        // Filter photos with location data
        val photosWithLocation = photos.filter { it.latitude != null && it.longitude != null }

        logger.debug { "Building photo markers from ${photosWithLocation.size} geotagged photos" }

        val markers =
            photosWithLocation.map { photo ->
                // Find associated place visit (if photo was taken during a visit)
                val associatedVisit =
                    findNearestPlaceVisit(
                        photo.latitude!!,
                        photo.longitude!!,
                        photo.timestamp,
                        placeVisits
                    )

                PhotoMarker(
                    photoId = photo.id,
                    latitude = photo.latitude,
                    longitude = photo.longitude,
                    timestamp = photo.timestamp,
                    thumbnailUri = photo.uri,
                    placeVisitId = associatedVisit?.id,
                    caption = null // Will be populated from PhotoAttachment if available
                )
            }

        logger.debug { "Built ${markers.size} photo markers" }
        return markers
    }

    /**
     * Find the nearest place visit to a photo location and timestamp. Returns null if no visit is close enough
     * (spatially and temporally).
     */
    private fun findNearestPlaceVisit(
        photoLat: Double,
        photoLon: Double,
        photoTime: Instant,
        visits: List<PlaceVisit>
    ): PlaceVisit? {
        val maxDistanceMeters = 200.0 // Photo must be within 200m of visit center

        return visits
            .filter { visit ->
                // Check if photo timestamp is within visit time range
                photoTime >= visit.startTime && photoTime <= visit.endTime
            }.minByOrNull { visit ->
                // Find closest visit by distance
                haversineDistance(
                    photoLat,
                    photoLon,
                    visit.centerLatitude,
                    visit.centerLongitude
                )
            }?.takeIf { visit ->
                // Only return if distance is within threshold
                haversineDistance(
                    photoLat,
                    photoLon,
                    visit.centerLatitude,
                    visit.centerLongitude
                ) <= maxDistanceMeters
            }
    }

    /** Calculate bounding box for the route including all points and photos. */
    private fun calculateBounds(
        routePoints: List<RoutePoint>,
        visits: List<PlaceVisit>,
        photoMarkers: List<PhotoMarker>
    ): RouteBounds? {
        val allLatitudes = mutableListOf<Double>()
        val allLongitudes = mutableListOf<Double>()

        // Add route points
        routePoints.forEach { point ->
            allLatitudes.add(point.latitude)
            allLongitudes.add(point.longitude)
        }

        // Add place visit centers
        visits.forEach { visit ->
            allLatitudes.add(visit.centerLatitude)
            allLongitudes.add(visit.centerLongitude)
        }

        // Add photo markers
        photoMarkers.forEach { marker ->
            allLatitudes.add(marker.latitude)
            allLongitudes.add(marker.longitude)
        }

        if (allLatitudes.isEmpty()) return null

        return RouteBounds(
            minLatitude = allLatitudes.minOrNull()!!,
            maxLatitude = allLatitudes.maxOrNull()!!,
            minLongitude = allLongitudes.minOrNull()!!,
            maxLongitude = allLongitudes.maxOrNull()!!
        )
    }

    /** Calculate comprehensive statistics for the route. */
    private fun calculateStatistics(
        trip: Trip,
        segments: List<RouteSegment>,
        visits: List<PlaceVisit>,
        photos: List<Photo>,
        samples: List<LocationSample>
    ): RouteStatistics {
        // Total distance from segments
        val totalDistance = segments.sumOf { it.distanceMeters }

        // Total duration
        val endTime = trip.endTime ?: Instant.DISTANT_FUTURE
        val totalDuration = (endTime - trip.startTime).inWholeSeconds

        // Countries and cities
        val countries = visits.mapNotNull { it.countryCode }.distinct()
        val cities = visits.mapNotNull { it.city }.distinct()

        // Transport breakdown
        val distanceByTransport =
            segments
                .groupBy { it.transportType }
                .mapValues { (_, segs) -> segs.sumOf { it.distanceMeters } }

        val durationByTransport =
            segments
                .groupBy { it.transportType }
                .mapValues { (_, segs) ->
                    segs.sumOf { (it.endTime - it.startTime).inWholeSeconds }
                }

        // Speed metrics
        val speeds = samples.mapNotNull { it.speed }
        val averageSpeed = if (speeds.isNotEmpty()) speeds.average() else null
        val maxSpeed = speeds.maxOrNull()

        return RouteStatistics(
            totalDistanceMeters = totalDistance,
            totalDurationSeconds = totalDuration,
            numberOfLocations = visits.size,
            numberOfPhotos = photos.size,
            numberOfVideos = 0, // TODO: Add video support
            countriesVisited = countries,
            citiesVisited = cities,
            distanceByTransport = distanceByTransport,
            durationByTransport = durationByTransport,
            averageSpeedMps = averageSpeed,
            maxSpeedMps = maxSpeed
        )
    }

    /** Calculate Haversine distance between two coordinates. */
    private fun haversineDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val earthRadiusMeters = 6371000.0

        val dLat = (lat2 - lat1) * PI / 180.0
        val dLon = (lon2 - lon1) * PI / 180.0

        val a =
            kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
                kotlin.math.cos(lat1 * PI / 180.0) * kotlin.math.cos(lat2 * PI / 180.0) *
                kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2)

        val c = 2 * kotlin.math.asin(kotlin.math.sqrt(a))

        return earthRadiusMeters * c
    }

    /**
     * Downsample location samples to a target count using evenly-spaced sampling. This preserves temporal distribution
     * while reducing point count for performance.
     *
     * @param samples Full list of location samples (sorted by time)
     * @param targetCount Target number of points
     * @return Downsampled list
     */
    private fun downsampleLocations(
        samples: List<LocationSample>,
        targetCount: Int
    ): List<LocationSample> {
        if (samples.size <= targetCount) return samples
        if (targetCount < 2) return samples.take(1)

        val result = mutableListOf<LocationSample>()

        // Always include first point
        result.add(samples.first())

        // Calculate step size
        val step = (samples.size - 1).toDouble() / (targetCount - 1)

        // Sample evenly across the route
        for (i in 1 until targetCount - 1) {
            val index = (i * step).toInt()
            result.add(samples[index])
        }

        // Always include last point
        result.add(samples.last())

        logger.debug { "Downsampled ${samples.size} points to ${result.size}" }
        return result
    }
}
