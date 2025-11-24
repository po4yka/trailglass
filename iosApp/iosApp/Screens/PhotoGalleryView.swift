import SwiftUI
import Shared
import PhotosUI

/**
 * SwiftUI photo gallery screen with Liquid Glass components.
 * Shows photos grouped by date with glass styling and animations.
 */
struct PhotoGalleryView: View {
    let appComponent: AppComponent
    @StateObject private var viewModel: PhotoGalleryViewModel
    @State private var scrollOffset: CGFloat = 0
    @State private var showGridView = false
    @State private var showPhotoPicker = false
    @State private var selectedPhotos: [PhotosPickerItem] = []

    init(appComponent: AppComponent) {
        self.appComponent = appComponent
        _viewModel = StateObject(wrappedValue: PhotoGalleryViewModel(controller: appComponent.photoGalleryController))
    }

    var body: some View {
        ZStack {
            Color.backgroundLight.ignoresSafeArea()

            VStack(spacing: 0) {
                // Medium flexible navigation bar
                MediumFlexibleNavigationBar(
                    title: "Photos",
                    scrollOffset: scrollOffset,
                    actions: [
                        NavigationAction(icon: showGridView ? "list.bullet" : "square.grid.2x2") {
                            withAnimation(MotionConfig.smooth) {
                                showGridView.toggle()
                            }
                        },
                        NavigationAction(icon: "plus") {
                            showPhotoPicker = true
                        },
                        NavigationAction(icon: "arrow.clockwise") {
                            viewModel.refresh()
                        }
                    ],
                    subtitle: {
                        Text(photoSubtitle)
                    }
                )

                if viewModel.isLoading {
                    GlassLoadingIndicator(variant: .pulsing, color: .lightBlue)
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else if let error = viewModel.error {
                    PhotoErrorView(error: error, onRetry: viewModel.refresh)
                } else if !viewModel.photoGroups.isEmpty {
                    PhotoGalleryContent(
                        photoGroups: viewModel.photoGroups,
                        appComponent: appComponent,
                        scrollOffset: $scrollOffset,
                        showGridView: showGridView
                    )
                } else {
                    EmptyGalleryView(onImportClick: { showPhotoPicker = true })
                }
            }
        }
        .navigationBarHidden(true)
        .onAppear {
            viewModel.loadGallery()
        }
        .photosPicker(
            isPresented: $showPhotoPicker,
            selection: $selectedPhotos,
            matching: .images
        )
        .onChange(of: selectedPhotos) { newPhotos in
            handlePhotoSelection(newPhotos)
        }
    }

    private var photoSubtitle: String {
        let count = viewModel.photoGroups.reduce(0) { $0 + $1.photoCount }
        return "\(count) photos"
    }

    private func handlePhotoSelection(_ photos: [PhotosPickerItem]) {
        // Process selected photos
        Task {
            for item in photos {
                do {
                    // Load photo data
                    if let data = try await item.loadTransferable(type: Data.self) {
                        // Use item identifier as URI (PhotosPickerItem provides unique identifier)
                        let uri = item.identifier ?? "photo_\(UUID().uuidString)"

                        // Convert Swift Data to Kotlin ByteArray
                        let kotlinData = data.toKotlinByteArray()

                        // Import photo via controller
                        viewModel.controller.importPhoto(
                            uri: uri,
                            photoData: kotlinData,
                            timestamp: nil // Will extract from EXIF
                        )
                    }
                } catch {
                    print("Failed to load photo: \(error)")
                    // TODO: Show error alert to user
                }
            }
        }
        selectedPhotos = [] // Clear selection
        showPhotoPicker = false
    }

}

/**
 * Gallery content with glass styling and scroll tracking.
 */
private struct PhotoGalleryContent: View {
    let photoGroups: [PhotoGroup]
    let appComponent: AppComponent
    @Binding var scrollOffset: CGFloat
    let showGridView: Bool

    var body: some View {
        ScrollView {
            GeometryReader { geometry in
                Color.clear.preference(
                    key: ScrollOffsetPreferenceKey.self,
                    value: geometry.frame(in: .named("scroll")).minY
                )
            }
            .frame(height: 0)

            LazyVStack(spacing: 20) {
                ForEach(photoGroups, id: \.date) { group in
                    PhotoGroupSection(
                        group: group,
                        appComponent: appComponent,
                        showGridView: showGridView
                    )
                }
            }
            .padding(16)
            .padding(.bottom, 96) // Extra padding for tab bar
        }
        .coordinateSpace(name: "scroll")
        .onPreferenceChange(ScrollOffsetPreferenceKey.self) { value in
            scrollOffset = value
        }
    }
}

/**
 * Photo group section with glass styling.
 */
private struct PhotoGroupSection: View {
    let group: PhotoGroup
    let appComponent: AppComponent
    let showGridView: Bool

    private let columns = [
        GridItem(.flexible()),
        GridItem(.flexible()),
        GridItem(.flexible())
    ]

    var body: some View {
        GlassEffectGroup(spacing: 12, padding: 16) {
            VStack(alignment: .leading, spacing: 12) {
                // Date header with glass styling
                HStack(alignment: .top) {
                    VStack(alignment: .leading, spacing: 4) {
                        Text(formatDate(group.date))
                            .font(.headline)
                            .fontWeight(.bold)
                            .foregroundColor(.primary)

                        if let location = group.location {
                            HStack(spacing: 4) {
                                Image(systemName: "location.fill")
                                    .font(.caption2)
                                Text(location)
                                    .font(.caption)
                            }
                            .foregroundColor(.blueSlate)
                        }
                    }

                    Spacer()

                    // Photo count badge
                    HStack(spacing: 4) {
                        Image(systemName: "photo.fill")
                            .font(.caption)
                        Text("\(group.photoCount)")
                            .font(.caption)
                            .fontWeight(.medium)
                    }
                    .padding(.horizontal, 10)
                    .padding(.vertical, 6)
                    .glassEffectTinted(.coastalPath, opacity: 0.6)
                    .foregroundColor(Color.coolSteel)
                }

                // Photo grid
                LazyVGrid(columns: columns, spacing: 8) {
                    ForEach(group.photos, id: \.photo.id) { photoWithMeta in
                        NavigationLink(destination: PhotoDetailView(photoId: photoWithMeta.photo.id, controller: appComponent.photoDetailController)) {
                            PhotoGlassGridItem(photo: photoWithMeta)
                        }
                        .buttonStyle(PlainButtonStyle())
                    }
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
 * Photo grid item with glass overlay.
 */
private struct PhotoGlassGridItem: View {
    let photo: PhotoWithMetadata

    var body: some View {
        ZStack(alignment: .topTrailing) {
            // Photo thumbnail
            AsyncImage(url: URL(string: photo.photo.uri)) { phase in
                switch phase {
                case .empty:
                    Rectangle()
                        .fill(Color.lightCyan.opacity(0.3))
                        .overlay(
                            GlassLoadingIndicator(variant: .pulsing, size: 24, color: .lightBlue)
                        )
                case .success(let image):
                    image
                        .resizable()
                        .aspectRatio(contentMode: .fill)
                case .failure:
                    Rectangle()
                        .fill(Color.lightCyan.opacity(0.3))
                        .overlay(
                            Image(systemName: "photo.fill")
                                .foregroundColor(.driftwood)
                        )
                @unknown default:
                    EmptyView()
                }
            }
            .frame(height: 120)
            .clipped()
            .cornerRadius(12)
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .strokeBorder(Color.white.opacity(0.2), lineWidth: 1)
            )

            // Attachment indicator with glass background
            if !photo.attachments.isEmpty {
                HStack(spacing: 3) {
                    Image(systemName: "mappin.circle.fill")
                        .font(.caption2)
                    Text("\(photo.attachments.count)")
                        .font(.caption2)
                        .fontWeight(.bold)
                }
                .foregroundColor(.white)
                .padding(.horizontal, 8)
                .padding(.vertical, 4)
                    .glassEffectTinted(
                        Color.coastalPath,
                        opacity: 0.8
                    )
                .padding(6)
            }
        }
    }
}

/**
 * Empty gallery with glass styling.
 */
private struct EmptyGalleryView: View {
    let onImportClick: () -> Void

    var body: some View {
        VStack(spacing: 24) {
            Image(systemName: "photo.on.rectangle.angled")
                .font(.system(size: 64))
                .foregroundColor(.lightBlue)

            VStack(spacing: 8) {
                Text("No photos yet")
                    .font(.title2)
                    .fontWeight(.semibold)
                    .foregroundColor(.primary)

                Text("Import photos to see your memories")
                    .font(.body)
                    .foregroundColor(.secondary)
            }

            GlassButton(
                title: "Import Photos",
                icon: "plus",
                variant: .filled,
                tint: .lightBlue,
                action: onImportClick
            )
        }
        .padding(32)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

/**
 * Error view with glass styling.
 */
private struct PhotoErrorView: View {
    let error: String
    let onRetry: () -> Void

    var body: some View {
        VStack(spacing: 20) {
            Image(systemName: "exclamationmark.triangle.fill")
                .font(.system(size: 48))
                .foregroundColor(.driftwood)

            VStack(spacing: 8) {
                Text("Something went wrong")
                    .font(.headline)
                    .foregroundColor(.primary)

                Text(error)
                    .font(.body)
                    .foregroundColor(.secondary)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 32)
            }

            GlassButton(
                title: "Retry",
                icon: "arrow.clockwise",
                variant: .filled,
                tint: .lightBlue,
                action: onRetry
            )
        }
        .padding(32)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

/**
 * ViewModel for PhotoGalleryView bridging Swift and Kotlin.
 */
class PhotoGalleryViewModel: ObservableObject {
    private let controller: PhotoGalleryController
    private var stateObserver: KotlinJob?

    @Published var photoGroups: [PhotoGroup] = []
    @Published var isLoading: Bool = false
    @Published var error: String?

    init(controller: PhotoGalleryController) {
        self.controller = controller
        observeState()
    }

    private func observeState() {
        // Observe Kotlin StateFlow
        stateObserver = controller.state.subscribe { [weak self] (state: PhotoGalleryState?) in
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
