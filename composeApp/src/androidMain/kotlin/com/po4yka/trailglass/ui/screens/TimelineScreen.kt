package com.po4yka.trailglass.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.po4yka.trailglass.feature.timeline.GetTimelineForDayUseCase
import com.po4yka.trailglass.feature.timeline.TimelineController
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Timeline screen showing daily timeline of visits and routes.
 */
@Composable
fun TimelineScreen(
    controller: TimelineController,
    modifier: Modifier = Modifier
) {
    val state by controller.state.collectAsState()

    // Load today on first composition
    LaunchedEffect(Unit) {
        val today = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
        controller.loadDay(today)
    }

    Column(modifier = modifier.fillMaxSize()) {
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
                TimelineContent(
                    items = state.items,
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                EmptyTimelineView(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
private fun TimelineContent(
    items: List<GetTimelineForDayUseCase.TimelineItemUI>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items) { item ->
            when (item) {
                is GetTimelineForDayUseCase.TimelineItemUI.DayStartUI -> {
                    DayMarkerCard(text = "Day Start", icon = Icons.Default.WbSunny)
                }
                is GetTimelineForDayUseCase.TimelineItemUI.DayEndUI -> {
                    DayMarkerCard(text = "Day End", icon = Icons.Default.NightsStay)
                }
                is GetTimelineForDayUseCase.TimelineItemUI.VisitUI -> {
                    VisitCard(visit = item.placeVisit)
                }
                is GetTimelineForDayUseCase.TimelineItemUI.RouteUI -> {
                    RouteCard(route = item.routeSegment)
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
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = null)
            Text(text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun VisitCard(
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    getCategoryIcon(visit.category),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = visit.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    if (visit.city != null) {
                        Text(
                            text = visit.city!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
                if (visit.isFavorite) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Favorite",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (visit.approximateAddress != null && visit.userLabel == null && visit.poiName == null) {
                Text(
                    text = visit.approximateAddress!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Duration chip
            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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

                // Category chip
                if (visit.category != com.po4yka.trailglass.domain.model.PlaceCategory.OTHER) {
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
private fun getCategoryIcon(category: com.po4yka.trailglass.domain.model.PlaceCategory): androidx.compose.ui.graphics.vector.ImageVector {
    return when (category) {
        com.po4yka.trailglass.domain.model.PlaceCategory.HOME -> Icons.Default.Home
        com.po4yka.trailglass.domain.model.PlaceCategory.WORK -> Icons.Default.Work
        com.po4yka.trailglass.domain.model.PlaceCategory.FOOD -> Icons.Default.Restaurant
        com.po4yka.trailglass.domain.model.PlaceCategory.SHOPPING -> Icons.Default.ShoppingBag
        com.po4yka.trailglass.domain.model.PlaceCategory.FITNESS -> Icons.Default.FitnessCenter
        com.po4yka.trailglass.domain.model.PlaceCategory.ENTERTAINMENT -> Icons.Default.Theaters
        com.po4yka.trailglass.domain.model.PlaceCategory.TRAVEL -> Icons.Default.Flight
        com.po4yka.trailglass.domain.model.PlaceCategory.HEALTHCARE -> Icons.Default.LocalHospital
        com.po4yka.trailglass.domain.model.PlaceCategory.EDUCATION -> Icons.Default.School
        com.po4yka.trailglass.domain.model.PlaceCategory.RELIGIOUS -> Icons.Default.Church
        com.po4yka.trailglass.domain.model.PlaceCategory.SOCIAL -> Icons.Default.People
        com.po4yka.trailglass.domain.model.PlaceCategory.OUTDOOR -> Icons.Default.Park
        com.po4yka.trailglass.domain.model.PlaceCategory.SERVICE -> Icons.Default.Build
        com.po4yka.trailglass.domain.model.PlaceCategory.OTHER -> Icons.Default.Place
    }
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
private fun RouteCard(
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
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                getTransportIcon(route.transportType),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Column {
                Text(
                    text = route.transportType.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )

                Text(
                    text = "${(route.distanceMeters / 1000).toInt()} km",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

private fun getTransportIcon(transportType: com.po4yka.trailglass.domain.model.TransportType) =
    when (transportType) {
        com.po4yka.trailglass.domain.model.TransportType.WALK -> Icons.Default.DirectionsWalk
        com.po4yka.trailglass.domain.model.TransportType.BIKE -> Icons.Default.DirectionsBike
        com.po4yka.trailglass.domain.model.TransportType.CAR -> Icons.Default.DirectionsCar
        com.po4yka.trailglass.domain.model.TransportType.TRAIN -> Icons.Default.Train
        com.po4yka.trailglass.domain.model.TransportType.PLANE -> Icons.Default.Flight
        com.po4yka.trailglass.domain.model.TransportType.BOAT -> Icons.Default.DirectionsBoat
        com.po4yka.trailglass.domain.model.TransportType.UNKNOWN -> Icons.Default.HelpOutline
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
            text = "No timeline data for this day",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
