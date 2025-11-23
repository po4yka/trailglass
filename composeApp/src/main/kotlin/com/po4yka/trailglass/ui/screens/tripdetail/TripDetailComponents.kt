package com.po4yka.trailglass.ui.screens.tripdetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
internal fun StatItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

enum class ExportFormat {
    GPX,
    KML
}

internal fun formatDuration(duration: kotlin.time.Duration): String {
    val days = duration.inWholeDays
    val hours = duration.inWholeHours % 24
    val minutes = duration.inWholeMinutes % 60

    return when {
        days > 0 && hours > 0 -> "$days days, $hours hours"
        days > 0 -> "$days days"
        hours > 0 && minutes > 0 -> "$hours hours, $minutes min"
        hours > 0 -> "$hours hours"
        else -> "$minutes min"
    }
}
