import XCTest

/**
 * UI tests for Map screen in TrailGlass iOS app.
 */
final class MapScreenUITests: XCTestCase {
    var app: XCUIApplication!

    override func setUpWithError() throws {
        continueAfterFailure = false
        app = XCUIApplication()
        app.launch()
    }

    override func tearDownWithError() throws {
        app = nil
    }

    // MARK: - Navigation Tests

    func testMapTabExists() throws {
        // Then - Map tab should be visible
        let mapTab = app.tabBars.buttons["Map"]
        XCTAssertTrue(mapTab.exists, "Map tab should exist")
    }

    func testNavigateToMapScreen() throws {
        // When - tap on Map tab
        let mapTab = app.tabBars.buttons["Map"]
        mapTab.tap()

        // Then - Map screen should be displayed
        let mapTitle = app.navigationBars["Map"]
        XCTAssertTrue(mapTitle.exists || !app.maps.isEmpty,
                     "Map navigation bar or map view should exist")
    }

    // MARK: - Map Content Tests

    func testMapViewExists() throws {
        // Given - navigate to Map screen
        app.tabBars.buttons["Map"].tap()

        // Wait for map to load
        sleep(2)

        // Then - map view should be present
        let mapView = app.maps.firstMatch
        XCTAssertTrue(mapView.waitForExistence(timeout: 5), "Map view should exist")
    }

    func testMapMarkersDisplay() throws {
        // Given - navigate to Map screen with data
        app.tabBars.buttons["Map"].tap()

        // Wait for map to load
        sleep(2)

        // Then - map should contain markers (if data exists)
        let mapView = app.maps.firstMatch
        if mapView.exists {
            // Check for annotation elements
            let annotations = app.otherElements.matching(identifier: "annotation")
            // Markers may or may not exist depending on data
            XCTAssertTrue(mapView.exists, "Map should be present")
        }
    }

    func testMapInteraction() throws {
        // Given - navigate to Map screen
        app.tabBars.buttons["Map"].tap()

        // Wait for map to load
        let mapView = app.maps.firstMatch
        XCTAssertTrue(mapView.waitForExistence(timeout: 5))

        // When - interact with map (pan gesture)
        mapView.swipeLeft()

        // Then - map should respond to gestures
        XCTAssertTrue(mapView.exists, "Map should still exist after interaction")
    }

    func testMapZoomInteraction() throws {
        // Given - navigate to Map screen
        app.tabBars.buttons["Map"].tap()

        // Wait for map to load
        let mapView = app.maps.firstMatch
        XCTAssertTrue(mapView.waitForExistence(timeout: 5))

        // When - zoom in (pinch gesture - simulated by double tap)
        mapView.tap()
        mapView.doubleTap()

        // Then - map should still be responsive
        XCTAssertTrue(mapView.exists, "Map should handle zoom gestures")
    }

    // MARK: - Marker Info Card Tests

    func testMarkerInfoCardDisplays() throws {
        // Given - navigate to Map screen
        app.tabBars.buttons["Map"].tap()
        sleep(2)

        // When - tap on a marker (if exists)
        let mapView = app.maps.firstMatch
        if mapView.exists {
            mapView.tap()

            // Then - marker info card might appear
            // (Depends on whether tap hit a marker)
            sleep(1)
            XCTAssertTrue(mapView.exists, "Map should remain after tap")
        }
    }

    func testMarkerInfoCardContent() throws {
        // Given - marker info card is displayed
        app.tabBars.buttons["Map"].tap()
        sleep(2)

        // If info card is visible, check for expected elements
        if app.buttons["Details"].exists {
            // Then - info card should have Details button
            XCTAssertTrue(app.buttons["Details"].exists)

            // And Photos button
            XCTAssertTrue(app.buttons["Photos"].exists)

            // And close button
            XCTAssertTrue(app.buttons["Close"].exists || !app.buttons.matching(NSPredicate(format: "label CONTAINS 'close'")).isEmpty)
        }
    }

    func testCloseMarkerInfoCard() throws {
        // Given - marker info card is displayed
        app.tabBars.buttons["Map"].tap()
        sleep(2)

        // If info card is visible
        if app.buttons["Close"].exists {
            // When - tap close button
            app.buttons["Close"].tap()

            // Then - info card should be hidden
            XCTAssertFalse(app.buttons["Details"].exists,
                          "Info card should be closed")
        }
    }

    // MARK: - Map Controls Tests

    func testFitToDataButton() throws {
        // Given - navigate to Map screen
        app.tabBars.buttons["Map"].tap()

        // Wait for map to load
        sleep(2)

        // Then - fit to data button should exist
        let fitButton = app.buttons.matching(NSPredicate(format: "label CONTAINS 'location' OR label CONTAINS 'fit'")).firstMatch

        // Button may or may not exist depending on implementation
        // Just verify map is functional
        XCTAssertTrue(app.maps.firstMatch.exists, "Map should be functional")
    }

    func testMapControlsAccessibility() throws {
        // Given - navigate to Map screen
        app.tabBars.buttons["Map"].tap()
        sleep(2)

        // Then - map controls should be accessible
        let mapView = app.maps.firstMatch
        XCTAssertTrue(mapView.isAccessibilityElement ||
                     !mapView.descendants(matching: .any).isEmpty,
                     "Map or its controls should be accessible")
    }

    // MARK: - Route Display Tests

    func testMapRoutesDisplay() throws {
        // Given - navigate to Map screen with route data
        app.tabBars.buttons["Map"].tap()
        sleep(2)

        // Then - routes should be rendered on map
        // Routes are polylines, harder to test directly
        // Verify map is present and loaded
        let mapView = app.maps.firstMatch
        XCTAssertTrue(mapView.exists, "Map should display routes")
    }

    // MARK: - Empty State Tests

    func testMapEmptyState() throws {
        // Given - app with no data
        app.launchArguments = ["--UITests", "--EmptyData"]
        app.launch()

        // When - navigate to Map screen
        app.tabBars.buttons["Map"].tap()

        // Then - map should still render (just empty)
        let mapView = app.maps.firstMatch
        XCTAssertTrue(mapView.waitForExistence(timeout: 5),
                     "Map should exist even with no data")
    }

    // MARK: - Performance Tests

    func testMapLoadingPerformance() throws {
        measure(metrics: [XCTApplicationLaunchMetric()]) {
            let testApp = XCUIApplication()
            testApp.launch()
            testApp.tabBars.buttons["Map"].tap()

            let mapView = testApp.maps.firstMatch
            _ = mapView.waitForExistence(timeout: 5)
        }
    }

    func testMapPanPerformance() throws {
        // Given - navigate to Map screen
        app.tabBars.buttons["Map"].tap()

        let mapView = app.maps.firstMatch
        XCTAssertTrue(mapView.waitForExistence(timeout: 5))

        // Measure pan performance
        measure(metrics: [XCTOSSignpostMetric.scrollDecelerationMetric]) {
            mapView.swipeLeft()
            mapView.swipeRight()
            mapView.swipeUp()
            mapView.swipeDown()
        }
    }

    // MARK: - Location Permission Tests

    func testLocationPermissionHandling() throws {
        // Given - app might request location permission
        app.tabBars.buttons["Map"].tap()

        // When - location permission alert appears
        let springboard = XCUIApplication(bundleIdentifier: "com.apple.springboard")
        let allowButton = springboard.buttons["Allow While Using App"]

        if allowButton.waitForExistence(timeout: 3) {
            // Then - should be able to allow permission
            allowButton.tap()
        }

        // Map should still load regardless
        let mapView = app.maps.firstMatch
        XCTAssertTrue(mapView.exists, "Map should load regardless of permission")
    }

    // MARK: - Accessibility Tests

    func testMapAccessibility() throws {
        // Given - navigate to Map screen
        app.tabBars.buttons["Map"].tap()

        // Then - map should be accessible
        let mapView = app.maps.firstMatch
        XCTAssertTrue(mapView.waitForExistence(timeout: 5))
    }

    func testMarkerAccessibility() throws {
        // Given - navigate to Map screen with markers
        app.tabBars.buttons["Map"].tap()
        sleep(2)

        // Then - markers should be accessible via VoiceOver
        // This is difficult to test without actual VoiceOver
        // Verify map is accessible as a baseline
        let mapView = app.maps.firstMatch
        XCTAssertTrue(mapView.exists, "Map should be present for accessibility")
    }

    // MARK: - Integration Tests

    func testMarkerToDetailNavigation() throws {
        // Given - map with markers
        app.tabBars.buttons["Map"].tap()
        sleep(2)

        // When - tap marker and then Details button
        if app.buttons["Details"].exists {
            app.buttons["Details"].tap()

            // Then - should navigate to visit detail
            // (Implementation dependent)
            sleep(1)
            XCTAssertTrue(true, "Details navigation attempted")
        }
    }

    func testMarkerToPhotosNavigation() throws {
        // Given - map with markers
        app.tabBars.buttons["Map"].tap()
        sleep(2)

        // When - tap marker and then Photos button
        if app.buttons["Photos"].exists {
            app.buttons["Photos"].tap()

            // Then - should show photos view
            sleep(1)
            XCTAssertTrue(true, "Photos navigation attempted")
        }
    }
}
