#!/bin/bash
set -e

echo "==================================="
echo "Running all linters for Trailglass"
echo "==================================="
echo ""

# Exit codes
EXIT_CODE=0

# Kotlin linting (ktlint)
echo "1/4 Running ktlint..."
echo "-----------------------------------"
if ./gradlew ktlintCheck --continue; then
    echo "✓ ktlint passed"
else
    echo "✗ ktlint failed"
    EXIT_CODE=1
fi
echo ""

# Static analysis (detekt)
echo "2/4 Running detekt..."
echo "-----------------------------------"
if ./gradlew detekt --continue; then
    echo "✓ detekt passed"
else
    echo "✗ detekt failed"
    echo "  Check reports at:"
    echo "  - shared/build/reports/detekt/detekt.html"
    echo "  - composeApp/build/reports/detekt/detekt.html"
    EXIT_CODE=1
fi
echo ""

# Android Lint
echo "3/4 Running Android Lint..."
echo "-----------------------------------"
if ./gradlew :composeApp:lint --continue; then
    echo "✓ Android Lint passed"
else
    echo "✗ Android Lint failed"
    echo "  Check report at: composeApp/build/reports/lint-results.html"
    EXIT_CODE=1
fi
echo ""

# SwiftLint (iOS)
echo "4/4 Running SwiftLint..."
echo "-----------------------------------"
if command -v swiftlint &> /dev/null; then
    cd iosApp
    if swiftlint lint --strict; then
        echo "✓ SwiftLint passed"
    else
        echo "✗ SwiftLint failed"
        EXIT_CODE=1
    fi
    cd ..
else
    echo "⚠ SwiftLint not installed, skipping..."
    echo "  Install with: brew install swiftlint"
fi
echo ""

# Summary
echo "==================================="
if [ $EXIT_CODE -eq 0 ]; then
    echo "✓ All linters passed successfully!"
else
    echo "✗ Some linters failed. See output above."
fi
echo "==================================="

exit $EXIT_CODE
