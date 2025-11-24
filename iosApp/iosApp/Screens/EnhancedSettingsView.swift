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

    init(controller: SettingsController, appComponent: AppComponent) {
        self.appComponent = appComponent
        _viewModel = StateObject(wrappedValue: SettingsViewModel(controller: controller))
    }

    private func exportData() {
        // Create export data structure
        let exportData = TrailglassExportData(
            version: "1.0",
            exportDate: Date(),
            trips: [], // TODO: Get trips from controller
            places: [], // TODO: Get places from controller
            regions: [], // TODO: Get regions from controller
            settings: [:] // TODO: Get settings from viewModel
        )

        do {
            // Convert to JSON
            let encoder = JSONEncoder()
            encoder.dateEncodingStrategy = .iso8601
            let jsonData = try encoder.encode(exportData)
            let jsonString = String(data: jsonData, encoding: .utf8) ?? "{}"

            // Create temporary file
            let tempURL = FileManager.default.temporaryDirectory.appendingPathComponent("trailglass_export.json")
            try jsonString.write(to: tempURL, atomically: true, encoding: .utf8)

            // Present share sheet
            presentShareSheet(for: tempURL)

        } catch {
            print("Failed to export data: \(error)")
            // TODO: Show error alert
        }
    }

    private func presentShareSheet(for fileURL: URL) {
        // TODO: Present UIActivityViewController
        print("Presenting share sheet for file: \(fileURL)")
    }

    private func importData() {
        // For now, show a placeholder alert
        // TODO: Implement proper document picker with UIViewControllerRepresentable
        print("Import data functionality - placeholder implementation")
        // In a real implementation:
        // let picker = UIDocumentPickerViewController(forOpeningContentTypes: [.json])
        // picker.delegate = self
        // present(picker, animated: true)
    }

    private func processImportedData(_ jsonData: Data) {
        do {
            // Parse JSON
            guard let jsonObject = try JSONSerialization.jsonObject(with: jsonData) as? [String: Any],
                  let version = jsonObject["version"] as? String else {
                throw ImportError.invalidFormat
            }

            // TODO: Validate version compatibility
            // TODO: Import trips, places, regions, settings
            print("Processing imported data with version: \(version)")

        } catch {
            print("Failed to import data: \(error)")
            // TODO: Show error alert
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
    }
}
