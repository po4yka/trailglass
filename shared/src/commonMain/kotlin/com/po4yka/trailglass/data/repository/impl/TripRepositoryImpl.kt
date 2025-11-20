package com.po4yka.trailglass.data.repository.impl

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.po4yka.trailglass.data.repository.TripRepository
import com.po4yka.trailglass.data.db.Database
import com.po4yka.trailglass.domain.model.Trip
import com.po4yka.trailglass.logging.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import me.tatarka.inject.annotations.Inject

/**
 * SQLDelight implementation of TripRepository.
 */
@Inject
class TripRepositoryImpl(
    private val database: Database
) : TripRepository {

    private val logger = logger()
    private val queries = database.tripsQueries

    override suspend fun upsertTrip(trip: Trip) = withContext(Dispatchers.IO) {
        logger.debug { "Upserting trip: ${trip.id}" }
        try {
            val now = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
            queries.upsertTrip(
                id = trip.id,
                name = trip.name,
                start_time = trip.startTime.toEpochMilliseconds(),
                end_time = trip.endTime?.toEpochMilliseconds(),
                primary_country = trip.primaryCountry,
                is_ongoing = if (trip.isOngoing) 1L else 0L,
                user_id = trip.userId,
                created_at = now,
                updated_at = now,
                deleted_at = null
            )
            logger.trace { "Successfully upserted trip ${trip.id}" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to upsert trip ${trip.id}" }
            throw e
        }
    }

    override suspend fun getTripById(tripId: String): Trip? = withContext(Dispatchers.IO) {
        logger.trace { "Getting trip by ID: $tripId" }
        try {
            queries.getTripById(tripId)
                .asFlow()
                .mapToOneOrNull(Dispatchers.IO)
                .first()
                ?.let { mapToTrip(it) }
                .also {
                    if (it != null) {
                        logger.trace { "Found trip $tripId" }
                    } else {
                        logger.trace { "Trip $tripId not found" }
                    }
                }
        } catch (e: Exception) {
            logger.error(e) { "Failed to get trip $tripId" }
            throw e
        }
    }

    override suspend fun getTripsForUser(userId: String): List<Trip> = withContext(Dispatchers.IO) {
        logger.debug { "Getting all trips for user $userId" }
        try {
            queries.getTripsForUser(userId)
                .asFlow()
                .mapToList(Dispatchers.IO)
                .first()
                .map { mapToTrip(it) }
                .also { trips ->
                    logger.info { "Found ${trips.size} trips for user $userId" }
                }
        } catch (e: Exception) {
            logger.error(e) { "Failed to get trips for user $userId" }
            throw e
        }
    }

    override suspend fun getOngoingTrips(userId: String): List<Trip> = withContext(Dispatchers.IO) {
        logger.debug { "Getting ongoing trips for user $userId" }
        try {
            queries.getOngoingTrips(userId)
                .asFlow()
                .mapToList(Dispatchers.IO)
                .first()
                .map { mapToTrip(it) }
                .also { trips ->
                    logger.debug { "Found ${trips.size} ongoing trips for user $userId" }
                }
        } catch (e: Exception) {
            logger.error(e) { "Failed to get ongoing trips for user $userId" }
            throw e
        }
    }

    override suspend fun getTripsInRange(
        userId: String,
        startTime: Instant,
        endTime: Instant
    ): List<Trip> = withContext(Dispatchers.IO) {
        logger.debug { "Getting trips for user $userId in range $startTime to $endTime" }
        try {
            queries.getTripsInRange(
                user_id = userId,
                start_time = startTime.toEpochMilliseconds(),
                end_time = endTime.toEpochMilliseconds()
            )
                .asFlow()
                .mapToList(Dispatchers.IO)
                .first()
                .map { mapToTrip(it) }
                .also { trips ->
                    logger.debug { "Found ${trips.size} trips in range" }
                }
        } catch (e: Exception) {
            logger.error(e) { "Failed to get trips in range for user $userId" }
            throw e
        }
    }

    override suspend fun completeTrip(tripId: String, endTime: Instant) = withContext(Dispatchers.IO) {
        logger.debug { "Marking trip $tripId as complete at $endTime" }
        try {
            val now = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
            queries.completeTrip(
                end_time = endTime.toEpochMilliseconds(),
                updated_at = now,
                id = tripId
            )
            logger.info { "Trip $tripId marked as complete" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to complete trip $tripId" }
            throw e
        }
    }

    override suspend fun deleteTrip(tripId: String) = withContext(Dispatchers.IO) {
        logger.debug { "Deleting trip $tripId" }
        try {
            queries.deleteTrip(tripId)
            logger.info { "Trip $tripId deleted" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to delete trip $tripId" }
            throw e
        }
    }

    override suspend fun getTripCount(userId: String): Long = withContext(Dispatchers.IO) {
        logger.trace { "Getting trip count for user $userId" }
        try {
            queries.getTripCount(userId)
                .executeAsOne()
                .also { count ->
                    logger.trace { "User $userId has $count trips" }
                }
        } catch (e: Exception) {
            logger.error(e) { "Failed to get trip count for user $userId" }
            throw e
        }
    }

    /**
     * Map database row to Trip domain object.
     */
    private fun mapToTrip(row: com.po4yka.trailglass.db.Trips): Trip {
        return Trip(
            id = row.id,
            name = row.name,
            startTime = Instant.fromEpochMilliseconds(row.start_time),
            endTime = row.end_time?.let { Instant.fromEpochMilliseconds(it) },
            primaryCountry = row.primary_country,
            isOngoing = row.is_ongoing == 1L,
            userId = row.user_id
        )
    }
}
