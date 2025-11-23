package com.po4yka.trailglass.ui.screens.tripdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.po4yka.trailglass.domain.model.Trip
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
internal fun TripHeaderCard(trip: Trip) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (trip.isOngoing) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = trip.displayName,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Date range
                    val startDate = trip.startTime.toLocalDateTime(TimeZone.currentSystemDefault())
                    val dateText =
                        trip.endTime?.let { endTime ->
                            val endDate = endTime.toLocalDateTime(TimeZone.currentSystemDefault())
                            "${startDate.date} to ${endDate.date}"
                        } ?: "Started ${startDate.date}"

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = dateText,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    // Duration
                    if (trip.duration != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = formatDuration(trip.duration!!),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                // Badges
                Column(horizontalAlignment = Alignment.End) {
                    if (trip.isOngoing) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.primary
                        ) {
                            Text("Ongoing")
                        }
                    }
                    if (trip.isAutoDetected) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Badge(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        ) {
                            Text("Auto-detected")
                        }
                    }
                    if (trip.isPublic) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Badge {
                            Text("Public")
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun TripStatisticsCard(trip: Trip) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Statistics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Statistics grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                if (trip.totalDistanceMeters > 0) {
                    StatItem(
                        icon = Icons.Default.Straighten,
                        label = "Distance",
                        value = "${(trip.totalDistanceMeters / 1000).toInt()} km"
                    )
                }
                if (trip.visitedPlaceCount > 0) {
                    StatItem(
                        icon = Icons.Default.Place,
                        label = "Places",
                        value = "${trip.visitedPlaceCount}"
                    )
                }
                if (trip.countriesVisited.isNotEmpty()) {
                    StatItem(
                        icon = Icons.Default.Public,
                        label = "Countries",
                        value = "${trip.countriesVisited.size}"
                    )
                }
            }
        }
    }
}

@Composable
internal fun VisitedPlacesCard(
    countries: List<String>,
    cities: List<String>
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (countries.isNotEmpty()) {
                Text(
                    text = "Countries Visited",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = countries.joinToString(", "),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (cities.isNotEmpty()) {
                if (countries.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                }
                Text(
                    text = "Cities Visited",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text =
                        cities.take(10).joinToString(", ") +
                            if (cities.size > 10) " and ${cities.size - 10} more" else "",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
