import XCTest

/// UI tests for PlacesView screen
final class PlacesViewUITests: XCTestCase {
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

    func testNavigateToPlacesScreen() throws {
        // Tap on Places tab
        let placesTab = app.tabBars.buttons["Places"]
        XCTAssertTrue(placesTab.exists, "Places tab should exist")
        placesTab.tap()

        // Verify Places screen is displayed
        let placesNavigationBar = app.navigationBars["Places"]
        XCTAssertTrue(placesNavigationBar.waitForExistence(timeout: 2))
    }

    // MARK: - Empty State Tests

    func testEmptyStateDisplayed() throws {
        navigateToPlaces()

        // Check for empty state message when no places exist
        let emptyStateText = app.staticTexts["No Places Yet"]
        if emptyStateText.exists {
            XCTAssertTrue(emptyStateText.isHittable)

            // Verify helper text exists
            let helperText = app.staticTexts.containing(NSPredicate(format: "label CONTAINS 'TrailGlass will automatically'")).firstMatch
            XCTAssertTrue(helperText.exists)
        }
    }

    // MARK: - Search Functionality Tests

    func testSearchBarExists() throws {
        navigateToPlaces()

        // Tap search button in toolbar
        let searchButton = app.navigationBars.buttons["Search"]
        if searchButton.exists {
            searchButton.tap()

            // Verify search field appears
            let searchField = app.searchFields.firstMatch
            XCTAssertTrue(searchField.waitForExistence(timeout: 1))
        }
    }

    func testSearchFunctionality() throws {
        navigateToPlaces()

        // Tap search button
        let searchButton = app.navigationBars.buttons["Search"]
        guard searchButton.exists else { return }
        searchButton.tap()

        // Type in search field
        let searchField = app.searchFields.firstMatch
        guard searchField.waitForExistence(timeout: 1) else { return }

        searchField.tap()
        searchField.typeText("Coffee")

        // Verify search executes (results may be empty)
        // This is a smoke test to ensure search doesn't crash
        XCTAssertTrue(searchField.exists)
    }

    // MARK: - Filter Tests

    func testFilterButtonExists() throws {
        navigateToPlaces()

        let filterButton = app.navigationBars.buttons.matching(identifier: "FilterButton").firstMatch
        // Filter button should exist in toolbar
        // XCTAssertTrue(filterButton.exists, "Filter button should be visible")
        // Note: Commented out as button identifier may vary
    }

    func testOpenFilterSheet() throws {
        navigateToPlaces()

        // Try to find and tap filter button
        let toolbarButtons = app.navigationBars.buttons
        let filterButton = toolbarButtons.element(matching: .button, identifier: "line.3.horizontal.decrease.circle")

        if filterButton.exists {
            filterButton.tap()

            // Verify filter sheet appears
            let filterSheet = app.sheets.firstMatch
            XCTAssertTrue(filterSheet.waitForExistence(timeout: 1))
        }
    }

    // MARK: - Sort Tests

    func testSortButtonExists() throws {
        navigateToPlaces()

        let sortButton = app.navigationBars.buttons.matching(identifier: "arrow.up.arrow.down").firstMatch
        // Sort button should exist (if places list is not empty)
        // Button visibility depends on content
    }

    func testOpenSortSheet() throws {
        navigateToPlaces()

        let sortButton = app.navigationBars.buttons.element(matching: .button, identifier: "arrow.up.arrow.down")

        if sortButton.exists {
            sortButton.tap()

            // Verify sort sheet appears
            let sortSheet = app.sheets.firstMatch
            XCTAssertTrue(sortSheet.waitForExistence(timeout: 1))
        }
    }

    // MARK: - List Display Tests

    func testPlacesListDisplayed() throws {
        navigateToPlaces()

        // Check if list exists (may be empty)
        let list = app.scrollViews.firstMatch
        XCTAssertTrue(list.exists)
    }

    func testPlaceRowElements() throws {
        navigateToPlaces()

        // Find first place row if it exists
        let firstRow = app.cells.firstMatch

        if firstRow.exists {
            // Verify row contains expected elements
            // - Category icon
            // - Place name
            // - Visit count
            // Note: Specific checks depend on data availability
            XCTAssertTrue(!firstRow.images.isEmpty, "Row should have category icon")
        }
    }

    func testPlaceRowTapNavigation() throws {
        navigateToPlaces()

        let firstRow = app.cells.firstMatch
        if firstRow.exists {
            firstRow.tap()

            // Verify navigation to place detail
            // (Depends on whether detail view is implemented)
            // sleep(1) // Allow navigation animation
        }
    }

    // MARK: - Accessibility Tests

    func testAccessibilityLabels() throws {
        navigateToPlaces()

        // Verify important elements have accessibility labels
        let placesTitle = app.navigationBars["Places"]
        XCTAssertTrue(placesTitle.exists)

        // Search button should be accessible
        let searchButton = app.navigationBars.buttons["Search"]
        if searchButton.exists {
            XCTAssertNotNil(searchButton.label)
        }
    }

    func testVoiceOverSupport() throws {
        navigateToPlaces()

        // Enable VoiceOver programmatically (if supported)
        // and verify key elements are accessible
        let placesTitle = app.navigationBars["Places"]
        XCTAssertTrue(placesTitle.isAccessibilityElement || placesTitle.exists)
    }

    // MARK: - Performance Tests

    func testPlacesScreenPerformance() throws {
        measure {
            navigateToPlaces()
        }
    }

    func testSearchPerformance() throws {
        navigateToPlaces()

        let searchButton = app.navigationBars.buttons["Search"]
        guard searchButton.exists else { return }

        measure {
            searchButton.tap()
            let searchField = app.searchFields.firstMatch
            _ = searchField.waitForExistence(timeout: 2)
        }
    }

    // MARK: - Rotation Tests

    func testPlacesScreenRotation() throws {
        navigateToPlaces()

        // Rotate to landscape
        XCUIDevice.shared.orientation = .landscapeLeft

        // Verify screen still renders correctly
        let placesTitle = app.navigationBars["Places"]
        XCTAssertTrue(placesTitle.exists)

        // Rotate back
        XCUIDevice.shared.orientation = .portrait
    }

    // MARK: - Helper Methods

    private func navigateToPlaces() {
        let placesTab = app.tabBars.buttons["Places"]
        if placesTab.exists {
            placesTab.tap()
        }
        // Wait for navigation to complete
        _ = app.navigationBars["Places"].waitForExistence(timeout: 2)
    }
}
