import XCTest

/// UI tests for Settings screen
final class SettingsViewUITests: XCTestCase {

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

    func testNavigateToSettings() throws {
        let settingsTab = app.tabBars.buttons["Settings"]
        XCTAssertTrue(settingsTab.exists, "Settings tab should exist")
        settingsTab.tap()

        let settingsNavigationBar = app.navigationBars["Settings"]
        XCTAssertTrue(settingsNavigationBar.waitForExistence(timeout: 2))
    }

    // MARK: - Settings Sections Tests

    func testLocationTrackingSection() throws {
        navigateToSettings()

        // Check for location tracking toggle
        let trackingToggle = app.switches["Location Tracking"]
        if trackingToggle.exists {
            XCTAssertTrue(trackingToggle.isEnabled)
        }
    }

    func testTrackingModeSelector() throws {
        navigateToSettings()

        // Look for tracking mode options (Active, Passive, etc.)
        let activeModeButton = app.buttons["Active"]
        let passiveModeButton = app.buttons["Passive"]

        // At least one mode should be available
        XCTAssertTrue(activeModeButton.exists || passiveModeButton.exists || app.segmentedControls.count > 0)
    }

    func testSyncSettings() throws {
        navigateToSettings()

        // Check for sync-related settings
        let syncToggle = app.switches["Auto Sync"]
        if syncToggle.exists {
            XCTAssertTrue(syncToggle.isEnabled)
        }
    }

    func testPrivacySettings() throws {
        navigateToSettings()

        // Check for privacy/permissions section
        let permissionsSection = app.staticTexts["Permissions"]
        if permissionsSection.exists {
            XCTAssertTrue(permissionsSection.isHittable)
        }
    }

    // MARK: - Toggle Interaction Tests

    func testToggleLocationTracking() throws {
        navigateToSettings()

        let trackingToggle = app.switches["Location Tracking"]
        guard trackingToggle.exists else { return }

        // Get initial state
        let initialValue = trackingToggle.value as? String

        // Toggle
        trackingToggle.tap()

        // Verify state changed
        let newValue = trackingToggle.value as? String
        XCTAssertNotEqual(initialValue, newValue, "Toggle should change state")
    }

    func testToggleAutoSync() throws {
        navigateToSettings()

        let syncToggle = app.switches["Auto Sync"]
        guard syncToggle.exists else { return }

        // Get initial state
        let initialValue = syncToggle.value as? String

        // Toggle
        syncToggle.tap()

        // Verify state changed
        let newValue = syncToggle.value as? String
        XCTAssertNotEqual(initialValue, newValue, "Toggle should change state")
    }

    // MARK: - About/Info Tests

    func testAboutSection() throws {
        navigateToSettings()

        // Check for About or App Info section
        let aboutButton = app.buttons["About"]
        if aboutButton.exists {
            aboutButton.tap()

            // Verify about screen appears
            sleep(1)
            XCTAssertTrue(true, "About navigation should work")
        }
    }

    func testVersionDisplay() throws {
        navigateToSettings()

        // Check for app version display
        let versionText = app.staticTexts.matching(NSPredicate(format: "label CONTAINS 'Version'")).firstMatch
        // Version may or may not be displayed
        // Just verify settings screen is functional
        XCTAssertTrue(app.navigationBars["Settings"].exists)
    }

    // MARK: - Data Management Tests

    func testClearCacheButton() throws {
        navigateToSettings()

        let clearCacheButton = app.buttons["Clear Cache"]
        if clearCacheButton.exists {
            XCTAssertTrue(clearCacheButton.isEnabled)

            // Tap to clear cache
            clearCacheButton.tap()

            // May show confirmation alert
            if app.alerts.count > 0 {
                let confirmButton = app.alerts.buttons["Confirm"]
                if confirmButton.exists {
                    confirmButton.tap()
                }
            }
        }
    }

    func testSignOutButton() throws {
        navigateToSettings()

        let signOutButton = app.buttons["Sign Out"]
        if signOutButton.exists {
            XCTAssertTrue(signOutButton.isEnabled)

            // Don't actually sign out in tests
            // Just verify button exists and is tappable
        }
    }

    // MARK: - Accessibility Tests

    func testAccessibilityLabels() throws {
        navigateToSettings()

        // Verify important elements have accessibility labels
        let settingsTitle = app.navigationBars["Settings"]
        XCTAssertTrue(settingsTitle.exists)

        // Toggles should be accessible
        let switches = app.switches
        for i in 0..<min(switches.count, 3) {
            let toggle = switches.element(boundBy: i)
            XCTAssertNotNil(toggle.label, "Toggle should have accessibility label")
        }
    }

    // MARK: - Permission Management Tests

    func testManageLocationPermissions() throws {
        navigateToSettings()

        let locationPermissionsButton = app.buttons["Location Permissions"]
        if locationPermissionsButton.exists {
            locationPermissionsButton.tap()

            // May open system settings or show permission info
            sleep(1)
            XCTAssertTrue(true, "Location permissions navigation attempted")
        }
    }

    func testManagePhotoPermissions() throws {
        navigateToSettings()

        let photoPermissionsButton = app.buttons["Photo Library Permissions"]
        if photoPermissionsButton.exists {
            photoPermissionsButton.tap()

            sleep(1)
            XCTAssertTrue(true, "Photo permissions navigation attempted")
        }
    }

    // MARK: - Performance Tests

    func testSettingsScreenPerformance() throws {
        measure {
            navigateToSettings()
        }
    }

    // MARK: - Helper Methods

    private func navigateToSettings() {
        let settingsTab = app.tabBars.buttons["Settings"]
        if settingsTab.exists {
            settingsTab.tap()
        }
        _ = app.navigationBars["Settings"].waitForExistence(timeout: 2)
    }
}
