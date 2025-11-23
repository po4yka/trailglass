package com.po4yka.trailglass.data.repository.impl

import com.po4yka.trailglass.data.db.Database
import com.po4yka.trailglass.data.repository.LocationRepository
import com.po4yka.trailglass.domain.error.Result
import com.po4yka.trailglass.domain.error.resultOf
import com.po4yka.trailglass.domain.model.LocationSample
import com.po4yka.trailglass.domain.model.LocationSource
import com.po4yka.trailglass.logging.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import me.tatarka.inject.annotations.Inject

/** SQLDelight implementation of LocationRepository. All operations return Result<T> for consistent error handling. */
@Inject
class LocationRepositoryImpl(
    private val database: Database
) : LocationRepository {
    private val queries = database.locationSampleQueries
    private val logger = logger()

    override suspend fun insertSample(sample: LocationSample): Result<Unit> =
        withContext(Dispatchers.IO) {
            resultOf {
                logger.debug { "Inserting location sample: ${sample.id} at (${sample.latitude}, ${sample.longitude})" }
                queries.insertSample(
                    id = sample.id,
                    timestamp = sample.timestamp.toEpochMilliseconds(),
                    latitude = sample.latitude,
                    longitude = sample.longitude,
                    altitude = sample.altitude,
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
            }
        }

    override suspend fun getSampleById(id: String): Result<LocationSample?> =
        withContext(Dispatchers.IO) {
            resultOf {
                queries.getSampleById(id).executeAsOneOrNull()?.toLocationSample()
            }
        }

    override suspend fun getSamples(
        userId: String,
        startTime: Instant,
        endTime: Instant
    ): Result<List<LocationSample>> =
        withContext(Dispatchers.IO) {
            resultOf {
                logger.debug { "Fetching samples for user $userId from $startTime to $endTime" }
                val samples =
                    queries
                        .getSamplesByTimeRange(
                            user_id = userId,
                            timestamp = startTime.toEpochMilliseconds(),
                            timestamp_ = endTime.toEpochMilliseconds()
                        ).executeAsList()
                        .map { it.toLocationSample() }
                logger.info { "Found ${samples.size} location samples for user $userId" }
                samples
            }
        }

    override suspend fun getSamplesForTrip(tripId: String): Result<List<LocationSample>> =
        withContext(Dispatchers.IO) {
            resultOf {
                queries.getSamplesByTrip(tripId).executeAsList().map { it.toLocationSample() }
            }
        }

    override suspend fun getUnprocessedSamples(
        userId: String,
        limit: Int
    ): Result<List<LocationSample>> =
        withContext(Dispatchers.IO) {
            resultOf {
                queries
                    .getUnprocessedSamples(userId, limit.toLong())
                    .executeAsList()
                    .map { it.toLocationSample() }
            }
        }

    override suspend fun updateTripId(
        sampleId: String,
        tripId: String?
    ): Result<Unit> =
        withContext(Dispatchers.IO) {
            resultOf {
                queries.updateTripId(
                    trip_id = tripId,
                    updated_at = Clock.System.now().toEpochMilliseconds(),
                    id = sampleId
                )
                Unit
            }
        }

    override suspend fun deleteSample(id: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            resultOf {
                val now = Clock.System.now().toEpochMilliseconds()
                queries.softDelete(deleted_at = now, updated_at = now, id = id)
                Unit
            }
        }

    override suspend fun deleteOldSamples(
        userId: String,
        beforeTime: Instant
    ): Result<Unit> =
        withContext(Dispatchers.IO) {
            resultOf {
                queries.deleteOldSamples(userId, beforeTime.toEpochMilliseconds())
                Unit
            }
        }

    private fun com.po4yka.trailglass.db.Location_samples.toLocationSample() =
        LocationSample(
            id = id,
            timestamp = Instant.fromEpochMilliseconds(timestamp),
            latitude = latitude,
            longitude = longitude,
            altitude = altitude,
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
