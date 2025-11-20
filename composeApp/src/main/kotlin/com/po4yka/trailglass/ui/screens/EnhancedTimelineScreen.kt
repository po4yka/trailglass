package com.po4yka.trailglass.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Church
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StickyNote2
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Theaters
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.po4yka.trailglass.domain.model.PlaceCategory
import com.po4yka.trailglass.domain.model.TransportType
import com.po4yka.trailglass.feature.timeline.EnhancedTimelineController
import com.po4yka.trailglass.feature.timeline.GetTimelineUseCase
import com.po4yka.trailglass.feature.timeline.TimelineFilter
import com.po4yka.trailglass.feature.timeline.TimelineZoomLevel
import com.po4yka.trailglass.ui.components.ErrorView
import kotlinx.datetime.LocalDate

/**
 * Enhanced timeline screen with zoom levels, filtering, and search.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedTimelineScreen(
    controller: EnhancedTimelineController,
    modifier: Modifier = Modifier
) {
    val state by controller.state.collectAsState()
    var showFilterSheet by remember { mutableStateOf(false) }
    var showSearchBar by remember { mutableStateOf(false) }

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
                                containerColor = if (state.filter.isActive)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.surfaceVariant
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

@Composable
private fun ZoomLevelSelector(
    currentZoom: TimelineZoomLevel,
    onZoomChanged: (TimelineZoomLevel) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TimelineZoomLevel.entries.forEach { zoom ->
                FilterChip(
                    selected = currentZoom == zoom,
                    onClick = { onZoomChanged(zoom) },
                    label = { Text(zoom.displayName) },
                    leadingIcon = if (currentZoom == zoom) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    } else null
                )
            }
        }
    }
}

@Composable
private fun DateNavigationBar(
    selectedDate: LocalDate,
    zoomLevel: TimelineZoomLevel,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrevious) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous")
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = formatDateForZoom(selectedDate, zoomLevel),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onToday) {
                    Icon(
                        Icons.Default.Today,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Today", style = MaterialTheme.typography.labelMedium)
                }
            }

            IconButton(onClick = onNext) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Next")
            }
        }
    }
}

private fun formatDateForZoom(date: LocalDate, zoom: TimelineZoomLevel): String {
    return when (zoom) {
        TimelineZoomLevel.DAY -> date.toString()
        TimelineZoomLevel.WEEK -> "Week of $date"
        TimelineZoomLevel.MONTH -> "${date.month.name} ${date.year}"
        TimelineZoomLevel.YEAR -> "${date.year}"
    }
}

@Composable
private fun ActiveFiltersChips(
    filter: TimelineFilter,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Filters:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                "${filter.activeFilterCount} active",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.weight(1f))

            TextButton(onClick = onClearAll) {
                Text("Clear All")
            }
        }
    }
}

@Composable
private fun TimelineSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text("Search timeline...") },
        trailingIcon = {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close search")
            }
        },
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimelineFilterBottomSheet(
    currentFilter: TimelineFilter,
    onFilterChanged: (TimelineFilter) -> Unit,
    onDismiss: () -> Unit
) {
    var localFilter by remember { mutableStateOf(currentFilter) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxHeight(0.8f)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
        ) {
            Text(
                "Filter Timeline",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Transport types
                item {
                    FilterSection(title = "Transport Types") {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(TransportType.entries) { type ->
                                FilterChip(
                                    selected = type in localFilter.transportTypes,
                                    onClick = {
                                        localFilter = if (type in localFilter.transportTypes) {
                                            localFilter.copy(transportTypes = localFilter.transportTypes - type)
                                        } else {
                                            localFilter.copy(transportTypes = localFilter.transportTypes + type)
                                        }
                                    },
                                    label = { Text(type.name) },
                                    leadingIcon = {
                                        Icon(
                                            getTransportIcon(type),
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                // Place categories
                item {
                    FilterSection(title = "Place Categories") {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(PlaceCategory.entries.filter { it != PlaceCategory.OTHER }) { category ->
                                FilterChip(
                                    selected = category in localFilter.placeCategories,
                                    onClick = {
                                        localFilter = if (category in localFilter.placeCategories) {
                                            localFilter.copy(placeCategories = localFilter.placeCategories - category)
                                        } else {
                                            localFilter.copy(placeCategories = localFilter.placeCategories + category)
                                        }
                                    },
                                    label = { Text(category.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                    leadingIcon = {
                                        Icon(
                                            getCategoryIcon(category),
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                // Favorites only
                item {
                    FilterSection(title = "Options") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Star, contentDescription = null)
                                Text("Show only favorites")
                            }
                            Switch(
                                checked = localFilter.showOnlyFavorites,
                                onCheckedChange = {
                                    localFilter = localFilter.copy(showOnlyFavorites = it)
                                }
                            )
                        }
                    }
                }

                // Action buttons
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                localFilter = TimelineFilter()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Reset")
                        }
                        Button(
                            onClick = {
                                onFilterChanged(localFilter)
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Apply")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        content()
    }
}

@Composable
private fun EnhancedTimelineContent(
    items: List<GetTimelineUseCase.TimelineItemUI>,
    zoomLevel: TimelineZoomLevel,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items) { item ->
            when (item) {
                is GetTimelineUseCase.TimelineItemUI.DayStartUI -> {
                    DayMarkerCard(text = "Day Start", icon = Icons.Default.WbSunny)
                }
                is GetTimelineUseCase.TimelineItemUI.DayEndUI -> {
                    DayMarkerCard(text = "Day End", icon = Icons.Default.NightsStay)
                }
                is GetTimelineUseCase.TimelineItemUI.VisitUI -> {
                    EnhancedVisitCard(visit = item.placeVisit)
                }
                is GetTimelineUseCase.TimelineItemUI.RouteUI -> {
                    EnhancedRouteCard(route = item.routeSegment)
                }
                is GetTimelineUseCase.TimelineItemUI.DaySummaryUI -> {
                    DaySummaryCard(summary = item)
                }
                is GetTimelineUseCase.TimelineItemUI.WeekSummaryUI -> {
                    WeekSummaryCard(summary = item)
                }
                is GetTimelineUseCase.TimelineItemUI.MonthSummaryUI -> {
                    MonthSummaryCard(summary = item)
                }
            }
        }
    }
}

@Composable
private fun DayMarkerCard(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(
                text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun EnhancedVisitCard(
    visit: com.po4yka.trailglass.domain.model.PlaceVisit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    getCategoryIcon(visit.category),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(28.dp)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = visit.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        if (visit.isFavorite) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = "Favorite",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    if (visit.city != null || visit.countryCode != null) {
                        Text(
                            text = listOfNotNull(visit.city, visit.countryCode).joinToString(", "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Address if no POI name
            if (visit.approximateAddress != null && visit.userLabel == null && visit.poiName == null) {
                Text(
                    text = visit.approximateAddress!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // User notes
            if (visit.userNotes != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.StickyNote2,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = visit.userNotes!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Metadata chips
            Row(
                modifier = Modifier.padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Duration
                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            formatDuration(visit.duration),
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )

                // Category
                if (visit.category != PlaceCategory.OTHER) {
                    AssistChip(
                        onClick = { },
                        label = {
                            Text(
                                visit.category.name.lowercase().replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun EnhancedRouteCard(
    route: com.po4yka.trailglass.domain.model.RouteSegment,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                getTransportIcon(route.transportType),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(28.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = route.transportType.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "${(route.distanceMeters / 1000).toInt()} km",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )

                    val duration = route.endTime - route.startTime
                    if (duration.inWholeMinutes > 0) {
                        Text(
                            text = "• ${formatDuration(duration)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            // Confidence indicator
            if (route.confidence < 0.7f) {
                Icon(
                    Icons.Default.HelpOutline,
                    contentDescription = "Low confidence",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun DaySummaryCard(
    summary: GetTimelineUseCase.TimelineItemUI.DaySummaryUI,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    text = summary.date.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryStatItem(
                    icon = Icons.Default.Place,
                    label = "Places",
                    value = "${summary.totalVisits}"
                )
                SummaryStatItem(
                    icon = Icons.Default.Straighten,
                    label = "Distance",
                    value = "${(summary.totalDistanceMeters / 1000).toInt()} km"
                )
                SummaryStatItem(
                    icon = Icons.Default.DirectionsWalk,
                    label = "Routes",
                    value = "${summary.totalRoutes}"
                )
            }
        }
    }
}

@Composable
private fun WeekSummaryCard(
    summary: GetTimelineUseCase.TimelineItemUI.WeekSummaryUI,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Column {
                    Text(
                        text = "Week Summary",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        text = "${summary.weekStart} - ${summary.weekEnd}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryStatItem(
                    icon = Icons.Default.Place,
                    label = "Places",
                    value = "${summary.totalVisits}"
                )
                SummaryStatItem(
                    icon = Icons.Default.Straighten,
                    label = "Distance",
                    value = "${(summary.totalDistanceMeters / 1000).toInt()} km"
                )
                SummaryStatItem(
                    icon = Icons.Default.CalendarToday,
                    label = "Days Active",
                    value = "${summary.activeDays}"
                )
            }
        }
    }
}

@Composable
private fun MonthSummaryCard(
    summary: GetTimelineUseCase.TimelineItemUI.MonthSummaryUI,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    text = "${summary.month.name} ${summary.year}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryStatItem(
                    icon = Icons.Default.Place,
                    label = "Places",
                    value = "${summary.totalVisits}"
                )
                SummaryStatItem(
                    icon = Icons.Default.Straighten,
                    label = "Distance",
                    value = "${(summary.totalDistanceMeters / 1000).toInt()} km"
                )
                SummaryStatItem(
                    icon = Icons.Default.DateRange,
                    label = "Weeks",
                    value = "${summary.activeWeeks}"
                )
            }

            if (summary.topCategories.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                TopCategoriesSection(categories = summary.topCategories)
            }
        }
    }
}

@Composable
private fun TopCategoriesSection(
    categories: List<PlaceCategory>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            "Top Categories",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onTertiaryContainer
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            categories.take(3).forEach { category ->
                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            category.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    leadingIcon = {
                        Icon(
                            getCategoryIcon(category),
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun SummaryStatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun getCategoryIcon(category: PlaceCategory): androidx.compose.ui.graphics.vector.ImageVector {
    return when (category) {
        PlaceCategory.HOME -> Icons.Default.Home
        PlaceCategory.WORK -> Icons.Default.Work
        PlaceCategory.FOOD -> Icons.Default.Restaurant
        PlaceCategory.SHOPPING -> Icons.Default.ShoppingBag
        PlaceCategory.FITNESS -> Icons.Default.FitnessCenter
        PlaceCategory.ENTERTAINMENT -> Icons.Default.Theaters
        PlaceCategory.TRAVEL -> Icons.Default.Flight
        PlaceCategory.HEALTHCARE -> Icons.Default.LocalHospital
        PlaceCategory.EDUCATION -> Icons.Default.School
        PlaceCategory.RELIGIOUS -> Icons.Default.Church
        PlaceCategory.SOCIAL -> Icons.Default.People
        PlaceCategory.OUTDOOR -> Icons.Default.Park
        PlaceCategory.SERVICE -> Icons.Default.Build
        PlaceCategory.OTHER -> Icons.Default.Place
    }
}

private fun getTransportIcon(transportType: TransportType) =
    when (transportType) {
        TransportType.WALK -> Icons.Default.DirectionsWalk
        TransportType.BIKE -> Icons.Default.DirectionsBike
        TransportType.CAR -> Icons.Default.DirectionsCar
        TransportType.TRAIN -> Icons.Default.Train
        TransportType.PLANE -> Icons.Default.Flight
        TransportType.BOAT -> Icons.Default.DirectionsBoat
        TransportType.UNKNOWN -> Icons.Default.HelpOutline
    }

private fun formatDuration(duration: kotlin.time.Duration): String {
    val hours = duration.inWholeHours
    val minutes = duration.inWholeMinutes % 60

    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
        hours > 0 -> "${hours}h"
        else -> "${minutes}m"
    }
}

@Composable
private fun EmptyTimelineView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.EventNote,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No timeline data",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = "Enable location tracking to see your timeline",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
