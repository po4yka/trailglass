package com.po4yka.trailglass.domain.algorithm

import com.po4yka.trailglass.domain.model.Coordinate
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.time.Duration
import kotlin.time.measureTime

private val logger = KotlinLogging.logger {}

/**
 * Standalone benchmark runner that can be executed to generate performance reports. This provides a simple way to run
 * performance benchmarks and see results.
 */
object BenchmarkRunner {
    private const val ITERATIONS = 1000
    private const val WARMUP_ITERATIONS = 100

    data class BenchmarkResult(
        val algorithmName: String,
        val scenarioName: String,
        val averageTimeMicros: Double,
        val minTimeMicros: Double,
        val maxTimeMicros: Double
    )

    data class Scenario(
        val name: String,
        val from: Coordinate,
        val to: Coordinate
    )

    private val scenarios =
        listOf(
            Scenario("100m", Coordinate(40.7128, -74.0060), Coordinate(40.7138, -74.0060)),
            Scenario("500m", Coordinate(40.7128, -74.0060), Coordinate(40.7173, -74.0060)),
            Scenario("10km", Coordinate(40.7128, -74.0060), Coordinate(40.6782, -73.9442)),
            Scenario("50km", Coordinate(40.7128, -74.0060), Coordinate(40.7357, -74.1724)),
            Scenario("1000km", Coordinate(40.7128, -74.0060), Coordinate(41.8781, -87.6298)),
            Scenario("5000km", Coordinate(40.7128, -74.0060), Coordinate(51.5074, -0.1278)),
            Scenario("15000km", Coordinate(40.7128, -74.0060), Coordinate(-33.8688, 151.2093))
        )

    fun runDistanceBenchmarks(): List<BenchmarkResult> {
        val results = mutableListOf<BenchmarkResult>()

        logger.info { "\n${"=".repeat(80)}" }
        logger.info { "DISTANCE ALGORITHM PERFORMANCE BENCHMARK" }
        logger.info { "=".repeat(80) }
        logger.info { "Iterations: $ITERATIONS, Warmup: $WARMUP_ITERATIONS" }
        logger.info { "" }

        val algorithms =
            listOf(
                "Simple" to SimpleDistance(),
                "Haversine" to HaversineDistance(),
                "Vincenty" to VincentyDistance()
            )

        for (scenario in scenarios) {
            logger.info { "-".repeat(80) }
            logger.info { "Scenario: ${scenario.name}" }
            logger.info { "" }

            val scenarioResults = mutableListOf<BenchmarkResult>()

            for ((name, algorithm) in algorithms) {
                // Warmup
                repeat(WARMUP_ITERATIONS) {
                    algorithm.calculate(scenario.from, scenario.to)
                }

                val times = mutableListOf<Duration>()
                repeat(ITERATIONS) {
                    val time =
                        measureTime {
                            algorithm.calculate(scenario.from, scenario.to)
                        }
                    times.add(time)
                }

                val avgTime = times.map { it.inWholeMicroseconds }.average()
                val minTime = times.minOf { it.inWholeMicroseconds }.toDouble()
                val maxTime = times.maxOf { it.inWholeMicroseconds }.toDouble()

                val result = BenchmarkResult(name, scenario.name, avgTime, minTime, maxTime)
                scenarioResults.add(result)
                results.add(result)

                logger.info { "  $name:" }
                logger.info { "    Average: ${String.format("%.2f", avgTime)}us" }
                logger.info { "    Min: ${String.format("%.2f", minTime)}us" }
                logger.info { "    Max: ${String.format("%.2f", maxTime)}us" }
            }

            // Show comparisons
            logger.info { "" }
            logger.info { "  Performance comparison:" }
            val baseline = scenarioResults[0]
            for (i in 1 until scenarioResults.size) {
                val ratio = scenarioResults[i].averageTimeMicros / baseline.averageTimeMicros
                logger.info {
                    "    ${scenarioResults[i].algorithmName} vs ${baseline.algorithmName}: ${
                        String.format(
                            "%.2f",
                            ratio
                        )
                    }x"
                }
            }
            logger.info { "" }
        }

        return results
    }

    fun runBearingBenchmarks(): List<BenchmarkResult> {
        val results = mutableListOf<BenchmarkResult>()

        logger.info { "\n${"=".repeat(80)}" }
        logger.info { "BEARING ALGORITHM PERFORMANCE BENCHMARK" }
        logger.info { "=".repeat(80) }
        logger.info { "" }

        val algorithms =
            listOf(
                "Initial" to InitialBearing(),
                "Final" to FinalBearing(),
                "Rhumb" to RhumbLineBearing()
            )

        val testScenarios = listOf(scenarios[2], scenarios[4], scenarios[6])

        for (scenario in testScenarios) {
            logger.info { "-".repeat(80) }
            logger.info { "Scenario: ${scenario.name}" }
            logger.info { "" }

            val scenarioResults = mutableListOf<BenchmarkResult>()

            for ((name, algorithm) in algorithms) {
                repeat(WARMUP_ITERATIONS) {
                    algorithm.calculate(scenario.from, scenario.to)
                }

                val times = mutableListOf<Duration>()
                repeat(ITERATIONS) {
                    val time =
                        measureTime {
                            algorithm.calculate(scenario.from, scenario.to)
                        }
                    times.add(time)
                }

                val avgTime = times.map { it.inWholeMicroseconds }.average()
                val minTime = times.minOf { it.inWholeMicroseconds }.toDouble()
                val maxTime = times.maxOf { it.inWholeMicroseconds }.toDouble()

                val result = BenchmarkResult(name, scenario.name, avgTime, minTime, maxTime)
                scenarioResults.add(result)
                results.add(result)

                logger.info { "  $name: ${String.format("%.2f", avgTime)}us avg" }
            }
            logger.info { "" }
        }

        return results
    }

    fun runInterpolationBenchmarks(): List<BenchmarkResult> {
        val results = mutableListOf<BenchmarkResult>()

        logger.info { "\n${"=".repeat(80)}" }
        logger.info { "INTERPOLATION ALGORITHM PERFORMANCE BENCHMARK" }
        logger.info { "=".repeat(80) }
        logger.info { "" }

        val algorithms =
            listOf(
                "Linear" to LinearInterpolation(),
                "SLERP" to SphericalInterpolation(),
                "Cubic" to CubicInterpolation()
            )

        val testScenarios = listOf(scenarios[2], scenarios[4])
        val stepCounts = listOf(10, 50, 100)

        for (scenario in testScenarios) {
            logger.info { "-".repeat(80) }
            logger.info { "Scenario: ${scenario.name}" }
            logger.info { "" }

            for (steps in stepCounts) {
                logger.info { "  Steps: $steps" }

                for ((name, algorithm) in algorithms) {
                    repeat(WARMUP_ITERATIONS) {
                        algorithm.generatePath(scenario.from, scenario.to, steps)
                    }

                    val times = mutableListOf<Duration>()
                    repeat(ITERATIONS) {
                        val time =
                            measureTime {
                                algorithm.generatePath(scenario.from, scenario.to, steps)
                            }
                        times.add(time)
                    }

                    val avgTime = times.map { it.inWholeMicroseconds }.average()
                    val result = BenchmarkResult("$name-$steps", scenario.name, avgTime, 0.0, 0.0)
                    results.add(result)

                    logger.info { "    $name: ${String.format("%.2f", avgTime)}us" }
                }
                logger.info { "" }
            }
        }

        return results
    }

    fun printSummary(distanceResults: List<BenchmarkResult>) {
        logger.info { "\n${"=".repeat(80)}" }
        logger.info { "PERFORMANCE SUMMARY" }
        logger.info { "=".repeat(80) }
        logger.info { "" }

        // Group by algorithm and calculate averages
        val byAlgorithm = distanceResults.groupBy { it.algorithmName }

        logger.info { "Average performance across all distances:" }
        byAlgorithm.forEach { (algo, results) ->
            val avg = results.map { it.averageTimeMicros }.average()
            logger.info { "  $algo: ${String.format("%.2f", avg)}us" }
        }

        logger.info { "" }
        logger.info { "Recommendations:" }
        logger.info { "  - Simple: Best for <1km (fastest, 1-5% error)" }
        logger.info { "  - Haversine: Best for 1km-10000km (balanced speed/accuracy)" }
        logger.info { "  - Vincenty: Best for >10000km or scientific use (most accurate)" }
        logger.info { "" }
        logger.info { "=".repeat(80) }
    }
}
