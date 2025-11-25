import SwiftUI
import MapKit
import Shared

/// Main map screen matching Android MapScreen functionality
struct MapScreen: View {
    @StateObject private var viewModel: MapViewModel
    @State private var showLocationPermissionDialog = false
    @State private var scrollOffset: CGFloat = 0
    @State private var showPlacesSheet = false
    var appComponent: AppComponent?

    init(mapController: MapController, appComponent: AppComponent? = nil) {
        _viewModel = StateObject(wrappedValue: MapViewModel(controller: mapController))
        self.appComponent = appComponent
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
                        ErrorBanner(
                            message: error,
                            onRetry: {
                                viewModel.refreshData()
                            },
                            onDismiss: {
                                viewModel.clearError()
                            }
                        )
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
                    NavigationAction(icon: "mappin.and.ellipse") {
                        showPlacesSheet = true
                    },
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
            LocationPermissionSheet(
                onRequestPermission: {
                    viewModel.requestLocationPermission()
                    showLocationPermissionDialog = false
                },
                onCancel: {
                    showLocationPermissionDialog = false
                }
            )
        }
        .onAppear {
            viewModel.refreshData()
        }
        .sheet(isPresented: $showPlacesSheet) {
            if let appComponent = appComponent {
                NavigationStack {
                    SimplePlacesView(placesController: appComponent.placesController)
                        .toolbar {
                            ToolbarItem(placement: .topBarLeading) {
                                Button("Done") {
                                    showPlacesSheet = false
                                }
                            }
                        }
                }
            }
        }
    }
}

#Preview {
    Text("MapScreen Preview - Requires DI setup")
}
