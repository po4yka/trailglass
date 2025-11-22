package com.po4yka.trailglass.domain.algorithm

import com.po4yka.trailglass.domain.model.Coordinate
import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.doubles.shouldBeLessThan
import io.kotest.matchers.shouldBe
import kotlin.math.abs
import kotlin.test.Test

class DistanceAlgorithmTest {
    // Test coordinates
    private val newYork = Coordinate(40.7128, -74.0060)
    private val london = Coordinate(51.5074, -0.1278)
    private val sydney = Coordinate(-33.8688, 151.2093)
    private val tokyo = Coordinate(35.6762, 139.6503)

    // Expected distances (in meters, approximate)
    private val newYorkToLondon = 5570_000.0 // ~5,570 km
    private val newYorkToSydney = 15_990_000.0 // ~15,990 km
    private val londonToTokyo = 9590_000.0 // ~9,590 km

    @Test
    fun `haversine distance should calculate NYC to London correctly`() {
        val algorithm = HaversineDistance()
        val distance = algorithm.calculate(newYork, london)

        // Should be within 1% of expected
        val errorMargin = newYorkToLondon * 0.01
        abs(distance - newYorkToLondon) shouldBeLessThan errorMargin
    }

    @Test
    fun `haversine distance should calculate NYC to Sydney correctly`() {
        val algorithm = HaversineDistance()
        val distance = algorithm.calculate(newYork, sydney)

        val errorMargin = newYorkToSydney * 0.01
        abs(distance - newYorkToSydney) shouldBeLessThan errorMargin
    }

    @Test
    fun `haversine distance should be zero for same point`() {
        val algorithm = HaversineDistance()
        val distance = algorithm.calculate(newYork, newYork)

        distance shouldBe 0.0
    }

    @Test
    fun `haversine distance should be symmetric`() {
        val algorithm = HaversineDistance()
        val dist1 = algorithm.calculate(newYork, london)
        val dist2 = algorithm.calculate(london, newYork)

        dist1 shouldBe dist2
    }

    @Test
    fun `vincenty distance should be more accurate than haversine`() {
        val haversine = HaversineDistance()
        val vincenty = VincentyDistance()

        val haversineDist = haversine.calculate(newYork, sydney)
        val vincentyDist = vincenty.calculate(newYork, sydney)

        // Vincenty should give slightly different (more accurate) result
        // But both should be close (within 0.5% typically)
        val diff = abs(haversineDist - vincentyDist)
        diff shouldBeLessThan (newYorkToSydney * 0.005)
    }

    @Test
    fun `vincenty distance should handle antipodal points`() {
        val algorithm = VincentyDistance()

        // Opposite sides of Earth (should be ~20,000 km)
        val point1 = Coordinate(0.0, 0.0)
        val point2 = Coordinate(0.0, 180.0)

        val distance = algorithm.calculate(point1, point2)

        // Should be approximately half Earth's circumference
        distance shouldBeGreaterThan 19_000_000.0
        distance shouldBeLessThan 21_000_000.0
    }

    @Test
    fun `simple distance should work for short distances`() {
        val algorithm = SimpleDistance()

        // Two points 1km apart (approximately)
        val point1 = Coordinate(40.0, -74.0)
        val point2 = Coordinate(40.009, -74.0) // ~1 km north

        val distance = algorithm.calculate(point1, point2)

        // Should be approximately 1000 meters
        abs(distance - 1000.0) shouldBeLessThan 100.0 // Within 10% for short distance
    }

    @Test
    fun `simple distance should have larger error for long distances`() {
        val simple = SimpleDistance()
        val haversine = HaversineDistance()

        val simpleDist = simple.calculate(newYork, sydney)
        val haversineDist = haversine.calculate(newYork, sydney)

        // Error should be significant (>5%) for very long distances
        val error = abs(simpleDist - haversineDist) / haversineDist
        error shouldBeGreaterThan 0.05 // >5% error
    }

    @Test
    fun `all algorithms should handle equator crossing`() {
        val point1 = Coordinate(10.0, 0.0) // Northern hemisphere
        val point2 = Coordinate(-10.0, 0.0) // Southern hemisphere

        val haversine = HaversineDistance().calculate(point1, point2)
        val vincenty = VincentyDistance().calculate(point1, point2)

        // Should be approximately 2,220 km
        haversine shouldBeGreaterThan 2_200_000.0
        haversine shouldBeLessThan 2_250_000.0

        // Both Haversine and Vincenty should give reasonable results
        vincenty shouldBeGreaterThan 2_200_000.0
        vincenty shouldBeLessThan 2_250_000.0
    }

    @Test
    fun `all algorithms should handle prime meridian crossing`() {
        val point1 = Coordinate(51.5, -0.5) // West of Greenwich
        val point2 = Coordinate(51.5, 0.5) // East of Greenwich

        val haversine = HaversineDistance().calculate(point1, point2)
        val vincenty = VincentyDistance().calculate(point1, point2)

        // Should be approximately 70 km
        haversine shouldBeGreaterThan 65_000.0
        haversine shouldBeLessThan 75_000.0

        abs(vincenty - haversine) shouldBeLessThan 500.0 // Within 500m
    }

    @Test
    fun `factory should create correct algorithm instances`() {
        val haversine = DistanceAlgorithmFactory.create(DistanceAlgorithmType.HAVERSINE)
        val vincenty = DistanceAlgorithmFactory.create(DistanceAlgorithmType.VINCENTY)
        val simple = DistanceAlgorithmFactory.create(DistanceAlgorithmType.SIMPLE)

        haversine::class shouldBe HaversineDistance::class
        vincenty::class shouldBe VincentyDistance::class
        simple::class shouldBe SimpleDistance::class
    }
}
