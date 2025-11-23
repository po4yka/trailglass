package com.po4yka.trailglass.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.filled.StickyNote2
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.po4yka.trailglass.data.db.Database
import com.po4yka.trailglass.data.paging.PlaceVisitPagingSource
import com.po4yka.trailglass.domain.model.PlaceCategory
import com.po4yka.trailglass.domain.model.PlaceVisit
import com.po4yka.trailglass.ui.components.ErrorView
import com.po4yka.trailglass.ui.theme.MotionConfig
import kotlin.time.Duration

/**
 * Enhanced timeline screen with Paging 3 integration for efficient scrolling.
 *
 * This screen uses Paging 3 to load place visits incrementally, providing better
 * performance for users with large location histories.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedTimelineScreenPaging(
    database: Database,
    userId: String,
    modifier: Modifier = Modifier,
    trackingController: com.po4yka.trailglass.feature.tracking.LocationTrackingController? = null,
    onAddPhoto: () -> Unit = {},
    onAddNote: () -> Unit = {},
    onCheckIn: () -> Unit = {}
) {
    // Create pager for place visits
    val pager =
        remember(userId) {
            Pager(
                config =
                    PagingConfig(
                        pageSize = 20,
                        enablePlaceholders = false,
                        prefetchDistance = 5
                    ),
                pagingSourceFactory = {
                    PlaceVisitPagingSource(
                        database = database.database,
                        userId = userId
                    )
                }
            )
        }

    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()

    // Tracking state
    val trackingState by (trackingController?.uiState?.collectAsState() ?: remember { mutableStateOf(null) })
    val isTracking = trackingState?.trackingState?.isTracking ?: false

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Timeline") },
                actions = {
                    IconButton(onClick = { lazyPagingItems.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
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
        TimelinePagingContent(
            lazyPagingItems = lazyPagingItems,
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        )
    }
}

@Composable
private fun TimelinePagingContent(
    lazyPagingItems: LazyPagingItems<PlaceVisit>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Handle different loading states
        when (val refreshState = lazyPagingItems.loadState.refresh) {
            is LoadState.Loading -> {
                item {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            is LoadState.Error -> {
                item {
                    ErrorView(
                        error = refreshState.error.message ?: "Unknown error",
                        onRetry = { lazyPagingItems.retry() },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            is LoadState.NotLoading -> {
                if (lazyPagingItems.itemCount == 0) {
                    item {
                        EmptyTimelineView(modifier = Modifier.fillParentMaxSize())
                    }
                } else {
                    // Display items with animated visibility
                    items(
                        count = lazyPagingItems.itemCount,
                        key = lazyPagingItems.itemKey { it.id }
                    ) { index ->
                        val item = lazyPagingItems[index]
                        if (item != null) {
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(animationSpec = MotionConfig.standardSpring()),
                                exit = fadeOut(animationSpec = MotionConfig.standardSpring())
                            ) {
                                EnhancedVisitCard(visit = item)
                            }
                        }
                    }

                    // Show loading indicator while loading more items
                    if (lazyPagingItems.loadState.append is LoadState.Loading) {
                        item {
                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }

                    // Show error while loading more items
                    if (lazyPagingItems.loadState.append is LoadState.Error) {
                        item {
                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                TextButton(onClick = { lazyPagingItems.retry() }) {
                                    Text("Retry loading more")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EnhancedVisitCard(
    visit: PlaceVisit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
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
                    modifier =
                        Modifier
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
                            Icons.AutoMirrored.Filled.StickyNote2,
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
                                visit.category.name
                                    .lowercase()
                                    .replaceFirstChar { it.uppercase() },
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
private fun EmptyTimelineView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.EventNote,
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

private fun getCategoryIcon(category: PlaceCategory): androidx.compose.ui.graphics.vector.ImageVector =
    when (category) {
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

private fun formatDuration(duration: Duration): String {
    val hours = duration.inWholeHours
    val minutes = duration.inWholeMinutes % 60

    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
        hours > 0 -> "${hours}h"
        else -> "${minutes}m"
    }
}
