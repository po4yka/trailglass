package com.po4yka.trailglass.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Commute
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.po4yka.trailglass.domain.model.TripRoute
import kotlin.math.roundToInt

/**
 * Summary card displayed at the bottom of the Route View.
 * Shows trip statistics and a play button for route replay.
 */
@Composable
fun RouteSummaryCard(
    tripRoute: TripRoute,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
        ) {
            // Header with play button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Route Overview",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(4.dp))

                    // Duration display
                    val stats = tripRoute.statistics
                    Text(
                        text =
                            formatDuration(
                                stats.totalDurationDays,
                                stats.totalDurationHours,
                                stats.totalDurationMinutes
                            ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Play button
                FilledTonalButton(
                    onClick = onPlayClick,
                    modifier = Modifier.height(48.dp)
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Replay",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Replay")
                }
            }

            Spacer(Modifier.height(16.dp))

            // Statistics grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatisticItem(
                    icon = Icons.Default.Straighten,
                    label = "Distance",
                    value = formatDistance(tripRoute.statistics.totalDistanceKilometers),
                    modifier = Modifier.weight(1f)
                )

                StatisticItem(
                    icon = Icons.Default.LocationOn,
                    label = "Locations",
                    value = tripRoute.statistics.numberOfLocations.toString(),
                    modifier = Modifier.weight(1f)
                )

                StatisticItem(
                    icon = Icons.Default.Photo,
                    label = "Photos",
                    value = tripRoute.statistics.numberOfPhotos.toString(),
                    modifier = Modifier.weight(1f)
                )

                tripRoute.statistics.countriesVisited.takeIf { it.isNotEmpty() }?.let { countries ->
                    StatisticItem(
                        icon = Icons.Default.Public,
                        label = "Countries",
                        value = countries.size.toString(),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Transport breakdown (if available)
            val transportStats = tripRoute.statistics.distanceByTransport
            if (transportStats.isNotEmpty() && transportStats.size > 1) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))

                Text(
                    text = "By Transport",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(8.dp))

                // Transport chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    transportStats.entries
                        .sortedByDescending { it.value }
                        .take(3) // Show top 3 transport types
                        .forEach { (type, distance) ->
                            TransportChip(
                                transportType = type.name,
                                distance = distance / 1000.0 // Convert to km
                            )
                        }
                }
            }
        }
    }
}

/**
 * Individual statistic item.
 */
@Composable
private fun StatisticItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.height(4.dp))
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

/**
 * Transport type chip with distance.
 */
@Composable
private fun TransportChip(
    transportType: String,
    distance: Double
) {
    AssistChip(
        onClick = { },
        label = {
            Text(
                text = "${transportType.lowercase().replaceFirstChar { it.uppercase() }}: ${distance.roundToInt()} km",
                style = MaterialTheme.typography.labelMedium
            )
        },
        leadingIcon = {
            Icon(
                imageVector =
                    when (transportType.uppercase()) {
                        "WALK" -> Icons.AutoMirrored.Filled.DirectionsWalk
                        "BIKE" -> Icons.AutoMirrored.Filled.DirectionsBike
                        "CAR" -> Icons.Default.DirectionsCar
                        "TRAIN" -> Icons.Default.Train
                        "PLANE" -> Icons.Default.Flight
                        "BOAT" -> Icons.Default.DirectionsBoat
                        else -> Icons.Default.Commute
                    },
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        }
    )
}

/**
 * Format duration for display.
 */
private fun formatDuration(
    days: Int,
    hours: Int,
    minutes: Int
): String =
    buildString {
        if (days > 0) append("$days day${if (days > 1) "s" else ""}, ")
        if (hours > 0) append("$hours hr${if (hours > 1) "s" else ""}, ")
        append("$minutes min${if (minutes > 1) "s" else ""}")
    }

/**
 * Format distance for display.
 */
private fun formatDistance(kilometers: Double): String =
    when {
        kilometers < 1.0 -> "${(kilometers * 1000).roundToInt()} m"
        kilometers < 10.0 -> "%.1f km".format(kilometers)
        else -> "${kilometers.roundToInt()} km"
    }
