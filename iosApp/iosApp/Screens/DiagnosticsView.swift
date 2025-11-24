import SwiftUI
import Shared

struct DiagnosticsView: View {
    @StateObject private var viewModel: DiagnosticsViewModel
    @State private var showingShareSheet = false

    init(controller: DiagnosticsController) {
        _viewModel = StateObject(wrappedValue: DiagnosticsViewModel(controller: controller))
    }

    var body: some View {
        Group {
            if viewModel.isLoading {
                VStack {
                    ProgressView()
                    Text("Loading diagnostics...")
                        .foregroundColor(.secondary)
                        .padding(.top)
                }
            } else {
                ScrollView {
                    LazyVStack(spacing: 16) {
                        LocationStatusCard(status: viewModel.state.locationStatus)
                        DatabaseStatusCard(status: viewModel.state.databaseStatus)
                        DiagnosticsSyncStatusCard(status: viewModel.state.syncStatus)
                        SystemStatusCard(status: viewModel.state.systemStatus)
                        PermissionsStatusCard(status: viewModel.state.permissionsStatus)
                    }
                    .padding()
                }
            }
        }
        .navigationTitle("Diagnostics")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                HStack {
                    Button(action: { showingShareSheet = true }) {
                        Image(systemName: "square.and.arrow.up")
                    }

                    Button(action: {}) {
                        Image(systemName: "arrow.clockwise")
                    }
                }
            }
        }
        // .sheet(isPresented: $showingShareSheet) {
        //     ShareSheet(activityItems: [viewModel.exportDiagnostics()])
        // }
    }
}

struct LocationStatusCard: View {
    let status: LocationStatus

    var body: some View {
        DiagnosticCard(title: "Location Status") {
            DiagnosticRow(label: "Tracking Mode", value: status.trackingMode.description)
            DiagnosticRow(label: "Last Update", value: status.lastLocationUpdate?.description ?? "Never")
            DiagnosticRow(
                label: "Accuracy",
                value: status.locationAccuracy.map { String(format: "%.2f m", $0) } ?? "N/A"
            )
            DiagnosticRow(label: "GPS Satellites", value: status.gpsSatellites.map { "\($0)" } ?? "N/A")
            DiagnosticRow(
                label: "Location Permission",
                value: status.locationPermissionGranted ? "✓ Granted" : "✗ Denied",
                isSuccess: status.locationPermissionGranted
            )
            DiagnosticRow(
                label: "Background Permission",
                value: status.backgroundLocationPermissionGranted ? "✓ Granted" : "✗ Denied",
                isSuccess: status.backgroundLocationPermissionGranted
            )
        }
    }
}

struct DatabaseStatusCard: View {
    let status: DatabaseStatus

    var body: some View {
        DiagnosticCard(title: "Database Status") {
            DiagnosticRow(label: "Location Samples", value: "\(status.locationSamplesCount)")
            DiagnosticRow(label: "Place Visits", value: "\(status.placeVisitsCount)")
            DiagnosticRow(label: "Route Segments", value: "\(status.routeSegmentsCount)")
            DiagnosticRow(label: "Trips", value: "\(status.tripsCount)")
            DiagnosticRow(label: "Photos", value: "\(status.photosCount)")
            DiagnosticRow(label: "Regions", value: "\(status.regionsCount)")
            DiagnosticRow(label: "Database Size", value: String(format: "%.2f MB", status.databaseSizeMB))
        }
    }
}

struct DiagnosticsSyncStatusCard: View {
    let status: SyncStatus

    var body: some View {
        DiagnosticCard(title: "Sync Status") {
            DiagnosticRow(label: "Last Sync", value: status.lastSyncTimestamp?.description ?? "Never")
            DiagnosticRow(label: "Sync Enabled", value: status.syncEnabled ? "Yes" : "No")
            DiagnosticRow(label: "Pending Items", value: "\(status.pendingSyncItems)")
            DiagnosticRow(label: "Errors", value: "\(status.syncErrorsCount)")
        }
    }
}

struct SystemStatusCard: View {
    let status: SystemStatus

    var body: some View {
        DiagnosticCard(title: "System Status") {
            DiagnosticRow(label: "App Version", value: status.appVersion)
            DiagnosticRow(label: "Build Number", value: status.buildNumber)
            DiagnosticRow(label: "OS Version", value: status.osVersion)
            DiagnosticRow(label: "Device Model", value: status.deviceModel)
            DiagnosticRow(label: "Network", value: status.networkConnectivity.description)
            DiagnosticRow(
                label: "Battery Level",
                value: status.batteryLevel.map { String(format: "%.0f%%", Double(truncating: $0 as NSNumber) * 100) } ?? "N/A"
            )
            DiagnosticRow(
                label: "Low Power Mode",
                value: status.lowPowerMode.map { $0.boolValue ? "Yes" : "No" } ?? "N/A"
            )
        }
    }
}

struct PermissionsStatusCard: View {
    let status: PermissionsStatus

    var body: some View {
        DiagnosticCard(title: "Permissions") {
            DiagnosticRow(
                label: "Location",
                value: status.locationPermissionGranted ? "✓ Granted" : "✗ Denied",
                isSuccess: status.locationPermissionGranted
            )
            DiagnosticRow(
                label: "Background Location",
                value: status.backgroundLocationPermissionGranted ? "✓ Granted" : "✗ Denied",
                isSuccess: status.backgroundLocationPermissionGranted
            )
            DiagnosticRow(
                label: "Notifications",
                value: status.notificationsPermissionGranted ? "✓ Granted" : "✗ Denied",
                isSuccess: status.notificationsPermissionGranted
            )
            DiagnosticRow(
                label: "Photo Library",
                value: status.photoLibraryPermissionGranted ? "✓ Granted" : "✗ Denied",
                isSuccess: status.photoLibraryPermissionGranted
            )
        }
    }
}

struct DiagnosticCard<Content: View>: View {
    let title: String
    let content: Content

    init(title: String, @ViewBuilder content: () -> Content) {
        self.title = title
        self.content = content()
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(title)
                .font(.headline)
                .foregroundColor(.blue)

            content
        }
        .padding()
        .background(Color(.systemBackground))
        .cornerRadius(10)
        .shadow(color: Color.black.opacity(0.1), radius: 2, x: 0, y: 1)
    }
}

struct DiagnosticRow: View {
    let label: String
    let value: String
    let isSuccess: Bool?

    init(label: String, value: String, isSuccess: Bool? = nil) {
        self.label = label
        self.value = value
        self.isSuccess = isSuccess
    }

    var body: some View {
        HStack {
            Text(label)
                .font(.system(.body, design: .monospaced))
                .foregroundColor(.secondary)

            Spacer()

            Text(value)
                .font(.system(.body, design: .monospaced))
                .foregroundColor(valueColor)
        }
    }

    private var valueColor: Color {
        guard let isSuccess = isSuccess else {
            return .primary
        }
        return isSuccess ? .green : .red
    }
}

class DiagnosticsViewModel: ObservableObject {
    @Published var state: DiagnosticsState
    @Published var isLoading: Bool = false

    private let controller: DiagnosticsController

    init(controller: DiagnosticsController) {
        self.controller = controller
        self.state = DiagnosticsState(
            locationStatus: LocationStatus(
                trackingMode: .idle,
                lastLocationUpdate: nil,
                locationAccuracy: nil,
                gpsSatellites: nil,
                locationPermissionGranted: false,
                backgroundLocationPermissionGranted: false
            ),
            databaseStatus: DatabaseStatus(
                locationSamplesCount: 0,
                placeVisitsCount: 0,
                routeSegmentsCount: 0,
                tripsCount: 0,
                photosCount: 0,
                regionsCount: 0,
                databaseSizeMB: 0.0
            ),
            syncStatus: SyncStatus(
                lastSyncTimestamp: nil,
                syncEnabled: false,
                pendingSyncItems: 0,
                syncErrorsCount: 0
            ),
            systemStatus: SystemStatus(
                appVersion: "",
                buildNumber: "",
                osVersion: "",
                deviceModel: "",
                networkConnectivity: .unknown,
                batteryLevel: nil,
                batteryOptimizationDisabled: nil,
                lowPowerMode: nil
            ),
            permissionsStatus: PermissionsStatus(
                locationPermissionGranted: false,
                backgroundLocationPermissionGranted: false,
                notificationsPermissionGranted: false,
                photoLibraryPermissionGranted: false
            ),
            isLoading: false,
            error: nil
        )

        // observeState() // Method not defined

        // let job = controller.state.subscribe { [weak self] state in
        //     guard let self = self else { return }
        //     DispatchQueue.main.async {
        //         self.state = state as! DiagnosticsState
        //         self.isLoading = state.isLoading
        //     }
        // } // Commented out - generic parameter inference issues

    func refresh() {
        controller.refreshAll()
    }
    }
}

// Preview disabled - requires proper DI setup
/*
#Preview {
    NavigationView {
        DiagnosticsView(controller: DiagnosticsController(
            locationTracker: PreviewMocks.locationTracker,
            locationRepository: PreviewMocks.locationRepository,
            placeVisitRepository: PreviewMocks.placeVisitRepository,
            routeSegmentRepository: PreviewMocks.routeSegmentRepository,
            tripRepository: PreviewMocks.tripRepository,
            photoRepository: PreviewMocks.photoRepository,
            syncCoordinator: PreviewMocks.syncCoordinator,
            networkConnectivityMonitor: PreviewMocks.networkConnectivityMonitor,
            platformDiagnostics: PlatformDiagnostics(),
            coroutineScope: PreviewMocks.coroutineScope
        ))
    }
}

struct PreviewMocks {
    static let locationTracker = LocationTracker()
    static let locationRepository = LocationRepository()
    static let placeVisitRepository = PlaceVisitRepository()
    static let routeSegmentRepository = RouteSegmentRepository()
    static let tripRepository = TripRepository()
    static let photoRepository = PhotoRepository()
    static let syncCoordinator = SyncCoordinator()
    static let networkConnectivityMonitor = NetworkConnectivityMonitor()
    static let coroutineScope = CoroutineScope()
}
*/
