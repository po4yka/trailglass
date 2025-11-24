import SwiftUI
import Shared

/**
 * SwiftUI trip detail screen matching Android TripDetailScreen.
 * Shows detailed view of a single trip with statistics, timeline, and actions.
 */
struct TripDetailView: View {
    let trip: Trip
    let onBack: () -> Void
    let onEdit: () -> Void
    let onShare: () -> Void
    let onExport: (ExportFormat) -> Void
    let onDelete: () -> Void

    @State private var showExportMenu = false
    @State private var showDeleteDialog = false

    var body: some View {
        NavigationView {
            ScrollView {
                LazyVStack(spacing: 16) {
                    // Trip header card
                    TripHeaderCard(trip: trip)

                    // Statistics card
                    TripStatisticsCard(trip: trip)

                    // Description card
                    if !trip.description.isEmpty {
                        Card {
                            VStack(alignment: .leading, spacing: 8) {
                                Text("Description")
                                    .font(.headline)

                                Text(trip.description)
                                    .font(.body)
                            }
                            .padding(16)
                        }
                    }

                    // Tags
                    if !trip.tags.isEmpty {
                        Card {
                            VStack(alignment: .leading, spacing: 8) {
                                Text("Tags")
                                    .font(.headline)

                                HStack(spacing: 8) {
                                    ForEach(trip.tags, id: \.self) { tag in
                                        HStack(spacing: 4) {
                                            Image(systemName: "tag")
                                                .font(.caption)
                                            Text(tag)
                                                .font(.body)
                                        }
                                        .padding(.horizontal, 8)
                                        .padding(.vertical, 4)
                                        .background(Color(.systemGray5))
                                        .cornerRadius(4)
                                    }
                                }
                            }
                            .padding(16)
                        }
                    }

                    // Visited countries and cities
                    if !trip.countriesVisited.isEmpty || !trip.citiesVisited.isEmpty {
                        VisitedPlacesCard(
                            countries: trip.countriesVisited,
                            cities: trip.citiesVisited
                        )
                    }
                }
                .padding(16)
            }
            .navigationTitle(trip.displayName)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button(action: onBack) {
                        Image(systemName: "chevron.left")
                    }
                }

                ToolbarItem(placement: .navigationBarTrailing) {
                    Menu {
                        Button(action: onShare) {
                            Label("Share", systemImage: "square.and.arrow.up")
                        }

                        Menu {
                            Button(action: { onExport(.gpx) }) {
                                Label("Export as GPX", systemImage: "map")
                            }
                            Button(action: { onExport(.kml) }) {
                                Label("Export as KML", systemImage: "globe")
                            }
                        } label: {
                            Label("Export", systemImage: "arrow.down.doc")
                        }

                        Button(action: onEdit) {
                            Label("Edit", systemImage: "pencil")
                        }

                        Divider()

                        Button(role: .destructive, action: { showDeleteDialog = true }) {
                            Label("Delete", systemImage: "trash")
                        }
                    } label: {
                        Image(systemName: "ellipsis.circle")
                    }
                }
            }
            .alert("Delete Trip?", isPresented: $showDeleteDialog) {
                Button("Cancel", role: .cancel) { }
                Button("Delete", role: .destructive, action: onDelete)
            } message: {
                Text("This action cannot be undone. All trip data will be permanently deleted.")
            }
        }
    }
}

/**
 * Trip header card.
 */
private struct TripHeaderCard: View {
    let trip: Trip

    var body: some View {
        Card {
            VStack(alignment: .leading, spacing: 12) {
                HStack(alignment: .top) {
                    VStack(alignment: .leading, spacing: 8) {
                        Text(trip.displayName)
                            .font(.title2)
                            .fontWeight(.bold)

                        // Date range
                        HStack(spacing: 4) {
                            Image(systemName: "calendar")
                                .font(.caption)
                            Text(formatDateRange(trip))
                                .font(.body)
                        }

                        // Duration
                        if let duration = trip.duration as? KotlinDuration {
                            HStack(spacing: 4) {
                                Image(systemName: "clock")
                                    .font(.caption)
                                Text(formatDuration(duration))
                                    .font(.body)
                            }
                        }
                    }

                    Spacer()

                    // Badges
                    VStack(alignment: .trailing, spacing: 8) {
                        if trip.isOngoing {
                            Text("Ongoing")
                                .font(.caption)
                                .padding(.horizontal, 8)
                                .padding(.vertical, 4)
                                .background(Color.blue)
                                .foregroundColor(.white)
                                .cornerRadius(4)
                        }

                        if trip.isAutoDetected {
                            Text("Auto-detected")
                                .font(.caption)
                                .padding(.horizontal, 8)
                                .padding(.vertical, 4)
                                .background(Color.purple.opacity(0.2))
                                .foregroundColor(.purple)
                                .cornerRadius(4)
                        }

                        if trip.isPublic {
                            Text("Public")
                                .font(.caption)
                                .padding(.horizontal, 8)
                                .padding(.vertical, 4)
                                .background(Color.green.opacity(0.2))
                                .foregroundColor(.green)
                                .cornerRadius(4)
                        }
                    }
                }
            }
            .padding(16)
        }
        .background(trip.isOngoing ? Color.blue.opacity(0.1) : Color(.systemGray6))
        .cornerRadius(12)
    }

    private func formatDateRange(_ trip: Trip) -> String {
        let dateFormatter = DateFormatter()
        dateFormatter.dateStyle = .medium
        dateFormatter.timeStyle = .none

        let startDate = dateFormatter.string(from: Date(timeIntervalSince1970: Double(trip.startTime.epochSeconds)))

        if let endTime = trip.endTime {
            let endDate = dateFormatter.string(from: Date(timeIntervalSince1970: Double(endTime.epochSeconds)))
            return "\(startDate) to \(endDate)"
        } else {
            return "Started \(startDate)"
        }
    }

    private func formatDuration(_ duration: KotlinDuration) -> String {
        let totalHours = duration.inWholeHours
        let days = totalHours / 24
        let hours = totalHours % 24
        let minutes = duration.inWholeMinutes % 60

        if days > 0 && hours > 0 {
            return "\(days) days, \(hours) hours"
        } else if days > 0 {
            return "\(days) days"
        } else if hours > 0 && minutes > 0 {
            return "\(hours) hours, \(minutes) min"
        } else if hours > 0 {
            return "\(hours) hours"
        } else {
            return "\(minutes) min"
        }
    }
}

/**
 * Trip statistics card.
 */
private struct TripStatisticsCard: View {
    let trip: Trip

    var body: some View {
        Card {
            VStack(alignment: .leading, spacing: 12) {
                Text("Statistics")
                    .font(.headline)

                HStack(spacing: 16) {
                    if trip.totalDistanceMeters > 0 {
                        StatItem(
                            icon: "ruler",
                            label: "Distance",
                            value: "\(Int(trip.totalDistanceMeters / 1000)) km"
                        )
                    }

                    if trip.visitedPlaceCount > 0 {
                        StatItem(
                            icon: "mappin.and.ellipse",
                            label: "Places",
                            value: "\(trip.visitedPlaceCount)"
                        )
                    }

                    if !trip.countriesVisited.isEmpty {
                        StatItem(
                            icon: "globe",
                            label: "Countries",
                            value: "\(trip.countriesVisited.count)"
                        )
                    }
                }
                .frame(maxWidth: .infinity)
            }
            .padding(16)
        }
    }
}

/**
 * Stat item.
 */
private struct StatItem: View {
    let icon: String
    let label: String
    let value: String

    var body: some View {
        VStack(spacing: 4) {
            Image(systemName: icon)
                .font(.title3)
                .foregroundColor(.blue)

            Text(value)
                .font(.headline)
                .fontWeight(.bold)

            Text(label)
                .font(.caption)
                .foregroundColor(.secondary)
        }
    }
}

/**
 * Visited places card.
 */
private struct VisitedPlacesCard: View {
    let countries: [String]
    let cities: [String]

    var body: some View {
        Card {
            VStack(alignment: .leading, spacing: 16) {
                if !countries.isEmpty {
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Countries Visited")
                            .font(.headline)

                        Text(countries.joined(separator: ", "))
                            .font(.body)
                    }
                }

                if !cities.isEmpty {
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Cities Visited")
                            .font(.headline)

                        let cityText = cities.prefix(10).joined(separator: ", ")
                            + (cities.count > 10 ? " and \(cities.count - 10) more" : "")

                        Text(cityText)
                            .font(.body)
                    }
                }
            }
            .padding(16)
        }
    }
}

/**
 * Reusable card component.
 */
private struct Card<Content: View>: View {
    let content: Content

    init(@ViewBuilder content: () -> Content) {
        self.content = content()
    }

    var body: some View {
        content
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(Color(.systemBackground))
            .cornerRadius(12)
            .shadow(radius: 2)
    }
}

/**
 * Export format enum.
 */
enum ExportFormat {
    case gpx
    case kml
}
