package com.po4yka.trailglass.domain.algorithm

import com.po4yka.trailglass.domain.model.Coordinate
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.test.Test
import kotlin.time.measureTime

private val logger = KotlinLogging.logger {}

/** Quick benchmark test that can be run to get performance data. Remove @Ignore to execute. */
class QuickBenchmark {
    @Test
    fun quickPerformanceCheck() {
        val iterations = 1000
        val nyc = Coordinate(40.7128, -74.0060)
        val london = Coordinate(51.5074, -0.1278)

        logger.info { "\nQuick Performance Check ($iterations iterations)" }
        logger.info { "=".repeat(60) }

        // Distance algorithms
        logger.info { "\nDistance (NYC to London, ~5570km):" }

        val simple = SimpleDistance()
        val simpleTime =
            measureTime {
                repeat(iterations) { simple.calculate(nyc, london) }
            }
        logger.info { "  Simple:    ${simpleTime.inWholeMicroseconds / iterations}us/op" }

        val haversine = HaversineDistance()
        val haversineTime =
            measureTime {
                repeat(iterations) { haversine.calculate(nyc, london) }
            }
        logger.info { "  Haversine: ${haversineTime.inWholeMicroseconds / iterations}us/op" }

        val vincenty = VincentyDistance()
        val vincentyTime =
            measureTime {
                repeat(iterations) { vincenty.calculate(nyc, london) }
            }
        logger.info { "  Vincenty:  ${vincentyTime.inWholeMicroseconds / iterations}us/op" }

        // Bearing algorithms
        logger.info { "\nBearing:" }

        val initial = InitialBearing()
        val initialTime =
            measureTime {
                repeat(iterations) { initial.calculate(nyc, london) }
            }
        logger.info { "  Initial:   ${initialTime.inWholeMicroseconds / iterations}us/op" }

        val final = FinalBearing()
        val finalTime =
            measureTime {
                repeat(iterations) { final.calculate(nyc, london) }
            }
        logger.info { "  Final:     ${finalTime.inWholeMicroseconds / iterations}us/op" }

        val rhumb = RhumbLineBearing()
        val rhumbTime =
            measureTime {
                repeat(iterations) { rhumb.calculate(nyc, london) }
            }
        logger.info { "  Rhumb:     ${rhumbTime.inWholeMicroseconds / iterations}us/op" }

        // Interpolation
        logger.info { "\nInterpolation (10 steps):" }

        val linear = LinearInterpolation()
        val linearTime =
            measureTime {
                repeat(iterations) { linear.generatePath(nyc, london, 10) }
            }
        logger.info { "  Linear:    ${linearTime.inWholeMicroseconds / iterations}us/op" }

        val slerp = SphericalInterpolation()
        val slerpTime =
            measureTime {
                repeat(iterations) { slerp.generatePath(nyc, london, 10) }
            }
        logger.info { "  SLERP:     ${slerpTime.inWholeMicroseconds / iterations}us/op" }

        val cubic = CubicInterpolation()
        val cubicTime =
            measureTime {
                repeat(iterations) { cubic.generatePath(nyc, london, 10) }
            }
        logger.info { "  Cubic:     ${cubicTime.inWholeMicroseconds / iterations}us/op" }

        logger.info { "\n" + "=".repeat(60) }
    }
}
