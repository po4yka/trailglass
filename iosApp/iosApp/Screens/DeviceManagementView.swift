import SwiftUI
import shared

/// Device Management screen showing list of user's devices
struct DeviceManagementView: View {
    @StateObject private var viewModel: DeviceManagementViewModel
    @Environment(\.dismiss) private var dismiss

    init(controller: DeviceManagementController) {
        _viewModel = StateObject(wrappedValue: DeviceManagementViewModel(controller: controller))
    }

    var body: some View {
        NavigationView {
            ZStack {
                switch viewModel.currentState {
                case .loading:
                    loadingView
                case .error(let message):
                    errorView(message: message)
                case .empty:
                    emptyView
                case .loaded:
                    deviceList
                }

                // Error snackbar (when devices are loaded but operation failed)
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
                        .background(Color.adaptiveWarning)
                        .cornerRadius(8)
                        .padding()
                    }
                }
            }
            .navigationTitle("Device Management")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button(action: { dismiss() }) {
                        Image(systemName: "chevron.left")
                    }
                }

                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: { viewModel.refresh() }) {
                        Image(systemName: "arrow.clockwise")
                    }
                }
            }
            .onAppear {
                viewModel.loadDevices()
            }
            .alert("Delete Device?", isPresented: $viewModel.showDeleteConfirmation) {
                Button("Cancel", role: .cancel) { }
                Button("Delete", role: .destructive) {
                    viewModel.confirmDelete()
                }
            } message: {
                if let device = viewModel.deviceToDelete {
                    Text("Are you sure you want to remove \"\(device.deviceName)\" from your devices? This will revoke access for this device.")
                }
            }
        }
    }

    private var loadingView: some View {
        VStack(spacing: 16) {
            ProgressView()
                .scaleEffect(1.5)

            Text("Loading devices...")
                .font(.body)
                .foregroundColor(.secondary)
        }
    }

    private func errorView(message: String) -> some View {
        VStack(spacing: 24) {
            Image(systemName: "exclamationmark.triangle.fill")
                .font(.system(size: 64))
                .foregroundColor(.adaptiveWarning)

            Text(message)
                .font(.body)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 32)

            Button("Retry") {
                viewModel.loadDevices()
            }
            .buttonStyle(.borderedProminent)
        }
    }

    private var emptyView: some View {
        VStack(spacing: 24) {
            Image(systemName: "externaldrive.badge.xmark")
                .font(.system(size: 64))
                .foregroundColor(.adaptivePrimary)

            Text("No devices found")
                .font(.title2)
                .fontWeight(.medium)

            Text("Your devices will appear here once they sync")
                .font(.body)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)
        }
        .padding(.horizontal, 32)
    }

    private var deviceList: some View {
        ScrollView {
            LazyVStack(spacing: 12) {
                ForEach(viewModel.devices, id: \.deviceId) { device in
                    DeviceCard(
                        device: device,
                        isDeleting: viewModel.deletingDeviceId == device.deviceId,
                        onDelete: {
                            viewModel.requestDelete(device: device)
                        }
                    )
                }
            }
            .padding()
        }
    }
}

/// Card displaying device information
struct DeviceCard: View {
    let device: DeviceDto
    let isDeleting: Bool
    let onDelete: () -> Void

    var body: some View {
        HStack(spacing: 16) {
            // Device icon
            Image(systemName: platformIcon)
                .font(.system(size: 32))
                .foregroundColor(device.isActive ? .blue : .gray)
                .frame(width: 40, height: 40)

            // Device info
            VStack(alignment: .leading, spacing: 4) {
                HStack(spacing: 8) {
                    Text(device.deviceName)
                        .font(.headline)

                    if device.isActive {
                        Text("This Device")
                            .font(.caption)
                            .fontWeight(.semibold)
                            .padding(.horizontal, 6)
                            .padding(.vertical, 2)
                            .background(Color.blue.opacity(0.2))
                            .foregroundColor(.blue)
                            .cornerRadius(4)
                    }
                }

                Text("\(device.platform) \(device.osVersion)")
                    .font(.subheadline)
                    .foregroundColor(.secondary)

                Text("App v\(device.appVersion)")
                    .font(.caption)
                    .foregroundColor(.secondary)

                Text("Last sync: \(formatTimestamp(device.lastSyncAt))")
                    .font(.caption)
                    .foregroundColor(.secondary)
            }

            Spacer()

            // Delete button
            if !device.isActive {
                if isDeleting {
                    ProgressView()
                        .scaleEffect(0.8)
                } else {
                    Button(action: onDelete) {
                        Image(systemName: "trash")
                            .foregroundColor(.red)
                    }
                }
            }
        }
        .padding()
        .background(Color(.systemGray6))
        .cornerRadius(12)
    }

    private var platformIcon: String {
        switch device.platform.lowercased() {
        case "android":
            return "app.badge.checkmark"
        case "ios":
            return "iphone"
        default:
            return "desktopcomputer"
        }
    }

    private func formatTimestamp(_ timestamp: String) -> String {
        let formatter = DateFormatter()
        formatter.dateStyle = .short
        formatter.timeStyle = .short

        // Try to parse ISO 8601 timestamp
        let isoFormatter = ISO8601DateFormatter()
        if let date = isoFormatter.date(from: timestamp) {
            return formatter.string(from: date)
        }

        return timestamp
    }
}

/// ViewModel for DeviceManagementView
class DeviceManagementViewModel: ObservableObject {
    private let controller: DeviceManagementController
    private var stateObserver: Kotlinx_coroutines_coreJob?

    @Published var currentState: ViewState = .loading
    @Published var devices: [DeviceDto] = []
    @Published var errorMessage: String?
    @Published var showError = false
    @Published var deletingDeviceId: String?
    @Published var showDeleteConfirmation = false
    @Published var deviceToDelete: DeviceDto?

    enum ViewState {
        case loading
        case error(String)
        case empty
        case loaded
    }

    init(controller: DeviceManagementController) {
        self.controller = controller
    }

    func loadDevices() {
        controller.loadDevices()

        // Observe state changes
        stateObserver = controller.state.subscribe { [weak self] state in
            guard let self = self, let state = state else { return }

            DispatchQueue.main.async {
                self.devices = state.devices
                self.deletingDeviceId = state.deletingDeviceId
                self.errorMessage = state.error

                // Update view state
                if state.isLoading && state.devices.isEmpty {
                    self.currentState = .loading
                } else if let error = state.error, state.devices.isEmpty {
                    self.currentState = .error(error)
                    self.showError = false
                } else if state.devices.isEmpty {
                    self.currentState = .empty
                } else {
                    self.currentState = .loaded
                    // Show error snackbar if there's an error but devices are loaded
                    self.showError = state.error != nil
                }
            }
        }
    }

    func refresh() {
        controller.refresh()
    }

    func requestDelete(device: DeviceDto) {
        deviceToDelete = device
        showDeleteConfirmation = true
    }

    func confirmDelete() {
        guard let device = deviceToDelete else { return }

        controller.deleteDevice(deviceId: device.deviceId) { [weak self] in
            DispatchQueue.main.async {
                self?.deviceToDelete = nil
            }
        }
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
    Text("DeviceManagementView Preview - Requires DI setup")
}
