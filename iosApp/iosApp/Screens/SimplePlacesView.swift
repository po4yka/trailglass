import SwiftUI
import Shared
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
                    SimplePlacesList(
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

#Preview {
    Text("SimplePlacesView Preview - Requires DI setup")
}
