package com.po4yka.trailglass.feature.trips

import com.po4yka.trailglass.data.repository.TripRepository
import com.po4yka.trailglass.domain.model.Trip
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import me.tatarka.inject.annotations.Inject

/**
 * Use case for updating trip information.
 */
@Inject
class UpdateTripUseCase(
    private val tripRepository: TripRepository
) {
    /**
     * Update a trip's editable fields.
     *
     * @param tripId Trip ID to update
     * @param name New name
     * @param description New description
     * @param endTime New end time
     * @param coverPhotoUri Cover photo URI
     * @param tags Trip tags
     * @param isPublic Whether trip is public
     * @return Result with updated Trip or error
     */
    suspend fun execute(
        tripId: String,
        name: String? = null,
        description: String? = null,
        endTime: Instant? = null,
        coverPhotoUri: String? = null,
        tags: List<String>? = null,
        isPublic: Boolean? = null
    ): Result<Trip> {
        return try {
            // Get existing trip
            val existing = tripRepository.getTripById(tripId)
                ?: return Result.failure(IllegalArgumentException("Trip not found: $tripId"))

            // Update with new values
            val updated = existing.copy(
                name = name ?: existing.name,
                description = description ?: existing.description,
                endTime = endTime ?: existing.endTime,
                isOngoing = (endTime ?: existing.endTime) == null,
                coverPhotoUri = coverPhotoUri ?: existing.coverPhotoUri,
                tags = tags ?: existing.tags,
                isPublic = isPublic ?: existing.isPublic,
                updatedAt = Clock.System.now()
            )

            // Save updated trip
            tripRepository.upsertTrip(updated)

            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Complete a trip (mark as ended).
     *
     * @param tripId Trip ID
     * @param endTime End time (defaults to now)
     * @return Result with updated Trip or error
     */
    suspend fun completeTrip(
        tripId: String,
        endTime: Instant = Clock.System.now()
    ): Result<Trip> {
        return try {
            tripRepository.completeTrip(tripId, endTime)

            val updated = tripRepository.getTripById(tripId)
                ?: return Result.failure(IllegalStateException("Trip not found after update"))

            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
