package com.po4yka.trailglass.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.po4yka.trailglass.domain.model.*
import com.po4yka.trailglass.feature.map.MapController

/**
 * Google Maps view with markers and routes.
 */
@Composable
fun MapView(
    controller: MapController,
    modifier: Modifier = Modifier,
    onMarkerClick: (MapMarker) -> Unit = {},
    onRouteClick: (MapRoute) -> Unit = {}
) {
    val state by controller.state.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        } else if (state.error != null) {
            MapErrorView(
                error = state.error!!,
                onRetry = { controller.refresh() },
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            GoogleMapContent(
                mapData = state.mapData,
                cameraPosition = state.cameraPosition,
                onMarkerClick = {
                    controller.selectMarker(it)
                    onMarkerClick(it)
                },
                onMapClick = {
                    controller.deselectMarker()
                },
                modifier = Modifier.fillMaxSize()
            )

            // Fit to data button
            if (state.mapData.markers.isNotEmpty() || state.mapData.routes.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { controller.fitToData() },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Fit to data")
                }
            }
        }
    }
}

@Composable
private fun GoogleMapContent(
    mapData: MapDisplayData,
    cameraPosition: CameraPosition?,
    onMarkerClick: (MapMarker) -> Unit,
    onMapClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Convert camera position
    val cameraPositionState = rememberCameraPositionState {
        position = cameraPosition?.let {
            CameraPosition.fromLatLngZoom(
                LatLng(it.target.latitude, it.target.longitude),
                it.zoom
            )
        } ?: CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 2f)
    }

    // Update camera when position changes
    LaunchedEffect(cameraPosition) {
        cameraPosition?.let {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                LatLng(it.target.latitude, it.target.longitude),
                it.zoom
            )
        }
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        onMapClick = { onMapClick() },
        uiSettings = MapUiSettings(
            zoomControlsEnabled = false,
            myLocationButtonEnabled = true,
            compassEnabled = true
        )
    ) {
        // Draw routes (polylines)
        mapData.routes.forEach { route ->
            val points = route.coordinates.map { coord ->
                LatLng(coord.latitude, coord.longitude)
            }

            Polyline(
                points = points,
                color = Color(route.color ?: 0xFF2196F3.toInt()),
                width = getRouteWidth(route.transportType),
                clickable = true,
                onClick = {
                    // Note: Polyline clicks aren't directly supported in Compose Maps
                    // Would need custom implementation
                }
            )
        }

        // Draw markers
        mapData.markers.forEach { marker ->
            Marker(
                state = MarkerState(
                    position = LatLng(marker.coordinate.latitude, marker.coordinate.longitude)
                ),
                title = marker.title,
                snippet = marker.snippet,
                onClick = {
                    onMarkerClick(marker)
                    true // Consume the event
                }
            )
        }
    }
}

@Composable
private fun MapErrorView(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )

        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

/**
 * Get route width based on transport type.
 */
private fun getRouteWidth(transportType: TransportType): Float {
    return when (transportType) {
        TransportType.WALK -> 8f
        TransportType.BIKE -> 10f
        TransportType.CAR -> 12f
        TransportType.TRAIN -> 14f
        TransportType.PLANE -> 16f
        TransportType.BOAT -> 12f
        TransportType.UNKNOWN -> 8f
    }
}
