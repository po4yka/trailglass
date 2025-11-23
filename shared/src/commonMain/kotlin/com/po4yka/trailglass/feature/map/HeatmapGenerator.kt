package com.po4yka.trailglass.feature.map

import com.po4yka.trailglass.domain.model.Coordinate
import com.po4yka.trailglass.domain.model.EnhancedMapMarker
import com.po4yka.trailglass.domain.model.HeatmapData
import com.po4yka.trailglass.domain.model.HeatmapGradient
import com.po4yka.trailglass.domain.model.HeatmapPoint
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/** Generates heatmap data from markers based on visit frequency. */
class HeatmapGenerator {
    /**
     * Generate heatmap data from markers.
     *
     * @param markers List of markers to visualize
     * @param radius Heatmap point radius in pixels
     * @param opacity Heatmap opacity (0.0 to 1.0)
     * @param gradient Color gradient to use
     * @param intensityMode How to calculate intensity values
     * @return HeatmapData ready for visualization
     */
    fun generate(
        markers: List<EnhancedMapMarker>,
        radius: Int = 20,
        opacity: Float = 0.6f,
        gradient: HeatmapGradient = HeatmapGradient.DEFAULT,
        intensityMode: IntensityMode = IntensityMode.VISIT_COUNT
    ): HeatmapData {
        if (markers.isEmpty()) {
            return HeatmapData(emptyList(), radius, opacity, gradient)
        }

        // Calculate intensity for each marker
        val maxIntensity =
            when (intensityMode) {
                IntensityMode.UNIFORM -> 1.0f
                IntensityMode.VISIT_COUNT -> markers.maxOf { it.visitCount }.toFloat()
                IntensityMode.DENSITY -> calculateMaxDensity(markers)
            }

        val points =
            markers.map { marker ->
                val intensity =
                    when (intensityMode) {
                        IntensityMode.UNIFORM -> 1.0f
                        IntensityMode.VISIT_COUNT -> marker.visitCount.toFloat()
                        IntensityMode.DENSITY -> calculateDensity(marker, markers)
                    }

                HeatmapPoint(
                    coordinate = marker.coordinate,
                    intensity = (intensity / maxIntensity).coerceIn(0f, 1f)
                )
            }

        return HeatmapData(
            points = points,
            radius = radius,
            opacity = opacity,
            gradient = gradient
        )
    }

    /** Generate heatmap data from coordinates with uniform intensity. */
    fun generateFromCoordinates(
        coordinates: List<Coordinate>,
        radius: Int = 20,
        opacity: Float = 0.6f,
        gradient: HeatmapGradient = HeatmapGradient.DEFAULT
    ): HeatmapData {
        val points =
            coordinates.map { coordinate ->
                HeatmapPoint(
                    coordinate = coordinate,
                    intensity = 1.0f
                )
            }

        return HeatmapData(
            points = points,
            radius = radius,
            opacity = opacity,
            gradient = gradient
        )
    }

    /** Generate weighted heatmap from coordinates with custom weights. */
    fun generateWeighted(
        coordinatesWithWeights: List<Pair<Coordinate, Float>>,
        radius: Int = 20,
        opacity: Float = 0.6f,
        gradient: HeatmapGradient = HeatmapGradient.DEFAULT
    ): HeatmapData {
        if (coordinatesWithWeights.isEmpty()) {
            return HeatmapData(emptyList(), radius, opacity, gradient)
        }

        val maxWeight = coordinatesWithWeights.maxOf { it.second }

        val points =
            coordinatesWithWeights.map { (coordinate, weight) ->
                HeatmapPoint(
                    coordinate = coordinate,
                    intensity = (weight / maxWeight).coerceIn(0f, 1f)
                )
            }

        return HeatmapData(
            points = points,
            radius = radius,
            opacity = opacity,
            gradient = gradient
        )
    }

    /** Calculate density of markers around a specific marker. */
    private fun calculateDensity(
        marker: EnhancedMapMarker,
        allMarkers: List<EnhancedMapMarker>,
        radius: Double = 0.01 // ~1km radius
    ): Float =
        allMarkers
            .count { other ->
                if (other.id == marker.id) {
                    false
                } else {
                    haversineDistance(marker.coordinate, other.coordinate) <= radius
                }
            }.toFloat()

    /** Calculate maximum density across all markers. */
    private fun calculateMaxDensity(markers: List<EnhancedMapMarker>): Float {
        if (markers.isEmpty()) return 1f

        return markers.maxOf { marker ->
            calculateDensity(marker, markers)
        }
    }

    /** Calculate haversine distance between two coordinates in degrees. */
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

        // Return in degrees
        return c * 180.0 / PI
    }

    /** Mode for calculating heatmap intensity values. */
    enum class IntensityMode {
        /** All points have equal intensity. */
        UNIFORM,

        /** Intensity based on visit count. */
        VISIT_COUNT,

        /** Intensity based on marker density (number of nearby markers). */
        DENSITY
    }
}
