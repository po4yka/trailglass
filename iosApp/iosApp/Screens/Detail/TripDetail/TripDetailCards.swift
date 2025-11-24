import SwiftUI
import Shared
import MapKit

// MARK: - Trip Overview Card

/// Trip overview card showing basic info
struct TripOverviewCard: View {
    let tripRoute: TripRoute
    let tripName: String?

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                VStack(alignment: .leading, spacing: 4) {
                    Text(tripName ?? tripRoute.tripId)
                        .font(.title2)
                        .fontWeight(.bold)

                    Text(formatDateRange(tripRoute.startTime, tripRoute.endTime))
                        .font(.subheadline)
                        .foregroundColor(.secondary)

                    let duration = tripRoute.endTime.epochSeconds - tripRoute.startTime.epochSeconds
                    Text(formatDuration(duration))
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                }

                Spacer()
            }
        }
        .padding()
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color(.systemGray6))
        .cornerRadius(12)
    }

    private func formatDateRange(_ start: Kotlinx_datetimeInstant, _ end: Kotlinx_datetimeInstant) -> String {
        let formatter = DateFormatter()
        formatter.dateStyle = .medium
        formatter.timeStyle = .short

        let startDate = Date(timeIntervalSince1970: TimeInterval(start.epochSeconds))
        let startStr = formatter.string(from: startDate)

        let endDate = Date(timeIntervalSince1970: TimeInterval(end.epochSeconds))
        let endStr = formatter.string(from: endDate)
        return "\(startStr) - \(endStr)"
    }

    private func formatDuration(_ duration: KotlinDuration) -> String {
        let hours = duration.inWholeHours
        let minutes = duration.inWholeMinutes % 60

        if hours > 24 {
            let days = hours / 24
            let remainingHours = hours % 24
            return "\(days)d \(remainingHours)h"
        } else if hours > 0 {
            return "\(hours)h \(minutes)m"
        } else {
            return "\(minutes)m"
        }
    }
}

// MARK: - Trip Statistics Cards

/// Trip statistics cards grid
struct TripStatisticsCards: View {
    let statistics: RouteStatistics

    var body: some View {
        VStack(spacing: 12) {
            HStack(spacing: 12) {
                TripStatCard(
                    icon: "figure.walk",
                    label: "Distance",
                    value: formatDistance(statistics.totalDistanceMeters),
                    color: .blue
                )

                TripStatCard(
                    icon: "mappin.and.ellipse",
                    label: "Places",
                    value: "\(statistics.numberOfLocations)",
                    color: .green
                )
            }

            HStack(spacing: 12) {
                TripStatCard(
                    icon: "arrow.up.right",
                    label: "Max Speed",
                    value: formatSpeed(Double(statistics.maxSpeedMps ?? 0)),
                    color: .orange
                )

                TripStatCard(
                    icon: "speedometer",
                    label: "Avg Speed",
                    value: formatSpeed(Double(truncating: (statistics.averageSpeedMps ?? 0) as NSNumber)),
                    color: .purple
                )
            }
        }
    }

    private func formatDistance(_ meters: Double) -> String {
        if meters < 1000 {
            return String(format: "%.0f m", meters)
        } else {
            return String(format: "%.2f km", meters / 1000)
        }
    }

    private func formatSpeed(_ mps: Double?) -> String {
        guard let mps = mps, mps > 0 else { return "-" }
        let kmh = mps * 3.6
        return String(format: "%.1f km/h", kmh)
    }
}

/// Individual stat card
struct TripStatCard: View {
    let icon: String
    let label: String
    let value: String
    let color: Color

    var body: some View {
        VStack(spacing: 8) {
            Image(systemName: icon)
                .font(.title2)
                .foregroundColor(color)

            Text(value)
                .font(.title3)
                .fontWeight(.bold)

            Text(label)
                .font(.caption)
                .foregroundColor(.secondary)
        }
        .frame(maxWidth: .infinity)
        .padding()
        .background(Color(.systemGray6))
        .cornerRadius(12)
    }
}

// MARK: - Route Map Preview

/// Route map preview
struct RouteMapPreview: View {
    let route: TripRoute

    @State private var region = MKCoordinateRegion(
        center: CLLocationCoordinate2D(latitude: 0, longitude: 0),
        span: MKCoordinateSpan(latitudeDelta: 0.05, longitudeDelta: 0.05)
    )

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Route Map")
                .font(.headline)
                .padding(.horizontal)

            Rectangle()
                .fill(Color.gray.opacity(0.3))
                .overlay(
                    Text("Map Preview")
                        .foregroundColor(.secondary)
                )
            }
            .frame(height: 200)
            .cornerRadius(12)
            .onAppear {
                if let first = route.fullPath.first {
                    region.center = CLLocationCoordinate2D(
                        latitude: first.latitude,
                        longitude: first.longitude
                    )
                }
            }
        }
    }

// MARK: - Transport Breakdown Card

/// Transport breakdown card
struct TransportBreakdownCard: View {
    let distribution: [TransportType: Double]

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Transport Types")
                .font(.headline)

            ForEach(Array(distribution.keys), id: \.self) { transportType in
                if let distance = distribution[transportType] {
                    HStack {
                        Image(systemName: transportIcon(transportType.name))
                            .foregroundColor(.blue)
                            .frame(width: 24)

                        Text(transportType.name.lowercased().capitalized)
                            .font(.body)

                        Spacer()

                        Text(String(format: "%.2f km", distance / 1000))
                            .font(.body)
                            .fontWeight(.medium)
                    }
                }
            }
        }
        .padding()
        .background(Color(.systemGray6))
        .cornerRadius(12)
    }

    private func transportIcon(_ type: String) -> String {
        switch type.lowercased() {
        case "walk": return "figure.walk"
        case "bike": return "bicycle"
        case "car": return "car.fill"
        case "train": return "train.side.front.car"
        case "plane": return "airplane"
        case "boat": return "ferry.fill"
        default: return "location.fill"
        }
    }
}

// MARK: - Places Visited Card

/// Places visited card
struct PlacesVisitedCard: View {
    let places: [String]

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Places Visited")
                .font(.headline)

            ForEach(places.prefix(10), id: \.self) { place in
                HStack {
                    Image(systemName: "mappin.circle.fill")
                        .foregroundColor(.blue)
                        .font(.caption)

                    Text(place)
                        .font(.body)

                    Spacer()
                }
                .padding(.vertical, 4)
            }

            if places.count > 10 {
                Text("And \(places.count - 10) more places...")
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
        }
        .padding()
        .background(Color(.systemGray6))
        .cornerRadius(12)
    }
}
