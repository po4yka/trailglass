import SwiftUI
import Shared
import MapKit

struct RegionDetailView: View {
    @ObservedObject var viewModel: RegionViewModel
    let region: Region?
    let onSave: () -> Void

    @State private var name: String = ""
    @State private var description: String = ""
    @State private var latitude: Double = 37.7749
    @State private var longitude: Double = -122.4194
    @State private var radiusMeters: Double = Region.companion.DEFAULT_RADIUS_METERS
    @State private var notificationsEnabled: Bool = true
    @State private var mapRegion: MKCoordinateRegion
    @State private var showingValidationError = false
    @State private var validationErrorMessage = ""

    @Environment(\.dismiss) private var dismiss

    init(viewModel: RegionViewModel, region: Region?, onSave: @escaping () -> Void) {
        self.viewModel = viewModel
        self.region = region
        self.onSave = onSave

        if let region = region {
            _name = State(initialValue: region.name)
            _description = State(initialValue: region.description_ ?? "")
            _latitude = State(initialValue: region.latitude)
            _longitude = State(initialValue: region.longitude)
            _radiusMeters = State(initialValue: region.radiusMeters)
            _notificationsEnabled = State(initialValue: region.notificationsEnabled)
            _mapRegion = State(initialValue: MKCoordinateRegion(
                center: CLLocationCoordinate2D(latitude: region.latitude, longitude: region.longitude),
                span: MKCoordinateSpan(latitudeDelta: 0.01, longitudeDelta: 0.01)
            ))
        } else {
            _mapRegion = State(initialValue: MKCoordinateRegion(
                center: CLLocationCoordinate2D(latitude: 37.7749, longitude: -122.4194),
                span: MKCoordinateSpan(latitudeDelta: 0.01, longitudeDelta: 0.01)
            ))
        }
    }

    var isEditMode: Bool {
        region != nil
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

                    if isEditMode, let region = region {
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
        .alert("Validation Error", isPresented: $showingValidationError) {
            Button("OK", role: .cancel) {}
        } message: {
            Text(validationErrorMessage)
        }
    }

    private var mapPreview: some View {
        GlassEffectGroup(spacing: 0, padding: 0) {
            Map(coordinateRegion: $mapRegion, annotationItems: [MapAnnotation(coordinate: mapRegion.center)]) { item in
                MapAnnotation(coordinate: item.coordinate) {
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
                }
            }
            .frame(height: 200)
            .cornerRadius(12)
            .onTapGesture {
                // TODO: Allow user to tap on map to select location
            }
        }
    }

    private var radiusInPixels: CGFloat {
        let metersPerPoint = mapRegion.span.latitudeDelta * 111000 / 400
        return CGFloat(radiusMeters / metersPerPoint)
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
                    TextField("Home, Work, Gym, etc.", text: $name)
                        .textFieldStyle(GlassTextFieldStyle())
                }

                VStack(alignment: .leading, spacing: 4) {
                    Text("Description (Optional)")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                    TextField("Add a description", text: $description)
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
                        TextField("Latitude", value: $latitude, format: .number)
                            .textFieldStyle(GlassTextFieldStyle())
                            .keyboardType(.decimalPad)
                            .onChange(of: latitude) { newValue in
                                updateMapRegion()
                            }
                    }

                    VStack(alignment: .leading, spacing: 4) {
                        Text("Longitude")
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                        TextField("Longitude", value: $longitude, format: .number)
                            .textFieldStyle(GlassTextFieldStyle())
                            .keyboardType(.decimalPad)
                            .onChange(of: longitude) { newValue in
                                updateMapRegion()
                            }
                    }
                }

                Button(action: useCurrentLocation) {
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

                    Text(formatRadius(radiusMeters))
                        .font(.subheadline)
                        .fontWeight(.medium)
                        .foregroundColor(.blueSlate)
                }

                Slider(
                    value: $radiusMeters,
                    in: Region.companion.MIN_RADIUS_METERS...Region.companion.MAX_RADIUS_METERS,
                    step: 50
                )
                .accentColor(.coastalPath)

                HStack {
                    Text("\(Int(Region.companion.MIN_RADIUS_METERS))m")
                        .font(.caption)
                        .foregroundColor(.secondary)
                    Spacer()
                    Text("\(Int(Region.companion.MAX_RADIUS_METERS / 1000))km")
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

                Toggle("", isOn: $notificationsEnabled)
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
            saveRegion()
        }
        .disabled(name.isEmpty)
        .opacity(name.isEmpty ? 0.5 : 1.0)
    }

    private func updateMapRegion() {
        withAnimation {
            mapRegion.center = CLLocationCoordinate2D(latitude: latitude, longitude: longitude)
        }
    }

    private func useCurrentLocation() {
        // TODO: Implement location manager to get current location
        // For now, just show a placeholder
        print("Use current location tapped")
    }

    private func saveRegion() {
        guard !name.isEmpty else {
            validationErrorMessage = "Please enter a name for this place"
            showingValidationError = true
            return
        }

        guard latitude >= -90 && latitude <= 90 else {
            validationErrorMessage = "Latitude must be between -90 and 90"
            showingValidationError = true
            return
        }

        guard longitude >= -180 && longitude <= 180 else {
            validationErrorMessage = "Longitude must be between -180 and 180"
            showingValidationError = true
            return
        }

        if let region = region {
            let updatedRegion = Region(
                id: region.id,
                userId: region.userId,
                name: name,
                description: description.isEmpty ? nil : description,
                latitude: latitude,
                longitude: longitude,
                radiusMeters: radiusMeters,
                notificationsEnabled: notificationsEnabled,
                createdAt: region.createdAt,
                updatedAt: Kotlinx_datetimeInstant.companion.fromEpochMilliseconds(
                    epochMilliseconds: Int64(Date().timeIntervalSince1970 * 1000)
                ),
                enterCount: region.enterCount,
                lastEnteredAt: region.lastEnteredAt
            )
            viewModel.updateRegion(updatedRegion)
        } else {
            viewModel.createRegion(
                name: name,
                description: description.isEmpty ? nil : description,
                latitude: latitude,
                longitude: longitude,
                radiusMeters: radiusMeters,
                notificationsEnabled: notificationsEnabled
            )
        }

        onSave()
        dismiss()
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
}

struct MapAnnotation: Identifiable {
    let id = UUID()
    let coordinate: CLLocationCoordinate2D
}

#Preview {
    NavigationView {
        RegionDetailView(
            viewModel: RegionViewModel(
                appComponent: InjectIOSAppComponent(platformModule: IOSPlatformModule())
            ),
            region: nil,
            onSave: {}
        )
    }
}
