package com.po4yka.trailglass.domain.algorithm

import com.po4yka.trailglass.domain.model.Coordinate
import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.doubles.shouldBeLessThan
import io.kotest.matchers.shouldBe
import kotlin.math.abs
import kotlin.test.Test

class BearingAlgorithmTest {

    @Test
    fun `initial bearing should calculate north correctly`() {
        val algorithm = InitialBearing()

        val start = Coordinate(40.0, -74.0)
        val end = Coordinate(41.0, -74.0)  // 1 degree north

        val bearing = algorithm.calculate(start, end)

        // Should be approximately 0° (north)
        abs(bearing - 0.0) shouldBeLessThan 1.0
    }

    @Test
    fun `initial bearing should calculate east correctly`() {
        val algorithm = InitialBearing()

        val start = Coordinate(40.0, -74.0)
        val end = Coordinate(40.0, -73.0)  // 1 degree east

        val bearing = algorithm.calculate(start, end)

        // Should be approximately 90° (east)
        abs(bearing - 90.0) shouldBeLessThan 1.0
    }

    @Test
    fun `initial bearing should calculate south correctly`() {
        val algorithm = InitialBearing()

        val start = Coordinate(40.0, -74.0)
        val end = Coordinate(39.0, -74.0)  // 1 degree south

        val bearing = algorithm.calculate(start, end)

        // Should be approximately 180° (south)
        abs(bearing - 180.0) shouldBeLessThan 1.0
    }

    @Test
    fun `initial bearing should calculate west correctly`() {
        val algorithm = InitialBearing()

        val start = Coordinate(40.0, -74.0)
        val end = Coordinate(40.0, -75.0)  // 1 degree west

        val bearing = algorithm.calculate(start, end)

        // Should be approximately 270° (west)
        abs(bearing - 270.0) shouldBeLessThan 1.0
    }

    @Test
    fun `initial bearing should be between 0 and 360`() {
        val algorithm = InitialBearing()

        val start = Coordinate(0.0, 0.0)
        val end = Coordinate(45.0, 45.0)

        val bearing = algorithm.calculate(start, end)

        bearing shouldBeGreaterThan 0.0
        bearing shouldBeLessThan 360.0
    }

    @Test
    fun `final bearing should differ from initial for long distances`() {
        val initialAlg = InitialBearing()
        val finalAlg = FinalBearing()

        // NYC to London - long distance where initial and final bearings differ significantly
        val newYork = Coordinate(40.7128, -74.0060)
        val london = Coordinate(51.5074, -0.1278)

        val initialBearing = initialAlg.calculate(newYork, london)
        val finalBearing = finalAlg.calculate(newYork, london)

        // Bearings should be significantly different for this distance
        abs(initialBearing - finalBearing) shouldBeGreaterThan 10.0
    }

    @Test
    fun `final bearing from A to B should be reverse of initial bearing from B to A`() {
        val finalAlg = FinalBearing()
        val initialAlg = InitialBearing()

        val newYork = Coordinate(40.7128, -74.0060)
        val london = Coordinate(51.5074, -0.1278)

        val finalNYtoLondon = finalAlg.calculate(newYork, london)
        val initialLondonToNY = initialAlg.calculate(london, newYork)

        // Final bearing A→B should equal reverse of initial bearing B→A
        val expectedReverse = (initialLondonToNY + 180) % 360

        abs(finalNYtoLondon - expectedReverse) shouldBeLessThan 0.1
    }

    @Test
    fun `rhumb line bearing should be constant along path`() {
        val algorithm = RhumbLineBearing()

        val start = Coordinate(40.0, -74.0)
        val mid = Coordinate(45.0, -70.0)
        val end = Coordinate(50.0, -66.0)

        val bearing1 = algorithm.calculate(start, mid)
        val bearing2 = algorithm.calculate(mid, end)

        // Rhumb line should have approximately constant bearing
        // (small variations due to finite steps, but should be close)
        abs(bearing1 - bearing2) shouldBeLessThan 5.0
    }

    @Test
    fun `rhumb line should differ from great circle for long distances`() {
        val rhumb = RhumbLineBearing()
        val initial = InitialBearing()

        // Long east-west journey where rhumb line differs from great circle
        val start = Coordinate(40.0, -74.0)
        val end = Coordinate(40.0, 74.0)  // Halfway around world at same latitude

        val rhumbBearing = rhumb.calculate(start, end)
        val greatCircleBearing = initial.calculate(start, end)

        // Rhumb line at constant latitude goes east (90°)
        // Great circle would arc north then south
        abs(rhumbBearing - 90.0) shouldBeLessThan 1.0
        abs(greatCircleBearing - 90.0) shouldBeGreaterThan 1.0  // Should differ
    }

    @Test
    fun `rhumb line should handle dateline crossing`() {
        val algorithm = RhumbLineBearing()

        val west = Coordinate(40.0, 170.0)   // Just west of dateline
        val east = Coordinate(40.0, -170.0)  // Just east of dateline

        val bearing = algorithm.calculate(west, east)

        // Should go east (90°) across the dateline
        abs(bearing - 90.0) shouldBeLessThan 5.0
    }

    @Test
    fun `all bearings should handle equator crossing`() {
        val initial = InitialBearing()
        val final = FinalBearing()
        val rhumb = RhumbLineBearing()

        val north = Coordinate(10.0, 0.0)
        val south = Coordinate(-10.0, 0.0)

        val initialBearing = initial.calculate(north, south)
        val finalBearing = final.calculate(north, south)
        val rhumbBearing = rhumb.calculate(north, south)

        // All should indicate southward direction (around 180°)
        abs(initialBearing - 180.0) shouldBeLessThan 1.0
        abs(finalBearing - 180.0) shouldBeLessThan 1.0
        abs(rhumbBearing - 180.0) shouldBeLessThan 1.0
    }

    @Test
    fun `bearing should handle prime meridian crossing`() {
        val algorithm = InitialBearing()

        val west = Coordinate(51.5, -1.0)
        val east = Coordinate(51.5, 1.0)

        val bearing = algorithm.calculate(west, east)

        // Should be approximately 90° (east)
        abs(bearing - 90.0) shouldBeLessThan 1.0
    }

    @Test
    fun `factory should create correct algorithm instances`() {
        val initial = BearingAlgorithmFactory.create(BearingAlgorithmType.INITIAL)
        val final = BearingAlgorithmFactory.create(BearingAlgorithmType.FINAL)
        val rhumb = BearingAlgorithmFactory.create(BearingAlgorithmType.RHUMB_LINE)

        initial::class shouldBe InitialBearing::class
        final::class shouldBe FinalBearing::class
        rhumb::class shouldBe RhumbLineBearing::class
    }
}
