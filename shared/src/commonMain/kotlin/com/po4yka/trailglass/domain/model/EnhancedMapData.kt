package com.po4yka.trailglass.domain.model

/**
 * Enhanced map marker with custom icon support.
 */
data class EnhancedMapMarker(
    val id: String,
    val coordinate: Coordinate,
    val title: String?,
    val snippet: String?,
    val placeVisitId: String,
    val category: PlaceCategory,
    val isFavorite: Boolean = false,
    val visitCount: Int = 1
)

/**
 * Marker cluster representing multiple markers in close proximity.
 */
data class MarkerCluster(
    val id: String,
    val coordinate: Coordinate, // Center of cluster
    val markers: List<EnhancedMapMarker>,
    val count: Int = markers.size
)

/**
 * Heatmap point with intensity value.
 */
data class HeatmapPoint(
    val coordinate: Coordinate,
    val intensity: Float // 0.0 to 1.0
)

/**
 * Heatmap data for visualization.
 */
data class HeatmapData(
    val points: List<HeatmapPoint>,
    val radius: Int = 20, // Radius in pixels
    val opacity: Float = 0.6f,
    val gradient: HeatmapGradient = HeatmapGradient.DEFAULT
)

/**
 * Heatmap color gradient configuration.
 */
data class HeatmapGradient(
    val colors: List<Int>,
    val startPoints: List<Float>
) {
    companion object {
        val DEFAULT =
            HeatmapGradient(
                colors =
                    listOf(
                        0x00000000.toInt(), // Transparent
                        0x550000FF.toInt(), // Blue
                        0xAA00FF00.toInt(), // Green
                        0xFFFFFF00.toInt(), // Yellow
                        0xFFFF0000.toInt() // Red
                    ),
                startPoints = listOf(0f, 0.2f, 0.4f, 0.6f, 1.0f)
            )

        val COOL =
            HeatmapGradient(
                colors =
                    listOf(
                        0x00000000.toInt(),
                        0x5500FFFF.toInt(),
                        0xAA0099FF.toInt(),
                        0xFF0066FF.toInt(),
                        0xFF0033FF.toInt()
                    ),
                startPoints = listOf(0f, 0.2f, 0.4f, 0.6f, 1.0f)
            )

        val WARM =
            HeatmapGradient(
                colors =
                    listOf(
                        0x00000000.toInt(),
                        0x55FFAA00.toInt(),
                        0xAAFF6600.toInt(),
                        0xFFFF3300.toInt(),
                        0xFFFF0000.toInt()
                    ),
                startPoints = listOf(0f, 0.2f, 0.4f, 0.6f, 1.0f)
            )
    }
}

/**
 * Enhanced map display data with clustering and heatmap support.
 */
data class EnhancedMapDisplayData(
    val markers: List<EnhancedMapMarker> = emptyList(),
    val clusters: List<MarkerCluster> = emptyList(),
    val routes: List<MapRoute> = emptyList(),
    val heatmapData: HeatmapData? = null,
    val region: MapRegion? = null,
    val clusteringEnabled: Boolean = true,
    val heatmapEnabled: Boolean = false
)

/**
 * Map visualization mode.
 */
enum class MapVisualizationMode {
    MARKERS, // Show individual markers
    CLUSTERS, // Show clustered markers
    HEATMAP, // Show heatmap visualization
    HYBRID // Show both markers/clusters and routes
}
