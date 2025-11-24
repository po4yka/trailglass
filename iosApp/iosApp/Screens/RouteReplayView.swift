import SwiftUI
import Shared
import MapKit

/// Route Replay screen - full-screen animated playback of trip route
struct RouteReplayView: View {
    let tripId: String
    @StateObject private var viewModel: RouteReplayViewModel
    @Environment(\.dismiss) private var dismiss

    init(tripId: String, controller: RouteReplayController) {
        self.tripId = tripId
        _viewModel = StateObject(wrappedValue: RouteReplayViewModel(
            tripId: tripId,
            controller: controller
        ))
    }

    var body: some View {
        ZStack {
            // Map background
            Color.black.ignoresSafeArea()

            switch viewModel.currentState {
            case .loading:
                loadingView
            case .error(let message):
                errorView(message: message)
            case .ready:
                replayContent
            }

            // Close button (always visible)
            VStack {
                HStack {
                    Button(action: { dismiss() }) {
                        Image(systemName: "xmark")
                            .font(.system(size: 18, weight: .semibold))
                            .foregroundColor(.white)
                            .frame(width: 36, height: 36)
                            .background(Color.black.opacity(0.6))
                            .clipShape(Circle())
                    }
                    .padding()

                    Spacer()
                }

                Spacer()
            }
        }
        .onAppear {
            viewModel.loadRoute()
        }
        .onDisappear {
            viewModel.cleanup()
        }
    }

    private var loadingView: some View {
        VStack(spacing: 16) {
            ProgressView()
                .tint(.white)
                .scaleEffect(1.5)

            Text("Loading replay...")
                .font(.body)
                .foregroundColor(.white)
        }
    }

    private func errorView(message: String) -> some View {
        VStack(spacing: 24) {
            Image(systemName: "exclamationmark.triangle.fill")
                .font(.system(size: 64))
                .foregroundColor(.red)

            Text(message)
                .font(.body)
                .foregroundColor(.white)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 32)

            HStack(spacing: 12) {
                Button("Close") {
                    dismiss()
                }
                .buttonStyle(.bordered)
                .tint(.white)

                Button("Retry") {
                    viewModel.loadRoute()
                }
                .buttonStyle(.borderedProminent)
            }
        }
    }

    private var replayContent: some View {
        ZStack {
            // Map view
            if let tripRoute = viewModel.tripRoute,
               let vehicleState = viewModel.vehicleState,
               let cameraPosition = viewModel.cameraPosition {
                RouteReplayMapView(
                    tripRoute: tripRoute,
                    vehicleState: vehicleState,
                    cameraPosition: cameraPosition,
                    cameraBearing: viewModel.cameraBearing,
                    cameraZoom: viewModel.cameraZoom
                )
                .ignoresSafeArea()
            }

            // Control panel
            if viewModel.showControls {
                VStack {
                    Spacer()

                    ReplayControlPanel(
                        isPlaying: viewModel.isPlaying,
                        progress: viewModel.progress,
                        playbackSpeed: viewModel.playbackSpeed,
                        onPlayPauseClick: { viewModel.togglePlayPause() },
                        onProgressChange: { viewModel.seekTo(progress: $0) },
                        onSpeedClick: { viewModel.cyclePlaybackSpeed() },
                        onRestartClick: { viewModel.restart() }
                    )
                    .padding()
                    .padding(.bottom, 8)
                }
            }
        }
        .contentShape(Rectangle())
        .onTapGesture {
            viewModel.toggleControls()
        }
    }
}

/// Replay control panel with play/pause, progress slider, and speed control
struct ReplayControlPanel: View {
    let isPlaying: Bool
    let progress: Float
    let playbackSpeed: PlaybackSpeed
    let onPlayPauseClick: () -> Void
    let onProgressChange: (Float) -> Void
    let onSpeedClick: () -> Void
    let onRestartClick: () -> Void

    var body: some View {
        VStack(spacing: 12) {
            // Progress slider
            HStack(spacing: 12) {
                Text(formatProgress(progress))
                    .font(.caption)
                    .foregroundColor(.white)
                    .frame(width: 40)

                Slider(
                    value: Binding(
                        get: { Double(progress) },
                        set: { onProgressChange(Float($0)) }
                    ),
                    in: 0...1
                )
                .tint(.blue)

                Text("100%")
                    .font(.caption)
                    .foregroundColor(.white)
                    .frame(width: 40)
            }

            // Control buttons
            HStack(spacing: 32) {
                // Restart button
                Button(action: onRestartClick) {
                    Image(systemName: "arrow.counterclockwise")
                        .font(.system(size: 24))
                        .foregroundColor(.blue)
                }

                // Play/Pause button (larger)
                Button(action: onPlayPauseClick) {
                    Image(systemName: isPlaying ? "pause.fill" : "play.fill")
                        .font(.system(size: 32))
                        .foregroundColor(.white)
                        .frame(width: 56, height: 56)
                        .background(Color.blue)
                        .clipShape(Circle())
                }

                // Speed control button
                Button(action: onSpeedClick) {
                    Text(playbackSpeed.displayName)
                        .font(.system(size: 16, weight: .semibold))
                        .foregroundColor(.blue)
                        .frame(width: 64, height: 36)
                        .background(Color.white.opacity(0.2))
                        .cornerRadius(8)
                        .overlay(
                            RoundedRectangle(cornerRadius: 8)
                                .stroke(Color.blue, lineWidth: 1)
                        )
                }
            }
        }
        .padding()
        .background(
            Color.black.opacity(0.8)
                .cornerRadius(16)
        )
    }

    private func formatProgress(_ progress: Float) -> String {
        return "\(Int(progress * 100))%"
    }
}

/// Map view for route replay with animated vehicle
struct RouteReplayMapView: View {
    let tripRoute: TripRoute
    let vehicleState: VehicleState
    let cameraPosition: Coordinate
    let cameraBearing: Double
    let cameraZoom: Float

    @State private var region = MKCoordinateRegion(
        center: CLLocationCoordinate2D(latitude: 0, longitude: 0),
        span: MKCoordinateSpan(latitudeDelta: 0.01, longitudeDelta: 0.01)
    )

    var body: some View {
        Map(coordinateRegion: .constant(region))
        .onAppear {
        }
        .onAppear {
            updateRegion()
        }
        .onChange(of: cameraPosition) { _ in
            updateRegion()
        }
    }

    private var vehicleMarker: VehicleMapMarker {
        VehicleMapMarker(
            coordinate: CLLocationCoordinate2D(
                latitude: vehicleState.position.latitude,
                longitude: vehicleState.position.longitude
            )
        )
    }

    private func updateRegion() {
        // Calculate zoom level from cameraZoom (16 is typical street level)
        let zoomFactor = pow(2.0, Double(16 - cameraZoom))
        let span = MKCoordinateSpan(
            latitudeDelta: 0.01 * zoomFactor,
            longitudeDelta: 0.01 * zoomFactor
        )

        region = MKCoordinateRegion(
            center: CLLocationCoordinate2D(
                latitude: cameraPosition.latitude,
                longitude: cameraPosition.longitude
            ),
            span: span
        )
    }
}

/// Vehicle marker for map
struct VehicleMapMarker: Identifiable {
    let id = UUID()
    let coordinate: CLLocationCoordinate2D
}

/// Animated vehicle marker with bearing
struct VehicleMarker: View {
    let bearing: Double

    var body: some View {
        ZStack {
            // Drop shadow
            Circle()
                .fill(Color.black.opacity(0.3))
                .frame(width: 24, height: 24)
                .blur(radius: 4)
                .offset(y: 2)

            // Vehicle icon
            Image(systemName: "location.fill")
                .font(.system(size: 24))
                .foregroundColor(.blue)
                .rotationEffect(.degrees(bearing))
        }
    }
}

/// ViewModel for RouteReplayView
class RouteReplayViewModel: ObservableObject {
    private let tripId: String
    private let controller: RouteReplayController
    private var stateObserver: KotlinJob?

    @Published var currentState: ViewState = .loading
    @Published var isPlaying = false
    @Published var progress: Float = 0.0
    @Published var playbackSpeed: PlaybackSpeed = .normal
    @Published var showControls = true

    // Route data
    @Published var tripRoute: TripRoute?
    @Published var vehicleState: VehicleState?
    @Published var cameraPosition: Coordinate?
    @Published var cameraBearing: Double = 0.0
    @Published var cameraZoom: Float = 16.0

    enum ViewState {
        case loading
        case error(String)
        case ready
    }

    init(tripId: String, controller: RouteReplayController) {
        self.tripId = tripId
        self.controller = controller
    }

    func loadRoute() {
        controller.loadRoute(tripId: tripId)

        // Observe state changes
        stateObserver = controller.state.subscribe { [weak self] (state: RouteReplayState?) in
            guard let self = self, let state = state else { return }

            DispatchQueue.main.async {
                self.isPlaying = state.isPlaying
                self.progress = state.progress
                self.playbackSpeed = state.playbackSpeed
                self.showControls = state.showControls
                self.tripRoute = state.tripRoute
                self.vehicleState = state.vehicleState
                self.cameraPosition = state.cameraPosition
                self.cameraBearing = state.cameraBearing
                self.cameraZoom = state.cameraZoom

                // Update view state
                if state.isLoading {
                    self.currentState = .loading
                } else if let error = state.error {
                    self.currentState = .error(error)
                } else if state.tripRoute != nil && state.vehicleState != nil {
                    self.currentState = .ready
                }
            }
        }
    }

    func togglePlayPause() {
        controller.togglePlayPause()
    }

    func seekTo(progress: Float) {
        controller.seekTo(progress: progress)
    }

    func cyclePlaybackSpeed() {
        controller.cyclePlaybackSpeed()
    }

    func restart() {
        controller.restart()
    }

    func toggleControls() {
        controller.toggleControls()
    }

    func cleanup() {
        controller.stop()
        stateObserver?.cancel(cause: nil)
    }

    deinit {
        stateObserver?.cancel(cause: nil)
    }
}

#Preview {
    Text("RouteReplayView Preview - Requires DI setup")
}
