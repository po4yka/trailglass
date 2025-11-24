import SwiftUI
import Shared

/// Paginated trips view with pull-to-refresh
/// Alternative to TripsView that uses direct repository access
struct PaginatedTripsView: View {
    @StateObject private var viewModel: PaginatedTripsViewModel
    @State private var scrollOffset: CGFloat = 0
    @State private var selectedTrip: Trip?

    init(repository: TripRepository, userId: String) {
        _viewModel = StateObject(
            wrappedValue: PaginatedTripsViewModel(
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
                    title: "Trips",
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
                if viewModel.isLoading && viewModel.trips.isEmpty {
                    LoadingView()
                } else if let error = viewModel.error, viewModel.trips.isEmpty {
                    ErrorView(error: error) {
                        viewModel.loadTrips()
                    }
                } else if viewModel.trips.isEmpty {
                    EmptyTripsView()
                } else {
                    TripsList(
                        trips: viewModel.trips,
                        ongoingTrips: viewModel.ongoingTrips,
                        completedTrips: viewModel.completedTrips,
                        onRefresh: {
                            viewModel.refresh()
                        },
                        onTripTap: { trip in
                            selectedTrip = trip
                        },
                        scrollOffset: $scrollOffset
                    )
                }
            }
        }
        .navigationBarHidden(true)
        .onAppear {
            if viewModel.trips.isEmpty {
                viewModel.loadTrips()
            }
        }
        .sheet(item: $selectedTrip) { trip in
            NavigationView {
                TripDetailView(tripId: trip.id, appComponent: nil)
            }
        }
    }

    private var subtitleText: String {
        let total = viewModel.totalCount
        let ongoing = viewModel.ongoingCount

        if ongoing > 0 {
            return "\(total) trips • \(ongoing) ongoing"
        } else {
            return "\(total) trips"
        }
    }
}

/// List of trips with pull-to-refresh
private struct TripsList: View {
    let trips: [Trip]
    let ongoingTrips: [Trip]
    let completedTrips: [Trip]
    let onRefresh: () -> Void
    let onTripTap: (Trip) -> Void
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

            VStack(spacing: 16) {
                // Ongoing trips section
                if !ongoingTrips.isEmpty {
                    VStack(alignment: .leading, spacing: 12) {
                        Text("Ongoing")
                            .font(.headline)
                            .foregroundColor(.coastalPath)
                            .padding(.horizontal, 16)

                        ForEach(ongoingTrips, id: \.id) { trip in
                            Button {
                                onTripTap(trip)
                            } label: {
                                TripCard(trip: trip)
                            }
                            .buttonStyle(PlainButtonStyle())
                        }
                    }
                }

                // Completed trips section
                if !completedTrips.isEmpty {
                    VStack(alignment: .leading, spacing: 12) {
                        Text(ongoingTrips.isEmpty ? "All Trips" : "Past Trips")
                            .font(.headline)
                            .foregroundColor(.blueSlate)
                            .padding(.horizontal, 16)
                            .padding(.top, ongoingTrips.isEmpty ? 0 : 8)

                        ForEach(completedTrips, id: \.id) { trip in
                            Button {
                                onTripTap(trip)
                            } label: {
                                TripCard(trip: trip)
                            }
                            .buttonStyle(PlainButtonStyle())
                        }
                    }
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

/// Individual trip card with glass styling
private struct TripCard: View {
    let trip: Trip

    var body: some View {
        GlassCard(variant: trip.isOngoing ? .route : .visit) {
            VStack(alignment: .leading, spacing: 12) {
                // Header
                HStack(alignment: .top) {
                    VStack(alignment: .leading, spacing: 8) {
                        HStack(spacing: 8) {
                            Text(trip.displayName)
                                .font(.headline)
                                .fontWeight(.bold)
                                .foregroundColor(.primary)

                            if trip.isOngoing {
                                HStack(spacing: 4) {
                                    Circle()
                                        .fill(Color.coastalPath)
                                        .frame(width: 6, height: 6)
                                    Text("Ongoing")
                                        .font(.caption2)
                                        .fontWeight(.medium)
                                }
                                .padding(.horizontal, 8)
                                .padding(.vertical, 4)
                                .glassBackground(
                                    material: .ultraThin,
                                    tint: .coastalPath,
                                    cornerRadius: 8
                                )
                            }
                        }

                        // Date range
                        Text(formatDateRange(trip))
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                    }

                    Spacer()

                    // Auto-detected badge
                    if trip.isAutoDetected {
                        HStack(spacing: 4) {
                            Image(systemName: "sparkles")
                                .font(.caption2)
                            Text("Auto")
                                .font(.caption2)
                                .fontWeight(.medium)
                        }
                        .padding(.horizontal, 8)
                        .padding(.vertical, 4)
                        .glassBackground(
                            material: .ultraThin,
                            tint: .duskPurple,
                            cornerRadius: 8
                        )
                        .foregroundColor(.duskPurple)
                    }
                }

                // Description
                if let description = trip.description {
                    Text(description)
                        .font(.body)
                        .lineLimit(2)
                        .foregroundColor(.primary)
                }

                // Statistics
                if !trip.summary.isEmpty {
                    HStack(spacing: 8) {
                        Image(systemName: "chart.bar.fill")
                            .font(.caption)
                            .foregroundColor(.blueSlate)
                        Text(trip.summary)
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                }

                // Tags
                if !trip.tags.isEmpty {
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 8) {
                            ForEach(Array(trip.tags.prefix(4)), id: \.self) { tag in
                                HStack(spacing: 4) {
                                    Image(systemName: "tag.fill")
                                        .font(.caption2)
                                    Text(tag)
                                        .font(.caption)
                                }
                                .padding(.horizontal, 8)
                                .padding(.vertical, 4)
                                .glassBackground(
                                    material: .ultraThin,
                                    tint: .coolSteel,
                                    cornerRadius: 6
                                )
                                .foregroundColor(.primary)
                            }

                            if trip.tags.count > 4 {
                                Text("+\(trip.tags.count - 4)")
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                                    .padding(.horizontal, 8)
                            }
                        }
                    }
                }
            }
        }
    }

    private func formatDateRange(_ trip: Trip) -> String {
        let dateFormatter = DateFormatter()
        dateFormatter.dateStyle = .medium
        dateFormatter.timeStyle = .none

        let startDate = Date(timeIntervalSince1970: TimeInterval(trip.startTime.epochSeconds))
        let formattedStart = dateFormatter.string(from: startDate)

        if let endTime = trip.endTime {
            let endDate = Date(timeIntervalSince1970: TimeInterval(endTime.epochSeconds))
            let formattedEnd = dateFormatter.string(from: endDate)
            return "\(formattedStart) - \(formattedEnd)"
        } else {
            return "Started \(formattedStart)"
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
                Text("Error Loading Trips")
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

/// Empty trips view
private struct EmptyTripsView: View {
    var body: some View {
        VStack(spacing: 24) {
            Image(systemName: "suitcase")
                .font(.system(size: 64))
                .foregroundColor(.coolSteel)

            VStack(spacing: 8) {
                Text("No Trips Yet")
                    .font(.title2)
                    .fontWeight(.semibold)
                    .foregroundColor(.primary)

                Text("Start tracking your adventures")
                    .font(.body)
                    .foregroundColor(.secondary)
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

extension Trip: @retroactive Identifiable {
    public var id: String { self.id }
}
