package com.po4yka.trailglass.feature.places

import com.po4yka.trailglass.data.repository.PlaceVisitRepository
import com.po4yka.trailglass.domain.model.PlaceVisit
import kotlinx.datetime.Instant
import me.tatarka.inject.annotations.Inject

/**
 * Use case for retrieving place visits for a user within a time range.
 */
@Inject
class GetPlaceVisitsUseCase(
    private val placeVisitRepository: PlaceVisitRepository
) {
    /**
     * Get place visits for a user within a time range.
     *
     * @param userId User ID
     * @param startTime Start of time range
     * @param endTime End of time range
     * @return List of place visits sorted by start time (descending)
     */
    suspend fun execute(
        userId: String,
        startTime: Instant,
        endTime: Instant
    ): Result<List<PlaceVisit>> =
        try {
            val visits = placeVisitRepository.getVisits(userId, startTime, endTime)
            Result.success(visits)
        } catch (e: Exception) {
            Result.failure(e)
        }

    /**
     * Get place visits for a user with pagination.
     *
     * @param userId User ID
     * @param limit Maximum number of visits to return
     * @param offset Number of visits to skip
     * @return List of place visits sorted by start time (descending)
     */
    suspend fun execute(
        userId: String,
        limit: Int = 50,
        offset: Int = 0
    ): Result<List<PlaceVisit>> =
        try {
            val visits = placeVisitRepository.getVisitsByUser(userId, limit, offset)
            Result.success(visits)
        } catch (e: Exception) {
            Result.failure(e)
        }
}
