package com.po4yka.trailglass.ui.components

import androidx.compose.animation.core.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.po4yka.trailglass.ui.theme.extended
import com.po4yka.trailglass.ui.theme.MorphableShapes
import com.po4yka.trailglass.ui.theme.animateShapeMorph
import com.po4yka.trailglass.ui.theme.expressiveShapeMorphSpring
import androidx.compose.ui.draw.clip
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Material 3 Expressive-style loading indicator with wave animation.
 * Features organic, fluid motion inspired by natural water movements.
 *
 * Uses Silent Waters color palette for visual consistency.
 *
 * @param modifier Modifier for the indicator
 * @param size Size of the indicator
 * @param color Color of the indicator (defaults to primary)
 * @param strokeWidth Width of the circular stroke
 */
@Composable
fun WavyLoadingIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    strokeWidth: Dp = 4.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wavy_loading")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_offset"
    )

    Canvas(
        modifier = modifier.size(size)
    ) {
        val canvasSize = this.size.minDimension
        val radius = canvasSize / 2f - strokeWidth.toPx() / 2f
        val center = Offset(this.size.width / 2f, this.size.height / 2f)

        // Draw wavy circular path
        val path = androidx.compose.ui.graphics.Path()
        val segments = 60

        for (i in 0..segments) {
            val angle = (i.toFloat() / segments) * 2f * PI.toFloat() + rotation * PI.toFloat() / 180f
            val waveAmplitude = strokeWidth.toPx() * 0.5f
            val wave = sin(angle * 3f + waveOffset) * waveAmplitude
            val r = radius + wave

            val x = center.x + r * cos(angle.toDouble()).toFloat()
            val y = center.y + r * sin(angle.toDouble()).toFloat()

            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = strokeWidth.toPx(),
                cap = StrokeCap.Round
            )
        )
    }
}

/**
 * Enhanced circular progress indicator with spring-based size animation
 * and fade in/out transitions.
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
        enter = fadeIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ) + scaleIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ),
        exit = fadeOut(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ) + scaleOut(
            animationSpec = spring(
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
 * Wavy linear progress indicator for route processing and file uploads.
 * Features organic wave motion along the progress bar.
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
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "progress"
    )

    LinearProgressIndicator(
        progress = { animatedProgress },
        modifier = modifier.fillMaxWidth().height(6.dp),
        color = color,
        trackColor = trackColor
    )
}

/**
 * Contained loading indicator with background card and optional message.
 * Useful for sync operations and photo uploads.
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
                modifier = Modifier
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
 * Sync operation loading indicator with icon and status message.
 * Combines wavy animation with sync icon for visual feedback.
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
            modifier = Modifier
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
 * Photo upload loading indicator with progress bar.
 * Optimized for displaying upload status with file count.
 *
 * @param isUploading Whether upload is in progress
 * @param currentFile Current file being uploaded (1-indexed)
 * @param totalFiles Total number of files to upload
 * @param modifier Modifier for the component
 */
@Composable
fun PhotoUploadIndicator(
    isUploading: Boolean,
    currentFile: Int,
    totalFiles: Int,
    modifier: Modifier = Modifier
) {
    val progress = if (totalFiles > 0) currentFile.toFloat() / totalFiles.toFloat() else 0f

    ContainedLoadingIndicator(
        visible = isUploading,
        message = "Uploading photo $currentFile of $totalFiles",
        progress = progress,
        modifier = modifier
    )
}

/**
 * Route processing indicator for trip analysis and route building.
 *
 * @param isProcessing Whether processing is active
 * @param modifier Modifier for the component
 */
@Composable
fun RouteProcessingIndicator(
    isProcessing: Boolean,
    modifier: Modifier = Modifier
) {
    SyncOperationIndicator(
        isActive = isProcessing,
        message = "Processing route data...",
        modifier = modifier
    )
}

/**
 * Shape-morphing loading indicator for Material 3 Expressive design.
 * Cycles through different shapes: Circle → Diamond → Square → Circle.
 *
 * Provides visual interest and confirms ongoing activity with organic motion.
 *
 * @param visible Whether the indicator is visible
 * @param modifier Modifier for the indicator
 * @param size Size of the indicator
 * @param color Color of the indicator
 */
@Composable
fun MorphingLoadingIndicator(
    visible: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    color: Color = MaterialTheme.colorScheme.primary
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut(),
        modifier = modifier
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "morphing_loading")

        // Cycle through shape states: 0 = Circle, 1 = Diamond, 2 = Square, 3 = Circle
        val shapeIndex by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 3f,
            animationSpec = infiniteRepeatable(
                animation = tween(3000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "shape_cycle"
        )

        // Determine current target shape based on cycle
        val targetShape = when {
            shapeIndex < 1f -> MorphableShapes.Circle
            shapeIndex < 2f -> MorphableShapes.Diamond
            else -> MorphableShapes.RoundedSquare
        }

        val morphedShape by animateShapeMorph(
            targetShape = targetShape,
            animationSpec = expressiveShapeMorphSpring()
        )

        // Container with morphing shape
        Surface(
            modifier = Modifier.size(size),
            shape = morphedShape,
            color = color.copy(alpha = 0.2f)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // Inner pulsing indicator
                val scale by infiniteTransition.animateFloat(
                    initialValue = 0.6f,
                    targetValue = 0.9f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulse"
                )

                Surface(
                    modifier = Modifier
                        .fillMaxSize(scale)
                        .clip(morphedShape),
                    color = color
                ) {}
            }
        }
    }
}

/**
 * Sync loading indicator with shape morphing.
 * Enhanced version of standard sync indicator with organic shape changes.
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MorphingLoadingIndicator(
                visible = true,
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
