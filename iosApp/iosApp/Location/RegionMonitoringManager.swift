import Foundation
import CoreLocation
import Shared
import Combine

/**
 Manages region monitoring (geofencing) using CLLocationManager.

 Handles:
 - Converting Region domain models to CLCircularRegion
 - Starting/stopping region monitoring
 - Handling iOS 20 region limit with prioritization
 - Region enter/exit callbacks
 */
class RegionMonitoringManager: NSObject, ObservableObject {

    // MARK: - Properties

    private let locationManager: CLLocationManager
    private var regions: [Region] = []
    private var currentLocation: CLLocation?

    // Publishers for region events
    let regionEnteredSubject = PassthroughSubject<Region, Never>()
    let regionExitedSubject = PassthroughSubject<Region, Never>()
    let errorSubject = PassthroughSubject<String, Never>()

    @Published var isMonitoring: Bool = false
    @Published var monitoredRegionIds: Set<String> = []

    // iOS has a hard limit of 20 monitored regions
    private let maxMonitoredRegions = 20

    // MARK: - Initialization

    override init() {
        self.locationManager = CLLocationManager()
        super.init()

        locationManager.delegate = self
        locationManager.desiredAccuracy = kCLLocationAccuracyBest
        locationManager.allowsBackgroundLocationUpdates = true
        locationManager.pausesLocationUpdatesAutomatically = false

        print("RegionMonitoringManager initialized")
    }

    // MARK: - Public Methods

    /**
     Start monitoring regions with automatic prioritization if count exceeds iOS limit.

     - Parameter regions: Array of regions to monitor
     */
    func startMonitoring(regions: [Region]) {
        print("Starting region monitoring for \(regions.count) regions")

        self.regions = regions

        // Check authorization status
        guard CLLocationManager.locationServicesEnabled() else {
            errorSubject.send("Location services are disabled")
            return
        }

        let authStatus = locationManager.authorizationStatus
        guard authStatus == .authorizedAlways || authStatus == .authorizedWhenInUse else {
            errorSubject.send("Location permission not granted")
            return
        }

        // Stop all current monitoring
        stopAllMonitoring()

        // Prioritize regions based on proximity to current location
        let prioritizedRegions = prioritizeRegions(regions)

        // Monitor up to the iOS limit
        let regionsToMonitor = Array(prioritizedRegions.prefix(maxMonitoredRegions))

        if regionsToMonitor.count < regions.count {
            print("Warning: Only monitoring \(regionsToMonitor.count) out of \(regions.count) regions due to iOS limit")
        }

        for region in regionsToMonitor {
            startMonitoringRegion(region)
        }

        isMonitoring = !regionsToMonitor.isEmpty

        // Start location updates to enable region prioritization updates
        if !regions.isEmpty {
            locationManager.startUpdatingLocation()
        }
    }

    /**
     Stop monitoring a specific region.

     - Parameter regionId: ID of the region to stop monitoring
     */
    func stopMonitoring(regionId: String) {
        print("Stopping monitoring for region: \(regionId)")

        guard let region = locationManager.monitoredRegions.first(where: {
            ($0 as? CLCircularRegion)?.identifier == regionId
        }) else {
            print("Region \(regionId) is not currently being monitored")
            return
        }

        locationManager.stopMonitoring(for: region)
        monitoredRegionIds.remove(regionId)

        if locationManager.monitoredRegions.isEmpty {
            isMonitoring = false
            locationManager.stopUpdatingLocation()
        }
    }

    /**
     Stop monitoring all regions.
     */
    func stopAllMonitoring() {
        print("Stopping all region monitoring")

        for region in locationManager.monitoredRegions {
            locationManager.stopMonitoring(for: region)
        }

        monitoredRegionIds.removeAll()
        isMonitoring = false
        locationManager.stopUpdatingLocation()
    }

    /**
     Request location permission for region monitoring.
     Region monitoring requires "Always" authorization for background monitoring.
     */
    func requestLocationPermission() {
        let authStatus = locationManager.authorizationStatus

        switch authStatus {
        case .notDetermined:
            // Request "Always" authorization for background region monitoring
            locationManager.requestAlwaysAuthorization()
        case .authorizedWhenInUse:
            // Prompt to upgrade to "Always"
            locationManager.requestAlwaysAuthorization()
        case .authorizedAlways:
            print("Already have always authorization")
        case .denied, .restricted:
            errorSubject.send("Location permission denied. Please enable in Settings.")
        @unknown default:
            break
        }
    }

    // MARK: - Private Methods

    private func startMonitoringRegion(_ region: Region) {
        guard region.radiusMeters >= locationManager.maximumRegionMonitoringDistance ||
              locationManager.maximumRegionMonitoringDistance == CLLocationDistanceMax else {
            print("Region \(region.name) radius too large: \(region.radiusMeters)m")
            errorSubject.send("Region \(region.name) radius exceeds maximum allowed")
            return
        }

        let center = CLLocationCoordinate2D(
            latitude: region.latitude,
            longitude: region.longitude
        )

        let clRegion = CLCircularRegion(
            center: center,
            radius: region.radiusMeters,
            identifier: region.id
        )

        clRegion.notifyOnEntry = region.notificationsEnabled
        clRegion.notifyOnExit = region.notificationsEnabled

        locationManager.startMonitoring(for: clRegion)
        monitoredRegionIds.insert(region.id)

        // Request initial state to handle case where user is already inside
        locationManager.requestState(for: clRegion)

        print("Started monitoring region: \(region.name) (\(region.id))")
    }

    /**
     Prioritize regions based on distance from current location.
     Nearest regions get priority when hitting iOS limit.
     */
    private func prioritizeRegions(_ regions: [Region]) -> [Region] {
        guard let currentLocation = currentLocation else {
            // No location yet, return regions as-is
            return regions
        }

        return regions.sorted { region1, region2 in
            let location1 = CLLocation(
                latitude: region1.latitude,
                longitude: region1.longitude
            )
            let location2 = CLLocation(
                latitude: region2.latitude,
                longitude: region2.longitude
            )

            let distance1 = currentLocation.distance(from: location1)
            let distance2 = currentLocation.distance(from: location2)

            return distance1 < distance2
        }
    }

    /**
     Update region prioritization when location changes significantly.
     */
    private func updateRegionPrioritization() {
        guard regions.count > maxMonitoredRegions else {
            // All regions can be monitored, no need to reprioritize
            return
        }

        let prioritizedRegions = prioritizeRegions(regions)
        let topRegions = Array(prioritizedRegions.prefix(maxMonitoredRegions))
        let topRegionIds = Set(topRegions.map { $0.id })

        // Check if we need to update monitoring
        if topRegionIds != monitoredRegionIds {
            print("Updating region monitoring based on new location")
            startMonitoring(regions: regions)
        }
    }

    private func findRegion(byId id: String) -> Region? {
        return regions.first { $0.id == id }
    }
}

// MARK: - CLLocationManagerDelegate

extension RegionMonitoringManager: CLLocationManagerDelegate {

    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        guard let location = locations.last else { return }

        let oldLocation = currentLocation
        currentLocation = location

        // Only reprioritize if we've moved significantly (>5km for major reprioritization, >500m for minor)
        if let old = oldLocation {
            let distance = location.distance(from: old)

            // Major reprioritization for movements >5km
            if distance > 5000 {
                print("Major location change detected: \(Int(distance))m - reprioritizing all regions")
                updateRegionPrioritization()
            } else if distance > 500 {
                // Minor reprioritization for movements >500m
                print("Minor location change detected: \(Int(distance))m - checking prioritization")
                updateRegionPrioritization()
            }
        } else {
            // First location update, prioritize regions
            print("Initial location received - prioritizing regions")
            updateRegionPrioritization()
        }
    }

    func locationManager(_ manager: CLLocationManager, didEnterRegion region: CLRegion) {
        print("Did enter region: \(region.identifier)")

        guard let regionId = (region as? CLCircularRegion)?.identifier,
              let trackedRegion = findRegion(byId: regionId) else {
            print("Unknown region entered: \(region.identifier)")
            return
        }

        regionEnteredSubject.send(trackedRegion)
    }

    func locationManager(_ manager: CLLocationManager, didExitRegion region: CLRegion) {
        print("Did exit region: \(region.identifier)")

        guard let regionId = (region as? CLCircularRegion)?.identifier,
              let trackedRegion = findRegion(byId: regionId) else {
            print("Unknown region exited: \(region.identifier)")
            return
        }

        regionExitedSubject.send(trackedRegion)
    }

    func locationManager(_ manager: CLLocationManager, monitoringDidFailFor region: CLRegion?, withError error: Error) {
        let regionInfo = region?.identifier ?? "unknown"
        print("Region monitoring failed for \(regionInfo): \(error.localizedDescription)")

        errorSubject.send("Monitoring failed: \(error.localizedDescription)")

        if let region = region {
            monitoredRegionIds.remove(region.identifier)
        }
    }

    func locationManager(_ manager: CLLocationManager, didDetermineState state: CLRegionState, for region: CLRegion) {
        print("Region \(region.identifier) state determined: \(state.rawValue)")

        // Handle initial state when monitoring starts
        guard let regionId = (region as? CLCircularRegion)?.identifier,
              let trackedRegion = findRegion(byId: regionId) else {
            return
        }

        switch state {
        case .inside:
            print("Already inside region: \(region.identifier)")
            regionEnteredSubject.send(trackedRegion)
        case .outside:
            print("Outside region: \(region.identifier)")
        case .unknown:
            print("Region state unknown: \(region.identifier)")
        }
    }

    func locationManager(_ manager: CLLocationManager, didChangeAuthorization status: CLAuthorizationStatus) {
        print("Location authorization changed: \(status.rawValue)")

        switch status {
        case .authorizedAlways, .authorizedWhenInUse:
            print("Location permission granted")
        case .denied, .restricted:
            errorSubject.send("Location permission denied")
            stopAllMonitoring()
        case .notDetermined:
            break
        @unknown default:
            break
        }
    }

    func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        // iOS 14+ callback
        locationManager(manager, didChangeAuthorization: manager.authorizationStatus)
    }
}
