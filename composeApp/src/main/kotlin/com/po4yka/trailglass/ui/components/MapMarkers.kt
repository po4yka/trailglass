package com.po4yka.trailglass.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.TileOverlay
import com.google.android.gms.maps.model.TileOverlayOptions
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.heatmaps.HeatmapTileProvider
import com.po4yka.trailglass.domain.model.EnhancedMapMarker
import com.po4yka.trailglass.domain.model.HeatmapData
import com.po4yka.trailglass.domain.model.MapRoute
import com.po4yka.trailglass.domain.model.MarkerCluster
import com.po4yka.trailglass.feature.map.MarkerIconProvider

@Composable
internal fun RenderEnhancedMarker(
    marker: EnhancedMapMarker,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val icon = MarkerIconProvider.getIcon(marker.category, marker.isFavorite)

    Marker(
        state =
            MarkerState(
                position = LatLng(marker.coordinate.latitude, marker.coordinate.longitude)
            ),
        title = marker.title,
        snippet = marker.snippet,
        icon = getMarkerBitmapDescriptor(icon.color, isSelected),
        onClick = {
            onClick()
            true
        }
    )
}

@Composable
internal fun RenderCluster(
    cluster: MarkerCluster,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val clusterColor = MarkerIconProvider.getClusterColor(cluster.count)

    Marker(
        state =
            MarkerState(
                position = LatLng(cluster.coordinate.latitude, cluster.coordinate.longitude)
            ),
        title = "${cluster.count} places",
        snippet = "Tap to expand",
        icon = getClusterBitmapDescriptor(cluster.count, clusterColor, isSelected),
        onClick = {
            onClick()
            true
        }
    )
}

@Composable
internal fun RenderRoute(
    route: MapRoute,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val points =
        route.coordinates.map { coord ->
            LatLng(coord.latitude, coord.longitude)
        }

    val routeColor = MarkerIconProvider.getRouteColor(route.transportType.name)

    Polyline(
        points = points,
        color =
            if (isSelected) {
                Color(routeColor).copy(alpha = 1f)
            } else {
                Color(routeColor).copy(alpha = 0.7f)
            },
        width =
            if (isSelected) {
                getRouteWidth(route.transportType) * 1.5f
            } else {
                getRouteWidth(route.transportType)
            },
        clickable = true,
        onClick = {
            onClick()
        }
    )
}

@OptIn(MapsComposeExperimentalApi::class)
@Composable
internal fun RenderHeatmap(heatmapData: HeatmapData) {
    // Convert heatmap points to WeightedLatLng
    val heatmapPoints =
        remember(heatmapData) {
            heatmapData.points.map { point ->
                com.google.maps.android.heatmaps.WeightedLatLng(
                    LatLng(point.coordinate.latitude, point.coordinate.longitude),
                    point.intensity.toDouble()
                )
            }
        }

    // Render heatmap using MapEffect with proper lifecycle management
    MapEffect(heatmapPoints) { map ->
        // Create gradient
        val gradient = com.google.maps.android.heatmaps.Gradient(
            heatmapData.gradient.colors.toIntArray(),
            heatmapData.gradient.startPoints.toFloatArray()
        )

        // Create heatmap tile provider with custom gradient colors
        val heatmapProvider =
            HeatmapTileProvider
                .Builder()
                .weightedData(heatmapPoints)
                .radius(heatmapData.radius) // Use radius from data
                .opacity(heatmapData.opacity.toDouble()) // Use opacity from data
                .gradient(gradient) // Use custom gradient
                .build()

        // Add tile overlay to the map
        // Note: MapEffect handles cleanup automatically when the effect recomposes
        map.addTileOverlay(
            TileOverlayOptions().tileProvider(heatmapProvider)
        )
    }
}

/**
 * Get marker bitmap descriptor with custom color and styling.
 *
 * Uses custom-generated bitmaps with proper marker shape and selection highlighting.
 */
private fun getMarkerBitmapDescriptor(
    color: Int,
    isSelected: Boolean
): BitmapDescriptor =
    MapMarkerBitmapGenerator.getCachedMarkerBitmap(
        color = color,
        isSelected = isSelected
    )

/**
 * Get cluster bitmap descriptor with count badge.
 *
 * Generates custom circular cluster markers with count text overlay.
 */
private fun getClusterBitmapDescriptor(
    count: Int,
    color: Int,
    isSelected: Boolean
): BitmapDescriptor =
    MapMarkerBitmapGenerator.getCachedClusterBitmap(
        count = count,
        color = color,
        isSelected = isSelected
    )
