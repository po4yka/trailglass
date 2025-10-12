package com.po4yka.trailglass.location

import com.po4yka.trailglass.domain.model.*
import com.po4yka.trailglass.location.trip.TripDayAggregator
import com.po4yka.trailglass.location.trip.TripDetector
import com.po4yka.trailglass.logging.logger

/**
 * Coordinates the entire location processing pipeline.
 * Takes raw location samples and produces structured timeline data.
 */
class LocationProcessor(
    private val placeVisitProcessor: PlaceVisitProcessor,
    private val routeSegmentBuilder: RouteSegmentBuilder = RouteSegmentBuilder(),
    private val tripDetector: TripDetector = TripDetector(),
    private val tripDayAggregator: TripDayAggregator = TripDayAggregator()
) {

    private val logger = logger()

    /**
     * Result of processing location data.
     */
    data class ProcessingResult(
        val visits: List<PlaceVisit>,
        val routes: List<RouteSegment>,
        val trips: List<Trip>,
        val tripDays: List<TripDay>
    )

    /**
     * Process location samples into structured timeline data.
     *
     * This is the main entry point for the processing pipeline:
     * 1. Detect place visits from location samples
     * 2. Build route segments between visits
     * 3. Detect trips based on distance from home
     * 4. Aggregate daily timelines for each trip
     *
     * @param samples Raw location samples to process
     * @param userId User ID for the data
     * @return Complete processing result with all derived data
     */
    suspend fun processLocationData(
        samples: List<LocationSample>,
        userId: String
    ): ProcessingResult {
        logger.info { "Processing ${samples.size} location samples for user $userId" }

        if (samples.isEmpty()) {
            logger.warn { "No samples to process" }
            return ProcessingResult(
                visits = emptyList(),
                routes = emptyList(),
                trips = emptyList(),
                tripDays = emptyList()
            )
        }

        // Step 1: Detect place visits
        logger.info { "Step 1/4: Detecting place visits" }
        val visits = placeVisitProcessor.detectPlaceVisits(samples)
        logger.info { "Detected ${visits.size} place visits" }

        // Step 2: Build route segments between visits
        logger.info { "Step 2/4: Building route segments" }
        val routes = routeSegmentBuilder.buildSegments(samples, visits)
        logger.info { "Built ${routes.size} route segments" }

        // Step 3: Detect trips
        logger.info { "Step 3/4: Detecting trips" }
        val trips = tripDetector.detectTrips(visits, userId)
        logger.info { "Detected ${trips.size} trips" }

        // Step 4: Aggregate daily timelines for each trip
        logger.info { "Step 4/4: Building daily timelines" }
        val tripDays = trips.flatMap { trip ->
            tripDayAggregator.aggregateTripDays(trip, visits, routes)
        }
        logger.info { "Built ${tripDays.size} trip days across ${trips.size} trips" }

        val result = ProcessingResult(
            visits = visits,
            routes = routes,
            trips = trips,
            tripDays = tripDays
        )

        logger.info {
            "Processing complete: ${visits.size} visits, ${routes.size} routes, " +
            "${trips.size} trips, ${tripDays.size} days"
        }

        return result
    }

    /**
     * Reprocess a specific date range.
     * Useful for incremental updates when new data arrives.
     *
     * @param samples Location samples in the date range
     * @param existingVisits Previously detected visits (for context)
     * @param userId User ID
     * @return Processing result for the specified range
     */
    suspend fun reprocessDateRange(
        samples: List<LocationSample>,
        existingVisits: List<PlaceVisit>,
        userId: String
    ): ProcessingResult {
        logger.info { "Reprocessing ${samples.size} samples with ${existingVisits.size} existing visits" }

        // For reprocessing, we need to include nearby existing visits for context
        // to properly detect route continuity
        val allSamples = samples.sortedBy { it.timestamp }

        // Detect new visits
        val newVisits = placeVisitProcessor.detectPlaceVisits(allSamples)

        // Combine with existing visits and deduplicate
        val allVisits = (existingVisits + newVisits)
            .distinctBy { it.id }
            .sortedBy { it.startTime }

        // Build routes
        val routes = routeSegmentBuilder.buildSegments(allSamples, allVisits)

        // Detect trips (this will re-analyze all visits including existing ones)
        val trips = tripDetector.detectTrips(allVisits, userId)

        // Build timelines
        val tripDays = trips.flatMap { trip ->
            tripDayAggregator.aggregateTripDays(trip, allVisits, routes)
        }

        logger.info {
            "Reprocessing complete: ${newVisits.size} new visits, " +
            "${routes.size} routes, ${trips.size} trips"
        }

        return ProcessingResult(
            visits = newVisits,
            routes = routes,
            trips = trips,
            tripDays = tripDays
        )
    }
}
