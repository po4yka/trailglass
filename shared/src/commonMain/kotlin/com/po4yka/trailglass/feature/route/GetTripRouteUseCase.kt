package com.po4yka.trailglass.feature.route

import com.po4yka.trailglass.data.repository.*
import com.po4yka.trailglass.domain.model.TripRoute
import com.po4yka.trailglass.logging.logger
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.tatarka.inject.annotations.Inject
import com.po4yka.trailglass.domain.error.Result as TrailGlassResult
import com.po4yka.trailglass.domain.error.TrailGlassError

/**
 * Use case for retrieving a complete trip route with all associated data.
 * This is the main entry point for getting route data for visualization and replay.
 *
 * Includes caching to avoid reprocessing the same trip multiple times.
 */
@Inject
class GetTripRouteUseCase(
    private val tripRepository: TripRepository,
    private val locationRepository: LocationRepository,
    private val routeSegmentRepository: RouteSegmentRepository,
    private val placeVisitRepository: PlaceVisitRepository,
    private val photoRepository: PhotoRepository,
    private val locationSampleFilter: LocationSampleFilter = LocationSampleFilter(),
    private val tripRouteBuilder: TripRouteBuilder = TripRouteBuilder(),
    private val enableCache: Boolean = true,
    private val maxCacheSize: Int = 10
) {

    private val logger = logger()

    // Thread-safe cache for trip routes
    private val cache = mutableMapOf<String, CachedRoute>()
    private val cacheMutex = Mutex()

    private data class CachedRoute(
        val tripRoute: TripRoute,
        val cachedAt: kotlinx.datetime.Instant
    )

    /**
     * Get complete route data for a trip.
     *
     * @param tripId Trip identifier
     * @param forceRefresh If true, bypass cache and fetch fresh data
     * @return Result containing TripRoute or error
     */
    suspend fun execute(tripId: String, forceRefresh: Boolean = false): TrailGlassResult<TripRoute> {
        return try {
            // Check cache first (unless force refresh)
            if (enableCache && !forceRefresh) {
                val cached = cacheMutex.withLock { cache[tripId] }
                if (cached != null) {
                    logger.debug { "Returning cached route for trip $tripId" }
                    return Result.success(cached.tripRoute)
                }
            }

            logger.info { "Fetching route for trip $tripId" }

            // 1. Get trip details
            val trip = tripRepository.getTripById(tripId)
                ?: return TrailGlassResult.Error(TrailGlassError.Unknown("Trip not found: $tripId"))

            // 2. Get all location samples for the trip time range
            val endTime = trip.endTime ?: kotlinx.datetime.Clock.System.now()
            val samplesResult = locationRepository.getSamples(
                userId = trip.userId,
                startTime = trip.startTime,
                endTime = endTime
            )
            val allSamples = samplesResult.getOrNull()
                ?: return TrailGlassResult.Error(TrailGlassError.Unknown("Failed to fetch location samples for trip $tripId"))

            // 3. Filter and validate location samples
            val filteredSamples = locationSampleFilter.filterAndValidate(allSamples)
            logger.debug { "Filtered ${allSamples.size} samples to ${filteredSamples.size}" }

            // Edge case: No GPS data
            if (filteredSamples.isEmpty()) {
                logger.warn { "No valid location samples for trip $tripId" }
                return Result.failure(
                    IllegalStateException("No GPS data available for this trip. Enable location tracking to see routes.")
                )
            }

            // Edge case: Very short route (< 2 points)
            if (filteredSamples.size < 2) {
                logger.warn { "Trip $tripId has only ${filteredSamples.size} location point(s)" }
                // Continue processing but log warning
            }

            // Edge case: Very long route (> 100k points)
            if (filteredSamples.size > 100000) {
                logger.warn {
                    "Trip $tripId has ${filteredSamples.size} points - this may impact performance. " +
                    "Consider splitting into multiple trips."
                }
            }

            // 4. Get route segments
            val routeSegments = try {
                routeSegmentRepository.getRouteSegmentsInRange(
                    userId = trip.userId,
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

            // Cache the result
            if (enableCache) {
                cacheMutex.withLock {
                    // Implement LRU-style eviction if cache is full
                    if (cache.size >= maxCacheSize) {
                        // Remove oldest entry
                        val oldestKey = cache.entries.minByOrNull { it.value.cachedAt }?.key
                        if (oldestKey != null) {
                            cache.remove(oldestKey)
                            logger.debug { "Evicted oldest cached route: $oldestKey" }
                        }
                    }

                    cache[tripId] = CachedRoute(
                        tripRoute = tripRoute,
                        cachedAt = kotlinx.datetime.Clock.System.now()
                    )
                    logger.debug { "Cached route for trip $tripId (cache size: ${cache.size})" }
                }
            }

            TrailGlassResult.Success(tripRoute)

        } catch (e: Exception) {
            logger.error(e) { "Unexpected error fetching trip route for $tripId" }
            TrailGlassResult.Error(TrailGlassError.Unknown(e.message ?: "Unknown error", e))
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

    /**
     * Invalidate cached route for a specific trip.
     * Call this when trip data has been modified.
     *
     * @param tripId Trip identifier to invalidate
     */
    suspend fun invalidateCache(tripId: String) {
        cacheMutex.withLock {
            cache.remove(tripId)
            logger.debug { "Invalidated cache for trip $tripId" }
        }
    }

    /**
     * Clear all cached routes.
     */
    suspend fun clearCache() {
        cacheMutex.withLock {
            val size = cache.size
            cache.clear()
            logger.info { "Cleared route cache ($size entries)" }
        }
    }

    /**
     * Get current cache statistics.
     */
    suspend fun getCacheStats(): CacheStats {
        return cacheMutex.withLock {
            CacheStats(
                size = cache.size,
                maxSize = maxCacheSize,
                enabled = enableCache
            )
        }
    }

    data class CacheStats(
        val size: Int,
        val maxSize: Int,
        val enabled: Boolean
    )
}
