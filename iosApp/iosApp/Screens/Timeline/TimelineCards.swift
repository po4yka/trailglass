import SwiftUI
import Shared

/**
 * Day marker card.
 */
struct DayMarkerCard: View {
    let text: String
    let icon: String

    var body: some View {
        HStack(spacing: 8) {
            Image(systemName: icon)
                .foregroundColor(.adaptivePrimary)
            Text(text)
                .font(.body)
                .fontWeight(.medium)
        }
        .padding(12)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color(.systemGray6))
        .cornerRadius(8)
    }
}

/**
 * Enhanced visit card.
 */
struct EnhancedVisitCard: View {
    let visit: PlaceVisit

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            // Header
            HStack(alignment: .top, spacing: 12) {
                Image(systemName: categoryIcon(visit.category as? PlaceCategory ?? .other))
                    .font(.title2)
                    .foregroundColor(.blue)

                VStack(alignment: .leading, spacing: 4) {
                    HStack(spacing: 8) {
                        Text(visit.displayName)
                            .font(.headline)

                        if visit.isFavorite {
                            Image(systemName: "star.fill")
                                .foregroundColor(.yellow)
                                .font(.caption)
                        }
                    }

                    if let city = visit.city {
                        Text(city)
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                }

                Spacer()
            }

            // Address (if no POI name)
            if let address = visit.approximateAddress, visit.userLabel == nil, visit.poiName == nil {
                Text(address)
                    .font(.caption)
                    .foregroundColor(.secondary)
            }

            // User notes
            if let notes = visit.userNotes {
                HStack(spacing: 8) {
                    Image(systemName: "note.text")
                        .font(.caption)
                    Text(notes)
                        .font(.caption)
                }
                .padding(8)
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(Color(.systemGray6))
                .cornerRadius(4)
            }

            // Metadata chips
            HStack(spacing: 8) {
                HStack(spacing: 4) {
                    Image(systemName: "clock")
                        .font(.caption)
                    Text(formatDuration(visit.duration))
                        .font(.caption)
                }
                .padding(.horizontal, 8)
                .padding(.vertical, 4)
                .background(Color(.systemGray5))
                .cornerRadius(4)

                if visit.category.name != "OTHER" {
                    Text(categoryName(visit.category as? PlaceCategory ?? .other))
                        .font(.caption)
                        .padding(.horizontal, 8)
                        .padding(.vertical, 4)
                        .background(Color(.systemGray5))
                        .cornerRadius(4)
                }
            }
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color.blue.opacity(0.1))
        .cornerRadius(12)
    }
}

/**
 * Enhanced route card.
 */
struct EnhancedRouteCard: View {
    let route: RouteSegment

    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: transportIcon(route.transportType))
                .font(.title2)
                .foregroundColor(.green)

            VStack(alignment: .leading, spacing: 4) {
                Text(transportName(route.transportType))
                    .font(.headline)

                HStack(spacing: 8) {
                    Text("\(Int(route.distanceMeters / 1000)) km")
                        .font(.caption)

                    let duration = Double(route.endTime.epochSeconds) - Double(route.startTime.epochSeconds)
                    if duration > 0 {
                        Text("• \(Int(duration / 60)) min")
                            .font(.caption)
                    }
                }
                .foregroundColor(.secondary)
            }

            Spacer()

            // Confidence indicator
            if route.confidence < 0.7 {
                Image(systemName: "questionmark.circle")
                    .foregroundColor(.secondary)
            }
        }
        .padding(16)
        .background(Color.green.opacity(0.1))
        .cornerRadius(12)
    }
}

/**
 * Day summary card.
 */
struct DaySummaryCard: View {
    let summary: GetTimelineUseCaseTimelineItemUIDaySummaryUI

    var body: some View {
        SummaryGlassCard(
            title: formatLocalDate(summary.date),
            subtitle: nil,
            icon: "calendar",
            stats: [
                (icon: "mappin.and.ellipse", label: "Places", value: "\(summary.totalVisits)"),
                (icon: "ruler", label: "Distance", value: "\(Int(summary.totalDistanceMeters / 1000)) km"),
                (icon: "figure.walk", label: "Routes", value: "\(summary.totalRoutes)")
            ]
        )
    }
}

/**
 * Week summary card.
 */
struct WeekSummaryCard: View {
    let summary: GetTimelineUseCaseTimelineItemUIWeekSummaryUI

    var body: some View {
        SummaryGlassCard(
            title: "Week Summary",
            subtitle: "\(formatLocalDate(summary.weekStart)) - \(formatLocalDate(summary.weekEnd))",
            icon: "calendar.badge.clock",
            stats: [
                (icon: "mappin.and.ellipse", label: "Places", value: "\(summary.totalVisits)"),
                (icon: "ruler", label: "Distance", value: "\(Int(summary.totalDistanceMeters / 1000)) km"),
                (icon: "calendar", label: "Days Active", value: "\(summary.activeDays)")
            ]
        )
    }
}

/**
 * Month summary card.
 */
struct MonthSummaryCard: View {
    let summary: GetTimelineUseCaseTimelineItemUIMonthSummaryUI

    var body: some View {
        VStack(spacing: 12) {
            SummaryGlassCard(
                title: "\(summary.month.name) \(summary.year)",
                subtitle: nil,
                icon: "calendar.circle",
                stats: [
                    (icon: "mappin.and.ellipse", label: "Places", value: "\(summary.totalVisits)"),
                    (icon: "ruler", label: "Distance", value: "\(Int(summary.totalDistanceMeters / 1000)) km"),
                    (icon: "calendar.badge.clock", label: "Weeks", value: "\(summary.activeWeeks)")
                ]
            )

            if !summary.topCategories.isEmpty {
                HStack(spacing: 8) {
                    ForEach(summary.topCategories.prefix(3), id: \.self) { category in
                        GlassFilterChip(
                            label: categoryName(category as? PlaceCategory ?? .other),
                            icon: categoryIcon(category as? PlaceCategory ?? .other),
                            isSelected: false,
                            tint: .blueSlate
                        ) {}
                    }
                }
            }
        }
    }
}

/**
 * Summary stat item.
 */
struct SummaryStatItem: View {
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
            Text(label)
                .font(.caption)
                .foregroundColor(.secondary)
        }
    }
}
