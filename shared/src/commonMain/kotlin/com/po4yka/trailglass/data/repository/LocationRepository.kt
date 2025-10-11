package com.po4yka.trailglass.data.repository

import com.po4yka.trailglass.domain.model.LocationSample
import kotlinx.datetime.Instant

/**
 * Repository for managing location samples.
 */
interface LocationRepository {

    /**
     * Insert a new location sample.
     */
    suspend fun insertSample(sample: LocationSample)

    /**
     * Get a location sample by ID.
     */
    suspend fun getSampleById(id: String): LocationSample?

    /**
     * Get location samples for a user within a time range.
     */
    suspend fun getSamples(
        userId: String,
        startTime: Instant,
        endTime: Instant
    ): List<LocationSample>

    /**
     * Get location samples for a specific trip.
     */
    suspend fun getSamplesForTrip(tripId: String): List<LocationSample>

    /**
     * Get unprocessed samples (not assigned to a trip yet).
     */
    suspend fun getUnprocessedSamples(userId: String, limit: Int): List<LocationSample>

    /**
     * Update the trip ID for a location sample.
     */
    suspend fun updateTripId(sampleId: String, tripId: String?)

    /**
     * Soft delete a location sample.
     */
    suspend fun deleteSample(id: String)

    /**
     * Permanently delete old samples.
     */
    suspend fun deleteOldSamples(userId: String, beforeTime: Instant)
}
