package com.po4yka.trailglass.ui.screens.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
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
import com.po4yka.trailglass.domain.model.TransportType
import com.po4yka.trailglass.feature.stats.GetStatsUseCase
import com.po4yka.trailglass.feature.stats.models.ComprehensiveStatistics
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeriodSelector(
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

    // Update selection based on external state if needed, but here we drive it locally for the toggle
    // Ideally we should derive isYearSelected from selectedPeriod, but for now we keep the local state logic
    // consistent with previous implementation, just changing the UI.

    SingleChoiceSegmentedButtonRow(
        modifier = modifier.padding(16.dp)
    ) {
        SegmentedButton(
            selected = isYearSelected,
            onClick = {
                if (!isYearSelected) {
                    isYearSelected = true
                    onPeriodChange(GetStatsUseCase.Period.Year(currentYear))
                }
            },
            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
            label = { Text("Year") }
        )

        SegmentedButton(
            selected = !isYearSelected,
            onClick = {
                if (isYearSelected) {
                    isYearSelected = false
                    val currentMonth =
                        Clock.System
                            .now()
                            .toLocalDateTime(TimeZone.currentSystemDefault())
                            .monthNumber
                    onPeriodChange(GetStatsUseCase.Period.Month(currentYear, currentMonth))
                }
            },
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
            label = { Text("Month") }
        )
    }
}

@Composable
fun EmptyStatsView(modifier: Modifier = Modifier) {
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

fun filterStatsByTransportMode(
    stats: ComprehensiveStatistics,
    transportMode: TransportType
): ComprehensiveStatistics {
    val filteredByTransportType =
        stats.distanceStats.byTransportType
            .filterKeys { it == transportMode }

    val filteredTotalDistanceMeters = filteredByTransportType.values.sum()

    return stats.copy(
        distanceStats =
            stats.distanceStats.copy(
                totalDistanceMeters = filteredTotalDistanceMeters,
                byTransportType = filteredByTransportType
            )
    )
}
