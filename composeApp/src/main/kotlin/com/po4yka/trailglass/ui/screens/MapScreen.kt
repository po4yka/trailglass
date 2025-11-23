package com.po4yka.trailglass.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.po4yka.trailglass.domain.model.MapMarker
import com.po4yka.trailglass.feature.map.MapController
import com.po4yka.trailglass.ui.components.MapView
import com.po4yka.trailglass.ui.dialogs.PhotoAttachmentInfoDialog
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.days

/** Map screen showing travels on a map. */
@Composable
fun MapScreen(
    controller: MapController,
    onNavigateToPlaceVisitDetail: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by controller.state.collectAsState()
    var showPhotoAttachmentInfo by remember { mutableStateOf(false) }

    // Load last 30 days of data on first composition
    LaunchedEffect(Unit) {
        val now = Clock.System.now()
        val thirtyDaysAgo = now.minus(30.days)
        controller.loadMapData(thirtyDaysAgo, now)
    }

    // Photo attachment info dialog
    if (showPhotoAttachmentInfo) {
        PhotoAttachmentInfoDialog(
            onDismiss = { showPhotoAttachmentInfo = false }
        )
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Map
        MapView(
            controller = controller,
            onMarkerClick = { marker ->
                // Navigate to visit detail
                onNavigateToPlaceVisitDetail(marker.placeVisitId)
            },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
        )

        // Selected marker info
        state.selectedMarker?.let { marker ->
            MarkerInfoCard(
                marker = marker,
                onClose = { controller.deselectMarker() },
                onViewDetails = { onNavigateToPlaceVisitDetail(marker.placeVisitId) },
                onAddPhoto = { showPhotoAttachmentInfo = true },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun MarkerInfoCard(
    marker: MapMarker,
    onClose: () -> Unit,
    onViewDetails: () -> Unit,
    onAddPhoto: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = marker.title ?: "Unknown location",
                        style = MaterialTheme.typography.titleMedium
                    )

                    marker.snippet?.let { snippetText ->
                        Text(
                            text = snippetText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onViewDetails,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Info, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Details")
                }

                OutlinedButton(
                    onClick = onAddPhoto,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Photo")
                }
            }
        }
    }
}
