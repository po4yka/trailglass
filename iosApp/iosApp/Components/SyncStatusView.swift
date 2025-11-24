import SwiftUI
import Shared

/// Compact sync status indicator for toolbar
struct SyncStatusIndicator: View {
    let syncStatus: SyncStatusUiModel
    let action: () -> Void

    @State private var rotation: Double = 0

    var body: some View {
        Button(action: action) {
            ZStack(alignment: .topTrailing) {
                // Main icon
                Image(systemName: iconName)
                    .foregroundColor(iconColor)
                    .rotationEffect(.degrees(isActive ? rotation : 0))
                    .onAppear {
                        if isActive {
                            withAnimation(.linear(duration: 2).repeatForever(autoreverses: false)) {
                                rotation = 360
                            }
                        }
                    }

                // Pending count badge
                if syncStatus.pendingCount > 0 && !isActive {
                    ZStack {
                        Circle()
                            .fill(Color.adaptiveWarning)
                            .frame(width: 16, height: 16)

                        Text(syncStatus.pendingCount > 9 ? "9+" : "\(syncStatus.pendingCount)")
                            .font(.system(size: 10, weight: .bold))
                            .foregroundColor(.white)
                    }
                    .offset(x: 8, y: -8)
                }

                // Conflict badge
                if syncStatus.conflictCount > 0 {
                    Image(systemName: "exclamationmark.triangle.fill")
                        .font(.system(size: 10))
                        .foregroundColor(.adaptiveWarning)
                        .offset(x: 8, y: 8)
                }
            }
        }
    }

    private var isActive: Bool {
        syncStatus.isActive
    }

    private var iconName: String {
        switch syncStatus.progress {
        case is SyncProgress.Idle:
            return "icloud"
        case is SyncProgress.InProgress:
            return "icloud.and.arrow.up.and.down"
        case is SyncProgress.Completed:
            return "icloud.fill"
        case is SyncProgress.Failed:
            return "icloud.slash"
        default:
            return "icloud"
        }
    }

    private var iconColor: Color {
        switch syncStatus.progress {
        case is SyncProgress.Idle:
            return .adaptiveDisabled
        case is SyncProgress.InProgress:
            return .adaptivePrimary
        case is SyncProgress.Completed:
            return .adaptiveSuccess
        case is SyncProgress.Failed:
            return .adaptiveWarning
        default:
            return .adaptiveDisabled
        }
    }
}

/// Detailed sync status card
struct SyncStatusCard: View {
    let syncStatus: SyncStatusUiModel
    let onSync: () -> Void
    let onViewConflicts: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            // Header
            HStack {
                Text("Sync Status")
                    .font(.headline)

                Spacer()

                SyncStatusIndicator(syncStatus: syncStatus, action: {})
            }

            // Progress
            progressView

            // Info row
            HStack {
                VStack(alignment: .leading, spacing: 4) {
                    Text("Pending: \(syncStatus.pendingCount)")
                        .font(.caption)
                        .foregroundColor(.secondary)

                    if let lastSync = syncStatus.lastSyncTime {
                        Text("Last sync: \(formatLastSyncTime(lastSync))")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                }

                Spacer()

                if syncStatus.conflictCount > 0 {
                    Button(action: onViewConflicts) {
                        HStack(spacing: 4) {
                            Image(systemName: "exclamationmark.triangle")
                            Text("\(syncStatus.conflictCount) Conflicts")
                        }
                        .font(.caption)
                        .foregroundColor(.adaptiveWarning)
                    }
                }
            }

            // Sync button
            Button(action: onSync) {
                HStack {
                    Image(systemName: "arrow.triangle.2.circlepath")
                    Text(isActive ? "Syncing..." : "Sync Now")
                }
                .frame(maxWidth: .infinity)
            }
            .buttonStyle(.borderedProminent)
            .disabled(isActive)
        }
        .padding()
        .background(backgroundColor)
        .cornerRadius(12)
    }

    private var isActive: Bool {
        syncStatus.isActive
    }

    @ViewBuilder
    private var progressView: some View {
        switch syncStatus.progress {
        case is SyncProgress.Idle:
            Text("Ready to sync")
                .font(.body)
                .foregroundColor(.secondary)

        case let progress as SyncProgress.InProgress:
            VStack(alignment: .leading, spacing: 8) {
                ProgressView(value: Double(progress.percentage) / 100.0)
                Text(progress.message)
                    .font(.body)
                    .foregroundColor(.secondary)
            }

        case let completed as SyncProgress.Completed:
            VStack(alignment: .leading, spacing: 4) {
                Text("Last sync completed")
                    .font(.body)
                    .foregroundColor(.adaptiveSuccess)

                Text("↑ \(completed.result.uploaded) uploaded, ↓ \(completed.result.downloaded) downloaded")
                    .font(.caption)
                    .foregroundColor(.secondary)

                if completed.result.conflicts > 0 {
                    Text("⚠ \(completed.result.conflicts) conflicts")
                        .font(.caption)
                        .foregroundColor(.adaptiveWarning)
                }
            }

        case let failed as SyncProgress.Failed:
            Text("Sync failed: \(failed.error)")
                .font(.body)
                .foregroundColor(.adaptiveWarning)

        default:
            Text("Unknown status")
                .font(.body)
                .foregroundColor(.secondary)
        }
    }

    private var backgroundColor: Color {
        switch syncStatus.progress {
        case is SyncProgress.Failed:
            return Color.adaptiveWarning.opacity(0.1)
        default:
            return Color(uiColor: .secondarySystemGroupedBackground)
        }
    }

    private func formatLastSyncTime(_ instant: Kotlinx_datetimeInstant) -> String {
        let date = Date(timeIntervalSince1970: TimeInterval(instant.epochSeconds))
        let formatter = DateFormatter()
        formatter.timeStyle = .short
        return formatter.string(from: date)
    }
}

// MARK: - Preview

struct SyncStatusView_Previews: PreviewProvider {
    static var previews: some View {
        VStack(spacing: 20) {
            SyncStatusCard(
                syncStatus: SyncStatusUiModel(
                    isActive: false,
                    progress: SyncProgress.Idle(),
                    lastSyncTime: nil,
                    pendingCount: 5,
                    conflictCount: 2,
                    lastError: nil,
                    networkState: NetworkState.CONNECTED,
                    networkType: NetworkType.WIFI,
                    isNetworkMetered: false
                ),
                onSync: {},
                onViewConflicts: {}
            )
            .padding()
        }
    }
}
