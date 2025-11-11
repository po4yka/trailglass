package com.po4yka.trailglass.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.po4yka.trailglass.domain.model.RouteStatistics
import kotlin.math.roundToInt

/**
 * Grid of metric cards showing trip statistics.
 */
@Composable
fun StatisticsMetricCards(
    statistics: RouteStatistics,
    visitCount: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // First row: Distance and Locations
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                icon = Icons.Default.Straighten,
                label = "Distance",
                value = formatDistance(statistics.totalDistanceKilometers),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.weight(1f)
            )

            MetricCard(
                icon = Icons.Default.LocationOn,
                label = "Locations",
                value = statistics.numberOfLocations.toString(),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.weight(1f)
            )
        }

        // Second row: Photos and Videos
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                icon = Icons.Default.Photo,
                label = "Photos",
                value = statistics.numberOfPhotos.toString(),
                color = MaterialTheme.colorScheme.tertiaryContainer,
                modifier = Modifier.weight(1f)
            )

            MetricCard(
                icon = Icons.Default.VideoLibrary,
                label = "Videos",
                value = statistics.numberOfVideos.toString(),
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.weight(1f)
            )
        }

        // Third row: Countries and Cities (if available)
        if (statistics.countriesVisited.isNotEmpty() || statistics.citiesVisited.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (statistics.countriesVisited.isNotEmpty()) {
                    MetricCard(
                        icon = Icons.Default.Public,
                        label = "Countries",
                        value = statistics.countriesVisited.size.toString(),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (statistics.citiesVisited.isNotEmpty()) {
                    MetricCard(
                        icon = Icons.Default.LocationCity,
                        label = "Cities",
                        value = statistics.citiesVisited.size.toString(),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Fill remaining space if only one metric
                if (statistics.countriesVisited.isEmpty() || statistics.citiesVisited.isEmpty()) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * Individual metric card.
 */
@Composable
private fun MetricCard(
    icon: ImageVector,
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        }
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
