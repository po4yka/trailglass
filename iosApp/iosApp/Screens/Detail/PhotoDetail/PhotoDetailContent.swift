import SwiftUI
import shared

/// Photo detail content layout
struct PhotoDetailContent: View {
    let photo: PhotoWithMetadata
    let onAttachToVisit: () -> Void

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                // Photo image
                PhotoImageView(uri: photo.photo.uri)

                // Basic metadata section
                PhotoInfoCard(photo: photo)

                // EXIF metadata section
                if let metadata = photo.metadata {
                    ExifInfoCard(metadata: metadata)

                    // Camera settings
                    if hasExifSettings(metadata) {
                        CameraSettingsCard(metadata: metadata)
                    }
                }

                // Location map
                if let lat = photo.photo.latitude, let lon = photo.photo.longitude {
                    PhotoLocationMap(latitude: lat, longitude: lon)
                }

                // Attachments section
                if !photo.attachments.isEmpty {
                    AttachmentsCard(attachments: photo.attachments)
                } else {
                    Button(action: onAttachToVisit) {
                        Label("Attach to Visit", systemImage: "plus.circle")
                    }
                    .buttonStyle(.borderedProminent)
                    .padding(.vertical)
                }
            }
            .padding()
        }
    }

    private func hasExifSettings(_ metadata: PhotoMetadata) -> Bool {
        return metadata.focalLength != nil ||
               metadata.aperture != nil ||
               metadata.iso != nil ||
               metadata.shutterSpeed != nil
    }
}
