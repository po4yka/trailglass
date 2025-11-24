import SwiftUI
import Shared

// MARK: - Export/Import Data Structures

struct TrailglassExportData: Codable {
    let version: String
    let exportDate: Date
    let trips: [TripExportData]
    let places: [PlaceExportData]
    let regions: [RegionExportData]
    let settings: [String: String] // Simplified settings as key-value pairs
}

struct TripExportData: Codable {
    let id: String
    let name: String
    let description: String?
    let startDate: Date
    let endDate: Date?
    // Add more fields as needed
}

struct PlaceExportData: Codable {
    let id: String
    let name: String
    let latitude: Double
    let longitude: Double
    let category: String
    // Add more fields as needed
}

struct RegionExportData: Codable {
    let id: String
    let name: String
    let latitude: Double
    let longitude: Double
    let radiusMeters: Double
    // Add more fields as needed
}

/**
 * SwiftUI settings screen with Liquid Glass components.
 * Comprehensive settings with all preference categories and glass styling.
 */
struct EnhancedSettingsView: View {
    let appComponent: AppComponent
    @StateObject private var viewModel: SettingsViewModel
    @State private var showClearDataAlert = false
    @State private var showShareSheet = false
    @State private var shareItems: [Any] = []
    @State private var showDocumentPicker = false
    @State private var showErrorAlert = false
    @State private var errorMessage = ""

    init(controller: SettingsController, appComponent: AppComponent) {
        self.appComponent = appComponent
        _viewModel = StateObject(wrappedValue: SettingsViewModel(controller: controller))
    }

    private func exportData() {
        Task {
            do {
                // Get data from repositories
                let trips = try await appComponent.tripRepository.getTripsForUser(userId: appComponent.userId)
                let placeVisits = try await appComponent.placeVisitRepository.getPlaceVisitsForUser(userId: appComponent.userId)
                let regions = try await appComponent.regionRepository.getRegionsForUser(userId: appComponent.userId)

                // Convert to export format
                let tripExports = trips.map { trip in
                    TripExportData(
                        id: trip.id,
                        name: trip.name,
                        description: trip.description,
                        startDate: Date(timeIntervalSince1970: TimeInterval(trip.startTime.epochSeconds)),
                        endDate: trip.endTime?.epochSeconds.map { Date(timeIntervalSince1970: TimeInterval($0)) }
                    )
                }

                let placeExports = placeVisits.map { visit in
                    PlaceExportData(
                        id: visit.id,
                        name: visit.name ?? "Unknown Place",
                        latitude: visit.latitude,
                        longitude: visit.longitude,
                        category: visit.category?.name ?? "Unknown"
                    )
                }

                let regionExports = regions.map { region in
                    RegionExportData(
                        id: region.id,
                        name: region.name,
                        latitude: region.center.latitude,
                        longitude: region.center.longitude,
                        radiusMeters: region.radiusMeters
                    )
                }

                // Get settings from viewModel
                var settingsDict: [String: String] = [:]
                if let settings = viewModel.settings {
                    // Convert settings to dictionary (simplified)
                    settingsDict["trackingEnabled"] = String(settings.trackingEnabled)
                    settingsDict["syncEnabled"] = String(settings.syncEnabled)
                }

                // Create export data structure
                let exportData = TrailglassExportData(
                    version: "1.0",
                    exportDate: Date(),
                    trips: tripExports,
                    places: placeExports,
                    regions: regionExports,
                    settings: settingsDict
                )

                // Convert to JSON
                let encoder = JSONEncoder()
                encoder.dateEncodingStrategy = .iso8601
                let jsonData = try encoder.encode(exportData)

                // Create temporary file
                let tempURL = FileManager.default.temporaryDirectory.appendingPathComponent("trailglass_export_\(Date().timeIntervalSince1970).json")
                try jsonData.write(to: tempURL)

                // Present share sheet
                shareItems = [tempURL]
                showShareSheet = true

            } catch {
                errorMessage = "Failed to export data: \(error.localizedDescription)"
                showErrorAlert = true
            }
        }
    }

    private func importData() {
        showDocumentPicker = true
    }

    private func processImportedData(_ jsonData: Data) {
        Task {
            do {
                // Parse JSON
                let decoder = JSONDecoder()
                decoder.dateDecodingStrategy = .iso8601
                let exportData = try decoder.decode(TrailglassExportData.self, from: jsonData)

                // Validate version compatibility
                guard exportData.version == "1.0" else {
                    throw ImportError.incompatibleVersion
                }

                // Import data (simplified - in production, you'd want more robust error handling)
                // Note: This is a basic implementation. Full import would require use cases or repository methods
                print("Processing imported data with version: \(exportData.version)")
                print("Trips: \(exportData.trips.count), Places: \(exportData.places.count), Regions: \(exportData.regions.count)")

                // TODO: Implement full import logic using repositories or use cases
                // For now, just log the import
                errorMessage = "Import functionality requires backend implementation"
                showErrorAlert = true

            } catch {
                errorMessage = "Failed to import data: \(error.localizedDescription)"
                showErrorAlert = true
            }
        }
    }

    enum ImportError: Error {
        case invalidFormat
        case incompatibleVersion
        case importFailed
    }

    var body: some View {
        ZStack {
            Color.backgroundLight.ignoresSafeArea()

            if viewModel.isLoading {
                GlassLoadingIndicator(variant: .pulsing, color: .coolSteel)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if let settings = viewModel.settings {
                SettingsContent(
                    settings: settings,
                    viewModel: viewModel,
                    appComponent: appComponent,
                    onClearData: { showClearDataAlert = true },
                    onExportData: { exportData() },
                    onImportData: { importData() }
                )
            } else {
                EmptySettingsView()
            }
        }
        .navigationTitle("Settings")
        .navigationBarTitleDisplayMode(.large)
        .alert("Error", isPresented: $viewModel.showError) {
            Button("OK") {
                viewModel.clearError()
            }
        } message: {
            if let error = viewModel.error {
                Text(error)
            }
        }
        .alert("Clear All Data", isPresented: $showClearDataAlert) {
            Button("Cancel", role: .cancel) {
                showClearDataAlert = false
            }
            Button("Clear All Data", role: .destructive) {
                viewModel.clearAllData()
                showClearDataAlert = false
            }
        } message: {
            Text("""
            This will permanently delete all your data including trips, locations, photos, and \
            settings. This action cannot be undone.

            Are you sure you want to continue?
            """)
        }
        .alert("Error", isPresented: $showErrorAlert) {
            Button("OK") {
                showErrorAlert = false
            }
        } message: {
            Text(errorMessage)
        }
        .sheet(isPresented: $showShareSheet) {
            ShareSheet(items: shareItems)
        }
        .sheet(isPresented: $showDocumentPicker) {
            DocumentPicker(contentTypes: [.json]) { url in
                // Access the file and read its contents
                guard url.startAccessingSecurityScopedResource() else {
                    errorMessage = "Failed to access selected file"
                    showErrorAlert = true
                    return
                }
                defer { url.stopAccessingSecurityScopedResource() }

                do {
                    let jsonData = try Data(contentsOf: url)
                    processImportedData(jsonData)
                } catch {
                    errorMessage = "Failed to read file: \(error.localizedDescription)"
                    showErrorAlert = true
                }
            }
        }
    }
}
