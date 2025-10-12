package com.po4yka.trailglass.data.repository.impl

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.po4yka.trailglass.data.repository.RouteSegmentRepository
import com.po4yka.trailglass.db.TrailGlassDatabase
import com.po4yka.trailglass.domain.model.Coordinate
import com.po4yka.trailglass.domain.model.RouteSegment
import com.po4yka.trailglass.domain.model.TransportType
import com.po4yka.trailglass.logging.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import me.tatarka.inject.annotations.Inject

/**
 * SQLDelight implementation of RouteSegmentRepository.
 */
@Inject
class RouteSegmentRepositoryImpl(
    private val database: TrailGlassDatabase
) : RouteSegmentRepository {

    private val logger = logger()
    private val queries = database.routeSegmentsQueries

    override suspend fun insertRouteSegment(segment: RouteSegment) = withContext(Dispatchers.IO) {
        logger.debug {
            "Inserting route segment: ${segment.id}, " +
            "transport: ${segment.transportType}, distance: ${segment.distanceMeters.toInt()}m"
        }
        try {
            database.transaction {
                // Insert main route segment
                queries.insertRouteSegment(
                    id = segment.id,
                    start_time = segment.startTime.toEpochMilliseconds(),
                    end_time = segment.endTime.toEpochMilliseconds(),
                    from_place_visit_id = segment.fromPlaceVisitId,
                    to_place_visit_id = segment.toPlaceVisitId,
                    transport_type = segment.transportType.name,
                    distance_meters = segment.distanceMeters,
                    average_speed_mps = segment.averageSpeedMps
                )

                // Insert location sample associations
                segment.locationSampleIds.forEachIndexed { index, sampleId ->
                    queries.insertRouteSegmentSample(
                        route_segment_id = segment.id,
                        location_sample_id = sampleId,
                        sequence_number = index.toLong()
                    )
                }

                // Insert simplified path points
                segment.simplifiedPath.forEachIndexed { index, coordinate ->
                    queries.insertSimplifiedPathPoint(
                        route_segment_id = segment.id,
                        sequence_number = index.toLong(),
                        latitude = coordinate.latitude,
                        longitude = coordinate.longitude
                    )
                }
            }
            logger.trace {
                "Successfully inserted route segment ${segment.id} with " +
                "${segment.locationSampleIds.size} samples and ${segment.simplifiedPath.size} path points"
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to insert route segment ${segment.id}" }
            throw e
        }
    }

    override suspend fun getRouteSegmentById(segmentId: String): RouteSegment? = withContext(Dispatchers.IO) {
        logger.trace { "Getting route segment by ID: $segmentId" }
        try {
            val segmentRow = queries.getRouteSegmentById(segmentId)
                .asFlow()
                .mapToOneOrNull(Dispatchers.IO)
                .first()

            if (segmentRow == null) {
                logger.trace { "Route segment $segmentId not found" }
                return@withContext null
            }

            // Load associated sample IDs
            val sampleIds = queries.getRouteSegmentSamples(segmentId)
                .executeAsList()
                .map { it.location_sample_id }

            // Load simplified path points
            val pathPoints = queries.getSimplifiedPathPoints(segmentId)
                .executeAsList()
                .map { Coordinate(it.latitude, it.longitude) }

            logger.trace { "Found route segment $segmentId with ${sampleIds.size} samples and ${pathPoints.size} path points" }

            mapToRouteSegment(segmentRow, sampleIds, pathPoints)
        } catch (e: Exception) {
            logger.error(e) { "Failed to get route segment $segmentId" }
            throw e
        }
    }

    override suspend fun getRouteSegmentsInRange(
        startTime: Instant,
        endTime: Instant
    ): List<RouteSegment> = withContext(Dispatchers.IO) {
        logger.debug { "Getting route segments in range $startTime to $endTime" }
        try {
            val segmentRows = queries.getRouteSegmentsInRange(
                start_time = startTime.toEpochMilliseconds(),
                end_time = endTime.toEpochMilliseconds()
            )
                .asFlow()
                .mapToList(Dispatchers.IO)
                .first()

            // Load samples and paths for each segment
            val segments = segmentRows.map { row ->
                val sampleIds = queries.getRouteSegmentSamples(row.id)
                    .executeAsList()
                    .map { it.location_sample_id }

                val pathPoints = queries.getSimplifiedPathPoints(row.id)
                    .executeAsList()
                    .map { Coordinate(it.latitude, it.longitude) }

                mapToRouteSegment(row, sampleIds, pathPoints)
            }

            logger.info { "Found ${segments.size} route segments in range" }
            segments
        } catch (e: Exception) {
            logger.error(e) { "Failed to get route segments in range" }
            throw e
        }
    }

    override suspend fun getRouteSegmentsForTrip(
        fromVisitId: String?,
        toVisitId: String?
    ): List<RouteSegment> = withContext(Dispatchers.IO) {
        logger.debug { "Getting route segments from $fromVisitId to $toVisitId" }
        try {
            val segmentRows = queries.getRouteSegmentsForVisits(
                from_place_visit_id = fromVisitId,
                to_place_visit_id = toVisitId
            )
                .asFlow()
                .mapToList(Dispatchers.IO)
                .first()

            // Load samples and paths for each segment
            val segments = segmentRows.map { row ->
                val sampleIds = queries.getRouteSegmentSamples(row.id)
                    .executeAsList()
                    .map { it.location_sample_id }

                val pathPoints = queries.getSimplifiedPathPoints(row.id)
                    .executeAsList()
                    .map { Coordinate(it.latitude, it.longitude) }

                mapToRouteSegment(row, sampleIds, pathPoints)
            }

            logger.debug { "Found ${segments.size} route segments" }
            segments
        } catch (e: Exception) {
            logger.error(e) { "Failed to get route segments for visits" }
            throw e
        }
    }

    override suspend fun deleteRouteSegment(segmentId: String) = withContext(Dispatchers.IO) {
        logger.debug { "Deleting route segment $segmentId" }
        try {
            database.transaction {
                queries.deleteSimplifiedPathPoints(segmentId)
                queries.deleteRouteSegmentSamples(segmentId)
                queries.deleteRouteSegment(segmentId)
            }
            logger.info { "Route segment $segmentId deleted" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to delete route segment $segmentId" }
            throw e
        }
    }

    override suspend fun getRouteSegmentCount(): Long = withContext(Dispatchers.IO) {
        logger.trace { "Getting route segment count" }
        try {
            queries.getRouteSegmentCount()
                .executeAsOne()
                .also { count ->
                    logger.trace { "Total route segments: $count" }
                }
        } catch (e: Exception) {
            logger.error(e) { "Failed to get route segment count" }
            throw e
        }
    }

    /**
     * Map database row to RouteSegment domain object.
     */
    private fun mapToRouteSegment(
        row: com.po4yka.trailglass.db.RouteSegment,
        sampleIds: List<String>,
        pathPoints: List<Coordinate>
    ): RouteSegment {
        return RouteSegment(
            id = row.id,
            startTime = Instant.fromEpochMilliseconds(row.start_time),
            endTime = Instant.fromEpochMilliseconds(row.end_time),
            fromPlaceVisitId = row.from_place_visit_id,
            toPlaceVisitId = row.to_place_visit_id,
            locationSampleIds = sampleIds,
            simplifiedPath = pathPoints,
            transportType = TransportType.valueOf(row.transport_type),
            distanceMeters = row.distance_meters,
            averageSpeedMps = row.average_speed_mps
        )
    }
}
