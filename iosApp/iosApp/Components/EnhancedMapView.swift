import SwiftUI
import MapKit
import Shared

/// Enhanced MapKit view with clustering, heatmap, and custom markers
struct SwiftUIEnhancedMapView: View {
    @ObservedObject var viewModel: EnhancedMapViewModel

    @State private var region = MKCoordinateRegion(
        center: CLLocationCoordinate2D(latitude: 0, longitude: 0),
        span: MKCoordinateSpan(latitudeDelta: 180, longitudeDelta: 180)
    )

    @State private var showModeSelector = false

    var body: some View {
        ZStack(alignment: .topLeading) {
            // Map view
            Map(coordinateRegion: $region, annotationItems: viewModel.allAnnotations) { annotation in
                annotation.annotationView
            }
            .overlay(
                // Heatmap overlay (would require custom implementation)
                heatmapOverlay
            )
            .onAppear {
                viewModel.setupInitialRegion { newRegion in
                    region = newRegion
                }
            }
            .onChange(of: region) { newRegion in
                viewModel.onRegionChanged(newRegion)
            }

            // Visualization mode selector
            VStack(alignment: .leading, spacing: 8) {
                Button(action: { showModeSelector.toggle() }) {
                    HStack {
                        Image(systemName: viewModel.currentModeIcon)
                        Text(viewModel.currentModeLabel)
                        Image(systemName: showModeSelector ? "chevron.up" : "chevron.down")
                    }
                    .padding(12)
                    .background(.ultraThinMaterial)
                    .cornerRadius(8)
                }

                if showModeSelector {
                    ForEach(viewModel.availableModes, id: \.self) { mode in
                        if mode != viewModel.currentMode {
                            Button(action: {
                                viewModel.setVisualizationMode(mode)
                                showModeSelector = false
                            }) {
                                HStack {
                                    Image(systemName: viewModel.iconForMode(mode))
                                    Text(viewModel.labelForMode(mode))
                                }
                                .padding(12)
                                .background(.ultraThinMaterial)
                                .cornerRadius(8)
                            }
                        }
                    }
                }
            }
            .padding()

            // Enhanced controls
            VStack(alignment: .trailing, spacing: 12) {
                // Clustering toggle
                Toggle(isOn: $viewModel.clusteringEnabled) {
                    HStack {
                        Image(systemName: "circle.grid.3x3.fill")
                        Text("Cluster")
                    }
                }
                .toggleStyle(.button)
                .padding(12)
                .background(.ultraThinMaterial)
                .cornerRadius(8)

                // Heatmap toggle
                Toggle(isOn: $viewModel.heatmapEnabled) {
                    HStack {
                        Image(systemName: "flame.fill")
                        Text("Heatmap")
                    }
                }
                .toggleStyle(.button)
                .padding(12)
                .background(.ultraThinMaterial)
                .cornerRadius(8)
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topTrailing)
            .padding()

            // Error view
            if let error = viewModel.error {
                VStack {
                    Image(systemName: "exclamationmark.triangle.fill")
                        .font(.largeTitle)
                        .foregroundColor(.adaptiveWarning)

                    Text(error)
                        .font(.body)
                        .foregroundColor(.adaptiveWarning)
                        .multilineTextAlignment(.center)
                        .padding()

                    Button("Dismiss") {
                        viewModel.clearError()
                    }
                    .buttonStyle(.borderedProminent)
                }
                .padding()
                .background(.ultraThinMaterial)
                .cornerRadius(12)
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            }
        }
    }

    @ViewBuilder
    private var heatmapOverlay: some View {
        if viewModel.heatmapEnabled, let heatmapData = viewModel.heatmapData {
            // Custom heatmap overlay would go here
            // This would require a custom MapKit overlay renderer
            // Heatmap gradient using Silent Waters palette:
            // Low intensity: lightCyan (E0FBFC)
            // Low-medium: lightBlue (C2DFE3)
            // Medium: coastalPath (7A9CAF)
            // High intensity: blueSlate (5C6B73)
            EmptyView()
        }
    }
}

/// View model for EnhancedMapView that bridges to shared EnhancedMapController
class EnhancedMapViewModel: ObservableObject {
    private let controller: EnhancedMapController
    private var stateObserver: KotlinJob?

    @Published var allAnnotations: [MapAnnotationItem] = []
    @Published var currentMode: MapVisualizationMode = .hybrid
    @Published var clusteringEnabled: Bool = true
    @Published var heatmapEnabled: Bool = false
    @Published var heatmapData: HeatmapData?
    @Published var error: String?

    init(controller: EnhancedMapController) {
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
                self.updateAnnotations(from: state)
                self.currentMode = state.visualizationMode
                self.clusteringEnabled = state.clusteringEnabled
                self.heatmapEnabled = state.heatmapEnabled
                self.heatmapData = state.mapData.heatmapData
                self.error = state.error
            }
        }
    }

    private func updateAnnotations(from state: EnhancedMapControllerEnhancedMapState) {
        var annotations: [MapAnnotationItem] = []

        // Add markers
        for marker in state.mapData.markers {
            let isSelected = marker.id == state.selectedMarker?.id
            annotations.append(.marker(marker, isSelected: isSelected))
        }

        // Add clusters
        for cluster in state.mapData.clusters {
            let isSelected = cluster.id == state.selectedCluster?.id
            annotations.append(.cluster(cluster, isSelected: isSelected))
        }

        self.allAnnotations = annotations
    }

    func setupInitialRegion(completion: @escaping (MKCoordinateRegion) -> Void) {
        // Get initial region from controller state
        if let region = controller.state.value?.mapData.region {
            let center = CLLocationCoordinate2D(
                latitude: region.center.latitude,
                longitude: region.center.longitude
            )
            let span = MKCoordinateSpan(
                latitudeDelta: region.latitudeDelta,
                longitudeDelta: region.longitudeDelta
            )
            completion(MKCoordinateRegion(center: center, span: span))
        }
    }

    func onRegionChanged(_ region: MKCoordinateRegion) {
        // Calculate zoom level from span
        // Zoom level = log2(360 / span) for latitude
        let zoomLevel = Float(log2(360.0 / region.span.latitudeDelta))
        controller.updateZoom(zoomLevel: zoomLevel)
    }

    func setVisualizationMode(_ mode: MapVisualizationMode) {
        controller.setVisualizationMode(mode: mode)
    }

    func clearError() {
        controller.clearError()
    }

    // UI helpers
    var availableModes: [MapVisualizationMode] {
        [.markers, .clusters, .heatmap, .hybrid]
    }

    var currentModeIcon: String {
        iconForMode(currentMode)
    }

    var currentModeLabel: String {
        labelForMode(currentMode)
    }

    func iconForMode(_ mode: MapVisualizationMode) -> String {
        switch mode {
        case .markers: return "mappin.circle.fill"
        case .clusters: return "circle.grid.3x3.fill"
        case .heatmap: return "flame.fill"
        case .hybrid: return "square.stack.3d.up.fill"
        default: return "map.fill"
        }
    }

    func labelForMode(_ mode: MapVisualizationMode) -> String {
        switch mode {
        case .markers: return "Markers"
        case .clusters: return "Clusters"
        case .heatmap: return "Heatmap"
        case .hybrid: return "Hybrid"
        default: return "Unknown"
        }
    }
}

#Preview {
    // Preview would require mocked controller
    Text("EnhancedMapView Preview")
}
