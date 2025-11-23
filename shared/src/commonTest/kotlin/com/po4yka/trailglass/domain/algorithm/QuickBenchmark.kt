package com.po4yka.trailglass.domain.algorithm

import com.po4yka.trailglass.domain.model.Coordinate
import kotlin.test.Test
import kotlin.time.measureTime

/** Quick benchmark test that can be run to get performance data. Remove @Ignore to execute. */
class QuickBenchmark {
    @Test
    fun quickPerformanceCheck() {
        val iterations = 1000
        val nyc = Coordinate(40.7128, -74.0060)
        val london = Coordinate(51.5074, -0.1278)

        println("\nQuick Performance Check ($iterations iterations)")
        println("=".repeat(60))

        // Distance algorithms
        println("\nDistance (NYC to London, ~5570km):")

        val simple = SimpleDistance()
        val simpleTime =
            measureTime {
                repeat(iterations) { simple.calculate(nyc, london) }
            }
        println("  Simple:    ${simpleTime.inWholeMicroseconds / iterations}us/op")

        val haversine = HaversineDistance()
        val haversineTime =
            measureTime {
                repeat(iterations) { haversine.calculate(nyc, london) }
            }
        println("  Haversine: ${haversineTime.inWholeMicroseconds / iterations}us/op")

        val vincenty = VincentyDistance()
        val vincentyTime =
            measureTime {
                repeat(iterations) { vincenty.calculate(nyc, london) }
            }
        println("  Vincenty:  ${vincentyTime.inWholeMicroseconds / iterations}us/op")

        // Bearing algorithms
        println("\nBearing:")

        val initial = InitialBearing()
        val initialTime =
            measureTime {
                repeat(iterations) { initial.calculate(nyc, london) }
            }
        println("  Initial:   ${initialTime.inWholeMicroseconds / iterations}us/op")

        val final = FinalBearing()
        val finalTime =
            measureTime {
                repeat(iterations) { final.calculate(nyc, london) }
            }
        println("  Final:     ${finalTime.inWholeMicroseconds / iterations}us/op")

        val rhumb = RhumbLineBearing()
        val rhumbTime =
            measureTime {
                repeat(iterations) { rhumb.calculate(nyc, london) }
            }
        println("  Rhumb:     ${rhumbTime.inWholeMicroseconds / iterations}us/op")

        // Interpolation
        println("\nInterpolation (10 steps):")

        val linear = LinearInterpolation()
        val linearTime =
            measureTime {
                repeat(iterations) { linear.generatePath(nyc, london, 10) }
            }
        println("  Linear:    ${linearTime.inWholeMicroseconds / iterations}us/op")

        val slerp = SphericalInterpolation()
        val slerpTime =
            measureTime {
                repeat(iterations) { slerp.generatePath(nyc, london, 10) }
            }
        println("  SLERP:     ${slerpTime.inWholeMicroseconds / iterations}us/op")

        val cubic = CubicInterpolation()
        val cubicTime =
            measureTime {
                repeat(iterations) { cubic.generatePath(nyc, london, 10) }
            }
        println("  Cubic:     ${cubicTime.inWholeMicroseconds / iterations}us/op")

        println("\n" + "=".repeat(60))
    }
}
