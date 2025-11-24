import SwiftUI
import Shared

/// SwiftUI view showing permission rationale before requesting.
struct PermissionRationaleView: View {
    let requestState: PermissionRequestState
    let onAccept: () -> Void
    let onDeny: () -> Void

    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationView {
            ScrollView {
                VStack(alignment: .leading, spacing: 24) {
                    // Icon and title
                    HStack(spacing: 16) {
                        Image(systemName: permissionIcon)
                            .font(.system(size: 40))
                            .foregroundColor(.adaptivePrimary)

                        Text(requestState.rationale.title)
                            .font(.title2)
                            .fontWeight(.bold)
                    }

                    // Description
                    Text(requestState.rationale.description)
                        .font(.body)
                        .foregroundColor(.secondary)

                    // Features list
                    if !requestState.rationale.features.isEmpty {
                        VStack(alignment: .leading, spacing: 12) {
                            Text("This permission enables:")
                                .font(.headline)

                            ForEach(requestState.rationale.features, id: \.self) { feature in
                                HStack(alignment: .top, spacing: 12) {
                                    Image(systemName: "checkmark.circle.fill")
                                        .foregroundColor(.adaptiveSuccess)
                                        .font(.system(size: 20))

                                    Text(feature)
                                        .font(.body)
                                        .fixedSize(horizontal: false, vertical: true)
                                }
                            }
                        }
                    }

                    // Required indicator
                    if requestState.rationale.isRequired {
                        HStack(spacing: 12) {
                            Image(systemName: "exclamationmark.triangle.fill")
                                .foregroundColor(.adaptiveWarning)

                            Text("Required for core functionality")
                                .font(.caption)
                                .fontWeight(.semibold)
                        }
                        .padding()
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .background(Color.adaptiveWarning.opacity(0.1))
                        .cornerRadius(8)
                    }

                    Spacer()

                    // Buttons
                    VStack(spacing: 12) {
                        Button(action: onAccept) {
                            Text("Grant Permission")
                                .fontWeight(.semibold)
                                .frame(maxWidth: .infinity)
                                .padding()
                                .background(Color.adaptivePrimary)
                                .foregroundColor(.white)
                                .cornerRadius(12)
                        }

                        Button(action: {
                            onDeny()
                            dismiss()
                        }) {
                            Text(requestState.rationale.isRequired ? "Not Now" : "Skip")
                                .foregroundColor(.secondary)
                        }
                    }
                }
                .padding()
            }
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: {
                        onDeny()
                        dismiss()
                    }) {
                        Image(systemName: "xmark.circle.fill")
                            .foregroundColor(.secondary)
                    }
                }
            }
        }
    }

    private var permissionIcon: String {
        switch requestState.permissionType {
        case .locationFine, .locationCoarse, .locationBackground:
            return "location.circle.fill"
        case .camera:
            return "camera.fill"
        case .photoLibrary:
            return "photo.fill"
        case .notifications:
            return "bell.fill"
        default:
            return "questionmark.circle.fill"
        }
    }
}

/// Alert view showing permission denied message.
struct PermissionDeniedAlert: View {
    let requestState: PermissionRequestState
    let onRetry: () -> Void
    let onContinue: () -> Void

    var body: some View {
        VStack(spacing: 20) {
            Image(systemName: "exclamationmark.triangle.fill")
                .font(.system(size: 50))
                .foregroundColor(.adaptiveWarning)

            Text("Permission Denied")
                .font(.title2)
                .fontWeight(.bold)

            Text("Without \(requestState.rationale.title.lowercased()), the following features won't work:")
                .font(.body)
                .multilineTextAlignment(.center)
                .foregroundColor(.secondary)

            VStack(alignment: .leading, spacing: 8) {
                ForEach(Array(requestState.rationale.features.prefix(3)), id: \.self) { feature in
                    HStack(alignment: .top, spacing: 8) {
                        Image(systemName: "xmark.circle.fill")
                            .foregroundColor(.adaptiveWarning)
                        Text(feature)
                            .font(.caption)
                            .fixedSize(horizontal: false, vertical: true)
                    }
                }
            }
            .padding()
            .background(Color(.systemGray6))
            .cornerRadius(8)

            if requestState.rationale.isRequired {
                Text("This permission is required for TrailGlass to function properly.")
                    .font(.caption)
                    .fontWeight(.semibold)
                    .foregroundColor(.adaptiveWarning)
                    .multilineTextAlignment(.center)
            }

            VStack(spacing: 12) {
                Button(action: onRetry) {
                    Text("Try Again")
                        .fontWeight(.semibold)
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(Color.adaptivePrimary)
                        .foregroundColor(.white)
                        .cornerRadius(12)
                }

                if !requestState.rationale.isRequired {
                    Button(action: onContinue) {
                        Text("Continue Anyway")
                            .foregroundColor(.secondary)
                    }
                }
            }
        }
        .padding()
    }
}

/// View showing instructions to enable permission in Settings.
struct PermissionSettingsInstructionsView: View {
    let requestState: PermissionRequestState
    let instructions: SettingsInstructions
    let onOpenSettings: () -> Void
    let onDismiss: () -> Void

    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationView {
            ScrollView {
                VStack(alignment: .leading, spacing: 24) {
                    // Header
                    VStack(alignment: .leading, spacing: 8) {
                        Image(systemName: "gear.circle.fill")
                            .font(.system(size: 50))
                            .foregroundColor(.adaptivePrimary)

                        Text("Permission Required")
                            .font(.title)
                            .fontWeight(.bold)

                        Text("You've previously denied \(requestState.rationale.title.lowercased()). To use this feature, you'll need to enable it in Settings.")
                            .font(.body)
                            .foregroundColor(.secondary)
                    }

                    // Instructions
                    VStack(alignment: .leading, spacing: 16) {
                        Text("How to enable:")
                            .font(.headline)

                        VStack(alignment: .leading, spacing: 12) {
                            ForEach(Array(instructions.steps.enumerated()), id: \.offset) { index, step in
                                HStack(alignment: .top, spacing: 16) {
                                    ZStack {
                                        Circle()
                                            .fill(Color.adaptivePrimary)
                                            .frame(width: 28, height: 28)

                                        Text("\(index + 1)")
                                            .font(.caption)
                                            .fontWeight(.bold)
                                            .foregroundColor(.white)
                                    }

                                    Text(step)
                                        .font(.body)
                                        .fixedSize(horizontal: false, vertical: true)
                                }
                            }
                        }
                        .padding()
                        .background(Color(.systemGray6))
                        .cornerRadius(12)
                    }

                    Spacer()

                    // Buttons
                    VStack(spacing: 12) {
                        Button(action: onOpenSettings) {
                            HStack {
                                Image(systemName: "gear")
                                Text("Open Settings")
                            }
                            .fontWeight(.semibold)
                            .frame(maxWidth: .infinity)
                            .padding()
                            .background(Color.adaptivePrimary)
                            .foregroundColor(.white)
                            .cornerRadius(12)
                        }

                        if !requestState.rationale.isRequired {
                            Button(action: {
                                onDismiss()
                                dismiss()
                            }) {
                                Text("Maybe Later")
                                    .foregroundColor(.secondary)
                            }
                        }
                    }
                }
                .padding()
            }
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    if !requestState.rationale.isRequired {
                        Button(action: {
                            onDismiss()
                            dismiss()
                        }) {
                            Image(systemName: "xmark.circle.fill")
                                .foregroundColor(.secondary)
                        }
                    }
                }
            }
        }
    }
}

/// Compact permission banner for inline display.
struct PermissionRequestBanner: View {
    let requestState: PermissionRequestState
    let onGrant: () -> Void
    let onDismiss: () -> Void

    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: permissionIcon)
                .font(.system(size: 24))
                .foregroundColor(.adaptivePrimary)

            VStack(alignment: .leading, spacing: 4) {
                Text(requestState.rationale.title)
                    .font(.subheadline)
                    .fontWeight(.semibold)

                Text(requestState.rationale.description)
                    .font(.caption)
                    .foregroundColor(.secondary)
                    .lineLimit(2)
            }

            Spacer()

            Button(action: onGrant) {
                Text("Grant")
                    .font(.caption)
                    .fontWeight(.semibold)
                    .padding(.horizontal, 12)
                    .padding(.vertical, 6)
                    .background(Color.adaptivePrimary)
                    .foregroundColor(.white)
                    .cornerRadius(8)
            }

            Button(action: onDismiss) {
                Image(systemName: "xmark.circle.fill")
                    .foregroundColor(.secondary)
            }
        }
        .padding()
        .background(Color.adaptivePrimary.opacity(0.1))
        .cornerRadius(12)
        .padding(.horizontal)
    }

    private var permissionIcon: String {
        switch requestState.permissionType {
        case .locationFine, .locationCoarse, .locationBackground:
            return "location.circle.fill"
        case .camera:
            return "camera.fill"
        case .photoLibrary:
            return "photo.fill"
        case .notifications:
            return "bell.fill"
        default:
            return "questionmark.circle.fill"
        }
    }
}

// MARK: - Preview
#if DEBUG
struct PermissionDialogs_Previews: PreviewProvider {
    static var previews: some View {
        let mockRationale = PermissionRationale(
            permissionType: .locationFine,
            title: "Precise Location Access",
            description: "TrailGlass needs precise location access to automatically record your visits.",
            features: [
                "Automatically detect when you arrive at places",
                "Accurately map your routes",
                "Calculate distances and durations"
            ],
            isRequired: true
        )

        let mockState = PermissionRequestState(
            permissionType: .locationFine,
            state: .notDetermined,
            rationale: mockRationale,
            shouldShowRationale: true,
            canRequest: true
        )

        let mockInstructions = SettingsInstructions(
            permissionType: .locationFine,
            steps: [
                "Open Settings app",
                "Find and tap TrailGlass",
                "Tap Location",
                "Select 'While Using the App' or 'Always'"
            ],
            quickAction: "Open Settings"
        )

        Group {
            PermissionRationaleView(
                requestState: mockState,
                onAccept: {},
                onDeny: {}
            )
            .previewDisplayName("Rationale")

            PermissionSettingsInstructionsView(
                requestState: mockState,
                instructions: mockInstructions,
                onOpenSettings: {},
                onDismiss: {}
            )
            .previewDisplayName("Settings Instructions")

            PermissionRequestBanner(
                requestState: mockState,
                onGrant: {},
                onDismiss: {}
            )
            .previewDisplayName("Banner")
        }
    }
}
#endif
