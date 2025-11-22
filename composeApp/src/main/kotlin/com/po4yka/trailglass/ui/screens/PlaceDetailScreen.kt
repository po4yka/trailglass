package com.po4yka.trailglass.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Church
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Theaters
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.po4yka.trailglass.domain.model.FrequentPlace
import com.po4yka.trailglass.domain.model.PlaceCategory
import com.po4yka.trailglass.domain.model.PlaceSignificance
import com.po4yka.trailglass.ui.components.MediumFlexibleTopAppBar
import com.po4yka.trailglass.ui.components.FlexibleTopAppBarDefaults
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration

/**
 * Screen showing detailed information about a frequent place.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceDetailScreen(
    place: FrequentPlace?,
    onToggleFavorite: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Create scroll behavior for collapsing app bar
    val scrollBehavior = FlexibleTopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    // Format place metadata for subtitle
    val metadataText = if (place != null) {
        buildString {
            append("${place.visitCount} visits")

            if (place.totalDuration > Duration.ZERO) {
                append(" • ${formatDuration(place.totalDuration)} total")
            }

            place.lastVisitTime?.let { lastVisit ->
                val now = kotlinx.datetime.Clock.System.now()
                val daysSince = (now - lastVisit).inWholeDays
                when {
                    daysSince == 0L -> append(" • Last visit today")
                    daysSince == 1L -> append(" • Last visit yesterday")
                    daysSince < 7 -> append(" • Last visit $daysSince days ago")
                    else -> append(" • Last visit ${formatDate(lastVisit)}")
                }
            }
        }
    } else {
        ""
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            if (place != null) {
                MediumFlexibleTopAppBar(
                    title = { Text(place.displayName) },
                    subtitle = { Text(metadataText) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    leadingIcon = {
                        Icon(
                            getCategoryIcon(place.category),
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    actions = {
                        IconButton(onClick = onToggleFavorite) {
                            Icon(
                                if (place.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = if (place.isFavorite) "Remove from favorites" else "Add to favorites",
                                tint = if (place.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior,
                    colors = when (place.significance) {
                        PlaceSignificance.PRIMARY -> FlexibleTopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            scrolledContainerColor = MaterialTheme.colorScheme.surface
                        )
                        PlaceSignificance.FREQUENT -> FlexibleTopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            scrolledContainerColor = MaterialTheme.colorScheme.surface
                        )
                        else -> FlexibleTopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            scrolledContainerColor = MaterialTheme.colorScheme.surface
                        )
                    }
                )
            } else {
                TopAppBar(
                    title = { Text("Place Details") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        if (place == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Place not found")
            }
        } else {
            PlaceDetailContent(
                place = place,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        }
    }
}

@Composable
private fun PlaceDetailContent(
    place: FrequentPlace,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Statistics Section
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Statistics",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                StatRow(
                    icon = Icons.Default.Event,
                    label = "Total Visits",
                    value = place.visitCount.toString()
                )

                if (place.totalDuration > Duration.ZERO) {
                    StatRow(
                        icon = Icons.Default.Schedule,
                        label = "Total Time",
                        value = formatDuration(place.totalDuration)
                    )
                }

                if (place.averageDuration > Duration.ZERO) {
                    StatRow(
                        icon = Icons.Default.Timelapse,
                        label = "Average Duration",
                        value = formatDuration(place.averageDuration)
                    )
                }

                place.firstVisitTime?.let { firstVisit ->
                    StatRow(
                        icon = Icons.Default.CalendarToday,
                        label = "First Visit",
                        value = formatDate(firstVisit)
                    )
                }

                place.lastVisitTime?.let { lastVisit ->
                    StatRow(
                        icon = Icons.Default.Update,
                        label = "Last Visit",
                        value = formatDate(lastVisit)
                    )
                }
            }
        }
        }

        // Location Section
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Location",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                place.address?.let { address ->
                    StatRow(
                        icon = Icons.Default.Place,
                        label = "Address",
                        value = address
                    )
                }

                StatRow(
                    icon = Icons.Default.MyLocation,
                    label = "Coordinates",
                    value = "${place.centerLatitude.format(6)}, ${place.centerLongitude.format(6)}"
                )

                StatRow(
                    icon = Icons.Default.RadioButtonChecked,
                    label = "Radius",
                    value = "${place.radiusMeters.toInt()} meters"
                )
            }
        }
        }

        // Notes Section (if available)
        if (place.userNotes != null || place.userLabel != null) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Custom Info",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    place.userLabel?.let { label ->
                        StatRow(
                            icon = Icons.AutoMirrored.Filled.Label,
                            label = "Custom Label",
                            value = label
                        )
                    }

                    place.userNotes?.let { notes ->
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Notes,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Notes",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = notes,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 28.dp)
                            )
                        }
                    }
                }
            }
        }
        }
    }
}

@Composable
private fun StatRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
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

private fun formatDuration(duration: Duration): String {
    val hours = duration.inWholeHours
    val minutes = duration.inWholeMinutes % 60

    return when {
        hours > 24 -> {
            val days = hours / 24
            val remainingHours = hours % 24
            if (remainingHours > 0) "$days days, ${remainingHours}h" else "$days days"
        }
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
        hours > 0 -> "${hours}h"
        else -> "${minutes}m"
    }
}

private fun formatDate(instant: Instant): String {
    val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${dateTime.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${dateTime.dayOfMonth}, ${dateTime.year}"
}

private fun Double.format(decimals: Int) = "%.${decimals}f".format(this)
