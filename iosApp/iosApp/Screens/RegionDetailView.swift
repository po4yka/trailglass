import SwiftUI
import Shared
import MapKit

class RegionDetailViewModel: NSObject, ObservableObject, CLLocationManagerDelegate {
    @Published var name: String = ""
    @Published var regionDescription: String = ""
    @Published var latitude: Double = 37.7749
    @Published var longitude: Double = -122.4194
    @Published var radiusMeters: Double = 100.0
    @Published var notificationsEnabled: Bool = true
    @Published var mapRegion: MKCoordinateRegion
    @Published var selectedCoordinate: CLLocationCoordinate2D?
    @Published var showingValidationError = false
    @Published var validationErrorMessage = ""

    private let locationManager = CLLocationManager()
    let viewModel: RegionViewModel
    let region: Region?
    let onSave: () -> Void

    init(viewModel: RegionViewModel, region: Region?, onSave: @escaping () -> Void) {
        self.viewModel = viewModel
        self.region = region
        self.onSave = onSave

        if let region = region {
            _name = Published(initialValue: region.name)
            _regionDescription = Published(initialValue: region.description_ ?? "")
            _latitude = Published(initialValue: region.latitude)
            _longitude = Published(initialValue: region.longitude)
            _radiusMeters = Published(initialValue: Double(region.radiusMeters))
            _notificationsEnabled = Published(initialValue: region.notificationsEnabled)
            _mapRegion = Published(initialValue: MKCoordinateRegion(
                center: CLLocationCoordinate2D(latitude: region.latitude, longitude: region.longitude),
                span: MKCoordinateSpan(latitudeDelta: 0.01, longitudeDelta: 0.01)
            ))
        } else {
            _mapRegion = Published(initialValue: MKCoordinateRegion(
                center: CLLocationCoordinate2D(latitude: 37.7749, longitude: -122.4194),
                span: MKCoordinateSpan(latitudeDelta: 0.01, longitudeDelta: 0.01)
            ))
        }

        super.init()

        // Set up location manager after super.init()
        locationManager.delegate = self
        locationManager.desiredAccuracy = kCLLocationAccuracyHundredMeters
    }

    // MARK: - Actions

    func useCurrentLocation() {
        // Check location authorization status
        let status = CLLocationManager.authorizationStatus()

        switch status {
        case .notDetermined:
            // Request permission
            locationManager.requestWhenInUseAuthorization()
            print("Requesting location permission")
        case .restricted, .denied:
            // Show error - location services disabled
            validationErrorMessage = "Location access is required to use current location. Please enable it in Settings."
            showingValidationError = true
        case .authorizedWhenInUse, .authorizedAlways:
            // Get current location
            if let location = locationManager.location {
                let coordinate = location.coordinate
                selectedCoordinate = coordinate
                mapRegion.center = coordinate
                latitude = coordinate.latitude
                longitude = coordinate.longitude
                print("Using current location: \(coordinate.latitude), \(coordinate.longitude)")
            } else {
                // Location not available
                validationErrorMessage = "Current location is not available. Please try again."
                showingValidationError = true
            }
        @unknown default:
            break
        }
    }

    func saveRegion() {
        guard !name.trimmingCharacters(in: .whitespaces).isEmpty else {
            validationErrorMessage = "Please enter a name for this place"
            showingValidationError = true
            return
        }

        viewModel.createRegion(
            name: name,
            description: regionDescription.isEmpty ? nil : regionDescription,
            latitude: latitude,
            longitude: longitude,
            radiusMeters: radiusMeters,
            notificationsEnabled: notificationsEnabled
        )

        onSave()
    }

    // MARK: - CLLocationManagerDelegate

}

struct RegionDetailView: View {
    @StateObject private var detailViewModel: RegionDetailViewModel
    @Environment(\.dismiss) private var dismiss

    init(regionViewModel: RegionViewModel, region: Region?, onSave: @escaping () -> Void) {
        _detailViewModel = StateObject(wrappedValue: RegionDetailViewModel(
            viewModel: regionViewModel,
            region: region,
            onSave: onSave
        ))
    }

    var isEditMode: Bool {
        detailViewModel.region != nil
    }

    var body: some View {
        ZStack {
            Color.backgroundLight.ignoresSafeArea()

            ScrollView {
                VStack(spacing: 16) {
                    mapPreview

                    basicInfoSection

                    locationSection

                    radiusSection

                    notificationsSection

                    if isEditMode, let region = detailViewModel.region {
                        statisticsSection(region: region)
                    }

                    saveButton
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 16)
            }
        }
        .navigationTitle(isEditMode ? "Edit Place" : "New Place")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .navigationBarLeading) {
                Button("Cancel") {
                    dismiss()
                }
            }
        }
        .alert("Validation Error", isPresented: $detailViewModel.showingValidationError) {
            Button("OK", role: .cancel) {}
        } message: {
            Text(detailViewModel.validationErrorMessage)
        }
    }

    private var mapPreview: some View {
        GlassEffectGroup(spacing: 0, padding: 0) {
            ZStack {
                Map(coordinateRegion: $detailViewModel.mapRegion)
                // Overlay the annotation on top of the map
                GeometryReader { geometry in
                    ZStack {
                        Circle()
                            .fill(Color.coastalPath.opacity(0.3))
                            .frame(width: radiusInPixels, height: radiusInPixels)

                        Circle()
                            .stroke(Color.coastalPath, lineWidth: 2)
                            .frame(width: radiusInPixels, height: radiusInPixels)

                        Image(systemName: "mappin.circle.fill")
                            .foregroundColor(.coastalPath)
                            .font(.title)
                    }
                    .position(x: geometry.size.width / 2, y: geometry.size.height / 2)
                }
            }
            .frame(height: 200)
            .cornerRadius(12)
        }
    }

    private var radiusInPixels: CGFloat {
        let metersPerPoint = detailViewModel.mapRegion.span.latitudeDelta * 111000 / 400
        return CGFloat(detailViewModel.radiusMeters / metersPerPoint)
    }

    private var basicInfoSection: some View {
        GlassEffectGroup(spacing: 12, padding: 16) {
            VStack(alignment: .leading, spacing: 12) {
                Text("Basic Information")
                    .font(.headline)
                    .foregroundColor(.primary)

                VStack(alignment: .leading, spacing: 4) {
                    Text("Name")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                    TextField("Home, Work, Gym, etc.", text: $detailViewModel.name)
                        .textFieldStyle(GlassTextFieldStyle())
                }

                VStack(alignment: .leading, spacing: 4) {
                    Text("Description (Optional)")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                    TextField("Add a description", text: $detailViewModel.regionDescription)
                        .textFieldStyle(GlassTextFieldStyle())
                }
            }
        }
    }

    private var locationSection: some View {
        GlassEffectGroup(spacing: 12, padding: 16) {
            VStack(alignment: .leading, spacing: 12) {
                Text("Location")
                    .font(.headline)
                    .foregroundColor(.primary)

                HStack(spacing: 12) {
                    VStack(alignment: .leading, spacing: 4) {
                        Text("Latitude")
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                        TextField("Latitude", value: $detailViewModel.latitude, format: .number)
                            .textFieldStyle(GlassTextFieldStyle())
                            .keyboardType(.decimalPad)
                            .onChange(of: detailViewModel.latitude) { newValue in
                                detailViewModel.mapRegion.center = CLLocationCoordinate2D(
                                    latitude: newValue,
                                    longitude: detailViewModel.longitude
                                )
                            }
                    }

                    VStack(alignment: .leading, spacing: 4) {
                        Text("Longitude")
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                        TextField("Longitude", value: $detailViewModel.longitude, format: .number)
                            .textFieldStyle(GlassTextFieldStyle())
                            .keyboardType(.decimalPad)
                            .onChange(of: detailViewModel.longitude) { newValue in
                                detailViewModel.mapRegion.center = CLLocationCoordinate2D(
                                    latitude: detailViewModel.latitude,
                                    longitude: newValue
                                )
                            }
                    }
                }

                Button(action: detailViewModel.useCurrentLocation) {
                    HStack {
                        Image(systemName: "location.fill")
                        Text("Use Current Location")
                    }
                    .font(.subheadline)
                    .foregroundColor(.coastalPath)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 8)
                    .background(
                        RoundedRectangle(cornerRadius: 8)
                            .fill(Color.coastalPath.opacity(0.1))
                    )
                }
                .buttonStyle(PlainButtonStyle())
            }
        }
    }

    private var radiusSection: some View {
        GlassEffectGroup(spacing: 12, padding: 16) {
            VStack(alignment: .leading, spacing: 12) {
                HStack {
                    Text("Radius")
                        .font(.headline)
                        .foregroundColor(.primary)

                    Spacer()

                    Text(formatRadius(detailViewModel.radiusMeters))
                        .font(.subheadline)
                        .fontWeight(.medium)
                        .foregroundColor(.blueSlate)
                }

                Slider(
                    value: $detailViewModel.radiusMeters,
                    in: Double(Region.companion.MIN_RADIUS_METERS)...5000.0,
                    step: 50
                )
                .accentColor(.coastalPath)

                HStack {
                    Text("\(Region.companion.MIN_RADIUS_METERS)m")
                        .font(.caption)
                        .foregroundColor(.secondary)
                    Spacer()
                    Text("\(Int(5000.0 / 1000))km")
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
            }
        }
    }

    private var notificationsSection: some View {
        GlassEffectGroup(spacing: 12, padding: 16) {
            HStack {
                VStack(alignment: .leading, spacing: 4) {
                    Text("Notifications")
                        .font(.headline)
                        .foregroundColor(.primary)

                    Text("Get notified when entering or leaving this place")
                        .font(.caption)
                        .foregroundColor(.secondary)
                }

                Spacer()

                Toggle("", isOn: $detailViewModel.notificationsEnabled)
                    .labelsHidden()
                    .tint(.coastalPath)
            }
        }
    }

    private func statisticsSection(region: Region) -> some View {
        GlassEffectGroup(spacing: 12, padding: 16) {
            VStack(alignment: .leading, spacing: 12) {
                Text("Statistics")
                    .font(.headline)
                    .foregroundColor(.primary)

                HStack(spacing: 20) {
                    VStack(alignment: .leading, spacing: 4) {
                        Text("Total Visits")
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                        Text("\(region.enterCount)")
                            .font(.title2)
                            .fontWeight(.bold)
                            .foregroundColor(.blueSlate)
                    }

                    Spacer()

                    VStack(alignment: .leading, spacing: 4) {
                        Text("Last Visit")
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                        if let lastEntered = region.lastEnteredDate {
                            Text(formatDate(lastEntered))
                                .font(.subheadline)
                                .foregroundColor(.blueSlate)
                        } else {
                            Text("Never")
                                .font(.subheadline)
                                .foregroundColor(.secondary)
                        }
                    }
                }

                HStack(spacing: 20) {
                    VStack(alignment: .leading, spacing: 4) {
                        Text("Created")
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                        Text(formatDate(region.createdDate))
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                    }

                    Spacer()

                    VStack(alignment: .leading, spacing: 4) {
                        Text("Last Updated")
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                        Text(formatDate(region.updatedDate))
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                    }
                }
            }
        }
    }

    private var saveButton: some View {
        GlassButton(
            title: isEditMode ? "Update Place" : "Create Place",
            icon: isEditMode ? "checkmark.circle.fill" : "plus.circle.fill",
            variant: .filled,
            tint: .coastalPath
        ) {
            detailViewModel.saveRegion()
        }
        .disabled(detailViewModel.name.isEmpty)
        .opacity(detailViewModel.name.isEmpty ? 0.5 : 1.0)
    }




    private func formatRadius(_ meters: Double) -> String {
        if meters >= 1000 {
            let km = meters / 1000.0
            return String(format: "%.1f km", km)
        } else {
            return String(format: "%.0f m", meters)
        }
    }

    private func formatDate(_ date: Date) -> String {
        let formatter = RelativeDateTimeFormatter()
        formatter.unitsStyle = .abbreviated
        return formatter.localizedString(for: date, relativeTo: Date())
    }
}

struct GlassTextFieldStyle: TextFieldStyle {
    func _body(configuration: TextField<Self._Label>) -> some View {
        configuration
            .padding(12)
            .background(
                RoundedRectangle(cornerRadius: 8)
                    .fill(Color.white.opacity(0.1))
                    .overlay(
                        RoundedRectangle(cornerRadius: 8)
                            .stroke(Color.coolSteel.opacity(0.3), lineWidth: 1)
                    )
            )
    }

    // MARK: - CLLocationManagerDelegate

}

struct MapAnnotation: Identifiable {
    let id = UUID()
    let coordinate: CLLocationCoordinate2D
}

#Preview {
    NavigationView {
        RegionDetailView(
            regionViewModel: RegionViewModel(
                appComponent: InjectIOSAppComponent(platformModule: IOSPlatformModule())
            ),
            region: nil,
            onSave: {}
        )
    }
}
