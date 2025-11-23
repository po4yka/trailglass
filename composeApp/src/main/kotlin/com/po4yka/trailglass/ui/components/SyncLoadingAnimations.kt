package com.po4yka.trailglass.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.po4yka.trailglass.ui.theme.MorphableShapes
import com.po4yka.trailglass.ui.theme.animateShapeMorph
import com.po4yka.trailglass.ui.theme.expressiveShapeMorphSpring
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Material 3 Expressive-style loading indicator with wave animation. Features organic, fluid motion inspired by natural
 * water movements.
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
        animationSpec =
            infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
        label = "rotation"
    )

    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec =
            infiniteRepeatable(
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
        val path =
            androidx.compose.ui.graphics
                .Path()
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
            style =
                Stroke(
                    width = strokeWidth.toPx(),
                    cap = StrokeCap.Round
                )
        )
    }
}

/**
 * Shape-morphing loading indicator for Material 3 Expressive design. Cycles through different shapes: Circle → Diamond
 * → Square → Circle.
 *
 * Provides visual interest and confirms ongoing activity with organic motion.
 *
 * @param modifier Modifier for the indicator
 * @param size Size of the indicator
 * @param color Color of the indicator
 */
@Composable
fun MorphingLoadingIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "morphing_loading")

    // Cycle through shape states: 0 = Circle, 1 = Diamond, 2 = Square, 3 = Circle
    val shapeIndex by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 3f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(3000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
        label = "shape_cycle"
    )

    // Determine current target shape based on cycle
    val targetShape =
        when {
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
        modifier = modifier.size(size),
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
                animationSpec =
                    infiniteRepeatable(
                        animation = tween(1000, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                label = "pulse"
            )

            Surface(
                modifier =
                    Modifier
                        .fillMaxSize(scale)
                        .clip(morphedShape),
                color = color
            ) {}
        }
    }
}
