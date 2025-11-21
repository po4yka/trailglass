package com.po4yka.trailglass.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Commute
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.po4yka.trailglass.domain.model.TransportType
import kotlin.math.roundToInt

/**
 * Expandable transport breakdown section showing distance and duration by transport type.
 */
@Composable
fun TransportBreakdownSection(
    distanceByTransport: Map<TransportType, Double>,
    durationByTransport: Map<TransportType, Long>,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Commute,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Transport Breakdown",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = onToggle) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand"
                    )
                }
            }

            // Expandable content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Sort by distance (descending)
                    val sortedTransports = distanceByTransport.entries
                        .sortedByDescending { it.value }

                    sortedTransports.forEach { (type, distance) ->
                        val duration = durationByTransport[type] ?: 0L
                        TransportBreakdownItem(
                            transportType = type,
                            distanceMeters = distance,
                            durationSeconds = duration
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual transport breakdown item.
 */
@Composable
private fun TransportBreakdownItem(
    transportType: TransportType,
    distanceMeters: Double,
    durationSeconds: Long
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Transport icon and name
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = getTransportIcon(transportType),
                contentDescription = null,
                tint = getTransportColor(transportType),
                modifier = Modifier.size(24.dp)
            )

            Column {
                Text(
                    text = transportType.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = formatDuration(durationSeconds),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Distance
        Text(
            text = formatDistance(distanceMeters / 1000.0),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Get icon for transport type.
 */
private fun getTransportIcon(type: TransportType): ImageVector {
    return when (type) {
        TransportType.WALK -> Icons.AutoMirrored.Filled.DirectionsWalk
        TransportType.BIKE -> Icons.AutoMirrored.Filled.DirectionsBike
        TransportType.CAR -> Icons.Default.DirectionsCar
        TransportType.TRAIN -> Icons.Default.Train
        TransportType.PLANE -> Icons.Default.Flight
        TransportType.BOAT -> Icons.Default.DirectionsBoat
        TransportType.UNKNOWN -> Icons.Default.Commute
    }
}

/**
 * Get color for transport type.
 */
@Composable
private fun getTransportColor(type: TransportType): androidx.compose.ui.graphics.Color {
    return when (type) {
        TransportType.WALK -> MaterialTheme.colorScheme.primary
        TransportType.BIKE -> MaterialTheme.colorScheme.secondary
        TransportType.CAR -> MaterialTheme.colorScheme.tertiary
        TransportType.TRAIN -> MaterialTheme.colorScheme.primary
        TransportType.PLANE -> MaterialTheme.colorScheme.error
        TransportType.BOAT -> MaterialTheme.colorScheme.secondary
        TransportType.UNKNOWN -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

/**
 * Format distance for display.
 */
private fun formatDistance(kilometers: Double): String {
    return when {
        kilometers < 1.0 -> "${(kilometers * 1000).roundToInt()} m"
        kilometers < 10.0 -> "%.1f km".format(kilometers)
        else -> "${kilometers.roundToInt()} km"
    }
}

/**
 * Format duration for display.
 */
private fun formatDuration(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60

    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 -> "${minutes}m"
        else -> "${seconds}s"
    }
}
