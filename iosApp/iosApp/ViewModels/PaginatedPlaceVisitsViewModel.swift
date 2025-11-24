import SwiftUI
import Shared
import Combine

/// ViewModel for paginated place visits using PlaceVisitRepository
class PaginatedPlaceVisitsViewModel: ObservableObject {
    private let repository: PlaceVisitRepository
    private let userId: String
    private let config: PaginationConfig
    private var cancellables = Set<AnyCancellable>()

    @Published var visits: [PlaceVisit] = []
    @Published var isLoading: Bool = false
    @Published var isLoadingMore: Bool = false
    @Published var hasMorePages: Bool = true
    @Published var error: String?
    @Published private(set) var currentPage: Int = 0

    private let pageSize: Int

    init(
        repository: PlaceVisitRepository,
        userId: String,
        config: PaginationConfig = .standard
    ) {
        self.repository = repository
        self.userId = userId
        self.config = config
        self.pageSize = config.pageSize
    }

    /// Load the initial page of data
    func loadInitialData() {
        guard !isLoading else { return }

        isLoading = true
        error = nil
        currentPage = 0
        hasMorePages = true

        Task { @MainActor in
            do {
                let offset = currentPage * pageSize
                let fetchedVisits = try await repository.getVisitsByUser(
                    userId: userId,
                    limit: Int32(pageSize),
                    offset: Int32(offset)
                )

                self.visits = fetchedVisits
                self.hasMorePages = fetchedVisits.count >= pageSize
                self.isLoading = false
            } catch {
                self.error = error.localizedDescription
                self.isLoading = false
            }
        }
    }

    /// Load the next page of data
    func loadMoreIfNeeded(currentItem: PlaceVisit?) {
        guard let currentItem = currentItem else { return }
        guard hasMorePages && !isLoadingMore && !isLoading else { return }

        // Check if we're near the end
        guard let index = visits.firstIndex(where: { $0.id == currentItem.id }) else {
            return
        }

        guard index >= visits.count - config.prefetchThreshold else {
            return
        }

        loadMore()
    }

    /// Load more data
    private func loadMore() {
        guard !isLoadingMore else { return }

        isLoadingMore = true
        error = nil

        Task { @MainActor in
            do {
                currentPage += 1
                let offset = currentPage * pageSize
                let fetchedVisits = try await repository.getVisitsByUser(
                    userId: userId,
                    limit: Int32(pageSize),
                    offset: Int32(offset)
                )

                self.visits.append(contentsOf: fetchedVisits)
                self.hasMorePages = fetchedVisits.count >= pageSize
                self.isLoadingMore = false
            } catch {
                self.error = error.localizedDescription
                self.isLoadingMore = false
                self.currentPage -= 1 // Revert page increment on error
            }
        }
    }

    /// Refresh data (pull-to-refresh)
    func refresh() {
        visits = []
        currentPage = 0
        hasMorePages = true
        error = nil
        loadInitialData()
    }
}

/// UI-friendly wrapper for PlaceVisit
struct PlaceVisitItem: Identifiable {
    let visit: PlaceVisit

    var id: String { visit.id }
    var displayName: String { visit.displayName }
    var city: String? { visit.city }
    var country: String? { visit.countryCode }
    var startTime: Date {
        Date(timeIntervalSince1970: TimeInterval(visit.startTime.epochSeconds))
    }
    var endTime: Date {
        Date(timeIntervalSince1970: TimeInterval(visit.endTime.epochSeconds))
    }
    var category: String { visit.category.name }
    var isFavorite: Bool { visit.isFavorite }

    var durationText: String {
        let totalMinutes = Int(visit.duration / 60)
        let hours = totalMinutes / 60
        let minutes = totalMinutes % 60

        if hours > 0 {
            return "\(hours)h \(minutes)m"
        } else {
            return "\(minutes)m"
        }
    }

    var categoryIcon: String {
        switch category {
        case "HOME": return "house.fill"
        case "WORK": return "briefcase.fill"
        case "FOOD": return "fork.knife"
        case "SHOPPING": return "cart.fill"
        case "FITNESS": return "figure.run"
        case "ENTERTAINMENT": return "theatermasks.fill"
        case "TRAVEL": return "airplane"
        case "HEALTHCARE": return "cross.case.fill"
        case "EDUCATION": return "book.fill"
        case "RELIGIOUS": return "building.columns.fill"
        case "SOCIAL": return "person.3.fill"
        case "OUTDOOR": return "tree.fill"
        case "SERVICE": return "wrench.and.screwdriver.fill"
        default: return "mappin.circle.fill"
        }
    }

    var categoryColor: Color {
        switch category {
        case "HOME": return .blue
        case "WORK": return .purple
        case "FOOD": return .orange
        case "SHOPPING": return .green
        case "FITNESS": return .red
        case "ENTERTAINMENT": return .pink
        default: return .gray
        }
    }

    init(visit: PlaceVisit) {
        self.visit = visit
    }
}
