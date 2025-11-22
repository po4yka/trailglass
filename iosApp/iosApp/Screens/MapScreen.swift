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
                // Main map view with routes and markers
                EnhancedMapView(viewModel: viewModel)

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
        .onAppear {
            viewModel.refreshData()
        }
    }
}

/// Enhanced map view with routes and markers using MKMapView
struct EnhancedMapView: UIViewRepresentable {
    @ObservedObject var viewModel: MapViewModel

    func makeUIView(context: Context) -> MKMapView {
        let mapView = MKMapView()
        mapView.delegate = context.coordinator
        mapView.showsUserLocation = true
        mapView.showsCompass = true
        mapView.showsScale = true
        return mapView
    }

    func updateUIView(_ mapView: MKMapView, context: Context) {
        // Update region
        if let region = viewModel.regionUpdate {
            mapView.setRegion(region, animated: true)
            viewModel.clearRegionUpdate()
        }

        // Update markers (annotations)
        updateAnnotations(mapView)

        // Update routes (overlays)
        updateOverlays(mapView)
    }

    private func updateAnnotations(_ mapView: MKMapView) {
        let existingMarkers = mapView.annotations.compactMap { $0 as? MarkerAnnotation }
        let newMarkerIds = Set(viewModel.markers.map { $0.id })
        let existingMarkerIds = Set(existingMarkers.map { $0.id })

        // Remove markers that no longer exist
        let toRemove = existingMarkers.filter { !newMarkerIds.contains($0.id) }
        mapView.removeAnnotations(toRemove)

        // Add new markers
        let toAdd = viewModel.markers.filter { !existingMarkerIds.contains($0.id) }
        let newAnnotations = toAdd.map { MarkerAnnotation(marker: $0) }
        mapView.addAnnotations(newAnnotations)
    }

    private func updateOverlays(_ mapView: MKMapView) {
        let existingRoutes = mapView.overlays.compactMap { $0 as? RoutePolyline }
        let newRouteIds = Set(viewModel.routes.map { $0.id })
        let existingRouteIds = Set(existingRoutes.map { $0.id })

        // Remove routes that no longer exist
        let routesToRemove = existingRoutes.filter { !newRouteIds.contains($0.id) }
        mapView.removeOverlays(routesToRemove)

        // Add new routes
        let routesToAdd = viewModel.routes.filter { !existingRouteIds.contains($0.id) }
        for route in routesToAdd {
            let coordinates = route.coordinates.map {
                CLLocationCoordinate2D(latitude: $0.latitude, longitude: $0.longitude)
            }
            let polyline = RoutePolyline(coordinates: coordinates, count: coordinates.count)
            polyline.id = route.id
            polyline.transportType = route.transportType.name
            polyline.colorInt = route.color?.int32Value
            mapView.addOverlay(polyline)
        }
    }

    func makeCoordinator() -> Coordinator {
        Coordinator(self)
    }

    class Coordinator: NSObject, MKMapViewDelegate {
        var parent: EnhancedMapView

        init(_ parent: EnhancedMapView) {
            self.parent = parent
        }

        func mapView(_ mapView: MKMapView, viewFor annotation: MKAnnotation) -> MKAnnotationView? {
            guard let markerAnnotation = annotation as? MarkerAnnotation else {
                return nil
            }

            let identifier = "MarkerAnnotation"
            var annotationView = mapView.dequeueReusableAnnotationView(withIdentifier: identifier) as? MKMarkerAnnotationView

            if annotationView == nil {
                annotationView = MKMarkerAnnotationView(annotation: annotation, reuseIdentifier: identifier)
                annotationView?.canShowCallout = true
            } else {
                annotationView?.annotation = annotation
            }

            annotationView?.markerTintColor = UIColor(Color.adaptivePrimary)
            annotationView?.glyphImage = UIImage(systemName: "mappin.circle.fill")

            return annotationView
        }

        func mapView(_ mapView: MKMapView, rendererFor overlay: MKOverlay) -> MKOverlayRenderer {
            guard let polyline = overlay as? RoutePolyline else {
                return MKOverlayRenderer(overlay: overlay)
            }

            let renderer = MKPolylineRenderer(polyline: polyline)
            renderer.strokeColor = colorForPolyline(polyline)
            renderer.lineWidth = 4.0
            renderer.lineCap = .round
            renderer.lineJoin = .round
            return renderer
        }

        func mapView(_ mapView: MKMapView, didSelect view: MKAnnotationView) {
            if let markerAnnotation = view.annotation as? MarkerAnnotation {
                parent.viewModel.selectMarker(markerAnnotation.marker)
            }
        }

        private func colorForPolyline(_ polyline: RoutePolyline) -> UIColor {
            if let colorInt = polyline.colorInt {
                // Convert Android color int (ARGB) to UIColor
                let alpha = CGFloat((colorInt >> 24) & 0xFF) / 255.0
                let red = CGFloat((colorInt >> 16) & 0xFF) / 255.0
                let green = CGFloat((colorInt >> 8) & 0xFF) / 255.0
                let blue = CGFloat(colorInt & 0xFF) / 255.0
                return UIColor(red: red, green: green, blue: blue, alpha: alpha)
            }

            // Use historical route color (Harbor Blue) from Silent Waters palette as default
            // Active routes should be set via route.color parameter from controller
            return UIColor(Color.historicalRoute)
        }
    }
}

/// Custom marker annotation
class MarkerAnnotation: NSObject, MKAnnotation {
    let id: String
    let coordinate: CLLocationCoordinate2D
    let title: String?
    let subtitle: String?
    let marker: shared.MapMarker

    init(marker: shared.MapMarker) {
        self.id = marker.id
        self.coordinate = CLLocationCoordinate2D(
            latitude: marker.coordinate.latitude,
            longitude: marker.coordinate.longitude
        )
        self.title = marker.title
        self.subtitle = marker.snippet
        self.marker = marker
        super.init()
    }
}

/// Custom polyline for routes
class RoutePolyline: MKPolyline {
    var id: String?
    var transportType: String?
    var colorInt: Int32?
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
        .background(Color.adaptiveWarning)
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
                .foregroundColor(.adaptiveWarning)

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

    @Published var markers: [shared.MapMarker] = []
    @Published var routes: [shared.MapRoute] = []
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
                self.markers = state.mapData.markers
                self.routes = state.mapData.routes
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

    func selectMarker(_ marker: shared.MapMarker) {
        controller.selectMarker(markerId: marker.id)
    }

    func clearRegionUpdate() {
        regionUpdate = nil
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

#Preview {
    Text("MapScreen Preview - Requires DI setup")
}
