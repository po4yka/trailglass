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
import com.po4yka.trailglass.data.remote.TrailGlassApiClient
import com.po4yka.trailglass.domain.model.PlaceVisit
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Place visit detail screen showing comprehensive information about a single visit.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceVisitDetailScreen(
    placeVisitId: String,
    apiClient: TrailGlassApiClient,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var placeVisit by remember { mutableStateOf<PlaceVisit?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    // Load place visit data
    LaunchedEffect(placeVisitId) {
        scope.launch {
            isLoading = true
            error = null
            val result = apiClient.getPlaceVisit(placeVisitId)
            result.onSuccess { dto ->
                // Convert DTO to domain model (simplified - you may need proper mapping)
                placeVisit = PlaceVisit(
                    id = dto.id,
                    startTime = dto.startTime,
                    endTime = dto.endTime,
                    centerLatitude = dto.centerLatitude,
                    centerLongitude = dto.centerLongitude,
                    approximateAddress = dto.approximateAddress,
                    poiName = dto.poiName,
                    city = dto.city,
                    countryCode = dto.countryCode,
                    userId = dto.userId
                )
                isLoading = false
            }.onFailure { e ->
                error = e.message ?: "Failed to load place visit"
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(placeVisit?.displayName ?: "Place Visit") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Share */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                error != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = error ?: "Unknown error",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = {
                            scope.launch {
                                isLoading = true
                                error = null
                                val result = apiClient.getPlaceVisit(placeVisitId)
                                result.onSuccess { dto ->
                                    placeVisit = PlaceVisit(
                                        id = dto.id,
                                        startTime = dto.startTime,
                                        endTime = dto.endTime,
                                        centerLatitude = dto.centerLatitude,
                                        centerLongitude = dto.centerLongitude,
                                        approximateAddress = dto.approximateAddress,
                                        poiName = dto.poiName,
                                        city = dto.city,
                                        countryCode = dto.countryCode,
                                        userId = dto.userId
                                    )
                                    isLoading = false
                                }.onFailure { e ->
                                    error = e.message ?: "Failed to load place visit"
                                    isLoading = false
                                }
                            }
                        }) {
                            Text("Retry")
                        }
                    }
                }
                placeVisit != null -> {
                    PlaceVisitContent(visit = placeVisit!!)
                }
            }
        }
    }
}

@Composable
private fun PlaceVisitContent(visit: PlaceVisit, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = visit.displayName,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    if (visit.approximateAddress != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Place,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = visit.approximateAddress,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    if (visit.city != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.LocationCity,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${visit.city}${visit.countryCode?.let { ", $it" } ?: ""}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }

        // Time information
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Visit Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    DetailRow(
                        icon = Icons.Default.Schedule,
                        label = "Arrived",
                        value = formatInstant(visit.startTime)
                    )

                    DetailRow(
                        icon = Icons.Default.Schedule,
                        label = "Left",
                        value = formatInstant(visit.endTime)
                    )

                    DetailRow(
                        icon = Icons.Default.Timer,
                        label = "Duration",
                        value = formatDuration(visit.duration)
                    )
                }
            }
        }

        // Location information
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Location",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    DetailRow(
                        icon = Icons.Default.MyLocation,
                        label = "Coordinates",
                        value = "${visit.centerLatitude.format(6)}, ${visit.centerLongitude.format(6)}"
                    )
                }
            }
        }

        // Categorization
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Classification",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    DetailRow(
                        icon = Icons.Default.Category,
                        label = "Category",
                        value = visit.category.name
                    )

                    DetailRow(
                        icon = Icons.Default.Stars,
                        label = "Significance",
                        value = visit.significance.name
                    )

                    if (visit.userLabel != null) {
                        DetailRow(
                            icon = Icons.Default.Label,
                            label = "Label",
                            value = visit.userLabel
                        )
                    }

                    if (visit.isFavorite) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.Favorite,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Marked as Favorite",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }

        // Notes
        if (visit.userNotes != null) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Notes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = visit.userNotes,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private fun formatInstant(instant: kotlinx.datetime.Instant): String {
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${localDateTime.date} ${localDateTime.time.hour.toString().padStart(2, '0')}:${localDateTime.time.minute.toString().padStart(2, '0')}"
}

private fun formatDuration(duration: kotlin.time.Duration): String {
    val hours = duration.inWholeHours
    val minutes = (duration.inWholeMinutes % 60)
    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        else -> "${minutes}m"
    }
}

private fun Double.format(decimals: Int) = "%.${decimals}f".format(this)
