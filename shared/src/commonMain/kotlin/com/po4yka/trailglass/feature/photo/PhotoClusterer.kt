package com.po4yka.trailglass.feature.photo

import com.po4yka.trailglass.domain.model.Photo
import com.po4yka.trailglass.domain.model.PhotoCluster
import com.po4yka.trailglass.domain.model.PhotoMetadata
import com.po4yka.trailglass.domain.model.PhotoWithMetadata
import com.po4yka.trailglass.logging.logger
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.UUID
import kotlin.math.*
import kotlin.time.Duration.Companion.hours

/**
 * Clusters photos by location and time proximity using DBSCAN-like algorithm.
 */
class PhotoClusterer(
    private val maxDistanceMeters: Double = 200.0, // Max distance between photos in cluster
    private val maxTimeGapHours: Double = 2.0, // Max time gap between photos
    private val minClusterSize: Int = 3 // Minimum photos to form a cluster
) {

    private val logger = logger()

    /**
     * Cluster photos based on spatiotemporal proximity.
     *
     * @param photos Photos with metadata to cluster
     * @return List of photo clusters
     */
    fun cluster(photos: List<PhotoWithMetadata>): List<PhotoCluster> {
        // Filter photos with location data
        val photosWithLocation = photos.filter { photoWithMeta ->
            val hasExifLocation = photoWithMeta.metadata?.exifLatitude != null &&
                                photoWithMeta.metadata.exifLongitude != null
            val hasPhotoLocation = photoWithMeta.photo.latitude != null &&
                                  photoWithMeta.photo.longitude != null
            hasExifLocation || hasPhotoLocation
        }

        if (photosWithLocation.size < minClusterSize) {
            logger.debug { "Not enough photos with location for clustering: ${photosWithLocation.size}" }
            return emptyList()
        }

        logger.info { "Clustering ${photosWithLocation.size} photos with location data" }

        // Sort by timestamp for temporal clustering
        val sortedPhotos = photosWithLocation.sortedBy { it.photo.timestamp }

        // Build clusters using DBSCAN-like approach
        val clusters = mutableListOf<MutableList<PhotoWithMetadata>>()
        val visited = mutableSetOf<String>()

        sortedPhotos.forEach { photo ->
            if (photo.photo.id !in visited) {
                val cluster = expandCluster(photo, sortedPhotos, visited)
                if (cluster.size >= minClusterSize) {
                    clusters.add(cluster)
                }
            }
        }

        logger.info { "Created ${clusters.size} photo clusters" }

        // Convert to PhotoCluster objects
        return clusters.mapIndexed { index, clusterPhotos ->
            createPhotoCluster(clusterPhotos, index)
        }
    }

    /**
     * Expand cluster starting from a seed photo.
     */
    private fun expandCluster(
        seed: PhotoWithMetadata,
        allPhotos: List<PhotoWithMetadata>,
        visited: MutableSet<String>
    ): MutableList<PhotoWithMetadata> {
        val cluster = mutableListOf(seed)
        visited.add(seed.photo.id)

        val queue = mutableListOf(seed)

        while (queue.isNotEmpty()) {
            val current = queue.removeAt(0)
            val neighbors = findNeighbors(current, allPhotos, visited)

            neighbors.forEach { neighbor ->
                if (neighbor.photo.id !in visited) {
                    visited.add(neighbor.photo.id)
                    cluster.add(neighbor)
                    queue.add(neighbor)
                }
            }
        }

        return cluster
    }

    /**
     * Find neighboring photos within distance and time thresholds.
     */
    private fun findNeighbors(
        photo: PhotoWithMetadata,
        allPhotos: List<PhotoWithMetadata>,
        visited: MutableSet<String>
    ): List<PhotoWithMetadata> {
        val photoLat = photo.metadata?.exifLatitude ?: photo.photo.latitude ?: return emptyList()
        val photoLon = photo.metadata?.exifLongitude ?: photo.photo.longitude ?: return emptyList()
        val photoTime = photo.photo.timestamp

        return allPhotos.filter { candidate ->
            if (candidate.photo.id in visited) return@filter false
            if (candidate.photo.id == photo.photo.id) return@filter false

            val candidateLat = candidate.metadata?.exifLatitude ?: candidate.photo.latitude
                ?: return@filter false
            val candidateLon = candidate.metadata?.exifLongitude ?: candidate.photo.longitude
                ?: return@filter false
            val candidateTime = candidate.photo.timestamp

            // Check distance
            val distance = calculateDistance(photoLat, photoLon, candidateLat, candidateLon)
            if (distance > maxDistanceMeters) return@filter false

            // Check time gap
            val timeGapHours = abs((photoTime - candidateTime).inWholeSeconds) / 3600.0
            if (timeGapHours > maxTimeGapHours) return@filter false

            true
        }
    }

    /**
     * Create PhotoCluster from a list of photos.
     */
    private fun createPhotoCluster(
        photos: MutableList<PhotoWithMetadata>,
        index: Int
    ): PhotoCluster {
        // Calculate center point (average)
        val lats = photos.mapNotNull { it.metadata?.exifLatitude ?: it.photo.latitude }
        val lons = photos.mapNotNull { it.metadata?.exifLongitude ?: it.photo.longitude }
        val centerLat = lats.average()
        val centerLon = lons.average()

        // Find time range
        val timestamps = photos.map { it.photo.timestamp }.sorted()
        val startTime = timestamps.first()
        val endTime = timestamps.last()

        // Use first photo as thumbnail (could be improved with better selection)
        val thumbnailPhotoId = photos.first().photo.id

        return PhotoCluster(
            id = UUID.randomUUID().toString(),
            centerLatitude = centerLat,
            centerLongitude = centerLon,
            startTime = startTime,
            endTime = endTime,
            photoCount = photos.size,
            thumbnailPhotoId = thumbnailPhotoId,
            associatedVisitId = null, // Will be set by association logic
            clusteredAt = Clock.System.now()
        )
    }

    /**
     * Calculate distance between two coordinates using Haversine formula.
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

    /**
     * Associate photo clusters with place visits.
     *
     * @param cluster Photo cluster
     * @param visits Candidate place visits
     * @return Visit ID if a good match is found
     */
    fun associateClusterWithVisit(
        cluster: PhotoCluster,
        visits: List<com.po4yka.trailglass.domain.model.PlaceVisit>
    ): String? {
        if (visits.isEmpty()) return null

        // Find visit that overlaps in time and location
        val matchingVisit = visits.firstOrNull { visit ->
            // Check time overlap
            val timeOverlap = cluster.startTime <= visit.endTime &&
                            cluster.endTime >= visit.startTime

            if (!timeOverlap) return@firstOrNull false

            // Check distance
            val distance = calculateDistance(
                cluster.centerLatitude, cluster.centerLongitude,
                visit.location.latitude, visit.location.longitude
            )

            distance <= 500.0 // 500m threshold for cluster-visit association
        }

        return matchingVisit?.id
    }
}
