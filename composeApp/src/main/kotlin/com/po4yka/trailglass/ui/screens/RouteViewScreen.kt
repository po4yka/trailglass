package com.po4yka.trailglass.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Satellite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.po4yka.trailglass.domain.model.Coordinate
import com.po4yka.trailglass.domain.model.PhotoMarker
import com.po4yka.trailglass.domain.model.TripRoute
import com.po4yka.trailglass.feature.route.MapStyle
import com.po4yka.trailglass.feature.route.RouteViewController
import com.po4yka.trailglass.feature.route.export.ExportFormat
import com.po4yka.trailglass.platform.AndroidRouteShareHandler
import com.po4yka.trailglass.ui.components.RouteMapView
import com.po4yka.trailglass.ui.components.RouteSummaryCard
import kotlinx.coroutines.launch

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
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var pendingExport by remember { mutableStateOf<com.po4yka.trailglass.feature.route.export.ExportResult?>(null) }

    // Load route data on first composition
    LaunchedEffect(tripId) {
        controller.loadRoute(tripId)
    }

    // Show privacy dialog when export is ready
    LaunchedEffect(state.exportResult) {
        state.exportResult?.let { exportResult ->
            pendingExport = exportResult
            showPrivacyDialog = true
        }
    }

    // Handle actual sharing after privacy confirmation
    fun shareExport(exportResult: com.po4yka.trailglass.feature.route.export.ExportResult) {
        scope.launch {
            val shareHandler = AndroidRouteShareHandler(context)
            shareHandler.shareRouteFile(
                fileName = exportResult.fileName,
                content = exportResult.content,
                mimeType = exportResult.mimeType
            ).onSuccess {
                controller.clearExportResult()
                snackbarHostState.showSnackbar(
                    message = "Export ready to share",
                    duration = SnackbarDuration.Short
                )
            }.onFailure { error ->
                controller.clearExportResult()
                snackbarHostState.showSnackbar(
                    message = "Failed to share: ${error.message}",
                    duration = SnackbarDuration.Short
                )
            }
        }
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
                            onClick = {
                                showMenu = false
                                // TODO: Implement map snapshot sharing
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "Map snapshot sharing coming soon!",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            },
                            leadingIcon = { Icon(Icons.Default.Share, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Export GPX") },
                            onClick = {
                                showMenu = false
                                val tripName = state.tripRoute?.tripId ?: "route"
                                controller.exportRoute(tripName, ExportFormat.GPX)
                            },
                            leadingIcon = { Icon(Icons.Default.Download, null) },
                            enabled = !state.isExporting
                        )
                        DropdownMenuItem(
                            text = { Text("Export KML") },
                            onClick = {
                                showMenu = false
                                val tripName = state.tripRoute?.tripId ?: "route"
                                controller.exportRoute(tripName, ExportFormat.KML)
                            },
                            leadingIcon = { Icon(Icons.Default.Download, null) },
                            enabled = !state.isExporting
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                    state.tripRoute?.let { tripRoute ->
                        RouteViewContent(
                            tripRoute = tripRoute,
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

            // Privacy warning dialog
            if (showPrivacyDialog && pendingExport != null) {
                PrivacyWarningDialog(
                    exportResult = pendingExport!!,
                    onConfirm = {
                        showPrivacyDialog = false
                        shareExport(pendingExport!!)
                        pendingExport = null
                    },
                    onDismiss = {
                        showPrivacyDialog = false
                        controller.clearExportResult()
                        pendingExport = null
                    }
                )
            }

            // Loading indicator for export
            if (state.isExporting) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = "Preparing export...",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
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

/**
 * Privacy warning dialog shown before sharing location data.
 */
@Composable
private fun PrivacyWarningDialog(
    exportResult: com.po4yka.trailglass.feature.route.export.ExportResult,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text("Privacy Warning")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = exportResult.privacyInfo.warningMessage,
                    style = MaterialTheme.typography.bodyMedium
                )

                HorizontalDivider()

                // Show detailed privacy info
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "File details:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "• ${exportResult.privacyInfo.numberOfPoints} GPS points",
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (exportResult.privacyInfo.numberOfPhotos > 0) {
                        Text(
                            text = "• ${exportResult.privacyInfo.numberOfPhotos} photo locations",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Text(
                        text = "• Timestamps included",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Text(
                    text = "Please ensure you trust the recipient before sharing.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Share Anyway")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
