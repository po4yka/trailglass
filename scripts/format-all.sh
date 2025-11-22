#!/bin/bash
set -e

echo "========================================"
echo "Auto-formatting code for Trailglass"
echo "========================================"
echo ""

# Exit codes
EXIT_CODE=0

# Kotlin formatting (ktlint)
echo "1/3 Formatting Kotlin code with ktlint..."
echo "----------------------------------------"
if ./gradlew ktlintFormat; then
    echo "✓ Kotlin code formatted"
else
    echo "✗ Kotlin formatting failed"
    EXIT_CODE=1
fi
echo ""

# Detekt auto-correct
echo "2/3 Auto-correcting issues with detekt..."
echo "----------------------------------------"
if ./gradlew detekt --auto-correct; then
    echo "✓ Detekt auto-corrections applied"
else
    echo "✗ Detekt auto-correct failed"
    EXIT_CODE=1
fi
echo ""

# SwiftLint (iOS)
echo "3/3 Formatting Swift code with SwiftLint..."
echo "----------------------------------------"
if command -v swiftlint &> /dev/null; then
    cd iosApp
    if swiftlint --fix; then
        echo "✓ Swift code formatted"
    else
        echo "✗ Swift formatting failed"
        EXIT_CODE=1
    fi
    cd ..
else
    echo "⚠ SwiftLint not installed, skipping..."
    echo "  Install with: brew install swiftlint"
fi
echo ""

# Summary
echo "========================================"
if [ $EXIT_CODE -eq 0 ]; then
    echo "✓ All code formatted successfully!"
    echo "  Review changes with: git diff"
else
    echo "✗ Some formatting operations failed."
fi
echo "========================================"

exit $EXIT_CODE
