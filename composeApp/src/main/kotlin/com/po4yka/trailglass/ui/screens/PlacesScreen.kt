package com.po4yka.trailglass.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.po4yka.trailglass.domain.model.FrequentPlace
import com.po4yka.trailglass.domain.model.PlaceSignificance
import com.po4yka.trailglass.ui.screens.places.EmptyPlacesView
import com.po4yka.trailglass.ui.screens.places.FrequentPlaceCard
import com.po4yka.trailglass.ui.screens.places.NoSearchResultsView
import com.po4yka.trailglass.ui.screens.places.RarePlacesSection
import com.po4yka.trailglass.ui.screens.places.SearchBar
import com.po4yka.trailglass.ui.screens.places.SectionHeader

/** Screen showing frequent places with categorization and visit statistics. */
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
                modifier =
                    Modifier
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
            item {
                RarePlacesSection(rarePlaces, onPlaceClick)
            }
        }
    }
}
