import Foundation
import SwiftUI
import Shared

/**
 Integration guide for Region Monitoring (Geofencing) in TrailGlass iOS.

 OVERVIEW:
 The region monitoring system consists of four main components:
 1. RegionMonitoringManager - Handles CLLocationManager region monitoring
 2. RegionNotificationManager - Manages local notifications
 3. RegionViewModel - Provides SwiftUI-friendly interface to Region data
 4. RegionCoordinator - Coordinates all components together

 SETUP IN APPDELEGADE:
 Add the following to AppDelegate.swift:

 ```swift
 class AppDelegate: UIResponder, UIApplicationDelegate {
     lazy var appComponent: AppComponent = {
         return CreateKt.createIOSAppComponent()
     }()

     // Add these properties
     lazy var regionMonitoringManager = RegionMonitoringManager()
     lazy var regionNotificationManager = RegionNotificationManager()
     lazy var regionViewModel = RegionViewModel(appComponent: appComponent)
     lazy var regionCoordinator: RegionCoordinator = {
         RegionCoordinator(
             monitoringManager: regionMonitoringManager,
             notificationManager: regionNotificationManager,
             viewModel: regionViewModel
         )
     }()

     func application(
         _ application: UIApplication,
         didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
     ) -> Bool {
         // ... existing initialization ...

         // Start region monitoring
         regionCoordinator.start()

         return true
     }
 }
 ```

 USAGE IN VIEWS:
 Access the components via AppDelegate:

 ```swift
 struct MapView: View {
     @EnvironmentObject var appDelegate: AppDelegate
     @StateObject private var viewModel: RegionViewModel

     init(appComponent: AppComponent) {
         let delegate = UIApplication.shared.delegate as! AppDelegate
         _viewModel = StateObject(wrappedValue: delegate.regionViewModel)
     }

     var body: some View {
         VStack {
             // Show regions on map
             ForEach(viewModel.regions, id: \.id) { region in
                 RegionAnnotation(region: region)
             }

             // Add region button
             Button("Add Region") {
                 viewModel.createRegion(
                     name: "Home",
                     latitude: 37.7749,
                     longitude: -122.4194,
                     radiusMeters: 200
                 )
             }
         }
     }
 }
 ```

 PERMISSIONS:
 The app will automatically request:
 - Location "Always" permission (required for background monitoring)
 - Notification permission (required for region notifications)

 Users can be prompted at appropriate times in the UI.

 NOTIFICATION HANDLING:
 To handle notification taps and navigate to the map:

 ```swift
 struct ContentView: View {
     @State private var selectedRegionId: String?

     var body: some View {
         NavigationView {
             // Your main view
         }
         .onReceive(NotificationCenter.default.publisher(for: .showRegionOnMap)) { notification in
             if let regionId = notification.userInfo?["regionId"] as? String {
                 selectedRegionId = regionId
                 // Navigate to map view showing this region
             }
         }
     }
 }
 ```

 TESTING:
 1. Run the app on a physical device (region monitoring doesn't work in simulator)
 2. Grant location "Always" and notification permissions
 3. Create a region near your current location
 4. Walk/drive outside the region radius and back in
 5. You should receive notifications when entering/exiting

 To test in Xcode:
 1. Debug > Simulate Location > Custom Location
 2. Enter coordinates inside/outside your test region
 3. Notifications should trigger

 LIMITATIONS:
 - iOS limits to 20 monitored regions at a time
 - The system automatically prioritizes nearest regions
 - Regions require minimum 50m radius, maximum 2000m
 - Background monitoring requires "Always" location permission

 TODO WHEN KMP REGIONCONTROLLER IS READY:
 1. Update RegionViewModel to use RegionRepository from shared
 2. Replace placeholder CRUD operations with actual repository calls
 3. Add proper error handling from KMP domain layer
 4. Implement region event persistence via RegionRepository
 5. Add RegionController to AppComponent DI
 */

// MARK: - Example Region Management View

struct RegionManagementView: View {
    @ObservedObject var viewModel: RegionViewModel
    @ObservedObject var coordinator: RegionCoordinator

    var body: some View {
        NavigationView {
            List {
                Section(header: Text("Active Regions")) {
                    if viewModel.regions.isEmpty {
                        Text("No regions configured")
                            .foregroundColor(.secondary)
                    } else {
                        ForEach(viewModel.regions, id: \.id) { region in
                            RegionRow(
                                region: region,
                                onToggleNotifications: {
                                    viewModel.toggleNotifications(forRegionId: region.id)
                                },
                                onDelete: {
                                    viewModel.deleteRegion(id: region.id)
                                }
                            )
                        }
                    }
                }

                if let lastEvent = coordinator.lastEvent {
                    Section(header: Text("Last Event")) {
                        HStack {
                            Image(systemName: lastEvent.iconName)
                                .foregroundColor(lastEvent.color)
                            VStack(alignment: .leading) {
                                Text(lastEvent.displayText)
                                    .font(.headline)
                                Text(lastEvent.timestamp, style: .relative)
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                            }
                        }
                    }
                }

                Section {
                    HStack {
                        Text("Monitoring Active")
                        Spacer()
                        Image(systemName: coordinator.isActive ? "checkmark.circle.fill" : "xmark.circle.fill")
                            .foregroundColor(coordinator.isActive ? .green : .red)
                    }
                }
            }
            .navigationTitle("Geofences")
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Add") {
                        // Show add region sheet
                    }
                }
            }
        }
    }
}

// MARK: - Supporting Views

struct RegionRow: View {
    let region: Region
    let onToggleNotifications: () -> Void
    let onDelete: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            HStack {
                Image(systemName: region.iconName)
                    .foregroundColor(region.displayColor)

                Text(region.name)
                    .font(.headline)

                Spacer()

                Button(action: onToggleNotifications) {
                    Image(systemName: region.notificationsEnabled ? "bell.fill" : "bell.slash.fill")
                        .foregroundColor(region.notificationsEnabled ? .blue : .gray)
                }
            }

            if let description = region.description {
                Text(description)
                    .font(.caption)
                    .foregroundColor(.secondary)
            }

            HStack {
                Label(region.radiusDisplayText, systemImage: "circle")
                    .font(.caption)
                    .foregroundColor(.secondary)

                Spacer()

                if region.enterCount > 0 {
                    Text("Entered \(region.enterCount) times")
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
            }
        }
        .padding(.vertical, 4)
        .swipeActions(edge: .trailing, allowsFullSwipe: true) {
            Button(role: .destructive, action: onDelete) {
                Label("Delete", systemImage: "trash")
            }
        }
    }
}

// MARK: - Example Map Annotation

struct RegionAnnotation: View {
    let region: Region

    var body: some View {
        ZStack {
            Circle()
                .stroke(region.displayColor, lineWidth: 2)
                .frame(width: 50, height: 50)

            Image(systemName: region.iconName)
                .foregroundColor(region.displayColor)
                .font(.system(size: 20))
        }
    }
}

// MARK: - Example Add Region Form

struct AddRegionView: View {
    @Environment(\.dismiss) var dismiss
    @ObservedObject var viewModel: RegionViewModel

    @State private var name: String = ""
    @State private var description: String = ""
    @State private var latitude: String = ""
    @State private var longitude: String = ""
    @State private var radius: Double = Region.companion.DEFAULT_RADIUS_METERS
    @State private var notificationsEnabled: Bool = true

    var body: some View {
        NavigationView {
            Form {
                Section(header: Text("Details")) {
                    TextField("Name", text: $name)
                    TextField("Description (optional)", text: $description)
                }

                Section(header: Text("Location")) {
                    TextField("Latitude", text: $latitude)
                        .keyboardType(.decimalPad)
                    TextField("Longitude", text: $longitude)
                        .keyboardType(.decimalPad)
                }

                Section(header: Text("Radius")) {
                    Slider(
                        value: $radius,
                        in: Region.companion.MIN_RADIUS_METERS...Region.companion.MAX_RADIUS_METERS,
                        step: 50
                    )
                    Text("\(Int(radius)) meters")
                        .foregroundColor(.secondary)
                }

                Section {
                    Toggle("Enable Notifications", isOn: $notificationsEnabled)
                }
            }
            .navigationTitle("Add Region")
            .navigationBarItems(
                leading: Button("Cancel") { dismiss() },
                trailing: Button("Add") { addRegion() }
                    .disabled(name.isEmpty || latitude.isEmpty || longitude.isEmpty)
            )
        }
    }

    private func addRegion() {
        guard let lat = Double(latitude),
              let lon = Double(longitude) else {
            return
        }

        viewModel.createRegion(
            name: name,
            description: description.isEmpty ? nil : description,
            latitude: lat,
            longitude: lon,
            radiusMeters: radius,
            notificationsEnabled: notificationsEnabled
        )

        dismiss()
    }
}
