import SwiftUI
import shared
import MapKit

/// Trip detail screen showing comprehensive trip information and statistics
struct TripDetailView: View {
    let tripId: String
    let appComponent: AppComponent
    @StateObject private var viewModel: TripDetailViewModel
    @Environment(\.dismiss) private var dismiss

    init(tripId: String, appComponent: AppComponent) {
        self.tripId = tripId
        self.appComponent = appComponent
        _viewModel = StateObject(wrappedValue: TripDetailViewModel(
            tripId: tripId,
            statsController: appComponent.tripStatisticsController,
            routeController: appComponent.routeViewController,
            tripsController: appComponent.tripsController
        ))
    }

    var body: some View {
        NavigationView {
            ScrollView {
                if viewModel.isLoading {
                    ProgressView()
                        .frame(maxWidth: .infinity, minHeight: 200)
                } else if let error = viewModel.errorMessage {
                    VStack(spacing: 16) {
                        Image(systemName: "exclamationmark.triangle")
                            .font(.system(size: 48))
                            .foregroundColor(.red)
                        Text(error)
                            .foregroundColor(.secondary)
                        Button("Retry", action: { viewModel.loadTrip() })
                            .buttonStyle(.borderedProminent)
                    }
                    .padding()
                } else if let stats = viewModel.tripStats {
                    TripDetailContent(
                        stats: stats,
                        route: viewModel.routeData,
                        onShare: { viewModel.shareTrip() },
                        onExport: { format in viewModel.exportTrip(format: format) },
                        onDelete: { viewModel.deleteTrip() }
                    )
                }
            }
            .navigationTitle(viewModel.tripStats?.tripName ?? "Trip Details")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button(action: { dismiss() }) {
                        Image(systemName: "chevron.left")
                    }
                }

                ToolbarItem(placement: .navigationBarTrailing) {
                    Menu {
                        Button(action: { viewModel.shareTrip() }) {
                            Label("Share", systemImage: "square.and.arrow.up")
                        }

                        Menu("Export") {
                            Button(action: { viewModel.exportTrip(format: "gpx") }) {
                                Label("Export as GPX", systemImage: "map")
                            }
                            Button(action: { viewModel.exportTrip(format: "kml") }) {
                                Label("Export as KML", systemImage: "globe")
                            }
                        }

                        Divider()

                        Button(role: .destructive, action: { viewModel.showDeleteAlert = true }) {
                            Label("Delete Trip", systemImage: "trash")
                        }
                    } label: {
                        Image(systemName: "ellipsis.circle")
                    }
                }
            }
            .alert("Delete Trip?", isPresented: $viewModel.showDeleteAlert) {
                Button("Cancel", role: .cancel) { }
                Button("Delete", role: .destructive, action: { viewModel.deleteTrip() })
            } message: {
                Text("This action cannot be undone. All trip data will be permanently deleted.")
            }
            .sheet(item: $viewModel.exportedFile) { exportedFile in
                ShareSheet(activityItems: [createExportFile(from: exportedFile)])
            }
        }
        .onAppear {
            viewModel.loadTrip()
        }
        .onChange(of: viewModel.tripDeleted) { deleted in
            if deleted {
                dismiss()
            }
        }
    }

    private func createExportFile(from exportedFile: ExportedFile) -> URL {
        let tempDir = FileManager.default.temporaryDirectory
        let fileURL = tempDir.appendingPathComponent(exportedFile.fileName)

        do {
            try exportedFile.content.write(to: fileURL, atomically: true, encoding: .utf8)
        } catch {
            print("Failed to write export file: \(error)")
        }

        return fileURL
    }
}

/// Trip detail content layout
private struct TripDetailContent: View {
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
    }
}

/// Trip overview card showing basic info
private struct TripOverviewCard: View {
    let stats: TripStatistics

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                VStack(alignment: .leading, spacing: 4) {
                    Text(stats.tripName)
                        .font(.title2)
                        .fontWeight(.bold)

                    Text(formatDateRange(stats.startTime, stats.endTime))
                        .font(.subheadline)
                        .foregroundColor(.secondary)

                    if let duration = stats.duration {
                        Text(formatDuration(duration))
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                    }
                }

                Spacer()

                // Status badge
                if let isActive = stats.isActive, isActive {
                    Text("Active")
                        .font(.caption)
                        .fontWeight(.semibold)
                        .padding(.horizontal, 12)
                        .padding(.vertical, 6)
                        .background(Color.green)
                        .foregroundColor(.white)
                        .cornerRadius(8)
                }
            }
        }
        .padding()
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color(.systemGray6))
        .cornerRadius(12)
    }

    private func formatDateRange(_ start: Kotlinx_datetimeInstant, _ end: Kotlinx_datetimeInstant?) -> String {
        let formatter = DateFormatter()
        formatter.dateStyle = .medium
        formatter.timeStyle = .short

        let startDate = Date(timeIntervalSince1970: TimeInterval(start.epochSeconds))
        let startStr = formatter.string(from: startDate)

        if let end = end {
            let endDate = Date(timeIntervalSince1970: TimeInterval(end.epochSeconds))
            let endStr = formatter.string(from: endDate)
            return "\(startStr) - \(endStr)"
        } else {
            return "Started \(startStr)"
        }
    }

    private func formatDuration(_ duration: KotlinDuration) -> String {
        let hours = duration.inWholeHours
        let minutes = duration.inWholeMinutes % 60

        if hours > 24 {
            let days = hours / 24
            let remainingHours = hours % 24
            return "\(days)d \(remainingHours)h"
        } else if hours > 0 {
            return "\(hours)h \(minutes)m"
        } else {
            return "\(minutes)m"
        }
    }
}

/// Trip statistics cards grid
private struct TripStatisticsCards: View {
    let stats: TripStatistics

    var body: some View {
        VStack(spacing: 12) {
            HStack(spacing: 12) {
                StatCard(
                    icon: "figure.walk",
                    label: "Distance",
                    value: formatDistance(stats.totalDistance),
                    color: .blue
                )

                StatCard(
                    icon: "mappin.and.ellipse",
                    label: "Places",
                    value: "\(stats.placesVisited.count)",
                    color: .green
                )
            }

            HStack(spacing: 12) {
                StatCard(
                    icon: "arrow.up.right",
                    label: "Max Speed",
                    value: formatSpeed(stats.maxSpeed),
                    color: .orange
                )

                StatCard(
                    icon: "speedometer",
                    label: "Avg Speed",
                    value: formatSpeed(stats.averageSpeed),
                    color: .purple
                )
            }
        }
    }

    private func formatDistance(_ meters: Double) -> String {
        if meters < 1000 {
            return String(format: "%.0f m", meters)
        } else {
            return String(format: "%.2f km", meters / 1000)
        }
    }

    private func formatSpeed(_ mps: Double?) -> String {
        guard let mps = mps, mps > 0 else { return "-" }
        let kmh = mps * 3.6
        return String(format: "%.1f km/h", kmh)
    }
}

/// Individual stat card
private struct StatCard: View {
    let icon: String
    let label: String
    let value: String
    let color: Color

    var body: some View {
        VStack(spacing: 8) {
            Image(systemName: icon)
                .font(.title2)
                .foregroundColor(color)

            Text(value)
                .font(.title3)
                .fontWeight(.bold)

            Text(label)
                .font(.caption)
                .foregroundColor(.secondary)
        }
        .frame(maxWidth: .infinity)
        .padding()
        .background(Color(.systemGray6))
        .cornerRadius(12)
    }
}

/// Route map preview
private struct RouteMapPreview: View {
    let route: RouteData

    @State private var region = MKCoordinateRegion(
        center: CLLocationCoordinate2D(latitude: 0, longitude: 0),
        span: MKCoordinateSpan(latitudeDelta: 0.05, longitudeDelta: 0.05)
    )

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Route Map")
                .font(.headline)
                .padding(.horizontal)

            Map(coordinateRegion: .constant(region), annotationItems: []) { _ in
                MapMarker(coordinate: CLLocationCoordinate2D(latitude: 0, longitude: 0))
            }
            .frame(height: 200)
            .cornerRadius(12)
            .onAppear {
                if let first = route.coordinates.first {
                    region.center = CLLocationCoordinate2D(
                        latitude: first.latitude,
                        longitude: first.longitude
                    )
                }
            }
        }
    }
}

/// Transport breakdown card
private struct TransportBreakdownCard: View {
    let distribution: [String: Double]

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Transport Types")
                .font(.headline)

            ForEach(Array(distribution.keys.sorted()), id: \.self) { key in
                if let distance = distribution[key] {
                    HStack {
                        Image(systemName: transportIcon(key))
                            .foregroundColor(.blue)
                            .frame(width: 24)

                        Text(key.capitalized)
                            .font(.body)

                        Spacer()

                        Text(String(format: "%.2f km", distance / 1000))
                            .font(.body)
                            .fontWeight(.medium)
                    }
                }
            }
        }
        .padding()
        .background(Color(.systemGray6))
        .cornerRadius(12)
    }

    private func transportIcon(_ type: String) -> String {
        switch type.lowercased() {
        case "walk": return "figure.walk"
        case "bike": return "bicycle"
        case "car": return "car.fill"
        case "train": return "train.side.front.car"
        case "plane": return "airplane"
        case "boat": return "ferry.fill"
        default: return "location.fill"
        }
    }
}

/// Places visited card
private struct PlacesVisitedCard: View {
    let places: [String]

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Places Visited")
                .font(.headline)

            ForEach(places.prefix(10), id: \.self) { place in
                HStack {
                    Image(systemName: "mappin.circle.fill")
                        .foregroundColor(.blue)
                        .font(.caption)

                    Text(place)
                        .font(.body)

                    Spacer()
                }
                .padding(.vertical, 4)
            }

            if places.count > 10 {
                Text("And \(places.count - 10) more places...")
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
        }
        .padding()
        .background(Color(.systemGray6))
        .cornerRadius(12)
    }
}

/// Exported file data for sharing
struct ExportedFile: Identifiable {
    let id = UUID()
    let fileName: String
    let content: String
    let mimeType: String
}

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
    @Published var exportedFile: ExportedFile?
    @Published var isExporting = false
    @Published var tripDeleted = false

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

#Preview {
    Text("TripDetailView Preview - Requires DI setup")
}
