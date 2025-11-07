package com.po4yka.trailglass.feature.route

import com.po4yka.trailglass.data.repository.*
import com.po4yka.trailglass.domain.model.TripRoute
import com.po4yka.trailglass.logging.logger

/**
 * Use case for retrieving a complete trip route with all associated data.
 * This is the main entry point for getting route data for visualization and replay.
 */
class GetTripRouteUseCase(
    private val tripRepository: TripRepository,
    private val locationRepository: LocationRepository,
    private val routeSegmentRepository: RouteSegmentRepository,
    private val placeVisitRepository: PlaceVisitRepository,
    private val photoRepository: PhotoRepository,
    private val locationSampleFilter: LocationSampleFilter = LocationSampleFilter(),
    private val tripRouteBuilder: TripRouteBuilder = TripRouteBuilder()
) {

    private val logger = logger()

    /**
     * Get complete route data for a trip.
     *
     * @param tripId Trip identifier
     * @return Result containing TripRoute or error
     */
    suspend fun execute(tripId: String): Result<TripRoute> {
        return try {
            logger.info { "Fetching route for trip $tripId" }

            // 1. Get trip details
            val trip = tripRepository.getTripById(tripId).getOrElse {
                logger.error(it) { "Failed to fetch trip $tripId" }
                return Result.failure(it)
            }

            // 2. Get all location samples for the trip time range
            val endTime = trip.endTime ?: kotlinx.datetime.Clock.System.now()
            val allSamples = locationRepository.getSamples(
                userId = trip.userId,
                startTime = trip.startTime,
                endTime = endTime
            ).getOrElse {
                logger.error(it) { "Failed to fetch location samples for trip $tripId" }
                return Result.failure(it)
            }

            // 3. Filter and validate location samples
            val filteredSamples = locationSampleFilter.filterAndValidate(allSamples)
            logger.debug { "Filtered ${allSamples.size} samples to ${filteredSamples.size}" }

            if (filteredSamples.isEmpty()) {
                logger.warn { "No valid location samples for trip $tripId" }
                return Result.failure(
                    IllegalStateException("No GPS data available for this trip")
                )
            }

            // 4. Get route segments
            val routeSegments = try {
                routeSegmentRepository.getRouteSegmentsInRange(
                    startTime = trip.startTime,
                    endTime = endTime
                )
            } catch (e: Exception) {
                logger.error(e) { "Failed to fetch route segments for trip $tripId" }
                emptyList()
            }

            // 5. Get place visits
            val placeVisits = try {
                placeVisitRepository.getVisits(
                    userId = trip.userId,
                    startTime = trip.startTime,
                    endTime = endTime
                )
            } catch (e: Exception) {
                logger.error(e) { "Failed to fetch place visits for trip $tripId" }
                emptyList()
            }

            // 6. Get photos with location data
            val photos = try {
                photoRepository.getPhotosInTimeRange(
                    userId = trip.userId,
                    startTime = trip.startTime,
                    endTime = endTime
                )
            } catch (e: Exception) {
                logger.error(e) { "Failed to fetch photos for trip $tripId" }
                emptyList()
            }

            // 7. Build complete trip route
            val tripRoute = tripRouteBuilder.buildTripRoute(
                trip = trip,
                locationSamples = filteredSamples,
                routeSegments = routeSegments,
                placeVisits = placeVisits,
                photos = photos
            )

            logger.info {
                "Successfully built route for trip $tripId: " +
                "${tripRoute.statistics.totalDistanceKilometers.toInt()}km, " +
                "${tripRoute.fullPath.size} points, " +
                "${tripRoute.photoMarkers.size} photos"
            }

            Result.success(tripRoute)

        } catch (e: Exception) {
            logger.error(e) { "Unexpected error fetching trip route for $tripId" }
            Result.failure(e)
        }
    }

    /**
     * Get route data for a memory (date range).
     * Memories are not yet implemented, but this provides the interface for future support.
     *
     * @param memoryId Memory identifier
     * @return Result containing TripRoute or error
     */
    suspend fun executeForMemory(memoryId: String): Result<TripRoute> {
        // TODO: Implement when Memory model is added
        return Result.failure(
            NotImplementedError("Memory route support not yet implemented")
        )
    }
}
