import XCTest

/**
 * UI tests for Stats screen in TrailGlass iOS app.
 *
 * To run these tests:
 * 1. Open iosApp.xcodeproj in Xcode
 * 2. Select Product > Test (Cmd+U)
 * 3. Or run specific test: Click the diamond icon next to test method
 *
 * Test coverage target: 75%+
 */
final class StatsScreenUITests: XCTestCase {

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

    func testStatsTabExists() throws {
        // Given - app is launched

        // Then - Stats tab should be visible
        let statsTab = app.tabBars.buttons["Stats"]
        XCTAssertTrue(statsTab.exists, "Stats tab should exist")
    }

    func testNavigateToStatsScreen() throws {
        // When - tap on Stats tab
        let statsTab = app.tabBars.buttons["Stats"]
        statsTab.tap()

        // Then - Stats screen should be displayed
        let statsTitle = app.navigationBars["Stats"]
        XCTAssertTrue(statsTitle.exists, "Stats navigation bar should exist")
    }

    // MARK: - Content Tests

    func testStatsOverviewCardsExist() throws {
        // Given - navigate to Stats screen
        app.tabBars.buttons["Stats"].tap()

        // Then - overview cards should be displayed
        XCTAssertTrue(app.staticTexts["Countries"].exists)
        XCTAssertTrue(app.staticTexts["Cities"].exists)
        XCTAssertTrue(app.staticTexts["Trips"].exists)
        XCTAssertTrue(app.staticTexts["Days"].exists)
    }

    func testStatsDisplaysCountryCount() throws {
        // Given - navigate to Stats screen
        app.tabBars.buttons["Stats"].tap()

        // Wait for data to load
        let countriesLabel = app.staticTexts["Countries"]
        XCTAssertTrue(countriesLabel.waitForExistence(timeout: 5))

        // Then - country count should be displayed
        // The actual count will depend on test data
        let exists = app.staticTexts.matching(NSPredicate(format: "label MATCHES %@", "\\d+")).count > 0
        XCTAssertTrue(exists, "Country count should be displayed")
    }

    func testTopCountriesSectionExists() throws {
        // Given - navigate to Stats screen
        app.tabBars.buttons["Stats"].tap()

        // When - scroll to Top Countries section
        let topCountriesHeader = app.staticTexts["Top Countries"]

        // Then - section should exist
        if !topCountriesHeader.exists {
            // Scroll down if not immediately visible
            app.swipeUp()
        }

        XCTAssertTrue(topCountriesHeader.waitForExistence(timeout: 2))
    }

    func testTopCitiesSectionExists() throws {
        // Given - navigate to Stats screen
        app.tabBars.buttons["Stats"].tap()

        // When - scroll to Top Cities section
        let topCitiesHeader = app.staticTexts["Top Cities"]

        // Then - section should exist
        if !topCitiesHeader.exists {
            // Scroll down to find it
            app.swipeUp()
        }

        XCTAssertTrue(topCitiesHeader.waitForExistence(timeout: 2))
    }

    // MARK: - Period Filter Tests

    func testPeriodFilterExists() throws {
        // Given - navigate to Stats screen
        app.tabBars.buttons["Stats"].tap()

        // Then - period filter chips should exist
        XCTAssertTrue(app.buttons["Year"].exists || app.staticTexts["Year"].exists)
        XCTAssertTrue(app.buttons["Month"].exists || app.staticTexts["Month"].exists)
    }

    func testSwitchPeriodFilter() throws {
        // Given - navigate to Stats screen
        app.tabBars.buttons["Stats"].tap()

        // When - tap on Month filter
        let monthButton = app.buttons["Month"]
        if monthButton.exists {
            monthButton.tap()

            // Then - stats should update (content still visible)
            XCTAssertTrue(app.staticTexts["Countries"].exists)
        }
    }

    // MARK: - Scrolling Tests

    func testScrollToBottomContent() throws {
        // Given - navigate to Stats screen
        app.tabBars.buttons["Stats"].tap()

        // Wait for content to load
        XCTAssertTrue(app.staticTexts["Countries"].waitForExistence(timeout: 5))

        // When - scroll to bottom
        let scrollView = app.scrollViews.firstMatch
        scrollView.swipeUp()
        scrollView.swipeUp()

        // Then - bottom content should be visible
        // Either Top Cities or country/city list items should be visible
        let topCitiesExists = app.staticTexts["Top Cities"].exists
        let hasListItems = app.tables.cells.count > 0

        XCTAssertTrue(topCitiesExists || hasListItems, "Bottom content should be visible after scrolling")
    }

    // MARK: - Empty State Tests

    func testStatsEmptyState() throws {
        // Given - fresh install or no data
        // (This test assumes app can start with no data)
        app.launchArguments = ["--UITests", "--EmptyData"]
        app.launch()

        // When - navigate to Stats screen
        app.tabBars.buttons["Stats"].tap()

        // Then - should display empty state or zeros
        // Implementation depends on design choice
        let countriesCard = app.staticTexts["Countries"]
        XCTAssertTrue(countriesCard.exists, "Stats cards should exist even with no data")
    }

    // MARK: - Performance Tests

    func testStatsScreenLaunchPerformance() throws {
        measure(metrics: [XCTApplicationLaunchMetric()]) {
            XCUIApplication().launch()
        }
    }

    func testStatsScreenScrollPerformance() throws {
        // Given - navigate to Stats screen
        app.tabBars.buttons["Stats"].tap()
        XCTAssertTrue(app.staticTexts["Countries"].waitForExistence(timeout: 5))

        // Measure scroll performance
        measure(metrics: [XCTOSSignpostMetric.scrollDecelerationMetric]) {
            let scrollView = app.scrollViews.firstMatch
            scrollView.swipeUp(velocity: .fast)
            scrollView.swipeDown(velocity: .fast)
        }
    }

    // MARK: - Accessibility Tests

    func testStatsScreenAccessibility() throws {
        // Given - navigate to Stats screen
        app.tabBars.buttons["Stats"].tap()

        // Then - important elements should have accessibility labels
        XCTAssertTrue(app.staticTexts["Countries"].isAccessibilityElement)
        XCTAssertTrue(app.staticTexts["Cities"].isAccessibilityElement)
    }
}
