package com.po4yka.trailglass.ui.screens.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.po4yka.trailglass.domain.model.PlaceCategory
import com.po4yka.trailglass.domain.model.TransportType
import com.po4yka.trailglass.feature.stats.models.ComprehensiveStatistics
import com.po4yka.trailglass.feature.stats.models.PlaceVisitCount
import com.po4yka.trailglass.ui.components.charts.BarChart
import com.po4yka.trailglass.ui.components.charts.BarData
import com.po4yka.trailglass.ui.components.charts.PieChart
import com.po4yka.trailglass.ui.components.charts.PieData
import com.po4yka.trailglass.ui.theme.extended

@Composable
fun OverviewCards(stats: ComprehensiveStatistics) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard(
                title = "Distance",
                value = "${stats.distanceStats.totalDistanceKm.toInt()} km",
                icon = Icons.Default.Straighten,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Countries",
                value = "${stats.geographicStats.countries.size}",
                icon = Icons.Default.Public,
                modifier = Modifier.weight(1f)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard(
                title = "Places",
                value = "${stats.placeStats.totalPlaces}",
                icon = Icons.Default.Place,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Active Days",
                value = "${stats.activeDays}",
                icon = Icons.Default.CalendarToday,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun DistanceStatsCard(stats: ComprehensiveStatistics) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                "Distance Overview",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            InfoRow("Total Distance", "${stats.distanceStats.totalDistanceKm.toInt()} km")
            InfoRow("Average Speed", "${stats.distanceStats.averageSpeed.toInt()} km/h")

            val hours = stats.distanceStats.totalDuration.inWholeHours
            InfoRow("Total Time", "${hours}h ${(stats.distanceStats.totalDuration.inWholeMinutes % 60)}m")

            stats.distanceStats.mostUsedTransportType?.let { type ->
                InfoRow("Most Used", type.name.lowercase().replaceFirstChar { it.uppercase() })
            }
        }
    }
}

@Composable
fun TransportDistributionCard(
    stats: ComprehensiveStatistics,
    selectedMode: TransportType?
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Distance by Transport Type",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            val gradientColors = MaterialTheme.colorScheme.extended.gradientColors
            val transportColors =
                TransportType.entries.associateWith { type ->
                    gradientColors[type.ordinal % gradientColors.size]
                }

            val barData =
                stats.distanceStats.byTransportType
                    .map { (type, meters) ->
                        val isSelected = selectedMode == type
                        BarData(
                            label = type.name.take(4),
                            value = (meters / 1000).toFloat(),
                            formattedValue = "${(meters / 1000).toInt()}km",
                            color =
                                if (selectedMode != null && !isSelected) {
                                    transportColors[type]?.copy(alpha = 0.3f)
                                } else {
                                    transportColors[type]
                                }
                        )
                    }.sortedByDescending { it.value }

            BarChart(
                data = barData,
                showValues = true
            )
        }
    }
}

@Composable
fun PlaceStatsCard(stats: ComprehensiveStatistics) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                "Place Overview",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            InfoRow("Total Places", "${stats.placeStats.totalPlaces}")
            InfoRow("Total Visits", "${stats.placeStats.totalVisits}")

            val avgDuration = stats.placeStats.averageVisitDuration
            val avgHours = avgDuration.inWholeHours
            val avgMinutes = avgDuration.inWholeMinutes % 60
            InfoRow("Avg Visit Duration", if (avgHours > 0) "${avgHours}h ${avgMinutes}m" else "${avgMinutes}m")

            stats.placeStats.topCategory?.let { category ->
                InfoRow("Top Category", category.name.lowercase().replaceFirstChar { it.uppercase() })
            }
        }
    }
}

@Composable
fun CategoryDistributionCard(stats: ComprehensiveStatistics) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Visits by Category",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            val categoryColors =
                mapOf(
                    PlaceCategory.HOME to MaterialTheme.colorScheme.extended.neutralCategory,
                    PlaceCategory.WORK to MaterialTheme.colorScheme.primary,
                    PlaceCategory.FOOD to MaterialTheme.colorScheme.extended.morningCategory,
                    PlaceCategory.SHOPPING to MaterialTheme.colorScheme.extended.waterCategory,
                    PlaceCategory.FITNESS to MaterialTheme.colorScheme.extended.success,
                    PlaceCategory.ENTERTAINMENT to MaterialTheme.colorScheme.extended.eveningCategory,
                    PlaceCategory.TRAVEL to MaterialTheme.colorScheme.extended.activeRoute,
                    PlaceCategory.HEALTHCARE to MaterialTheme.colorScheme.extended.warning,
                    PlaceCategory.EDUCATION to MaterialTheme.colorScheme.secondary,
                    PlaceCategory.RELIGIOUS to MaterialTheme.colorScheme.tertiary,
                    PlaceCategory.SOCIAL to MaterialTheme.colorScheme.extended.waterCategory,
                    PlaceCategory.OUTDOOR to MaterialTheme.colorScheme.extended.success,
                    PlaceCategory.SERVICE to MaterialTheme.colorScheme.extended.neutralCategory,
                    PlaceCategory.OTHER to MaterialTheme.colorScheme.extended.disabled
                )

            val pieData =
                stats.placeStats.visitsByCategory
                    .filter { it.key != PlaceCategory.OTHER || it.value > 0 }
                    .map { (category, count) ->
                        PieData(
                            label = category.name.lowercase().replaceFirstChar { it.uppercase() },
                            value = count.toFloat(),
                            color = categoryColors[category] ?: Color.Gray
                        )
                    }.sortedByDescending { it.value }
                    .take(6)

            if (pieData.isNotEmpty()) {
                PieChart(data = pieData)
            }
        }
    }
}

@Composable
fun MostVisitedPlaceCard(place: PlaceVisitCount) {
    Card(modifier = Modifier.fillMaxWidth()) {
        ListItem(
            headlineContent = { Text(place.placeName) },
            supportingContent = {
                Text("${place.visitCount} visits • ${place.totalDuration.inWholeHours}h total")
            },
            trailingContent = {
                Text(
                    place.category.name.take(3),
                    style = MaterialTheme.typography.labelSmall
                )
            },
            leadingContent = {
                Icon(Icons.Default.Place, contentDescription = null)
            }
        )
    }
}

@Composable
fun TravelPatternsCard(stats: ComprehensiveStatistics) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                "Travel Patterns",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            stats.travelPatterns.peakTravelDay?.let { day ->
                InfoRow("Most Active Day", day.name.lowercase().replaceFirstChar { it.uppercase() })
            }

            stats.travelPatterns.peakTravelHour?.let { hour ->
                val timeRange =
                    when (hour) {
                        in 0..5 -> "Night (12AM-6AM)"
                        in 6..11 -> "Morning (6AM-12PM)"
                        in 12..17 -> "Afternoon (12PM-6PM)"
                        else -> "Evening (6PM-12AM)"
                    }
                InfoRow("Most Active Time", timeRange)
            }

            val split = stats.travelPatterns.weekdayVsWeekend
            InfoRow("Weekday vs Weekend", "${split.weekdayPercentage.toInt()}% / ${split.weekendPercentage.toInt()}%")
        }
    }
}

@Composable
fun ActivityHeatmapCard(stats: ComprehensiveStatistics) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            val heatmapData =
                stats.travelPatterns.weekdayActivity.mapValues { (_, _) ->
                    stats.travelPatterns.hourlyActivity.mapValues { (_, hourActivity) ->
                        hourActivity.totalEvents
                    }
                }

            com.po4yka.trailglass.ui.components.charts
                .ActivityHeatmap(data = heatmapData)
        }
    }
}

@Composable
fun GeographicStatsCard(stats: ComprehensiveStatistics) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                "Geographic Overview",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            InfoRow("Countries Visited", "${stats.geographicStats.countries.size}")
            InfoRow("Cities Visited", "${stats.geographicStats.cities.size}")

            stats.geographicStats.homeBase?.let { home ->
                InfoRow("Home Base", home.city ?: home.name)
            }

            stats.geographicStats.furthestLocation?.let { furthest ->
                InfoRow("Furthest Location", furthest.city ?: furthest.name)
            }
        }
    }
}

@Composable
fun TopCountriesCard(stats: ComprehensiveStatistics) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Top Countries",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            stats.geographicStats.topCountries.take(5).forEach { country ->
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            country.countryCode,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "${country.cities.size} cities",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }

                    Text(
                        "${country.visitCount} visits",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (country != stats.geographicStats.topCountries.last()) {
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
