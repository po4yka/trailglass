import SwiftUI
import shared

/**
 * SwiftUI photo gallery screen showing photos grouped by date.
 * Matches Android PhotoGalleryScreen functionality.
 */
struct PhotoGalleryView: View {
    @StateObject private var viewModel: PhotoGalleryViewModel

    init(controller: PhotoGalleryController) {
        _viewModel = StateObject(wrappedValue: PhotoGalleryViewModel(controller: controller))
    }

    var body: some View {
        NavigationView {
            Group {
                if viewModel.isLoading {
                    ProgressView()
                } else if let error = viewModel.error {
                    ErrorView(error: error, onRetry: viewModel.refresh)
                } else if !viewModel.photoGroups.isEmpty {
                    PhotoGalleryContent(
                        photoGroups: viewModel.photoGroups,
                        onPhotoClick: viewModel.onPhotoClick
                    )
                } else {
                    EmptyGalleryView(onImportClick: viewModel.importPhotos)
                }
            }
            .navigationTitle("Photos")
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: viewModel.importPhotos) {
                        Image(systemName: "plus")
                    }
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: viewModel.refresh) {
                        Image(systemName: "arrow.clockwise")
                    }
                }
            }
            .onAppear {
                viewModel.loadGallery()
            }
        }
    }
}

/**
 * Gallery content showing photo groups.
 */
private struct PhotoGalleryContent: View {
    let photoGroups: [PhotoGroup]
    let onPhotoClick: (String) -> Void

    var body: some View {
        ScrollView {
            LazyVStack(spacing: 24) {
                ForEach(photoGroups, id: \.date) { group in
                    PhotoGroupSection(
                        group: group,
                        onPhotoClick: onPhotoClick
                    )
                }
            }
            .padding(16)
        }
    }
}

/**
 * Section showing photos for a specific date.
 */
private struct PhotoGroupSection: View {
    let group: PhotoGroup
    let onPhotoClick: (String) -> Void

    private let columns = [
        GridItem(.flexible()),
        GridItem(.flexible()),
        GridItem(.flexible())
    ]

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            // Date header
            HStack {
                VStack(alignment: .leading) {
                    Text(formatDate(group.date))
                        .font(.title2)
                        .fontWeight(.bold)

                    if let location = group.location {
                        Text(location)
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                }

                Spacer()

                Text("\(group.photoCount) photos")
                    .font(.body)
                    .foregroundColor(.secondary)
            }

            // Photo grid
            LazyVGrid(columns: columns, spacing: 4) {
                ForEach(group.photos, id: \.photo.id) { photoWithMeta in
                    PhotoGridItem(
                        photo: photoWithMeta,
                        onClick: { onPhotoClick(photoWithMeta.photo.id) }
                    )
                }
            }
        }
    }

    private func formatDate(_ date: Kotlinx_datetimeLocalDate) -> String {
        // Format date appropriately
        return date.description()
    }
}

/**
 * Individual photo grid item.
 */
private struct PhotoGridItem: View {
    let photo: PhotoWithMetadata
    let onClick: () -> Void

    var body: some View {
        Button(action: onClick) {
            ZStack(alignment: .topTrailing) {
                // Photo thumbnail
                AsyncImage(url: URL(string: photo.photo.uri)) { phase in
                    switch phase {
                    case .empty:
                        ProgressView()
                            .frame(maxWidth: .infinity, maxHeight: .infinity)
                    case .success(let image):
                        image
                            .resizable()
                            .aspectRatio(contentMode: .fill)
                    case .failure:
                        Image(systemName: "photo")
                            .foregroundColor(.red)
                            .frame(maxWidth: .infinity, maxHeight: .infinity)
                    @unknown default:
                        EmptyView()
                    }
                }
                .frame(height: 120)
                .clipped()
                .cornerRadius(8)

                // Attachment indicator
                if !photo.attachments.isEmpty {
                    Image(systemName: "mappin.circle.fill")
                        .font(.caption)
                        .padding(4)
                        .background(Color.blue.opacity(0.9))
                        .foregroundColor(.white)
                        .cornerRadius(4)
                        .padding(4)
                }
            }
        }
        .buttonStyle(PlainButtonStyle())
    }
}

/**
 * Empty gallery state.
 */
private struct EmptyGalleryView: View {
    let onImportClick: () -> Void

    var body: some View {
        VStack(spacing: 16) {
            Image(systemName: "photo.on.rectangle")
                .font(.system(size: 64))
                .foregroundColor(.secondary)

            Text("No photos yet")
                .font(.title)

            Text("Import photos to see your memories")
                .font(.body)
                .foregroundColor(.secondary)

            Button(action: onImportClick) {
                Label("Import Photos", systemImage: "plus")
            }
            .buttonStyle(.borderedProminent)
            .padding(.top, 8)
        }
    }
}

/**
 * Error view with retry option.
 */
private struct ErrorView: View {
    let error: String
    let onRetry: () -> Void

    var body: some View {
        VStack(spacing: 16) {
            Image(systemName: "exclamationmark.triangle")
                .font(.system(size: 64))
                .foregroundColor(.red)

            Text("Something went wrong")
                .font(.title)

            Text(error)
                .font(.body)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal)

            Button("Retry", action: onRetry)
                .buttonStyle(.borderedProminent)
        }
    }
}

/**
 * ViewModel for PhotoGalleryView bridging Swift and Kotlin.
 */
class PhotoGalleryViewModel: ObservableObject {
    private let controller: PhotoGalleryController

    @Published var photoGroups: [PhotoGroup] = []
    @Published var isLoading: Bool = false
    @Published var error: String? = nil

    init(controller: PhotoGalleryController) {
        self.controller = controller
        observeState()
    }

    private func observeState() {
        // Observe Kotlin StateFlow
        // This requires a Kotlin-Swift bridge helper
        // For now, this is a placeholder showing the structure
    }

    func loadGallery() {
        controller.loadGallery()
    }

    func importPhotos() {
        controller.importPhotos()
    }

    func refresh() {
        controller.refresh()
    }

    func onPhotoClick(_ photoId: String) {
        // Navigate to photo detail
    }
}
