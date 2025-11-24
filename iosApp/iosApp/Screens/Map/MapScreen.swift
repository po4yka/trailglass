import SwiftUI
import MapKit
import Shared

/// Main map screen matching Android MapScreen functionality
struct MapScreen: View {
    @StateObject private var viewModel: MapViewModel
    @State private var showLocationPermissionDialog = false
    @State private var scrollOffset: CGFloat = 0

    init(mapController: MapController) {
        _viewModel = StateObject(wrappedValue: MapViewModel(controller: mapController))
    }

    var body: some View {
        ZStack(alignment: .top) {
            // Map content
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
            .ignoresSafeArea()

            // Flexible navigation bar
            LargeFlexibleNavigationBar(
                title: "Map",
                scrollOffset: scrollOffset,
                actions: [
                    NavigationAction(icon: "location.fill") {
                        viewModel.centerOnUserLocation()
                    },
                    NavigationAction(icon: "rectangle.expand.vertical") {
                        viewModel.fitAllMarkers()
                    },
                    NavigationAction(icon: "arrow.clockwise") {
                        viewModel.refreshData()
                    }
                ],
                subtitle: {
                    Text("\(viewModel.markers.count) places • \(viewModel.routes.count) routes")
                },
                backgroundContent: {
                    LinearGradient(
                        colors: [Color.lightCyan, Color.coastalPath],
                        startPoint: .top,
                        endPoint: .bottom
                    )
                }
            )
        }
        .sheet(isPresented: $showLocationPermissionDialog) {
            // TODO: Implement location permission sheet
            // For now, use a simple permission request view
            VStack {
                Text("Location Permission Required")
                    .font(.headline)
                    .padding()
                Button("Request Permission") {
                    viewModel.requestLocationPermission()
                    showLocationPermissionDialog = false
                }
                .padding()
                Button("Cancel") {
                    showLocationPermissionDialog = false
                }
                .padding()
            }
        }
        .onAppear {
            viewModel.refreshData()
        }
    }
}

#Preview {
    Text("MapScreen Preview - Requires DI setup")
}
