# iOS UI Tests

Comprehensive UI tests for the TrailGlass iOS app using XCTest framework.

## Test Files

- **StatsScreenUITests.swift**: Tests for the Statistics screen
- **TimelineScreenUITests.swift**: Tests for the Timeline screen
- **MapScreenUITests.swift**: Tests for the Map visualization screen

## Running Tests

### From Xcode

1. Open `iosApp.xcodeproj` in Xcode
2. Select a test target (iOS Simulator recommended)
3. Run tests:
   - **All tests**: Product → Test (⌘U)
   - **Single test file**: Click diamond icon next to test class
   - **Single test**: Click diamond icon next to test method

### From Command Line

```bash
# Run all UI tests
xcodebuild test \
  -project iosApp.xcodeproj \
  -scheme iosApp \
  -destination 'platform=iOS Simulator,name=iPhone 15,OS=latest'

# Run specific test class
xcodebuild test \
  -project iosApp.xcodeproj \
  -scheme iosApp \
  -destination 'platform=iOS Simulator,name=iPhone 15,OS=latest' \
  -only-testing:iosAppUITests/StatsScreenUITests
```

## Test Coverage

**Target: 75%+ coverage**

### Generating Coverage Reports

1. In Xcode, enable code coverage:
   - Product → Scheme → Edit Scheme
   - Test → Options
   - Check "Code Coverage"
   - Select "iosApp" target

2. Run tests (⌘U)

3. View coverage:
   - Report Navigator (⌘9)
   - Select latest test run
   - Click "Coverage" tab

### Coverage Report from Command Line

```bash
# Run tests with coverage
xcodebuild test \
  -project iosApp.xcodeproj \
  -scheme iosApp \
  -destination 'platform=iOS Simulator,name=iPhone 15,OS=latest' \
  -enableCodeCoverage YES

# Generate coverage report (requires xcov or similar tool)
# Install xcov: gem install xcov
xcov --scheme iosApp --workspace iosApp.xcworkspace
```

## Test Categories

### Navigation Tests
- Tab bar navigation
- Screen transitions
- Back navigation

### Content Tests
- UI elements presence
- Data display
- Empty states

### Interaction Tests
- Button taps
- Gestures (swipe, pinch, pan)
- Form inputs

### Performance Tests
- Launch time
- Scroll performance
- Render time

### Accessibility Tests
- VoiceOver support
- Dynamic Type
- Accessibility labels

## Test Data

Tests use mock data or launch arguments:

```swift
// Empty data state
app.launchArguments = ["--UITests", "--EmptyData"]

// Specific test scenario
app.launchArguments = ["--UITests", "--Scenario:FullData"]
```

## Best Practices

1. **Use waitForExistence**: Always wait for elements before interacting
   ```swift
   XCTAssertTrue(element.waitForExistence(timeout: 5))
   ```

2. **Accessibility Identifiers**: Set identifiers for testability
   ```swift
   .accessibilityIdentifier("mapView")
   ```

3. **Avoid Hard-Coded Waits**: Use `waitForExistence` instead of `sleep()`
   ```swift
   // Bad
   sleep(2)

   // Good
   element.waitForExistence(timeout: 5)
   ```

4. **Test Independence**: Each test should be independent
   ```swift
   override func setUpWithError() throws {
       app = XCUIApplication()
       app.launch()
   }
   ```

5. **Descriptive Test Names**: Use clear, descriptive names
   ```swift
   func testStatsScreen_displaysOverviewCards()
   func testMapMarker_showsInfoCardOnTap()
   ```

## Troubleshooting

### Tests Fail to Find Elements

- Ensure app is built for testing (Debug configuration)
- Check accessibility identifiers are set
- Verify element hierarchy with Xcode's Accessibility Inspector

### Slow Test Execution

- Use `continueAfterFailure = false` to stop on first failure
- Run tests in parallel (Xcode → Scheme → Test → Options)
- Use simulator instead of physical device

### Flaky Tests

- Add proper wait conditions
- Avoid hard-coded waits
- Ensure test data is deterministic
- Clean build folder if needed

## CI/CD Integration

### GitHub Actions Example

```yaml
- name: Run iOS UI Tests
  run: |
    xcodebuild test \
      -project iosApp.xcodeproj \
      -scheme iosApp \
      -destination 'platform=iOS Simulator,name=iPhone 15,OS=latest' \
      -enableCodeCoverage YES \
      -resultBundlePath TestResults.xcresult

- name: Upload Coverage
  run: |
    xcov --scheme iosApp \
      --output_directory coverage
```

## Resources

- [XCTest Documentation](https://developer.apple.com/documentation/xctest)
- [UI Testing in Xcode](https://developer.apple.com/library/archive/documentation/DeveloperTools/Conceptual/testing_with_xcode/chapters/09-ui_testing.html)
- [WWDC: UI Testing Tips](https://developer.apple.com/videos/play/wwdc2015/406/)

## Coverage Goals

| Module | Target | Current |
|--------|--------|---------|
| Stats Screen | 75% | - |
| Timeline Screen | 75% | - |
| Map Screen | 75% | - |
| Overall | 75% | - |

Run tests regularly and monitor coverage to maintain quality standards.
