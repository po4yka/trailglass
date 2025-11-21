#!/bin/bash

# Run the performance benchmark tests and capture output
# This script runs the tests and shows the console output

echo "Running Performance Benchmarks..."
echo "This may take several minutes..."
echo ""

./gradlew :shared:cleanTest :shared:test --info 2>&1 | tee benchmark_output.log

echo ""
echo "Benchmark complete. Results saved to benchmark_output.log"
echo ""
echo "To view results:"
echo "  grep -A 500 'BENCHMARK' benchmark_output.log"
