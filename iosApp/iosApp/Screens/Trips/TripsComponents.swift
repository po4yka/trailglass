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
    @Published var sortOption: TripsController.SortOption = .dateDesc
    @Published var filterShowOngoing = true
    @Published var filterShowCompleted = true
    @Published var searchQuery = ""
    @Published var showCreateTripDialog = false

    init(controller: TripsController) {
        self.controller = controller
        observeState()
    }

    private func observeState() {
        stateObserver = controller.state.subscribe { [weak self] (state: TripsState?) in
            guard let self = self, let state = state else { return }

            Task { @MainActor in
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

    func setSortOption(_ option: TripsController.SortOption) {
        controller.setSortOption(option: option)
    }

    func toggleOngoingFilter() {
        guard let state = controller.state.value as? TripsState else { return }
        let newOptions = state.filterOptions.doCopy(
            showOngoing: !filterShowOngoing,
            showCompleted: state.filterOptions.showCompleted,
            searchQuery: state.filterOptions.searchQuery
        )
        controller.setFilterOptions(options: newOptions)
    }

    func toggleCompletedFilter() {
        guard let state = controller.state.value as? TripsState else { return }
        let newOptions = state.filterOptions.doCopy(
            showOngoing: state.filterOptions.showOngoing,
            showCompleted: !filterShowCompleted,
            searchQuery: state.filterOptions.searchQuery
        )
        controller.setFilterOptions(options: newOptions)
    }

    func showCreateDialog() {
        showCreateTripDialog = true
    }

    deinit {
        stateObserver?.cancel(cause: nil)
    }

    func createTrip(name: String, description: String?) {
        // Create a new Trip object with current time as start
        let now = Kotlinx_datetimeInstant.companion.fromEpochMilliseconds(
            epochMilliseconds: Int64(Date().timeIntervalSince1970 * 1000)
        )
        let newTrip = Trip(
            id: "", // Will be generated by use case
            name: name,
            startTime: now,
            endTime: nil,
            primaryCountry: nil,
            isOngoing: true,
            userId: "", // Will be set by use case
            totalDistanceMeters: 0.0,
            visitedPlaceCount: 0,
            countriesVisited: [],
            citiesVisited: [],
            description: description,
            coverPhotoUri: nil,
            isPublic: false,
            tags: [],
            isAutoDetected: false,
            detectionConfidence: 0.0,
            createdAt: nil,
            updatedAt: nil
        )
        controller.createTrip(trip: newTrip)
    }

    func cancelCreateTrip() {
        showCreateTripDialog = false
    }
}

/**
 * Dialog for creating a new trip.
 */
struct CreateTripDialog: View {
    @Binding var isPresented: Bool
    let onCreate: (String, String?) -> Void
    let onCancel: () -> Void

    @State private var tripName = ""
    @State private var tripDescription = ""

    var body: some View {
        NavigationView {
            Form {
                Section("Trip Details") {
                    TextField("Trip Name", text: $tripName)
                        .autocapitalization(.words)

                    TextField("Description (optional)", text: $tripDescription)
                        .autocapitalization(.sentences)
                }
            }
            .navigationTitle("Create New Trip")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Cancel", action: onCancel)
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Create") {
                        if !tripName.trimmingCharacters(in: .whitespaces).isEmpty {
                            let description = tripDescription.trimmingCharacters(in: .whitespaces).isEmpty ? nil : tripDescription
                            onCreate(tripName, description)
                            tripName = ""
                            tripDescription = ""
                        }
                    }
                    .disabled(tripName.trimmingCharacters(in: .whitespaces).isEmpty)
                }
            }
        }
    }
}
