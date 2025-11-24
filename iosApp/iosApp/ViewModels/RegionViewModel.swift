import SwiftUI
import Shared
import Combine

/**
 ViewModel for managing regions (geofences) in the iOS app.

 This ViewModel will serve as a wrapper around the KMP RegionController when it's implemented.
 Currently provides a SwiftUI-friendly interface for Region CRUD operations.

 Features:
 - Observe regions from shared KMP layer
 - Create, update, and delete regions
 - Convert Region models to SwiftUI-friendly properties
 - Handle loading and error states
 */
class RegionViewModel: ObservableObject {

    // MARK: - Published Properties

    @Published var regions: [Region] = []
    @Published var isLoading: Bool = false
    @Published var error: String?

    // MARK: - Private Properties

    private let regionsController: RegionsController
    private var cancellables = Set<AnyCancellable>()
    private var stateObserver: KotlinJob?

    // MARK: - Initialization

    init(regionsController: RegionsController) {
        self.regionsController = regionsController
        print("RegionViewModel initialized")
        observeState()
    }

    convenience init(appComponent: AppComponent) {
        self.init(regionsController: appComponent.regionsController)
    }

    // MARK: - State Observation

    private func observeState() {
        stateObserver = regionsController.state.subscribe { [weak self] state in
            guard let self = self, let state = state else { return }

            DispatchQueue.main.async {
                self.regions = state.regions
                self.isLoading = state.isLoading
                self.error = state.error
            }
        }
    }

    deinit {
        stateObserver?.cancel(cause: nil)
        regionsController.cleanup()
    }

    // MARK: - Public Methods

    /**
     Load all regions for the current user.
     */
    func loadRegions() {
        regionsController.loadRegions()
    }

    /**
     Create a new region.

     - Parameters:
       - name: Name of the region
       - description: Optional description
       - latitude: Center latitude
       - longitude: Center longitude
       - radiusMeters: Radius in meters
       - notificationsEnabled: Whether to show notifications
     */
    func createRegion(
        name: String,
        description: String? = nil,
        latitude: Double,
        longitude: Double,
        radiusMeters: Double = 100.0,
        notificationsEnabled: Bool = true
    ) {
        // Validate radius
        let validRadius = max(
            Double(Region.companion.MIN_RADIUS_METERS),
            min(radiusMeters, 5000.0)
        )

        regionsController.createRegion(
            name: name,
            description: description,
            latitude: latitude,
            longitude: longitude,
            radiusMeters: Int32(validRadius),
            notificationsEnabled: notificationsEnabled
        )
    }

    /**
     Update an existing region.

     - Parameter region: The updated region
     */
    func updateRegion(_ region: Region) {
        regionsController.updateRegion(
            regionId: region.id,
            name: region.name,
            description: region.description_,
            latitude: region.latitude,
            longitude: region.longitude,
            radiusMeters: region.radiusMeters,
            notificationsEnabled: region.notificationsEnabled
        )
    }

    /**
     Delete a region.

     - Parameter regionId: ID of the region to delete
     */
    func deleteRegion(id: String) {
        regionsController.deleteRegion(regionId: id)
    }

    /**
     Toggle notifications for a region.

     - Parameter regionId: ID of the region
     */
    func toggleNotifications(forRegionId regionId: String) {
        guard let index = regions.firstIndex(where: { $0.id == regionId }) else {
            return
        }

        var updatedRegion = regions[index]
        updatedRegion = Region(
            id: updatedRegion.id,
            userId: updatedRegion.userId,
            name: updatedRegion.name,
            description: updatedRegion.description,
            latitude: updatedRegion.latitude,
            longitude: updatedRegion.longitude,
            radiusMeters: updatedRegion.radiusMeters,
            notificationsEnabled: !updatedRegion.notificationsEnabled,
            createdAt: updatedRegion.createdAt,
            updatedAt: Kotlinx_datetimeInstant.companion.fromEpochMilliseconds(
                epochMilliseconds: Int64(Date().timeIntervalSince1970 * 1000)
            ),
            enterCount: updatedRegion.enterCount,
            lastEnterTime: updatedRegion.lastEnterTime,
            lastExitTime: updatedRegion.lastExitTime
        )

        updateRegion(updatedRegion)
    }

    /**
     Clear any error state.
     */
    func clearError() {
        error = nil
    }
}

// MARK: - UI Helpers

extension RegionViewModel {
    /**
     Get regions sorted by distance from a given location.

     - Parameters:
       - latitude: Reference latitude
       - longitude: Reference longitude
     - Returns: Sorted array of regions
     */
    func regionsSortedByDistance(from latitude: Double, longitude: Double) -> [Region] {
        return regions.sorted { region1, region2 in
            let distance1 = distanceBetween(
                lat1: latitude, lon1: longitude,
                lat2: region1.latitude, lon2: region1.longitude
            )
            let distance2 = distanceBetween(
                lat1: latitude, lon1: longitude,
                lat2: region2.latitude, lon2: region2.longitude
            )
            return distance1 < distance2
        }
    }

    /**
     Calculate distance between two coordinates using Haversine formula.

     - Returns: Distance in meters
     */
    private func distanceBetween(lat1: Double, lon1: Double, lat2: Double, lon2: Double) -> Double {
        let earthRadius = 6371000.0 // meters

        let dLat = (lat2 - lat1) * .pi / 180.0
        let dLon = (lon2 - lon1) * .pi / 180.0

        let a = sin(dLat / 2) * sin(dLat / 2) +
                cos(lat1 * .pi / 180.0) * cos(lat2 * .pi / 180.0) *
                sin(dLon / 2) * sin(dLon / 2)

        let c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c
    }
}

// MARK: - Region UI Extensions

extension Region {
    /**
     Convert to SwiftUI Date.
     */
    var createdDate: Date {
        Date(timeIntervalSince1970: TimeInterval(createdAt.epochSeconds))
    }

    var updatedDate: Date {
        Date(timeIntervalSince1970: TimeInterval(updatedAt.epochSeconds))
    }

    var lastEnteredDate: Date? {
        guard let lastEntered = lastEnterTime else { return nil }
        return Date(timeIntervalSince1970: TimeInterval(lastEntered.epochSeconds))
    }

    var lastExitDate: Date? {
        guard let lastExit = lastExitTime else { return nil }
        return Date(timeIntervalSince1970: TimeInterval(lastExit.epochSeconds))
    }

    /**
     Format radius for display.
     */
    var radiusDisplayText: String {
        if radiusMeters >= 1000 {
            let km = radiusMeters / 1000.0
            return String(format: "%.1f km", km)
        } else {
            return String(format: "%.0f m", radiusMeters)
        }
    }

    /**
     Get SF Symbol icon for the region.
     */
    var iconName: String {
        return "mappin.circle.fill"
    }

    /**
     Get color for the region.
     */
    var displayColor: Color {
        return notificationsEnabled ? .blue : .gray
    }
}
