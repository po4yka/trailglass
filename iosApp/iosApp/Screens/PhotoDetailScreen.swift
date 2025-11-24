import SwiftUI
import Shared

/**
 * SwiftUI photo detail screen with full metadata display.
 * Matches Android PhotoDetailScreen functionality.
 */
struct PhotoDetailView: View {
    let photoId: String
    @StateObject private var viewModel: PhotoDetailViewModel
    @Environment(\.dismiss) private var dismiss

    init(photoId: String, controller: PhotoDetailController) {
        self.photoId = photoId
        _viewModel = StateObject(wrappedValue: PhotoDetailViewModel(controller: controller))
    }

    var body: some View {
        Group {
            if viewModel.isLoading {
                ProgressView()
            } else if let error = viewModel.error {
                ErrorView(error: error, onRetry: { viewModel.loadPhoto(photoId) })
            } else if let photo = viewModel.photo {
                PhotoDetailContent(
                    photo: photo,
                    onAttachToVisit: viewModel.showAttachmentDialog
                )
            }
        }
        .navigationTitle("Photo Details")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                Button(action: viewModel.sharePhoto) {
                    Image(systemName: "square.and.arrow.up")
                }
            }
            ToolbarItem(placement: .navigationBarTrailing) {
                Button(role: .destructive, action: viewModel.deletePhoto) {
                    Image(systemName: "trash")
                }
            }
        }
        .onAppear {
            viewModel.loadPhoto(photoId)
        }
    }
}

/**
 * Photo detail content with metadata sections.
 */
private struct PhotoDetailContent: View {
    let photo: PhotoWithMetadata
    let onAttachToVisit: () -> Void

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                // Photo image
                AsyncImage(url: URL(string: photo.photo.uri)) { phase in
                    switch phase {
                    case .empty:
                        ProgressView()
                            .frame(maxWidth: .infinity)
                            .frame(height: 300)
                    case .success(let image):
                        image
                            .resizable()
                            .aspectRatio(contentMode: .fit)
                            .frame(maxWidth: .infinity)
                    case .failure:
                        Image(systemName: "photo")
                            .font(.system(size: 64))
                            .foregroundColor(.red)
                            .frame(maxWidth: .infinity)
                            .frame(height: 300)
                    @unknown default:
                        EmptyView()
                    }
                }
                .cornerRadius(12)
                .padding(.horizontal)

                // Metadata section
                MetadataSection(photo: photo)

                // EXIF section
                if let metadata = photo.metadata {
                    ExifSection(metadata: metadata)
                }

                // Attachments section
                if !photo.attachments.isEmpty {
                    AttachmentsSection(attachments: photo.attachments)
                } else {
                    Button(action: onAttachToVisit) {
                        Label("Attach to Visit", systemImage: "plus")
                    }
                    .buttonStyle(.borderedProminent)
                }
            }
            .padding(.vertical)
        }
    }
}

/**
 * Photo metadata section.
 */
private struct MetadataSection: View {
    let photo: PhotoWithMetadata

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Photo Information")
                .font(.headline)

            VStack(spacing: 8) {
                InfoRow(label: "Taken", value: formatTimestamp(photo.photo.timestamp))

                if let lat = photo.photo.latitude, let lon = photo.photo.longitude {
                    InfoRow(label: "Location", value: "\(lat), \(lon)")
                }

                if let width = photo.photo.width, let height = photo.photo.height {
                    InfoRow(label: "Dimensions", value: "\(width) × \(height)")
                }

                if let sizeBytes = photo.photo.sizeBytes {
                    let sizeMB = Double(sizeBytes) / (1024.0 * 1024.0)
                    InfoRow(label: "Size", value: String(format: "%.2f MB", sizeMB))
                }
            }
        }
        .padding()
        .background(Color(.systemGray6))
        .cornerRadius(12)
        .padding(.horizontal)
    }

    private func formatTimestamp(_ timestamp: Kotlinx_datetimeInstant) -> String {
        return timestamp.description()
    }
}

/**
 * EXIF metadata section.
 */
private struct ExifSection: View {
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
            }

            // Camera settings
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    if let focalLength = metadata.focalLength {
                        SettingChip(text: "\(Int(focalLength.doubleValue))mm")
                    }

                    if let aperture = metadata.aperture {
                        SettingChip(text: "f/\(aperture.doubleValue)")
                    }

                    if let iso = metadata.iso {
                        SettingChip(text: "ISO \(iso.intValue)")
                    }

                    if let shutterSpeed = metadata.shutterSpeed {
                        SettingChip(text: shutterSpeed)
                    }
                }
            }

            if let lat = metadata.exifLatitude, let lon = metadata.exifLongitude {
                InfoRow(label: "GPS Coordinates", value: "\(lat.doubleValue), \(lon.doubleValue)")

                if let alt = metadata.exifAltitude {
                    InfoRow(label: "Altitude", value: "\(Int(alt.doubleValue))m")
                }
            }
        }
        .padding()
        .background(Color(.systemGray6))
        .cornerRadius(12)
        .padding(.horizontal)
    }
}

/**
 * Attachments section.
 */
private struct AttachmentsSection: View {
    let attachments: [PhotoAttachment]

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Attached to Visits")
                .font(.headline)

            ForEach(attachments, id: \.id) { attachment in
                HStack {
                    Image(systemName: "mappin.circle")
                        .foregroundColor(.blue)

                    VStack(alignment: .leading) {
                        Text("Visit \(attachment.placeVisitId.prefix(8))")
                            .font(.body)

                        if let caption = attachment.caption {
                            Text(caption)
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                    }

                    Spacer()
                }
                .padding(.vertical, 4)
            }
        }
        .padding()
        .background(Color(.systemGray6))
        .cornerRadius(12)
        .padding(.horizontal)
    }
}

/**
 * Camera setting chip.
 */
private struct SettingChip: View {
    let text: String

    var body: some View {
        Text(text)
            .font(.caption)
            .padding(.horizontal, 12)
            .padding(.vertical, 6)
            .background(Color.blue.opacity(0.2))
            .foregroundColor(.blue)
            .cornerRadius(8)
    }
}

// InfoRow moved to SharedComponents.swift

/**
 * ViewModel for PhotoDetailView.
 */
class PhotoDetailViewModel: ObservableObject {
    private let controller: PhotoDetailController

    @Published var photo: PhotoWithMetadata?
    @Published var isLoading: Bool = false
    @Published var error: String?

    init(controller: PhotoDetailController) {
        self.controller = controller
        observeState()
    }

    private func observeState() {
        // Observe Kotlin StateFlow
    }

    func loadPhoto(_ photoId: String) {
        controller.loadPhoto(photoId: photoId)
    }

    func sharePhoto() {
        controller.sharePhoto()
    }

    func deletePhoto() {
        controller.deletePhoto()
    }

    func showAttachmentDialog() {
        controller.showAttachmentDialog()
    }
}
