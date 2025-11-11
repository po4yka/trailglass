package com.po4yka.trailglass.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.po4yka.trailglass.domain.model.Coordinate
import com.po4yka.trailglass.domain.model.PhotoMarker
import com.po4yka.trailglass.domain.model.TripRoute
import com.po4yka.trailglass.feature.route.MapStyle
import com.po4yka.trailglass.feature.route.RouteViewController
import com.po4yka.trailglass.ui.components.RouteMapView
import com.po4yka.trailglass.ui.components.RouteSummaryCard

/**
 * Route View screen - displays trip route on a map with visualization controls.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteViewScreen(
    tripId: String,
    controller: RouteViewController,
    onNavigateToReplay: (String) -> Unit = {},
    onNavigateToStatistics: (String) -> Unit = {},
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by controller.state.collectAsState()

    // Load route data on first composition
    LaunchedEffect(tripId) {
        controller.loadRoute(tripId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Route") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Map style selector button
                    IconButton(onClick = { controller.toggleMapStyleSelector() }) {
                        Icon(Icons.Default.Layers, contentDescription = "Map Style")
                    }

                    // Recenter button
                    IconButton(onClick = { controller.recenterCamera() }) {
                        Icon(Icons.Default.MyLocation, contentDescription = "Recenter")
                    }

                    // Overflow menu
                    var showMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Replay") },
                            onClick = {
                                showMenu = false
                                onNavigateToReplay(tripId)
                            },
                            leadingIcon = { Icon(Icons.Default.PlayArrow, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Statistics") },
                            onClick = {
                                showMenu = false
                                onNavigateToStatistics(tripId)
                            },
                            leadingIcon = { Icon(Icons.Default.BarChart, null) }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Share Map") },
                            onClick = { showMenu = false },
                            leadingIcon = { Icon(Icons.Default.Share, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Export GPX") },
                            onClick = { showMenu = false },
                            leadingIcon = { Icon(Icons.Default.Download, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Export KML") },
                            onClick = { showMenu = false },
                            leadingIcon = { Icon(Icons.Default.Download, null) }
                        )
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    // Loading state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator()
                            Text(
                                text = "Loading route...",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }

                state.error != null -> {
                    // Error state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = state.error ?: "Unknown error",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                            Button(onClick = { controller.loadRoute(tripId) }) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Retry")
                            }
                        }
                    }
                }

                state.tripRoute != null -> {
                    // Content state
                    RouteViewContent(
                        tripRoute = state.tripRoute,
                        mapStyle = state.mapStyle,
                        shouldRecenterCamera = state.shouldRecenterCamera,
                        selectedPhotoMarker = state.selectedPhotoMarker,
                        onPhotoMarkerClick = { marker -> controller.selectPhotoMarker(marker) },
                        onCameraRecentered = { controller.acknowledgeRecenter() },
                        onPlayClick = { onNavigateToReplay(tripId) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Map style selector bottom sheet
            if (state.showMapStyleSelector) {
                MapStyleSelectorSheet(
                    currentStyle = state.mapStyle,
                    onStyleSelected = { style ->
                        controller.setMapStyle(style)
                        controller.toggleMapStyleSelector()
                    },
                    onDismiss = { controller.toggleMapStyleSelector() }
                )
            }
        }
    }
}

/**
 * Main content showing the route map and summary card.
 */
@Composable
private fun RouteViewContent(
    tripRoute: TripRoute,
    mapStyle: MapStyle,
    shouldRecenterCamera: Boolean,
    selectedPhotoMarker: PhotoMarker?,
    onPhotoMarkerClick: (PhotoMarker) -> Unit,
    onCameraRecentered: () -> Unit,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        // Map view
        RouteMapView(
            tripRoute = tripRoute,
            mapStyle = mapStyle,
            shouldRecenterCamera = shouldRecenterCamera,
            selectedPhotoMarker = selectedPhotoMarker,
            onPhotoMarkerClick = onPhotoMarkerClick,
            onCameraRecentered = onCameraRecentered,
            modifier = Modifier.fillMaxSize()
        )

        // Summary card at the bottom
        RouteSummaryCard(
            tripRoute = tripRoute,
            onPlayClick = onPlayClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

/**
 * Map style selector bottom sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MapStyleSelectorSheet(
    currentStyle: MapStyle,
    onStyleSelected: (MapStyle) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Choose Map Style",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            MapStyle.entries.forEach { style ->
                MapStyleOption(
                    style = style,
                    isSelected = style == currentStyle,
                    onClick = { onStyleSelected(style) }
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

/**
 * Individual map style option in the selector.
 */
@Composable
private fun MapStyleOption(
    style: MapStyle,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = MaterialTheme.shapes.medium,
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (style) {
                        MapStyle.STANDARD -> Icons.Default.Map
                        MapStyle.SATELLITE -> Icons.Default.Satellite
                        MapStyle.TERRAIN -> Icons.Default.Terrain
                        MapStyle.DARK -> Icons.Default.DarkMode
                    },
                    contentDescription = null,
                    tint = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Text(
                    text = style.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            }

            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
