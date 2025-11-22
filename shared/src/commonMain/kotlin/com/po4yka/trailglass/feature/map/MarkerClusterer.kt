package com.po4yka.trailglass.feature.map

import com.po4yka.trailglass.domain.model.Coordinate
import com.po4yka.trailglass.domain.model.EnhancedMapMarker
import com.po4yka.trailglass.domain.model.MarkerCluster
import com.po4yka.trailglass.util.UuidGenerator
import kotlin.math.*

/**
 * Clusters markers using grid-based clustering algorithm.
 * More efficient than distance-based clustering for large datasets.
 */
class MarkerClusterer(
    private val gridSize: Int = 60, // Grid size in pixels at zoom level 20
    private val minClusterSize: Int = 2
) {
    /**
     * Cluster markers based on zoom level and screen size.
     *
     * @param markers List of markers to cluster
     * @param zoomLevel Current map zoom level (1-20)
     * @param viewportWidth Viewport width in pixels
     * @param viewportHeight Viewport height in pixels
     * @return Pair of clusters and unclustered markers
     */
    fun cluster(
        markers: List<EnhancedMapMarker>,
        zoomLevel: Float,
        viewportWidth: Int = 1000,
        viewportHeight: Int = 1000
    ): Pair<List<MarkerCluster>, List<EnhancedMapMarker>> {
        if (markers.isEmpty()) {
            return Pair(emptyList(), emptyList())
        }

        // Calculate grid cell size based on zoom level
        val cellSize = calculateCellSize(zoomLevel)

        // Group markers into grid cells
        val cells = mutableMapOf<String, MutableList<EnhancedMapMarker>>()

        markers.forEach { marker ->
            val cellKey = getCellKey(marker.coordinate, cellSize)
            cells.getOrPut(cellKey) { mutableListOf() }.add(marker)
        }

        // Create clusters from cells with multiple markers
        val clusters = mutableListOf<MarkerCluster>()
        val singleMarkers = mutableListOf<EnhancedMapMarker>()

        cells.forEach { (_, cellMarkers) ->
            if (cellMarkers.size >= minClusterSize) {
                // Create cluster
                val center = calculateCenter(cellMarkers.map { it.coordinate })
                clusters.add(
                    MarkerCluster(
                        id = UuidGenerator.randomUUID(),
                        coordinate = center,
                        markers = cellMarkers,
                        count = cellMarkers.size
                    )
                )
            } else {
                // Keep as individual markers
                singleMarkers.addAll(cellMarkers)
            }
        }

        return Pair(clusters, singleMarkers)
    }

    /**
     * Calculate grid cell size based on zoom level.
     * Higher zoom = smaller cells = less clustering.
     */
    private fun calculateCellSize(zoomLevel: Float): Double {
        // At zoom 20, use gridSize directly
        // At lower zooms, increase cell size exponentially
        val scale = 2.0.pow(20 - zoomLevel.toDouble())
        return gridSize * scale / 256.0 // Convert to lat/lng degrees
    }

    /**
     * Get grid cell key for a coordinate.
     */
    private fun getCellKey(
        coordinate: Coordinate,
        cellSize: Double
    ): String {
        val cellX = floor(coordinate.longitude / cellSize).toInt()
        val cellY = floor(coordinate.latitude / cellSize).toInt()
        return "$cellX,$cellY"
    }

    /**
     * Calculate geometric center of coordinates.
     */
    private fun calculateCenter(coordinates: List<Coordinate>): Coordinate {
        if (coordinates.isEmpty()) {
            return Coordinate(0.0, 0.0)
        }

        val avgLat = coordinates.map { it.latitude }.average()
        val avgLng = coordinates.map { it.longitude }.average()

        return Coordinate(avgLat, avgLng)
    }
}

/**
 * Distance-based marker clusterer using DBSCAN-like algorithm.
 * More accurate but slower for large datasets.
 */
class DistanceBasedClusterer(
    private val maxDistance: Double = 0.001, // ~100 meters
    private val minClusterSize: Int = 2
) {
    /**
     * Cluster markers based on distance.
     */
    fun cluster(markers: List<EnhancedMapMarker>): Pair<List<MarkerCluster>, List<EnhancedMapMarker>> {
        if (markers.isEmpty()) {
            return Pair(emptyList(), emptyList())
        }

        val visited = mutableSetOf<String>()
        val clusters = mutableListOf<MarkerCluster>()
        val singleMarkers = mutableListOf<EnhancedMapMarker>()

        markers.forEach { marker ->
            if (marker.id !in visited) {
                val neighbors = findNeighbors(marker, markers, maxDistance)

                if (neighbors.size >= minClusterSize) {
                    // Create cluster
                    neighbors.forEach { visited.add(it.id) }

                    val center = calculateCenter(neighbors.map { it.coordinate })
                    clusters.add(
                        MarkerCluster(
                            id = UuidGenerator.randomUUID(),
                            coordinate = center,
                            markers = neighbors,
                            count = neighbors.size
                        )
                    )
                } else {
                    // Single marker
                    visited.add(marker.id)
                    singleMarkers.add(marker)
                }
            }
        }

        return Pair(clusters, singleMarkers)
    }

    /**
     * Find all markers within maxDistance of the given marker.
     */
    private fun findNeighbors(
        marker: EnhancedMapMarker,
        allMarkers: List<EnhancedMapMarker>,
        maxDistance: Double
    ): List<EnhancedMapMarker> =
        allMarkers.filter { other ->
            if (other.id == marker.id) {
                true // Include self
            } else {
                val distance = haversineDistance(marker.coordinate, other.coordinate)
                distance <= maxDistance
            }
        }

    /**
     * Calculate haversine distance between two coordinates in degrees.
     */
    private fun haversineDistance(
        c1: Coordinate,
        c2: Coordinate
    ): Double {
        val dLat = (c2.latitude - c1.latitude) * PI / 180.0
        val dLng = (c2.longitude - c1.longitude) * PI / 180.0

        val a =
            sin(dLat / 2).pow(2) +
                cos(c1.latitude * PI / 180.0) *
                cos(c2.latitude * PI / 180.0) *
                sin(dLng / 2).pow(2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        // Return in degrees for simplicity
        return c * 180.0 / PI
    }

    private fun calculateCenter(coordinates: List<Coordinate>): Coordinate {
        if (coordinates.isEmpty()) {
            return Coordinate(0.0, 0.0)
        }

        val avgLat = coordinates.map { it.latitude }.average()
        val avgLng = coordinates.map { it.longitude }.average()

        return Coordinate(avgLat, avgLng)
    }
}
