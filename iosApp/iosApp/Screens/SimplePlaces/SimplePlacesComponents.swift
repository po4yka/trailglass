import SwiftUI
import Shared
import Combine

/// List of frequent places with glass styling
struct PlacesList: View {
    let places: [FrequentPlaceItem]
    let onPlaceTap: (FrequentPlaceItem) -> Void
    @Binding var scrollOffset: CGFloat
    @Binding var selectedCategories: Set<String>

    // Common categories for filtering
    private let categories: [(id: String, label: String, icon: String, color: Color)] = [
        ("HOME", "Home", "house.fill", .coolSteel),
        ("WORK", "Work", "briefcase.fill", .blueSlate),
        ("FOOD", "Food", "fork.knife", .morningCategory),
        ("SHOPPING", "Shopping", "cart.fill", .seaGlass),
        ("FITNESS", "Fitness", "figure.run", .coastalPath),
        ("ENTERTAINMENT", "Entertainment", "theatermasks.fill", .duskPurple)
    ]

    var filteredPlaces: [FrequentPlaceItem] {
        if selectedCategories.isEmpty {
            return places
        }
        return places.filter { selectedCategories.contains($0.category) }
    }

    var body: some View {
        ScrollView {
            GeometryReader { geometry in
                Color.clear.preference(
                    key: ScrollOffsetPreferenceKey.self,
                    value: geometry.frame(in: .named("scroll")).minY
                )
            }
            .frame(height: 0)

            VStack(spacing: 16) {
                // Category filter chips
                ScrollableGlassGroup {
                    ForEach(categories, id: \.id) { category in
                        GlassFilterChip(
                            label: category.label,
                            icon: category.icon,
                            isSelected: selectedCategories.contains(category.id),
                            tint: category.color
                        ) {
                            if selectedCategories.contains(category.id) {
                                selectedCategories.remove(category.id)
                            } else {
                                selectedCategories.insert(category.id)
                            }
                        }
                    }
                }

                // Places list
                GlassEffectGroup(spacing: 12, padding: 16) {
                    VStack(spacing: 12) {
                        ForEach(filteredPlaces) { place in
                            Button {
                                onPlaceTap(place)
                            } label: {
                                PlaceGlassCard(place: place)
                            }
                            .buttonStyle(PlainButtonStyle())
                        }
                    }
                }
                .padding(.horizontal, 16)
            }
            .padding(.bottom, 96) // Extra padding for tab bar
        }
        .coordinateSpace(name: "scroll")
        .onPreferenceChange(ScrollOffsetPreferenceKey.self) { value in
            scrollOffset = value
        }
    }
}

/// Scroll offset preference key
struct ScrollOffsetPreferenceKey: PreferenceKey {
    static var defaultValue: CGFloat = 0

    static func reduce(value: inout CGFloat, nextValue: () -> CGFloat) {
        value = nextValue()
    }
}

/// Places view model
class SimplePlacesViewModel: ObservableObject {
    private let controller: PlacesController
    private var stateObserver: Kotlinx_coroutines_coreJob?
    private var frequentPlaces: [FrequentPlace] = []
    private var searchDebouncer: Timer?

    @Published var places: [FrequentPlaceItem] = []
    @Published var isLoading: Bool = false
    @Published var error: String?
    @Published var selectedPlace: FrequentPlaceItem?
    @Published var searchQuery: String = ""

    init(controller: PlacesController) {
        self.controller = controller
        observeState()
    }

    deinit {
        stateObserver?.cancel(cause: nil)
        searchDebouncer?.invalidate()
    }

    private func observeState() {
        stateObserver = controller.state.subscribe { [weak self] state in
            guard let self = self, let state = state else { return }

            DispatchQueue.main.async {
                self.isLoading = state.isLoading
                self.error = state.error
                self.frequentPlaces = state.places
                self.places = state.places.map { FrequentPlaceItem(from: $0) }

                // Update search query from controller if it was cleared externally
                if state.searchQuery.isEmpty && !self.searchQuery.isEmpty {
                    self.searchQuery = ""
                }
            }
        }
    }

    func refresh() {
        controller.refresh()
    }

    func toggleFavorite(_ placeId: String) {
        controller.toggleFavorite(placeId: placeId)
    }

    func getFrequentPlace(for item: FrequentPlaceItem) -> FrequentPlace? {
        return frequentPlaces.first { $0.id == item.id }
    }

    func onSearchQueryChanged(_ newQuery: String) {
        // Debounce search to avoid too many updates
        searchDebouncer?.invalidate()
        searchDebouncer = Timer.scheduledTimer(withTimeInterval: 0.3, repeats: false) { [weak self] _ in
            self?.controller.search(query: newQuery)
        }
    }
}

/// Frequent place item model for SwiftUI
struct FrequentPlaceItem: Identifiable {
    let id: String
    let displayName: String
    let city: String?
    let visitCount: Int32
    let averageDurationMinutes: Int
    let category: String
    let significance: String
    let isFavorite: Bool

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
        case "TRAVEL": return .cyan
        case "HEALTHCARE": return .red
        case "EDUCATION": return .indigo
        case "RELIGIOUS": return .brown
        case "SOCIAL": return .mint
        case "OUTDOOR": return .green
        case "SERVICE": return .gray
        default: return .gray
        }
    }

    var significanceLabel: String {
        switch significance {
        case "PRIMARY": return "Primary"
        case "FREQUENT": return "Frequent"
        case "OCCASIONAL": return "Occasional"
        case "RARE": return "Rare"
        default: return ""
        }
    }

    var significanceColor: Color {
        switch significance {
        case "PRIMARY": return .purple
        case "FREQUENT": return .blue
        case "OCCASIONAL": return .orange
        case "RARE": return .gray
        default: return .gray
        }
    }

    init(from frequentPlace: FrequentPlace) {
        self.id = frequentPlace.id
        self.displayName = frequentPlace.displayName
        self.city = frequentPlace.city
        self.visitCount = frequentPlace.visitCount
        // Convert Duration to minutes
        self.averageDurationMinutes = Int(frequentPlace.averageDuration.toDouble(unit: .minutes))
        self.category = frequentPlace.category.name
        self.significance = frequentPlace.significance.name
        self.isFavorite = frequentPlace.isFavorite
    }
}
