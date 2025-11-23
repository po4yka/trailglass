package com.po4yka.trailglass.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Enhanced circular progress indicator with spring-based size animation and fade in/out transitions.
 *
 * @param visible Whether the indicator is visible
 * @param modifier Modifier for the indicator
 * @param size Size of the indicator
 * @param color Color of the indicator (defaults to primary)
 */
@Composable
fun AnimatedLoadingIndicator(
    visible: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    color: Color = MaterialTheme.colorScheme.primary
) {
    AnimatedVisibility(
        visible = visible,
        enter =
            fadeIn(
                animationSpec =
                    spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
            ) +
                scaleIn(
                    animationSpec =
                        spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                ),
        exit =
            fadeOut(
                animationSpec =
                    spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
            ) +
                scaleOut(
                    animationSpec =
                        spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                )
    ) {
        CircularProgressIndicator(
            modifier = modifier.size(size),
            color = color
        )
    }
}

/**
 * Wavy linear progress indicator for route processing and file uploads. Features organic wave motion along the progress
 * bar.
 *
 * @param progress Current progress (0.0 to 1.0)
 * @param modifier Modifier for the indicator
 * @param color Color of the progress indicator (defaults to primary)
 * @param trackColor Color of the track (defaults to surfaceVariant)
 */
@Composable
fun WavyLinearProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            ),
        label = "progress"
    )

    LinearProgressIndicator(
        progress = { animatedProgress },
        modifier =
            modifier
                .fillMaxWidth()
                .height(6.dp),
        color = color,
        trackColor = trackColor
    )
}

/**
 * Contained loading indicator with background card and optional message. Useful for sync operations and photo uploads.
 *
 * @param visible Whether the indicator is visible
 * @param message Optional message to display
 * @param progress Optional progress value (0.0 to 1.0, null for indeterminate)
 * @param modifier Modifier for the container
 */
@Composable
fun ContainedLoadingIndicator(
    visible: Boolean,
    message: String? = null,
    progress: Float? = null,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut(),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 2.dp
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (progress != null) {
                    WavyLinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    WavyLoadingIndicator(
                        size = 32.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (message != null) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Sync operation loading indicator with icon and status message. Combines wavy animation with sync icon for visual
 * feedback.
 *
 * @param isActive Whether sync is currently active
 * @param message Status message to display
 * @param modifier Modifier for the component
 */
@Composable
fun SyncOperationIndicator(
    isActive: Boolean,
    message: String,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isActive,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut(),
        modifier = modifier
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(24.dp),
                contentAlignment = Alignment.Center
            ) {
                WavyLoadingIndicator(
                    size = 24.dp,
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.dp
                )
                Icon(
                    imageVector = Icons.Default.CloudSync,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Sync loading indicator with shape morphing. Enhanced version of standard sync indicator with organic shape changes.
 *
 * @param isActive Whether sync is active
 * @param modifier Modifier for the component
 */
@Composable
fun MorphingSyncIndicator(
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isActive,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut(),
        modifier = modifier
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MorphingLoadingIndicator(
                size = 32.dp,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Syncing...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
