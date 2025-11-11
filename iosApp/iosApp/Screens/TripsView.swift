import SwiftUI

/**
 * SwiftUI trips screen matching Android TripsScreen.
 * Shows all trips with filtering and sorting options.
 */
struct TripsView: View {
    let trips: [Trip]
    let onTripClick: (Trip) -> Void
    let onCreateTrip: () -> Void
    let onRefresh: () -> Void

    var body: some View {
        NavigationView {
            ZStack {
                if trips.isEmpty {
                    EmptyTripsView(onCreateTrip: onCreateTrip)
                } else {
                    TripsContent(
                        trips: trips,
                        onTripClick: onTripClick,
                        onRefresh: onRefresh
                    )
                }

                // Floating action button
                VStack {
                    Spacer()
                    HStack {
                        Spacer()
                        Button(action: onCreateTrip) {
                            Image(systemName: "plus")
                                .font(.title2)
                                .foregroundColor(.white)
                                .frame(width: 56, height: 56)
                                .background(Color.blue)
                                .clipShape(Circle())
                                .shadow(radius: 4)
                        }
                        .padding(.trailing, 16)
                        .padding(.bottom, 16)
                    }
                }
            }
            .navigationTitle("Trips")
        }
    }
}

/**
 * Trips content with list.
 */
private struct TripsContent: View {
    let trips: [Trip]
    let onTripClick: (Trip) -> Void
    let onRefresh: () -> Void

    var body: some View {
        ScrollView {
            LazyVStack(spacing: 12) {
                // Header
                HStack {
                    VStack(alignment: .leading, spacing: 4) {
                        Text("Trips")
                            .font(.largeTitle)
                            .fontWeight(.bold)

                        Text("\(trips.count) total")
                            .font(.body)
                            .foregroundColor(.secondary)
                    }

                    Spacer()

                    Button(action: onRefresh) {
                        Image(systemName: "arrow.clockwise")
                    }
                }
                .padding(.horizontal, 16)
                .padding(.top, 16)

                // Group trips: ongoing first, then by date
                let ongoingTrips = trips.filter { $0.isOngoing }
                let completedTrips = trips.filter { !$0.isOngoing }

                // Ongoing trips section
                if !ongoingTrips.isEmpty {
                    SectionHeader(text: "Ongoing")
                        .padding(.horizontal, 16)
                        .padding(.top, 8)

                    ForEach(ongoingTrips, id: \.id) { trip in
                        TripCard(trip: trip, onClick: { onTripClick(trip) })
                            .padding(.horizontal, 16)
                    }
                }

                // Completed trips section
                if !completedTrips.isEmpty {
                    SectionHeader(text: ongoingTrips.isEmpty ? "All Trips" : "Past Trips")
                        .padding(.horizontal, 16)
                        .padding(.top, 8)

                    ForEach(completedTrips, id: \.id) { trip in
                        TripCard(trip: trip, onClick: { onTripClick(trip) })
                            .padding(.horizontal, 16)
                    }
                }
            }
            .padding(.bottom, 80) // Extra padding for FAB
        }
    }
}

/**
 * Section header.
 */
private struct SectionHeader: View {
    let text: String

    var body: some View {
        Text(text)
            .font(.title3)
            .fontWeight(.semibold)
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.vertical, 8)
    }
}

/**
 * Trip card.
 */
private struct TripCard: View {
    let trip: Trip
    let onClick: () -> Void

    var body: some View {
        Button(action: onClick) {
            VStack(alignment: .leading, spacing: 12) {
                // Header row
                HStack(alignment: .top) {
                    VStack(alignment: .leading, spacing: 8) {
                        HStack(spacing: 8) {
                            Text(trip.displayName)
                                .font(.title3)
                                .fontWeight(.bold)
                                .foregroundColor(.primary)

                            if trip.isOngoing {
                                Text("Ongoing")
                                    .font(.caption)
                                    .padding(.horizontal, 8)
                                    .padding(.vertical, 4)
                                    .background(Color.blue)
                                    .foregroundColor(.white)
                                    .cornerRadius(4)
                            }
                        }

                        // Date range
                        Text(formatDateRange(trip))
                            .font(.body)
                            .foregroundColor(.secondary)
                    }

                    Spacer()

                    // Auto-detected badge
                    if trip.isAutoDetected {
                        Text("Auto")
                            .font(.caption)
                            .padding(.horizontal, 8)
                            .padding(.vertical, 4)
                            .background(Color.purple.opacity(0.2))
                            .foregroundColor(.purple)
                            .cornerRadius(4)
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
                    Text(trip.summary)
                        .font(.caption)
                        .foregroundColor(.secondary)
                }

                // Tags
                if !trip.tags.isEmpty {
                    HStack(spacing: 8) {
                        ForEach(Array(trip.tags.prefix(3)), id: \.self) { tag in
                            HStack(spacing: 4) {
                                Image(systemName: "tag")
                                    .font(.caption2)
                                Text(tag)
                                    .font(.caption)
                            }
                            .padding(.horizontal, 8)
                            .padding(.vertical, 4)
                            .background(Color(.systemGray5))
                            .cornerRadius(4)
                        }

                        if trip.tags.count > 3 {
                            Text("+\(trip.tags.count - 3)")
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                    }
                }
            }
            .padding(16)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(trip.isOngoing ? Color.blue.opacity(0.1) : Color(.systemGray6))
            .cornerRadius(12)
        }
    }

    private func formatDateRange(_ trip: Trip) -> String {
        let dateFormatter = DateFormatter()
        dateFormatter.dateStyle = .medium
        dateFormatter.timeStyle = .none

        let startDate = dateFormatter.string(from: Date(timeIntervalSince1970: trip.startTime.epochSeconds))

        if let endTime = trip.endTime {
            let endDate = dateFormatter.string(from: Date(timeIntervalSince1970: endTime.epochSeconds))
            return "\(startDate) - \(endDate)"
        } else {
            return "Started \(startDate)"
        }
    }
}

/**
 * Empty trips view.
 */
private struct EmptyTripsView: View {
    let onCreateTrip: () -> Void

    var body: some View {
        VStack(spacing: 16) {
            Image(systemName: "suitcase")
                .font(.system(size: 64))
                .foregroundColor(.secondary)

            Text("No Trips Yet")
                .font(.title2)
                .foregroundColor(.secondary)

            Text("Start tracking your adventures")
                .font(.body)
                .foregroundColor(.secondary)

            Button(action: onCreateTrip) {
                HStack {
                    Image(systemName: "plus")
                    Text("Create Trip")
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 8)
            }
            .buttonStyle(.borderedProminent)
        }
    }
}
