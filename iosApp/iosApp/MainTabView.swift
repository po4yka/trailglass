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
            // Stats tab - using basic StatsController
            StatsTabView(statsController: appComponent.statsController)
                .tabItem {
                    Label("Stats", systemImage: "chart.bar.fill")
                }

            // Timeline tab - using basic TimelineController
            TimelineTabView(timelineController: appComponent.timelineController)
                .tabItem {
                    Label("Timeline", systemImage: "clock.fill")
                }

            // Map tab - using MapController
            MapScreen(mapController: appComponent.mapController)
                .tabItem {
                    Label("Map", systemImage: "map.fill")
                }

            // Photos tab - using PhotoController
            PhotosTabView(photoController: appComponent.photoController)
                .tabItem {
                    Label("Photos", systemImage: "photo.fill")
                }

            // Settings tab - placeholder for now
            SettingsTabView()
                .tabItem {
                    Label("Settings", systemImage: "gear")
                }
        }
    }
}

/// Simple Stats tab wrapper
struct StatsTabView: View {
    let statsController: StatsController

    var body: some View {
        NavigationView {
            VStack {
                Text("Stats View")
                    .font(.largeTitle)
                Text("Controller: StatsController")
                    .font(.caption)
                    .foregroundColor(.gray)
            }
            .navigationTitle("Stats")
        }
    }
}

/// Simple Timeline tab wrapper
struct TimelineTabView: View {
    let timelineController: TimelineController

    var body: some View {
        NavigationView {
            VStack {
                Text("Timeline View")
                    .font(.largeTitle)
                Text("Controller: TimelineController")
                    .font(.caption)
                    .foregroundColor(.gray)
            }
            .navigationTitle("Timeline")
        }
    }
}

/// Simple Photos tab wrapper
struct PhotosTabView: View {
    let photoController: PhotoController

    var body: some View {
        NavigationView {
            VStack {
                Text("Photos View")
                    .font(.largeTitle)
                Text("Controller: PhotoController")
                    .font(.caption)
                    .foregroundColor(.gray)
            }
            .navigationTitle("Photos")
        }
    }
}

/// Simple Settings tab wrapper
struct SettingsTabView: View {
    var body: some View {
        NavigationView {
            VStack {
                Text("Settings View")
                    .font(.largeTitle)
                Text("TODO: SettingsController")
                    .font(.caption)
                    .foregroundColor(.gray)
            }
            .navigationTitle("Settings")
        }
    }
}
