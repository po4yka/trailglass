package com.po4yka.trailglass.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.po4yka.trailglass.domain.model.Coordinate
import com.po4yka.trailglass.domain.model.TransportType
import com.po4yka.trailglass.domain.model.TripRoute
import com.po4yka.trailglass.feature.route.VehicleState
import com.po4yka.trailglass.ui.theme.extended

/**
 * Map view for route replay with animated vehicle and camera following.
 */
@Composable
fun RouteReplayMapView(
    tripRoute: TripRoute,
    vehicleState: VehicleState,
    cameraPosition: Coordinate,
    cameraBearing: Double,
    cameraTilt: Double,
    cameraZoom: Float,
    modifier: Modifier = Modifier
) {
    val cameraPositionState = rememberCameraPositionState()

    // Update camera to follow vehicle
    LaunchedEffect(cameraPosition, cameraBearing, cameraTilt) {
        val newCameraPosition = CameraPosition.Builder()
            .target(LatLng(cameraPosition.latitude, cameraPosition.longitude))
            .bearing(cameraBearing.toFloat())
            .tilt(cameraTilt.toFloat())
            .zoom(cameraZoom)
            .build()

        cameraPositionState.animate(
            CameraUpdateFactory.newCameraPosition(newCameraPosition),
            durationMs = 100 // Smooth animation
        )
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            mapType = MapType.NORMAL
        ),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = false,
            myLocationButtonEnabled = false,
            compassEnabled = false,
            mapToolbarEnabled = false,
            zoomGesturesEnabled = false,  // Disable user gestures during replay
            scrollGesturesEnabled = false,
            tiltGesturesEnabled = false
            // Note: rotateGesturesEnabled not available in maps-compose 6.2.0
        )
    ) {
        // Draw route polyline
        if (tripRoute.fullPath.isNotEmpty()) {
            RoutePolylineReplay(routePoints = tripRoute.fullPath)
        }

        // Vehicle marker
        VehicleMarker(
            vehicleState = vehicleState,
            transportType = vehicleState.currentPoint.transportType
        )
    }
}

/**
 * Route polyline for replay (draws the entire route).
 * Uses Silent Waters palette: Coastal Path for active/replaying routes.
 */
@Composable
private fun RoutePolylineReplay(
    routePoints: List<com.po4yka.trailglass.domain.model.RoutePoint>
) {
    // Use Coastal Path color for active route being replayed
    val routeColor = MaterialTheme.colorScheme.extended.activeRoute

    // Draw single polyline for the entire route
    Polyline(
        points = routePoints.map { LatLng(it.latitude, it.longitude) },
        color = routeColor,
        width = 8f,
        zIndex = 1f
    )
}

/**
 * Animated vehicle marker.
 */
@Composable
private fun VehicleMarker(
    vehicleState: VehicleState,
    transportType: TransportType
) {
    val markerState = remember {
        MarkerState(position = LatLng(vehicleState.position.latitude, vehicleState.position.longitude))
    }

    // Update marker position and rotation
    LaunchedEffect(vehicleState) {
        markerState.position = LatLng(vehicleState.position.latitude, vehicleState.position.longitude)
    }

    Marker(
        state = markerState,
        title = "Current Position",
        snippet = formatVehicleInfo(vehicleState, transportType),
        rotation = vehicleState.bearing.toFloat(),
        flat = true, // Makes marker rotate with bearing
        zIndex = 10f // On top of route
    )
}

/**
 * Format vehicle information for marker snippet.
 */
private fun formatVehicleInfo(vehicleState: VehicleState, transportType: TransportType): String {
    val speed = vehicleState.currentPoint.speed
    return buildString {
        append(transportType.name.lowercase().replaceFirstChar { it.uppercase() })
        if (speed != null) {
            append(" • ${(speed * 3.6).toInt()} km/h")
        }
    }
}
