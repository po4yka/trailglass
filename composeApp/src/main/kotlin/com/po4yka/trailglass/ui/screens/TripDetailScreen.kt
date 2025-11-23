package com.po4yka.trailglass.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.po4yka.trailglass.domain.model.Trip
import com.po4yka.trailglass.ui.components.FlexibleTopAppBarDefaults
import com.po4yka.trailglass.ui.components.LargeFlexibleTopAppBar
import com.po4yka.trailglass.ui.theme.BlueSlate
import com.po4yka.trailglass.ui.theme.CoastalPath
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/** Detailed view of a single trip with statistics, timeline, and actions. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailScreen(
    trip: Trip,
    onBack: () -> Unit = {},
    onViewRoute: () -> Unit = {},
    onEdit: () -> Unit = {},
    onShare: () -> Unit = {},
    onExport: (ExportFormat) -> Unit = {},
    onDelete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showExportMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Create scroll behavior for collapsing app bar
    val scrollBehavior = FlexibleTopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    // Format trip metadata for subtitle
    val startDate = trip.startTime.toLocalDateTime(TimeZone.currentSystemDefault())
    val metadataText =
        buildString {
            // Date range
            trip.endTime?.let { endTime ->
                val endDate = endTime.toLocalDateTime(TimeZone.currentSystemDefault())
                append("${startDate.date} to ${endDate.date}")
            } ?: append("Started ${startDate.date}")

            // Duration
            if (trip.duration != null) {
                append(" • ${formatDuration(trip.duration!!)}")
            }

            // Distance
            if (trip.totalDistanceMeters > 0) {
                append(" • ${(trip.totalDistanceMeters / 1000).toInt()} km")
            }

            // Places
            if (trip.visitedPlaceCount > 0) {
                append(" • ${trip.visitedPlaceCount} places")
            }
        }

    Scaffold(
        modifier =
            modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text(trip.displayName) },
                subtitle = { Text(metadataText) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onShare) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                    IconButton(onClick = { showExportMenu = true }) {
                        Icon(Icons.Default.Download, contentDescription = "Export")
                    }
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
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = FlexibleTopAppBarDefaults.silentWatersColors(),
                backgroundContent = {
                    // Hero gradient background using trip route color or default
                    Box(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors =
                                            if (trip.isOngoing) {
                                                listOf(CoastalPath, BlueSlate)
                                            } else {
                                                listOf(
                                                    MaterialTheme.colorScheme.primaryContainer,
                                                    MaterialTheme.colorScheme.surfaceVariant
                                                )
                                            }
                                    )
                                )
                    )
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // View Route button
            item {
                Button(
                    onClick = onViewRoute,
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                ) {
                    Icon(Icons.Default.Map, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("View Route on Map")
                }
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
                    colors =
                        ButtonDefaults.textButtonColors(
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
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (trip.isOngoing) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
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
                    val dateText =
                        trip.endTime?.let { endTime ->
                            val endDate = endTime.toLocalDateTime(TimeZone.currentSystemDefault())
                            "${startDate.date} to ${endDate.date}"
                        } ?: "Started ${startDate.date}"

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
                    text =
                        cities.take(10).joinToString(", ") +
                            if (cities.size > 10) " and ${cities.size - 10} more" else "",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

enum class ExportFormat {
    GPX,
    KML
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
