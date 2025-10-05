package com.po4yka.trailglass.feature.photo

import com.po4yka.trailglass.data.repository.PhotoRepository
import com.po4yka.trailglass.domain.model.Photo
import com.po4yka.trailglass.domain.model.PlaceVisit
import com.po4yka.trailglass.logging.logger
import kotlin.math.*

/**
 * Use case for suggesting photos that might belong to a place visit.
 * Uses time and location matching.
 */
class SuggestPhotosForVisitUseCase(
    private val photoRepository: PhotoRepository,
    private val suggestionRadiusMeters: Double = 500.0 // 500m radius
) {

    private val logger = logger()

    /**
     * Suggest photos for a place visit.
     *
     * @param visit The place visit to suggest photos for
     * @param userId User ID
     * @return List of suggested photos, sorted by relevance (best match first)
     */
    suspend fun execute(visit: PlaceVisit, userId: String): List<Photo> {
        logger.debug { "Suggesting photos for visit ${visit.id}" }

        // Get photos within the visit time range
        val photos = photoRepository.getPhotosInTimeRange(
            userId = userId,
            startTime = visit.startTime,
            endTime = visit.endTime
        )

        logger.debug { "Found ${photos.size} photos in time range" }

        // Filter and score photos
        val scoredPhotos = photos.mapNotNull { photo ->
            val score = calculateRelevanceScore(photo, visit)
            if (score > 0.0) {
                photo to score
            } else {
                null
            }
        }

        // Sort by score (descending)
        val suggestedPhotos = scoredPhotos
            .sortedByDescending { it.second }
            .map { it.first }

        logger.info { "Suggesting ${suggestedPhotos.size} photos for visit ${visit.id}" }
        return suggestedPhotos
    }

    /**
     * Calculate relevance score for a photo-visit pair.
     * Higher score = better match.
     *
     * Score factors:
     * - Time overlap (1.0 if exact overlap)
     * - Location proximity (1.0 if within suggestionRadiusMeters)
     */
    private fun calculateRelevanceScore(photo: Photo, visit: PlaceVisit): Double {
        var score = 0.0

        // Time score (1.0 if photo is within visit time)
        val photoTime = photo.timestamp
        if (photoTime >= visit.startTime && photoTime <= visit.endTime) {
            score += 1.0
        } else {
            // Reduce score for photos taken close to visit time
            val timeDiff = minOf(
                abs((photoTime - visit.startTime).inWholeMinutes),
                abs((photoTime - visit.endTime).inWholeMinutes)
            )

            // Decay score based on time difference (30 minutes = 0.5 score)
            score += maxOf(0.0, 1.0 - (timeDiff / 30.0))
        }

        // Location score (if photo has location data)
        if (photo.latitude != null && photo.longitude != null) {
            val distance = haversineDistance(
                photo.latitude,
                photo.longitude,
                visit.centerLatitude,
                visit.centerLongitude
            )

            if (distance <= suggestionRadiusMeters) {
                // Within radius - add full location score
                score += 1.0
            } else {
                // Decay score based on distance (2x radius = 0.5 score)
                score += maxOf(0.0, 1.0 - (distance / (suggestionRadiusMeters * 2)))
            }
        }

        return score
    }

    /**
     * Calculate Haversine distance between two coordinates.
     */
    private fun haversineDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val earthRadiusMeters = 6371000.0

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)

        val c = 2 * asin(sqrt(a))

        return earthRadiusMeters * c
    }
}
