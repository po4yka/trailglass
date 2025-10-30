package com.po4yka.trailglass.domain.model

/**
 * Sealed class representing different types of camera movements.
 *
 * This provides a command-based API for controlling map camera animations,
 * allowing platform-specific implementations to handle animations appropriately.
 */
sealed class CameraMove {
    /**
     * Instant camera movement with no animation.
     *
     * @param position The target camera position
     */
    data class Instant(val position: CameraPosition) : CameraMove()

    /**
     * Smooth easing animation to a new position.
     *
     * @param position The target camera position
     * @param durationMs Animation duration in milliseconds (default: 1000ms)
     */
    data class Ease(
        val position: CameraPosition,
        val durationMs: Int = 1000
    ) : CameraMove()

    /**
     * Fly-to animation with arc trajectory.
     *
     * Creates a smooth arc-like movement between two positions,
     * similar to Google Maps' "fly to" animation.
     *
     * @param position The target camera position
     * @param durationMs Animation duration in milliseconds (default: 2000ms)
     */
    data class Fly(
        val position: CameraPosition,
        val durationMs: Int = 2000
    ) : CameraMove()

    /**
     * Follow user's location in real-time.
     *
     * Camera will continuously track the user's current location.
     *
     * @param zoom Zoom level for following (default: 15f - street level)
     * @param tilt Camera tilt angle in degrees (default: 0f)
     * @param bearing Camera bearing in degrees (default: 0f - north up)
     */
    data class FollowUser(
        val zoom: Float = 15f,
        val tilt: Float = 0f,
        val bearing: Float = 0f
    ) : CameraMove()
}
