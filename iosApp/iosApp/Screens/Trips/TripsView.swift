import SwiftUI
import Shared

/**
 * SwiftUI trips screen with TripsController integration.
 * Shows all trips with filtering, sorting, and navigation to trip details.
 * Updated with Liquid Glass components.
 */
struct TripsView: View {
    let appComponent: AppComponent
    @StateObject private var viewModel: TripsViewModel
    @State private var scrollOffset: CGFloat = 0

    init(appComponent: AppComponent) {
        self.appComponent = appComponent
        _viewModel = StateObject(wrappedValue: TripsViewModel(controller: appComponent.tripsController))
    }

    var body: some View {
        ZStack {
            Color.backgroundLight.ignoresSafeArea()

            VStack(spacing: 0) {
                // Large flexible navigation bar with hero background
                LargeFlexibleNavigationBar(
                    title: "Trips",
                    scrollOffset: scrollOffset,
                    actions: [
                        NavigationAction(icon: "line.3.horizontal.decrease.circle") {
                            // Filter action
                        },
                        NavigationAction(icon: "arrow.up.arrow.down.circle") {
                            // Sort action
                        }
                    ],
                    subtitle: {
                        Text(tripSubtitle)
                    },
                    backgroundContent: {
                        HeroGradientBackground(
                            startColor: .lightCyan,
                            endColor: .coastalPath
                        )
                    }
                )

                if viewModel.isLoading && viewModel.trips.isEmpty {
                    GlassLoadingIndicator(variant: .pulsing, color: .coastalPath)
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else if let error = viewModel.error {
                    ErrorView(error: error, onRetry: { viewModel.loadTrips() })
                } else if viewModel.filteredTrips.isEmpty {
                    if viewModel.trips.isEmpty {
                        EmptyTripsView(onCreateTrip: { viewModel.showCreateDialog() })
                    } else {
                        NoResultsView(message: "No trips match your filters")
                    }
                } else {
                    TripsContent(
                        trips: viewModel.filteredTrips,
                        ongoingTrips: viewModel.ongoingTrips,
                        appComponent: appComponent,
                        onRefresh: { viewModel.refresh() },
                        scrollOffset: $scrollOffset
                    )
                }
            }

            // Glass FAB
            VStack {
                Spacer()
                HStack {
                    Spacer()
                    GlassButton(
                        icon: "plus",
                        variant: .filled,
                        tint: .coastalPath
                    ) {
                        viewModel.showCreateDialog()
                    }
                    .frame(width: 56, height: 56)
                    .padding(.trailing, 16)
                    .padding(.bottom, 96) // Extra padding for tab bar
                }
            }
        }
        .navigationBarHidden(true)
        .onAppear {
            viewModel.loadTrips()
        }
    }

    private var tripSubtitle: String {
        let totalCount = viewModel.trips.count
        let ongoingCount = viewModel.ongoingTrips.count
        if ongoingCount > 0 {
            return "\(totalCount) trips • \(ongoingCount) ongoing"
        }
        return "\(totalCount) trips"
    }
}

#Preview {
    Text("TripsView Preview - Requires DI setup")
}
