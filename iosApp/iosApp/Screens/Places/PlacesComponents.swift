import SwiftUI
import shared
import Combine

/// Filter sheet
struct PlacesFilterSheet: View {
    let currentFilters: PlaceFilters
    let onApply: (PlaceFilters) -> Void
    let onCancel: () -> Void

    @State private var minVisits: Int
    @State private var categories: Set<String>

    init(currentFilters: PlaceFilters, onApply: @escaping (PlaceFilters) -> Void, onCancel: @escaping () -> Void) {
        self.currentFilters = currentFilters
        self.onApply = onApply
        self.onCancel = onCancel
        _minVisits = State(initialValue: currentFilters.minVisits)
        _categories = State(initialValue: Set(currentFilters.categories))
    }

    var body: some View {
        NavigationView {
            Form {
                Section("Minimum Visits") {
                    Stepper("\(minVisits) visits", value: $minVisits, in: 1...100)
                }

                Section("Categories") {
                    ForEach(PlaceCategory.allCases, id: \.self) { category in
                        Toggle(category.displayName, isOn: Binding(
                            get: { categories.contains(category.rawValue) },
                            set: { isOn in
                                if isOn {
                                    categories.insert(category.rawValue)
                                } else {
                                    categories.remove(category.rawValue)
                                }
                            }
                        ))
                    }
                }
            }
            .navigationTitle("Filter Places")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Cancel", action: onCancel)
                }

                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Apply") {
                        let filters = PlaceFilters(
                            minVisits: minVisits,
                            categories: Array(categories)
                        )
                        onApply(filters)
                    }
                }
            }
        }
    }
}

/// Sort sheet
struct PlacesSortSheet: View {
    let currentSort: PlaceSortOption
    let onApply: (PlaceSortOption) -> Void
    let onCancel: () -> Void

    var body: some View {
        NavigationView {
            List {
                ForEach(PlaceSortOption.allCases, id: \.self) { option in
                    Button(action: {
                        onApply(option)
                    }) {
                        HStack {
                            Text(option.displayName)
                                .foregroundColor(.primary)

                            Spacer()

                            if option == currentSort {
                                Image(systemName: "checkmark")
                                    .foregroundColor(.blue)
                            }
                        }
                    }
                }
            }
            .navigationTitle("Sort By")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Done", action: onCancel)
                }
            }
        }
    }
}

/// Places view model
class PlacesViewModel: ObservableObject {
    private let controller: PlacesController
    private var stateObserver: Kotlinx_coroutines_coreJob?

    @Published var places: [PlaceItem] = []
    @Published var isLoading: Bool = false
    @Published var error: String?
    @Published var searchQuery: String = ""
    @Published var filters: PlaceFilters = PlaceFilters(minVisits: 1, categories: [])
    @Published var sortOption: PlaceSortOption = .mostVisited
    @Published var hasActiveFilters: Bool = false

    init(controller: PlacesController) {
        self.controller = controller
        observeState()
        setupSearchObserver()
    }

    deinit {
        stateObserver?.cancel(cause: nil)
    }

    private func observeState() {
        stateObserver = controller.state.subscribe { [weak self] state in
            guard let self = self, let state = state else { return }

            DispatchQueue.main.async {
                self.isLoading = state.isLoading
                self.error = state.error?.userMessage
                self.places = state.places.map { PlaceItem(from: $0) }
                self.hasActiveFilters = state.hasActiveFilters
            }
        }
    }

    private func setupSearchObserver() {
        // Observe search query changes
        $searchQuery
            .debounce(for: .milliseconds(300), scheduler: RunLoop.main)
            .sink { [weak self] query in
                self?.controller.search(query: query)
            }
            .store(in: &cancellables)
    }

    private var cancellables = Set<AnyCancellable>()

    func refresh() {
        controller.refresh()
    }

    func selectPlace(_ place: PlaceItem) {
        controller.selectPlace(placeId: place.id)
    }

    func clearSearch() {
        searchQuery = ""
        controller.clearSearch()
    }

    func applyFilters(_ filters: PlaceFilters) {
        self.filters = filters
        controller.applyFilters(
            minVisits: Int32(filters.minVisits),
            categories: filters.categories
        )
    }

    func setSortOption(_ option: PlaceSortOption) {
        self.sortOption = option
        controller.setSortOption(option: option.toKotlin())
    }
}

/// Place item model for SwiftUI
struct PlaceItem: Identifiable {
    let id: String
    let name: String?
    let address: String?
    let visitCount: Int
    let totalDuration: Int64?
    let category: String
    let isFavorite: Bool

    var categoryIcon: String {
        PlaceCategory(rawValue: category)?.icon ?? "mappin.circle"
    }

    var categoryColor: Color {
        PlaceCategory(rawValue: category)?.color ?? .gray
    }

    init(from place: com.po4yka.trailglass.domain.model.PlaceVisit) {
        self.id = place.id
        self.name = place.name
        self.address = place.address
        self.visitCount = Int(place.visitCount ?? 1)
        self.totalDuration = place.duration?.toInt64()
        self.category = place.category?.rawValue ?? "unknown"
        self.isFavorite = place.isFavorite
    }
}

/// Place filters
struct PlaceFilters {
    let minVisits: Int
    let categories: [String]
}

/// Sort options
enum PlaceSortOption: String, CaseIterable {
    case mostVisited = "most_visited"
    case leastVisited = "least_visited"
    case alphabetical = "alphabetical"
    case recentlyVisited = "recently_visited"

    var displayName: String {
        switch self {
        case .mostVisited: return "Most Visited"
        case .leastVisited: return "Least Visited"
        case .alphabetical: return "Alphabetical"
        case .recentlyVisited: return "Recently Visited"
        }
    }

    func toKotlin() -> com.po4yka.trailglass.feature.places.PlaceSortOption {
        switch self {
        case .mostVisited: return .mostVisited
        case .leastVisited: return .leastVisited
        case .alphabetical: return .alphabetical
        case .recentlyVisited: return .recentlyVisited
        }
    }
}

/// Place categories
enum PlaceCategory: String, CaseIterable {
    case home
    case work
    case restaurant
    case shopping
    case entertainment
    case travel
    case other
    case unknown

    var displayName: String {
        rawValue.capitalized
    }

    var icon: String {
        switch self {
        case .home: return "house.fill"
        case .work: return "briefcase.fill"
        case .restaurant: return "fork.knife"
        case .shopping: return "cart.fill"
        case .entertainment: return "theatermasks.fill"
        case .travel: return "airplane"
        case .other: return "mappin.circle"
        case .unknown: return "questionmark.circle"
        }
    }

    var color: Color {
        switch self {
        case .home: return .blue
        case .work: return .purple
        case .restaurant: return .orange
        case .shopping: return .green
        case .entertainment: return .pink
        case .travel: return .cyan
        case .other: return .gray
        case .unknown: return .gray
        }
    }
}
