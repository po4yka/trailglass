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

    init(controller: EnhancedTimelineController) {
        _viewModel = StateObject(wrappedValue: EnhancedTimelineViewModel(controller: controller))
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
                    TimelineContent(
                        items: viewModel.items,
                        zoomLevel: viewModel.zoomLevel,
                        scrollOffset: $scrollOffset
                    )
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
        .searchable(text: $viewModel.searchQuery, isPresented: $showSearchBar, prompt: "Search timeline...")
        .sheet(isPresented: $showFilterSheet) {
            TimelineFilterSheet(
                currentFilter: viewModel.filter,
                onFilterChanged: { viewModel.updateFilter($0) },
                onDismiss: { showFilterSheet = false }
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
            let monthNames = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"]
            return "Month View • \(monthNames[Int(date.monthNumber) - 1]) \(date.year)"
        case .year:
            return "Year View • \(date.year)"
        @unknown default:
            return "Timeline"
        }
    }
}
