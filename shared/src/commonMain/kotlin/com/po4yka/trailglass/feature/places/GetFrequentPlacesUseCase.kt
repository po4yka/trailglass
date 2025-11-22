package com.po4yka.trailglass.feature.places

import com.po4yka.trailglass.data.repository.FrequentPlaceRepository
import com.po4yka.trailglass.data.repository.PlaceVisitRepository
import com.po4yka.trailglass.domain.model.FrequentPlace
import com.po4yka.trailglass.domain.model.PlaceSignificance
import com.po4yka.trailglass.logging.logger
import kotlinx.datetime.Clock
import me.tatarka.inject.annotations.Inject
import kotlin.time.Duration.Companion.days

/**
 * Use case for getting and managing frequent places.
 */
@Inject
class GetFrequentPlacesUseCase(
    private val frequentPlaceRepository: FrequentPlaceRepository,
    private val placeVisitRepository: PlaceVisitRepository,
    private val placeClusterer: PlaceClusterer
) {
    private val logger = logger()

    /**
     * Get all frequent places for a user, sorted by significance and visit count.
     *
     * @param userId User ID
     * @param minSignificance Minimum significance level to include
     * @return List of frequent places
     */
    suspend fun execute(
        userId: String,
        minSignificance: PlaceSignificance = PlaceSignificance.RARE
    ): Result<List<FrequentPlace>> =
        try {
            val places = frequentPlaceRepository.getPlacesByUser(userId)

            // Filter by significance
            val filtered =
                places.filter { place ->
                    place.significance.ordinal >= minSignificance.ordinal
                }

            // Sort by significance (PRIMARY first) then by visit count
            val sorted =
                filtered.sortedWith(
                    compareByDescending<FrequentPlace> { it.significance.ordinal }
                        .thenByDescending { it.visitCount }
                )

            Result.success(sorted)
        } catch (e: Exception) {
            logger.error(e) { "Failed to get frequent places for user $userId" }
            Result.failure(e)
        }

    /**
     * Refresh frequent places by re-clustering all place visits.
     * This should be run periodically to update the frequent places based on new visits.
     *
     * @param userId User ID
     * @return Updated list of frequent places
     */
    suspend fun refresh(userId: String): Result<List<FrequentPlace>> =
        try {
            logger.info { "Refreshing frequent places for user $userId" }

            // Get all place visits for the user (last 90 days for performance)
            val cutoffTime = Clock.System.now() - 90.days
            val endTime = Clock.System.now()
            val visits = placeVisitRepository.getVisits(userId, cutoffTime, endTime)

            logger.debug { "Found ${visits.size} visits to cluster" }

            // Get existing frequent places
            val existingPlaces = frequentPlaceRepository.getPlacesByUser(userId)

            // Update frequent places with new visits
            val updatedPlaces =
                placeClusterer.updateFrequentPlaces(
                    visits = visits,
                    existingPlaces = existingPlaces,
                    userId = userId
                )

            logger.info { "Updated ${updatedPlaces.size} frequent places" }

            // Save updated places
            updatedPlaces.forEach { place ->
                val existing = frequentPlaceRepository.getPlaceById(place.id)
                if (existing != null) {
                    frequentPlaceRepository.updatePlace(place)
                } else {
                    frequentPlaceRepository.insertPlace(place)
                }
            }

            Result.success(updatedPlaces)
        } catch (e: Exception) {
            logger.error(e) { "Failed to refresh frequent places for user $userId" }
            Result.failure(e)
        }
}
