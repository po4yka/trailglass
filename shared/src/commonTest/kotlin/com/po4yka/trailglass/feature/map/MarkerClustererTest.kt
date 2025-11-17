package com.po4yka.trailglass.feature.map

import com.po4yka.trailglass.domain.model.Coordinate
import com.po4yka.trailglass.domain.model.EnhancedMapMarker
import com.po4yka.trailglass.domain.model.PlaceCategory
import io.kotest.matchers.shouldBe
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import kotlin.test.Test

/**
 * Unit tests for MarkerClusterer.
 */
class MarkerClustererTest {

    @Test
    fun `cluster with empty markers returns empty results`() {
        // Arrange
        val clusterer = MarkerClusterer()
        val markers = emptyList<EnhancedMapMarker>()

        // Act
        val (clusters, singleMarkers) = clusterer.cluster(markers, zoomLevel = 10f)

        // Assert
        clusters.shouldBeEmpty()
        singleMarkers.shouldBeEmpty()
    }

    @Test
    fun `cluster with single marker returns single marker`() {
        // Arrange
        val clusterer = MarkerClusterer()
        val markers = listOf(createMarker("1", 0.0, 0.0))

        // Act
        val (clusters, singleMarkers) = clusterer.cluster(markers, zoomLevel = 10f)

        // Assert
        clusters.shouldBeEmpty()
        singleMarkers shouldHaveSize 1
        singleMarkers[0].id shouldBe "1"
    }

    @Test
    fun `cluster with distant markers returns all as single markers`() {
        // Arrange
        val clusterer = MarkerClusterer()
        val markers = listOf(
            createMarker("1", 0.0, 0.0),
            createMarker("2", 10.0, 10.0),
            createMarker("3", 20.0, 20.0)
        )

        // Act - high zoom level = less clustering
        val (clusters, singleMarkers) = clusterer.cluster(markers, zoomLevel = 18f)

        // Assert
        clusters.shouldBeEmpty()
        singleMarkers shouldHaveSize 3
    }

    @Test
    fun `cluster with nearby markers creates clusters at low zoom`() {
        // Arrange
        val clusterer = MarkerClusterer(minClusterSize = 2)
        val markers = listOf(
            createMarker("1", 0.0, 0.0),
            createMarker("2", 0.001, 0.001),  // Very close
            createMarker("3", 0.002, 0.002),  // Very close
            createMarker("4", 10.0, 10.0)      // Far away
        )

        // Act - low zoom level = more clustering
        val (clusters, singleMarkers) = clusterer.cluster(markers, zoomLevel = 5f)

        // Assert
        // At low zoom, nearby markers should cluster
        clusters.shouldHaveSize(1)
        clusters[0].count shouldBeGreaterThanOrEqual 2

        // Far marker should remain single
        singleMarkers shouldHaveSize 1
    }

    @Test
    fun `cluster respects minClusterSize`() {
        // Arrange
        val clusterer = MarkerClusterer(minClusterSize = 3)
        val markers = listOf(
            createMarker("1", 0.0, 0.0),
            createMarker("2", 0.0001, 0.0001)  // Very close, only 2 markers
        )

        // Act
        val (clusters, singleMarkers) = clusterer.cluster(markers, zoomLevel = 5f)

        // Assert
        // Only 2 markers, below minClusterSize, should not cluster
        clusters.shouldBeEmpty()
        singleMarkers shouldHaveSize 2
    }

    @Test
    fun `cluster at high zoom breaks apart clusters`() {
        // Arrange
        val clusterer = MarkerClusterer()
        val markers = listOf(
            createMarker("1", 0.0, 0.0),
            createMarker("2", 0.001, 0.001),
            createMarker("3", 0.002, 0.002)
        )

        // Act - high zoom level = markers appear separated
        val (clusters, singleMarkers) = clusterer.cluster(markers, zoomLevel = 18f)

        // Assert
        // At high zoom, markers should not cluster
        singleMarkers shouldHaveSize 3
        clusters.shouldBeEmpty()
    }

    @Test
    fun `cluster calculates center coordinate correctly`() {
        // Arrange
        val clusterer = MarkerClusterer(minClusterSize = 2)
        val markers = listOf(
            createMarker("1", 0.0, 0.0),
            createMarker("2", 2.0, 2.0)
        )

        // Act - low zoom to force clustering
        val (clusters, _) = clusterer.cluster(markers, zoomLevel = 1f)

        // Assert
        if (clusters.isNotEmpty()) {
            val cluster = clusters[0]
            // Center should be approximately at (1.0, 1.0)
            cluster.coordinate.latitude shouldBe 1.0
            cluster.coordinate.longitude shouldBe 1.0
        }
    }

    @Test
    fun `cluster includes all markers in cluster`() {
        // Arrange
        val clusterer = MarkerClusterer(minClusterSize = 2)
        val markers = listOf(
            createMarker("1", 0.0, 0.0),
            createMarker("2", 0.001, 0.001),
            createMarker("3", 0.002, 0.002)
        )

        // Act - low zoom to force clustering
        val (clusters, singleMarkers) = clusterer.cluster(markers, zoomLevel = 1f)

        // Assert
        val totalMarkersInClusters = clusters.sumOf { it.markers.size }
        val totalMarkers = totalMarkersInClusters + singleMarkers.size

        totalMarkers shouldBe markers.size
    }

    @Test
    fun `DistanceBasedClusterer with empty markers returns empty results`() {
        // Arrange
        val clusterer = DistanceBasedClusterer()
        val markers = emptyList<EnhancedMapMarker>()

        // Act
        val (clusters, singleMarkers) = clusterer.cluster(markers)

        // Assert
        clusters.shouldBeEmpty()
        singleMarkers.shouldBeEmpty()
    }

    @Test
    fun `DistanceBasedClusterer clusters nearby markers`() {
        // Arrange
        val clusterer = DistanceBasedClusterer(maxDistance = 0.1, minClusterSize = 2)
        val markers = listOf(
            createMarker("1", 0.0, 0.0),
            createMarker("2", 0.01, 0.01),  // Close, within maxDistance
            createMarker("3", 10.0, 10.0)    // Far away
        )

        // Act
        val (clusters, singleMarkers) = clusterer.cluster(markers)

        // Assert
        clusters shouldHaveSize 1
        clusters[0].count shouldBe 2
        singleMarkers shouldHaveSize 1
        singleMarkers[0].id shouldBe "3"
    }

    @Test
    fun `DistanceBasedClusterer respects maxDistance`() {
        // Arrange
        val clusterer = DistanceBasedClusterer(maxDistance = 0.001, minClusterSize = 2)
        val markers = listOf(
            createMarker("1", 0.0, 0.0),
            createMarker("2", 0.1, 0.1)  // Outside maxDistance
        )

        // Act
        val (clusters, singleMarkers) = clusterer.cluster(markers)

        // Assert
        // Markers are too far apart, should not cluster
        clusters.shouldBeEmpty()
        singleMarkers shouldHaveSize 2
    }

    @Test
    fun `DistanceBasedClusterer respects minClusterSize`() {
        // Arrange
        val clusterer = DistanceBasedClusterer(maxDistance = 0.1, minClusterSize = 3)
        val markers = listOf(
            createMarker("1", 0.0, 0.0),
            createMarker("2", 0.01, 0.01)  // Only 2 markers
        )

        // Act
        val (clusters, singleMarkers) = clusterer.cluster(markers)

        // Assert
        // Only 2 markers, below minClusterSize
        clusters.shouldBeEmpty()
        singleMarkers shouldHaveSize 2
    }

    @Test
    fun `DistanceBasedClusterer includes all markers exactly once`() {
        // Arrange
        val clusterer = DistanceBasedClusterer(maxDistance = 0.05, minClusterSize = 2)
        val markers = listOf(
            createMarker("1", 0.0, 0.0),
            createMarker("2", 0.01, 0.01),
            createMarker("3", 0.02, 0.02),
            createMarker("4", 10.0, 10.0),
            createMarker("5", 10.01, 10.01)
        )

        // Act
        val (clusters, singleMarkers) = clusterer.cluster(markers)

        // Assert
        val totalMarkersInClusters = clusters.sumOf { it.markers.size }
        val totalMarkers = totalMarkersInClusters + singleMarkers.size

        totalMarkers shouldBe markers.size

        // Verify no duplicates
        val allMarkerIds = mutableSetOf<String>()
        clusters.forEach { cluster ->
            cluster.markers.forEach { marker ->
                allMarkerIds.add(marker.id)
            }
        }
        singleMarkers.forEach { marker ->
            allMarkerIds.add(marker.id)
        }

        allMarkerIds.size shouldBe markers.size
    }

    private fun createMarker(
        id: String,
        lat: Double,
        lng: Double
    ): EnhancedMapMarker {
        return EnhancedMapMarker(
            id = id,
            coordinate = Coordinate(lat, lng),
            title = "Test Marker $id",
            snippet = null,
            placeVisitId = "visit_$id",
            category = PlaceCategory.OTHER,
            isFavorite = false,
            visitCount = 1
        )
    }
}
