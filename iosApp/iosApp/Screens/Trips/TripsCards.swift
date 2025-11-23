import SwiftUI
import shared

/**
 * Trip glass card with Liquid Glass styling.
 */
struct TripGlassCard: View {
    let trip: Trip

    var body: some View {
        GlassCard(variant: trip.isOngoing ? .route : .visit) {
            VStack(alignment: .leading, spacing: 12) {
                // Header row
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
                                .padding(.vertical: 4)
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

/**
 * Trips content with glass cards.
 */
struct TripsContent: View {
    let trips: [Trip]
    let ongoingTrips: [Trip]
    let appComponent: AppComponent
    let onRefresh: () -> Void
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

            GlassEffectGroup(spacing: 12, padding: 16) {
                VStack(spacing: 12) {
                    // Group trips: ongoing first, then completed
                    let ongoing = trips.filter { $0.isOngoing }
                    let completed = trips.filter { !$0.isOngoing }

                    // Ongoing trips section
                    if !ongoing.isEmpty {
                        VStack(alignment: .leading, spacing: 8) {
                            Text("Ongoing")
                                .font(.headline)
                                .foregroundColor(.coastalPath)
                                .padding(.horizontal, 4)

                            ForEach(ongoing, id: \.id) { trip in
                                NavigationLink(destination: TripDetailView(tripId: trip.id, appComponent: appComponent)) {
                                    TripGlassCard(trip: trip)
                                }
                                .buttonStyle(PlainButtonStyle())
                            }
                        }
                    }

                    // Completed trips section
                    if !completed.isEmpty {
                        VStack(alignment: .leading, spacing: 8) {
                            Text(ongoing.isEmpty ? "All Trips" : "Past Trips")
                                .font(.headline)
                                .foregroundColor(.blueSlate)
                                .padding(.horizontal, 4)
                                .padding(.top, ongoing.isEmpty ? 0 : 12)

                            ForEach(completed, id: \.id) { trip in
                                NavigationLink(destination: TripDetailView(tripId: trip.id, appComponent: appComponent)) {
                                    TripGlassCard(trip: trip)
                                }
                                .buttonStyle(PlainButtonStyle())
                            }
                        }
                    }
                }
            }
            .padding(.horizontal, 16)
            .padding(.bottom, 96) // Extra padding for FAB and tab bar
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
