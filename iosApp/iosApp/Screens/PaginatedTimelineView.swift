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
                    EmptyTimelineView()
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
        .sheet(item: $selectedVisit) { visit in
            PlaceDetailView(
                place: nil,
                onToggleFavorite: {},
                onDismiss: { selectedVisit = nil }
            )
        }
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
                Image(systemName: categoryIcon)
                    .font(.title2)
                    .foregroundColor(categoryColor)
                    .frame(width: 44, height: 44)
                    .glassBackground(
                        material: .ultraThin,
                        tint: categoryColor,
                        cornerRadius: 10
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
                    .glassBackground(
                        material: .ultraThin,
                        tint: categoryColor,
                        cornerRadius: 6
                    )
                    .foregroundColor(categoryColor)
            }
        }
    }

    private var categoryIcon: String {
        switch visit.category.name {
        case "HOME": return "house.fill"
        case "WORK": return "briefcase.fill"
        case "FOOD": return "fork.knife"
        case "SHOPPING": return "cart.fill"
        case "FITNESS": return "figure.run"
        case "ENTERTAINMENT": return "theatermasks.fill"
        default: return "mappin.circle.fill"
        }
    }

    private var categoryColor: Color {
        switch visit.category.name {
        case "HOME": return .blue
        case "WORK": return .purple
        case "FOOD": return .orange
        case "SHOPPING": return .green
        case "FITNESS": return .red
        case "ENTERTAINMENT": return .pink
        default: return .gray
        }
    }

    private func formatTime(_ instant: Kotlinx_datetimeInstant) -> String {
        let date = Date(timeIntervalSince1970: TimeInterval(instant.epochSeconds))
        let formatter = DateFormatter()
        formatter.timeStyle = .short
        return formatter.string(from: date)
    }

    private func formatDuration(_ duration: KotlinDuration) -> String {
        let hours = duration.inWholeHours
        let minutes = duration.inWholeMinutes % 60

        if hours > 0 {
            return "\(hours)h \(minutes)m"
        } else {
            return "\(minutes)m"
        }
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

/// Error view
private struct ErrorView: View {
    let error: String
    let onRetry: () -> Void

    var body: some View {
        VStack(spacing: 20) {
            Image(systemName: "exclamationmark.triangle.fill")
                .font(.system(size: 48))
                .foregroundColor(.driftwood)

            VStack(spacing: 8) {
                Text("Error Loading Timeline")
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
                tint: .coastalPath,
                action: onRetry
            )
        }
        .padding(32)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

/// Empty timeline view
private struct EmptyTimelineView: View {
    var body: some View {
        VStack(spacing: 24) {
            Image(systemName: "clock")
                .font(.system(size: 64))
                .foregroundColor(.coastalPath)

            VStack(spacing: 8) {
                Text("No Timeline Data")
                    .font(.title2)
                    .fontWeight(.semibold)
                    .foregroundColor(.primary)

                Text("Enable location tracking to see your timeline")
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

/// Scroll offset preference key
private struct ScrollOffsetPreferenceKey: PreferenceKey {
    static var defaultValue: CGFloat = 0

    static func reduce(value: inout CGFloat, nextValue: () -> CGFloat) {
        value = nextValue()
    }
}

extension PlaceVisit: @retroactive Identifiable {
    public var id: String { self.id }
}
