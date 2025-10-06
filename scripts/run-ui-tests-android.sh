#!/bin/bash

# TrailGlass Android UI Test Runner
# Runs Android instrumented tests (requires connected device or emulator)

set -e

echo "========================================="
echo "TrailGlass Android UI Tests"
echo "========================================="

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Navigate to project root
cd "$(dirname "$0")/.."

# Check if device/emulator is connected
echo -e "\n${BLUE}Checking for connected devices...${NC}"
DEVICES=$(adb devices | grep -w "device" | wc -l)

if [ "$DEVICES" -eq 0 ]; then
    echo -e "${RED}Error: No Android device or emulator connected!${NC}"
    echo "Please start an emulator or connect a device and try again."
    exit 1
fi

echo -e "${GREEN}Found $DEVICES connected device(s)${NC}"

# Run instrumented tests
echo -e "\n${BLUE}Running Android UI tests...${NC}"
./gradlew :composeApp:connectedAndroidTest

echo -e "\n${GREEN}=========================================${NC}"
echo -e "${GREEN}Android UI Tests Complete!${NC}"
echo -e "${GREEN}=========================================${NC}"

echo -e "\n${YELLOW}Test Report:${NC}"
echo "  composeApp/build/reports/androidTests/connected/index.html"

echo -e "\nOpen the HTML report in your browser to view test results."
