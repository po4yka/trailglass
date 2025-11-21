package com.po4yka.trailglass.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.po4yka.trailglass.feature.route.PlaybackSpeed
import com.po4yka.trailglass.feature.route.RouteReplayController
import com.po4yka.trailglass.ui.components.RouteReplayMapView

/**
 * Route Replay screen - full-screen animated playback of trip route.
 */
@Composable
fun RouteReplayScreen(
    tripId: String,
    controller: RouteReplayController,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by controller.state.collectAsState()

    // Load route on first composition
    LaunchedEffect(tripId) {
        controller.loadRoute(tripId)
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            state.isLoading -> {
                // Loading state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = "Loading replay...",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            state.error != null -> {
                // Error state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = state.error ?: "Unknown error",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(onClick = onBack) {
                                Text("Close")
                            }
                            Button(onClick = { controller.loadRoute(tripId) }) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Retry")
                            }
                        }
                    }
                }
            }

            state.tripRoute != null && state.vehicleState != null -> {
                // Replay content
                RouteReplayContent(
                    controller = controller,
                    onBack = onBack,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Close button (always visible)
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Close",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Main replay content with map and controls.
 */
@Composable
private fun RouteReplayContent(
    controller: RouteReplayController,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by controller.state.collectAsState()

    Box(modifier = modifier) {
        // Full-screen map with animated vehicle
        RouteReplayMapView(
            tripRoute = state.tripRoute!!,
            vehicleState = state.vehicleState!!,
            cameraPosition = state.cameraPosition!!,
            cameraBearing = state.cameraBearing,
            cameraTilt = state.cameraTilt,
            cameraZoom = state.cameraZoom,
            modifier = Modifier.fillMaxSize()
        )

        // Playback controls (bottom)
        AnimatedVisibility(
            visible = state.showControls,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            ReplayControlPanel(
                isPlaying = state.isPlaying,
                progress = state.progress,
                playbackSpeed = state.playbackSpeed,
                onPlayPauseClick = { controller.togglePlayPause() },
                onProgressChange = { controller.seekTo(it) },
                onSpeedClick = { controller.cyclePlaybackSpeed() },
                onRestartClick = { controller.restart() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }

        // Tap to toggle controls visibility
        Surface(
            onClick = { controller.toggleControls() },
            modifier = Modifier.fillMaxSize(),
            color = Color.Transparent
        ) {}
    }
}

/**
 * Replay control panel with play/pause, progress slider, and speed control.
 */
@Composable
private fun ReplayControlPanel(
    isPlaying: Boolean,
    progress: Float,
    playbackSpeed: PlaybackSpeed,
    onPlayPauseClick: () -> Unit,
    onProgressChange: (Float) -> Unit,
    onSpeedClick: () -> Unit,
    onRestartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Progress slider
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = formatProgress(progress),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.width(40.dp)
                )

                Slider(
                    value = progress,
                    onValueChange = onProgressChange,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "100%",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.width(40.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            // Control buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Restart button
                IconButton(onClick = onRestartClick) {
                    Icon(
                        Icons.Default.Replay,
                        contentDescription = "Restart",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                // Play/Pause button (larger)
                FilledIconButton(
                    onClick = onPlayPauseClick,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Speed control button
                OutlinedButton(
                    onClick = onSpeedClick,
                    modifier = Modifier.width(64.dp)
                ) {
                    Text(
                        text = playbackSpeed.displayName,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

/**
 * Format progress as percentage.
 */
private fun formatProgress(progress: Float): String {
    return "${(progress * 100).toInt()}%"
}
