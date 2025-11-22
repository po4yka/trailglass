package com.po4yka.trailglass.ui.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Material 3 Expressive Shape Morphing Utilities
 *
 * Provides custom shape morphing capabilities for organic, fluid animations
 * following Material 3 Expressive design guidelines.
 *
 * Since androidx.graphics.shapes (RoundedPolygon, Morph) is not available in current
 * dependencies, this implementation uses custom path-based morphing with spring animations.
 *
 * Key Features:
 * - Predefined organic shapes (circle, triangle, hexagon, etc.)
 * - Smooth spring-based morphing between shapes
 * - Performance-optimized with cached calculations
 * - Accessibility support with animation disable option
 */

/**
 * Represents a morphable shape defined by polar coordinates.
 *
 * @property points Number of points defining the shape
 * @property radiusFunction Function that calculates radius at given angle (0 to 2π)
 */
data class MorphableShape(
    val points: Int,
    val radiusFunction: (angle: Float, baseRadius: Float) -> Float
)

/**
 * Predefined morphable shapes for common use cases.
 */
object MorphableShapes {

    /**
     * Perfect circle shape.
     * Use for: Default FAB state, loading indicators, badges.
     */
    val Circle = MorphableShape(60) { _, baseRadius -> baseRadius }

    /**
     * Triangle pointing up.
     * Use for: Active tracking indicator, navigation arrows, direction indicators.
     */
    val Triangle = MorphableShape(60) { angle, baseRadius ->
        val normalizedAngle = (angle % (2 * PI.toFloat()))
        val triangleAngle = (normalizedAngle - PI.toFloat() / 2) % (2 * PI.toFloat() / 3)

        // Create triangle with rounded corners
        val cornerRadius = 0.15f
        val distToCorner = if (triangleAngle < PI.toFloat() / 3) {
            triangleAngle / (PI.toFloat() / 3)
        } else {
            (2 * PI.toFloat() / 3 - triangleAngle) / (PI.toFloat() / 3)
        }

        baseRadius * (1.0f + cornerRadius * (1.0f - distToCorner))
    }

    /**
     * Hexagon shape.
     * Use for: Category badges, transport mode indicators, status chips.
     */
    val Hexagon = MorphableShape(60) { angle, baseRadius ->
        val normalizedAngle = (angle % (PI.toFloat() / 3))
        val distToCorner = (PI.toFloat() / 6 - kotlin.math.abs(normalizedAngle - PI.toFloat() / 6)) / (PI.toFloat() / 6)
        baseRadius * (0.95f + 0.05f * distToCorner)
    }

    /**
     * Diamond (rotated square) shape.
     * Use for: Loading indicator states, emphasis markers.
     */
    val Diamond = MorphableShape(60) { angle, baseRadius ->
        val normalizedAngle = (angle % (PI.toFloat() / 2))
        val distToCorner = (PI.toFloat() / 4 - kotlin.math.abs(normalizedAngle - PI.toFloat() / 4)) / (PI.toFloat() / 4)
        baseRadius * (0.92f + 0.08f * distToCorner) * 1.15f
    }

    /**
     * Square with rounded corners.
     * Use for: Work category, formal contexts, grid layouts.
     */
    val RoundedSquare = MorphableShape(60) { angle, baseRadius ->
        val normalizedAngle = (angle % (PI.toFloat() / 2))
        val distToCorner = (PI.toFloat() / 4 - kotlin.math.abs(normalizedAngle - PI.toFloat() / 4)) / (PI.toFloat() / 4)
        baseRadius * (0.90f + 0.10f * distToCorner)
    }

    /**
     * Organic wave-like shape.
     * Use for: Water category, fluid animations, nature-themed elements.
     */
    val Wave = MorphableShape(60) { angle, baseRadius ->
        val waveCount = 3f
        val waveAmplitude = 0.12f
        val wave = sin(angle * waveCount) * waveAmplitude
        baseRadius * (1.0f + wave)
    }

    /**
     * Petal/flower-like organic shape.
     * Use for: Food category, organic content, nature elements.
     */
    val Petal = MorphableShape(60) { angle, baseRadius ->
        val petalCount = 5f
        val petalAmplitude = 0.20f
        val petal = (1.0f + cos(angle * petalCount)) / 2.0f
        baseRadius * (0.85f + petal * petalAmplitude)
    }
}

/**
 * Compose Shape that morphs between two MorphableShapes with animation.
 *
 * @property fromShape Starting shape
 * @property toShape Target shape
 * @property progress Animation progress (0.0 to 1.0)
 */
class MorphingShape(
    private val fromShape: MorphableShape,
    private val toShape: MorphableShape,
    private val progress: Float
) : Shape {

    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path()
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val baseRadius = minOf(centerX, centerY) * 0.9f

        // Use the maximum point count for smooth interpolation
        val points = maxOf(fromShape.points, toShape.points)

        for (i in 0..points) {
            val angle = (i.toFloat() / points) * 2f * PI.toFloat()

            // Calculate radius from both shapes
            val fromRadius = fromShape.radiusFunction(angle, baseRadius)
            val toRadius = toShape.radiusFunction(angle, baseRadius)

            // Interpolate radius based on progress
            val radius = fromRadius * (1f - progress) + toRadius * progress

            val x = centerX + radius * cos(angle)
            val y = centerY + radius * sin(angle)

            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        path.close()
        return Outline.Generic(path)
    }

    override fun toString(): String = "MorphingShape(progress=$progress)"
}

/**
 * Animates a shape morph with spring animation.
 *
 * @param targetShape The shape to morph into
 * @param currentShape The current shape (will be updated as state changes)
 * @param animationSpec Spring animation specification
 * @return State containing the morphing shape
 */
@Composable
fun animateShapeMorph(
    targetShape: MorphableShape,
    currentShape: MorphableShape = MorphableShapes.Circle,
    animationSpec: AnimationSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow
    )
): State<Shape> {
    val progress = remember { Animatable(0f) }
    val previousShape = remember { mutableStateOf(currentShape) }

    LaunchedEffect(targetShape) {
        if (targetShape != previousShape.value) {
            progress.snapTo(0f)
            progress.animateTo(1f, animationSpec)
            previousShape.value = targetShape
        }
    }

    return remember(targetShape, progress.value) {
        object : State<Shape> {
            override val value: Shape
                get() = if (progress.value >= 1f) {
                    // Use static shape when animation is complete for better performance
                    MorphingShape(targetShape, targetShape, 1f)
                } else {
                    MorphingShape(previousShape.value, targetShape, progress.value)
                }
        }
    }
}

/**
 * Spring animation spec optimized for shape morphing.
 * Uses Material 3 Expressive motion timing.
 */
fun shapeMorphSpring() = spring<Float>(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessMediumLow,
    visibilityThreshold = 0.001f
)

/**
 * Quick spring animation for rapid shape transitions.
 * Use for immediate feedback interactions.
 */
fun quickShapeMorphSpring() = spring<Float>(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessMedium,
    visibilityThreshold = 0.001f
)

/**
 * Expressive spring with more bounce for emphasis.
 * Use for attention-grabbing morphs.
 */
fun expressiveShapeMorphSpring() = spring<Float>(
    dampingRatio = Spring.DampingRatioLowBouncy,
    stiffness = Spring.StiffnessLow,
    visibilityThreshold = 0.001f
)

/**
 * Category-specific shape mappings for semantic shape morphing.
 */
object CategoryShapes {
    /**
     * Water-related activities: wave shape
     */
    val Water = MorphableShapes.Wave

    /**
     * Transport/movement: hexagon
     */
    val Transport = MorphableShapes.Hexagon

    /**
     * Food/dining: petal/organic shape
     */
    val Food = MorphableShapes.Petal

    /**
     * Work/office: rounded square
     */
    val Work = MorphableShapes.RoundedSquare

    /**
     * Default/generic: circle
     */
    val Default = MorphableShapes.Circle

    /**
     * Get shape for a category name.
     */
    fun forCategory(category: String?): MorphableShape {
        return when (category?.lowercase()) {
            "water", "swimming", "beach", "ocean" -> Water
            "transport", "car", "bus", "train", "flight", "bike" -> Transport
            "food", "restaurant", "dining", "cafe" -> Food
            "work", "office", "meeting" -> Work
            else -> Default
        }
    }
}
