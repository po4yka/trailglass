import SwiftUI
import MapKit
import Shared

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
        updateOverlays(mapView, context: context)
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

    private func updateOverlays(_ mapView: MKMapView, context: Context) {
        let existingRoutes = mapView.overlays.compactMap { $0 as? RoutePolyline }
        let newRouteIds = Set(viewModel.routes.map { $0.id })
        let existingRouteIds = Set(existingRoutes.map { $0.id })

        // Check if selection changed to trigger re-rendering
        let selectionChanged = context.coordinator.lastSelectedRouteId != viewModel.selectedRouteId
        if selectionChanged {
            context.coordinator.lastSelectedRouteId = viewModel.selectedRouteId
            // Force re-render of all overlays by removing and re-adding them
            if !existingRoutes.isEmpty {
                mapView.removeOverlays(existingRoutes)
                for route in viewModel.routes {
                    let coordinates = route.coordinates.map {
                        CLLocationCoordinate2D(latitude: $0.latitude, longitude: $0.longitude)
                    }
                    let polyline = RoutePolyline(coordinates: coordinates, count: coordinates.count)
                    polyline.id = route.id
                    polyline.transportType = route.transportType.name
                    polyline.colorInt = route.color?.int32Value
                    mapView.addOverlay(polyline)
                }
                return
            }
        }

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
        var lastSelectedRouteId: String?

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

            // Determine if selected
            let isSelected = isRouteSelected(polyline)

            // Set color with Silent Waters palette
            renderer.strokeColor = colorForPolyline(polyline, isSelected: isSelected)

            // Set width based on transport type and selection
            let baseWidth = lineWidthForTransportType(polyline.transportType ?? "UNKNOWN")
            renderer.lineWidth = isSelected ? baseWidth * 1.5 : baseWidth

            // Line styling
            renderer.lineCap = .round
            renderer.lineJoin = .round

            return renderer
        }

        func mapView(_ mapView: MKMapView, didSelect view: MKAnnotationView) {
            if let markerAnnotation = view.annotation as? MarkerAnnotation {
                parent.viewModel.selectMarker(markerAnnotation.marker)
            }
        }

        private func isRouteSelected(_ polyline: RoutePolyline) -> Bool {
            guard let routeId = polyline.id,
                  let selectedRouteId = parent.viewModel.selectedRouteId else {
                return false
            }
            return routeId == selectedRouteId
        }

        private func lineWidthForTransportType(_ type: String) -> CGFloat {
            switch type.uppercased() {
            case "WALK": return 3.0
            case "BIKE": return 4.0
            case "CAR": return 5.0
            case "TRAIN": return 6.0
            case "PLANE": return 7.0
            case "BOAT": return 5.0
            default: return 3.0
            }
        }

        private func colorForPolyline(_ polyline: RoutePolyline, isSelected: Bool) -> UIColor {
            // Base color - use Silent Waters palette
            let baseColor: UIColor

            if let colorInt = polyline.colorInt {
                // If color is provided by the controller, use it
                // Convert Android color int (ARGB) to UIColor
                let alpha = CGFloat((colorInt >> 24) & 0xFF) / 255.0
                let red = CGFloat((colorInt >> 16) & 0xFF) / 255.0
                let green = CGFloat((colorInt >> 8) & 0xFF) / 255.0
                let blue = CGFloat(colorInt & 0xFF) / 255.0
                baseColor = UIColor(red: red, green: green, blue: blue, alpha: 1.0)
            } else {
                // Default: use historical route color (harborBlue) from Silent Waters palette
                baseColor = UIColor(Color.harborBlue)
            }

            // Apply opacity based on selection
            let alpha: CGFloat = isSelected ? 1.0 : 0.7
            return baseColor.withAlphaComponent(alpha)
        }
    }
}

/// Map view model
class MapViewModel: ObservableObject {
    private let controller: MapController
    private var stateObserver: Kotlinx_coroutines_coreJob?

    @Published var markers: [Shared.MapMarker] = []
    @Published var routes: [Shared.MapRoute] = []
    @Published var selectedMarkerId: String?
    @Published var selectedRouteId: String?
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
                self.selectedRouteId = state.selectedRoute?.id
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

    func selectMarker(_ marker: Shared.MapMarker) {
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
