package com.po4yka.trailglass.ui.screens.places

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.po4yka.trailglass.domain.model.FrequentPlace
import com.po4yka.trailglass.domain.model.PlaceCategory
import com.po4yka.trailglass.domain.model.PlaceSignificance
import kotlin.time.Duration

@Composable
internal fun FrequentPlaceCard(
    place: FrequentPlace,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    when (place.significance) {
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
                    place.city?.let { city ->
                        Text(
                            text = city,
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
                modifier =
                    Modifier
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
                                place.category.name
                                    .lowercase()
                                    .replaceFirstChar { it.uppercase() },
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
internal fun StatChip(
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

private fun formatDuration(duration: Duration): String {
    val hours = duration.inWholeHours
    val minutes = duration.inWholeMinutes % 60

    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
        hours > 0 -> "${hours}h"
        else -> "${minutes}m"
    }
}
