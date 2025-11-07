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
import com.po4yka.trailglass.feature.stats.GetStatsUseCase
import com.po4yka.trailglass.feature.stats.StatsController
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Stats screen showing travel statistics.
 */
@Composable
fun StatsScreen(
    controller: StatsController,
    modifier: Modifier = Modifier
) {
    val state by controller.state.collectAsState()

    // Load current year on first composition
    LaunchedEffect(Unit) {
        val currentYear = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .year
        controller.loadPeriod(GetStatsUseCase.Period.Year(currentYear))
    }

    Column(modifier = modifier.fillMaxSize()) {
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
                StatsContent(
                    stats = state.stats!!,
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                EmptyView(modifier = Modifier.fillMaxSize())
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
    val currentYear = Clock.System.now()
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
                leadingIcon = if (isYearSelected) {
                    { Icon(Icons.Default.Check, contentDescription = null) }
                } else null
            )

            FilterChip(
                selected = !isYearSelected,
                onClick = {
                    isYearSelected = false
                    val currentMonth = Clock.System.now()
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                        .monthNumber
                    onPeriodChange(GetStatsUseCase.Period.Month(currentYear, currentMonth))
                },
                label = { Text("Month") },
                leadingIcon = if (!isYearSelected) {
                    { Icon(Icons.Default.Check, contentDescription = null) }
                } else null
            )
        }
    }
}

@Composable
private fun StatsContent(
    stats: GetStatsUseCase.Stats,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Summary cards
        item {
            Text(
                text = "Overview",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = "Countries",
                    value = stats.countriesVisited.size.toString(),
                    icon = Icons.Default.Public,
                    modifier = Modifier.weight(1f)
                )

                StatCard(
                    title = "Days",
                    value = stats.totalDays.toString(),
                    icon = Icons.Default.CalendarToday,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = "Trips",
                    value = stats.totalTrips.toString(),
                    icon = Icons.Default.Flight,
                    modifier = Modifier.weight(1f)
                )

                StatCard(
                    title = "Visits",
                    value = stats.totalVisits.toString(),
                    icon = Icons.Default.Place,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Top countries
        if (stats.topCountries.isNotEmpty()) {
            item {
                Text(
                    text = "Top Countries",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            items(stats.topCountries) { (country, count) ->
                ListItem(
                    headlineContent = { Text(country) },
                    trailingContent = { Text("$count visits") },
                    leadingContent = {
                        Icon(Icons.Default.Flag, contentDescription = null)
                    }
                )
            }
        }

        // Top cities
        if (stats.topCities.isNotEmpty()) {
            item {
                Text(
                    text = "Top Cities",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            items(stats.topCities) { (city, count) ->
                ListItem(
                    headlineContent = { Text(city) },
                    trailingContent = { Text("$count visits") },
                    leadingContent = {
                        Icon(Icons.Default.LocationCity, contentDescription = null)
                    }
                )
            }
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
        colors = CardDefaults.cardColors(
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
private fun ErrorView(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = error,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Composable
private fun EmptyView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.TravelExplore,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No travel data yet",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Start tracking your location to see your travel stats",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
