package com.po4yka.trailglass.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.GroupWork
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.TileOverlay
import com.google.android.gms.maps.model.TileOverlayOptions
import com.google.maps.android.compose.*
import com.google.maps.android.heatmaps.HeatmapTileProvider
import com.po4yka.trailglass.domain.model.*
import com.po4yka.trailglass.feature.map.EnhancedMapController
import com.po4yka.trailglass.feature.map.MarkerIconProvider
import kotlinx.coroutines.launch

/**
 * Enhanced Google Maps view with clustering, heatmap, and custom markers.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedMapView(
    controller: EnhancedMapController,
    modifier: Modifier = Modifier,
    onMarkerClick: (EnhancedMapMarker) -> Unit = {},
    onClusterClick: (MarkerCluster) -> Unit = {},
    onRouteClick: (MapRoute) -> Unit = {}
) {
    val state by controller.state.collectAsState()
    val scope = rememberCoroutineScope()

    Box(modifier = modifier.fillMaxSize()) {
        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        } else if (state.error != null) {
            EnhancedMapErrorView(
                error = state.error!!,
                onRetry = { controller.clearError() },
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            EnhancedGoogleMapContent(
                mapData = state.mapData,
                selectedMarker = state.selectedMarker,
                selectedCluster = state.selectedCluster,
                selectedRoute = state.selectedRoute,
                onMarkerClick = { marker ->
                    controller.selectMarker(marker)
                    onMarkerClick(marker)
                },
                onClusterClick = { cluster ->
                    controller.selectCluster(cluster)
                    onClusterClick(cluster)
                },
                onRouteClick = { route ->
                    controller.selectRoute(route)
                    onRouteClick(route)
                },
                onZoomChange = { zoomLevel ->
                    controller.updateZoom(zoomLevel)
                },
                modifier = Modifier.fillMaxSize()
            )

            // Visualization mode selector
            VisualizationModeSelector(
                currentMode = state.visualizationMode,
                onModeChange = { mode ->
                    controller.setVisualizationMode(mode)
                },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            )

            // Control panel for clustering and heatmap
            EnhancedMapControls(
                clusteringEnabled = state.clusteringEnabled,
                heatmapEnabled = state.heatmapEnabled,
                onToggleClustering = { controller.toggleClustering() },
                onToggleHeatmap = { controller.toggleHeatmap() },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            )
        }
    }
}

@Composable
private fun EnhancedGoogleMapContent(
    mapData: EnhancedMapDisplayData,
    selectedMarker: EnhancedMapMarker?,
    selectedCluster: MarkerCluster?,
    selectedRoute: MapRoute?,
    onMarkerClick: (EnhancedMapMarker) -> Unit,
    onClusterClick: (MarkerCluster) -> Unit,
    onRouteClick: (MapRoute) -> Unit,
    onZoomChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val cameraPositionState = rememberCameraPositionState {
        position = mapData.region?.let {
            // Calculate appropriate zoom level based on region deltas
            val zoom = calculateZoomLevel(it.latitudeDelta, it.longitudeDelta)
            CameraPosition.fromLatLngZoom(
                LatLng(it.center.latitude, it.center.longitude),
                zoom
            )
        } ?: CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 2f)
    }

    // Track zoom level changes
    LaunchedEffect(cameraPositionState.position.zoom) {
        onZoomChange(cameraPositionState.position.zoom)
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        uiSettings = MapUiSettings(
            zoomControlsEnabled = false,
            myLocationButtonEnabled = true,
            compassEnabled = true
        )
    ) {
        // Draw heatmap if enabled
        val heatmapData = mapData.heatmapData
        if (mapData.heatmapEnabled && heatmapData is HeatmapData) {
            RenderHeatmap(heatmapData = heatmapData)
        }

        // Draw routes
        mapData.routes.forEach { route ->
            val isSelected = route.id == selectedRoute?.id
            RenderRoute(
                route = route,
                isSelected = isSelected,
                onClick = { onRouteClick(route) }
            )
        }

        // Draw clusters
        mapData.clusters.forEach { cluster ->
            val isSelected = cluster.id == selectedCluster?.id
            RenderCluster(
                cluster = cluster,
                isSelected = isSelected,
                onClick = { onClusterClick(cluster) }
            )
        }

        // Draw individual markers
        mapData.markers.forEach { marker ->
            val isSelected = marker.id == selectedMarker?.id
            RenderEnhancedMarker(
                marker = marker,
                isSelected = isSelected,
                onClick = { onMarkerClick(marker) }
            )
        }
    }
}

@Composable
private fun RenderEnhancedMarker(
    marker: EnhancedMapMarker,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val icon = MarkerIconProvider.getIcon(marker.category, marker.isFavorite)

    Marker(
        state = MarkerState(
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
private fun RenderCluster(
    cluster: MarkerCluster,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val clusterColor = MarkerIconProvider.getClusterColor(cluster.count)

    Marker(
        state = MarkerState(
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
private fun RenderRoute(
    route: MapRoute,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val points = route.coordinates.map { coord ->
        LatLng(coord.latitude, coord.longitude)
    }

    val routeColor = MarkerIconProvider.getRouteColor(route.transportType.name)

    Polyline(
        points = points,
        color = if (isSelected) {
            Color(routeColor).copy(alpha = 1f)
        } else {
            Color(routeColor).copy(alpha = 0.7f)
        },
        width = if (isSelected) {
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

@Composable
private fun RenderHeatmap(heatmapData: HeatmapData) {
    // Convert heatmap points to WeightedLatLng
    val heatmapPoints = remember(heatmapData) {
        heatmapData.points.map { point ->
            com.google.maps.android.heatmaps.WeightedLatLng(
                LatLng(point.coordinate.latitude, point.coordinate.longitude),
                point.intensity.toDouble()
            )
        }
    }

    // Track the current overlay reference to enable proper cleanup
    var currentOverlay by remember { mutableStateOf<TileOverlay?>(null) }

    // Use DisposableEffect for proper lifecycle management
    // This ensures cleanup when the composable leaves composition or when heatmapPoints change
    DisposableEffect(heatmapPoints) {
        onDispose {
            // Remove the overlay when the effect is disposed or keys change
            currentOverlay?.remove()
            currentOverlay = null
        }
    }

    // Render heatmap using MapEffect
    MapEffect(heatmapPoints) { map ->
        // Remove previous overlay before adding a new one
        currentOverlay?.remove()

        // Create heatmap tile provider with custom gradient colors
        val heatmapProvider = HeatmapTileProvider.Builder()
            .weightedData(heatmapPoints)
            .radius(50) // Radius of influence for each point in pixels
            .opacity(0.6) // Transparency of heatmap layer
            .build()

        // Add tile overlay to the map and store the reference
        currentOverlay = map.addTileOverlay(
            TileOverlayOptions().tileProvider(heatmapProvider)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VisualizationModeSelector(
    currentMode: MapVisualizationMode,
    onModeChange: (MapVisualizationMode) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        tonalElevation = 2.dp
    ) {
        Column {
            FilterChip(
                selected = true,
                onClick = { expanded = !expanded },
                label = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = getModeIcon(currentMode),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(getModeLabel(currentMode))
                        Icon(
                            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                modifier = Modifier.padding(8.dp)
            )

            if (expanded) {
                Column(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    MapVisualizationMode.entries.forEach { mode ->
                        if (mode != currentMode) {
                            FilterChip(
                                selected = false,
                                onClick = {
                                    onModeChange(mode)
                                    expanded = false
                                },
                                label = {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = getModeIcon(mode),
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(getModeLabel(mode))
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EnhancedMapControls(
    clusteringEnabled: Boolean,
    heatmapEnabled: Boolean,
    onToggleClustering: () -> Unit,
    onToggleHeatmap: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Clustering toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.GroupWork,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Text("Cluster", style = MaterialTheme.typography.bodySmall)
                Switch(
                    checked = clusteringEnabled,
                    onCheckedChange = { onToggleClustering() },
                    modifier = Modifier.height(24.dp)
                )
            }

            // Heatmap toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Whatshot,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Text("Heatmap", style = MaterialTheme.typography.bodySmall)
                Switch(
                    checked = heatmapEnabled,
                    onCheckedChange = { onToggleHeatmap() },
                    modifier = Modifier.height(24.dp)
                )
            }
        }
    }
}

@Composable
private fun EnhancedMapErrorView(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.dp)
        )

        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )

        Button(onClick = onRetry) {
            Text("Dismiss")
        }
    }
}

/**
 * Get marker bitmap descriptor with custom color and styling.
 *
 * Uses custom-generated bitmaps with proper marker shape and selection highlighting.
 */
private fun getMarkerBitmapDescriptor(color: Int, isSelected: Boolean): BitmapDescriptor {
    return MapMarkerBitmapGenerator.getCachedMarkerBitmap(
        color = color,
        isSelected = isSelected
    )
}

/**
 * Get cluster bitmap descriptor with count badge.
 *
 * Generates custom circular cluster markers with count text overlay.
 */
private fun getClusterBitmapDescriptor(
    count: Int,
    color: Int,
    isSelected: Boolean
): BitmapDescriptor {
    return MapMarkerBitmapGenerator.getCachedClusterBitmap(
        count = count,
        color = color,
        isSelected = isSelected
    )
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

/**
 * Get icon for visualization mode.
 */
private fun getModeIcon(mode: MapVisualizationMode) = when (mode) {
    MapVisualizationMode.MARKERS -> Icons.Default.Place
    MapVisualizationMode.CLUSTERS -> Icons.Default.GroupWork
    MapVisualizationMode.HEATMAP -> Icons.Default.Whatshot
    MapVisualizationMode.HYBRID -> Icons.Default.Layers
}

/**
 * Get label for visualization mode.
 */
private fun getModeLabel(mode: MapVisualizationMode) = when (mode) {
    MapVisualizationMode.MARKERS -> "Markers"
    MapVisualizationMode.CLUSTERS -> "Clusters"
    MapVisualizationMode.HEATMAP -> "Heatmap"
    MapVisualizationMode.HYBRID -> "Hybrid"
}

/**
 * Calculate appropriate zoom level based on region deltas.
 * Uses the larger delta (latitude or longitude) to determine zoom.
 */
private fun calculateZoomLevel(latitudeDelta: Double, longitudeDelta: Double): Float {
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
