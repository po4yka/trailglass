import SwiftUI
import shared

/**
 * SwiftUI trips screen with TripsController integration.
 * Shows all trips with filtering, sorting, and navigation to trip details.
 */
struct TripsView: View {
    let appComponent: AppComponent
    @StateObject private var viewModel: TripsViewModel

    init(appComponent: AppComponent) {
        self.appComponent = appComponent
        _viewModel = StateObject(wrappedValue: TripsViewModel(controller: appComponent.tripsController))
    }

    var body: some View {
        NavigationStack {
            ZStack {
                if viewModel.isLoading && viewModel.trips.isEmpty {
                    ProgressView()
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
                        onRefresh: { viewModel.refresh() }
                    )
                }

                // Floating action button
                VStack {
                    Spacer()
                    HStack {
                        Spacer()
                        Button(action: { viewModel.showCreateDialog() }) {
                            Image(systemName: "plus")
                                .font(.title2)
                                .foregroundColor(.white)
                                .frame(width: 56, height: 56)
                                .background(Color.blue)
                                .clipShape(Circle())
                                .shadow(radius: 4)
                        }
                        .padding(.trailing, 16)
                        .padding(.bottom, 16)
                    }
                }
            }
            .navigationTitle("Trips")
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Menu {
                        // Sort options
                        Menu("Sort By") {
                            Button(action: { viewModel.setSortOption(.dateDesc) }) {
                                Label("Newest First", systemImage: viewModel.sortOption == .dateDesc ? "checkmark" : "")
                            }
                            Button(action: { viewModel.setSortOption(.dateAsc) }) {
                                Label("Oldest First", systemImage: viewModel.sortOption == .dateAsc ? "checkmark" : "")
                            }
                            Button(action: { viewModel.setSortOption(.nameAsc) }) {
                                Label("Name A-Z", systemImage: viewModel.sortOption == .nameAsc ? "checkmark" : "")
                            }
                            Button(action: { viewModel.setSortOption(.durationDesc) }) {
                                Label("Longest Duration", systemImage: viewModel.sortOption == .durationDesc ? "checkmark" : "")
                            }
                            Button(action: { viewModel.setSortOption(.distanceDesc) }) {
                                Label("Farthest Distance", systemImage: viewModel.sortOption == .distanceDesc ? "checkmark" : "")
                            }
                        }

                        // Filter options
                        Menu("Filter") {
                            Button(action: { viewModel.toggleOngoingFilter() }) {
                                Label("Ongoing Trips", systemImage: viewModel.filterShowOngoing ? "checkmark" : "")
                            }
                            Button(action: { viewModel.toggleCompletedFilter() }) {
                                Label("Completed Trips", systemImage: viewModel.filterShowCompleted ? "checkmark" : "")
                            }
                        }
                    } label: {
                        Image(systemName: "ellipsis.circle")
                    }
                }

                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: { viewModel.refresh() }) {
                        Image(systemName: "arrow.clockwise")
                    }
                }
            }
            .searchable(text: $viewModel.searchQuery, prompt: "Search trips...")
        }
        .onAppear {
            viewModel.loadTrips()
        }
    }
}

/**
 * Trips content with list.
 */
private struct TripsContent: View {
    let trips: [Trip]
    let ongoingTrips: [Trip]
    let appComponent: AppComponent
    let onRefresh: () -> Void

    var body: some View {
        ScrollView {
            LazyVStack(spacing: 12) {
                // Header
                HStack {
                    VStack(alignment: .leading, spacing: 4) {
                        Text("Trips")
                            .font(.largeTitle)
                            .fontWeight(.bold)

                        Text("\(trips.count) total")
                            .font(.body)
                            .foregroundColor(.secondary)
                    }

                    Spacer()

                    Button(action: onRefresh) {
                        Image(systemName: "arrow.clockwise")
                    }
                }
                .padding(.horizontal, 16)
                .padding(.top, 16)

                // Group trips: ongoing first, then completed
                let ongoing = trips.filter { $0.isOngoing }
                let completed = trips.filter { !$0.isOngoing }

                // Ongoing trips section
                if !ongoing.isEmpty {
                    SectionHeader(text: "Ongoing")
                        .padding(.horizontal, 16)
                        .padding(.top, 8)

                    ForEach(ongoing, id: \.id) { trip in
                        NavigationLink(destination: TripDetailView(tripId: trip.id, appComponent: appComponent)) {
                            TripCard(trip: trip)
                        }
                        .buttonStyle(PlainButtonStyle())
                        .padding(.horizontal, 16)
                    }
                }

                // Completed trips section
                if !completed.isEmpty {
                    SectionHeader(text: ongoing.isEmpty ? "All Trips" : "Past Trips")
                        .padding(.horizontal, 16)
                        .padding(.top, 8)

                    ForEach(completed, id: \.id) { trip in
                        NavigationLink(destination: TripDetailView(tripId: trip.id, appComponent: appComponent)) {
                            TripCard(trip: trip)
                        }
                        .buttonStyle(PlainButtonStyle())
                        .padding(.horizontal, 16)
                    }
                }
            }
            .padding(.bottom, 80) // Extra padding for FAB
        }
    }
}

/**
 * Section header.
 */
private struct SectionHeader: View {
    let text: String

    var body: some View {
        Text(text)
            .font(.title3)
            .fontWeight(.semibold)
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.vertical, 8)
    }
}

/**
 * Trip card.
 */
private struct TripCard: View {
    let trip: Trip

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            // Header row
            HStack(alignment: .top) {
                VStack(alignment: .leading, spacing: 8) {
                    HStack(spacing: 8) {
                        Text(trip.displayName)
                            .font(.title3)
                            .fontWeight(.bold)
                            .foregroundColor(.primary)

                        if trip.isOngoing {
                            Text("Ongoing")
                                .font(.caption)
                                .padding(.horizontal, 8)
                                .padding(.vertical, 4)
                                .background(Color.blue)
                                .foregroundColor(.white)
                                .cornerRadius(4)
                        }
                    }

                    // Date range
                    Text(formatDateRange(trip))
                        .font(.body)
                        .foregroundColor(.secondary)
                }

                Spacer()

                // Auto-detected badge
                if trip.isAutoDetected {
                    Text("Auto")
                        .font(.caption)
                        .padding(.horizontal, 8)
                        .padding(.vertical, 4)
                        .background(Color.purple.opacity(0.2))
                        .foregroundColor(.purple)
                        .cornerRadius(4)
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
                Text(trip.summary)
                    .font(.caption)
                    .foregroundColor(.secondary)
            }

            // Tags
            if !trip.tags.isEmpty {
                HStack(spacing: 8) {
                    ForEach(Array(trip.tags.prefix(3)), id: \.self) { tag in
                        HStack(spacing: 4) {
                            Image(systemName: "tag")
                                .font(.caption2)
                            Text(tag)
                                .font(.caption)
                        }
                        .padding(.horizontal, 8)
                        .padding(.vertical, 4)
                        .background(Color(.systemGray5))
                        .cornerRadius(4)
                    }

                    if trip.tags.count > 3 {
                        Text("+\(trip.tags.count - 3)")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                }
            }
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(trip.isOngoing ? Color.blue.opacity(0.1) : Color(.systemGray6))
        .cornerRadius(12)
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
 * Empty trips view.
 */
private struct EmptyTripsView: View {
    let onCreateTrip: () -> Void

    var body: some View {
        VStack(spacing: 16) {
            Image(systemName: "suitcase")
                .font(.system(size: 64))
                .foregroundColor(.secondary)

            Text("No Trips Yet")
                .font(.title2)
                .foregroundColor(.secondary)

            Text("Start tracking your adventures")
                .font(.body)
                .foregroundColor(.secondary)

            Button(action: onCreateTrip) {
                HStack {
                    Image(systemName: "plus")
                    Text("Create Trip")
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 8)
            }
            .buttonStyle(.borderedProminent)
        }
    }
}

/**
 * No results view (when filters yield no results).
 */
private struct NoResultsView: View {
    let message: String

    var body: some View {
        VStack(spacing: 16) {
            Image(systemName: "magnifyingglass")
                .font(.system(size: 48))
                .foregroundColor(.secondary)

            Text(message)
                .font(.body)
                .foregroundColor(.secondary)

            Text("Try adjusting your filters or search query")
                .font(.caption)
                .foregroundColor(.secondary)
        }
    }
}

/**
 * Error view.
 */
private struct ErrorView: View {
    let error: String
    let onRetry: () -> Void

    var body: some View {
        VStack(spacing: 16) {
            Image(systemName: "exclamationmark.triangle")
                .font(.system(size: 48))
                .foregroundColor(.red)

            Text("Error Loading Trips")
                .font(.headline)

            Text(error)
                .font(.body)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)

            Button("Retry", action: onRetry)
                .buttonStyle(.borderedProminent)
        }
        .padding()
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
