package com.po4yka.trailglass.feature.places

import com.po4yka.trailglass.domain.model.Coordinate
import com.po4yka.trailglass.domain.model.FrequentPlace
import com.po4yka.trailglass.domain.model.PlaceCategory
import com.po4yka.trailglass.domain.model.PlaceVisit
import com.po4yka.trailglass.util.distanceMetersTo
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import me.tatarka.inject.annotations.Inject
import kotlin.time.Duration

/**
 * Clusters place visits to identify frequently visited places using DBSCAN-like algorithm.
 *
 * This helps identify places like "Home", "Work", "Favorite Cafe" by grouping visits that are spatially close to each
 * other.
 */
@Inject
class PlaceClusterer(
    private val categorizer: PlaceCategorizer = PlaceCategorizer(),
    private val clusterRadiusMeters: Double = 50.0,
    private val minVisitsForPlace: Int = 2
) {
    /**
     * Cluster place visits into frequent places.
     *
     * @param visits List of all place visits to cluster
     * @param userId User ID for the frequent places
     * @return List of frequent places with aggregated statistics
     */
    fun clusterVisits(
        visits: List<PlaceVisit>,
        userId: String
    ): List<FrequentPlace> {
        if (visits.isEmpty()) return emptyList()

        // Sort visits by time for easier processing
        val sortedVisits = visits.sortedBy { it.startTime }

        // Group visits into spatial clusters
        val clusters = performClustering(sortedVisits)

        // Convert clusters to FrequentPlace objects
        return clusters
            .filter { it.size >= minVisitsForPlace }
            .mapIndexed { index, cluster ->
                createFrequentPlace(cluster, userId, index)
            }.sortedByDescending { it.visitCount }
    }

    /**
     * Assign new visits to existing frequent places or create new ones.
     *
     * @param visits New visits to assign
     * @param existingPlaces Existing frequent places
     * @param userId User ID
     * @return Updated list of frequent places
     */
    fun updateFrequentPlaces(
        visits: List<PlaceVisit>,
        existingPlaces: List<FrequentPlace>,
        userId: String
    ): List<FrequentPlace> {
        val updatedPlaces = existingPlaces.toMutableList()
        val unassignedVisits = mutableListOf<PlaceVisit>()

        // Try to assign each visit to an existing place
        visits.forEach { visit ->
            val nearestPlace = findNearestPlace(visit, existingPlaces)

            if (nearestPlace != null) {
                // Update the existing place
                val updated = updateFrequentPlaceWithVisit(nearestPlace, visit)
                val index = updatedPlaces.indexOfFirst { it.id == nearestPlace.id }
                if (index >= 0) {
                    updatedPlaces[index] = updated
                }
            } else {
                unassignedVisits.add(visit)
            }
        }

        // Cluster unassigned visits to create new frequent places
        if (unassignedVisits.isNotEmpty()) {
            val newPlaces = clusterVisits(unassignedVisits, userId)
            updatedPlaces.addAll(newPlaces)
        }

        return updatedPlaces.sortedByDescending { it.visitCount }
    }

    /** Perform spatial clustering using a simple distance-based algorithm. */
    private fun performClustering(visits: List<PlaceVisit>): List<List<PlaceVisit>> {
        val clusters = mutableListOf<MutableList<PlaceVisit>>()
        val visited = mutableSetOf<Int>()

        visits.forEachIndexed { index, visit ->
            if (index in visited) return@forEachIndexed

            val cluster = mutableListOf(visit)
            visited.add(index)

            // Find all visits within cluster radius
            val visitCoord = Coordinate(visit.centerLatitude, visit.centerLongitude)
            visits.forEachIndexed { otherIndex, otherVisit ->
                if (otherIndex !in visited) {
                    val otherCoord = Coordinate(otherVisit.centerLatitude, otherVisit.centerLongitude)
                    val distance = visitCoord.distanceMetersTo(otherCoord)

                    if (distance <= clusterRadiusMeters) {
                        cluster.add(otherVisit)
                        visited.add(otherIndex)
                    }
                }
            }

            if (cluster.isNotEmpty()) {
                clusters.add(cluster)
            }
        }

        return clusters
    }

    /** Create a FrequentPlace from a cluster of visits. */
    private fun createFrequentPlace(
        cluster: List<PlaceVisit>,
        userId: String,
        index: Int
    ): FrequentPlace {
        // Aggregate cluster statistics in single pass
        val stats = aggregateClusterStats(cluster)

        // Determine category based on all visits in cluster
        val (category, confidence) = determineCategoryForCluster(cluster)

        // Determine significance
        val significance =
            categorizer.determineSignificance(
                visitCount = stats.visitCount,
                totalDuration = stats.totalDuration,
                lastVisitTime = stats.lastVisitTime
            )

        // Use name/address from most recent visit with that information
        val recentWithInfo = cluster.reversed().firstOrNull { it.poiName != null || it.approximateAddress != null }
        val name = recentWithInfo?.poiName
        val address = recentWithInfo?.approximateAddress
        val city = recentWithInfo?.city
        val countryCode = recentWithInfo?.countryCode

        val now = Clock.System.now()

        return FrequentPlace(
            id = "place_${userId}_${index}_${stats.centerLat.hashCode()}_${stats.centerLon.hashCode()}",
            centerLatitude = stats.centerLat,
            centerLongitude = stats.centerLon,
            radiusMeters = clusterRadiusMeters,
            name = name,
            address = address,
            city = city,
            countryCode = countryCode,
            category = category,
            categoryConfidence = confidence,
            significance = significance,
            visitCount = stats.visitCount,
            totalDuration = stats.totalDuration,
            firstVisitTime = stats.firstVisitTime,
            lastVisitTime = stats.lastVisitTime,
            userId = userId,
            createdAt = now,
            updatedAt = now
        )
    }

    /** Determine the best category for a cluster of visits. Uses voting from individual visit categorizations. */
    private fun determineCategoryForCluster(
        cluster: List<PlaceVisit>
    ): Pair<PlaceCategory, com.po4yka.trailglass.domain.model.CategoryConfidence> {
        val categorizations =
            cluster.map { visit ->
                categorizer.categorize(visit, cluster.filter { it != visit })
            }

        // Count votes for each category
        val categoryVotes =
            categorizations
                .groupBy { it.first }
                .mapValues { (_, pairs) -> pairs.size }

        // Get the category with most votes
        val winningCategory = categoryVotes.maxByOrNull { it.value }?.key ?: PlaceCategory.OTHER

        // Determine confidence based on vote consensus
        val totalVotes = categorizations.size
        val winningVotes = categoryVotes[winningCategory] ?: 0
        val consensus = winningVotes.toDouble() / totalVotes

        val confidence =
            when {
                consensus >= 0.8 -> com.po4yka.trailglass.domain.model.CategoryConfidence.HIGH
                consensus >= 0.5 -> com.po4yka.trailglass.domain.model.CategoryConfidence.MEDIUM
                else -> com.po4yka.trailglass.domain.model.CategoryConfidence.LOW
            }

        return winningCategory to confidence
    }

    /** Find the nearest frequent place to a visit. */
    private fun findNearestPlace(
        visit: PlaceVisit,
        places: List<FrequentPlace>
    ): FrequentPlace? {
        val visitCoord = Coordinate(visit.centerLatitude, visit.centerLongitude)
        return places
            .map { place ->
                val placeCoord = Coordinate(place.centerLatitude, place.centerLongitude)
                place to visitCoord.distanceMetersTo(placeCoord)
            }.filter { (_, distance) -> distance <= clusterRadiusMeters }
            .minByOrNull { (_, distance) -> distance }
            ?.first
    }

    /** Update a frequent place with a new visit. */
    private fun updateFrequentPlaceWithVisit(
        place: FrequentPlace,
        visit: PlaceVisit
    ): FrequentPlace {
        val newVisitCount = place.visitCount + 1
        val newTotalDuration = place.totalDuration + visit.duration
        val newLastVisitTime = maxOf(place.lastVisitTime ?: visit.endTime, visit.endTime)
        val newFirstVisitTime = minOf(place.firstVisitTime ?: visit.startTime, visit.startTime)

        // Recalculate significance
        val newSignificance =
            categorizer.determineSignificance(
                visitCount = newVisitCount,
                totalDuration = newTotalDuration,
                lastVisitTime = newLastVisitTime
            )

        return place.copy(
            visitCount = newVisitCount,
            totalDuration = newTotalDuration,
            lastVisitTime = newLastVisitTime,
            firstVisitTime = newFirstVisitTime,
            significance = newSignificance,
            updatedAt = Clock.System.now()
        )
    }

    /** Aggregated statistics for a cluster of visits. */
    private data class ClusterStats(
        val centerLat: Double,
        val centerLon: Double,
        val visitCount: Int,
        val totalDuration: Duration,
        val firstVisitTime: Instant,
        val lastVisitTime: Instant
    )

    /**
     * Aggregate cluster statistics in a single pass through the visits.
     * More efficient than multiple iterations for large clusters.
     */
    private fun aggregateClusterStats(cluster: List<PlaceVisit>): ClusterStats {
        var sumLat = 0.0
        var sumLon = 0.0
        var totalDuration = Duration.ZERO
        var firstVisitTime: Instant? = null
        var lastVisitTime: Instant? = null

        cluster.forEach { visit ->
            sumLat += visit.centerLatitude
            sumLon += visit.centerLongitude
            totalDuration += visit.duration

            val startTime = visit.startTime
            val endTime = visit.endTime

            if (firstVisitTime == null || startTime < firstVisitTime!!) {
                firstVisitTime = startTime
            }
            if (lastVisitTime == null || endTime > lastVisitTime!!) {
                lastVisitTime = endTime
            }
        }

        val count = cluster.size
        return ClusterStats(
            centerLat = sumLat / count,
            centerLon = sumLon / count,
            visitCount = count,
            totalDuration = totalDuration,
            firstVisitTime = firstVisitTime ?: Clock.System.now(),
            lastVisitTime = lastVisitTime ?: Clock.System.now()
        )
    }
}
