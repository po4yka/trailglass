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
        VStack(spacing: 0) {
            // Large flexible navigation bar with trip metadata
            if let stats = viewModel.tripStats {
                LargeFlexibleNavigationBar(
                    title: stats.tripName,
                    scrollOffset: viewModel.scrollOffset,
                    actions: [
                        NavigationAction(icon: "ellipsis.circle") {
                            viewModel.showMenu = true
                        }
                    ],
                    subtitle: {
                        Text(formatTripMetadata(stats))
                    },
                    backgroundContent: {
                        HeroGradientBackground(
                            startColor: Color.lightCyan,
                            endColor: Color.coastalPath
                        )
                    }
                )
            }

            // Content
            ScrollView {
                GeometryReader { geometry in
                    Color.clear.preference(
                        key: ScrollOffsetPreferenceKey.self,
                        value: geometry.frame(in: .named("scroll")).minY
                    )
                }
                .frame(height: 0)

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
            .coordinateSpace(name: "scroll")
            .onPreferenceChange(ScrollOffsetPreferenceKey.self) { value in
                viewModel.scrollOffset = value
            }
        }
        .alert("Delete Trip?", isPresented: $viewModel.showDeleteAlert) {
            Button("Cancel", role: .cancel) { }
            Button("Delete", role: .destructive, action: { viewModel.deleteTrip() })
        } message: {
            Text("This action cannot be undone. All trip data will be permanently deleted.")
        }
        .confirmationDialog("Trip Actions", isPresented: $viewModel.showMenu) {
            Button("Route Replay") {
                viewModel.showRouteReplay = true
            }
            Button("Share") {
                viewModel.shareTrip()
            }
            Button("Export as GPX") {
                viewModel.exportTrip(format: "gpx")
            }
            Button("Export as KML") {
                viewModel.exportTrip(format: "kml")
            }
            Button("Delete Trip", role: .destructive) {
                viewModel.showDeleteAlert = true
            }
            Button("Cancel", role: .cancel) { }
        }
        .sheet(item: $viewModel.exportedFile) { exportedFile in
            ShareSheet(activityItems: [createExportFile(from: exportedFile)])
        }
        .fullScreenCover(isPresented: $viewModel.showRouteReplay) {
            RouteReplayView(
                tripId: tripId,
                controller: appComponent.routeReplayController
            )
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

    private func formatTripMetadata(_ stats: TripStatistics) -> String {
        let distance = formatDistance(stats.totalDistance)
        let duration = stats.duration.map { formatDuration($0) } ?? ""
        return "\(distance) • \(duration)"
    }

    private func formatDistance(_ meters: Double) -> String {
        if meters < 1000 {
            return String(format: "%.0f m", meters)
        } else {
            return String(format: "%.1f km", meters / 1000)
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

#Preview {
    Text("TripDetailView Preview - Requires DI setup")
}
