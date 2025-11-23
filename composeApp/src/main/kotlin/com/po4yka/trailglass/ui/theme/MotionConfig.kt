package com.po4yka.trailglass.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring

/**
 * Motion configuration for Material 3 Expressive animations.
 *
 * Material 3 Expressive introduces enhanced motion schemes with spring-based animations that provide more natural and
 * delightful user experiences.
 *
 * This object provides predefined spring configurations aligned with Material 3 Expressive motion guidelines for
 * different use cases.
 *
 * Note: When Material3 1.5.0 becomes stable, consider migrating to MotionScheme.expressive() for built-in expressive
 * motion support.
 */
object MotionConfig {
    /**
     * Standard spring animation for general UI transitions.
     *
     * Use for: Most UI transitions, state changes, and general animations. Characteristics: Balanced damping, medium
     * stiffness, smooth feel.
     */
    inline fun <reified T> standardSpring() =
        spring<T>(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )

    /**
     * Expressive spring animation with more bounce for emphasis.
     *
     * Use for: Hero transitions, important state changes, attention-grabbing animations. Characteristics: Lower damping
     * for more bounce, creates playful, expressive feel.
     */
    inline fun <reified T> expressiveSpring() =
        spring<T>(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        )

    /**
     * Quick spring animation for fast, responsive interactions.
     *
     * Use for: User input feedback, quick toggle animations, immediate responses. Characteristics: High stiffness,
     * quick response, minimal overshoot.
     */
    inline fun <reified T> quickSpring() =
        spring<T>(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        )

    /**
     * Slow spring animation for dramatic, deliberate transitions.
     *
     * Use for: Large layout changes, screen transitions, modal presentations. Characteristics: Low stiffness, slower
     * motion, emphasizes the transition.
     */
    inline fun <reified T> slowSpring() =
        spring<T>(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )

    /**
     * Smooth spring animation with no bounce for subtle transitions.
     *
     * Use for: Fade animations, size changes, professional/formal contexts. Characteristics: No bounce (high damping),
     * smooth easing-like motion.
     */
    inline fun <reified T> smoothSpring() =
        spring<T>(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        )

    /**
     * High bounce spring for playful, attention-grabbing animations.
     *
     * Use for: Celebration animations, success states, gamification elements. Characteristics: Very low damping,
     * prominent bounce, playful feel.
     */
    inline fun <reified T> bouncySpring() =
        spring<T>(
            dampingRatio = 0.5f, // Custom value for high bounce
            stiffness = Spring.StiffnessMedium
        )
}

// Extension functions for applying motion configs to common animation scenarios.

/** Creates a spring spec for offset animations (position changes). */
fun MotionConfig.offsetSpring() =
    spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

/** Creates a spring spec for scale animations. */
fun MotionConfig.scaleSpring() =
    spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMedium
    )

/** Creates a spring spec for alpha (opacity) animations. */
fun MotionConfig.alphaSpring() =
    spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessHigh
    )

/** Creates a spring spec for rotation animations. */
fun MotionConfig.rotationSpring() =
    spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )
