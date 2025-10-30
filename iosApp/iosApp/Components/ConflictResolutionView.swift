import SwiftUI
import shared

/// Screen showing list of unresolved conflicts
struct ConflictListView: View {
    let conflicts: [ConflictUiModel]
    let onConflictTap: (ConflictUiModel) -> Void
    let onDismiss: () -> Void

    var body: some View {
        NavigationView {
            Group {
                if conflicts.isEmpty {
                    VStack(spacing: 16) {
                        Image(systemName: "checkmark.circle")
                            .font(.system(size: 64))
                            .foregroundColor(.green)

                        Text("No Conflicts")
                            .font(.title)

                        Text("All data is in sync")
                            .font(.body)
                            .foregroundColor(.secondary)
                    }
                } else {
                    List(conflicts, id: \.conflictId) { conflict in
                        ConflictCard(conflict: conflict)
                            .onTapGesture {
                                onConflictTap(conflict)
                            }
                            .listRowInsets(EdgeInsets(top: 8, leading: 16, bottom: 8, trailing: 16))
                            .listRowSeparator(.hidden)
                    }
                    .listStyle(.plain)
                }
            }
            .navigationTitle("Sync Conflicts (\(conflicts.count))")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Done") {
                        onDismiss()
                    }
                }
            }
        }
    }
}

/// Card displaying a single conflict
struct ConflictCard: View {
    let conflict: ConflictUiModel

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            // Header
            HStack {
                HStack(spacing: 8) {
                    Image(systemName: "exclamationmark.triangle.fill")
                        .foregroundColor(.red)

                    Text(conflict.entityName)
                        .font(.headline)
                }

                Spacer()

                EntityTypeChip(entityType: conflict.entityType.name)
            }

            // Description
            Text(conflict.conflictDescription)
                .font(.body)
                .foregroundColor(.primary)

            // Versions
            HStack(spacing: 16) {
                VStack(alignment: .leading, spacing: 4) {
                    Text("Local (v\(conflict.localVersion))")
                        .font(.caption)
                        .foregroundColor(.secondary)

                    Text(formatDateTime(conflict.localModified))
                        .font(.caption2)
                        .foregroundColor(.secondary)
                }

                Spacer()

                VStack(alignment: .leading, spacing: 4) {
                    Text("Remote (v\(conflict.remoteVersion))")
                        .font(.caption)
                        .foregroundColor(.secondary)

                    Text(formatDateTime(conflict.remoteModified))
                        .font(.caption2)
                        .foregroundColor(.secondary)
                }
            }

            // Action button
            HStack {
                Spacer()

                Button("Resolve") {}
                    .buttonStyle(.borderedProminent)
                    .tint(.red)
            }
        }
        .padding()
        .background(Color.red.opacity(0.1))
        .cornerRadius(12)
    }

    private func formatDateTime(_ instant: Kotlinx_datetimeInstant) -> String {
        let date = Date(timeIntervalSince1970: TimeInterval(instant.epochSeconds))
        let formatter = DateFormatter()
        formatter.dateStyle = .short
        formatter.timeStyle = .short
        return formatter.string(from: date)
    }
}

/// Dialog for resolving a conflict
struct ConflictResolutionSheet: View {
    let conflict: ConflictUiModel
    let onResolve: (ConflictResolutionChoice) -> Void
    let onDismiss: () -> Void

    var body: some View {
        NavigationView {
            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    // Description
                    VStack(alignment: .leading, spacing: 8) {
                        HStack {
                            Image(systemName: "exclamationmark.triangle.fill")
                                .foregroundColor(.red)

                            Text("Conflict Details")
                                .font(.headline)
                        }

                        Text(conflict.conflictDescription)
                            .font(.body)
                            .foregroundColor(.secondary)
                    }

                    // Local version
                    VStack(alignment: .leading, spacing: 12) {
                        Text("Your Local Version")
                            .font(.headline)
                            .foregroundColor(.blue)

                        VStack(alignment: .leading, spacing: 8) {
                            Text("Modified: \(formatDateTime(conflict.localModified))")
                                .font(.caption)
                                .foregroundColor(.secondary)

                            Text(conflict.localPreview)
                                .font(.body)
                                .padding()
                                .frame(maxWidth: .infinity, alignment: .leading)
                                .background(Color.blue.opacity(0.1))
                                .cornerRadius(8)
                        }

                        Button(action: { onResolve(.keepLocal) }) {
                            Text("Keep Local Version")
                                .frame(maxWidth: .infinity)
                        }
                        .buttonStyle(.borderedProminent)
                        .tint(.blue)
                    }
                    .padding()
                    .background(Color.blue.opacity(0.05))
                    .cornerRadius(12)

                    // Remote version
                    VStack(alignment: .leading, spacing: 12) {
                        Text("Server Version")
                            .font(.headline)
                            .foregroundColor(.green)

                        VStack(alignment: .leading, spacing: 8) {
                            Text("Modified: \(formatDateTime(conflict.remoteModified))")
                                .font(.caption)
                                .foregroundColor(.secondary)

                            Text(conflict.remotePreview)
                                .font(.body)
                                .padding()
                                .frame(maxWidth: .infinity, alignment: .leading)
                                .background(Color.green.opacity(0.1))
                                .cornerRadius(8)
                        }

                        Button(action: { onResolve(.keepRemote) }) {
                            Text("Use Server Version")
                                .frame(maxWidth: .infinity)
                        }
                        .buttonStyle(.borderedProminent)
                        .tint(.green)
                    }
                    .padding()
                    .background(Color.green.opacity(0.05))
                    .cornerRadius(12)

                    // Merge option
                    Button(action: { onResolve(.merge) }) {
                        Text("Merge Automatically")
                            .frame(maxWidth: .infinity)
                    }
                    .buttonStyle(.bordered)
                }
                .padding()
            }
            .navigationTitle("Resolve Conflict")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Cancel") {
                        onDismiss()
                    }
                }
            }
        }
    }

    private func formatDateTime(_ instant: Kotlinx_datetimeInstant) -> String {
        let date = Date(timeIntervalSince1970: TimeInterval(instant.epochSeconds))
        let formatter = DateFormatter()
        formatter.dateStyle = .medium
        formatter.timeStyle = .short
        return formatter.string(from: date)
    }
}

/// Chip for entity type
struct EntityTypeChip: View {
    let entityType: String

    var body: some View {
        Text(entityType)
            .font(.caption)
            .padding(.horizontal, 8)
            .padding(.vertical, 4)
            .background(Color.blue.opacity(0.2))
            .foregroundColor(.blue)
            .cornerRadius(4)
    }
}

// MARK: - Preview

struct ConflictResolutionView_Previews: PreviewProvider {
    static var previews: some View {
        ConflictListView(
            conflicts: [],
            onConflictTap: { _ in },
            onDismiss: {}
        )
    }
}
