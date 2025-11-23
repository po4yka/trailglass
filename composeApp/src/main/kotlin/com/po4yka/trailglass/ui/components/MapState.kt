package com.po4yka.trailglass.ui.components

import com.google.android.gms.maps.model.LatLng
import com.po4yka.trailglass.domain.model.TransportType

/**
 * Calculate appropriate zoom level based on region deltas. Uses the larger delta (latitude or longitude) to determine
 * zoom.
 */
internal fun calculateZoomLevel(
    latitudeDelta: Double,
    longitudeDelta: Double
): Float {
    val maxDelta = maxOf(latitudeDelta, longitudeDelta)
    return when {
        maxDelta >= 40.0 -> 3f
        maxDelta >= 20.0 -> 4f
        maxDelta >= 10.0 -> 5f
        maxDelta >= 5.0 -> 6f
        maxDelta >= 2.0 -> 7f
        maxDelta >= 1.0 -> 8f
        maxDelta >= 0.5 -> 9f
        maxDelta >= 0.25 -> 10f
        maxDelta >= 0.1 -> 11f
        maxDelta >= 0.05 -> 12f
        maxDelta >= 0.025 -> 13f
        maxDelta >= 0.01 -> 14f
        else -> 15f
    }
}

/** Get route width based on transport type. */
internal fun getRouteWidth(transportType: TransportType): Float =
    when (transportType) {
        TransportType.WALK -> 8f
        TransportType.BIKE -> 10f
        TransportType.CAR -> 12f
        TransportType.TRAIN -> 14f
        TransportType.PLANE -> 16f
        TransportType.BOAT -> 12f
        TransportType.UNKNOWN -> 8f
    }

/**
 * Convert coordinates to LatLng for Google Maps.
 */
internal fun com.po4yka.trailglass.domain.model.Coordinate.toLatLng(): LatLng = LatLng(this.latitude, this.longitude)
