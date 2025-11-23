package com.po4yka.trailglass.domain.algorithm

import com.po4yka.trailglass.domain.model.Coordinate
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.time.Duration
import kotlin.time.measureTime

/**
 * Performance benchmark tests for geographic algorithms.
 *
 * These tests measure execution time for different algorithm implementations across various distance ranges and
 * scenarios. They are marked with @Ignore by default since they're for manual performance testing rather than CI/CD.
 *
 * To run these benchmarks:
 * - Remove the @Ignore annotation
 * - Run: ./gradlew :shared:test --tests AlgorithmPerformanceBenchmark
 * - View results in console output
 *
 * Note: Results may vary based on platform (JVM, Native, JS) and hardware.
 */
@Ignore("Performance benchmarks - run manually")
class AlgorithmPerformanceBenchmark {
    companion object {
        private const val ITERATIONS = 1000
        private const val WARMUP_ITERATIONS = 100
    }

    /** Test scenarios covering different distance ranges. */
    data class DistanceScenario(
        val name: String,
        val from: Coordinate,
        val to: Coordinate,
        val expectedDistance: Double
    )

    private val scenarios =
        listOf(
            // Short distances (<1km)
            DistanceScenario(
                name = "100m (New York, same block)",
                from = Coordinate(40.7128, -74.0060),
                to = Coordinate(40.7138, -74.0060),
                expectedDistance = 111.0
            ),
            DistanceScenario(
                name = "500m (New York, 5 blocks)",
                from = Coordinate(40.7128, -74.0060),
                to = Coordinate(40.7173, -74.0060),
                expectedDistance = 500.0
            ),
            // Medium distances (1-100km)
            DistanceScenario(
                name = "10km (NYC to Brooklyn)",
                from = Coordinate(40.7128, -74.0060),
                to = Coordinate(40.6782, -73.9442),
                expectedDistance = 10_000.0
            ),
            DistanceScenario(
                name = "50km (NYC to Newark)",
                from = Coordinate(40.7128, -74.0060),
                to = Coordinate(40.7357, -74.1724),
                expectedDistance = 50_000.0
            ),
            // Long distances (100-10000km)
            DistanceScenario(
                name = "1000km (NYC to Chicago)",
                from = Coordinate(40.7128, -74.0060),
                to = Coordinate(41.8781, -87.6298),
                expectedDistance = 1_150_000.0
            ),
            DistanceScenario(
                name = "5000km (NYC to London)",
                from = Coordinate(40.7128, -74.0060),
                to = Coordinate(51.5074, -0.1278),
                expectedDistance = 5_570_000.0
            ),
            // Very long distances (>10000km)
            DistanceScenario(
                name = "15000km (NYC to Sydney, half Earth)",
                from = Coordinate(40.7128, -74.0060),
                to = Coordinate(-33.8688, 151.2093),
                expectedDistance = 15_990_000.0
            )
        )

    /** Benchmark results for a single algorithm. */
    data class BenchmarkResult(
        val algorithmName: String,
        val scenarioName: String,
        val iterations: Int,
        val totalTime: Duration,
        val averageTime: Duration,
        val minTime: Duration,
        val maxTime: Duration,
        val result: Double
    ) {
        val averageTimeNanos: Long get() = averageTime.inWholeNanoseconds
        val averageTimeMicros: Double get() = averageTime.inWholeMicroseconds.toDouble()

        fun format(): String =
            buildString {
                appendLine("  $algorithmName:")
                appendLine("    Total time: ${totalTime.inWholeMilliseconds}ms")
                appendLine("    Average: ${formatDuration(averageTime)}")
                appendLine("    Min: ${formatDuration(minTime)}")
                appendLine("    Max: ${formatDuration(maxTime)}")
                appendLine("    Result: ${result.toLong()}m")
            }

        private fun formatDuration(duration: Duration): String =
            when {
                duration.inWholeMilliseconds > 0 -> "${duration.inWholeMilliseconds}ms"
                duration.inWholeMicroseconds > 0 -> "${duration.inWholeMicroseconds}us"
                else -> "${duration.inWholeNanoseconds}ns"
            }
    }

    /** Run a benchmark for a given algorithm and scenario. */
    private fun benchmarkDistance(
        algorithm: DistanceAlgorithm,
        algorithmName: String,
        scenario: DistanceScenario
    ): BenchmarkResult {
        // Warmup
        repeat(WARMUP_ITERATIONS) {
            algorithm.calculate(scenario.from, scenario.to)
        }

        val times = mutableListOf<Duration>()
        var result = 0.0

        repeat(ITERATIONS) {
            val time =
                measureTime {
                    result = algorithm.calculate(scenario.from, scenario.to)
                }
            times.add(time)
        }

        val totalTime = times.reduce { acc, duration -> acc + duration }
        val averageTime = totalTime / ITERATIONS
        val minTime = times.minOrNull() ?: Duration.ZERO
        val maxTime = times.maxOrNull() ?: Duration.ZERO

        return BenchmarkResult(
            algorithmName = algorithmName,
            scenarioName = scenario.name,
            iterations = ITERATIONS,
            totalTime = totalTime,
            averageTime = averageTime,
            minTime = minTime,
            maxTime = maxTime,
            result = result
        )
    }

    /** Run a benchmark for bearing algorithms. */
    private fun benchmarkBearing(
        algorithm: BearingAlgorithm,
        algorithmName: String,
        scenario: DistanceScenario
    ): BenchmarkResult {
        // Warmup
        repeat(WARMUP_ITERATIONS) {
            algorithm.calculate(scenario.from, scenario.to)
        }

        val times = mutableListOf<Duration>()
        var result = 0.0

        repeat(ITERATIONS) {
            val time =
                measureTime {
                    result = algorithm.calculate(scenario.from, scenario.to)
                }
            times.add(time)
        }

        val totalTime = times.reduce { acc, duration -> acc + duration }
        val averageTime = totalTime / ITERATIONS
        val minTime = times.minOrNull() ?: Duration.ZERO
        val maxTime = times.maxOrNull() ?: Duration.ZERO

        return BenchmarkResult(
            algorithmName = algorithmName,
            scenarioName = scenario.name,
            iterations = ITERATIONS,
            totalTime = totalTime,
            averageTime = averageTime,
            minTime = minTime,
            maxTime = maxTime,
            result = result
        )
    }

    /** Run a benchmark for interpolation algorithms. */
    private fun benchmarkInterpolation(
        algorithm: InterpolationAlgorithm,
        algorithmName: String,
        scenario: DistanceScenario,
        steps: Int
    ): BenchmarkResult {
        // Warmup
        repeat(WARMUP_ITERATIONS) {
            algorithm.generatePath(scenario.from, scenario.to, steps)
        }

        val times = mutableListOf<Duration>()
        var resultSize = 0

        repeat(ITERATIONS) {
            val time =
                measureTime {
                    val path = algorithm.generatePath(scenario.from, scenario.to, steps)
                    resultSize = path.size
                }
            times.add(time)
        }

        val totalTime = times.reduce { acc, duration -> acc + duration }
        val averageTime = totalTime / ITERATIONS
        val minTime = times.minOrNull() ?: Duration.ZERO
        val maxTime = times.maxOrNull() ?: Duration.ZERO

        return BenchmarkResult(
            algorithmName = "$algorithmName (steps=$steps)",
            scenarioName = scenario.name,
            iterations = ITERATIONS,
            totalTime = totalTime,
            averageTime = averageTime,
            minTime = minTime,
            maxTime = maxTime,
            result = resultSize.toDouble()
        )
    }

    @Test
    fun `benchmark distance algorithms`() {
        println("\n" + "=".repeat(80))
        println("DISTANCE ALGORITHM PERFORMANCE BENCHMARK")
        println("=".repeat(80))
        println("Iterations per test: $ITERATIONS")
        println("Warmup iterations: $WARMUP_ITERATIONS")
        println()

        val algorithms =
            listOf(
                SimpleDistance() to "Simple",
                HaversineDistance() to "Haversine",
                VincentyDistance() to "Vincenty"
            )

        for (scenario in scenarios) {
            println("-".repeat(80))
            println("Scenario: ${scenario.name}")
            println("Expected distance: ${scenario.expectedDistance.toLong()}m")
            println()

            val results =
                algorithms.map { (algorithm, name) ->
                    benchmarkDistance(algorithm, name, scenario)
                }

            results.forEach { println(it.format()) }

            // Performance comparison
            println("  Performance comparison:")
            val baseline = results.first()
            results.drop(1).forEach { result ->
                val ratio = result.averageTimeNanos.toDouble() / baseline.averageTimeNanos
                val comparison =
                    if (ratio > 1) {
                        "${String.format("%.2f", ratio)}x slower"
                    } else {
                        "${String.format("%.2f", 1 / ratio)}x faster"
                    }
                println("    ${result.algorithmName} vs ${baseline.algorithmName}: $comparison")
            }
            println()
        }

        println("=".repeat(80))
    }

    @Test
    fun `benchmark bearing algorithms`() {
        println("\n" + "=".repeat(80))
        println("BEARING ALGORITHM PERFORMANCE BENCHMARK")
        println("=".repeat(80))
        println("Iterations per test: $ITERATIONS")
        println("Warmup iterations: $WARMUP_ITERATIONS")
        println()

        val algorithms =
            listOf(
                InitialBearing() to "Initial",
                FinalBearing() to "Final",
                RhumbLineBearing() to "Rhumb Line"
            )

        // Use a subset of scenarios for bearing tests
        val bearingScenarios =
            listOf(
                scenarios[2], // 10km
                scenarios[4], // 1000km
                scenarios[6] // 15000km
            )

        for (scenario in bearingScenarios) {
            println("-".repeat(80))
            println("Scenario: ${scenario.name}")
            println()

            val results =
                algorithms.map { (algorithm, name) ->
                    benchmarkBearing(algorithm, name, scenario)
                }

            results.forEach { println(it.format()) }

            // Performance comparison
            println("  Performance comparison:")
            val baseline = results.first()
            results.drop(1).forEach { result ->
                val ratio = result.averageTimeNanos.toDouble() / baseline.averageTimeNanos
                val comparison =
                    if (ratio > 1) {
                        "${String.format("%.2f", ratio)}x slower"
                    } else {
                        "${String.format("%.2f", 1 / ratio)}x faster"
                    }
                println("    ${result.algorithmName} vs ${baseline.algorithmName}: $comparison")
            }
            println()
        }

        println("=".repeat(80))
    }

    @Test
    fun `benchmark interpolation algorithms with different step counts`() {
        println("\n" + "=".repeat(80))
        println("INTERPOLATION ALGORITHM PERFORMANCE BENCHMARK")
        println("=".repeat(80))
        println("Iterations per test: $ITERATIONS")
        println("Warmup iterations: $WARMUP_ITERATIONS")
        println()

        val algorithms =
            listOf(
                LinearInterpolation() to "Linear",
                SphericalInterpolation() to "SLERP",
                CubicInterpolation() to "Cubic"
            )

        val stepCounts = listOf(10, 50, 100)

        // Use medium and long distance scenarios
        val interpolationScenarios =
            listOf(
                scenarios[2], // 10km
                scenarios[4] // 1000km
            )

        for (scenario in interpolationScenarios) {
            println("-".repeat(80))
            println("Scenario: ${scenario.name}")
            println()

            for (steps in stepCounts) {
                println("  Step count: $steps")
                println()

                val results =
                    algorithms.map { (algorithm, name) ->
                        benchmarkInterpolation(algorithm, name, scenario, steps)
                    }

                results.forEach { result ->
                    println("    ${result.algorithmName}:")
                    println("      Average: ${result.averageTimeMicros.toLong()}us")
                    println("      Total: ${result.totalTime.inWholeMilliseconds}ms")
                }

                // Performance comparison
                println("    Performance comparison:")
                val baseline = results.first()
                results.drop(1).forEach { result ->
                    val ratio = result.averageTimeNanos.toDouble() / baseline.averageTimeNanos
                    val comparison =
                        if (ratio > 1) {
                            "${String.format("%.2f", ratio)}x slower"
                        } else {
                            "${String.format("%.2f", 1 / ratio)}x faster"
                        }
                    val algoName = result.algorithmName.substringBefore(" (")
                    val baselineName = baseline.algorithmName.substringBefore(" (")
                    println("      $algoName vs $baselineName: $comparison")
                }
                println()
            }
        }

        println("=".repeat(80))
    }

    @Test
    fun `run all benchmarks using runner`() {
        val distanceResults = BenchmarkRunner.runDistanceBenchmarks()
        BenchmarkRunner.runBearingBenchmarks()
        BenchmarkRunner.runInterpolationBenchmarks()
        BenchmarkRunner.printSummary(distanceResults)
    }

    @Test
    fun `benchmark comprehensive comparison summary`() {
        println("\n" + "=".repeat(80))
        println("COMPREHENSIVE ALGORITHM PERFORMANCE SUMMARY")
        println("=".repeat(80))
        println()

        // Distance algorithms across all scenarios
        println("DISTANCE ALGORITHMS - AVERAGE PERFORMANCE ACROSS ALL SCENARIOS")
        println("-".repeat(80))

        val distanceAlgorithms =
            listOf(
                SimpleDistance() to "Simple",
                HaversineDistance() to "Haversine",
                VincentyDistance() to "Vincenty"
            )

        val distanceResults = mutableMapOf<String, MutableList<Duration>>()

        for ((algorithm, name) in distanceAlgorithms) {
            distanceResults[name] = mutableListOf()
            for (scenario in scenarios) {
                val result = benchmarkDistance(algorithm, name, scenario)
                distanceResults[name]!!.add(result.averageTime)
            }
        }

        // Calculate average across all scenarios
        distanceResults.forEach { (name, times) ->
            val avgTime = times.reduce { acc, duration -> acc + duration } / times.size
            println("$name: ${avgTime.inWholeMicroseconds}us average")
        }

        println()

        // Performance ratios
        val simpleAvg = distanceResults["Simple"]!!.reduce { acc, d -> acc + d } / scenarios.size
        val haversineAvg = distanceResults["Haversine"]!!.reduce { acc, d -> acc + d } / scenarios.size
        val vincentyAvg = distanceResults["Vincenty"]!!.reduce { acc, d -> acc + d } / scenarios.size

        println("Performance ratios (across all distances):")
        println(
            "  Haversine vs Simple: ${
                String.format(
                    "%.2f",
                    haversineAvg.inWholeNanoseconds.toDouble() / simpleAvg.inWholeNanoseconds
                )
            }x"
        )
        println(
            "  Vincenty vs Simple: ${
                String.format(
                    "%.2f",
                    vincentyAvg.inWholeNanoseconds.toDouble() / simpleAvg.inWholeNanoseconds
                )
            }x"
        )
        println(
            "  Vincenty vs Haversine: ${
                String.format(
                    "%.2f",
                    vincentyAvg.inWholeNanoseconds.toDouble() / haversineAvg.inWholeNanoseconds
                )
            }x"
        )

        println()
        println("=".repeat(80))
        println()

        // Recommendations
        println("RECOMMENDATIONS")
        println("-".repeat(80))
        println("Short distances (<1km):")
        println("  - Use Simple for best performance (fastest)")
        println("  - Accuracy trade-off: ~1-5% error acceptable")
        println()
        println("Medium distances (1-100km):")
        println("  - Use Haversine for balance of speed and accuracy")
        println("  - Accuracy: <0.5% error")
        println()
        println("Long distances (>100km):")
        println("  - Use Haversine for most applications (good speed, good accuracy)")
        println("  - Use Vincenty for scientific/surveying applications (best accuracy)")
        println()
        println("Bearing calculations:")
        println("  - All algorithms have similar performance")
        println("  - Use Initial for most navigation use cases")
        println()
        println("Interpolation:")
        println("  - Use Linear for simple animations (fastest)")
        println("  - Use SLERP for geographic accuracy (great circle path)")
        println("  - Use Cubic for smooth visual animations")
        println()
        println("=".repeat(80))
    }
}
