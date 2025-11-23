package com.po4yka.trailglass.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.po4yka.trailglass.domain.model.EnhancedMapDisplayData
import com.po4yka.trailglass.domain.model.EnhancedMapMarker
import com.po4yka.trailglass.domain.model.HeatmapData
import com.po4yka.trailglass.domain.model.MapRoute
import com.po4yka.trailglass.domain.model.MarkerCluster
import com.po4yka.trailglass.feature.map.EnhancedMapController

/** Enhanced Google Maps view with clustering, heatmap, and custom markers. */
@Composable
fun EnhancedMapView(
    controller: EnhancedMapController,
    modifier: Modifier = Modifier,
    onMarkerClick: (EnhancedMapMarker) -> Unit = {},
    onClusterClick: (MarkerCluster) -> Unit = {},
    onRouteClick: (MapRoute) -> Unit = {}
) {
    val state by controller.state.collectAsState()
    rememberCoroutineScope()

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
            MapVisualizationSelector(
                currentMode = state.visualizationMode,
                onModeChange = { mode ->
                    controller.setVisualizationMode(mode)
                },
                modifier =
                    Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
            )

            // Control panel for clustering and heatmap
            MapOptionsPanel(
                clusteringEnabled = state.clusteringEnabled,
                heatmapEnabled = state.heatmapEnabled,
                onToggleClustering = { controller.toggleClustering() },
                onToggleHeatmap = { controller.toggleHeatmap() },
                modifier =
                    Modifier
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
    val cameraPositionState =
        rememberCameraPositionState {
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
        uiSettings =
            MapUiSettings(
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
