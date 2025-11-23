package com.po4yka.trailglass.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** Summary timer display showing days, hours, minutes, and seconds in a segmented layout. */
@Composable
fun SummaryTimerDisplay(
    days: Int,
    hours: Int,
    minutes: Int,
    seconds: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Total Duration",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(16.dp))

            // Segmented timer display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TimerSegment(
                    value = days,
                    label = if (days == 1) "Day" else "Days",
                    modifier = Modifier.weight(1f)
                )

                TimerSegment(
                    value = hours,
                    label = if (hours == 1) "Hour" else "Hours",
                    modifier = Modifier.weight(1f)
                )

                TimerSegment(
                    value = minutes,
                    label = if (minutes == 1) "Minute" else "Minutes",
                    modifier = Modifier.weight(1f)
                )

                TimerSegment(
                    value = seconds,
                    label = if (seconds == 1) "Second" else "Seconds",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/** Individual timer segment. */
@Composable
private fun TimerSegment(
    value: Int,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value.toString().padStart(2, '0'),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
        )
    }
}
