package com.po4yka.trailglass.feature.photo

import com.po4yka.trailglass.domain.model.Photo
import com.po4yka.trailglass.domain.model.PhotoMetadata
import com.po4yka.trailglass.domain.model.PlaceVisit
import com.po4yka.trailglass.logging.logger
import kotlin.math.*
import kotlin.time.Duration.Companion.hours
import me.tatarka.inject.annotations.Inject

/**
 * Associates photos with place visits based on location and time.
 */
@Inject
class PhotoLocationAssociator {

    private val logger = logger()

    /**
     * Find the best matching visit for a photo based on location and time.
     *
     * @param photo Photo to match
     * @param metadata Photo metadata with location
     * @param visits Candidate place visits
     * @return Best matching visit or null if no good match
     */
    fun findBestMatch(
        photo: Photo,
        metadata: PhotoMetadata?,
        visits: List<PlaceVisit>
    ): PlaceVisit? {
        if (visits.isEmpty()) return null

        // Get photo location
        val photoLat = metadata?.exifLatitude ?: photo.latitude ?: return null
        val photoLon = metadata?.exifLongitude ?: photo.longitude ?: return null
        val photoTime = metadata?.exifTimestampOriginal ?: photo.timestamp

        // Score each visit
        val scoredVisits = visits.map { visit ->
            val score = calculateMatchScore(
                photoLat, photoLon, photoTime,
                visit
            )
            visit to score
        }.filter { it.second > 0.0 }

        if (scoredVisits.isEmpty()) return null

        // Return visit with highest score
        val bestMatch = scoredVisits.maxByOrNull { it.second }

        logger.debug {
            "Best match for photo ${photo.id}: visit ${bestMatch?.first?.id} " +
            "with score ${bestMatch?.second}"
        }

        // Only return if score is above threshold (0.5 = 50% confidence)
        return if (bestMatch != null && bestMatch.second >= 0.5) {
            bestMatch.first
        } else {
            null
        }
    }

    /**
     * Find all suitable visits for a photo (for suggestion UI).
     *
     * @param photo Photo to match
     * @param metadata Photo metadata
     * @param visits Candidate visits
     * @param minScore Minimum score threshold (0.0 to 1.0)
     * @return List of visits sorted by match score
     */
    fun findSuitableVisits(
        photo: Photo,
        metadata: PhotoMetadata?,
        visits: List<PlaceVisit>,
        minScore: Double = 0.3
    ): List<Pair<PlaceVisit, Double>> {
        val photoLat = metadata?.exifLatitude ?: photo.latitude ?: return emptyList()
        val photoLon = metadata?.exifLongitude ?: photo.longitude ?: return emptyList()
        val photoTime = metadata?.exifTimestampOriginal ?: photo.timestamp

        return visits.mapNotNull { visit ->
            val score = calculateMatchScore(photoLat, photoLon, photoTime, visit)
            if (score >= minScore) visit to score else null
        }.sortedByDescending { it.second }
    }

    /**
     * Calculate match score between photo and visit.
     * Returns score from 0.0 (no match) to 1.0 (perfect match).
     */
    private fun calculateMatchScore(
        photoLat: Double,
        photoLon: Double,
        photoTime: kotlinx.datetime.Instant,
        visit: PlaceVisit
    ): Double {
        // Distance score (0.0 to 1.0, decays with distance)
        val distance = calculateDistance(
            photoLat, photoLon,
            visit.centerLatitude, visit.centerLongitude
        )
        val distanceScore = calculateDistanceScore(distance)

        // Time score (0.0 to 1.0, based on overlap and proximity)
        val timeScore = calculateTimeScore(photoTime, visit)

        // Combined score (weighted average)
        val distanceWeight = 0.6
        val timeWeight = 0.4

        val totalScore = (distanceScore * distanceWeight) + (timeScore * timeWeight)

        logger.trace {
            "Match score for visit ${visit.id}: " +
            "distance=${distance.toInt()}m (score=$distanceScore), " +
            "time score=$timeScore, total=$totalScore"
        }

        return totalScore
    }

    /**
     * Calculate distance score from meters.
     * - 0m to 50m: score 1.0 (perfect)
     * - 50m to 200m: score 0.8 to 0.5 (good)
     * - 200m to 1km: score 0.5 to 0.2 (fair)
     * - 1km+: score < 0.2 (poor)
     */
    private fun calculateDistanceScore(meters: Double): Double {
        return when {
            meters <= 50 -> 1.0
            meters <= 200 -> 0.8 - ((meters - 50) / 150) * 0.3 // 0.8 to 0.5
            meters <= 1000 -> 0.5 - ((meters - 200) / 800) * 0.3 // 0.5 to 0.2
            else -> max(0.0, 0.2 - ((meters - 1000) / 5000) * 0.2) // 0.2 to 0.0
        }
    }

    /**
     * Calculate time score based on photo time vs visit time.
     * - During visit: score 1.0 (perfect)
     * - Within 1 hour of visit: score 0.8 (very good)
     * - Within 6 hours: score 0.5 to 0.3 (good)
     * - Beyond 6 hours: score < 0.3 (poor)
     */
    private fun calculateTimeScore(
        photoTime: kotlinx.datetime.Instant,
        visit: PlaceVisit
    ): Double {
        // Check if photo was taken during visit
        if (photoTime >= visit.startTime && photoTime <= visit.endTime) {
            return 1.0
        }

        // Calculate time difference (minimum distance to visit window)
        val timeDiff = minOf(
            abs((photoTime - visit.startTime).inWholeSeconds),
            abs((photoTime - visit.endTime).inWholeSeconds)
        )

        val hours = timeDiff / 3600.0

        return when {
            hours <= 1.0 -> 0.8
            hours <= 6.0 -> 0.5 - ((hours - 1.0) / 5.0) * 0.2 // 0.5 to 0.3
            else -> max(0.0, 0.3 - ((hours - 6.0) / 18.0) * 0.3) // 0.3 to 0.0
        }
    }

    /**
     * Calculate distance between two coordinates using Haversine formula.
     * Returns distance in meters.
     */
    private fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val earthRadiusKm = 6371.0

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadiusKm * c * 1000.0 // Convert to meters
    }
}
