package com.po4yka.trailglass.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.po4yka.trailglass.domain.model.TripRoute
import com.po4yka.trailglass.feature.route.TripStatisticsController
import com.po4yka.trailglass.ui.components.StatisticsMetricCards
import com.po4yka.trailglass.ui.components.SummaryTimerDisplay
import com.po4yka.trailglass.ui.components.TransportBreakdownSection

/**
 * Trip Statistics screen - shows detailed statistics for a trip.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripStatisticsScreen(
    tripId: String,
    controller: TripStatisticsController,
    onBack: () -> Unit = {},
    tripName: String? = null,
    primaryCountry: String? = null,
    modifier: Modifier = Modifier
) {
    val state by controller.state.collectAsState()

    // Load statistics on first composition
    LaunchedEffect(tripId) {
        controller.loadStatistics(tripId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trip Statistics") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        when {
            state.isLoading -> {
                // Loading state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = "Loading statistics...",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            state.error != null -> {
                // Error state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = state.error ?: "Unknown error",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = { controller.loadStatistics(tripId) }) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Retry")
                        }
                    }
                }
            }

            state.tripRoute != null -> {
                // Content state
                TripStatisticsContent(
                    tripRoute = state.tripRoute,
                    tripName = tripName,
                    primaryCountry = primaryCountry,
                    showTransportBreakdown = state.showTransportBreakdown,
                    onToggleTransportBreakdown = { controller.toggleTransportBreakdown() },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
        }
    }
}

/**
 * Main statistics content.
 */
@Composable
private fun TripStatisticsContent(
    tripRoute: TripRoute,
    tripName: String?,
    primaryCountry: String?,
    showTransportBreakdown: Boolean,
    onToggleTransportBreakdown: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with country flag and trip name
        item {
            TripStatisticsHeader(
                tripName = tripName,
                primaryCountry = primaryCountry
            )
        }

        // Summary timer display
        item {
            SummaryTimerDisplay(
                days = tripRoute.statistics.totalDurationDays,
                hours = tripRoute.statistics.totalDurationHours,
                minutes = tripRoute.statistics.totalDurationMinutes,
                seconds = tripRoute.statistics.remainingSeconds
            )
        }

        // Metric cards grid
        item {
            StatisticsMetricCards(
                statistics = tripRoute.statistics,
                visitCount = tripRoute.visits.size
            )
        }

        // Transport breakdown section
        if (tripRoute.statistics.distanceByTransport.isNotEmpty()) {
            item {
                TransportBreakdownSection(
                    distanceByTransport = tripRoute.statistics.distanceByTransport,
                    durationByTransport = tripRoute.statistics.durationByTransport,
                    isExpanded = showTransportBreakdown,
                    onToggle = onToggleTransportBreakdown
                )
            }
        }

        // Speed metrics (if available)
        if (tripRoute.statistics.averageSpeedMps != null || tripRoute.statistics.maxSpeedMps != null) {
            item {
                SpeedMetricsCard(statistics = tripRoute.statistics)
            }
        }

        // Countries and cities visited
        if (tripRoute.statistics.countriesVisited.isNotEmpty() || tripRoute.statistics.citiesVisited.isNotEmpty()) {
            item {
                LocationsVisitedCard(
                    countries = tripRoute.statistics.countriesVisited,
                    cities = tripRoute.statistics.citiesVisited
                )
            }
        }
    }
}

/**
 * Header with country flag and trip name.
 */
@Composable
private fun TripStatisticsHeader(
    tripName: String?,
    primaryCountry: String?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Country flag (using emoji for simplicity)
            if (primaryCountry != null) {
                Text(
                    text = getCountryFlag(primaryCountry),
                    style = MaterialTheme.typography.displayMedium
                )
            } else {
                Icon(
                    Icons.Default.Public,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // Trip name
            Column {
                Text(
                    text = tripName ?: "Unnamed Trip",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                if (primaryCountry != null) {
                    Text(
                        text = primaryCountry,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

/**
 * Speed metrics card.
 */
@Composable
private fun SpeedMetricsCard(statistics: com.po4yka.trailglass.domain.model.RouteStatistics) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Speed,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Speed Metrics",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                if (statistics.averageSpeedMps != null) {
                    SpeedMetricItem(
                        label = "Average Speed",
                        speedMps = statistics.averageSpeedMps
                    )
                }

                if (statistics.maxSpeedMps != null) {
                    SpeedMetricItem(
                        label = "Max Speed",
                        speedMps = statistics.maxSpeedMps
                    )
                }
            }
        }
    }
}

/**
 * Individual speed metric item.
 */
@Composable
private fun SpeedMetricItem(label: String, speedMps: Double) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "${(speedMps * 3.6).toInt()} km/h",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Locations visited card.
 */
@Composable
private fun LocationsVisitedCard(
    countries: List<String>,
    cities: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Locations Visited",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(12.dp))

            if (countries.isNotEmpty()) {
                Text(
                    text = "Countries (${countries.size})",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = countries.joinToString(", "),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (cities.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Cities (${cities.size})",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = cities.take(10).joinToString(", ") + if (cities.size > 10) ", ..." else "",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/**
 * Get country flag emoji from country code.
 * Simple implementation - can be enhanced with a proper mapping.
 */
private fun getCountryFlag(countryCode: String): String {
    // Convert country code to flag emoji
    // This is a simple implementation - you might want to use a library for proper mapping
    return when (countryCode.uppercase()) {
        "US", "USA" -> "🇺🇸"
        "GB", "UK" -> "🇬🇧"
        "FR" -> "🇫🇷"
        "DE" -> "🇩🇪"
        "IT" -> "🇮🇹"
        "ES" -> "🇪🇸"
        "JP" -> "🇯🇵"
        "CN" -> "🇨🇳"
        "IN" -> "🇮🇳"
        "BR" -> "🇧🇷"
        "CA" -> "🇨🇦"
        "AU" -> "🇦🇺"
        "MX" -> "🇲🇽"
        "KR" -> "🇰🇷"
        "RU" -> "🇷🇺"
        "GE", "GEORGIA" -> "🇬🇪"
        else -> "🌍" // Default globe icon
    }
}
