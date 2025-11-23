package com.po4yka.trailglass.domain.algorithm

import com.po4yka.trailglass.domain.model.Coordinate
import kotlin.time.Duration
import kotlin.time.measureTime

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

        println("\n${"=".repeat(80)}")
        println("DISTANCE ALGORITHM PERFORMANCE BENCHMARK")
        println("${"=".repeat(80)}")
        println("Iterations: $ITERATIONS, Warmup: $WARMUP_ITERATIONS")
        println()

        val algorithms =
            listOf(
                "Simple" to SimpleDistance(),
                "Haversine" to HaversineDistance(),
                "Vincenty" to VincentyDistance()
            )

        for (scenario in scenarios) {
            println("${"-".repeat(80)}")
            println("Scenario: ${scenario.name}")
            println()

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

                println("  $name:")
                println("    Average: ${String.format("%.2f", avgTime)}us")
                println("    Min: ${String.format("%.2f", minTime)}us")
                println("    Max: ${String.format("%.2f", maxTime)}us")
            }

            // Show comparisons
            println()
            println("  Performance comparison:")
            val baseline = scenarioResults[0]
            for (i in 1 until scenarioResults.size) {
                val ratio = scenarioResults[i].averageTimeMicros / baseline.averageTimeMicros
                println(
                    "    ${scenarioResults[i].algorithmName} vs ${baseline.algorithmName}: ${
                        String.format(
                            "%.2f",
                            ratio
                        )
                    }x"
                )
            }
            println()
        }

        return results
    }

    fun runBearingBenchmarks(): List<BenchmarkResult> {
        val results = mutableListOf<BenchmarkResult>()

        println("\n${"=".repeat(80)}")
        println("BEARING ALGORITHM PERFORMANCE BENCHMARK")
        println("${"=".repeat(80)}")
        println()

        val algorithms =
            listOf(
                "Initial" to InitialBearing(),
                "Final" to FinalBearing(),
                "Rhumb" to RhumbLineBearing()
            )

        val testScenarios = listOf(scenarios[2], scenarios[4], scenarios[6])

        for (scenario in testScenarios) {
            println("${"-".repeat(80)}")
            println("Scenario: ${scenario.name}")
            println()

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

                println("  $name: ${String.format("%.2f", avgTime)}us avg")
            }
            println()
        }

        return results
    }

    fun runInterpolationBenchmarks(): List<BenchmarkResult> {
        val results = mutableListOf<BenchmarkResult>()

        println("\n${"=".repeat(80)}")
        println("INTERPOLATION ALGORITHM PERFORMANCE BENCHMARK")
        println("${"=".repeat(80)}")
        println()

        val algorithms =
            listOf(
                "Linear" to LinearInterpolation(),
                "SLERP" to SphericalInterpolation(),
                "Cubic" to CubicInterpolation()
            )

        val testScenarios = listOf(scenarios[2], scenarios[4])
        val stepCounts = listOf(10, 50, 100)

        for (scenario in testScenarios) {
            println("${"-".repeat(80)}")
            println("Scenario: ${scenario.name}")
            println()

            for (steps in stepCounts) {
                println("  Steps: $steps")

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

                    println("    $name: ${String.format("%.2f", avgTime)}us")
                }
                println()
            }
        }

        return results
    }

    fun printSummary(distanceResults: List<BenchmarkResult>) {
        println("\n${"=".repeat(80)}")
        println("PERFORMANCE SUMMARY")
        println("${"=".repeat(80)}")
        println()

        // Group by algorithm and calculate averages
        val byAlgorithm = distanceResults.groupBy { it.algorithmName }

        println("Average performance across all distances:")
        byAlgorithm.forEach { (algo, results) ->
            val avg = results.map { it.averageTimeMicros }.average()
            println("  $algo: ${String.format("%.2f", avg)}us")
        }

        println()
        println("Recommendations:")
        println("  - Simple: Best for <1km (fastest, 1-5% error)")
        println("  - Haversine: Best for 1km-10000km (balanced speed/accuracy)")
        println("  - Vincenty: Best for >10000km or scientific use (most accurate)")
        println()
        println("${"=".repeat(80)}")
    }
}
