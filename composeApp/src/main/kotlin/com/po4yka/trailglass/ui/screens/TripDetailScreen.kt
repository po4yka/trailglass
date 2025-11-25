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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.po4yka.trailglass.domain.model.Trip
import com.po4yka.trailglass.ui.components.FlexibleTopAppBarDefaults
import com.po4yka.trailglass.ui.components.LargeFlexibleTopAppBar
import com.po4yka.trailglass.ui.screens.tripdetail.ExportFormat
import com.po4yka.trailglass.ui.screens.tripdetail.TripStatisticsCard
import com.po4yka.trailglass.ui.screens.tripdetail.VisitedPlacesCard
import com.po4yka.trailglass.ui.screens.tripdetail.formatDuration
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
                                Icon(Icons.Default.Map, contentDescription = "Export as GPX")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Export as KML") },
                            onClick = {
                                showExportMenu = false
                                onExport(ExportFormat.KML)
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Public, contentDescription = "Export as KML")
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
                    Icon(Icons.Default.Map, contentDescription = "View route on map")
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
                                                contentDescription = "Tag",
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
