import SwiftUI
import shared
import Combine

/// Simplified Places screen that works with the PlacesController
struct SimplePlacesView: View {
    @StateObject private var viewModel: SimplePlacesViewModel

    init(placesController: PlacesController) {
        _viewModel = StateObject(wrappedValue: SimplePlacesViewModel(controller: placesController))
    }

    var body: some View {
        NavigationView {
            ZStack {
                if viewModel.isLoading {
                    ProgressView()
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else if let error = viewModel.error {
                    ErrorView(error: error) {
                        viewModel.refresh()
                    }
                } else if viewModel.places.isEmpty {
                    EmptyPlacesView()
                } else {
                    PlacesList(
                        places: viewModel.places,
                        onPlaceTap: { place in
                            viewModel.selectedPlace = place
                        }
                    )
                }
            }
            .navigationTitle("Places")
            .navigationBarTitleDisplayMode(.large)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: { viewModel.refresh() }) {
                        Image(systemName: "arrow.clockwise")
                    }
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
    }
}

/// List of frequent places
struct PlacesList: View {
    let places: [FrequentPlaceItem]
    let onPlaceTap: (FrequentPlaceItem) -> Void

    var body: some View {
        List(places) { place in
            PlaceRow(place: place)
                .onTapGesture {
                    onPlaceTap(place)
                }
        }
        .listStyle(.plain)
    }
}

/// Individual place row
struct PlaceRow: View {
    let place: FrequentPlaceItem

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                // Category icon
                Image(systemName: place.categoryIcon)
                    .foregroundColor(place.categoryColor)
                    .font(.title2)
                    .frame(width: 44, height: 44)
                    .background(place.categoryColor.opacity(0.15))
                    .cornerRadius(8)

                VStack(alignment: .leading, spacing: 4) {
                    HStack {
                        Text(place.displayName)
                            .font(.headline)

                        if place.isFavorite {
                            Image(systemName: "star.fill")
                                .foregroundColor(.yellow)
                                .font(.caption)
                        }
                    }

                    if let city = place.city {
                        Text(city)
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                            .lineLimit(1)
                    }

                    HStack(spacing: 12) {
                        Label("\(place.visitCount) visits", systemImage: "mappin.circle")
                            .font(.caption)
                            .foregroundColor(.secondary)

                        if place.averageDurationMinutes > 0 {
                            Label(formatDuration(minutes: place.averageDurationMinutes), systemImage: "clock")
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                    }
                }

                Spacer()

                // Significance badge
                Text(place.significanceLabel)
                    .font(.caption2)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 4)
                    .background(place.significanceColor.opacity(0.2))
                    .foregroundColor(place.significanceColor)
                    .cornerRadius(8)
            }
        }
        .padding(.vertical, 4)
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

/// Empty state view
struct EmptyPlacesView: View {
    var body: some View {
        VStack(spacing: 16) {
            Image(systemName: "mappin.slash")
                .font(.system(size: 64))
                .foregroundColor(.secondary)

            Text("No Places Yet")
                .font(.title2)
                .fontWeight(.semibold)

            Text("As you travel, TrailGlass will automatically detect and record your frequent places.")
                .font(.body)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 32)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

/// Error view
struct ErrorView: View {
    let error: String
    let onRetry: () -> Void

    var body: some View {
        VStack(spacing: 16) {
            Image(systemName: "exclamationmark.triangle")
                .font(.system(size: 64))
                .foregroundColor(.red)

            Text("Error")
                .font(.title2)
                .fontWeight(.semibold)

            Text(error)
                .font(.body)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 32)

            Button(action: onRetry) {
                Text("Retry")
                    .fontWeight(.semibold)
            }
            .buttonStyle(.borderedProminent)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

/// Places view model
class SimplePlacesViewModel: ObservableObject {
    private let controller: PlacesController
    private var stateObserver: Kotlinx_coroutines_coreJob?
    private var frequentPlaces: [FrequentPlace] = []

    @Published var places: [FrequentPlaceItem] = []
    @Published var isLoading: Bool = false
    @Published var error: String?
    @Published var selectedPlace: FrequentPlaceItem?

    init(controller: PlacesController) {
        self.controller = controller
        observeState()
    }

    deinit {
        stateObserver?.cancel(cause: nil)
    }

    private func observeState() {
        stateObserver = controller.state.subscribe { [weak self] state in
            guard let self = self, let state = state else { return }

            DispatchQueue.main.async {
                self.isLoading = state.isLoading
                self.error = state.error
                self.frequentPlaces = state.places
                self.places = state.places.map { FrequentPlaceItem(from: $0) }
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
import Combine

#Preview {
    Text("SimplePlacesView Preview - Requires DI setup")
}
