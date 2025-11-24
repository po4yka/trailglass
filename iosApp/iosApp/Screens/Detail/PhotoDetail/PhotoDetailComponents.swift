import SwiftUI
import Shared
import MapKit

/// Photo image viewer
struct PhotoImageView: View {
    let uri: String

    var body: some View {
        AsyncImage(url: URL(string: uri)) { phase in
            switch phase {
            case .empty:
                ProgressView()
                    .frame(maxWidth: .infinity)
                    .frame(height: 300)
                    .background(Color(.systemGray6))
            case .success(let image):
                image
                    .resizable()
                    .aspectRatio(contentMode: .fit)
                    .frame(maxWidth: .infinity)
            case .failure:
                VStack(spacing: 12) {
                    Image(systemName: "photo")
                        .font(.system(size: 64))
                        .foregroundColor(.red)
                    Text("Failed to load image")
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
                .frame(maxWidth: .infinity)
                .frame(height: 300)
                .background(Color(.systemGray6))
            @unknown default:
                EmptyView()
            }
        }
        .cornerRadius(12)
    }
}

/// Basic photo information card
struct PhotoInfoCard: View {
    let photo: PhotoWithMetadata

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Photo Information")
                .font(.headline)

            VStack(spacing: 8) {
                InfoRow(
                    label: "Taken",
                    value: formatTimestamp(photo.photo.timestamp)
                )

                if let lat = photo.photo.latitude, let lon = photo.photo.longitude {
                    InfoRow(
                        label: "Location",
                        value: String(format: "%.6f, %.6f", lat, lon)
                    )
                }

                if let width = photo.photo.width, let height = photo.photo.height {
                    InfoRow(
                        label: "Dimensions",
                        value: "\(width) × \(height)"
                    )
                }

                if let sizeBytes = photo.photo.sizeBytes {
                    let sizeMB = Double(sizeBytes) / (1024.0 * 1024.0)
                    InfoRow(
                        label: "Size",
                        value: String(format: "%.2f MB", sizeMB)
                    )
                }

                if let mimeType = photo.photo.mimeType {
                    InfoRow(label: "Type", value: mimeType)
                }
            }
        }
        .padding()
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color(.systemGray6))
        .cornerRadius(12)
    }

    private func formatTimestamp(_ timestamp: Kotlinx_datetimeInstant) -> String {
        let date = Date(timeIntervalSince1970: TimeInterval(timestamp.epochSeconds))
        let formatter = DateFormatter()
        formatter.dateStyle = .medium
        formatter.timeStyle = .short
        return formatter.string(from: date)
    }
}

/// EXIF camera information card
struct ExifInfoCard: View {
    let metadata: PhotoMetadata

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Camera Information")
                .font(.headline)

            VStack(spacing: 8) {
                if let make = metadata.cameraMake {
                    InfoRow(label: "Make", value: make)
                }

                if let model = metadata.cameraModel {
                    InfoRow(label: "Model", value: model)
                }

                if let lens = metadata.lens {
                    InfoRow(label: "Lens", value: lens)
                }

                if let exifLat = metadata.exifLatitude, let exifLon = metadata.exifLongitude {
                    InfoRow(
                        label: "EXIF GPS",
                        value: String(format: "%.6f, %.6f", exifLat.doubleValue, exifLon.doubleValue)
                    )

                    if let alt = metadata.exifAltitude {
                        InfoRow(
                            label: "Altitude",
                            value: String(format: "%.1f m", alt.doubleValue)
                        )
                    }
                }
            }
        }
        .padding()
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color(.systemGray6))
        .cornerRadius(12)
    }
}

/// Camera settings card
struct CameraSettingsCard: View {
    let metadata: PhotoMetadata

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Camera Settings")
                .font(.headline)

            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    if let focalLength = metadata.focalLength {
                        SettingChip(
                            icon: "camera.aperture",
                            text: String(format: "%.0f mm", focalLength.doubleValue)
                        )
                    }

                    if let aperture = metadata.aperture {
                        SettingChip(
                            icon: "circle.dotted",
                            text: String(format: "f/%.1f", aperture.doubleValue)
                        )
                    }

                    if let iso = metadata.iso {
                        SettingChip(
                            icon: "chart.bar",
                            text: "ISO \(iso.intValue)"
                        )
                    }

                    if let shutterSpeed = metadata.shutterSpeed {
                        SettingChip(
                            icon: "timer",
                            text: shutterSpeed
                        )
                    }

                    if let flash = metadata.flash {
                        SettingChip(
                            icon: "bolt.fill",
                            text: flash.boolValue ? "Flash On" : "No Flash"
                        )
                    }
                }
                .padding(.horizontal, 4)
            }
        }
        .padding()
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color(.systemGray6))
        .cornerRadius(12)
    }
}

/// Camera setting chip
struct SettingChip: View {
    let icon: String
    let text: String

    var body: some View {
        HStack(spacing: 6) {
            Image(systemName: icon)
                .font(.caption)
            Text(text)
                .font(.caption)
        }
        .padding(.horizontal, 12)
        .padding(.vertical, 6)
        .background(Color.blue.opacity(0.15))
        .foregroundColor(.blue)
        .cornerRadius(8)
    }
}

/// Photo location map
struct PhotoLocationMap: View {
    let latitude: Double
    let longitude: Double

    @State private var region: MKCoordinateRegion

    init(latitude: Double, longitude: Double) {
        self.latitude = latitude
        self.longitude = longitude
        _region = State(initialValue: MKCoordinateRegion(
            center: CLLocationCoordinate2D(latitude: latitude, longitude: longitude),
            span: MKCoordinateSpan(latitudeDelta: 0.01, longitudeDelta: 0.01)
        ))
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Location on Map")
                .font(.headline)
                .padding(.horizontal)

            Map(
                coordinateRegion: .constant(region),
                annotationItems: [
                    MapMarkerItem(coordinate: CLLocationCoordinate2D(latitude: latitude, longitude: longitude))
                ]
            ) { item in
                MapMarker(coordinate: item.coordinate, tint: .red)
            }
            .frame(height: 200)
            .cornerRadius(12)
        }
    }
}

/// Map marker item
struct MapMarkerItem: Identifiable {
    let id = UUID()
    let coordinate: CLLocationCoordinate2D
}

/// Attachments card
struct AttachmentsCard: View {
    let attachments: [PhotoAttachment]

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Attached to Visits")
                .font(.headline)

            ForEach(attachments, id: \.id) { attachment in
                HStack(spacing: 12) {
                    Image(systemName: "mappin.circle.fill")
                        .font(.title2)
                        .foregroundColor(.blue)

                    VStack(alignment: .leading, spacing: 4) {
                        Text("Visit \(attachment.placeVisitId.prefix(8))")
                            .font(.body)
                            .fontWeight(.medium)

                        if let caption = attachment.caption {
                            Text(caption)
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }

                        Text(formatTimestamp(attachment.attachedAt))
                            .font(.caption2)
                            .foregroundColor(.secondary)
                    }

                    Spacer()
                }
                .padding()
                .background(Color(.systemGray5))
                .cornerRadius(8)
            }
        }
        .padding()
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color(.systemGray6))
        .cornerRadius(12)
    }

    private func formatTimestamp(_ timestamp: Kotlinx_datetimeInstant) -> String {
        let date = Date(timeIntervalSince1970: TimeInterval(timestamp.epochSeconds))
        let formatter = DateFormatter()
        formatter.dateStyle = .short
        formatter.timeStyle = .short
        return "Attached \(formatter.string(from: date))"
    }
}

/// Info row for key-value pairs
struct InfoRow: View {
    let label: String
    let value: String

    var body: some View {
        HStack {
            Text(label)
                .font(.subheadline)
                .foregroundColor(.secondary)

            Spacer()

            Text(value)
                .font(.subheadline)
                .fontWeight(.medium)
        }
    }
}

/// ViewModel for PhotoDetailView
class PhotoDetailViewModel: ObservableObject {
    private let photoId: String
    private let controller: PhotoDetailController

    private var stateObserver: Kotlinx_coroutines_coreJob?

    @Published var photoWithMetadata: PhotoWithMetadata?
    @Published var isLoading = true
    @Published var errorMessage: String?
    @Published var showDeleteAlert = false

    init(photoId: String, controller: PhotoDetailController) {
        self.photoId = photoId
        self.controller = controller
    }

    func loadPhoto() {
        isLoading = true
        errorMessage = nil

        // Load photo
        controller.loadPhoto(photoId: photoId)

        // Observe state
        stateObserver = controller.state.subscribe { [weak self] state in
            guard let self = self, let state = state else { return }

            DispatchQueue.main.async {
                self.isLoading = state.isLoading
                self.errorMessage = state.error
                self.photoWithMetadata = state.photo
            }
        }
    }

    func showAttachmentDialog() {
        controller.showAttachmentDialog()
    }

    func sharePhoto() {
        controller.sharePhoto()
        // TODO: Implement platform-specific share sheet
        print("Share photo: \(photoId)")
    }

    func deletePhoto() {
        controller.deletePhoto()
    }

    deinit {
        stateObserver?.cancel(cause: nil)
    }
}
