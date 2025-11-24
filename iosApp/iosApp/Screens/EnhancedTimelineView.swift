import SwiftUI
import Shared

/**
 * SwiftUI timeline screen matching Android EnhancedTimelineScreen.
 * Shows timeline with zoom levels, filtering, and search.
 */
struct EnhancedTimelineView: View {
    @StateObject private var viewModel: EnhancedTimelineViewModel
    @State private var showFilterSheet = false
    @State private var showSearchBar = false
    @State private var scrollOffset: CGFloat = 0

    private let appComponent: AppComponent

    init(controller: EnhancedTimelineController, locationTrackingController: LocationTrackingController, appComponent: AppComponent) {
        self.appComponent = appComponent
        _viewModel = StateObject(wrappedValue: EnhancedTimelineViewModel(controller: controller, locationTrackingController: locationTrackingController))
    }

    var body: some View {
        VStack(spacing: 0) {
            // Large flexible navigation bar with hero background
            LargeFlexibleNavigationBar(
                title: "Timeline",
                scrollOffset: scrollOffset,
                actions: [
                    NavigationAction(icon: "magnifyingglass") {
                        showSearchBar.toggle()
                    },
                    NavigationAction(icon: "line.3.horizontal.decrease.circle") {
                        showFilterSheet = true
                    },
                    NavigationAction(icon: "arrow.clockwise") {
                        viewModel.refresh()
                    }
                ],
                subtitle: {
                    Text(formatDateForZoom(viewModel.selectedDate, viewModel.zoomLevel))
                },
                backgroundContent: {
                    HeroGradientBackground(
                        startColor: Color.lightCyan,
                        endColor: Color.coolSteel
                    )
                }
            )

            // Zoom level selector
            ZoomLevelSelector(
                currentZoom: viewModel.zoomLevel,
                onZoomChanged: { viewModel.setZoomLevel($0) }
            )

            // Date navigation
            DateNavigationBar(
                selectedDate: viewModel.selectedDate,
                zoomLevel: viewModel.zoomLevel,
                onPrevious: { viewModel.navigatePrevious() },
                onNext: { viewModel.navigateNext() },
                onToday: { viewModel.jumpToToday() }
            )

            // Active filters indicator
            if viewModel.filter.isActive {
                ActiveFiltersChips(
                    filter: viewModel.filter,
                    onClearAll: { viewModel.clearFilters() }
                )
            }

            // Content
            ZStack {
                if viewModel.isLoading {
                    VStack {
                        Spacer()
                        GlassLoadingIndicator(variant: .pulsing, size: 72, color: .coastalPath)
                        Spacer()
                    }
                } else if let error = viewModel.error {
                    VStack {
                        Spacer()
                        ErrorView(error: error) {
                            viewModel.refresh()
                        }
                        Spacer()
                    }
                } else if !viewModel.items.isEmpty {
                    // TimelineContent(...) // Commented out - requires rebuilt Kotlin framework
                    Text("Timeline items available: \(viewModel.items.count)")
                        .foregroundColor(.secondary)
                } else {
                    VStack {
                        Spacer()
                        EmptyTimelineView()
                        Spacer()
                    }
                }

                // Tracking FAB overlay
                VStack {
                    Spacer()
                    HStack {
                        Spacer()
                        TrackingFAB(
                            isTracking: $viewModel.isTracking,
                            onToggleTracking: { viewModel.toggleTracking() },
                            onAddPhoto: { viewModel.addPhoto() },
                            onAddNote: { viewModel.addNote() },
                            onCheckIn: { viewModel.checkIn() }
                        )
                    }
                }
            }
        }
        .sheet(isPresented: $showFilterSheet) {
            TimelineFilterSheet(
                currentFilter: viewModel.filter,
                onFilterChanged: { viewModel.updateFilter($0) },
                onDismiss: { showFilterSheet = false }
            )
        }
        .sheet(isPresented: $viewModel.showPhotoPicker) {
            PhotoPickerSheet(
                onPhotoSelected: { photos in
                    // Handle selected photos
                    Task {
                        for photo in photos {
                            // Convert PhotosPickerItem to Photo and import
                            if let data = try? await photo.loadTransferable(type: Data.self) {
                                let uri = photo.identifier ?? "photo_\(UUID().uuidString)"
                                let kotlinData = data.toKotlinByteArray()

                                // Use PhotoGalleryController to import photo
                                do {
                                    try await appComponent.photoGalleryController.importPhoto(
                                        uri: uri,
                                        photoData: kotlinData,
                                        timestamp: nil
                                    )
                                } catch {
                                    print("Failed to import photo: \(error)")
                                }
                            }
                        }
                    }
                },
                onDismiss: { viewModel.showPhotoPicker = false }
            )
        }
        .sheet(isPresented: $viewModel.showNoteEditor) {
            NoteEditorSheet(
                onNoteSaved: { title, content, attachLocation in
                    // Handle note creation
                    Task {
                        // TODO: Create note via repository or use case
                        print("Create note: \(title) - \(content) - attachLocation: \(attachLocation)")
                        // For now, just close the sheet
                        viewModel.showNoteEditor = false
                    }
                },
                onDismiss: { viewModel.showNoteEditor = false }
            )
        }
        .sheet(isPresented: $viewModel.showCheckIn) {
            ManualCheckInSheet(
                onCheckInSaved: { placeName, category, notes, latitude, longitude in
                    // Handle check-in creation
                    Task {
                        // TODO: Create place visit via repository or use case
                        print("Create check-in: \(placeName) at (\(latitude ?? 0), \(longitude ?? 0))")
                        // For now, just close the sheet
                        viewModel.showCheckIn = false
                    }
                },
                onDismiss: { viewModel.showCheckIn = false }
            )
        }
        .onAppear {
            viewModel.loadTimeline()
        }
    }

    private func formatDateForZoom(_ date: LocalDate, _ zoom: TimelineZoomLevel) -> String {
        switch zoom {
        case .day:
            return "Day View • \(date.year)-\(String(format: "%02d", date.monthNumber))-\(String(format: "%02d", date.dayOfMonth))"
        case .week:
            return "Week View"
        case .month:
            let formatter = DateFormatter()
            return "Month View • \(formatter.shortMonthSymbols[Int(date.monthNumber) - 1]) \(date.year)"
        case .year:
            return "Year View • \(date.year)"
        default:
            return "Timeline"
        }
    }
}
