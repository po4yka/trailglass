#!/usr/bin/env kotlin

/**
 * Simple script to demonstrate benchmark usage.
 *
 * To run benchmarks properly, use:
 * ./gradlew :shared:test --info
 *
 * And look for AlgorithmPerformanceBenchmark output in the logs,
 * or temporarily remove the @Ignore annotation from the test class.
 */

println("""
Performance Benchmark Guide
===========================

The benchmark tests are located in:
  shared/src/commonTest/kotlin/com/po4yka/trailglass/domain/algorithm/AlgorithmPerformanceBenchmark.kt

To run the benchmarks:

1. Remove the @Ignore annotation from the test class
2. Run: ./gradlew :shared:test
3. Check the console output for benchmark results

OR

Use the standalone BenchmarkRunner:
  shared/src/commonTest/kotlin/com/po4yka/trailglass/domain/algorithm/BenchmarkRunner.kt

Sample expected results (will vary by platform):

DISTANCE ALGORITHMS:
  Simple:     ~0.1-0.5us per calculation
  Haversine:  ~0.5-2us per calculation
  Vincenty:   ~2-10us per calculation

Performance ratios:
  - Haversine is typically 2-5x slower than Simple
  - Vincenty is typically 5-20x slower than Simple
  - Vincenty is typically 2-5x slower than Haversine

BEARING ALGORITHMS:
  All algorithms have similar performance (~0.5-2us)
  - Initial: Fastest (baseline)
  - Final: Similar to Initial (reuses Initial calculation)
  - Rhumb Line: Slightly slower due to logarithmic calculations

INTERPOLATION ALGORITHMS:
  Performance depends heavily on step count:

  10 steps:
    - Linear: ~1-5us
    - SLERP: ~10-50us (due to trigonometric operations)
    - Cubic: ~5-20us

  100 steps:
    - Linear: ~10-50us
    - SLERP: ~100-500us
    - Cubic: ~50-200us

RECOMMENDATIONS:

  Distance calculations:
    - <1km: Use Simple (fastest, acceptable error)
    - 1-100km: Use Haversine (best balance)
    - >100km: Use Haversine (usually sufficient) or Vincenty (scientific accuracy)

  Bearing calculations:
    - Use Initial for most cases (standard compass heading)
    - Use Final only when arrival heading is needed
    - Use Rhumb Line for constant-bearing navigation

  Interpolation:
    - Use Linear for simple UI animations (fastest)
    - Use SLERP for geographic accuracy (great circles)
    - Use Cubic for smooth visual transitions
""")
