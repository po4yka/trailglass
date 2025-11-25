import SwiftUI
import Shared

/**
 * Tracking preferences section.
 */
struct TrackingPreferencesSection: View {
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
struct PrivacySettingsSection: View {
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
struct UnitPreferencesSection: View {
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
struct AppearanceSettingsSection: View {
    let appearance: AppearanceSettings
    let onUpdate: (AppearanceSettings) -> Void

    var body: some View {
        Picker("Theme", selection: Binding(
            get: { appearance.theme },
            set: { onUpdate(AppearanceSettings(
                theme: $0,
                useDeviceWallpaper: appearance.useDeviceWallpaper,
                showMapInTimeline: appearance.showMapInTimeline,
                compactView: appearance.compactView,
                photoGalleryViewMode: appearance.photoGalleryViewMode
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
                compactView: appearance.compactView,
                photoGalleryViewMode: appearance.photoGalleryViewMode
            )) }
        ))

        Toggle("Compact View", isOn: Binding(
            get: { appearance.compactView },
            set: { onUpdate(AppearanceSettings(
                theme: appearance.theme,
                useDeviceWallpaper: appearance.useDeviceWallpaper,
                showMapInTimeline: appearance.showMapInTimeline,
                compactView: $0,
                photoGalleryViewMode: appearance.photoGalleryViewMode
            )) }
        ))
    }
}

/**
 * Account settings section.
 */
struct AccountSettingsSection: View {
    let account: AccountSettings
    let appComponent: AppComponent

    var body: some View {
        HStack {
            Text("Email")
            Spacer()
            Text(account.email ?? "Not signed in")
                .foregroundColor(.secondary)
        }

        if account.email != nil {
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
        .accessibilityLabel("Device Management")
        .accessibilityHint("View and manage connected devices")
    }
}

/**
 * Developer section with navigation links to diagnostic tools.
 */
struct DeveloperSection: View {
    let appComponent: AppComponent

    var body: some View {
        NavigationLink(destination: RegionsListView(viewModel: RegionViewModel(appComponent: appComponent))) {
            HStack {
                Image(systemName: "mappin.circle.fill")
                    .frame(width: 24)
                    .foregroundColor(.coastalPath)
                Text("Places")
                Spacer()
                Image(systemName: "chevron.right")
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
        }
        .accessibilityLabel("Places")
        .accessibilityHint("View and manage monitored places")

        NavigationLink(destination: LogViewerView()) {
            HStack {
                Image(systemName: "doc.text.fill")
                    .frame(width: 24)
                    .foregroundColor(.blueSlate)
                Text("Logs")
                Spacer()
                Image(systemName: "chevron.right")
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
        }
        .accessibilityLabel("Logs")
        .accessibilityHint("View application logs and debug information")

        NavigationLink(destination: DiagnosticsView(controller: appComponent.diagnosticsController)) {
            HStack {
                Image(systemName: "stethoscope")
                    .frame(width: 24)
                    .foregroundColor(.coolSteel)
                Text("Diagnostics")
                Spacer()
                Image(systemName: "chevron.right")
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
        }
        .accessibilityLabel("Diagnostics")
        .accessibilityHint("View system diagnostics and health information")
    }
}

/**
 * Data management section with glass buttons.
 */
struct DataManagementSection: View {
    let data: DataManagement
    let onClearData: () -> Void
    let onExportData: () -> Void
    let onImportData: () -> Void

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
                    onExportData()
                }
                .accessibilityLabel("Export Data")
                .accessibilityHint("Export all your travel data to a file")

                GlassButton(
                    title: "Import Data",
                    icon: "square.and.arrow.down",
                    variant: .outlined,
                    tint: .coolSteel
                ) {
                    onImportData()
                }
                .accessibilityLabel("Import Data")
                .accessibilityHint("Import travel data from a file")

                GlassButton(
                    title: "Clear All Data",
                    icon: "trash.fill",
                    variant: .outlined,
                    tint: .driftwood,
                    action: onClearData
                )
                .accessibilityLabel("Clear All Data")
                .accessibilityHint("Permanently delete all travel data from this device")
            }
        }
    }
}

/**
 * Tracking mode selection section.
 * Provides simple interface for selecting battery-optimized tracking modes.
 */
struct TrackingModeSection: View {
    let currentMode: TrackingMode
    let onModeSelected: (TrackingMode) -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Tracking Mode")
                .font(.subheadline)
                .fontWeight(.semibold)
                .foregroundColor(.secondary)

            VStack(spacing: 8) {
                TrackingModeRow(
                    mode: .idle,
                    title: "Off",
                    description: "No tracking, GPS off",
                    batteryImpact: "No battery impact",
                    isSelected: currentMode == .idle,
                    onSelect: { onModeSelected(.idle) }
                )

                TrackingModeRow(
                    mode: .significant,
                    title: "Efficient",
                    description: "Major movements only (WiFi/Cell changes)",
                    batteryImpact: "Minimal (1-2% per day)",
                    isSelected: currentMode == .significant,
                    onSelect: { onModeSelected(.significant) }
                )

                TrackingModeRow(
                    mode: .passive,
                    title: "Balanced",
                    description: "Periodic updates (5min/500m)",
                    batteryImpact: "Low (3-5% per day)",
                    isSelected: currentMode == .passive,
                    onSelect: { onModeSelected(.passive) }
                )

                TrackingModeRow(
                    mode: .active,
                    title: "High",
                    description: "Continuous tracking (30sec/10m)",
                    batteryImpact: "Moderate (10-15% per day)",
                    isSelected: currentMode == .active,
                    onSelect: { onModeSelected(.active) }
                )
            }
        }
    }
}

/**
 * Individual tracking mode row with selection radio button.
 */
struct TrackingModeRow: View {
    let mode: TrackingMode
    let title: String
    let description: String
    let batteryImpact: String
    let isSelected: Bool
    let onSelect: () -> Void

    var body: some View {
        Button(action: onSelect) {
            HStack(spacing: 12) {
                Image(systemName: isSelected ? "circle.fill" : "circle")
                    .foregroundColor(isSelected ? .coastalPath : .secondary)
                    .font(.system(size: 20))

                VStack(alignment: .leading, spacing: 4) {
                    Text(title)
                        .font(.body)
                        .fontWeight(.medium)
                        .foregroundColor(.primary)

                    Text(description)
                        .font(.caption)
                        .foregroundColor(.secondary)

                    HStack(spacing: 4) {
                        Image(systemName: "battery.100")
                            .font(.caption2)
                            .foregroundColor(.coastalPath)
                        Text(batteryImpact)
                            .font(.caption2)
                            .foregroundColor(.coastalPath)
                    }
                }

                Spacer()
            }
            .padding(12)
            .background(
                RoundedRectangle(cornerRadius: 8)
                    .fill(isSelected ? Color.coastalPath.opacity(0.1) : Color.clear)
            )
            .overlay(
                RoundedRectangle(cornerRadius: 8)
                    .stroke(isSelected ? Color.coastalPath : Color.gray.opacity(0.3), lineWidth: 1)
            )
        }
        .buttonStyle(PlainButtonStyle())
        .accessibilityLabel("\(title) tracking mode")
        .accessibilityHint(isSelected ? "Currently selected, \(batteryImpact)" : "Double tap to switch to \(title) mode, \(batteryImpact)")
        .accessibilityAddTraits(isSelected ? [.isButton, .isSelected] : .isButton)
    }
}

/**
 * About section.
 */
struct AboutSection: View {
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
