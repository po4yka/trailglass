import SwiftUI
import shared

/// Main tab navigation view for TrailGlass iOS app
struct MainTabView: View {
    let appComponent: AppComponent

    init(appComponent: AppComponent) {
        self.appComponent = appComponent
    }

    var body: some View {
        TabView {
            // Stats tab - using EnhancedStatsController for full featured stats
            EnhancedStatsView(controller: appComponent.enhancedStatsController)
                .tabItem {
                    Label("Stats", systemImage: "chart.bar.fill")
                }

            // Timeline tab - using EnhancedTimelineController for full timeline with filters
            EnhancedTimelineView(controller: appComponent.enhancedTimelineController)
                .tabItem {
                    Label("Timeline", systemImage: "clock.fill")
                }

            // Map tab - using MapController for map visualization
            MapScreen(mapController: appComponent.mapController)
                .tabItem {
                    Label("Map", systemImage: "map.fill")
                }

            // Trips tab - using TripsController for trip management with filtering
            TripsView(appComponent: appComponent)
                .tabItem {
                    Label("Trips", systemImage: "suitcase.fill")
                }

            // Photos tab - using PhotoGalleryController for photo gallery with navigation
            PhotoGalleryView(appComponent: appComponent)
                .tabItem {
                    Label("Photos", systemImage: "photo.fill")
                }

            // Settings tab - using SettingsController for comprehensive settings
            EnhancedSettingsView(controller: appComponent.settingsController)
                .tabItem {
                    Label("Settings", systemImage: "gear")
                }
        }
    }
}
