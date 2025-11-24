package com.po4yka.trailglass.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GpsNotFixed
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.po4yka.trailglass.domain.model.ArcTrajectoryCalculator
import com.po4yka.trailglass.domain.model.CameraMove
import com.po4yka.trailglass.domain.model.Coordinate
import com.po4yka.trailglass.domain.model.MapDisplayData
import com.po4yka.trailglass.domain.model.MapEvent
import com.po4yka.trailglass.domain.model.MapEventSink
import com.po4yka.trailglass.domain.model.MapMarker
import com.po4yka.trailglass.domain.model.MapRoute
import com.po4yka.trailglass.domain.model.TransportType
import com.po4yka.trailglass.feature.map.MapController
import com.po4yka.trailglass.ui.theme.extended
import kotlinx.coroutines.launch

/** Google Maps view with markers, routes, and optional region overlays. */
@Composable
fun MapView(
    controller: MapController,
    modifier: Modifier = Modifier,
    onMarkerClick: (MapMarker) -> Unit = {},
    onRouteClick: (MapRoute) -> Unit = {},
    regions: List<com.po4yka.trailglass.domain.model.Region> = emptyList(),
    onRegionClick: (String) -> Unit = {}
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
                cameraMove = state.cameraMove,
                eventSink = controller,
                onMarkerClick = onMarkerClick,
                regions = regions,
                onRegionClick = onRegionClick,
                modifier = Modifier.fillMaxSize()
            )

            // Follow mode toggle button
            val scope = rememberCoroutineScope()
            FloatingActionButton(
                onClick = {
                    scope.launch {
                        controller.toggleFollowMode()
                    }
                },
                containerColor =
                    if (state.isFollowModeEnabled) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                modifier =
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 88.dp, end = 16.dp)
            ) {
                Icon(
                    imageVector =
                        if (state.isFollowModeEnabled) {
                            Icons.Default.GpsFixed
                        } else {
                            Icons.Default.GpsNotFixed
                        },
                    contentDescription =
                        if (state.isFollowModeEnabled) {
                            "Disable follow mode"
                        } else {
                            "Enable follow mode"
                        }
                )
            }

            // Fit to data button
            if (state.mapData.markers.isNotEmpty() || state.mapData.routes.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { controller.fitToData() },
                    modifier =
                        Modifier
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
    cameraMove: CameraMove?,
    eventSink: MapEventSink,
    onMarkerClick: (MapMarker) -> Unit,
    regions: List<com.po4yka.trailglass.domain.model.Region> = emptyList(),
    onRegionClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Initialize camera position state
    val cameraPositionState =
        rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 2f)
        }

    // Apply camera movements with appropriate animations
    LaunchedEffect(cameraMove) {
        cameraMove?.let { move ->
            when (move) {
                is CameraMove.Instant -> {
                    // Instant movement - no animation
                    cameraPositionState.position = move.position.toGmsCameraPosition()
                }

                is CameraMove.Ease -> {
                    // Smooth easing animation
                    cameraPositionState.animate(
                        update =
                            com.google.android.gms.maps.CameraUpdateFactory.newCameraPosition(
                                move.position.toGmsCameraPosition()
                            ),
                        durationMs = move.durationMs
                    )
                }

                is CameraMove.Fly -> {
                    // Fly-to animation with arc trajectory
                    // Get current position
                    val currentPosition = cameraPositionState.position.toDomainCameraPosition()

                    // Calculate arc trajectory waypoints
                    val arcWaypoints =
                        ArcTrajectoryCalculator.calculateArcTrajectory(
                            start = currentPosition,
                            end = move.position,
                            steps = 20 // 20 intermediate steps for smooth arc
                        )

                    // Animate through each waypoint
                    // Each segment gets a fraction of the total duration
                    val segmentDuration = move.durationMs / arcWaypoints.size

                    for (waypoint in arcWaypoints.drop(1)) { // Skip first (current position)
                        cameraPositionState.animate(
                            update =
                                com.google.android.gms.maps.CameraUpdateFactory.newCameraPosition(
                                    waypoint.toGmsCameraPosition()
                                ),
                            durationMs = segmentDuration
                        )
                    }
                }

                is CameraMove.FollowUser -> {
                    // Follow user mode - centers camera on user location
                    // Note: Continuous tracking requires periodic location updates from MapController
                    // Current implementation: one-time center on last known location

                    // The map already has location tracking enabled via myLocationButtonEnabled
                    // When follow mode is toggled, center on user's current position
                    // The MapController should send periodic FollowUser moves for continuous tracking

                    // Get last known location from camera state (if available)
                    // For now, we rely on the map's built-in location tracking
                    // and the controller sending updated positions periodically

                    // Create camera position with user-specified parameters
                    // The actual user location should come from MapController via move.position
                    // but for basic follow mode, we can just apply the zoom/tilt/bearing settings

                    // This is a simplified implementation that applies zoom/tilt/bearing
                    // Full implementation would track user location continuously
                    val currentTarget = cameraPositionState.position.target
                    val followPosition =
                        CameraPosition
                            .Builder()
                            .target(currentTarget) // Keep current target for now
                            .zoom(move.zoom)
                            .tilt(move.tilt)
                            .bearing(move.bearing)
                            .build()

                    cameraPositionState.animate(
                        update =
                            com.google.android.gms.maps.CameraUpdateFactory
                                .newCameraPosition(followPosition),
                        durationMs = 500
                    )
                }
            }
        }
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        onMapClick = { latLng ->
            // Send map tapped event
            eventSink.send(
                MapEvent.MapTapped(
                    Coordinate(latitude = latLng.latitude, longitude = latLng.longitude)
                )
            )
        },
        onMapLoaded = {
            // Send map ready event
            eventSink.send(MapEvent.MapReady)
        },
        uiSettings =
            MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = true,
                compassEnabled = true
            )
    ) {
        // Draw routes (polylines)
        mapData.routes.forEach { route ->
            val points =
                route.coordinates.map { coord ->
                    LatLng(coord.latitude, coord.longitude)
                }

            // Determine route color: use explicit color if set, otherwise use historical route color
            val routeColor =
                route.color?.let { colorValue ->
                    Color(colorValue)
                } ?: MaterialTheme.colorScheme.extended.historicalRoute

            // Draw outline for better visibility
            Polyline(
                points = points,
                color = Color.White.copy(alpha = 0.7f),
                width = getRouteWidth(route.transportType) + 4f,
                geodesic = true,
                clickable = false
            )

            // Draw main route line
            Polyline(
                points = points,
                color = routeColor,
                width = getRouteWidth(route.transportType),
                geodesic = true,
                clickable = true,
                onClick = {
                    // Note: Polyline clicks aren't directly supported in Compose Maps
                    // Would need custom implementation
                }
            )
        }

        // Draw regions (circles with dotted borders)
        regions.forEach { region ->
            Circle(
                center = LatLng(region.latitude, region.longitude),
                radius = region.radiusMeters,
                strokeColor = MaterialTheme.colorScheme.tertiary,
                strokeWidth = 3f,
                strokePattern =
                    listOf(
                        com.google.maps.android.compose
                            .DashPattern(10f),
                        com.google.maps.android.compose
                            .GapPattern(10f)
                    ),
                fillColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
                clickable = true,
                onClick = {
                    onRegionClick(region.id)
                }
            )

            // Small marker at region center
            Marker(
                state =
                    MarkerState(
                        position = LatLng(region.latitude, region.longitude)
                    ),
                title = region.name,
                snippet = "${region.enterCount} visits",
                alpha = 0.7f
            )
        }

        // Draw markers
        mapData.markers.forEach { marker ->
            Marker(
                state =
                    MarkerState(
                        position = LatLng(marker.coordinate.latitude, marker.coordinate.longitude)
                    ),
                title = marker.title,
                snippet = marker.snippet,
                onClick = {
                    // Send marker tapped event
                    eventSink.send(MapEvent.MarkerTapped(marker.id))
                    // Also call the external callback for additional handling
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

/** Get route width based on transport type. */
private fun getRouteWidth(transportType: TransportType): Float =
    when (transportType) {
        TransportType.WALK -> 8f
        TransportType.BIKE -> 10f
        TransportType.CAR -> 12f
        TransportType.TRAIN -> 14f
        TransportType.PLANE -> 16f
        TransportType.BOAT -> 12f
        TransportType.UNKNOWN -> 8f
    }

/** Convert domain CameraPosition to Google Maps CameraPosition. */
private fun com.po4yka.trailglass.domain.model.CameraPosition.toGmsCameraPosition(): CameraPosition =
    CameraPosition
        .Builder()
        .target(LatLng(target.latitude, target.longitude))
        .zoom(zoom)
        .tilt(tilt)
        .bearing(bearing)
        .build()

/** Convert Google Maps CameraPosition to domain CameraPosition. */
private fun CameraPosition.toDomainCameraPosition(): com.po4yka.trailglass.domain.model.CameraPosition =
    com.po4yka.trailglass.domain.model.CameraPosition(
        target =
            Coordinate(
                latitude = target.latitude,
                longitude = target.longitude
            ),
        zoom = zoom,
        tilt = tilt,
        bearing = bearing
    )
