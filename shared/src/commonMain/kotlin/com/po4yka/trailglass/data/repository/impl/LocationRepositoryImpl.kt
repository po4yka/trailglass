package com.po4yka.trailglass.data.repository.impl

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.po4yka.trailglass.data.db.Database
import com.po4yka.trailglass.data.repository.LocationRepository
import com.po4yka.trailglass.domain.model.LocationSample
import com.po4yka.trailglass.domain.model.LocationSource
import com.po4yka.trailglass.logging.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * SQLDelight implementation of LocationRepository.
 */
class LocationRepositoryImpl(
    private val database: Database
) : LocationRepository {

    private val queries = database.locationSampleQueries
    private val logger = logger()

    override suspend fun insertSample(sample: LocationSample) = withContext(Dispatchers.IO) {
        logger.debug { "Inserting location sample: ${sample.id} at (${sample.latitude}, ${sample.longitude})" }
        try {
            queries.insertSample(
                id = sample.id,
                timestamp = sample.timestamp.toEpochMilliseconds(),
                latitude = sample.latitude,
                longitude = sample.longitude,
                accuracy = sample.accuracy,
                speed = sample.speed,
                bearing = sample.bearing,
                source = sample.source.name,
                trip_id = sample.tripId,
                uploaded_at = sample.uploadedAt?.toEpochMilliseconds(),
                device_id = sample.deviceId,
                user_id = sample.userId,
                created_at = Clock.System.now().toEpochMilliseconds(),
                updated_at = Clock.System.now().toEpochMilliseconds(),
                deleted_at = null
            )
            logger.trace { "Successfully inserted location sample ${sample.id}" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to insert location sample ${sample.id}" }
            throw e
        }
    }

    override suspend fun getSampleById(id: String): LocationSample? = withContext(Dispatchers.IO) {
        queries.getSampleById(id).executeAsOneOrNull()?.toLocationSample()
    }

    override suspend fun getSamples(
        userId: String,
        startTime: Instant,
        endTime: Instant
    ): List<LocationSample> = withContext(Dispatchers.IO) {
        logger.debug { "Fetching samples for user $userId from $startTime to $endTime" }
        val samples = queries.getSamplesByTimeRange(
            user_id = userId,
            start = startTime.toEpochMilliseconds(),
            end = endTime.toEpochMilliseconds()
        ).executeAsList().map { it.toLocationSample() }
        logger.info { "Found ${samples.size} location samples for user $userId" }
        samples
    }

    override suspend fun getSamplesForTrip(tripId: String): List<LocationSample> =
        withContext(Dispatchers.IO) {
            queries.getSamplesByTrip(tripId).executeAsList().map { it.toLocationSample() }
        }

    override suspend fun getUnprocessedSamples(userId: String, limit: Int): List<LocationSample> =
        withContext(Dispatchers.IO) {
            queries.getUnprocessedSamples(userId, limit.toLong())
                .executeAsList()
                .map { it.toLocationSample() }
        }

    override suspend fun updateTripId(sampleId: String, tripId: String?) =
        withContext(Dispatchers.IO) {
            queries.updateTripId(
                trip_id = tripId,
                updated_at = Clock.System.now().toEpochMilliseconds(),
                id = sampleId
            )
        }

    override suspend fun deleteSample(id: String) = withContext(Dispatchers.IO) {
        val now = Clock.System.now().toEpochMilliseconds()
        queries.softDelete(deleted_at = now, updated_at = now, id = id)
    }

    override suspend fun deleteOldSamples(userId: String, beforeTime: Instant) =
        withContext(Dispatchers.IO) {
            queries.deleteOldSamples(userId, beforeTime.toEpochMilliseconds())
        }

    private fun com.po4yka.trailglass.db.Location_samples.toLocationSample() = LocationSample(
        id = id,
        timestamp = Instant.fromEpochMilliseconds(timestamp),
        latitude = latitude,
        longitude = longitude,
        accuracy = accuracy,
        speed = speed,
        bearing = bearing,
        source = LocationSource.valueOf(source),
        tripId = trip_id,
        uploadedAt = uploaded_at?.let { Instant.fromEpochMilliseconds(it) },
        deviceId = device_id,
        userId = user_id
    )
}
