package com.po4yka.trailglass.ui.screens.timeline

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Church
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Theaters
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.po4yka.trailglass.domain.model.PlaceCategory
import com.po4yka.trailglass.domain.model.TransportType
import com.po4yka.trailglass.feature.timeline.TimelineFilter
import com.po4yka.trailglass.feature.timeline.TimelineZoomLevel
import kotlinx.datetime.LocalDate

@Composable
fun ZoomLevelSelector(
    currentZoom: TimelineZoomLevel,
    onZoomChanged: (TimelineZoomLevel) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier =
                Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TimelineZoomLevel.entries.forEach { zoom ->
                FilterChip(
                    selected = currentZoom == zoom,
                    onClick = { onZoomChanged(zoom) },
                    label = { Text(zoom.displayName) },
                    leadingIcon =
                        if (currentZoom == zoom) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        } else {
                            null
                        }
                )
            }
        }
    }
}

@Composable
fun DateNavigationBar(
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
            modifier =
                Modifier
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

@Composable
fun ActiveFiltersChips(
    filter: TimelineFilter,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier =
                Modifier
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
fun TimelineSearchBar(
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
        colors =
            TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
    )
}

@Composable
fun FilterSection(
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
fun TransportTypeFilterChips(
    selectedTypes: Set<TransportType>,
    onTypeToggled: (TransportType) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(TransportType.entries) { type ->
            FilterChip(
                selected = type in selectedTypes,
                onClick = { onTypeToggled(type) },
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

@Composable
fun PlaceCategoryFilterChips(
    selectedCategories: Set<PlaceCategory>,
    onCategoryToggled: (PlaceCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(PlaceCategory.entries.filter { it != PlaceCategory.OTHER }) { category ->
            FilterChip(
                selected = category in selectedCategories,
                onClick = { onCategoryToggled(category) },
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

@Composable
fun FavoritesFilterSwitch(
    showOnlyFavorites: Boolean,
    onToggled: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
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
            checked = showOnlyFavorites,
            onCheckedChange = onToggled
        )
    }
}

@Composable
fun EmptyTimelineView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
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

fun getCategoryIcon(category: PlaceCategory): androidx.compose.ui.graphics.vector.ImageVector =
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

fun getTransportIcon(transportType: TransportType) =
    when (transportType) {
        TransportType.WALK -> Icons.AutoMirrored.Filled.DirectionsWalk
        TransportType.BIKE -> Icons.AutoMirrored.Filled.DirectionsBike
        TransportType.CAR -> Icons.Default.DirectionsCar
        TransportType.TRAIN -> Icons.Default.Train
        TransportType.PLANE -> Icons.Default.Flight
        TransportType.BOAT -> Icons.Default.DirectionsBoat
        TransportType.UNKNOWN -> Icons.AutoMirrored.Filled.HelpOutline
    }

fun formatDuration(duration: kotlin.time.Duration): String {
    val hours = duration.inWholeHours
    val minutes = duration.inWholeMinutes % 60

    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
        hours > 0 -> "${hours}h"
        else -> "${minutes}m"
    }
}

fun formatDateForZoom(
    date: LocalDate,
    zoom: TimelineZoomLevel
): String =
    when (zoom) {
        TimelineZoomLevel.DAY -> date.toString()
        TimelineZoomLevel.WEEK -> "Week of $date"
        TimelineZoomLevel.MONTH -> "${date.month.name} ${date.year}"
        TimelineZoomLevel.YEAR -> "${date.year}"
    }
