package com.po4yka.trailglass.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.po4yka.trailglass.feature.timeline.EnhancedTimelineController
import com.po4yka.trailglass.ui.components.ErrorView
import com.po4yka.trailglass.ui.screens.timeline.ActiveFiltersChips
import com.po4yka.trailglass.ui.screens.timeline.DateNavigationBar
import com.po4yka.trailglass.ui.screens.timeline.EmptyTimelineView
import com.po4yka.trailglass.ui.screens.timeline.EnhancedTimelineContent
import com.po4yka.trailglass.ui.screens.timeline.TimelineFilterBottomSheet
import com.po4yka.trailglass.ui.screens.timeline.TimelineSearchBar
import com.po4yka.trailglass.ui.screens.timeline.ZoomLevelSelector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedTimelineScreen(
    controller: EnhancedTimelineController,
    modifier: Modifier = Modifier,
    trackingController: com.po4yka.trailglass.feature.tracking.LocationTrackingController? = null,
    onAddPhoto: () -> Unit = {},
    onAddNote: () -> Unit = {},
    onCheckIn: () -> Unit = {}
) {
    val state by controller.state.collectAsState()
    var showFilterSheet by remember { mutableStateOf(false) }
    var showSearchBar by remember { mutableStateOf(false) }

    // Tracking state
    val trackingState by (trackingController?.uiState?.collectAsState() ?: remember { mutableStateOf(null) })
    val isTracking = trackingState?.trackingState?.isTracking ?: false

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (showSearchBar) {
                        TimelineSearchBar(
                            query = state.searchQuery ?: "",
                            onQueryChange = { controller.search(it.takeIf { q -> q.isNotBlank() }) },
                            onClose = {
                                showSearchBar = false
                                controller.clearSearch()
                            }
                        )
                    } else {
                        Text("Timeline")
                    }
                },
                actions = {
                    if (!showSearchBar) {
                        IconButton(onClick = { showSearchBar = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                        IconButton(onClick = { showFilterSheet = true }) {
                            Badge(
                                containerColor =
                                    if (state.filter.isActive) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    }
                            ) {
                                if (state.filter.activeFilterCount > 0) {
                                    Text("${state.filter.activeFilterCount}")
                                }
                            }
                            Icon(Icons.Default.FilterList, contentDescription = "Filters")
                        }
                        IconButton(onClick = { controller.refresh() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (trackingController != null) {
                com.po4yka.trailglass.ui.components.TrackingFABMenu(
                    isTracking = isTracking,
                    onToggleTracking = {
                        if (isTracking) {
                            trackingController.stopTracking()
                        } else {
                            trackingController.startTracking(
                                com.po4yka.trailglass.location.tracking.TrackingMode.ACTIVE
                            )
                        }
                    },
                    onAddPhoto = onAddPhoto,
                    onAddNote = onAddNote,
                    onCheckIn = onCheckIn
                )
            }
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            // Zoom level selector
            ZoomLevelSelector(
                currentZoom = state.zoomLevel,
                onZoomChanged = { controller.setZoomLevel(it) }
            )

            // Date navigation
            DateNavigationBar(
                selectedDate = state.selectedDate,
                zoomLevel = state.zoomLevel,
                onPrevious = { controller.navigatePrevious() },
                onNext = { controller.navigateNext() },
                onToday = { controller.jumpToToday() }
            )

            // Active filters indicator
            if (state.filter.isActive) {
                ActiveFiltersChips(
                    filter = state.filter,
                    onClearAll = { controller.clearFilters() }
                )
            }

            // Content
            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                state.error != null -> {
                    ErrorView(
                        error = state.error!!,
                        onRetry = { controller.refresh() },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                state.items.isNotEmpty() -> {
                    EnhancedTimelineContent(
                        items = state.items,
                        zoomLevel = state.zoomLevel,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                else -> {
                    EmptyTimelineView(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }

    // Filter bottom sheet
    if (showFilterSheet) {
        TimelineFilterBottomSheet(
            currentFilter = state.filter,
            onFilterChanged = { controller.updateFilter(it) },
            onDismiss = { showFilterSheet = false }
        )
    }
}
