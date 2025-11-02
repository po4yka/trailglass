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
import com.po4yka.trailglass.domain.model.Trip
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.hours

/**
 * Detailed view of a single trip with statistics, timeline, and actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailScreen(
    trip: Trip,
    onBack: () -> Unit = {},
    onEdit: () -> Unit = {},
    onShare: () -> Unit = {},
    onExport: (ExportFormat) -> Unit = {},
    onDelete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showExportMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(trip.displayName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onShare) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                    IconButton(onClick = { showExportMenu = true }) {
                        Icon(Icons.Default.Download, contentDescription = "Export")
                    }
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }

                    // Export menu
                    DropdownMenu(
                        expanded = showExportMenu,
                        onDismissRequest = { showExportMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Export as GPX") },
                            onClick = {
                                showExportMenu = false
                                onExport(ExportFormat.GPX)
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Map, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Export as KML") },
                            onClick = {
                                showExportMenu = false
                                onExport(ExportFormat.KML)
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Public, contentDescription = null)
                            }
                        )
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Trip header card
            item {
                TripHeaderCard(trip)
            }

            // Statistics card
            item {
                TripStatisticsCard(trip)
            }

            // Description card
            if (trip.description != null) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Description",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = trip.description!!,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            // Tags
            if (trip.tags.isNotEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Tags",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                trip.tags.forEach { tag ->
                                    AssistChip(
                                        onClick = { },
                                        label = { Text(tag) },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Tag,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Visited countries and cities
            if (trip.countriesVisited.isNotEmpty() || trip.citiesVisited.isNotEmpty()) {
                item {
                    VisitedPlacesCard(
                        countries = trip.countriesVisited,
                        cities = trip.citiesVisited
                    )
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Trip?") },
            text = { Text("This action cannot be undone. All trip data will be permanently deleted.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun TripHeaderCard(trip: Trip) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (trip.isOngoing)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = trip.displayName,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Date range
                    val startDate = trip.startTime.toLocalDateTime(TimeZone.currentSystemDefault())
                    val dateText = if (trip.endTime != null) {
                        val endDate = trip.endTime.toLocalDateTime(TimeZone.currentSystemDefault())
                        "${startDate.date} to ${endDate.date}"
                    } else {
                        "Started ${startDate.date}"
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = dateText,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    // Duration
                    if (trip.duration != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = formatDuration(trip.duration!!),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                // Badges
                Column(horizontalAlignment = Alignment.End) {
                    if (trip.isOngoing) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.primary
                        ) {
                            Text("Ongoing")
                        }
                    }
                    if (trip.isAutoDetected) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Badge(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        ) {
                            Text("Auto-detected")
                        }
                    }
                    if (trip.isPublic) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Badge {
                            Text("Public")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TripStatisticsCard(trip: Trip) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Statistics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Statistics grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                if (trip.totalDistanceMeters > 0) {
                    StatItem(
                        icon = Icons.Default.Straighten,
                        label = "Distance",
                        value = "${(trip.totalDistanceMeters / 1000).toInt()} km"
                    )
                }
                if (trip.visitedPlaceCount > 0) {
                    StatItem(
                        icon = Icons.Default.Place,
                        label = "Places",
                        value = "${trip.visitedPlaceCount}"
                    )
                }
                if (trip.countriesVisited.isNotEmpty()) {
                    StatItem(
                        icon = Icons.Default.Public,
                        label = "Countries",
                        value = "${trip.countriesVisited.size}"
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(
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
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun VisitedPlacesCard(
    countries: List<String>,
    cities: List<String>
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (countries.isNotEmpty()) {
                Text(
                    text = "Countries Visited",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = countries.joinToString(", "),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (cities.isNotEmpty()) {
                if (countries.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                }
                Text(
                    text = "Cities Visited",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = cities.take(10).joinToString(", ") +
                            if (cities.size > 10) " and ${cities.size - 10} more" else "",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

enum class ExportFormat {
    GPX, KML
}

private fun formatDuration(duration: kotlin.time.Duration): String {
    val days = duration.inWholeDays
    val hours = duration.inWholeHours % 24
    val minutes = duration.inWholeMinutes % 60

    return when {
        days > 0 && hours > 0 -> "$days days, $hours hours"
        days > 0 -> "$days days"
        hours > 0 && minutes > 0 -> "$hours hours, $minutes min"
        hours > 0 -> "$hours hours"
        else -> "$minutes min"
    }
}
