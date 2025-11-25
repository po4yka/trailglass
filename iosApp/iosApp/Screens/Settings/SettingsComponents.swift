import SwiftUI
import Shared

/**
 * Settings section card with glass styling.
 */
struct SettingsSectionCard<Content: View>: View {
    let title: String
    let icon: String
    let tint: Color
    @ViewBuilder let content: () -> Content

    var body: some View {
        GlassEffectGroup(spacing: 12, padding: 16) {
            VStack(alignment: .leading, spacing: 12) {
                // Section header
                HStack(spacing: 8) {
                    Image(systemName: icon)
                        .foregroundColor(tint)
                        .font(.headline)
                    Text(title)
                        .font(.headline)
                        .foregroundColor(.primary)
                }
                .padding(.bottom, 4)

                content()
            }
        }
        .padding(.horizontal, 16)
    }
}

/**
 * Main settings content with glass cards.
 */
struct SettingsContent: View {
    let settings: AppSettings
    let viewModel: SettingsViewModel
    let appComponent: AppComponent
    let onClearData: () -> Void
    let onExportData: () -> Void
    let onImportData: () -> Void

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                // Tracking Section
                SettingsSectionCard(title: "Location Tracking", icon: "location.fill", tint: .coastalPath) {
                    TrackingPreferencesSection(
                        preferences: settings.trackingPreferences,
                        onUpdate: { viewModel.updateTrackingPreferences($0) }
                    )
                }

                // Privacy Section
                SettingsSectionCard(title: "Privacy", icon: "lock.fill", tint: .blueSlate) {
                    PrivacySettingsSection(
                        privacy: settings.privacySettings,
                        onUpdate: { viewModel.updatePrivacySettings($0) }
                    )
                }

                // Units Section
                SettingsSectionCard(title: "Units & Format", icon: "ruler.fill", tint: .coolSteel) {
                    UnitPreferencesSection(
                        units: settings.unitPreferences,
                        onUpdate: { viewModel.updateUnitPreferences($0) }
                    )
                }

                // Appearance Section
                SettingsSectionCard(title: "Appearance", icon: "paintbrush.fill", tint: .lightBlue) {
                    AppearanceSettingsSection(
                        appearance: settings.appearanceSettings,
                        onUpdate: { viewModel.updateAppearanceSettings($0) }
                    )
                }

                // Account Section
                SettingsSectionCard(title: "Account & Sync", icon: "person.fill", tint: .seaGlass) {
                    AccountSettingsSection(
                        account: settings.accountSettings,
                        appComponent: appComponent
                    )
                }

                // Data Management Section
                SettingsSectionCard(title: "Data Management", icon: "externaldrive.fill", tint: .neutralCategory) {
                    DataManagementSection(
                        data: settings.dataManagement,
                        onClearData: onClearData,
                        onExportData: onExportData,
                        onImportData: onImportData
                    )
                }

                // Developer Section
                SettingsSectionCard(title: "Developer", icon: "wrench.and.screwdriver.fill", tint: .seaGlass) {
                    DeveloperSection(appComponent: appComponent)
                }

                // About Section
                SettingsSectionCard(title: "About", icon: "info.circle.fill", tint: .blueSlate) {
                    AboutSection()
                }

                // Reset Button
                GlassButton(
                    title: "Reset All Settings",
                    icon: "arrow.clockwise",
                    variant: .outlined,
                    tint: .driftwood
                ) {
                    viewModel.resetToDefaults()
                }
                .padding(.horizontal, 16)
            }
            .padding(.vertical, 16)
            .padding(.bottom, 96) // Extra padding for tab bar
        }
    }
}

/**
 * Empty settings view with glass styling.
 */
struct EmptySettingsView: View {
    var body: some View {
        VStack(spacing: 20) {
            Image(systemName: "gearshape.fill")
                .font(.system(size: 64))
                .foregroundColor(.coolSteel)

            Text("Settings Unavailable")
                .font(.title2)
                .fontWeight(.semibold)
                .foregroundColor(.primary)

            Text("Unable to load settings at this time")
                .font(.body)
                .foregroundColor(.secondary)
        }
        .padding(32)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

// MARK: - ViewModel

/**
 * ViewModel bridging SwiftUI to Kotlin controller.
 */
class SettingsViewModel: ObservableObject {
    private let controller: SettingsController
    private var stateObserver: KotlinJob?

    @Published var settings: AppSettings?
    @Published var isLoading: Bool = true
    @Published var error: String?
    @Published var showError: Bool = false

    init(controller: SettingsController) {
        self.controller = controller
        observeState()
    }

    deinit {
        stateObserver?.cancel(cause: nil)
    }

    func updateTrackingPreferences(_ preferences: TrackingPreferences) {
        controller.updateTrackingPreferences(preferences: preferences)
    }

    func updatePrivacySettings(_ privacy: PrivacySettings) {
        controller.updatePrivacySettings(privacy: privacy)
    }

    func updateUnitPreferences(_ units: UnitPreferences) {
        controller.updateUnitPreferences(units: units)
    }

    func updateAppearanceSettings(_ appearance: AppearanceSettings) {
        controller.updateAppearanceSettings(appearance: appearance)
    }

    func resetToDefaults() {
        controller.resetToDefaults()
    }

    func clearAllData() {
        controller.clearAllData()
    }

    func clearError() {
        controller.clearError()
        showError = false
    }

    private func observeState() {
        stateObserver = controller.state.subscribe { [weak self] (state: SettingsState?) in
            guard let self = self, let state = state else { return }

            Task { @MainActor in
                self.isLoading = state.isLoading
                self.error = state.error
                self.showError = state.error != nil
                self.settings = state.settings
            }
        }
    }
}

// MARK: - Helper Functions

func accuracyName(_ accuracy: TrackingAccuracy) -> String {
    switch accuracy {
    case .high: return "High"
    case .balanced: return "Balanced"
    case .low: return "Low"
    default: return "Unknown"
    }
}

func intervalName(_ interval: UpdateInterval) -> String {
    switch interval {
    case .frequent: return "Frequent (30s)"
    case .normal: return "Normal (2min)"
    case .batterySaver: return "Battery Saver (10min)"
    default: return "Unknown"
    }
}

func distanceUnitName(_ unit: DistanceUnit) -> String {
    switch unit {
    case .metric: return "Metric (km)"
    case .imperial: return "Imperial (mi)"
    default: return "Unknown"
    }
}

func tempUnitName(_ unit: TemperatureUnit) -> String {
    switch unit {
    case .celsius: return "Celsius"
    case .fahrenheit: return "Fahrenheit"
    default: return "Unknown"
    }
}

func timeFormatName(_ format: TimeFormat) -> String {
    switch format {
    case .twelveHour: return "12-hour"
    case .twentyFourHour: return "24-hour"
    default: return "Unknown"
    }
}

func themeName(_ theme: AppTheme) -> String {
    switch theme {
    case .light: return "Light"
    case .dark: return "Dark"
    case .system: return "System"
    default: return "Unknown"
    }
}

func formatTimestamp(_ instant: KotlinInstant) -> String {
    let date = Date(timeIntervalSince1970: TimeInterval(instant.epochSeconds))
    let formatter = DateFormatter()
    formatter.dateStyle = .medium
    formatter.timeStyle = .short
    return formatter.string(from: date)
}
