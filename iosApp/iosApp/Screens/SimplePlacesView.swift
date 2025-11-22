import SwiftUI
import shared
import Combine

/// Simplified Places screen with Liquid Glass components
struct SimplePlacesView: View {
    @StateObject private var viewModel: SimplePlacesViewModel
    @State private var scrollOffset: CGFloat = 0
    @State private var selectedCategories: Set<String> = []

    init(placesController: PlacesController) {
        _viewModel = StateObject(wrappedValue: SimplePlacesViewModel(controller: placesController))
    }

    var body: some View {
        ZStack {
            Color.backgroundLight.ignoresSafeArea()

            VStack(spacing: 0) {
                // Large flexible navigation bar with gradient
                LargeFlexibleNavigationBar(
                    title: "Places",
                    scrollOffset: scrollOffset,
                    actions: [
                        NavigationAction(icon: "magnifyingglass") {
                            // Search action
                        },
                        NavigationAction(icon: "arrow.clockwise") {
                            viewModel.refresh()
                        }
                    ],
                    subtitle: {
                        Text(placesSubtitle)
                    },
                    backgroundContent: {
                        HeroGradientBackground(
                            startColor: .lightCyan,
                            endColor: .seaGlass
                        )
                    }
                )

                if viewModel.isLoading {
                    GlassLoadingIndicator(variant: .pulsing, color: .seaGlass)
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else if let error = viewModel.error {
                    PlacesErrorView(error: error) {
                        viewModel.refresh()
                    }
                } else if viewModel.places.isEmpty && viewModel.searchQuery.isEmpty {
                    EmptyPlacesView()
                } else if viewModel.places.isEmpty && !viewModel.searchQuery.isEmpty {
                    NoSearchResultsView(query: viewModel.searchQuery)
                } else {
                    PlacesList(
                        places: viewModel.places,
                        onPlaceTap: { place in
                            viewModel.selectedPlace = place
                        },
                        scrollOffset: $scrollOffset,
                        selectedCategories: $selectedCategories
                    )
                }
            }

            .sheet(item: $viewModel.selectedPlace) { selectedPlace in
                if let frequentPlace = viewModel.getFrequentPlace(for: selectedPlace) {
                    NavigationView {
                        PlaceDetailView(
                            place: frequentPlace,
                            onToggleFavorite: {
                                viewModel.toggleFavorite(selectedPlace.id)
                            },
                            onDismiss: {
                                viewModel.selectedPlace = nil
                            }
                        )
                    }
                }
            }
        }
        .navigationBarHidden(true)
    }

    private var placesSubtitle: String {
        let count = viewModel.places.count
        return "\(count) places visited"
    }
}

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

/// Place glass card with Liquid Glass styling
struct PlaceGlassCard: View {
    let place: FrequentPlaceItem

    var body: some View {
        GlassCard(variant: .visit) {
            HStack(spacing: 12) {
                // Category icon with glass background
                Image(systemName: place.categoryIcon)
                    .foregroundColor(place.categoryColor)
                    .font(.title2)
                    .frame(width: 44, height: 44)
                    .glassBackground(
                        material: .ultraThin,
                        tint: place.categoryColor,
                        cornerRadius: 10
                    )

                VStack(alignment: .leading, spacing: 6) {
                    // Name and favorite
                    HStack(spacing: 6) {
                        Text(place.displayName)
                            .font(.headline)
                            .foregroundColor(.primary)
                            .lineLimit(1)

                        if place.isFavorite {
                            Image(systemName: "star.fill")
                                .foregroundColor(.warning)
                                .font(.caption)
                        }
                    }

                    // City/location
                    if let city = place.city {
                        Text(city)
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                            .lineLimit(1)
                    }

                    // Visit count and duration
                    HStack(spacing: 12) {
                        HStack(spacing: 4) {
                            Image(systemName: "mappin.circle.fill")
                                .font(.caption)
                            Text("\(place.visitCount)")
                                .font(.caption)
                        }
                        .foregroundColor(.blueSlate)

                        if place.averageDurationMinutes > 0 {
                            HStack(spacing: 4) {
                                Image(systemName: "clock.fill")
                                    .font(.caption)
                                Text(formatDuration(minutes: place.averageDurationMinutes))
                                    .font(.caption)
                            }
                            .foregroundColor(.coolSteel)
                        }
                    }
                }

                Spacer()

                // Significance badge
                VStack(spacing: 4) {
                    Text(place.significanceLabel)
                        .font(.caption2)
                        .fontWeight(.medium)
                }
                .padding(.horizontal, 10)
                .padding(.vertical, 6)
                .glassBackground(
                    material: .ultraThin,
                    tint: place.significanceColor,
                    cornerRadius: 8
                )
                .foregroundColor(place.significanceColor)
            }
        }
    }

    private func formatDuration(minutes: Int) -> String {
        let hours = minutes / 60
        let mins = minutes % 60

        if hours > 0 {
            return "\(hours)h \(mins)m"
        } else {
            return "\(mins)m"
        }
    }
}

/// Empty state view with glass styling
struct EmptyPlacesView: View {
    var body: some View {
        VStack(spacing: 24) {
            Image(systemName: "mappin.slash")
                .font(.system(size: 64))
                .foregroundColor(.seaGlass)

            VStack(spacing: 8) {
                Text("No Places Yet")
                    .font(.title2)
                    .fontWeight(.semibold)
                    .foregroundColor(.primary)

                Text("As you travel, TrailGlass will automatically detect and record your frequent places.")
                    .font(.body)
                    .foregroundColor(.secondary)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 32)
            }
        }
        .padding(32)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

/// No search results view with glass styling
struct NoSearchResultsView: View {
    let query: String

    var body: some View {
        VStack(spacing: 20) {
            Image(systemName: "magnifyingglass")
                .font(.system(size: 48))
                .foregroundColor(.blueSlate)

            VStack(spacing: 8) {
                Text("No Results")
                    .font(.headline)
                    .foregroundColor(.primary)

                Text("No places found matching \"\(query)\"")
                    .font(.body)
                    .foregroundColor(.secondary)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 32)
            }
        }
        .padding(32)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

/// Error view with glass styling
struct PlacesErrorView: View {
    let error: String
    let onRetry: () -> Void

    var body: some View {
        VStack(spacing: 20) {
            Image(systemName: "exclamationmark.triangle.fill")
                .font(.system(size: 48))
                .foregroundColor(.driftwood)

            VStack(spacing: 8) {
                Text("Error Loading Places")
                    .font(.headline)
                    .foregroundColor(.primary)

                Text(error)
                    .font(.body)
                    .foregroundColor(.secondary)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 32)
            }

            GlassButton(
                title: "Retry",
                icon: "arrow.clockwise",
                variant: .filled,
                tint: .seaGlass,
                action: onRetry
            )
        }
        .padding(32)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

/// Scroll offset preference key
private struct ScrollOffsetPreferenceKey: PreferenceKey {
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

// Import Combine for subscriptions
#Preview {
    Text("SimplePlacesView Preview - Requires DI setup")
}
