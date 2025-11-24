import SwiftUI
import Shared

/// Places screen showing all visited places, matching Android PlacesScreen
struct PlacesView: View {
    @StateObject private var viewModel: PlacesViewModel
    @State private var showSearchBar = false
    @State private var showFilterSheet = false
    @State private var showSortSheet = false

    init(placesController: PlacesController) {
        _viewModel = StateObject(wrappedValue: PlacesViewModel(controller: placesController))
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
                        onPlaceSelected: { place in
                            viewModel.selectPlace(place)
                        }
                    )
                }
            }
            .navigationTitle(showSearchBar ? "" : "Places")
            .navigationBarTitleDisplayMode(.large)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    if showSearchBar {
                        Button("Done") {
                            showSearchBar = false
                            viewModel.clearSearch()
                        }
                    } else {
                        HStack(spacing: 8) {
                            Button(action: { showSearchBar = true }) {
                                Image(systemName: "magnifyingglass")
                            }

                            Button(action: { showFilterSheet = true }) {
                                Image(systemName: "line.3.horizontal.decrease.circle")
                                    .overlay(
                                        viewModel.hasActiveFilters ?
                                        Circle()
                                            .fill(Color.blue)
                                            .frame(width: 8, height: 8)
                                            .offset(x: 8, y: -8)
                                        : nil
                                    )
                            }

                            Button(action: { showSortSheet = true }) {
                                Image(systemName: "arrow.up.arrow.down")
                            }
                        }
                    }
                }
            }
            .searchable(text: $viewModel.searchQuery, isPresented: $showSearchBar, prompt: "Search places...")
            .sheet(isPresented: $showFilterSheet) {
                PlacesFilterSheet(
                    currentFilters: viewModel.filters,
                    onApply: { filters in
                        viewModel.applyFilters(filters)
                        showFilterSheet = false
                    },
                    onCancel: {
                        showFilterSheet = false
                    }
                )
            }
            .sheet(isPresented: $showSortSheet) {
                PlacesSortSheet(
                    currentSort: viewModel.sortOption,
                    onApply: { sortOption in
                        viewModel.setSortOption(sortOption)
                        showSortSheet = false
                    },
                    onCancel: {
                        showSortSheet = false
                    }
                )
            }
        }
    }
}

#Preview {
    Text("PlacesView Preview - Requires DI setup")
}
