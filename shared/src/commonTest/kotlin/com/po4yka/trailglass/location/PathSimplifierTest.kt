package com.po4yka.trailglass.location

import com.po4yka.trailglass.domain.model.LocationSample
import com.po4yka.trailglass.domain.model.LocationSource
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PathSimplifierTest {

    private val simplifier = PathSimplifier(epsilonMeters = 10.0)

    @Test
    fun testSimplify_removesCollinearPoints() {
        val p1 = createSample(0.0, 0.0)
        val p2 = createSample(0.0, 0.0001) // Very close to line p1-p3
        val p3 = createSample(0.0, 0.0002)

        val result = simplifier.simplify(listOf(p1, p2, p3))

        assertEquals(2, result.size)
        assertEquals(p1.latitude, result[0].latitude)
        assertEquals(p3.latitude, result[1].latitude)
    }

    @Test
    fun testSimplify_keepsSignificantPoints() {
        val p1 = createSample(0.0, 0.0)
        val p2 = createSample(0.001, 0.001) // Significant deviation
        val p3 = createSample(0.0, 0.002)

        val result = simplifier.simplify(listOf(p1, p2, p3))

        assertEquals(3, result.size)
    }

    @Test
    fun testSimplify_handlesShortPaths() {
        val p1 = createSample(0.0, 0.0)
        val p2 = createSample(0.0, 0.001)

        val result = simplifier.simplify(listOf(p1, p2))

        assertEquals(2, result.size)
    }

    private fun createSample(lat: Double, lon: Double): LocationSample {
        return LocationSample(
            id = "test",
            timestamp = Clock.System.now(),
            latitude = lat,
            longitude = lon,
            accuracy = 10.0,
            source = LocationSource.GPS,
            deviceId = "test_device",
            userId = "test_user"
        )
    }
}
