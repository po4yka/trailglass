package com.po4yka.trailglass.domain.model

import kotlin.math.*

/**
 * Utility for calculating arc trajectories for camera animations.
 *
 * Provides smooth, visually appealing camera movements that zoom out,
 * follow a curved path, and zoom back in - similar to Google Maps' fly-to animation.
 */
object ArcTrajectoryCalculator {

    /**
     * Calculate intermediate camera positions for an arc trajectory.
     *
     * Creates a smooth arc that:
     * 1. Zooms out to see both start and end points
     * 2. Follows a curved path (great circle)
     * 3. Zooms back in to the target position
     *
     * @param start Starting camera position
     * @param end Ending camera position
     * @param steps Number of intermediate steps (default: 20 for smooth animation)
     * @return List of camera positions forming an arc trajectory
     */
    fun calculateArcTrajectory(
        start: CameraPosition,
        end: CameraPosition,
        steps: Int = 20
    ): List<CameraPosition> {
        if (steps < 2) return listOf(start, end)

        val distance = calculateDistance(start.target, end.target)

        // Calculate apex zoom (how much to zoom out)
        // Larger distances require more zoom out
        val apexZoomDelta = calculateApexZoomDelta(distance, start.zoom, end.zoom)
        val apexZoom = min(start.zoom, end.zoom) - apexZoomDelta

        val positions = mutableListOf<CameraPosition>()

        for (i in 0..steps) {
            val fraction = i.toDouble() / steps

            // Calculate interpolated coordinate along great circle
            val coordinate = interpolateCoordinate(start.target, end.target, fraction)

            // Calculate zoom along parabolic curve (zoom out in middle)
            val zoom = calculateParabolicZoom(
                startZoom = start.zoom,
                endZoom = end.zoom,
                apexZoom = apexZoom,
                fraction = fraction
            )

            // Interpolate tilt and bearing
            val tilt = interpolateLinear(start.tilt, end.tilt, fraction)
            val bearing = interpolateAngular(start.bearing, end.bearing, fraction)

            positions.add(
                CameraPosition(
                    target = coordinate,
                    zoom = zoom,
                    tilt = tilt,
                    bearing = bearing
                )
            )
        }

        return positions
    }

    /**
     * Calculate distance between two coordinates in degrees.
     * Uses haversine formula for great circle distance.
     *
     * @param start Starting coordinate
     * @param end Ending coordinate
     * @return Distance in degrees
     */
    private fun calculateDistance(start: Coordinate, end: Coordinate): Double {
        val lat1 = Math.toRadians(start.latitude)
        val lat2 = Math.toRadians(end.latitude)
        val dLat = Math.toRadians(end.latitude - start.latitude)
        val dLon = Math.toRadians(end.longitude - start.longitude)

        val a = sin(dLat / 2).pow(2) +
                cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return Math.toDegrees(c)
    }

    /**
     * Calculate how much to zoom out at the apex of the arc.
     *
     * @param distance Distance between points in degrees
     * @param startZoom Starting zoom level
     * @param endZoom Ending zoom level
     * @return Zoom delta (amount to zoom out)
     */
    private fun calculateApexZoomDelta(
        distance: Double,
        startZoom: Float,
        endZoom: Float
    ): Float {
        // More zoom out for larger distances
        return when {
            distance > 10.0 -> 5f   // Continental distance
            distance > 5.0 -> 4f    // Large region
            distance > 1.0 -> 3f    // City-to-city
            distance > 0.5 -> 2f    // District-to-district
            distance > 0.1 -> 1.5f  // Neighborhood
            else -> 1f              // Street level
        }
    }

    /**
     * Interpolate coordinate along great circle path.
     *
     * Uses spherical linear interpolation (slerp) for smooth great circle path.
     *
     * @param start Starting coordinate
     * @param end Ending coordinate
     * @param fraction Progress from 0.0 to 1.0
     * @return Interpolated coordinate
     */
    private fun interpolateCoordinate(
        start: Coordinate,
        end: Coordinate,
        fraction: Double
    ): Coordinate {
        val lat1 = Math.toRadians(start.latitude)
        val lon1 = Math.toRadians(start.longitude)
        val lat2 = Math.toRadians(end.latitude)
        val lon2 = Math.toRadians(end.longitude)

        // Calculate great circle distance
        val d = acos(
            sin(lat1) * sin(lat2) +
            cos(lat1) * cos(lat2) * cos(lon2 - lon1)
        )

        if (d < 0.001) {
            // Points are very close, use linear interpolation
            return Coordinate(
                latitude = start.latitude + (end.latitude - start.latitude) * fraction,
                longitude = start.longitude + (end.longitude - start.longitude) * fraction
            )
        }

        // Spherical interpolation
        val a = sin((1 - fraction) * d) / sin(d)
        val b = sin(fraction * d) / sin(d)

        val x = a * cos(lat1) * cos(lon1) + b * cos(lat2) * cos(lon2)
        val y = a * cos(lat1) * sin(lon1) + b * cos(lat2) * sin(lon2)
        val z = a * sin(lat1) + b * sin(lat2)

        val latResult = atan2(z, sqrt(x * x + y * y))
        val lonResult = atan2(y, x)

        return Coordinate(
            latitude = Math.toDegrees(latResult),
            longitude = Math.toDegrees(lonResult)
        )
    }

    /**
     * Calculate zoom level along parabolic curve.
     *
     * Creates a smooth zoom out/in effect with apex in the middle.
     *
     * @param startZoom Starting zoom level
     * @param endZoom Ending zoom level
     * @param apexZoom Zoom level at apex (middle)
     * @param fraction Progress from 0.0 to 1.0
     * @return Interpolated zoom level
     */
    private fun calculateParabolicZoom(
        startZoom: Float,
        endZoom: Float,
        apexZoom: Float,
        fraction: Double
    ): Float {
        // Use parabolic interpolation for smooth zoom curve
        // The parabola peaks (or valleys) at fraction = 0.5
        val t = fraction.toFloat()

        // Parabolic blend: zoom out to apex in first half, zoom in during second half
        val parabolicFactor = 1 - 4 * (t - 0.5f).pow(2) // Peaks at t=0.5

        // Blend between linear interpolation and apex zoom
        val linearZoom = startZoom + (endZoom - startZoom) * t
        val targetApex = min(startZoom, endZoom)
        val apexInfluence = (apexZoom - targetApex) * parabolicFactor

        return linearZoom + apexInfluence
    }

    /**
     * Linear interpolation between two values.
     *
     * @param start Starting value
     * @param end Ending value
     * @param fraction Progress from 0.0 to 1.0
     * @return Interpolated value
     */
    private fun interpolateLinear(start: Float, end: Float, fraction: Double): Float {
        return start + (end - start) * fraction.toFloat()
    }

    /**
     * Angular interpolation (handles wrapping at 0/360 degrees).
     *
     * @param start Starting angle in degrees
     * @param end Ending angle in degrees
     * @param fraction Progress from 0.0 to 1.0
     * @return Interpolated angle
     */
    private fun interpolateAngular(start: Float, end: Float, fraction: Double): Float {
        // Normalize angles to 0-360
        val startNorm = (start % 360 + 360) % 360
        val endNorm = (end % 360 + 360) % 360

        // Calculate shortest angular distance
        var delta = endNorm - startNorm
        if (delta > 180) delta -= 360
        if (delta < -180) delta += 360

        return (startNorm + delta * fraction.toFloat()) % 360
    }
}
