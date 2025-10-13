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
                    Icons.Default.Place,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = visit.city ?: "Unknown location",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            if (visit.approximateAddress != null) {
                Text(
                    text = visit.approximateAddress!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (visit.poiName != null) {
                Text(
                    text = visit.poiName!!,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
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
