import SwiftUI
import shared

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

    var body: some View {
        ZStack {
            Color.backgroundLight.ignoresSafeArea()

            VStack(spacing: 0) {
                // Standard navigation bar for modal-style settings
                HStack {
                    Text("Settings")
                        .font(.largeTitle)
                        .fontWeight(.bold)
                        .foregroundColor(.primary)

                    Spacer()
                }
                .padding(.horizontal, 20)
                .padding(.vertical, 16)
                .background(
                    Rectangle()
                        .fill(.ultraThinMaterial)
                        .overlay(
                            Rectangle()
                                .fill(Color.lightCyan.opacity(0.2))
                        )
                )

                if viewModel.isLoading {
                    GlassLoadingIndicator(variant: .pulsing, color: .coolSteel)
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else if let settings = viewModel.settings {
                    SettingsContent(
                        settings: settings,
                        viewModel: viewModel,
                        appComponent: appComponent,
                        onClearData: { showClearDataAlert = true }
                    )
                } else {
                    EmptySettingsView()
                }
            }
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
                Text("This will permanently delete all your data including trips, locations, photos, and settings. This action cannot be undone.\n\nAre you sure you want to continue?")
            }
        }
        .navigationBarHidden(true)
    }
}

/**
 * Main settings content with glass cards.
 */
private struct SettingsContent: View {
    let settings: AppSettings
    let viewModel: SettingsViewModel
    let appComponent: AppComponent
    let onClearData: () -> Void

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
                        onClearData: onClearData
                    )
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
 * Settings section card with glass styling.
 */
private struct SettingsSectionCard<Content: View>: View {
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
 * Tracking preferences section.
 */
private struct TrackingPreferencesSection: View {
    let preferences: TrackingPreferences
    let onUpdate: (TrackingPreferences) -> Void

    var body: some View {
        // Accuracy
        Picker("Tracking Accuracy", selection: Binding(
            get: { preferences.accuracy },
            set: { onUpdate(TrackingPreferences(
                accuracy: $0,
                updateInterval: preferences.updateInterval,
                batteryOptimization: preferences.batteryOptimization,
                trackWhenStationary: preferences.trackWhenStationary,
                minimumDistance: preferences.minimumDistance
            )) }
        )) {
            ForEach([TrackingAccuracy.high, .balanced, .low], id: \.self) { accuracy in
                Text(accuracyName(accuracy)).tag(accuracy)
            }
        }

        // Update Interval
        Picker("Update Interval", selection: Binding(
            get: { preferences.updateInterval },
            set: { onUpdate(TrackingPreferences(
                accuracy: preferences.accuracy,
                updateInterval: $0,
                batteryOptimization: preferences.batteryOptimization,
                trackWhenStationary: preferences.trackWhenStationary,
                minimumDistance: preferences.minimumDistance
            )) }
        )) {
            ForEach([UpdateInterval.frequent, .normal, .batterySaver], id: \.self) { interval in
                Text(intervalName(interval)).tag(interval)
            }
        }

        // Battery Optimization
        Toggle("Battery Optimization", isOn: Binding(
            get: { preferences.batteryOptimization },
            set: { onUpdate(TrackingPreferences(
                accuracy: preferences.accuracy,
                updateInterval: preferences.updateInterval,
                batteryOptimization: $0,
                trackWhenStationary: preferences.trackWhenStationary,
                minimumDistance: preferences.minimumDistance
            )) }
        ))

        // Track When Stationary
        Toggle("Track When Stationary", isOn: Binding(
            get: { preferences.trackWhenStationary },
            set: { onUpdate(TrackingPreferences(
                accuracy: preferences.accuracy,
                updateInterval: preferences.updateInterval,
                batteryOptimization: preferences.batteryOptimization,
                trackWhenStationary: $0,
                minimumDistance: preferences.minimumDistance
            )) }
        ))
    }
}

/**
 * Privacy settings section.
 */
private struct PrivacySettingsSection: View {
    let privacy: PrivacySettings
    let onUpdate: (PrivacySettings) -> Void

    var body: some View {
        Toggle("Share Analytics", isOn: Binding(
            get: { privacy.shareAnalytics },
            set: { onUpdate(PrivacySettings(
                dataRetentionDays: privacy.dataRetentionDays,
                shareAnalytics: $0,
                shareCrashReports: privacy.shareCrashReports,
                autoBackup: privacy.autoBackup,
                encryptBackups: privacy.encryptBackups,
                enableE2EEncryption: privacy.enableE2EEncryption
            )) }
        ))

        Toggle("Share Crash Reports", isOn: Binding(
            get: { privacy.shareCrashReports },
            set: { onUpdate(PrivacySettings(
                dataRetentionDays: privacy.dataRetentionDays,
                shareAnalytics: privacy.shareAnalytics,
                shareCrashReports: $0,
                autoBackup: privacy.autoBackup,
                encryptBackups: privacy.encryptBackups,
                enableE2EEncryption: privacy.enableE2EEncryption
            )) }
        ))

        Toggle("Auto Backup", isOn: Binding(
            get: { privacy.autoBackup },
            set: { onUpdate(PrivacySettings(
                dataRetentionDays: privacy.dataRetentionDays,
                shareAnalytics: privacy.shareAnalytics,
                shareCrashReports: privacy.shareCrashReports,
                autoBackup: $0,
                encryptBackups: privacy.encryptBackups,
                enableE2EEncryption: privacy.enableE2EEncryption
            )) }
        ))

        Toggle("Encrypt Backups", isOn: Binding(
            get: { privacy.encryptBackups },
            set: { onUpdate(PrivacySettings(
                dataRetentionDays: privacy.dataRetentionDays,
                shareAnalytics: privacy.shareAnalytics,
                shareCrashReports: privacy.shareCrashReports,
                autoBackup: privacy.autoBackup,
                encryptBackups: $0,
                enableE2EEncryption: privacy.enableE2EEncryption
            )) }
        ))

        Toggle("End-to-End Encryption", isOn: Binding(
            get: { privacy.enableE2EEncryption },
            set: { onUpdate(PrivacySettings(
                dataRetentionDays: privacy.dataRetentionDays,
                shareAnalytics: privacy.shareAnalytics,
                shareCrashReports: privacy.shareCrashReports,
                autoBackup: privacy.autoBackup,
                encryptBackups: privacy.encryptBackups,
                enableE2EEncryption: $0
            )) }
        ))

        HStack {
            Text("Data Retention")
            Spacer()
            Text("\(privacy.dataRetentionDays) days")
                .foregroundColor(.secondary)
        }
    }
}

/**
 * Unit preferences section.
 */
private struct UnitPreferencesSection: View {
    let units: UnitPreferences
    let onUpdate: (UnitPreferences) -> Void

    var body: some View {
        Picker("Distance Units", selection: Binding(
            get: { units.distanceUnit },
            set: { onUpdate(UnitPreferences(
                distanceUnit: $0,
                temperatureUnit: units.temperatureUnit,
                timeFormat: units.timeFormat
            )) }
        )) {
            ForEach([DistanceUnit.metric, .imperial], id: \.self) { unit in
                Text(distanceUnitName(unit)).tag(unit)
            }
        }

        Picker("Temperature Units", selection: Binding(
            get: { units.temperatureUnit },
            set: { onUpdate(UnitPreferences(
                distanceUnit: units.distanceUnit,
                temperatureUnit: $0,
                timeFormat: units.timeFormat
            )) }
        )) {
            ForEach([TemperatureUnit.celsius, .fahrenheit], id: \.self) { unit in
                Text(tempUnitName(unit)).tag(unit)
            }
        }

        Picker("Time Format", selection: Binding(
            get: { units.timeFormat },
            set: { onUpdate(UnitPreferences(
                distanceUnit: units.distanceUnit,
                temperatureUnit: units.temperatureUnit,
                timeFormat: $0
            )) }
        )) {
            ForEach([TimeFormat.twelveHour, .twentyFourHour], id: \.self) { format in
                Text(timeFormatName(format)).tag(format)
            }
        }
    }
}

/**
 * Appearance settings section.
 */
private struct AppearanceSettingsSection: View {
    let appearance: AppearanceSettings
    let onUpdate: (AppearanceSettings) -> Void

    var body: some View {
        Picker("Theme", selection: Binding(
            get: { appearance.theme },
            set: { onUpdate(AppearanceSettings(
                theme: $0,
                useDeviceWallpaper: appearance.useDeviceWallpaper,
                showMapInTimeline: appearance.showMapInTimeline,
                compactView: appearance.compactView
            )) }
        )) {
            ForEach([AppTheme.light, .dark, .system], id: \.self) { theme in
                Text(themeName(theme)).tag(theme)
            }
        }

        Toggle("Show Map in Timeline", isOn: Binding(
            get: { appearance.showMapInTimeline },
            set: { onUpdate(AppearanceSettings(
                theme: appearance.theme,
                useDeviceWallpaper: appearance.useDeviceWallpaper,
                showMapInTimeline: $0,
                compactView: appearance.compactView
            )) }
        ))

        Toggle("Compact View", isOn: Binding(
            get: { appearance.compactView },
            set: { onUpdate(AppearanceSettings(
                theme: appearance.theme,
                useDeviceWallpaper: appearance.useDeviceWallpaper,
                showMapInTimeline: appearance.showMapInTimeline,
                compactView: $0
            )) }
        ))
    }
}

/**
 * Account settings section.
 */
private struct AccountSettingsSection: View {
    let account: AccountSettings
    let appComponent: AppComponent

    var body: some View {
        HStack {
            Text("Email")
            Spacer()
            Text(account.email ?? "Not signed in")
                .foregroundColor(.secondary)
        }

        if account.email != null {
            Toggle("Auto Sync", isOn: .constant(account.autoSync))
                .disabled(true)

            Toggle("Sync on WiFi Only", isOn: .constant(account.syncOnWifiOnly))
                .disabled(true)

            if let lastSync = account.lastSyncTime {
                HStack {
                    Text("Last Sync")
                    Spacer()
                    Text(formatTimestamp(lastSync))
                        .foregroundColor(.secondary)
                }
            }
        }

        // Device Management navigation
        NavigationLink(destination: DeviceManagementView(controller: appComponent.deviceManagementController)) {
            HStack {
                Image(systemName: "externaldrive.connected.to.line.below")
                    .frame(width: 24)
                Text("Device Management")
            }
        }
    }
}

/**
 * Data management section with glass buttons.
 */
private struct DataManagementSection: View {
    let data: DataManagement
    let onClearData: () -> Void

    var body: some View {
        VStack(spacing: 12) {
            // Storage info
            HStack {
                HStack(spacing: 6) {
                    Image(systemName: "internaldrive")
                        .foregroundColor(.coolSteel)
                        .font(.subheadline)
                    Text("Storage Used")
                        .font(.subheadline)
                }
                Spacer()
                Text(String(format: "%.2f MB", data.storageUsedMb))
                    .font(.subheadline)
                    .fontWeight(.medium)
                    .foregroundColor(.blueSlate)
            }
            .padding(.vertical, 4)

            if let lastExport = data.lastExportTime {
                HStack {
                    HStack(spacing: 6) {
                        Image(systemName: "arrow.up.doc")
                            .foregroundColor(.coolSteel)
                            .font(.subheadline)
                        Text("Last Export")
                            .font(.subheadline)
                    }
                    Spacer()
                    Text(formatTimestamp(lastExport))
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                }
                .padding(.vertical, 4)
            }

            if let lastBackup = data.lastBackupTime {
                HStack {
                    HStack(spacing: 6) {
                        Image(systemName: "clock.arrow.circlepath")
                            .foregroundColor(.coolSteel)
                            .font(.subheadline)
                        Text("Last Backup")
                            .font(.subheadline)
                    }
                    Spacer()
                    Text(formatTimestamp(lastBackup))
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                }
                .padding(.vertical, 4)
            }

            Divider()
                .padding(.vertical, 4)

            // Action buttons
            VStack(spacing: 10) {
                GlassButton(
                    title: "Export Data",
                    icon: "square.and.arrow.up",
                    variant: .outlined,
                    tint: .coolSteel
                ) {
                    // TODO: Export data
                }

                GlassButton(
                    title: "Import Data",
                    icon: "square.and.arrow.down",
                    variant: .outlined,
                    tint: .coolSteel
                ) {
                    // TODO: Import data
                }

                GlassButton(
                    title: "Clear All Data",
                    icon: "trash.fill",
                    variant: .outlined,
                    tint: .driftwood,
                    action: onClearData
                )
            }
        }
    }
}

/**
 * About section.
 */
private struct AboutSection: View {
    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text("TrailGlass")
                .font(.title3)
                .fontWeight(.bold)

            Text("Version 1.0.0-alpha")
                .font(.body)
                .foregroundColor(.secondary)

            Text("A comprehensive location tracking and travel management app")
                .font(.caption)
                .foregroundColor(.secondary)
                .padding(.top, 4)
        }
    }
}

/**
 * Empty settings view with glass styling.
 */
private struct EmptySettingsView: View {
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

    @Published var settings: AppSettings?
    @Published var isLoading: Bool = true
    @Published var error: String?
    @Published var showError: Bool = false

    init(controller: SettingsController) {
        self.controller = controller
        observeState()
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
        // TODO: Implement StateFlow observation bridge
        // For now, manually trigger updates
    }
}

// MARK: - Helper Functions

private func accuracyName(_ accuracy: TrackingAccuracy) -> String {
    switch accuracy {
    case .high: return "High"
    case .balanced: return "Balanced"
    case .low: return "Low"
    @unknown default: return "Unknown"
    }
}

private func intervalName(_ interval: UpdateInterval) -> String {
    switch interval {
    case .frequent: return "Frequent (30s)"
    case .normal: return "Normal (2min)"
    case .batterySaver: return "Battery Saver (10min)"
    @unknown default: return "Unknown"
    }
}

private func distanceUnitName(_ unit: DistanceUnit) -> String {
    switch unit {
    case .metric: return "Metric (km)"
    case .imperial: return "Imperial (mi)"
    @unknown default: return "Unknown"
    }
}

private func tempUnitName(_ unit: TemperatureUnit) -> String {
    switch unit {
    case .celsius: return "Celsius"
    case .fahrenheit: return "Fahrenheit"
    @unknown default: return "Unknown"
    }
}

private func timeFormatName(_ format: TimeFormat) -> String {
    switch format {
    case .twelveHour: return "12-hour"
    case .twentyFourHour: return "24-hour"
    @unknown default: return "Unknown"
    }
}

private func themeName(_ theme: AppTheme) -> String {
    switch theme {
    case .light: return "Light"
    case .dark: return "Dark"
    case .system: return "System"
    @unknown default: return "Unknown"
    }
}

private func formatTimestamp(_ instant: KotlinInstant) -> String {
    let date = Date(timeIntervalSince1970: TimeInterval(instant.epochSeconds))
    let formatter = DateFormatter()
    formatter.dateStyle = .medium
    formatter.timeStyle = .short
    return formatter.string(from: date)
}
