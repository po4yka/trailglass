package com.po4yka.trailglass.data.repository

import com.po4yka.trailglass.domain.model.LocationSample
import com.po4yka.trailglass.domain.model.PlaceVisit
import kotlinx.datetime.Instant

/**
 * Repository for managing place visits.
 */
interface PlaceVisitRepository {

    /**
     * Insert a new place visit.
     */
    suspend fun insertVisit(visit: PlaceVisit)

    /**
     * Get a place visit by ID.
     */
    suspend fun getVisitById(id: String): PlaceVisit?

    /**
     * Get place visits for a user within a time range.
     */
    suspend fun getVisits(
        userId: String,
        startTime: Instant,
        endTime: Instant
    ): List<PlaceVisit>

    /**
     * Get place visits for a user with pagination.
     */
    suspend fun getVisitsByUser(
        userId: String,
        limit: Int = 50,
        offset: Int = 0
    ): List<PlaceVisit>

    /**
     * Update an existing place visit.
     */
    suspend fun updateVisit(visit: PlaceVisit)

    /**
     * Link location samples to a place visit.
     */
    suspend fun linkSamples(visitId: String, sampleIds: List<String>)

    /**
     * Unlink a location sample from a place visit.
     */
    suspend fun unlinkSample(visitId: String, sampleId: String)

    /**
     * Get all location samples linked to a place visit.
     */
    suspend fun getSamplesForVisit(visitId: String): List<LocationSample>

    /**
     * Soft delete a place visit.
     */
    suspend fun deleteVisit(id: String)
}
