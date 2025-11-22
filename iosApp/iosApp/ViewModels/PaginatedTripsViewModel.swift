import SwiftUI
import shared
import Combine

/// ViewModel for paginated trips using TripRepository
class PaginatedTripsViewModel: ObservableObject {
    private let repository: TripRepository
    private let userId: String
    private var cancellables = Set<AnyCancellable>()

    @Published var trips: [Trip] = []
    @Published var ongoingTrips: [Trip] = []
    @Published var completedTrips: [Trip] = []
    @Published var isLoading: Bool = false
    @Published var error: String?

    init(repository: TripRepository, userId: String) {
        self.repository = repository
        self.userId = userId
    }

    /// Load all trips for the user
    /// Note: TripRepository doesn't have pagination yet, so we load all at once
    func loadTrips() {
        guard !isLoading else { return }

        isLoading = true
        error = nil

        Task { @MainActor in
            do {
                let fetchedTrips = try await repository.getTripsForUser(userId: userId)

                self.trips = fetchedTrips
                self.ongoingTrips = fetchedTrips.filter { $0.isOngoing }
                self.completedTrips = fetchedTrips.filter { !$0.isOngoing }
                    .sorted { $0.startTime.epochSeconds > $1.startTime.epochSeconds }
                self.isLoading = false
            } catch {
                self.error = error.localizedDescription
                self.isLoading = false
            }
        }
    }

    /// Refresh trips (pull-to-refresh)
    func refresh() {
        trips = []
        ongoingTrips = []
        completedTrips = []
        error = nil
        loadTrips()
    }

    /// Get ongoing trips count
    var ongoingCount: Int {
        ongoingTrips.count
    }

    /// Get completed trips count
    var completedCount: Int {
        completedTrips.count
    }

    /// Get total trips count
    var totalCount: Int {
        trips.count
    }
}

/// UI-friendly wrapper for Trip with pagination support
/// When TripRepository adds pagination, this will be extended
struct TripItem: Identifiable {
    let trip: Trip

    var id: String { trip.id }
    var displayName: String { trip.displayName }
    var description: String? { trip.description }
    var isOngoing: Bool { trip.isOngoing }
    var isAutoDetected: Bool { trip.isAutoDetected }
    var tags: [String] { trip.tags }
    var summary: String { trip.summary }

    var startDate: Date {
        Date(timeIntervalSince1970: TimeInterval(trip.startTime.epochSeconds))
    }

    var endDate: Date? {
        guard let endTime = trip.endTime else { return nil }
        return Date(timeIntervalSince1970: TimeInterval(endTime.epochSeconds))
    }

    var dateRangeText: String {
        let formatter = DateFormatter()
        formatter.dateStyle = .medium
        formatter.timeStyle = .none

        let start = formatter.string(from: startDate)

        if let end = endDate {
            let endString = formatter.string(from: end)
            return "\(start) - \(endString)"
        } else {
            return "Started \(start)"
        }
    }

    var statsText: String {
        var parts: [String] = []

        if trip.countriesVisited.count > 0 {
            parts.append("\(trip.countriesVisited.count) countries")
        }

        if trip.visitedPlaceCount > 0 {
            parts.append("\(trip.visitedPlaceCount) places")
        }

        if trip.totalDistanceMeters > 0 {
            let km = Int(trip.totalDistanceMeters / 1000)
            parts.append("\(km) km")
        }

        return parts.joined(separator: " • ")
    }

    init(trip: Trip) {
        self.trip = trip
    }
}
