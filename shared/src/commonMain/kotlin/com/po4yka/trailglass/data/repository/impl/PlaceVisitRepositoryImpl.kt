package com.po4yka.trailglass.data.repository.impl

import com.po4yka.trailglass.data.auth.DefaultUserSession
import com.po4yka.trailglass.data.db.Database
import com.po4yka.trailglass.data.repository.PlaceVisitRepository
import com.po4yka.trailglass.domain.model.CategoryConfidence
import com.po4yka.trailglass.domain.model.LocationSample
import com.po4yka.trailglass.domain.model.LocationSource
import com.po4yka.trailglass.domain.model.PlaceCategory
import com.po4yka.trailglass.domain.model.PlaceSignificance
import com.po4yka.trailglass.domain.model.PlaceVisit
import com.po4yka.trailglass.logging.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import me.tatarka.inject.annotations.Inject

/**
 * SQLDelight implementation of PlaceVisitRepository.
 */
@Inject
class PlaceVisitRepositoryImpl(
    private val database: Database
) : PlaceVisitRepository {

    private val visitQueries = database.placeVisitQueries
    private val logger = logger()

    override suspend fun insertVisit(visit: PlaceVisit) = withContext(Dispatchers.IO) {
        logger.info { "Inserting place visit: ${visit.id} at ${visit.city ?: "(${visit.centerLatitude}, ${visit.centerLongitude})"}" }
        try {
            // Insert the visit
            visitQueries.insertVisit(
            id = visit.id,
            start_time = visit.startTime.toEpochMilliseconds(),
            end_time = visit.endTime.toEpochMilliseconds(),
            center_latitude = visit.centerLatitude,
            center_longitude = visit.centerLongitude,
            approximate_address = visit.approximateAddress,
            poi_name = visit.poiName,
            city = visit.city,
            country_code = visit.countryCode,
            category = visit.category.name,
            category_confidence = visit.categoryConfidence.name,
            significance = visit.significance.name,
            user_label = visit.userLabel,
            user_notes = visit.userNotes,
            is_favorite = if (visit.isFavorite) 1L else 0L,
            frequent_place_id = visit.frequentPlaceId,
            user_id = visit.userId ?: DefaultUserSession.getInstance().getCurrentUserId() ?: "anonymous",
            created_at = visit.createdAt?.toEpochMilliseconds() ?: Clock.System.now().toEpochMilliseconds(),
            updated_at = visit.updatedAt?.toEpochMilliseconds() ?: Clock.System.now().toEpochMilliseconds(),
            deleted_at = null
        )

            // Link samples
            visit.locationSampleIds.forEach { sampleId ->
                visitQueries.linkSample(visit.id, sampleId)
            }
            logger.debug { "Successfully inserted place visit ${visit.id} with ${visit.locationSampleIds.size} linked samples" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to insert place visit ${visit.id}" }
            throw e
        }
    }

    override suspend fun getVisitById(id: String): PlaceVisit? = withContext(Dispatchers.IO) {
        val visit = visitQueries.getVisitById(id).executeAsOneOrNull() ?: return@withContext null
        val sampleIds = visitQueries.getSamplesForVisit(id)
            .executeAsList()
            .map { it.id }

        visit.toPlaceVisit(sampleIds)
    }

    override suspend fun getVisits(
        userId: String,
        startTime: Instant,
        endTime: Instant
    ): List<PlaceVisit> = withContext(Dispatchers.IO) {
        visitQueries.getVisitsByTimeRange(
            user_id = userId,
            query_end_time = endTime.toEpochMilliseconds(),
            query_start_time = startTime.toEpochMilliseconds()
        ).executeAsList().map { visit ->
            val sampleIds = visitQueries.getSamplesForVisit(visit.id)
                .executeAsList()
                .map { it.id }
            visit.toPlaceVisit(sampleIds)
        }
    }

    override suspend fun getVisitsByUser(
        userId: String,
        limit: Int,
        offset: Int
    ): List<PlaceVisit> = withContext(Dispatchers.IO) {
        visitQueries.getVisitsByUser(userId, limit.toLong(), offset.toLong())
            .executeAsList()
            .map { visit ->
                val sampleIds = visitQueries.getSamplesForVisit(visit.id)
                    .executeAsList()
                    .map { it.id }
                visit.toPlaceVisit(sampleIds)
            }
    }

    override suspend fun updateVisit(visit: PlaceVisit): Unit = withContext(Dispatchers.IO) {
        logger.debug { "Updating visit: ${visit.id}" }
        visitQueries.updateVisit(
            start_time = visit.startTime.toEpochMilliseconds(),
            end_time = visit.endTime.toEpochMilliseconds(),
            center_latitude = visit.centerLatitude,
            center_longitude = visit.centerLongitude,
            approximate_address = visit.approximateAddress,
            poi_name = visit.poiName,
            city = visit.city,
            country_code = visit.countryCode,
            category = visit.category.name,
            category_confidence = visit.categoryConfidence.name,
            significance = visit.significance.name,
            user_label = visit.userLabel,
            user_notes = visit.userNotes,
            is_favorite = if (visit.isFavorite) 1L else 0L,
            frequent_place_id = visit.frequentPlaceId,
            updated_at = Clock.System.now().toEpochMilliseconds(),
            id = visit.id
        )
    }

    override suspend fun linkSamples(visitId: String, sampleIds: List<String>) =
        withContext(Dispatchers.IO) {
            sampleIds.forEach { sampleId ->
                visitQueries.linkSample(visitId, sampleId)
            }
        }

    override suspend fun unlinkSample(visitId: String, sampleId: String): Unit =
        withContext(Dispatchers.IO) {
            logger.debug { "Unlinking sample $sampleId from visit $visitId" }
            visitQueries.unlinkSample(visitId, sampleId)
        }

    override suspend fun getSamplesForVisit(visitId: String): List<LocationSample> =
        withContext(Dispatchers.IO) {
            visitQueries.getSamplesForVisit(visitId)
                .executeAsList()
                .map { it.toLocationSample() }
        }

    override suspend fun deleteVisit(id: String): Unit = withContext(Dispatchers.IO) {
        logger.debug { "Deleting visit: $id" }
        val now = Clock.System.now().toEpochMilliseconds()
        visitQueries.softDelete(deleted_at = now, updated_at = now, id = id)
    }

    private fun com.po4yka.trailglass.db.Place_visits.toPlaceVisit(sampleIds: List<String>) =
        PlaceVisit(
            id = id,
            startTime = Instant.fromEpochMilliseconds(start_time),
            endTime = Instant.fromEpochMilliseconds(end_time),
            centerLatitude = center_latitude,
            centerLongitude = center_longitude,
            approximateAddress = approximate_address,
            poiName = poi_name,
            city = city,
            countryCode = country_code,
            locationSampleIds = sampleIds,
            category = PlaceCategory.valueOf(category),
            categoryConfidence = CategoryConfidence.valueOf(category_confidence),
            significance = PlaceSignificance.valueOf(significance),
            userLabel = user_label,
            userNotes = user_notes,
            isFavorite = is_favorite != 0L,
            frequentPlaceId = frequent_place_id,
            userId = user_id,
            createdAt = Instant.fromEpochMilliseconds(created_at),
            updatedAt = Instant.fromEpochMilliseconds(updated_at)
        )

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
