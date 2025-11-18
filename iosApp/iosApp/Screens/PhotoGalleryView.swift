import SwiftUI
import shared

/**
 * SwiftUI photo gallery screen showing photos grouped by date.
 * Matches Android PhotoGalleryScreen functionality with navigation to detail view.
 */
struct PhotoGalleryView: View {
    let appComponent: AppComponent
    @StateObject private var viewModel: PhotoGalleryViewModel

    init(appComponent: AppComponent) {
        self.appComponent = appComponent
        _viewModel = StateObject(wrappedValue: PhotoGalleryViewModel(controller: appComponent.photoGalleryController))
    }

    var body: some View {
        NavigationStack {
            Group {
                if viewModel.isLoading {
                    ProgressView()
                } else if let error = viewModel.error {
                    ErrorView(error: error, onRetry: viewModel.refresh)
                } else if !viewModel.photoGroups.isEmpty {
                    PhotoGalleryContent(
                        photoGroups: viewModel.photoGroups,
                        appComponent: appComponent
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
 * Gallery content showing photo groups with navigation.
 */
private struct PhotoGalleryContent: View {
    let photoGroups: [PhotoGroup]
    let appComponent: AppComponent

    var body: some View {
        ScrollView {
            LazyVStack(spacing: 24) {
                ForEach(photoGroups, id: \.date) { group in
                    PhotoGroupSection(
                        group: group,
                        appComponent: appComponent
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
    let appComponent: AppComponent

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
                    NavigationLink(destination: PhotoDetailView(photoId: photoWithMeta.photo.id, appComponent: appComponent)) {
                        PhotoGridItem(photo: photoWithMeta)
                    }
                    .buttonStyle(PlainButtonStyle())
                }
            }
        }
    }

    private func formatDate(_ date: Kotlinx_datetimeLocalDate) -> String {
        let formatter = DateFormatter()
        formatter.dateStyle = .medium
        formatter.timeStyle = .none

        // Convert Kotlinx LocalDate to Swift Date
        let components = DateComponents(
            year: Int(date.year),
            month: Int(date.monthNumber),
            day: Int(date.dayOfMonth)
        )

        if let swiftDate = Calendar.current.date(from: components) {
            return formatter.string(from: swiftDate)
        }

        return date.description()
    }
}

/**
 * Individual photo grid item.
 */
private struct PhotoGridItem: View {
    let photo: PhotoWithMetadata

    var body: some View {
        ZStack(alignment: .topTrailing) {
            // Photo thumbnail
            AsyncImage(url: URL(string: photo.photo.uri)) { phase in
                switch phase {
                case .empty:
                    Rectangle()
                        .fill(Color(.systemGray5))
                        .overlay(ProgressView())
                case .success(let image):
                    image
                        .resizable()
                        .aspectRatio(contentMode: .fill)
                case .failure:
                    Rectangle()
                        .fill(Color(.systemGray5))
                        .overlay(
                            Image(systemName: "photo")
                                .foregroundColor(.red)
                        )
                @unknown default:
                    EmptyView()
                }
            }
            .frame(height: 120)
            .clipped()
            .cornerRadius(8)

            // Attachment indicator
            if !photo.attachments.isEmpty {
                HStack(spacing: 2) {
                    Image(systemName: "mappin.circle.fill")
                        .font(.caption2)
                    Text("\(photo.attachments.count)")
                        .font(.caption2)
                        .fontWeight(.bold)
                }
                .padding(.horizontal, 6)
                .padding(.vertical, 3)
                .background(Color.blue.opacity(0.9))
                .foregroundColor(.white)
                .cornerRadius(6)
                .padding(4)
            }
        }
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
    private var stateObserver: Kotlinx_coroutines_coreJob?

    @Published var photoGroups: [PhotoGroup] = []
    @Published var isLoading: Bool = false
    @Published var error: String? = nil

    init(controller: PhotoGalleryController) {
        self.controller = controller
        observeState()
    }

    private func observeState() {
        // Observe Kotlin StateFlow
        stateObserver = controller.state.subscribe { [weak self] state in
            guard let self = self, let state = state else { return }

            DispatchQueue.main.async {
                self.photoGroups = state.photoGroups
                self.isLoading = state.isLoading
                self.error = state.error
            }
        }
    }

    func loadGallery() {
        controller.loadGallery()
    }

    func importPhotos() {
        controller.importPhotos()
        // TODO: Show platform-specific photo picker
        print("Import photos requested")
    }

    func refresh() {
        controller.refresh()
    }

    deinit {
        stateObserver?.cancel(cause: nil)
    }
}

#Preview {
    Text("PhotoGalleryView Preview - Requires DI setup")
}
