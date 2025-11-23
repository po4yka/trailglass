package com.po4yka.trailglass.feature.map

import com.po4yka.trailglass.domain.model.MapRegion

/**
 * Helper functions for map operations.
 */
internal object MapOperations {
    /**
     * Calculate appropriate zoom level for a region.
     *
     * Higher latitudeDelta = lower zoom.
     *
     * @param region The map region to calculate zoom for
     * @return Zoom level from 1 (world) to 20 (building)
     */
    fun calculateZoomLevel(region: MapRegion): Float {
        val delta = maxOf(region.latitudeDelta, region.longitudeDelta)

        return when {
            delta > 10.0 -> 5f // Country level
            delta > 5.0 -> 7f // Large region
            delta > 1.0 -> 9f // City level
            delta > 0.5 -> 11f // District
            delta > 0.1 -> 13f // Neighborhood
            delta > 0.05 -> 15f // Street level
            else -> 17f // Building level
        }
    }
}
