import SwiftUI
import Shared

// MARK: - Trip Detail Content

/// Trip detail content layout
struct TripDetailContent: View {
    let tripRoute: TripRoute?
    let tripName: String?
    let onShare: () -> Void
    let onExport: (String) -> Void
    let onDelete: () -> Void

    var body: some View {
        LazyVStack(spacing: 16) {
            if let tripRoute = tripRoute {
                // Trip overview card
                TripOverviewCard(tripRoute: tripRoute, tripName: tripName)

                // Statistics cards
                TripStatisticsCards(statistics: tripRoute.statistics)

                // Map preview (if route available)
                if !tripRoute.fullPath.isEmpty {
                    RouteMapPreview(route: tripRoute)
                }

                // Transport breakdown
                if !tripRoute.statistics.distanceByTransport.isEmpty {
                    TransportBreakdownCard(distribution: tripRoute.statistics.distanceByTransport as! [TransportType : Double])
                }

                // Places visited
                if !tripRoute.visits.isEmpty {
                    PlacesVisitedCard(places: tripRoute.visits.map { $0.displayName })
                }
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

    private var statsObserver: KotlinJob?
    private var routeObserver: KotlinJob?

    @Published var tripRoute: TripRoute?
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
        statsController.loadStatistics(tripId: tripId)

        // Observe stats state
        statsObserver = statsController.state.subscribe { [weak self] (state: TripStatsState?) in
            guard let self = self, let state = state else { return }

            DispatchQueue.main.async {
                self.isLoading = state.isLoading
                self.errorMessage = state.error
                self.tripRoute = state.tripRoute
            }
        }

        // Load route
        routeController.loadRoute(tripId: tripId)

        // Observe route state
        routeObserver = routeController.state.subscribe { [weak self] (state: RouteViewState?) in
            guard let self = self, let state = state else { return }

            DispatchQueue.main.async {
                // Route data is already loaded from stats controller
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
        guard let tripRoute = tripRoute else {
            errorMessage = "Cannot export: route data not available"
            return
        }

        // Use trip ID as name if name not available
        let tripName = tripRoute.tripId

        // Convert format string to ExportFormat enum
        let exportFormat: Shared.ExportFormat
        switch format.lowercased() {
        case "gpx":
            exportFormat = Shared.ExportFormat.gpx
        case "kml":
            exportFormat = Shared.ExportFormat.kml
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
