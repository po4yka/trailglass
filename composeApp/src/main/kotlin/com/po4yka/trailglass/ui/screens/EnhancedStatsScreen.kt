package com.po4yka.trailglass.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.po4yka.trailglass.domain.model.PlaceCategory
import com.po4yka.trailglass.domain.model.TransportType
import com.po4yka.trailglass.feature.stats.EnhancedStatsController
import com.po4yka.trailglass.feature.stats.GetStatsUseCase
import com.po4yka.trailglass.feature.stats.models.ComprehensiveStatistics
import com.po4yka.trailglass.ui.components.ErrorView
import com.po4yka.trailglass.ui.components.TransportModeSelector
import com.po4yka.trailglass.ui.components.charts.ActivityHeatmap
import com.po4yka.trailglass.ui.components.charts.BarChart
import com.po4yka.trailglass.ui.components.charts.BarData
import com.po4yka.trailglass.ui.components.charts.PieChart
import com.po4yka.trailglass.ui.components.charts.PieData
import com.po4yka.trailglass.ui.theme.extended
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/** Enhanced stats screen with comprehensive analytics and visualizations. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedStatsScreen(
    controller: EnhancedStatsController,
    modifier: Modifier = Modifier
) {
    val state by controller.state.collectAsState()
    var selectedTransportMode by remember { mutableStateOf<TransportType?>(null) }

    // Load current year on first composition
    LaunchedEffect(Unit) {
        val currentYear =
            Clock.System
                .now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .year
        controller.loadPeriod(GetStatsUseCase.Period.Year(currentYear))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistics & Analytics") },
                actions = {
                    IconButton(onClick = { controller.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            // Period selector
            PeriodSelector(
                selectedPeriod = state.period,
                onPeriodChange = { controller.loadPeriod(it) },
                modifier = Modifier.fillMaxWidth()
            )

            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                state.error != null -> {
                    ErrorView(
                        error = state.error!!,
                        onRetry = { controller.refresh() },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                state.stats != null -> {
                    EnhancedStatsContent(
                        stats = state.stats!!,
                        selectedTransportMode = selectedTransportMode,
                        onTransportModeSelected = { mode ->
                            selectedTransportMode = if (selectedTransportMode == mode) null else mode
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                else -> {
                    EmptyStatsView(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}

@Composable
private fun PeriodSelector(
    selectedPeriod: GetStatsUseCase.Period?,
    onPeriodChange: (GetStatsUseCase.Period) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentYear =
        Clock.System
            .now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .year

    var isYearSelected by remember { mutableStateOf(true) }

    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = isYearSelected,
                onClick = {
                    isYearSelected = true
                    onPeriodChange(GetStatsUseCase.Period.Year(currentYear))
                },
                label = { Text("Year") },
                leadingIcon =
                    if (isYearSelected) {
                        { Icon(Icons.Default.Check, contentDescription = null) }
                    } else {
                        null
                    }
            )

            FilterChip(
                selected = !isYearSelected,
                onClick = {
                    isYearSelected = false
                    val currentMonth =
                        Clock.System
                            .now()
                            .toLocalDateTime(TimeZone.currentSystemDefault())
                            .monthNumber
                    onPeriodChange(GetStatsUseCase.Period.Month(currentYear, currentMonth))
                },
                label = { Text("Month") },
                leadingIcon =
                    if (!isYearSelected) {
                        { Icon(Icons.Default.Check, contentDescription = null) }
                    } else {
                        null
                    }
            )
        }
    }
}

@Composable
private fun EnhancedStatsContent(
    stats: ComprehensiveStatistics,
    selectedTransportMode: TransportType?,
    onTransportModeSelected: (TransportType) -> Unit,
    modifier: Modifier = Modifier
) {
    // Filter stats by selected transport mode
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
        // Transport Mode Selector
        item {
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

        // Overview section
        item {
            SectionHeader("Overview")
        }

        item {
            OverviewCards(filteredStats)
        }

        // Distance statistics
        item {
            SectionHeader("Distance Traveled")
        }

        item {
            DistanceStatsCard(filteredStats)
        }

        // Transport type distribution
        if (stats.distanceStats.byTransportType.isNotEmpty()) {
            item {
                TransportDistributionCard(stats, selectedTransportMode)
            }
        }

        // Place statistics
        item {
            SectionHeader("Places Visited")
        }

        item {
            PlaceStatsCard(filteredStats)
        }

        // Category distribution
        if (filteredStats.placeStats.visitsByCategory.isNotEmpty()) {
            item {
                CategoryDistributionCard(filteredStats)
            }
        }

        // Most visited places
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

        // Travel patterns
        item {
            SectionHeader("Travel Patterns")
        }

        item {
            TravelPatternsCard(filteredStats)
        }

        // Activity heatmap
        if (filteredStats.travelPatterns.weekdayActivity.isNotEmpty()) {
            item {
                ActivityHeatmapCard(filteredStats)
            }
        }

        // Geographic statistics
        item {
            SectionHeader("Geography")
        }

        item {
            GeographicStatsCard(filteredStats)
        }

        // Top countries
        if (filteredStats.geographicStats.topCountries.isNotEmpty()) {
            item {
                TopCountriesCard(filteredStats)
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
private fun OverviewCards(stats: ComprehensiveStatistics) {
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
private fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Text(
                text = value,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun DistanceStatsCard(stats: ComprehensiveStatistics) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Distance Overview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

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
private fun TransportDistributionCard(
    stats: ComprehensiveStatistics,
    selectedMode: TransportType?
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Distance by Transport Type",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Use gradient colors from Silent Waters palette for transport types
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
private fun PlaceStatsCard(stats: ComprehensiveStatistics) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Place Overview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

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
private fun CategoryDistributionCard(stats: ComprehensiveStatistics) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Visits by Category",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Use semantic and gradient colors from Silent Waters palette
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
private fun MostVisitedPlaceCard(place: com.po4yka.trailglass.feature.stats.models.PlaceVisitCount) {
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
private fun TravelPatternsCard(stats: ComprehensiveStatistics) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Travel Patterns", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

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
private fun ActivityHeatmapCard(stats: ComprehensiveStatistics) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Convert activity data to heatmap format
            val heatmapData =
                stats.travelPatterns.weekdayActivity.mapValues { (_, _) ->
                    stats.travelPatterns.hourlyActivity.mapValues { (_, hourActivity) ->
                        hourActivity.totalEvents
                    }
                }

            ActivityHeatmap(data = heatmapData)
        }
    }
}

@Composable
private fun GeographicStatsCard(stats: ComprehensiveStatistics) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Geographic Overview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

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
private fun TopCountriesCard(stats: ComprehensiveStatistics) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Top Countries",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
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
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "${country.cities.size} cities",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        "${country.visitCount} visits",
                        style = MaterialTheme.typography.bodyMedium
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
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun EmptyStatsView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.BarChart,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No statistics available",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = "Start tracking to see your travel statistics",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Filter comprehensive statistics by transport mode. Returns a new ComprehensiveStatistics instance with only data from
 * the selected transport mode.
 */
private fun filterStatsByTransportMode(
    stats: ComprehensiveStatistics,
    transportMode: TransportType
): ComprehensiveStatistics {
    // Filter distance stats
    val filteredByTransportType =
        stats.distanceStats.byTransportType
            .filterKeys { it == transportMode }

    val filteredTotalDistanceMeters = filteredByTransportType.values.sum()

    // For now, return stats with filtered transport data
    // This is a simplified filter - a full implementation would require
    // filtering routes, visits connected to those routes, etc.
    return stats.copy(
        distanceStats =
            stats.distanceStats.copy(
                totalDistanceMeters = filteredTotalDistanceMeters,
                byTransportType = filteredByTransportType
            )
    )
}
