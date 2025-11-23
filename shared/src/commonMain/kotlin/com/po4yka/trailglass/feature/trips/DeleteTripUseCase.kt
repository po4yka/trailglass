package com.po4yka.trailglass.feature.trips

import com.po4yka.trailglass.data.repository.TripRepository
import com.po4yka.trailglass.domain.error.TrailGlassError
import me.tatarka.inject.annotations.Inject
import com.po4yka.trailglass.domain.error.Result as TrailGlassResult

/** Use case for deleting a trip. */
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
    suspend fun execute(tripId: String): TrailGlassResult<Unit> {
        return try {
            // Verify trip exists before deleting
            val trip =
                tripRepository.getTripById(tripId)
                    ?: return TrailGlassResult.Error(TrailGlassError.Unknown("Trip not found: $tripId"))

            // Delete the trip
            tripRepository.deleteTrip(tripId)

            TrailGlassResult.Success(Unit)
        } catch (e: Exception) {
            TrailGlassResult.Error(TrailGlassError.Unknown(e.message ?: "Unknown error", e))
        }
    }
}
