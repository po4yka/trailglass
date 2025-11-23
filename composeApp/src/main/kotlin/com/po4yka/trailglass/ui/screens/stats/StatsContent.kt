package com.po4yka.trailglass.ui.screens.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.po4yka.trailglass.domain.model.TransportType
import com.po4yka.trailglass.feature.stats.models.ComprehensiveStatistics
import com.po4yka.trailglass.ui.components.TransportModeSelector

@Composable
fun EnhancedStatsContent(
    stats: ComprehensiveStatistics,
    selectedTransportMode: TransportType?,
    onTransportModeSelected: (TransportType) -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredStats =
        if (selectedTransportMode != null) {
            filterStatsByTransportMode(stats, selectedTransportMode)
        } else {
            stats
        }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            TransportFilterCard(
                selectedTransportMode = selectedTransportMode,
                onTransportModeSelected = onTransportModeSelected
            )
        }

        item {
            SectionHeader("Overview")
        }

        item {
            OverviewCards(filteredStats)
        }

        item {
            SectionHeader("Distance Traveled")
        }

        item {
            DistanceStatsCard(filteredStats)
        }

        if (stats.distanceStats.byTransportType.isNotEmpty()) {
            item {
                TransportDistributionCard(stats, selectedTransportMode)
            }
        }

        item {
            SectionHeader("Places Visited")
        }

        item {
            PlaceStatsCard(filteredStats)
        }

        if (filteredStats.placeStats.visitsByCategory.isNotEmpty()) {
            item {
                CategoryDistributionCard(filteredStats)
            }
        }

        if (filteredStats.placeStats.mostVisitedPlaces.isNotEmpty()) {
            item {
                SectionHeader("Most Visited Places")
            }

            filteredStats.placeStats.mostVisitedPlaces.take(5).forEach { place ->
                item {
                    MostVisitedPlaceCard(place)
                }
            }
        }

        item {
            SectionHeader("Travel Patterns")
        }

        item {
            TravelPatternsCard(filteredStats)
        }

        if (filteredStats.travelPatterns.weekdayActivity.isNotEmpty()) {
            item {
                ActivityHeatmapCard(filteredStats)
            }
        }

        item {
            SectionHeader("Geography")
        }

        item {
            GeographicStatsCard(filteredStats)
        }

        if (filteredStats.geographicStats.topCountries.isNotEmpty()) {
            item {
                TopCountriesCard(filteredStats)
            }
        }
    }
}

@Composable
private fun TransportFilterCard(
    selectedTransportMode: TransportType?,
    onTransportModeSelected: (TransportType) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Filter by Transport Mode",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            TransportModeSelector(
                selectedMode = selectedTransportMode,
                onModeSelected = onTransportModeSelected,
                showLabels = true
            )

            if (selectedTransportMode != null) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Showing: ${
                            selectedTransportMode.name.lowercase().replaceFirstChar {
                                it.uppercase()
                            }
                        }",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                    TextButton(onClick = { onTransportModeSelected(selectedTransportMode) }) {
                        Text("Clear Filter")
                    }
                }
            }
        }
    }
}
