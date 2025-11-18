package com.po4yka.trailglass.feature.trips

import com.po4yka.trailglass.data.repository.TripRepository
import me.tatarka.inject.annotations.Inject

/**
 * Use case for deleting a trip.
 */
@Inject
class DeleteTripUseCase(
    private val tripRepository: TripRepository
) {
    /**
     * Delete a trip by ID.
     *
     * @param tripId Trip ID to delete
     * @return Result indicating success or error
     */
    suspend fun execute(tripId: String): Result<Unit> {
        return try {
            // Verify trip exists before deleting
            val trip = tripRepository.getTripById(tripId)
                ?: return Result.failure(IllegalArgumentException("Trip not found: $tripId"))

            // Delete the trip
            tripRepository.deleteTrip(tripId)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
