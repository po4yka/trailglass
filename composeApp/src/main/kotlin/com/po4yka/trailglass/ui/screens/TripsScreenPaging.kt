package com.po4yka.trailglass.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tag
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
import com.po4yka.trailglass.data.db.Database
import com.po4yka.trailglass.data.paging.TripPagingSource
import com.po4yka.trailglass.domain.model.Trip
import com.po4yka.trailglass.ui.components.ErrorView
import com.po4yka.trailglass.ui.theme.MorphableShapes
import com.po4yka.trailglass.ui.theme.MotionConfig
import com.po4yka.trailglass.ui.theme.animateShapeMorph
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Trips screen with Paging 3 integration for efficient scrolling.
 *
 * This screen uses Paging 3 to load trips incrementally, providing better
 * performance for users with large trip histories.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripsScreenPaging(
    database: Database,
    userId: String,
    onTripClick: (Trip) -> Unit = {},
    onCreateTrip: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Create pager for trips
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
                    TripPagingSource(
                        database = database.database,
                        userId = userId
                    )
                }
            )
        }

    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateTrip,
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Trip")
            }
        }
    ) { paddingValues ->
        TripsPagingContent(
            lazyPagingItems = lazyPagingItems,
            onTripClick = onTripClick,
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        )
    }
}

@Composable
private fun TripsPagingContent(
    lazyPagingItems: LazyPagingItems<Trip>,
    onTripClick: (Trip) -> Unit,
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
                        EmptyTripsView(modifier = Modifier.fillParentMaxSize())
                    }
                } else {
                    // Header
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Trips",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${lazyPagingItems.itemCount} total",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { lazyPagingItems.refresh() }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                            }
                        }
                    }

                    // Group trips: ongoing first, then by date
                    val ongoingTrips =
                        (0 until lazyPagingItems.itemCount)
                            .mapNotNull { lazyPagingItems[it] }
                            .filter { it.isOngoing }

                    val completedTrips =
                        (0 until lazyPagingItems.itemCount)
                            .mapNotNull { lazyPagingItems[it] }
                            .filter { !it.isOngoing }

                    // Ongoing trips section
                    if (ongoingTrips.isNotEmpty()) {
                        item {
                            SectionHeader("Ongoing")
                        }
                        items(
                            count = ongoingTrips.size,
                            key = { ongoingTrips[it].id }
                        ) { index ->
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(animationSpec = MotionConfig.standardSpring()),
                                exit = fadeOut(animationSpec = MotionConfig.standardSpring())
                            ) {
                                TripCard(
                                    trip = ongoingTrips[index],
                                    onClick = { onTripClick(ongoingTrips[index]) }
                                )
                            }
                        }
                    }

                    // Completed trips section
                    if (completedTrips.isNotEmpty()) {
                        item {
                            SectionHeader(if (ongoingTrips.isNotEmpty()) "Past Trips" else "All Trips")
                        }
                        items(
                            count = completedTrips.size,
                            key = { completedTrips[it].id }
                        ) { index ->
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(animationSpec = MotionConfig.standardSpring()),
                                exit = fadeOut(animationSpec = MotionConfig.standardSpring())
                            ) {
                                TripCard(
                                    trip = completedTrips[index],
                                    onClick = { onTripClick(completedTrips[index]) }
                                )
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
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun TripCard(
    trip: Trip,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Track press state for shape morphing
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Animate card shape: slight morph when pressed for tactile feedback
    // Ongoing trips get a more dynamic shape
    val cardShape by animateShapeMorph(
        targetShape =
            when {
                isPressed -> MorphableShapes.RoundedSquare
                trip.isOngoing -> MorphableShapes.Wave
                else -> MorphableShapes.Circle
            },
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
    )

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (trip.isOngoing) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
            ),
        shape = cardShape,
        interactionSource = interactionSource
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = trip.displayName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (trip.isOngoing) {
                            Badge {
                                Text("Ongoing")
                            }
                        }
                    }

                    // Date range
                    val startDate = trip.startTime.toLocalDateTime(TimeZone.currentSystemDefault())
                    val dateText =
                        trip.endTime?.let { endTime ->
                            val endDate = endTime.toLocalDateTime(TimeZone.currentSystemDefault())
                            "${startDate.date} - ${endDate.date}"
                        } ?: "Started ${startDate.date}"

                    Text(
                        text = dateText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Auto-detected badge
                if (trip.isAutoDetected) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.tertiaryContainer
                    ) {
                        Text(
                            text = "Auto",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Description
            if (trip.description != null) {
                Text(
                    text = trip.description!!,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp),
                    maxLines = 2
                )
            }

            // Statistics
            if (trip.summary.isNotEmpty()) {
                Text(
                    text = trip.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Tags
            if (trip.tags.isNotEmpty()) {
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    trip.tags.take(3).forEach { tag ->
                        AssistChip(
                            onClick = { },
                            label = {
                                Text(
                                    text = tag,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Tag,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        )
                    }
                    if (trip.tags.size > 3) {
                        Text(
                            text = "+${trip.tags.size - 3}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyTripsView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.Luggage,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "No Trips Yet",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Start tracking your adventures",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
