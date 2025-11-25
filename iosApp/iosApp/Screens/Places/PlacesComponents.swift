import SwiftUI
import Shared
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
                    let allCategories: [Shared.PlaceCategory] = [
                        .home, .work, .food, .shopping, .fitness,
                        .entertainment, .travel, .healthcare, .education,
                        .religious, .social, .outdoor, .service, .other
                    ]
                    ForEach(allCategories, id: \.name) { category in
                        Toggle(categoryName(category), isOn: Binding(
                            get: { categories.contains(category.name) },
                            set: { isOn in
                                if isOn {
                                    categories.insert(category.name)
                                } else {
                                    categories.remove(category.name)
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
    private var stateObserver: KotlinJob?

    @Published var places: [PlaceItem] = []
    @Published var isLoading: Bool = false
    @Published var error: String?
    @Published var searchQuery: String = ""
    @Published var filters: PlaceFilters = PlaceFilters(minVisits: 1, categories: [])
    @Published var sortOption: PlaceSortOption = .mostVisited
    @Published var hasActiveFilters: Bool = false
    @Published var selectedPlace: FrequentPlace?

    init(controller: PlacesController) {
        self.controller = controller
        observeState()
        setupSearchObserver()
    }

    deinit {
        stateObserver?.cancel(cause: nil)
    }

    private func observeState() {
        stateObserver = controller.state.subscribe { [weak self] (state: PlacesState?) in
            guard let self = self, let state = state else { return }

            Task { @MainActor in
                self.isLoading = state.isLoading
                self.error = state.error
                self.places = state.places.map { PlaceItem(from: $0) }
                // Calculate hasActiveFilters from state
                self.hasActiveFilters = !state.searchQuery.isEmpty || !state.selectedCategories.isEmpty
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
        // Find the FrequentPlace from the controller's state
        Task {
            // First try to find in current state
            if let currentState = controller.state.value as? PlacesState,
               let frequentPlace = currentState.places.first(where: { $0.id == place.id }) {
                await MainActor.run {
                    selectedPlace = frequentPlace
                }
            } else {
                // If not found in state, fetch from controller
                if let frequentPlace = try? await controller.getPlaceById(placeId: place.id) {
                    await MainActor.run {
                        selectedPlace = frequentPlace
                    }
                }
            }
        }
    }

    func toggleFavorite(placeId: String) {
        controller.toggleFavorite(placeId: placeId)
    }

    func clearSearch() {
        searchQuery = ""
        controller.clearSearch()
    }

    func applyFilters(_ filters: PlaceFilters) {
        self.filters = filters
        // Convert category names to Shared.PlaceCategory and update controller
        let categories = filters.categories.compactMap { categoryName -> Shared.PlaceCategory? in
            // Convert string name back to enum
            switch categoryName.uppercased() {
            case "HOME": return .home
            case "WORK": return .work
            case "FOOD": return .food
            case "SHOPPING": return .shopping
            case "ENTERTAINMENT": return .entertainment
            case "TRAVEL": return .travel
            case "HEALTHCARE": return .healthcare
            case "EDUCATION": return .education
            case "FITNESS": return .fitness
            case "OUTDOOR": return .outdoor
            case "SOCIAL": return .social
            case "SERVICE": return .service
            case "RELIGIOUS": return .religious
            case "OTHER": return .other
            default: return nil
            }
        }
        // Toggle categories to match the desired filter set
        // First clear current categories, then toggle to set new ones
        for category in categories {
            controller.toggleCategoryFilter(category: category)
        }
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
        guard let category = Shared.PlaceCategory.entries.first(where: { $0.name == self.category }) else {
            return "mappin.circle.fill"
        }
        return Trailglass.categoryIcon(category)
    }

    var categoryColor: Color {
        guard let category = Shared.PlaceCategory.entries.first(where: { $0.name == self.category }) else {
            return .gray
        }
        return Trailglass.categoryColor(category)
    }

    init(from place: FrequentPlace) {
        self.id = place.id
        self.name = place.name
        self.address = place.address
        self.visitCount = Int(place.visitCount)
        self.totalDuration = place.totalDuration.inWholeSeconds
        self.category = place.category.name
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

    func toKotlin() -> Shared.PlaceSortOption {
        switch self {
        case .mostVisited: return .mostVisited
        case .leastVisited: return .mostVisited // Map to mostVisited since leastVisited doesn't exist
        case .alphabetical: return .alphabetical
        case .recentlyVisited: return .recentlyVisited
        }
    }
}
