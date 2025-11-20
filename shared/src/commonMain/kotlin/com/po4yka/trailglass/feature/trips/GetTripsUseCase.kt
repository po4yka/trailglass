package com.po4yka.trailglass.feature.trips

import com.po4yka.trailglass.data.repository.TripRepository
import com.po4yka.trailglass.domain.model.Trip
import com.po4yka.trailglass.logging.logger
import kotlinx.datetime.Instant
import me.tatarka.inject.annotations.Inject
import com.po4yka.trailglass.domain.error.Result as TrailGlassResult
import com.po4yka.trailglass.domain.error.TrailGlassError

/**
 * Use case for retrieving trips.
 */
@Inject
class GetTripsUseCase(
    private val tripRepository: TripRepository
) {
    private val logger = logger()

    /**
     * Get all trips for a user, sorted by start time (descending).
     *
     * @param userId User ID
     * @return Result with list of trips or error
     */
    suspend fun execute(userId: String): TrailGlassResult<List<Trip>> {
        return try {
            val trips = tripRepository.getTripsForUser(userId)
                .sortedByDescending { it.startTime }

            TrailGlassResult.Success(trips)
        } catch (e: Exception) {
            logger.error(e) { "Failed to get trips for user $userId" }
            TrailGlassResult.Error(TrailGlassError.Unknown(e.message ?: "Unknown error", e))
        }
    }

    /**
     * Get a single trip by ID.
     *
     * @param tripId Trip ID
     * @return Result with Trip or error
     */
    suspend fun getById(tripId: String): TrailGlassResult<Trip> {
        return try {
            val trip = tripRepository.getTripById(tripId)
                ?: return TrailGlassResult.Error(TrailGlassError.Unknown("Trip not found: $tripId"))

            TrailGlassResult.Success(trip)
        } catch (e: Exception) {
            logger.error(e) { "Failed to get trip $tripId" }
            TrailGlassResult.Error(TrailGlassError.Unknown(e.message ?: "Unknown error", e))
        }
    }

    /**
     * Get ongoing trips for a user.
     *
     * @param userId User ID
     * @return Result with list of ongoing trips or error
     */
    suspend fun getOngoing(userId: String): TrailGlassResult<List<Trip>> {
        return try {
            val trips = tripRepository.getOngoingTrips(userId)
                .sortedByDescending { it.startTime }

            TrailGlassResult.Success(trips)
        } catch (e: Exception) {
            logger.error(e) { "Failed to get ongoing trips for user $userId" }
            TrailGlassResult.Error(TrailGlassError.Unknown(e.message ?: "Unknown error", e))
        }
    }

    /**
     * Get trips within a time range.
     *
     * @param userId User ID
     * @param startTime Range start
     * @param endTime Range end
     * @return Result with list of trips or error
     */
    suspend fun getInRange(
        userId: String,
        startTime: Instant,
        endTime: Instant
    ): TrailGlassResult<List<Trip>> {
        return try {
            val trips = tripRepository.getTripsInRange(userId, startTime, endTime)
                .sortedByDescending { it.startTime }

            TrailGlassResult.Success(trips)
        } catch (e: Exception) {
            logger.error(e) { "Failed to get trips in range for user $userId" }
            TrailGlassResult.Error(TrailGlassError.Unknown(e.message ?: "Unknown error", e))
        }
    }
}
