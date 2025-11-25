package com.po4yka.trailglass.ui.screens.places

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Church
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Theaters
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.po4yka.trailglass.domain.model.FrequentPlace
import com.po4yka.trailglass.domain.model.PlaceCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SearchBar(
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
        colors =
            TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
        shape = MaterialTheme.shapes.large
    )
}

@Composable
internal fun RarePlacesSection(
    rarePlaces: List<FrequentPlace>,
    onPlaceClick: (FrequentPlace) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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

        if (isExpanded) {
            rarePlaces.forEach { place ->
                FrequentPlaceCard(
                    place = place,
                    onClick = { onPlaceClick(place) }
                )
            }
        }
    }
}

@Composable
internal fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
internal fun getCategoryIcon(category: PlaceCategory): androidx.compose.ui.graphics.vector.ImageVector =
    when (category) {
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

@Composable
internal fun EmptyPlacesView(
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.padding(horizontal = 40.dp)
        ) {
            Icon(
                Icons.Default.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "No Places Yet",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Start location tracking to discover your frequent places automatically",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onNavigateToSettings,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text("Go to Settings")
            }
        }
    }
}

@Composable
internal fun NoSearchResultsView(
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
