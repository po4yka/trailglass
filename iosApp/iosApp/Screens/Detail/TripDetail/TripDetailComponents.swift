import SwiftUI
import Shared

// MARK: - Trip Detail Content

/// Trip detail content layout
struct TripDetailContent: View {
    let stats: TripStatistics
    let route: RouteData?
    let onShare: () -> Void
    let onExport: (String) -> Void
    let onDelete: () -> Void

    var body: some View {
        LazyVStack(spacing: 16) {
            // Trip overview card
            TripOverviewCard(stats: stats)

            // Statistics cards
            TripStatisticsCards(stats: stats)

            // Map preview (if route available)
            if let route = route, !route.coordinates.isEmpty {
                RouteMapPreview(route: route)
            }

            // Transport breakdown
            if let transportDist = stats.transportDistribution, !transportDist.isEmpty {
                TransportBreakdownCard(distribution: transportDist)
            }

            // Places visited
            if !stats.placesVisited.isEmpty {
                PlacesVisitedCard(places: stats.placesVisited)
            }
        }
        .padding()
        .padding(.bottom, 80) // Add padding for floating tab bar
    }
}

// MARK: - Exported File

/// Exported file data for sharing
struct ExportedFile: Identifiable {
    let id = UUID()
    let fileName: String
    let content: String
    let mimeType: String
}

// MARK: - ViewModel

/// ViewModel for TripDetailView
class TripDetailViewModel: ObservableObject {
    private let tripId: String
    private let statsController: TripStatisticsController
    private let routeController: RouteViewController
    private let tripsController: TripsController

    private var statsObserver: Kotlinx_coroutines_coreJob?
    private var routeObserver: Kotlinx_coroutines_coreJob?

    @Published var tripStats: TripStatistics?
    @Published var routeData: RouteData?
    @Published var isLoading = true
    @Published var errorMessage: String?
    @Published var showDeleteAlert = false
    @Published var showRouteReplay = false
    @Published var exportedFile: ExportedFile?
    @Published var isExporting = false
    @Published var tripDeleted = false
    @Published var scrollOffset: CGFloat = 0
    @Published var showMenu = false

    init(tripId: String, statsController: TripStatisticsController, routeController: RouteViewController, tripsController: TripsController) {
        self.tripId = tripId
        self.statsController = statsController
        self.routeController = routeController
        self.tripsController = tripsController
    }

    func loadTrip() {
        isLoading = true
        errorMessage = nil

        // Load trip statistics
        statsController.loadTripStatistics(tripId: tripId)

        // Observe stats state
        statsObserver = statsController.state.subscribe { [weak self] state in
            guard let self = self, let state = state else { return }

            DispatchQueue.main.async {
                self.isLoading = state.isLoading
                self.errorMessage = state.error?.userMessage
                self.tripStats = state.statistics
            }
        }

        // Load route
        routeController.loadRoute(tripId: tripId)

        // Observe route state
        routeObserver = routeController.state.subscribe { [weak self] state in
            guard let self = self, let state = state else { return }

            DispatchQueue.main.async {
                self.routeData = state.routeData
                self.isExporting = state.isExporting

                // Handle export result
                if let exportResult = state.exportResult {
                    self.exportedFile = ExportedFile(
                        fileName: exportResult.fileName,
                        content: exportResult.content,
                        mimeType: exportResult.mimeType
                    )
                    // Clear the result from controller after handling
                    self.routeController.clearExportResult()
                }
            }
        }
    }

    func shareTrip() {
        // TODO: Implement share functionality
        print("Share trip: \(tripId)")
    }

    func exportTrip(format: String) {
        guard let tripName = tripStats?.tripName else {
            errorMessage = "Cannot export: trip name not available"
            return
        }

        // Convert format string to ExportFormat enum
        let exportFormat: ExportFormat
        switch format.lowercased() {
        case "gpx":
            exportFormat = ExportFormat.gpx
        case "kml":
            exportFormat = ExportFormat.kml
        default:
            errorMessage = "Unsupported export format: \(format)"
            return
        }

        // Call export on route controller
        routeController.exportRoute(tripName: tripName, format: exportFormat)
    }

    func deleteTrip() {
        tripsController.deleteTrip(tripId: tripId, onSuccess: { [weak self] in
            DispatchQueue.main.async {
                self?.tripDeleted = true
            }
        })
    }

    deinit {
        statsObserver?.cancel(cause: nil)
        routeObserver?.cancel(cause: nil)
    }
}

// MARK: - Share Sheet

/// SwiftUI wrapper for UIActivityViewController (share sheet)
struct ShareSheet: UIViewControllerRepresentable {
    let activityItems: [Any]
    let applicationActivities: [UIActivity]? = nil

    func makeUIViewController(context: Context) -> UIActivityViewController {
        let controller = UIActivityViewController(
            activityItems: activityItems,
            applicationActivities: applicationActivities
        )
        return controller
    }

    func updateUIViewController(_ uiViewController: UIActivityViewController, context: Context) {
        // No updates needed
    }
}
