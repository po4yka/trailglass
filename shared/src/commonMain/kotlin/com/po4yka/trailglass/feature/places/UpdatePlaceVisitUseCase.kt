package com.po4yka.trailglass.feature.places

import com.po4yka.trailglass.data.repository.PlaceVisitRepository
import com.po4yka.trailglass.domain.model.PlaceCategory
import com.po4yka.trailglass.domain.model.PlaceVisit
import kotlinx.datetime.Clock
import me.tatarka.inject.annotations.Inject

/**
 * Use case for updating place visit information (user labels, categories, etc.).
 */
@Inject
class UpdatePlaceVisitUseCase(
    private val placeVisitRepository: PlaceVisitRepository
) {
    /**
     * Update a place visit's user-defined fields.
     *
     * @param visitId Visit ID to update
     * @param userLabel Optional custom label for the place
     * @param userNotes Optional notes about the visit
     * @param category Optional category override
     * @param isFavorite Whether to mark as favorite
     */
    suspend fun execute(
        visitId: String,
        userLabel: String? = null,
        userNotes: String? = null,
        category: PlaceCategory? = null,
        isFavorite: Boolean? = null
    ): Result<PlaceVisit> {
        return try {
            // Get existing visit
            val existing = placeVisitRepository.getVisitById(visitId)
                ?: return Result.failure(IllegalArgumentException("Visit not found: $visitId"))

            // Update with new values
            val updated = existing.copy(
                userLabel = userLabel ?: existing.userLabel,
                userNotes = userNotes ?: existing.userNotes,
                category = category ?: existing.category,
                isFavorite = isFavorite ?: existing.isFavorite,
                updatedAt = Clock.System.now()
            )

            // Save updated visit
            placeVisitRepository.updateVisit(updated)

            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update multiple visits at once (e.g., when labeling all visits to a frequent place).
     *
     * @param visitIds List of visit IDs to update
     * @param userLabel Optional custom label
     * @param category Optional category
     */
    suspend fun executeBatch(
        visitIds: List<String>,
        userLabel: String? = null,
        category: PlaceCategory? = null
    ): Result<List<PlaceVisit>> {
        return try {
            val updated = visitIds.mapNotNull { visitId ->
                val existing = placeVisitRepository.getVisitById(visitId) ?: return@mapNotNull null

                existing.copy(
                    userLabel = userLabel ?: existing.userLabel,
                    category = category ?: existing.category,
                    updatedAt = Clock.System.now()
                ).also { placeVisitRepository.updateVisit(it) }
            }

            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
