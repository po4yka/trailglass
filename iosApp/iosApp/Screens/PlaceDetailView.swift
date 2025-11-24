import SwiftUI
import Shared

/// Detail view for a frequent place
struct PlaceDetailView: View {
    let place: FrequentPlace
    let onToggleFavorite: () -> Void
    let onDismiss: () -> Void

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 24) {
                // Header Section
                headerSection

                // Statistics Section
                statisticsSection

                // Location Section
                locationSection

                // Custom Info Section
                if place.userLabel != nil || place.userNotes != nil {
                    customInfoSection
                }
            }
            .padding()
        }
        .navigationTitle("Place Details")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                Button(action: onToggleFavorite) {
                    Image(systemName: place.isFavorite ? "star.fill" : "star")
                        .foregroundColor(place.isFavorite ? .yellow : .gray)
                }
            }
        }
    }

    private var headerSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack(spacing: 12) {
                Image(systemName: categoryIcon(for: place.category))
                    .font(.system(size: 40))
                    .foregroundColor(categoryColor(for: place.category))
                    .frame(width: 60, height: 60)
                    .background(categoryColor(for: place.category).opacity(0.15))
                    .cornerRadius(12)

                VStack(alignment: .leading, spacing: 4) {
                    Text(place.displayName)
                        .font(.title2)
                        .fontWeight(.bold)

                    if let city = place.city {
                        Text(city)
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                    }
                }
            }

            HStack(spacing: 8) {
                // Significance Badge
                HStack(spacing: 4) {
                    Image(systemName: "star.fill")
                        .font(.caption)
                    Text(place.significance.name.capitalized.lowercased())
                        .font(.caption)
                        .fontWeight(.medium)
                }
                .padding(.horizontal, 12)
                .padding(.vertical, 6)
                .background(significanceColor(for: place.significance).opacity(0.2))
                .foregroundColor(significanceColor(for: place.significance))
                .cornerRadius(8)

                // Category Badge
                if place.category != .other {
                    Text(place.category.name.capitalized.lowercased())
                        .font(.caption)
                        .fontWeight(.medium)
                        .padding(.horizontal, 12)
                        .padding(.vertical, 6)
                        .background(Color.gray.opacity(0.2))
                        .foregroundColor(.primary)
                        .cornerRadius(8)
                }
            }
        }
        .padding()
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(
            RoundedRectangle(cornerRadius: 12)
                .fill(significanceBackgroundColor(for: place.significance))
        )
    }

    private var statisticsSection: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Statistics")
                .font(.headline)
                .fontWeight(.bold)

            VStack(spacing: 12) {
                StatRow(
                    icon: "calendar",
                    label: "Total Visits",
                    value: "\(place.visitCount)"
                )

                if place.totalDuration.toDouble(unit: .minutes) > 0 {
                    StatRow(
                        icon: "clock",
                        label: "Total Time",
                        value: formatDuration(place.totalDuration)
                    )
                }

                if place.averageDuration.toDouble(unit: .minutes) > 0 {
                    StatRow(
                        icon: "timer",
                        label: "Average Duration",
                        value: formatDuration(place.averageDuration)
                    )
                }

                if let firstVisit = place.firstVisitTime {
                    StatRow(
                        icon: "calendar.badge.clock",
                        label: "First Visit",
                        value: formatDate(firstVisit)
                    )
                }

                if let lastVisit = place.lastVisitTime {
                    StatRow(
                        icon: "arrow.clockwise",
                        label: "Last Visit",
                        value: formatDate(lastVisit)
                    )
                }
            }
        }
        .padding()
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(
            RoundedRectangle(cornerRadius: 12)
                .fill(Color(.systemGray6))
        )
    }

    private var locationSection: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Location")
                .font(.headline)
                .fontWeight(.bold)

            VStack(spacing: 12) {
                if let address = place.address {
                    StatRow(
                        icon: "mappin",
                        label: "Address",
                        value: address
                    )
                }

                StatRow(
                    icon: "location",
                    label: "Coordinates",
                    value: String(format: "%.6f, %.6f", place.centerLatitude, place.centerLongitude)
                )

                StatRow(
                    icon: "circle.dashed",
                    label: "Radius",
                    value: "\(Int(place.radiusMeters)) meters"
                )
            }
        }
        .padding()
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(
            RoundedRectangle(cornerRadius: 12)
                .fill(Color(.systemGray6))
        )
    }

    private var customInfoSection: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Custom Info")
                .font(.headline)
                .fontWeight(.bold)

            VStack(spacing: 12) {
                if let userLabel = place.userLabel {
                    StatRow(
                        icon: "tag",
                        label: "Custom Label",
                        value: userLabel
                    )
                }

                if let userNotes = place.userNotes {
                    VStack(alignment: .leading, spacing: 4) {
                        HStack(spacing: 8) {
                            Image(systemName: "note.text")
                                .foregroundColor(.blue)
                                .frame(width: 20)
                            Text("Notes")
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                        Text(userNotes)
                            .font(.body)
                            .padding(.leading, 28)
                    }
                }
            }
        }
        .padding()
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(
            RoundedRectangle(cornerRadius: 12)
                .fill(Color(.systemGray6))
        )
    }

    private func formatDuration(_ duration: KotlinDuration) -> String {
        let minutes = Int(duration.toDouble(unit: .minutes))
        let hours = minutes / 60
        let mins = minutes % 60

        if hours > 24 {
            let days = hours / 24
            let remainingHours = hours % 24
            if remainingHours > 0 {
                return "\(days)d \(remainingHours)h"
            } else {
                return "\(days)d"
            }
        } else if hours > 0 {
            return "\(hours)h \(mins)m"
        } else {
            return "\(mins)m"
        }
    }

    private func formatDate(_ instant: Kotlinx_datetimeInstant) -> String {
        let date = Date(timeIntervalSince1970: TimeInterval(instant.epochSeconds))
        let formatter = DateFormatter()
        formatter.dateStyle = .medium
        return formatter.string(from: date)
    }

    private func categoryIcon(for category: PlaceCategory) -> String {
        switch category {
        case .home: return "house.fill"
        case .work: return "briefcase.fill"
        case .food: return "fork.knife"
        case .shopping: return "cart.fill"
        case .fitness: return "figure.run"
        case .entertainment: return "theatermasks.fill"
        case .travel: return "airplane"
        case .healthcare: return "cross.case.fill"
        case .education: return "book.fill"
        case .religious: return "building.columns.fill"
        case .social: return "person.3.fill"
        case .outdoor: return "tree.fill"
        case .service: return "wrench.and.screwdriver.fill"
        default: return "mappin.circle.fill"
        }
    }

    private func categoryColor(for category: PlaceCategory) -> Color {
        switch category {
        case .home: return .blue
        case .work: return .purple
        case .food: return .orange
        case .shopping: return .green
        case .fitness: return .red
        case .entertainment: return .pink
        case .travel: return .cyan
        case .healthcare: return .red
        case .education: return .indigo
        case .religious: return .brown
        case .social: return .mint
        case .outdoor: return .green
        case .service: return .gray
        default: return .gray
        }
    }

    private func significanceColor(for significance: PlaceSignificance) -> Color {
        switch significance {
        case .primary: return .purple
        case .frequent: return .blue
        case .occasional: return .orange
        case .rare: return .gray
        default: return .gray
        }
    }

    private func significanceBackgroundColor(for significance: PlaceSignificance) -> Color {
        switch significance {
        case .primary: return Color.purple.opacity(0.1)
        case .frequent: return Color.blue.opacity(0.1)
        case .occasional: return Color.orange.opacity(0.05)
        default: return Color(.systemGray6)
        }
    }
}

/// Reusable stat row component
struct StatRow: View {
    let icon: String
    let label: String
    let value: String

    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: icon)
                .foregroundColor(.blue)
                .frame(width: 20)

            VStack(alignment: .leading, spacing: 2) {
                Text(label)
                    .font(.caption)
                    .foregroundColor(.secondary)
                Text(value)
                    .font(.body)
                    .fontWeight(.medium)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
        }
    }
}

// Preview disabled - requires proper Kotlin API
/*
#Preview {
    NavigationView {
        PlaceDetailView(
            place: FrequentPlace(
                id: "1",
                centerLatitude: 37.7749,
                centerLongitude: -122.4194,
                radiusMeters: 50.0,
                name: "My Favorite Cafe",
                address: "123 Main St, San Francisco, CA",
                city: "San Francisco",
                countryCode: "US",
                category: .food,
                categoryConfidence: .high,
                significance: .frequent,
                visitCount: 42,
                totalDuration: KotlinDuration.Companion().parse(isoString: "PT24H30M"),
                firstVisitTime: Kotlinx_datetimeInstant.Companion().fromEpochSeconds(epochSeconds: 1_609_459_200, nanosecondAdjustment: 0),
                lastVisitTime: Kotlinx_datetimeInstant.Companion().fromEpochSeconds(epochSeconds: 1_640_995_200, nanosecondAdjustment: 0),
                userLabel: nil,
                userNotes: nil,
                isFavorite: true,
                userId: "user123",
                createdAt: Kotlinx_datetimeInstant.Companion().fromEpochSeconds(epochSeconds: 1_609_459_200, nanosecondAdjustment: 0),
                updatedAt: Kotlinx_datetimeInstant.Companion().fromEpochSeconds(epochSeconds: 1_640_995_200, nanosecondAdjustment: 0)
            ),
            onToggleFavorite: {},
            onDismiss: {}
        )
    }
}
*/
