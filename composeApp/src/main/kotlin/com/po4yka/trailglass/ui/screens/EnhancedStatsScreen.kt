package com.po4yka.trailglass.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.po4yka.trailglass.domain.model.TransportType
import com.po4yka.trailglass.feature.stats.EnhancedStatsController
import com.po4yka.trailglass.feature.stats.GetStatsUseCase
import com.po4yka.trailglass.ui.components.ErrorView
import com.po4yka.trailglass.ui.screens.stats.EmptyStatsView
import com.po4yka.trailglass.ui.screens.stats.EnhancedStatsContent
import com.po4yka.trailglass.ui.screens.stats.PeriodSelector
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun EnhancedStatsScreen(
    controller: EnhancedStatsController,
    modifier: Modifier = Modifier
) {
    val state by controller.state.collectAsState()
    var selectedTransportMode by remember { mutableStateOf<TransportType?>(null) }

    LaunchedEffect(Unit) {
        val currentYear =
            Clock.System
                .now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .year
        controller.loadPeriod(GetStatsUseCase.Period.Year(currentYear))
    }

    Column(modifier = modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Statistics & Analytics",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(onClick = { controller.refresh() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                }
            }
        }

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
