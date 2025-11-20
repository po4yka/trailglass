package com.po4yka.trailglass.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*
import com.po4yka.trailglass.domain.model.PhotoMarker
import com.po4yka.trailglass.domain.model.RoutePoint
import com.po4yka.trailglass.domain.model.TripRoute
import com.po4yka.trailglass.domain.model.TransportType
import com.po4yka.trailglass.feature.route.MapStyle

/**
 * Map view component for displaying trip routes.
 * Shows route polyline, start/end markers, and photo markers.
 */
@Composable
fun RouteMapView(
    tripRoute: TripRoute,
    mapStyle: MapStyle,
    shouldRecenterCamera: Boolean,
    selectedPhotoMarker: PhotoMarker?,
    onPhotoMarkerClick: (PhotoMarker) -> Unit,
    onCameraRecentered: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cameraPositionState = rememberCameraPositionState()

    // Apply camera recentering when requested
    LaunchedEffect(shouldRecenterCamera) {
        if (shouldRecenterCamera && tripRoute.fullPath.isNotEmpty()) {
            val bounds = tripRoute.bounds
            val latLngBounds = LatLngBounds(
                LatLng(bounds.minLatitude, bounds.minLongitude),
                LatLng(bounds.maxLatitude, bounds.maxLongitude)
            )
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngBounds(latLngBounds, 100) // 100px padding
            )
            onCameraRecentered()
        }
    }

    // Map properties based on style
    val mapProperties = remember(mapStyle) {
        MapProperties(
            mapType = when (mapStyle) {
                MapStyle.STANDARD -> MapType.NORMAL
                MapStyle.SATELLITE -> MapType.SATELLITE
                MapStyle.TERRAIN -> MapType.TERRAIN
                MapStyle.DARK -> MapType.NORMAL // Apply dark style separately
            }
        )
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = mapProperties,
        uiSettings = MapUiSettings(
            zoomControlsEnabled = false,
            myLocationButtonEnabled = false,
            compassEnabled = true,
            mapToolbarEnabled = false
        )
    ) {
        // Draw route polyline
        if (tripRoute.fullPath.isNotEmpty()) {
            RoutePolyline(routePoints = tripRoute.fullPath)
        }

        // Start marker (green)
        if (tripRoute.fullPath.isNotEmpty()) {
            val startPoint = tripRoute.fullPath.first()
            Marker(
                state = MarkerState(position = LatLng(startPoint.latitude, startPoint.longitude)),
                title = "Start",
                snippet = tripRoute.startTime.toString()
            )
        }

        // End marker (red)
        if (tripRoute.fullPath.isNotEmpty()) {
            val endPoint = tripRoute.fullPath.last()
            Marker(
                state = MarkerState(position = LatLng(endPoint.latitude, endPoint.longitude)),
                title = "End",
                snippet = tripRoute.endTime.toString()
            )
        }

        // Photo markers
        tripRoute.photoMarkers.forEach { photoMarker ->
            PhotoMarkerIcon(
                photoMarker = photoMarker,
                isSelected = photoMarker.photoId == selectedPhotoMarker?.photoId,
                onClick = { onPhotoMarkerClick(photoMarker) }
            )
        }

        // Place visit markers
        tripRoute.visits.forEach { visit ->
            Marker(
                state = MarkerState(
                    position = LatLng(visit.centerLatitude, visit.centerLongitude)
                ),
                title = visit.poiName ?: visit.city ?: "Place",
                snippet = "${visit.startTime} - ${visit.endTime}"
            )
        }
    }
}

/**
 * Route polyline with color based on transport type.
 */
@Composable
private fun RoutePolyline(routePoints: List<RoutePoint>) {
    // Group consecutive points by transport type
    val segments = remember(routePoints) {
        groupByTransportType(routePoints)
    }

    // Draw each segment with appropriate color
    segments.forEach { (transportType, points) ->
        if (points.size >= 2) {
            Polyline(
                points = points.map { LatLng(it.latitude, it.longitude) },
                color = getTransportColor(transportType),
                width = getTransportWidth(transportType)
            )
        }
    }
}

/**
 * Photo marker on the map.
 */
@Composable
private fun PhotoMarkerIcon(
    photoMarker: PhotoMarker,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val markerState = MarkerState(
        position = LatLng(photoMarker.latitude, photoMarker.longitude)
    )

    // Custom photo marker (for now using standard marker, can be customized later)
    Marker(
        state = markerState,
        title = "Photo",
        snippet = photoMarker.timestamp.toString(),
        onClick = {
            onClick()
            true // Consume click
        }
    )
}

/**
 * Group route points by consecutive transport type for colored segments.
 */
private fun groupByTransportType(
    points: List<RoutePoint>
): List<Pair<TransportType, List<RoutePoint>>> {
    if (points.isEmpty()) return emptyList()

    val segments = mutableListOf<Pair<TransportType, List<RoutePoint>>>()
    var currentType = points.first().transportType
    var currentSegment = mutableListOf(points.first())

    for (i in 1 until points.size) {
        val point = points[i]
        if (point.transportType == currentType) {
            currentSegment.add(point)
        } else {
            // Transport type changed, save current segment and start new one
            segments.add(currentType to currentSegment.toList())
            currentType = point.transportType
            currentSegment = mutableListOf(point)
        }
    }

    // Add final segment
    if (currentSegment.isNotEmpty()) {
        segments.add(currentType to currentSegment.toList())
    }

    return segments
}

/**
 * Get polyline color based on transport type.
 */
private fun getTransportColor(type: TransportType): Color {
    return when (type) {
        TransportType.WALK -> Color(0xFF4CAF50)      // Green
        TransportType.BIKE -> Color(0xFF2196F3)      // Blue
        TransportType.CAR -> Color(0xFFFF9800)       // Orange
        TransportType.TRAIN -> Color(0xFF9C27B0)     // Purple
        TransportType.PLANE -> Color(0xFFF44336)     // Red
        TransportType.BOAT -> Color(0xFF00BCD4)      // Cyan
        TransportType.UNKNOWN -> Color(0xFF757575)   // Gray
    }
}

/**
 * Get polyline width based on transport type.
 */
private fun getTransportWidth(type: TransportType): Float {
    return when (type) {
        TransportType.WALK -> 5f
        TransportType.BIKE -> 6f
        TransportType.CAR -> 8f
        TransportType.TRAIN -> 10f
        TransportType.PLANE -> 12f
        TransportType.BOAT -> 8f
        TransportType.UNKNOWN -> 5f
    }
}
