package com.po4yka.trailglass.feature.trips

import com.po4yka.trailglass.data.repository.TripRepository
import com.po4yka.trailglass.domain.model.Trip
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import me.tatarka.inject.annotations.Inject
import kotlin.random.Random

/**
 * Use case for creating a new trip (manual or from detection).
 */
@Inject
class CreateTripUseCase(
    private val tripRepository: TripRepository
) {
    /**
     * Create a new trip.
     *
     * @param userId User ID
     * @param name Trip name
     * @param startTime Trip start time
     * @param endTime Trip end time (null if ongoing)
     * @param description Optional description
     * @param isAutoDetected Whether this was auto-detected
     * @return Result with created Trip or error
     */
    suspend fun execute(
        userId: String,
        name: String? = null,
        startTime: Instant,
        endTime: Instant? = null,
        description: String? = null,
        isAutoDetected: Boolean = false,
        detectionConfidence: Float = 0f
    ): Result<Trip> {
        return try {
            val now = Clock.System.now()

            val trip = Trip(
                id = generateTripId(),
                name = name,
                startTime = startTime,
                endTime = endTime,
                isOngoing = endTime == null,
                userId = userId,
                description = description,
                isAutoDetected = isAutoDetected,
                detectionConfidence = detectionConfidence,
                createdAt = now,
                updatedAt = now
            )

            tripRepository.upsertTrip(trip)

            Result.success(trip)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun generateTripId(): String {
        return "trip_${Clock.System.now().toEpochMilliseconds()}_${Random.nextInt(10000)}"
    }
}
