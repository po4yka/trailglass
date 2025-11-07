import SwiftUI
import MapKit
import shared

/// Main map screen matching Android MapScreen functionality
struct MapScreen: View {
    @StateObject private var viewModel: MapViewModel
    @State private var showLocationPermissionDialog = false

    init(mapController: MapController) {
        _viewModel = StateObject(wrappedValue: MapViewModel(controller: mapController))
    }

    var body: some View {
        NavigationView {
            ZStack {
                // Main map view
                MapView(viewModel: viewModel)

                // Loading indicator
                if viewModel.isLoading {
                    ProgressView()
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                        .background(Color.black.opacity(0.2))
                }

                // Error message
                if let error = viewModel.error {
                    VStack {
                        Spacer()
                        ErrorBanner(message: error) {
                            viewModel.clearError()
                        }
                        .padding()
                    }
                }

                // Location permission prompt
                if viewModel.needsLocationPermission {
                    VStack {
                        Spacer()
                        LocationPermissionPrompt {
                            showLocationPermissionDialog = true
                        }
                        .padding()
                    }
                }
            }
            .navigationTitle("Map")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Menu {
                        Button(action: { viewModel.centerOnUserLocation() }) {
                            Label("Center on Location", systemImage: "location.fill")
                        }

                        Button(action: { viewModel.fitAllMarkers() }) {
                            Label("Fit All Markers", systemImage: "rectangle.expand.vertical")
                        }

                        Divider()

                        Button(action: { viewModel.refreshData() }) {
                            Label("Refresh", systemImage: "arrow.clockwise")
                        }
                    } label: {
                        Image(systemName: "ellipsis.circle")
                    }
                }
            }
            .sheet(isPresented: $showLocationPermissionDialog) {
                PermissionDialogs.locationPermissionSheet(
                    onRequestPermission: {
                        viewModel.requestLocationPermission()
                        showLocationPermissionDialog = false
                    },
                    onCancel: {
                        showLocationPermissionDialog = false
                    }
                )
            }
        }
    }
}

/// Map view component
struct MapView: View {
    @ObservedObject var viewModel: MapViewModel

    @State private var region = MKCoordinateRegion(
        center: CLLocationCoordinate2D(latitude: 0, longitude: 0),
        span: MKCoordinateSpan(latitudeDelta: 180, longitudeDelta: 180)
    )

    var body: some View {
        Map(coordinateRegion: $region, showsUserLocation: true, annotationItems: viewModel.markers) { marker in
            MapAnnotation(coordinate: CLLocationCoordinate2D(
                latitude: marker.latitude,
                longitude: marker.longitude
            )) {
                MarkerView(marker: marker, isSelected: marker.id == viewModel.selectedMarkerId) {
                    viewModel.selectMarker(marker)
                }
            }
        }
        .onAppear {
            if let initialRegion = viewModel.getInitialRegion() {
                region = initialRegion
            }
        }
        .onChange(of: viewModel.regionUpdate) { newRegion in
            if let newRegion = newRegion {
                region = newRegion
            }
        }
    }
}

/// Simple marker view
struct MarkerView: View {
    let marker: MapMarker
    let isSelected: Bool
    let onTap: () -> Void

    var body: some View {
        VStack(spacing: 0) {
            ZStack {
                Circle()
                    .fill(Color.blue)
                    .frame(width: isSelected ? 44 : 32, height: isSelected ? 44 : 32)
                    .shadow(radius: isSelected ? 4 : 2)

                Image(systemName: "mappin.circle.fill")
                    .foregroundColor(.white)
                    .font(.system(size: isSelected ? 20 : 14))
            }

            if isSelected, let title = marker.title {
                Text(title)
                    .font(.caption)
                    .padding(4)
                    .background(Color.white)
                    .cornerRadius(4)
                    .shadow(radius: 2)
                    .offset(y: 4)
            }
        }
        .onTapGesture {
            onTap()
        }
    }
}

/// Error banner
struct ErrorBanner: View {
    let message: String
    let onDismiss: () -> Void

    var body: some View {
        HStack {
            Image(systemName: "exclamationmark.triangle.fill")
                .foregroundColor(.white)

            Text(message)
                .foregroundColor(.white)
                .font(.body)

            Spacer()

            Button(action: onDismiss) {
                Image(systemName: "xmark.circle.fill")
                    .foregroundColor(.white)
            }
        }
        .padding()
        .background(Color.red)
        .cornerRadius(8)
    }
}

/// Location permission prompt
struct LocationPermissionPrompt: View {
    let onRequestPermission: () -> Void

    var body: some View {
        VStack(spacing: 12) {
            Image(systemName: "location.slash.fill")
                .font(.largeTitle)
                .foregroundColor(.orange)

            Text("Location Permission Required")
                .font(.headline)

            Text("TrailGlass needs access to your location to show you on the map and track your trips.")
                .font(.body)
                .multilineTextAlignment(.center)
                .foregroundColor(.secondary)

            Button(action: onRequestPermission) {
                Text("Enable Location")
                    .fontWeight(.semibold)
                    .frame(maxWidth: .infinity)
            }
            .buttonStyle(.borderedProminent)
        }
        .padding()
        .background(Color(UIColor.systemBackground))
        .cornerRadius(12)
        .shadow(radius: 4)
    }
}

/// Map view model
class MapViewModel: ObservableObject {
    private let controller: MapController
    private var stateObserver: Kotlinx_coroutines_coreJob?

    @Published var markers: [MapMarker] = []
    @Published var selectedMarkerId: String?
    @Published var regionUpdate: MKCoordinateRegion?
    @Published var isLoading: Bool = false
    @Published var error: String?
    @Published var needsLocationPermission: Bool = false

    init(controller: MapController) {
        self.controller = controller
        observeState()
    }

    deinit {
        stateObserver?.cancel(cause: nil)
    }

    private func observeState() {
        stateObserver = controller.state.subscribe { [weak self] state in
            guard let self = self, let state = state else { return }

            DispatchQueue.main.async {
                self.isLoading = state.isLoading
                self.error = state.error?.userMessage
                self.markers = state.mapData.markers.map { MapMarker(from: $0) }
                self.selectedMarkerId = state.selectedMarker?.id
                self.needsLocationPermission = !state.hasLocationPermission

                // Update region if changed
                if let region = state.mapData.region {
                    self.regionUpdate = MKCoordinateRegion(
                        center: CLLocationCoordinate2D(
                            latitude: region.center.latitude,
                            longitude: region.center.longitude
                        ),
                        span: MKCoordinateSpan(
                            latitudeDelta: region.latitudeDelta,
                            longitudeDelta: region.longitudeDelta
                        )
                    )
                }
            }
        }
    }

    func getInitialRegion() -> MKCoordinateRegion? {
        guard let region = controller.state.value?.mapData.region else { return nil }

        return MKCoordinateRegion(
            center: CLLocationCoordinate2D(
                latitude: region.center.latitude,
                longitude: region.center.longitude
            ),
            span: MKCoordinateSpan(
                latitudeDelta: region.latitudeDelta,
                longitudeDelta: region.longitudeDelta
            )
        )
    }

    func selectMarker(_ marker: MapMarker) {
        controller.selectMarker(markerId: marker.id)
    }

    func centerOnUserLocation() {
        controller.centerOnUserLocation()
    }

    func fitAllMarkers() {
        controller.fitAllMarkers()
    }

    func refreshData() {
        controller.refresh()
    }

    func clearError() {
        controller.clearError()
    }

    func requestLocationPermission() {
        // This would be handled by the permission system
        // For now, just trigger a refresh to re-check permissions
        controller.refresh()
    }
}

/// Map marker model for SwiftUI
struct MapMarker: Identifiable {
    let id: String
    let latitude: Double
    let longitude: Double
    let title: String?
    let snippet: String?

    init(from marker: com.po4yka.trailglass.feature.map.MapMarker) {
        self.id = marker.id
        self.latitude = marker.coordinate.latitude
        self.longitude = marker.coordinate.longitude
        self.title = marker.title
        self.snippet = marker.snippet
    }
}

#Preview {
    Text("MapScreen Preview - Requires DI setup")
}
