package com.po4yka.trailglass.data.repository

import com.po4yka.trailglass.domain.error.Result
import com.po4yka.trailglass.domain.model.LocationSample
import kotlinx.datetime.Instant

/** Repository for managing location samples. All operations return Result<T> for consistent error handling. */
interface LocationRepository {
    /** Insert a new location sample. */
    suspend fun insertSample(sample: LocationSample): Result<Unit>

    /** Get a location sample by ID. */
    suspend fun getSampleById(id: String): Result<LocationSample?>

    /** Get location samples for a user within a time range. */
    suspend fun getSamples(
        userId: String,
        startTime: Instant,
        endTime: Instant
    ): Result<List<LocationSample>>

    /** Get location samples for a specific trip. */
    suspend fun getSamplesForTrip(tripId: String): Result<List<LocationSample>>

    /** Get unprocessed samples (not assigned to a trip yet). */
    suspend fun getUnprocessedSamples(
        userId: String,
        limit: Int
    ): Result<List<LocationSample>>

    /** Update the trip ID for a location sample. */
    suspend fun updateTripId(
        sampleId: String,
        tripId: String?
    ): Result<Unit>

    /** Soft delete a location sample. */
    suspend fun deleteSample(id: String): Result<Unit>

    /** Permanently delete old samples. */
    suspend fun deleteOldSamples(
        userId: String,
        beforeTime: Instant
    ): Result<Unit>
}
