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
import com.po4yka.trailglass.domain.model.*
import kotlin.time.Duration

/**
 * Screen showing frequent places with categorization and visit statistics.
 */
@Composable
fun PlacesScreen(
    places: List<FrequentPlace>,
    searchQuery: String = "",
    onPlaceClick: (FrequentPlace) -> Unit = {},
    onRefresh: () -> Unit = {},
    onSearch: (String) -> Unit = {},
    onClearSearch: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showSearch by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        // Search bar
        if (showSearch) {
            SearchBar(
                query = searchQuery,
                onQueryChange = onSearch,
                onClose = {
                    showSearch = false
                    onClearSearch()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        if (places.isEmpty() && searchQuery.isBlank()) {
            EmptyPlacesView(modifier = Modifier.fillMaxSize())
        } else if (places.isEmpty() && searchQuery.isNotBlank()) {
            NoSearchResultsView(
                query = searchQuery,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            PlacesContent(
                places = places,
                onPlaceClick = onPlaceClick,
                onRefresh = onRefresh,
                showSearchButton = !showSearch,
                onShowSearch = { showSearch = true },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text("Search places...") },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "Search")
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                }
            } else {
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = MaterialTheme.shapes.large
    )
}

@Composable
private fun PlacesContent(
    places: List<FrequentPlace>,
    onPlaceClick: (FrequentPlace) -> Unit,
    onRefresh: () -> Unit,
    showSearchButton: Boolean = true,
    onShowSearch: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Frequent Places",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (showSearchButton) {
                        IconButton(onClick = onShowSearch) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                    }
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            }
        }

        // Group by significance
        val grouped = places.groupBy { it.significance }
        val primaryPlaces = grouped[PlaceSignificance.PRIMARY] ?: emptyList()
        val frequentPlaces = grouped[PlaceSignificance.FREQUENT] ?: emptyList()
        val occasionalPlaces = grouped[PlaceSignificance.OCCASIONAL] ?: emptyList()
        val rarePlaces = grouped[PlaceSignificance.RARE] ?: emptyList()

        // Primary places section
        if (primaryPlaces.isNotEmpty()) {
            item {
                SectionHeader("Primary Places")
            }
            items(primaryPlaces) { place ->
                FrequentPlaceCard(
                    place = place,
                    onClick = { onPlaceClick(place) }
                )
            }
        }

        // Frequent places section
        if (frequentPlaces.isNotEmpty()) {
            item {
                SectionHeader("Frequent Places")
            }
            items(frequentPlaces) { place ->
                FrequentPlaceCard(
                    place = place,
                    onClick = { onPlaceClick(place) }
                )
            }
        }

        // Occasional places section
        if (occasionalPlaces.isNotEmpty()) {
            item {
                SectionHeader("Occasional Places")
            }
            items(occasionalPlaces) { place ->
                FrequentPlaceCard(
                    place = place,
                    onClick = { onPlaceClick(place) }
                )
            }
        }

        // Rare places section (collapsed by default)
        if (rarePlaces.isNotEmpty()) {
            var isExpanded by remember { mutableStateOf(false) }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeader("Other Places (${rarePlaces.size})")
                    IconButton(onClick = { isExpanded = !isExpanded }) {
                        Icon(
                            if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "Collapse" else "Expand"
                        )
                    }
                }
            }

            if (isExpanded) {
                items(rarePlaces) { place ->
                    FrequentPlaceCard(
                        place = place,
                        onClick = { onPlaceClick(place) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun FrequentPlaceCard(
    place: FrequentPlace,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (place.significance) {
                PlaceSignificance.PRIMARY -> MaterialTheme.colorScheme.primaryContainer
                PlaceSignificance.FREQUENT -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header row with icon and name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    getCategoryIcon(place.category),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = place.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (place.city != null) {
                        Text(
                            text = place.city!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (place.isFavorite) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Favorite",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Statistics row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Visit count
                StatChip(
                    icon = Icons.Default.Event,
                    label = "${place.visitCount} visits"
                )

                // Average duration
                if (place.averageDuration > Duration.ZERO) {
                    StatChip(
                        icon = Icons.Default.Schedule,
                        label = formatDuration(place.averageDuration)
                    )
                }

                // Category badge (if not OTHER)
                if (place.category != PlaceCategory.OTHER) {
                    AssistChip(
                        onClick = { },
                        label = {
                            Text(
                                place.category.name.lowercase().replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun StatChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun getCategoryIcon(category: PlaceCategory): androidx.compose.ui.graphics.vector.ImageVector {
    return when (category) {
        PlaceCategory.HOME -> Icons.Default.Home
        PlaceCategory.WORK -> Icons.Default.Work
        PlaceCategory.FOOD -> Icons.Default.Restaurant
        PlaceCategory.SHOPPING -> Icons.Default.ShoppingBag
        PlaceCategory.FITNESS -> Icons.Default.FitnessCenter
        PlaceCategory.ENTERTAINMENT -> Icons.Default.Theaters
        PlaceCategory.TRAVEL -> Icons.Default.Flight
        PlaceCategory.HEALTHCARE -> Icons.Default.LocalHospital
        PlaceCategory.EDUCATION -> Icons.Default.School
        PlaceCategory.RELIGIOUS -> Icons.Default.Church
        PlaceCategory.SOCIAL -> Icons.Default.People
        PlaceCategory.OUTDOOR -> Icons.Default.Park
        PlaceCategory.SERVICE -> Icons.Default.Build
        PlaceCategory.OTHER -> Icons.Default.Place
    }
}

private fun formatDuration(duration: Duration): String {
    val hours = duration.inWholeHours
    val minutes = duration.inWholeMinutes % 60

    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
        hours > 0 -> "${hours}h"
        else -> "${minutes}m"
    }
}

@Composable
private fun EmptyPlacesView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.LocationOff,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "No Places Yet",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Enable location tracking to discover your frequent places",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NoSearchResultsView(
    query: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.SearchOff,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "No Results",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "No places found matching \"$query\"",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
