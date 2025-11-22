import SwiftUI
import shared

/// Banner showing network connectivity status
/// Appears at top of screen when offline or network is limited
struct NetworkStatusBanner: View {
    let networkState: NetworkState
    let networkType: NetworkType
    let isMetered: Bool

    var body: some View {
        if shouldShow {
            HStack(spacing: 12) {
                Image(systemName: iconName)
                    .font(.system(size: 20))
                    .foregroundColor(contentColor)

                VStack(alignment: .leading, spacing: 4) {
                    Text(statusText)
                        .font(.body)
                        .foregroundColor(contentColor)

                    if isMetered && networkType != .none {
                        Text("Using \(networkType.name.lowercased()) (metered)")
                            .font(.caption)
                            .foregroundColor(contentColor.opacity(0.7))
                    }
                }

                Spacer()
            }
            .padding()
            .background(backgroundColor)
            .transition(.move(edge: .top).combined(with: .opacity))
        }
    }

    private var shouldShow: Bool {
        !(networkState is NetworkState.Connected)
    }

    private var iconName: String {
        switch networkState {
        case is NetworkState.Disconnected:
            return "icloud.slash"
        case is NetworkState.Limited:
            return "exclamationmark.triangle"
        case is NetworkState.Connected:
            return "checkmark.icloud"
        default:
            return "icloud.slash"
        }
    }

    private var statusText: String {
        switch networkState {
        case is NetworkState.Disconnected:
            return "No internet connection"
        case let limited as NetworkState.Limited:
            return "Limited connectivity: \(limited.reason)"
        case is NetworkState.Connected:
            return "Connected"
        default:
            return "Unknown status"
        }
    }

    private var backgroundColor: Color {
        switch networkState {
        case is NetworkState.Disconnected:
            return Color.adaptiveWarning.opacity(0.2)
        case is NetworkState.Limited:
            return Color.adaptiveWarning.opacity(0.15)
        case is NetworkState.Connected:
            return Color.adaptiveSuccess.opacity(0.2)
        default:
            return Color.adaptiveDisabled.opacity(0.2)
        }
    }

    private var contentColor: Color {
        switch networkState {
        case is NetworkState.Disconnected:
            return .adaptiveWarning
        case is NetworkState.Limited:
            return .warning
        case is NetworkState.Connected:
            return .adaptiveSuccess
        default:
            return .adaptiveDisabled
        }
    }
}

/// Compact network status indicator for toolbar or tab bar
struct NetworkStatusIndicatorCompact: View {
    let networkState: NetworkState
    let networkType: NetworkType

    var body: some View {
        Image(systemName: iconName)
            .font(.system(size: 16))
            .foregroundColor(iconColor)
    }

    private var iconName: String {
        switch networkState {
        case is NetworkState.Connected:
            return networkTypeIcon
        case is NetworkState.Disconnected:
            return "icloud.slash"
        case is NetworkState.Limited:
            return "exclamationmark.triangle"
        default:
            return "icloud.slash"
        }
    }

    private var networkTypeIcon: String {
        switch networkType {
        case .wifi:
            return "wifi"
        case .cellular:
            return "antenna.radiowaves.left.and.right"
        case .ethernet:
            return "cable.connector"
        case .none:
            return "icloud"
        default:
            return "icloud"
        }
    }

    private var iconColor: Color {
        switch networkState {
        case is NetworkState.Connected:
            return .adaptiveSuccess
        case is NetworkState.Disconnected:
            return .adaptiveWarning
        case is NetworkState.Limited:
            return .warning
        default:
            return .adaptiveDisabled
        }
    }
}

// MARK: - Preview

struct NetworkStatusBanner_Previews: PreviewProvider {
    static var previews: some View {
        VStack(spacing: 20) {
            NetworkStatusBanner(
                networkState: NetworkState.Disconnected(),
                networkType: .none,
                isMetered: false
            )

            NetworkStatusBanner(
                networkState: NetworkState.Limited(reason: "No internet access"),
                networkType: .cellular,
                isMetered: true
            )

            HStack {
                NetworkStatusIndicatorCompact(
                    networkState: NetworkState.Connected(),
                    networkType: .wifi
                )

                NetworkStatusIndicatorCompact(
                    networkState: NetworkState.Disconnected(),
                    networkType: .none
                )

                NetworkStatusIndicatorCompact(
                    networkState: NetworkState.Connected(),
                    networkType: .cellular
                )
            }
            .padding()
        }
    }
}
