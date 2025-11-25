import SwiftUI
import Shared

/// Paginated timeline view showing place visits with manual pagination
/// This is an alternative to EnhancedTimelineView that uses direct repository access
/// instead of controller-based data fetching
struct PaginatedTimelineView: View {
    @StateObject private var viewModel: PaginatedPlaceVisitsViewModel
    @State private var scrollOffset: CGFloat = 0
    @State private var selectedVisit: PlaceVisit?

    init(repository: PlaceVisitRepository, userId: String) {
        _viewModel = StateObject(
            wrappedValue: PaginatedPlaceVisitsViewModel(
                repository: repository,
                userId: userId
            )
        )
    }

    var body: some View {
        ZStack {
            Color.backgroundLight.ignoresSafeArea()

            VStack(spacing: 0) {
                // Navigation bar
                LargeFlexibleNavigationBar(
                    title: "Timeline",
                    scrollOffset: scrollOffset,
                    actions: [
                        NavigationAction(icon: "arrow.clockwise") {
                            viewModel.refresh()
                        }
                    ],
                    subtitle: {
                        Text(subtitleText)
                    },
                    backgroundContent: {
                        HeroGradientBackground(
                            startColor: .lightCyan,
                            endColor: .coastalPath
                        )
                    }
                )

                // Content
                if viewModel.isLoading && viewModel.visits.isEmpty {
                    LoadingView()
                } else if let error = viewModel.error, viewModel.visits.isEmpty {
                    ErrorView(error: error) {
                        viewModel.loadInitialData()
                    }
                } else if viewModel.visits.isEmpty {
                    EmptyStateView(
                        icon: "clock",
                        title: "No Timeline Data",
                        message: "Enable location tracking to see your timeline"
                    )
                } else {
                    VisitsList(
                        visits: viewModel.visits,
                        isLoadingMore: viewModel.isLoadingMore,
                        hasMorePages: viewModel.hasMorePages,
                        onVisitAppear: { visit in
                            viewModel.loadMoreIfNeeded(currentItem: visit)
                        },
                        onRefresh: {
                            viewModel.refresh()
                        },
                        onVisitTap: { visit in
                            selectedVisit = visit
                        },
                        scrollOffset: $scrollOffset
                    )
                }
            }
        }
        .navigationBarHidden(true)
        .onAppear {
            if viewModel.visits.isEmpty {
                viewModel.loadInitialData()
            }
        }
        // .sheet(item: $selectedVisit) { visit in
        //     PlaceDetailView(
        //         place: nil, // TODO: Create proper FrequentPlace
        //         onToggleFavorite: {},
        //         onDismiss: { selectedVisit = nil }
        //     )
        // }
    }

    private var subtitleText: String {
        let count = viewModel.visits.count
        if viewModel.hasMorePages {
            return "\(count)+ visits"
        } else {
            return "\(count) visits"
        }
    }
}

/// List of visits with pagination support
private struct VisitsList: View {
    let visits: [PlaceVisit]
    let isLoadingMore: Bool
    let hasMorePages: Bool
    let onVisitAppear: (PlaceVisit) -> Void
    let onRefresh: () -> Void
    let onVisitTap: (PlaceVisit) -> Void
    @Binding var scrollOffset: CGFloat

    var body: some View {
        ScrollView {
            GeometryReader { geometry in
                Color.clear.preference(
                    key: ScrollOffsetPreferenceKey.self,
                    value: geometry.frame(in: .named("scroll")).minY
                )
            }
            .frame(height: 0)

            LazyVStack(spacing: 12) {
                ForEach(visits, id: \.id) { visit in
                    Button {
                        onVisitTap(visit)
                    } label: {
                        VisitCard(visit: visit)
                    }
                    .buttonStyle(PlainButtonStyle())
                    .onAppear {
                        onVisitAppear(visit)
                    }
                }

                // Loading indicator at the bottom
                if isLoadingMore {
                    HStack {
                        Spacer()
                        GlassLoadingIndicator(
                            variant: .pulsing,
                            size: 32,
                            color: .coastalPath
                        )
                        Spacer()
                    }
                    .padding(.vertical, 16)
                }

                // End of list indicator
                if !hasMorePages && !visits.isEmpty {
                    HStack {
                        Spacer()
                        Text("No more visits")
                            .font(.caption)
                            .foregroundColor(.secondary)
                        Spacer()
                    }
                    .padding(.vertical, 16)
                }
            }
            .padding(.horizontal, 16)
            .padding(.top, 8)
            .padding(.bottom, 96)
        }
        .coordinateSpace(name: "scroll")
        .onPreferenceChange(ScrollOffsetPreferenceKey.self) { value in
            scrollOffset = value
        }
        .refreshable {
            onRefresh()
        }
    }
}

/// Individual visit card with glass styling
private struct VisitCard: View {
    let visit: PlaceVisit

    var body: some View {
        GlassCard(variant: .visit) {
            HStack(spacing: 12) {
                // Category icon
                Image(systemName: categoryIcon(visit.category))
                    .font(.title2)
                    .foregroundColor(categoryColor(visit.category))
                    .frame(width: 44, height: 44)
                    .glassEffectTinted(
                        categoryColor(visit.category),
                        opacity: 0.8
                    )

                VStack(alignment: .leading, spacing: 6) {
                    // Name and favorite
                    HStack(spacing: 6) {
                        Text(visit.displayName)
                            .font(.headline)
                            .foregroundColor(.primary)
                            .lineLimit(1)

                        if visit.isFavorite {
                            Image(systemName: "star.fill")
                                .foregroundColor(.warning)
                                .font(.caption)
                        }
                    }

                    // Location
                    if let city = visit.city {
                        Text(city)
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                            .lineLimit(1)
                    }

                    // Time and duration
                    HStack(spacing: 12) {
                        HStack(spacing: 4) {
                            Image(systemName: "clock.fill")
                                .font(.caption)
                            Text(formatTime(visit.startTime))
                                .font(.caption)
                        }
                        .foregroundColor(.blueSlate)

                        HStack(spacing: 4) {
                            Image(systemName: "hourglass")
                                .font(.caption)
                            Text(formatDuration(visit.duration))
                                .font(.caption)
                        }
                        .foregroundColor(.coolSteel)
                    }
                }

                Spacer()

                // Category badge
                Text(visit.category.name.capitalized)
                    .font(.caption2)
                    .fontWeight(.medium)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 4)
                    .glassEffectTinted(
                        categoryColor(visit.category),
                        opacity: 0.8
                    )
                    .foregroundColor(categoryColor(visit.category))
            }
        }
    }

    private func formatTime(_ instant: Kotlinx_datetimeInstant) -> String {
        let date = Date(timeIntervalSince1970: TimeInterval(instant.epochSeconds))
        let formatter = DateFormatter()
        formatter.timeStyle = .short
        return formatter.string(from: date)
    }
}

/// Loading view
private struct LoadingView: View {
    var body: some View {
        VStack {
            Spacer()
            GlassLoadingIndicator(
                variant: .pulsing,
                size: 72,
                color: .coastalPath
            )
            Spacer()
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

// Shared components moved to SharedComponents.swift


extension PlaceVisit: @retroactive Identifiable {
    public var id: String { self.id }
}
