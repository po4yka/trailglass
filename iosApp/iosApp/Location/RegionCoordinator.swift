import Foundation
import CoreLocation
import SwiftUI
import Shared
import Combine

/**
 Coordinates region monitoring, notifications, and state updates.

 This is the main integration point that:
 - Observes region changes from RegionViewModel
 - Updates CLLocationManager monitored regions
 - Handles region entry/exit events
 - Shows notifications for transitions
 - Updates region statistics
 - Logs events

 Usage:
 ```swift
 let coordinator = RegionCoordinator(
     monitoringManager: regionMonitoringManager,
     notificationManager: regionNotificationManager,
     viewModel: regionViewModel
 )
 coordinator.start()
 ```
 */
@MainActor
class RegionCoordinator: ObservableObject {

    // MARK: - Constants

    private let maxRecentEvents = 50

    // MARK: - Properties

    private let monitoringManager: RegionMonitoringManager
    private let notificationManager: RegionNotificationManager
    private let viewModel: RegionViewModel

    @Published var isActive: Bool = false
    @Published var lastEvent: RegionEventInfo?
    @Published var recentEvents: [RegionEventInfo] = []

    private var cancellables = Set<AnyCancellable>()

    // MARK: - Initialization

    init(
        monitoringManager: RegionMonitoringManager,
        notificationManager: RegionNotificationManager,
        viewModel: RegionViewModel
    ) {
        self.monitoringManager = monitoringManager
        self.notificationManager = notificationManager
        self.viewModel = viewModel

        print("RegionCoordinator initialized")
    }

    // MARK: - Public Methods

    /**
     Start coordinating region monitoring and notifications.
     */
    func start() {
        print("RegionCoordinator starting...")

        // Request permissions
        setupPermissions()

        // Setup region monitoring subscriptions
        setupRegionMonitoring()

        // Setup notification handling
        setupNotificationHandling()

        // Observe region changes from ViewModel
        observeRegionChanges()

        // Load initial regions
        viewModel.loadRegions()

        isActive = true
        print("RegionCoordinator started")
    }

    /**
     Stop coordinating and clean up.
     */
    func stop() {
        print("RegionCoordinator stopping...")

        monitoringManager.stopAllMonitoring()
        cancellables.removeAll()

        isActive = false
        print("RegionCoordinator stopped")
    }

    /**
     Refresh regions from the ViewModel and update monitoring.
     */
    func refresh() {
        print("RegionCoordinator refreshing...")
        viewModel.loadRegions()
    }

    // MARK: - Private Methods - Setup

    private func setupPermissions() {
        // Request location permission for region monitoring
        monitoringManager.requestLocationPermission()

        // Request notification permission
        Task {
            let granted = await notificationManager.requestAuthorization()
            if !granted {
                print("Notification permission denied - notifications will not be shown")
            }
        }
    }

    private func setupRegionMonitoring() {
        // Subscribe to region entered events
        monitoringManager.regionEnteredSubject
            .sink { [weak self] region in
                self?.handleRegionEntered(region)
            }
            .store(in: &cancellables)

        // Subscribe to region exited events
        monitoringManager.regionExitedSubject
            .sink { [weak self] region in
                self?.handleRegionExited(region)
            }
            .store(in: &cancellables)

        // Subscribe to monitoring errors
        monitoringManager.errorSubject
            .sink { [weak self] errorMessage in
                self?.handleMonitoringError(errorMessage)
            }
            .store(in: &cancellables)
    }

    private func setupNotificationHandling() {
        // Subscribe to notification taps
        notificationManager.notificationTappedSubject
            .sink { [weak self] regionId in
                self?.handleNotificationTapped(regionId: regionId)
            }
            .store(in: &cancellables)
    }

    private func observeRegionChanges() {
        // Observe changes to the regions array
        viewModel.$regions
            .dropFirst() // Skip initial empty value
            .sink { [weak self] regions in
                self?.updateMonitoredRegions(regions)
            }
            .store(in: &cancellables)
    }

    // MARK: - Private Methods - Event Handlers

    private func handleRegionEntered(_ region: Region) {
        print("Region entered: \(region.name)")

        // Update last event
        let eventInfo = RegionEventInfo(
            region: region,
            type: .enter,
            timestamp: Date()
        )
        lastEvent = eventInfo

        // Show notification
        notificationManager.notifyRegionEntered(region)

        // Update statistics
        updateRegionStatistics(region: region, eventType: .enter)

        // Log event
        logRegionEvent(region: region, eventType: .enter)

        // Save event to database via RegionRepository when available
        // For now, store in local cache until KMP RegionRepository is ready
        addRecentEvent(eventInfo)

        // TODO: When KMP RegionRepository is ready, uncomment:
        // Task {
        //     do {
        //         try await regionRepository.recordRegionEvent(
        //             regionId: region.id,
        //             eventType: .ENTER,
        //             timestamp: event.timestamp,
        //             coordinate: event.coordinate
        //         )
        //     } catch {
        //         print("Failed to save region event: \(error)")
        //     }
        // }
    }

    private func handleRegionExited(_ region: Region) {
        print("Region exited: \(region.name)")

        // Update last event
        let eventInfo = RegionEventInfo(
            region: region,
            type: .exit,
            timestamp: Date()
        )
        lastEvent = eventInfo

        // Show notification
        notificationManager.notifyRegionExited(region)

        // Log event
        logRegionEvent(region: region, eventType: .exit)

        // Save event to database via RegionRepository when available
        // For now, store in local cache until KMP RegionRepository is ready
        addRecentEvent(eventInfo)

        // TODO: When KMP RegionRepository is ready, uncomment:
        // Task {
        //     do {
        //         try await regionRepository.recordRegionEvent(
        //             regionId: region.id,
        //             eventType: .EXIT,
        //             timestamp: event.timestamp,
        //             coordinate: event.coordinate
        //         )
        //     } catch {
        //         print("Failed to save region event: \(error)")
        //     }
        // }
    }

    private func handleMonitoringError(_ errorMessage: String) {
        print("Region monitoring error: \(errorMessage)")
        viewModel.error = errorMessage
    }

    private func handleNotificationTapped(regionId: String) {
        #if DEBUG
        print("Notification tapped for region: \(regionId)")
        #endif

        // Navigate to map view showing the region
        // Post notification that the app's navigation system can listen to
        NotificationCenter.default.post(
            name: .showRegionOnMap,
            object: nil,
            userInfo: [
                "regionId": regionId,
                "action": "show_region",
                "timestamp": Date()
            ]
        )

        // Also post a more general navigation notification
        NotificationCenter.default.post(
            name: .navigateToMap,
            object: nil,
            userInfo: [
                "tab": "map",
                "regionId": regionId,
                "zoomToRegion": true
            ]
        )
    }

    /// Add event to recent events list with efficient array manipulation
    private func addRecentEvent(_ event: RegionEventInfo) {
        recentEvents.insert(event, at: 0)
        if recentEvents.count > maxRecentEvents {
            recentEvents.removeLast()
        }
    }

    private func updateMonitoredRegions(_ regions: [Region]) {
        print("Updating monitored regions: \(regions.count) total")

        // Filter to only regions with notifications enabled
        let enabledRegions = regions.filter { $0.notificationsEnabled }

        // Start monitoring the updated set of regions
        monitoringManager.startMonitoring(regions: enabledRegions)
    }

    private func updateRegionStatistics(region: Region, eventType: RegionEventType) {
        // Create updated region with incremented enter count and updated timestamp
        if eventType == .enter {
            let now = Date()
            let updatedRegion = Region(
                id: region.id,
                userId: region.userId,
                name: region.name,
                description: region.description,
                latitude: region.latitude,
                longitude: region.longitude,
                radiusMeters: region.radiusMeters,
                notificationsEnabled: region.notificationsEnabled,
                createdAt: region.createdAt,
                updatedAt: now.kotlinInstant,
                enterCount: region.enterCount + 1,
                lastEnterTime: now.kotlinInstant,
                lastExitTime: nil
            )

            // Update in ViewModel
            viewModel.updateRegion(updatedRegion)
        }
    }

    private func logRegionEvent(region: Region, eventType: RegionEventType) {
        let eventName = eventType == .enter ? "ENTER" : "EXIT"
        print("""
        [REGION EVENT] \(eventName)
          Region: \(region.name) (\(region.id))
          Location: \(region.latitude), \(region.longitude)
          Radius: \(region.radiusMeters)m
          Time: \(Date())
        """)
    }
}

// MARK: - Supporting Types

/**
 Information about a region event for display.
 */
struct RegionEventInfo {
    let region: Region
    let type: RegionEventType
    let timestamp: Date

    var displayText: String {
        let action = type == .enter ? "Entered" : "Left"
        return "\(action) \(region.name)"
    }

    var iconName: String {
        return type == .enter ? "arrow.down.circle.fill" : "arrow.up.circle.fill"
    }

    var eventColor: Color {
        return type == .enter ? .green : .orange
    }
}
