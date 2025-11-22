import SwiftUI
import shared

/**
 * SwiftUI trips screen with TripsController integration.
 * Shows all trips with filtering, sorting, and navigation to trip details.
 * Updated with Liquid Glass components.
 */
struct TripsView: View {
    let appComponent: AppComponent
    @StateObject private var viewModel: TripsViewModel
    @State private var scrollOffset: CGFloat = 0

    init(appComponent: AppComponent) {
        self.appComponent = appComponent
        _viewModel = StateObject(wrappedValue: TripsViewModel(controller: appComponent.tripsController))
    }

    var body: some View {
        ZStack {
            Color.backgroundLight.ignoresSafeArea()

            VStack(spacing: 0) {
                // Large flexible navigation bar with hero background
                LargeFlexibleNavigationBar(
                    title: "Trips",
                    scrollOffset: scrollOffset,
                    actions: [
                        NavigationAction(icon: "line.3.horizontal.decrease.circle") {
                            // Filter action
                        },
                        NavigationAction(icon: "arrow.up.arrow.down.circle") {
                            // Sort action
                        }
                    ],
                    subtitle: {
                        Text(tripSubtitle)
                    },
                    backgroundContent: {
                        HeroGradientBackground(
                            startColor: .lightCyan,
                            endColor: .coastalPath
                        )
                    }
                )

                if viewModel.isLoading && viewModel.trips.isEmpty {
                    GlassLoadingIndicator(variant: .pulsing, color: .coastalPath)
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else if let error = viewModel.error {
                    ErrorView(error: error, onRetry: { viewModel.loadTrips() })
                } else if viewModel.filteredTrips.isEmpty {
                    if viewModel.trips.isEmpty {
                        EmptyTripsView(onCreateTrip: { viewModel.showCreateDialog() })
                    } else {
                        NoResultsView(message: "No trips match your filters")
                    }
                } else {
                    TripsContent(
                        trips: viewModel.filteredTrips,
                        ongoingTrips: viewModel.ongoingTrips,
                        appComponent: appComponent,
                        onRefresh: { viewModel.refresh() },
                        scrollOffset: $scrollOffset
                    )
                }
            }

            // Glass FAB
            VStack {
                Spacer()
                HStack {
                    Spacer()
                    GlassButton(
                        icon: "plus",
                        variant: .filled,
                        tint: .coastalPath
                    ) {
                        viewModel.showCreateDialog()
                    }
                    .frame(width: 56, height: 56)
                    .padding(.trailing, 16)
                    .padding(.bottom, 96) // Extra padding for tab bar
                }
            }
        }
        .navigationBarHidden(true)
        .onAppear {
            viewModel.loadTrips()
        }
    }

    private var tripSubtitle: String {
        let totalCount = viewModel.trips.count
        let ongoingCount = viewModel.ongoingTrips.count
        if ongoingCount > 0 {
            return "\(totalCount) trips • \(ongoingCount) ongoing"
        }
        return "\(totalCount) trips"
    }
}

/**
 * Trips content with glass cards.
 */
private struct TripsContent: View {
    let trips: [Trip]
    let ongoingTrips: [Trip]
    let appComponent: AppComponent
    let onRefresh: () -> Void
    @Binding var scrollOffset: CGFloat

    var body: some View {
        ScrollView {
            GeometryReader { geometry in
                Color.clear.preference(
                    key: ScrollOffsetPreferenceKey.self,
                    value: geometry.frame(in: .named("scroll")).minY
                )
            }
            .frame(height: 0)

            GlassEffectGroup(spacing: 12, padding: 16) {
                VStack(spacing: 12) {
                    // Group trips: ongoing first, then completed
                    let ongoing = trips.filter { $0.isOngoing }
                    let completed = trips.filter { !$0.isOngoing }

                    // Ongoing trips section
                    if !ongoing.isEmpty {
                        VStack(alignment: .leading, spacing: 8) {
                            Text("Ongoing")
                                .font(.headline)
                                .foregroundColor(.coastalPath)
                                .padding(.horizontal, 4)

                            ForEach(ongoing, id: \.id) { trip in
                                NavigationLink(destination: TripDetailView(tripId: trip.id, appComponent: appComponent)) {
                                    TripGlassCard(trip: trip)
                                }
                                .buttonStyle(PlainButtonStyle())
                            }
                        }
                    }

                    // Completed trips section
                    if !completed.isEmpty {
                        VStack(alignment: .leading, spacing: 8) {
                            Text(ongoing.isEmpty ? "All Trips" : "Past Trips")
                                .font(.headline)
                                .foregroundColor(.blueSlate)
                                .padding(.horizontal, 4)
                                .padding(.top, ongoing.isEmpty ? 0 : 12)

                            ForEach(completed, id: \.id) { trip in
                                NavigationLink(destination: TripDetailView(tripId: trip.id, appComponent: appComponent)) {
                                    TripGlassCard(trip: trip)
                                }
                                .buttonStyle(PlainButtonStyle())
                            }
                        }
                    }
                }
            }
            .padding(.horizontal, 16)
            .padding(.bottom, 96) // Extra padding for FAB and tab bar
        }
        .coordinateSpace(name: "scroll")
        .onPreferenceChange(ScrollOffsetPreferenceKey.self) { value in
            scrollOffset = value
        }
        .refreshable {
            onRefresh()
        }
    }
}

/**
 * Trip glass card with Liquid Glass styling.
 */
private struct TripGlassCard: View {
    let trip: Trip

    var body: some View {
        GlassCard(variant: trip.isOngoing ? .route : .visit) {
            VStack(alignment: .leading, spacing: 12) {
                // Header row
                HStack(alignment: .top) {
                    VStack(alignment: .leading, spacing: 8) {
                        HStack(spacing: 8) {
                            Text(trip.displayName)
                                .font(.headline)
                                .fontWeight(.bold)
                                .foregroundColor(.primary)

                            if trip.isOngoing {
                                HStack(spacing: 4) {
                                    Circle()
                                        .fill(Color.coastalPath)
                                        .frame(width: 6, height: 6)
                                    Text("Ongoing")
                                        .font(.caption2)
                                        .fontWeight(.medium)
                                }
                                .padding(.horizontal, 8)
                                .padding(.vertical: 4)
                                .glassBackground(
                                    material: .ultraThin,
                                    tint: .coastalPath,
                                    cornerRadius: 8
                                )
                            }
                        }

                        // Date range
                        Text(formatDateRange(trip))
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                    }

                    Spacer()

                    // Auto-detected badge
                    if trip.isAutoDetected {
                        HStack(spacing: 4) {
                            Image(systemName: "sparkles")
                                .font(.caption2)
                            Text("Auto")
                                .font(.caption2)
                                .fontWeight(.medium)
                        }
                        .padding(.horizontal, 8)
                        .padding(.vertical, 4)
                        .glassBackground(
                            material: .ultraThin,
                            tint: .duskPurple,
                            cornerRadius: 8
                        )
                        .foregroundColor(.duskPurple)
                    }
                }

                // Description
                if let description = trip.description {
                    Text(description)
                        .font(.body)
                        .lineLimit(2)
                        .foregroundColor(.primary)
                }

                // Statistics
                if !trip.summary.isEmpty {
                    HStack(spacing: 8) {
                        Image(systemName: "chart.bar.fill")
                            .font(.caption)
                            .foregroundColor(.blueSlate)
                        Text(trip.summary)
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                }

                // Tags
                if !trip.tags.isEmpty {
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 8) {
                            ForEach(Array(trip.tags.prefix(4)), id: \.self) { tag in
                                HStack(spacing: 4) {
                                    Image(systemName: "tag.fill")
                                        .font(.caption2)
                                    Text(tag)
                                        .font(.caption)
                                }
                                .padding(.horizontal, 8)
                                .padding(.vertical, 4)
                                .glassBackground(
                                    material: .ultraThin,
                                    tint: .coolSteel,
                                    cornerRadius: 6
                                )
                                .foregroundColor(.primary)
                            }

                            if trip.tags.count > 4 {
                                Text("+\(trip.tags.count - 4)")
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                                    .padding(.horizontal, 8)
                            }
                        }
                    }
                }
            }
        }
    }

    private func formatDateRange(_ trip: Trip) -> String {
        let dateFormatter = DateFormatter()
        dateFormatter.dateStyle = .medium
        dateFormatter.timeStyle = .none

        let startDate = Date(timeIntervalSince1970: TimeInterval(trip.startTime.epochSeconds))
        let formattedStart = dateFormatter.string(from: startDate)

        if let endTime = trip.endTime {
            let endDate = Date(timeIntervalSince1970: TimeInterval(endTime.epochSeconds))
            let formattedEnd = dateFormatter.string(from: endDate)
            return "\(formattedStart) - \(formattedEnd)"
        } else {
            return "Started \(formattedStart)"
        }
    }
}

/**
 * Scroll offset preference key.
 */
private struct ScrollOffsetPreferenceKey: PreferenceKey {
    static var defaultValue: CGFloat = 0

    static func reduce(value: inout CGFloat, nextValue: () -> CGFloat) {
        value = nextValue()
    }
}

/**
 * Empty trips view with glass styling.
 */
private struct EmptyTripsView: View {
    let onCreateTrip: () -> Void

    var body: some View {
        VStack(spacing: 24) {
            Image(systemName: "suitcase")
                .font(.system(size: 64))
                .foregroundColor(.coolSteel)

            VStack(spacing: 8) {
                Text("No Trips Yet")
                    .font(.title2)
                    .fontWeight(.semibold)
                    .foregroundColor(.primary)

                Text("Start tracking your adventures")
                    .font(.body)
                    .foregroundColor(.secondary)
            }

            GlassButton(
                title: "Create Trip",
                icon: "plus",
                variant: .filled,
                tint: .coastalPath,
                action: onCreateTrip
            )
        }
        .padding(32)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

/**
 * No results view with glass styling.
 */
private struct NoResultsView: View {
    let message: String

    var body: some View {
        VStack(spacing: 20) {
            Image(systemName: "magnifyingglass")
                .font(.system(size: 48))
                .foregroundColor(.blueSlate)

            VStack(spacing: 8) {
                Text(message)
                    .font(.body)
                    .fontWeight(.medium)
                    .foregroundColor(.primary)

                Text("Try adjusting your filters or search query")
                    .font(.caption)
                    .foregroundColor(.secondary)
                    .multilineTextAlignment(.center)
            }
        }
        .padding(32)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

/**
 * Error view with glass styling.
 */
private struct ErrorView: View {
    let error: String
    let onRetry: () -> Void

    var body: some View {
        VStack(spacing: 20) {
            Image(systemName: "exclamationmark.triangle.fill")
                .font(.system(size: 48))
                .foregroundColor(.driftwood)

            VStack(spacing: 8) {
                Text("Error Loading Trips")
                    .font(.headline)
                    .foregroundColor(.primary)

                Text(error)
                    .font(.body)
                    .foregroundColor(.secondary)
                    .multilineTextAlignment(.center)
            }

            GlassButton(
                title: "Retry",
                icon: "arrow.clockwise",
                variant: .filled,
                tint: .coastalPath,
                action: onRetry
            )
        }
        .padding(32)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

/**
 * ViewModel for TripsView bridging Swift and Kotlin.
 */
class TripsViewModel: ObservableObject {
    private let controller: TripsController
    private var stateObserver: Kotlinx_coroutines_coreJob?

    @Published var trips: [Trip] = []
    @Published var filteredTrips: [Trip] = []
    @Published var ongoingTrips: [Trip] = []
    @Published var completedTrips: [Trip] = []
    @Published var isLoading = false
    @Published var error: String?
    @Published var sortOption: TripsControllerSortOption = .dateDesc
    @Published var filterShowOngoing = true
    @Published var filterShowCompleted = true
    @Published var searchQuery = ""

    init(controller: TripsController) {
        self.controller = controller
        observeState()
    }

    private func observeState() {
        stateObserver = controller.state.subscribe { [weak self] state in
            guard let self = self, let state = state else { return }

            DispatchQueue.main.async {
                self.trips = state.trips
                self.filteredTrips = state.filteredTrips
                self.ongoingTrips = state.ongoingTrips
                self.completedTrips = state.completedTrips
                self.isLoading = state.isLoading
                self.error = state.error
                self.sortOption = state.sortOption
                self.filterShowOngoing = state.filterOptions.showOngoing
                self.filterShowCompleted = state.filterOptions.showCompleted
                self.searchQuery = state.filterOptions.searchQuery
            }
        }
    }

    func loadTrips() {
        controller.loadTrips()
    }

    func refresh() {
        controller.refresh()
    }

    func setSortOption(_ option: TripsControllerSortOption) {
        controller.setSortOption(option: option)
    }

    func toggleOngoingFilter() {
        let newValue = !filterShowOngoing
        controller.setFilterOptions(options: TripsControllerFilterOptions(
            showOngoing: newValue,
            showCompleted: filterShowCompleted,
            searchQuery: searchQuery
        ))
    }

    func toggleCompletedFilter() {
        let newValue = !filterShowCompleted
        controller.setFilterOptions(options: TripsControllerFilterOptions(
            showOngoing: filterShowOngoing,
            showCompleted: newValue,
            searchQuery: searchQuery
        ))
    }

    func showCreateDialog() {
        controller.showCreateDialog()
        // TODO: Implement create trip dialog
        print("Create trip dialog requested")
    }

    deinit {
        stateObserver?.cancel(cause: nil)
    }
}

#Preview {
    Text("TripsView Preview - Requires DI setup")
}
