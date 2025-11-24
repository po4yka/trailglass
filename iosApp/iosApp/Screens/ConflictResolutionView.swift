import SwiftUI
import Shared

/// Conflict Resolution screen showing sync conflicts
struct ConflictResolutionView: View {
    @StateObject private var viewModel: ConflictResolutionViewModel
    @Environment(\.dismiss) private var dismiss

    init(controller: ConflictResolutionController) {
        _viewModel = StateObject(wrappedValue: ConflictResolutionViewModel(controller: controller))
    }

    var body: some View {
        NavigationView {
            ZStack {
                if viewModel.isComplete {
                    completionView
                } else if let conflict = viewModel.currentConflict {
                    conflictView(conflict: conflict)
                } else {
                    emptyView
                }

                // Error alert
                if viewModel.showError, let errorMessage = viewModel.errorMessage {
                    VStack {
                        Spacer()

                        HStack {
                            Text(errorMessage)
                                .foregroundColor(.white)
                                .font(.body)

                            Spacer()

                            Button("Dismiss") {
                                viewModel.clearError()
                            }
                            .foregroundColor(.white)
                        }
                        .padding()
                        .background(Color.red)
                        .cornerRadius(8)
                        .padding()
                    }
                }
            }
            .navigationTitle("Resolve Conflicts")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Close") {
                        dismiss()
                    }
                }
            }
        }
    }

    private func conflictView(conflict: SyncConflictDto) -> some View {
        ScrollView {
            VStack(spacing: 16) {
                // Progress
                ProgressView(value: Double(viewModel.currentIndex + 1), total: Double(viewModel.totalConflicts))
                    .padding(.horizontal)

                Text("Conflict \(viewModel.currentIndex + 1) of \(viewModel.totalConflicts)")
                    .font(.caption)
                    .foregroundColor(.secondary)

                // Conflict info
                VStack(alignment: .leading, spacing: 8) {
                    HStack {
                        Image(systemName: "exclamationmark.triangle.fill")
                            .foregroundColor(.orange)

                        VStack(alignment: .leading) {
                            Text(formatEntityType(conflict.entityType))
                                .font(.headline)

                            Text(formatConflictType(conflict.conflictType))
                                .font(.subheadline)
                                .foregroundColor(.secondary)
                        }
                    }
                }
                .padding()
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(Color.orange.opacity(0.1))
                .cornerRadius(12)
                .padding(.horizontal)

                // Local version
                VersionCard(
                    title: "Your Version (Local)",
                    icon: "iphone",
                    data: conflict.localVersion
                )

                // Remote version
                VersionCard(
                    title: "Server Version (Remote)",
                    icon: "cloud",
                    data: conflict.remoteVersion
                )

                // Resolution options
                VStack(spacing: 12) {
                    Text("Choose Resolution")
                        .font(.headline)
                        .frame(maxWidth: .infinity, alignment: .leading)

                    Button(action: { viewModel.resolveKeepLocal() }) {
                        HStack {
                            Image(systemName: "iphone")
                            Text("Keep My Version")
                        }
                        .frame(maxWidth: .infinity)
                    }
                    .buttonStyle(.borderedProminent)
                    .disabled(viewModel.isResolving)

                    Button(action: { viewModel.resolveKeepRemote() }) {
                        HStack {
                            Image(systemName: "cloud")
                            Text("Keep Server Version")
                        }
                        .frame(maxWidth: .infinity)
                    }
                    .buttonStyle(.bordered)
                    .disabled(viewModel.isResolving)

                    Button(action: { viewModel.resolveMerge() }) {
                        HStack {
                            Image(systemName: "arrow.triangle.merge")
                            Text("Merge Both")
                        }
                        .frame(maxWidth: .infinity)
                    }
                    .buttonStyle(.bordered)
                    .disabled(viewModel.isResolving)

                    HStack(spacing: 12) {
                        if viewModel.currentIndex > 0 {
                            Button("Previous") {
                                viewModel.previousConflict()
                            }
                            .buttonStyle(.bordered)
                            .disabled(viewModel.isResolving)
                        }

                        if viewModel.hasMoreConflicts {
                            Button("Skip") {
                                viewModel.skipConflict()
                            }
                            .buttonStyle(.bordered)
                            .disabled(viewModel.isResolving)
                        }
                    }
                }
                .padding(.horizontal)

                if viewModel.isResolving {
                    ProgressView("Resolving...")
                        .padding()
                }
            }
            .padding(.vertical)
        }
    }

    private var completionView: some View {
        VStack(spacing: 24) {
            Image(systemName: "checkmark.circle.fill")
                .font(.system(size: 64))
                .foregroundColor(.green)

            Text("All Conflicts Resolved!")
                .font(.title2)
                .fontWeight(.bold)

            Text("Successfully resolved \(viewModel.resolvedCount) \(viewModel.resolvedCount == 1 ? "conflict" : "conflicts")")
                .font(.body)
                .foregroundColor(.secondary)

            Button("Done") {
                dismiss()
            }
            .buttonStyle(.borderedProminent)
        }
        .padding(32)
    }

    private var emptyView: some View {
        VStack(spacing: 24) {
            Image(systemName: "info.circle")
                .font(.system(size: 64))
                .foregroundColor(.blue)

            Text("No Conflicts")
                .font(.title2)

            Text("There are no sync conflicts to resolve")
                .font(.body)
                .foregroundColor(.secondary)

            Button("Close") {
                dismiss()
            }
        }
        .padding(32)
    }

    private func formatEntityType(_ type: EntityType) -> String {
        switch type {
        case .location: return "Location"
        case .placeVisit: return "Place Visit"
        case .trip: return "Trip"
        case .photo: return "Photo"
        case .settings: return "Settings"
        default: return "Unknown"
        }
    }

    private func formatConflictType(_ type: ConflictType) -> String {
        switch type {
        case .concurrentModification: return "Modified on both devices"
        case .deletionConflict: return "Deleted on one, modified on another"
        case .versionMismatch: return "Version mismatch"
        default: return "Unknown conflict"
        }
    }
}

struct VersionCard: View {
    let title: String
    let icon: String
    let data: [String: String]

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Image(systemName: icon)
                    .foregroundColor(.blue)

                Text(title)
                    .font(.headline)
            }

            Divider()

            ForEach(Array(data.keys.sorted()), id: \.self) { key in
                HStack(alignment: .top) {
                    Text("\(key):")
                        .font(.caption)
                        .fontWeight(.medium)
                        .frame(width: 100, alignment: .leading)

                    Text(data[key] ?? "")
                        .font(.caption)
                        .foregroundColor(.secondary)

                    Spacer()
                }
            }
        }
        .padding()
        .background(Color(.systemGray6))
        .cornerRadius(12)
        .padding(.horizontal)
    }
}

/// ViewModel for ConflictResolutionView
class ConflictResolutionViewModel: ObservableObject {
    private let controller: ConflictResolutionController
    private var stateObserver: Kotlinx_coroutines_coreJob?

    @Published var currentConflict: SyncConflictDto?
    @Published var currentIndex: Int = 0
    @Published var totalConflicts: Int = 0
    @Published var isResolving = false
    @Published var errorMessage: String?
    @Published var showError = false
    @Published var resolvedCount = 0
    @Published var isComplete = false
    @Published var hasMoreConflicts = false

    init(controller: ConflictResolutionController) {
        self.controller = controller

        // Observe state changes
        stateObserver = controller.state.subscribe { [weak self] state in
            guard let self = self, let state = state else { return }

            DispatchQueue.main.async {
                self.currentConflict = state.currentConflict
                self.currentIndex = Int(state.currentConflictIndex)
                self.totalConflicts = state.conflicts.count
                self.isResolving = state.isResolving
                self.errorMessage = state.error
                self.showError = state.error != nil
                self.resolvedCount = Int(state.resolvedCount)
                self.isComplete = state.isComplete
                self.hasMoreConflicts = state.hasMoreConflicts
            }
        }
    }

    func resolveKeepLocal() {
        controller.resolveKeepLocal()
    }

    func resolveKeepRemote() {
        controller.resolveKeepRemote()
    }

    func resolveMerge() {
        controller.resolveMerge()
    }

    func skipConflict() {
        controller.skipConflict()
    }

    func previousConflict() {
        controller.previousConflict()
    }

    func clearError() {
        controller.clearError()
        showError = false
    }

    deinit {
        stateObserver?.cancel(cause: nil)
    }
}

#Preview {
    Text("ConflictResolutionView Preview - Requires DI setup")
}
