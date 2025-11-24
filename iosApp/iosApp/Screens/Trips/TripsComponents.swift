import SwiftUI
import Shared

// ScrollOffsetPreferenceKey moved to SharedComponents.swift

/**
 * Empty trips view with glass styling.
 */
struct TripsEmptyView: View {
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
struct NoResultsView: View {
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

// ErrorView moved to SharedComponents.swift

/**
 * ViewModel for TripsView bridging Swift and Kotlin.
 */
class TripsViewModel: ObservableObject {
    private let controller: TripsController
    private var stateObserver: KotlinJob?

    @Published var trips: [Trip] = []
    @Published var filteredTrips: [Trip] = []
    @Published var ongoingTrips: [Trip] = []
    @Published var completedTrips: [Trip] = []
    @Published var isLoading = false
    @Published var error: String?
    @Published var sortOption: Shared.TripsControllerSortOption = .dateDesc
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

    func setSortOption(_ option: Shared.TripsControllerSortOption) {
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
