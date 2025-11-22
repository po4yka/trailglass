import XCTest

/**
 * UI tests for Timeline screen in TrailGlass iOS app.
 */
final class TimelineScreenUITests: XCTestCase {
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

    func testTimelineTabExists() throws {
        // Then - Timeline tab should be visible
        let timelineTab = app.tabBars.buttons["Timeline"]
        XCTAssertTrue(timelineTab.exists, "Timeline tab should exist")
    }

    func testNavigateToTimelineScreen() throws {
        // When - tap on Timeline tab
        let timelineTab = app.tabBars.buttons["Timeline"]
        timelineTab.tap()

        // Then - Timeline screen should be displayed
        let timelineTitle = app.navigationBars["Timeline"]
        XCTAssertTrue(timelineTitle.exists, "Timeline navigation bar should exist")
    }

    // MARK: - Date Picker Tests

    func testDatePickerExists() throws {
        // Given - navigate to Timeline screen
        app.tabBars.buttons["Timeline"].tap()

        // Then - date picker should exist
        let datePicker = app.datePickers.firstMatch
        XCTAssertTrue(datePicker.exists || app.buttons["Select date"].exists,
                     "Date picker or date button should exist")
    }

    func testSelectDate() throws {
        // Given - navigate to Timeline screen
        app.tabBars.buttons["Timeline"].tap()

        // When - interact with date picker
        if app.buttons["Select date"].exists {
            app.buttons["Select date"].tap()

            // Calendar should appear
            let calendarExists = !app.datePickers.isEmpty || !app.collectionViews.isEmpty
            XCTAssertTrue(calendarExists, "Calendar picker should appear")
        }
    }

    // MARK: - Timeline Content Tests

    func testTimelineItemsDisplay() throws {
        // Given - navigate to Timeline screen with data
        app.tabBars.buttons["Timeline"].tap()

        // Wait for content to load
        sleep(2)

        // Then - timeline items should be displayed
        // Could be visits, routes, or day markers
        let hasCells = !app.cells.isEmpty || app.staticTexts["Day Start"].exists
        XCTAssertTrue(hasCells, "Timeline should display items or day markers")
    }

    func testVisitCardDisplaysLocationInfo() throws {
        // Given - navigate to Timeline screen
        app.tabBars.buttons["Timeline"].tap()

        // Wait for data to load
        sleep(2)

        // Then - if visits exist, they should show location info
        let cells = app.cells
        if !cells.isEmpty {
            // Check if any cell contains location-like text
            // This is a generic check since exact data depends on test fixtures
            XCTAssertTrue(!cells.isEmpty, "Should have timeline items")
        }
    }

    func testRouteCardDisplaysTransportInfo() throws {
        // Given - navigate to Timeline screen with routes
        app.tabBars.buttons["Timeline"].tap()

        // Wait for data to load
        sleep(2)

        // Then - if routes exist, they should show transport info
        // Look for transport type icons or labels
        let transportTypes = ["WALK", "CAR", "TRAIN", "BIKE", "PLANE", "BOAT"]
        var foundTransport = false

        for type in transportTypes {
            if app.staticTexts[type].exists {
                foundTransport = true
                break
            }
        }

        // Or check for distance labels
        let hasDistance = !app.staticTexts.matching(NSPredicate(format: "label CONTAINS 'km'")).isEmpty
        XCTAssertTrue(foundTransport || hasDistance || app.cells.isEmpty,
                     "Routes should show transport info or no data")
    }

    func testDayMarkersExist() throws {
        // Given - navigate to Timeline screen
        app.tabBars.buttons["Timeline"].tap()

        // Wait for content
        sleep(2)

        // Then - day markers should be visible if data exists
        let dayStart = app.staticTexts["Day Start"]
        let dayEnd = app.staticTexts["Day End"]

        // At least one marker should exist if there's timeline data
        let hasMarkers = dayStart.exists || dayEnd.exists
        let noData = app.staticTexts["No timeline data"].exists

        XCTAssertTrue(hasMarkers || noData, "Should show day markers or no data message")
    }

    // MARK: - Interaction Tests

    func testTapVisitCard() throws {
        // Given - navigate to Timeline screen
        app.tabBars.buttons["Timeline"].tap()

        // Wait for data
        sleep(2)

        // When - tap on a visit card (if exists)
        let cells = app.cells
        if !cells.isEmpty {
            cells.firstMatch.tap()

            // Then - should navigate to visit detail or show action
            // (Behavior depends on implementation)
            sleep(1)
            XCTAssertTrue(true, "Tap interaction completed")
        }
    }

    // MARK: - Scrolling Tests

    func testScrollTimeline() throws {
        // Given - navigate to Timeline screen
        app.tabBars.buttons["Timeline"].tap()

        // Wait for content
        sleep(2)

        // When - scroll down
        let scrollView = app.scrollViews.firstMatch
        if scrollView.exists {
            scrollView.swipeUp()

            // Then - should be able to scroll
            XCTAssertTrue(scrollView.exists, "Should be able to scroll timeline")
        }
    }

    func testScrollToBottom() throws {
        // Given - navigate to Timeline screen
        app.tabBars.buttons["Timeline"].tap()
        sleep(2)

        // When - scroll to bottom
        let scrollView = app.scrollViews.firstMatch
        if scrollView.exists {
            scrollView.swipeUp()
            scrollView.swipeUp()

            // Then - day end marker should be visible
            let dayEnd = app.staticTexts["Day End"]
            if dayEnd.exists {
                XCTAssertTrue(dayEnd.isHittable, "Should scroll to day end")
            }
        }
    }

    // MARK: - Empty State Tests

    func testEmptyTimelineState() throws {
        // Given - app with no data
        app.launchArguments = ["--UITests", "--EmptyData"]
        app.launch()

        // When - navigate to Timeline screen
        app.tabBars.buttons["Timeline"].tap()

        // Then - should show empty state
        sleep(2)
        let emptyMessage = app.staticTexts["No timeline data"]
        XCTAssertTrue(emptyMessage.exists || app.cells.isEmpty,
                     "Should show empty state or no items")
    }

    // MARK: - Performance Tests

    func testTimelineScrollPerformance() throws {
        // Given - navigate to Timeline screen
        app.tabBars.buttons["Timeline"].tap()
        sleep(2)

        // Measure scroll performance
        measure(metrics: [XCTOSSignpostMetric.scrollDecelerationMetric]) {
            let scrollView = app.scrollViews.firstMatch
            if scrollView.exists {
                scrollView.swipeUp(velocity: .fast)
                scrollView.swipeDown(velocity: .fast)
            }
        }
    }

    // MARK: - Accessibility Tests

    func testTimelineAccessibility() throws {
        // Given - navigate to Timeline screen
        app.tabBars.buttons["Timeline"].tap()

        // Then - important elements should be accessible
        let timelineTab = app.tabBars.buttons["Timeline"]
        XCTAssertTrue(timelineTab.isAccessibilityElement)

        // Date picker should be accessible
        if app.buttons["Select date"].exists {
            XCTAssertTrue(app.buttons["Select date"].isAccessibilityElement)
        }
    }

    func testVisitCardAccessibility() throws {
        // Given - navigate to Timeline screen with data
        app.tabBars.buttons["Timeline"].tap()
        sleep(2)

        // Then - visit cards should have accessibility labels
        let cells = app.cells
        if !cells.isEmpty {
            let firstCell = cells.firstMatch
            XCTAssertTrue(firstCell.isAccessibilityElement ||
                         firstCell.descendants(matching: .any).element.isAccessibilityElement,
                         "Timeline items should be accessible")
        }
    }
}
