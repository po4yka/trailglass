#!/bin/bash

# TrailGlass Test Runner
# Runs all tests and generates coverage reports
# Target: 75%+ code coverage

set -e

echo "========================================="
echo "TrailGlass Test Suite"
echo "========================================="

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Navigate to project root
cd "$(dirname "$0")/.."

# 1. Run shared module tests (Kotlin Multiplatform)
echo -e "\n${BLUE}[1/4] Running shared module tests...${NC}"
./gradlew :shared:test --info

# 2. Run Android unit tests
echo -e "\n${BLUE}[2/4] Running Android unit tests...${NC}"
./gradlew :composeApp:testDebugUnitTest --info

# 3. Generate coverage reports with Kover
echo -e "\n${BLUE}[3/4] Generating coverage reports...${NC}"
./gradlew koverHtmlReport koverXmlReport

# 4. Display coverage summary
echo -e "\n${BLUE}[4/4] Coverage Summary${NC}"
./gradlew koverVerify

echo -e "\n${GREEN}=========================================${NC}"
echo -e "${GREEN}Test Suite Complete!${NC}"
echo -e "${GREEN}=========================================${NC}"

echo -e "\n${YELLOW}Coverage Reports:${NC}"
echo "  - HTML: shared/build/reports/kover/html/index.html"
echo "  - XML:  shared/build/reports/kover/report.xml"

echo -e "\n${YELLOW}Test Reports:${NC}"
echo "  - Shared: shared/build/reports/tests/test/index.html"
echo "  - Android: composeApp/build/reports/tests/testDebugUnitTest/index.html"

echo -e "\n${YELLOW}Target: 75%+ coverage${NC}"
echo "Open the HTML reports in your browser to view detailed coverage."
