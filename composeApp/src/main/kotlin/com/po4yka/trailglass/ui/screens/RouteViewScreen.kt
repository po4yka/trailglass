package com.po4yka.trailglass.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.po4yka.trailglass.feature.route.RouteViewController
import com.po4yka.trailglass.feature.route.export.ExportFormat
import com.po4yka.trailglass.platform.AndroidRouteShareHandler
import com.po4yka.trailglass.ui.screens.routeview.MapStyleSelectorSheet
import com.po4yka.trailglass.ui.screens.routeview.PrivacyWarningDialog
import com.po4yka.trailglass.ui.screens.routeview.RouteViewContent
import kotlinx.coroutines.launch

/** Route View screen - displays trip route on a map with visualization controls. */
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
            shareHandler
                .shareRouteFile(
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            modifier =
                Modifier
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
                    modifier =
                        Modifier
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
