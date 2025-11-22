package com.po4yka.trailglass.domain.algorithm

import com.po4yka.trailglass.domain.model.Coordinate
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.doubles.shouldBeLessThan
import io.kotest.matchers.shouldBe
import kotlin.math.abs
import kotlin.test.Test

class InterpolationAlgorithmTest {
    private val start = Coordinate(40.0, -74.0)
    private val end = Coordinate(41.0, -73.0)

    @Test
    fun `linear interpolation at 0 should return start point`() {
        val algorithm = LinearInterpolation()
        val result = algorithm.interpolate(start, end, 0.0)

        result.latitude shouldBe start.latitude
        result.longitude shouldBe start.longitude
    }

    @Test
    fun `linear interpolation at 1 should return end point`() {
        val algorithm = LinearInterpolation()
        val result = algorithm.interpolate(start, end, 1.0)

        result.latitude shouldBe end.latitude
        result.longitude shouldBe end.longitude
    }

    @Test
    fun `linear interpolation at 0_5 should return midpoint`() {
        val algorithm = LinearInterpolation()
        val result = algorithm.interpolate(start, end, 0.5)

        val expectedLat = (start.latitude + end.latitude) / 2
        val expectedLon = (start.longitude + end.longitude) / 2

        result.latitude shouldBe expectedLat
        result.longitude shouldBe expectedLon
    }

    @Test
    fun `linear interpolation should clamp fraction to 0-1 range`() {
        val algorithm = LinearInterpolation()

        val below = algorithm.interpolate(start, end, -0.5)
        val above = algorithm.interpolate(start, end, 1.5)

        below.latitude shouldBe start.latitude
        above.latitude shouldBe end.latitude
    }

    @Test
    fun `spherical interpolation at 0 should return start point`() {
        val algorithm = SphericalInterpolation()
        val result = algorithm.interpolate(start, end, 0.0)

        abs(result.latitude - start.latitude) shouldBeLessThan 0.0001
        abs(result.longitude - start.longitude) shouldBeLessThan 0.0001
    }

    @Test
    fun `spherical interpolation at 1 should return end point`() {
        val algorithm = SphericalInterpolation()
        val result = algorithm.interpolate(start, end, 1.0)

        abs(result.latitude - end.latitude) shouldBeLessThan 0.0001
        abs(result.longitude - end.longitude) shouldBeLessThan 0.0001
    }

    @Test
    fun `spherical interpolation should follow great circle`() {
        val algorithm = SphericalInterpolation()
        val distanceAlg = HaversineDistance()

        val mid = algorithm.interpolate(start, end, 0.5)

        // Distance from start to mid should be approximately half total distance
        val dist1 = distanceAlg.calculate(start, mid)
        val dist2 = distanceAlg.calculate(mid, end)
        val totalDist = distanceAlg.calculate(start, end)

        abs(dist1 - totalDist / 2) shouldBeLessThan (totalDist * 0.01) // Within 1%
        abs(dist2 - totalDist / 2) shouldBeLessThan (totalDist * 0.01)
    }

    @Test
    fun `spherical interpolation should differ from linear for long distances`() {
        val linear = LinearInterpolation()
        val spherical = SphericalInterpolation()

        // Long distance where linear and spherical differ significantly
        val newYork = Coordinate(40.7128, -74.0060)
        val london = Coordinate(51.5074, -0.1278)

        val linearMid = linear.interpolate(newYork, london, 0.5)
        val sphericalMid = spherical.interpolate(newYork, london, 0.5)

        // Spherical should follow great circle (northern arc), linear doesn't
        sphericalMid.latitude shouldBeGreaterThan linearMid.latitude
    }

    @Test
    fun `spherical interpolation should handle antipodal points`() {
        val algorithm = SphericalInterpolation()

        val point1 = Coordinate(0.0, 0.0)
        val point2 = Coordinate(0.0, 180.0)

        // Should not throw error
        val mid = algorithm.interpolate(point1, point2, 0.5)

        // Midpoint should be at 90° or -90° longitude
        abs(mid.longitude - 90.0) shouldBeLessThan 1.0
    }

    @Test
    fun `cubic interpolation at 0 should return start point`() {
        val algorithm = CubicInterpolation()
        val result = algorithm.interpolate(start, end, 0.0)

        result.latitude shouldBe start.latitude
        result.longitude shouldBe start.longitude
    }

    @Test
    fun `cubic interpolation at 1 should return end point`() {
        val algorithm = CubicInterpolation()
        val result = algorithm.interpolate(start, end, 1.0)

        result.latitude shouldBe end.latitude
        result.longitude shouldBe end.longitude
    }

    @Test
    fun `cubic interpolation should create smooth curve`() {
        val algorithm = CubicInterpolation()

        val points =
            (0..10).map { i ->
                algorithm.interpolate(start, end, i / 10.0)
            }

        // Path should be monotonic (always moving toward destination)
        for (i in 1 until points.size) {
            points[i].latitude shouldBeGreaterThan points[i - 1].latitude
            points[i].longitude shouldBeGreaterThan points[i - 1].longitude
        }
    }

    @Test
    fun `cubic interpolation should have smooth acceleration and deceleration`() {
        val algorithm = CubicInterpolation()
        val distanceAlg = HaversineDistance()

        // Use longer distance to make differences more pronounced
        val longStart = Coordinate(40.0, -74.0)
        val longEnd = Coordinate(50.0, -64.0)

        // Generate path with more steps
        val points =
            (0..20).map { i ->
                algorithm.interpolate(longStart, longEnd, i / 20.0)
            }

        // Calculate segment distances
        val segments =
            (1 until points.size).map { i ->
                distanceAlg.calculate(points[i - 1], points[i])
            }

        // Average middle segments should be longer than edge segments
        val firstThird = segments.take(segments.size / 3).average()
        val middleThird = segments.drop(segments.size / 3).take(segments.size / 3).average()
        val lastThird = segments.takeLast(segments.size / 3).average()

        // Middle section should have faster movement
        middleThird shouldBeGreaterThan (firstThird * 0.9) // Allow some tolerance
        middleThird shouldBeGreaterThan (lastThird * 0.9)
    }

    @Test
    fun `generatePath should create correct number of points`() {
        val algorithm = LinearInterpolation()

        val path = algorithm.generatePath(start, end, steps = 5)

        // Should include start, end, and 5 intermediate points = 7 total
        path shouldHaveSize 7
        path.first() shouldBe start
        path.last() shouldBe end
    }

    @Test
    fun `generatePath with zero steps should return start and end only`() {
        val algorithm = LinearInterpolation()

        val path = algorithm.generatePath(start, end, steps = 0)

        path shouldHaveSize 2
        path.first() shouldBe start
        path.last() shouldBe end
    }

    @Test
    fun `all interpolation algorithms should handle same start and end`() {
        val linear = LinearInterpolation()
        val spherical = SphericalInterpolation()
        val cubic = CubicInterpolation()

        val point = Coordinate(40.0, -74.0)

        val linearResult = linear.interpolate(point, point, 0.5)
        val sphericalResult = spherical.interpolate(point, point, 0.5)
        val cubicResult = cubic.interpolate(point, point, 0.5)

        linearResult.latitude shouldBe point.latitude
        sphericalResult.latitude shouldBe point.latitude
        cubicResult.latitude shouldBe point.latitude
    }

    @Test
    fun `spherical interpolation should be more accurate than linear for globe`() {
        val linear = LinearInterpolation()
        val spherical = SphericalInterpolation()
        val distanceAlg = HaversineDistance()

        // Use east-west path where spherical and linear differ significantly
        val west = Coordinate(40.0, -100.0)
        val east = Coordinate(40.0, 100.0)

        // Calculate path distances with more steps for accuracy
        val linearPath = linear.generatePath(west, east, steps = 20)
        val sphericalPath = spherical.generatePath(west, east, steps = 20)

        // Sum segment distances
        fun pathLength(path: List<Coordinate>): Double =
            (1 until path.size).sumOf { i ->
                distanceAlg.calculate(path[i - 1], path[i])
            }

        val linearDist = pathLength(linearPath)
        val sphericalDist = pathLength(sphericalPath)
        val directDist = distanceAlg.calculate(west, east)

        // Spherical should be much closer to actual great circle distance
        val sphericalError = abs(sphericalDist - directDist) / directDist
        val linearError = abs(linearDist - directDist) / directDist

        sphericalError shouldBeLessThan linearError
    }

    @Test
    fun `factory should create correct algorithm instances`() {
        val linear = InterpolationAlgorithmFactory.create(InterpolationAlgorithmType.LINEAR)
        val slerp = InterpolationAlgorithmFactory.create(InterpolationAlgorithmType.SLERP)
        val cubic = InterpolationAlgorithmFactory.create(InterpolationAlgorithmType.CUBIC)

        linear::class shouldBe LinearInterpolation::class
        slerp::class shouldBe SphericalInterpolation::class
        cubic::class shouldBe CubicInterpolation::class
    }
}
