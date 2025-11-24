import SwiftUI
import Shared

/**
 * Individual stat card components for the statistics screen.
 */

/**
 * Individual stat card.
 */
struct StatCard: View {
    let title: String
    let value: String
    let icon: String

    var body: some View {
        VStack(spacing: 8) {
            Image(systemName: icon)
                .font(.system(size: 32))
                .foregroundColor(.adaptivePrimary)

            Text(value)
                .font(.system(size: 28, weight: .bold))

            Text(title)
                .font(.subheadline)
                .foregroundColor(.secondary)
        }
        .frame(maxWidth: .infinity)
        .padding(16)
        .background(Color(.systemGray6))
        .cornerRadius(12)
    }
}

/**
 * Overview cards with key metrics.
 */
struct OverviewCards: View {
    let stats: ComprehensiveStatistics

    var body: some View {
        VStack(spacing: 8) {
            HStack(spacing: 8) {
                StatGlassCard(
                    title: "Distance",
                    value: "\(Int(stats.distanceStats.totalDistanceKm)) km",
                    icon: "ruler",
                    tint: .coastalPath
                )
                StatGlassCard(
                    title: "Countries",
                    value: "\(stats.geographicStats.countries.count)",
                    icon: "globe",
                    tint: .seaGlass
                )
            }

            HStack(spacing: 8) {
                StatGlassCard(
                    title: "Places",
                    value: "\(stats.placeStats.totalPlaces)",
                    icon: "mappin.and.ellipse",
                    tint: .blueSlate
                )
                StatGlassCard(
                    title: "Active Days",
                    value: "\(stats.activeDays)",
                    icon: "calendar",
                    tint: .coolSteel
                )
            }
        }
    }
}

/**
 * Distance statistics card.
 */
struct DistanceStatsCard: View {
    let stats: ComprehensiveStatistics

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Distance Overview")
                .font(.headline)

            InfoRow(label: "Total Distance", value: "\(Int(stats.distanceStats.totalDistanceKm)) km")
            InfoRow(label: "Average Speed", value: "\(Int(stats.distanceStats.averageSpeed)) km/h")

            let hours = stats.distanceStats.totalDuration.hours
            let minutes = stats.distanceStats.totalDuration.minutes % 60
            InfoRow(label: "Total Time", value: "\(hours)h \(minutes)m")

            if let mostUsed = stats.distanceStats.mostUsedTransportType {
                InfoRow(label: "Most Used", value: transportName(mostUsed))
            }
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color(.systemBackground))
        .cornerRadius(12)
        .shadow(radius: 2)
    }
}

/**
 * Transport distribution card with bar chart.
 */
struct TransportDistributionCard: View {
    let stats: ComprehensiveStatistics

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Distance by Transport Type")
                .font(.headline)

            let barData = stats.distanceStats.byTransportType.map { type, meters in
                BarData(
                    label: String(transportName(type).prefix(4)),
                    value: Float(meters / 1000),
                    formattedValue: "\(Int(meters / 1000))km",
                    color: transportColor(type)
                )
            }.sorted { $0.value > $1.value }

            BarChartView(data: barData, showValues: true)
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color(.systemBackground))
        .cornerRadius(12)
        .shadow(radius: 2)
    }
}

/**
 * Place statistics card.
 */
struct PlaceStatsCard: View {
    let stats: ComprehensiveStatistics

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Place Overview")
                .font(.headline)

            InfoRow(label: "Total Places", value: "\(stats.placeStats.totalPlaces)")
            InfoRow(label: "Total Visits", value: "\(stats.placeStats.totalVisits)")

            let avgDuration = stats.placeStats.averageVisitDuration
            let avgHours = avgDuration.hours
            let avgMinutes = avgDuration.minutes % 60
            let avgValue = avgHours > 0 ? "\(avgHours)h \(avgMinutes)m" : "\(avgMinutes)m"
            InfoRow(label: "Avg Visit Duration", value: avgValue)

            if let topCategory = stats.placeStats.topCategory {
                InfoRow(label: "Top Category", value: categoryName(topCategory))
            }
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color(.systemBackground))
        .cornerRadius(12)
        .shadow(radius: 2)
    }
}

/**
 * Category distribution card with pie chart.
 */
struct CategoryDistributionCard: View {
    let stats: ComprehensiveStatistics

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Visits by Category")
                .font(.headline)

            let pieData = stats.placeStats.visitsByCategory
                .filter { $0.key.name != "OTHER" || $0.value > 0 }
                .map { category, count in
                    PieData(
                        label: categoryName(category),
                        value: Float(count),
                        color: categoryColor(category)
                    )
                }
                .sorted { $0.value > $1.value }
                .prefix(6)

            if !pieData.isEmpty {
                PieChartView(data: Array(pieData))
            }
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color(.systemBackground))
        .cornerRadius(12)
        .shadow(radius: 2)
    }
}

/**
 * Most visited place card.
 */
struct MostVisitedPlaceCard: View {
    let place: PlaceVisitCount

    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: "mappin.circle.fill")
                .foregroundColor(.adaptivePrimary)

            VStack(alignment: .leading, spacing: 4) {
                Text(place.placeName)
                    .font(.body)

                Text("\(place.visitCount) visits • \(place.totalDuration.hours)h total")
                    .font(.caption)
                    .foregroundColor(.secondary)
            }

            Spacer()

            Text(String(place.category.name.prefix(3)))
                .font(.caption)
                .foregroundColor(.secondary)
        }
        .padding(12)
        .background(Color(.systemBackground))
        .cornerRadius(12)
        .shadow(radius: 2)
    }
}

/**
 * Travel patterns card.
 */
struct TravelPatternsCard: View {
    let stats: ComprehensiveStatistics

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Travel Patterns")
                .font(.headline)

            if let peakDay = stats.travelPatterns.peakTravelDay {
                InfoRow(label: "Most Active Day", value: dayOfWeekName(peakDay))
            }

            if let peakHour = stats.travelPatterns.peakTravelHour {
                let timeRange = timeRangeForHour(peakHour)
                InfoRow(label: "Most Active Time", value: timeRange)
            }

            let split = stats.travelPatterns.weekdayVsWeekend
            InfoRow(
                label: "Weekday vs Weekend",
                value: "\(Int(split.weekdayPercentage))% / \(Int(split.weekendPercentage))%"
            )
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color(.systemBackground))
        .cornerRadius(12)
        .shadow(radius: 2)
    }
}

/**
 * Activity heatmap card.
 */
struct ActivityHeatmapCard: View {
    let stats: ComprehensiveStatistics

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            // Convert weekday activity to heatmap format
            let heatmapData = stats.travelPatterns.weekdayActivity.reduce(into: [String: [Int: Int]]()) { result, entry in
                let dayName = dayOfWeekName(entry.key)
                result[dayName] = stats.travelPatterns.hourlyActivity.mapValues { $0.totalEvents }
            }

            ActivityHeatmapView(data: heatmapData)
        }
        .padding(16)
        .background(Color(.systemBackground))
        .cornerRadius(12)
        .shadow(radius: 2)
    }
}

/**
 * Geographic statistics card.
 */
struct GeographicStatsCard: View {
    let stats: ComprehensiveStatistics

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Geographic Overview")
                .font(.headline)

            InfoRow(label: "Countries Visited", value: "\(stats.geographicStats.countries.count)")
            InfoRow(label: "Cities Visited", value: "\(stats.geographicStats.cities.count)")

            if let homeBase = stats.geographicStats.homeBase {
                InfoRow(label: "Home Base", value: homeBase.city ?? homeBase.name)
            }

            if let furthest = stats.geographicStats.furthestLocation {
                InfoRow(label: "Furthest Location", value: furthest.city ?? furthest.name)
            }
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color(.systemBackground))
        .cornerRadius(12)
        .shadow(radius: 2)
    }
}

/**
 * Top countries card.
 */
struct TopCountriesCard: View {
    let stats: ComprehensiveStatistics

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Top Countries")
                .font(.headline)

            ForEach(Array(stats.geographicStats.topCountries.prefix(5)), id: \.countryCode) { country in
                HStack {
                    VStack(alignment: .leading, spacing: 4) {
                        Text(country.countryCode)
                            .font(.body)
                            .fontWeight(.medium)

                        Text("\(country.cities.count) cities")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }

                    Spacer()

                    Text("\(country.visitCount) visits")
                        .font(.body)
                }

                if country.countryCode != stats.geographicStats.topCountries.last?.countryCode {
                    Divider()
                }
            }
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color(.systemBackground))
        .cornerRadius(12)
        .shadow(radius: 2)
    }
}
