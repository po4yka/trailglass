package com.po4yka.trailglass.location

import com.po4yka.trailglass.data.repository.LocationRepository
import com.po4yka.trailglass.data.repository.PlaceVisitRepository
import com.po4yka.trailglass.domain.model.LocationSample
import com.po4yka.trailglass.domain.model.PlaceVisit
import com.po4yka.trailglass.location.geocoding.ReverseGeocoder
import kotlinx.datetime.Instant

/**
 * Database-backed location service that persists location data.
 * This service integrates reverse geocoding and database storage.
 */
class DatabaseLocationService(
    private val locationRepository: LocationRepository,
    private val placeVisitRepository: PlaceVisitRepository,
    reverseGeocoder: ReverseGeocoder,
    private val userId: String = "default_user" // TODO: Get from auth context
) : LocationRecorder {

    private val placeVisitProcessor = PlaceVisitProcessor(reverseGeocoder)

    override suspend fun recordSample(sample: LocationSample) {
        locationRepository.insertSample(sample)
    }

    override suspend fun processSamples() {
        // Get unprocessed samples
        val unprocessedSamples = locationRepository.getUnprocessedSamples(userId, limit = 1000)

        if (unprocessedSamples.isEmpty()) return

        // Detect place visits with reverse geocoding
        val detectedVisits = placeVisitProcessor.detectPlaceVisits(unprocessedSamples)

        // Store detected visits
        detectedVisits.forEach { visit ->
            placeVisitRepository.insertVisit(visit)
        }
    }

    /**
     * Get location samples for a time range.
     */
    suspend fun getSamples(startTime: Instant, endTime: Instant): List<LocationSample> {
        return locationRepository.getSamples(userId, startTime, endTime)
    }

    /**
     * Get place visits for a time range.
     */
    suspend fun getPlaceVisits(startTime: Instant, endTime: Instant): List<PlaceVisit> {
        return placeVisitRepository.getVisits(userId, startTime, endTime)
    }

    /**
     * Get place visits with pagination.
     */
    suspend fun getPlaceVisits(limit: Int = 50, offset: Int = 0): List<PlaceVisit> {
        return placeVisitRepository.getVisitsByUser(userId, limit, offset)
    }

    /**
     * Clear old location samples.
     */
    suspend fun clearOldSamples(beforeTime: Instant) {
        locationRepository.deleteOldSamples(userId, beforeTime)
    }
}
