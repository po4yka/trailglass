package com.po4yka.trailglass.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.po4yka.trailglass.TrailGlassApplication
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Example screen showing how to use the DI component in Compose.
 *
 * This demonstrates accessing controllers and repositories through
 * the application DI component.
 */
@Composable
fun ExampleUsageScreen() {
    val context = LocalContext.current
    val appComponent = remember {
        (context.applicationContext as TrailGlassApplication).appComponent
    }

    // Access controllers through DI
    val timelineController = appComponent.timelineController
    val statsController = appComponent.statsController
    val locationRepository = appComponent.locationRepository

    val coroutineScope = rememberCoroutineScope()
    var statusMessage by remember { mutableStateOf("Ready") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "DI Integration Example",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = statusMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary
        )

        Divider()

        // Example: Using TimelineController
        Button(
            onClick = {
                coroutineScope.launch {
                    val today = Clock.System.now()
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                        .date
                    timelineController.loadDay(today)
                    statusMessage = "Loaded timeline for $today"
                }
            }
        ) {
            Text("Load Today's Timeline")
        }

        // Example: Using LocationRepository
        Button(
            onClick = {
                coroutineScope.launch {
                    val result = locationRepository.getSamples(
                        userId = appComponent.userId,
                        startTime = Clock.System.now() - kotlin.time.Duration.parse("24h"),
                        endTime = Clock.System.now()
                    )
                    result.onSuccess { samples ->
                        statusMessage = "Found ${samples.size} location samples"
                    }.onError { error ->
                        statusMessage = "Error: ${error.userMessage}"
                    }
                }
            }
        ) {
            Text("Query Location Samples")
        }

        Divider()

        Text(
            text = "Available Dependencies",
            style = MaterialTheme.typography.titleMedium
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text("• TimelineController: ${timelineController::class.simpleName}")
            Text("• StatsController: ${statsController::class.simpleName}")
            Text("• LocationRepository: ${locationRepository::class.simpleName}")
            Text("• User ID: ${appComponent.userId}")
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "All dependencies are injected via kotlin-inject",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
