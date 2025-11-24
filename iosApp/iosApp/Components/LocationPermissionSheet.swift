import SwiftUI

/**
 * Location permission request sheet with clear explanation.
 */
struct LocationPermissionSheet: View {
    let onRequestPermission: () -> Void
    let onCancel: () -> Void

    var body: some View {
        VStack(spacing: 24) {
            // Icon
            Image(systemName: "location.fill")
                .font(.system(size: 64))
                .foregroundColor(.blue)
                .padding(.top, 32)

            // Title
            Text("Location Permission Required")
                .font(.title2)
                .fontWeight(.bold)
                .multilineTextAlignment(.center)

            // Description
            VStack(alignment: .leading, spacing: 12) {
                Text("TrailGlass needs location access to:")
                    .font(.headline)

                VStack(alignment: .leading, spacing: 8) {
                    PermissionItem(icon: "map", text: "Show your location on the map")
                    PermissionItem(icon: "location.circle", text: "Center the map on your current location")
                    PermissionItem(icon: "arrow.triangle.turn.up.right.diamond", text: "Provide location-based features")
                }
            }
            .padding(.horizontal)

            Spacer()

            // Buttons
            VStack(spacing: 12) {
                Button(action: onRequestPermission) {
                    Text("Allow Location Access")
                        .font(.headline)
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(Color.blue)
                        .cornerRadius(12)
                }

                Button(action: onCancel) {
                    Text("Not Now")
                        .font(.body)
                        .foregroundColor(.secondary)
                }
            }
            .padding(.horizontal, 24)
            .padding(.bottom, 32)
        }
        .padding()
    }
}

private struct PermissionItem: View {
    let icon: String
    let text: String

    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: icon)
                .font(.body)
                .foregroundColor(.blue)
                .frame(width: 24)

            Text(text)
                .font(.body)
                .foregroundColor(.primary)
        }
    }
}

