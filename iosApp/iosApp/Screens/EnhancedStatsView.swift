import SwiftUI

/**
 * SwiftUI statistics screen matching Android EnhancedStatsScreen.
 * Shows comprehensive analytics and visualizations.
 */
struct EnhancedStatsView: View {
    @StateObject private var viewModel: EnhancedStatsViewModel
    @State private var scrollOffset: CGFloat = 0

    init(controller: EnhancedStatsController) {
        _viewModel = StateObject(wrappedValue: EnhancedStatsViewModel(controller: controller))
    }

    var body: some View {
        VStack(spacing: 0) {
            // Large flexible navigation bar with chart preview background
            LargeFlexibleNavigationBar(
                title: "Statistics",
                scrollOffset: scrollOffset,
                actions: [
                    NavigationAction(icon: "arrow.clockwise") {
                        viewModel.refresh()
                    }
                ],
                subtitle: {
                    Text(viewModel.selectedPeriod == .year ? "Year Overview" : "Month Overview")
                },
                backgroundContent: {
                    HeroGradientBackground(
                        startColor: Color.lightCyan,
                        endColor: Color.coolSteel
                    )
                }
            )

            if viewModel.isLoading {
                Spacer()
                GlassLoadingIndicator(variant: .morphing, size: 72, color: .coolSteel)
                Spacer()
            } else if let error = viewModel.error {
                ErrorView(error: error) {
                    viewModel.refresh()
                }
            } else if let stats = viewModel.stats {
                StatsContent(stats: stats, viewModel: viewModel, scrollOffset: $scrollOffset)
            } else {
                EmptyStatsView()
            }
        }
        .onAppear {
            viewModel.loadCurrentYear()
        }
    }
}

/**
 * Main statistics content.
 */
private struct StatsContent: View {
    let stats: ComprehensiveStatistics
    let viewModel: EnhancedStatsViewModel
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

            VStack(spacing: 0) {
                // Period selector
                PeriodSelector(
                    selectedPeriod: viewModel.selectedPeriod,
                    onPeriodChange: { viewModel.loadPeriod($0) }
                )

                VStack(spacing: 16) {
                    // Overview section
                    SectionHeader(text: "Overview")
                    OverviewCards(stats: stats)

                    // Distance statistics
                    SectionHeader(text: "Distance Traveled")
                    DistanceStatsCard(stats: stats)

                    // Transport distribution
                    if !stats.distanceStats.byTransportType.isEmpty {
                        TransportDistributionCard(stats: stats)
                    }

                    // Place statistics
                    SectionHeader(text: "Places Visited")
                    PlaceStatsCard(stats: stats)

                    // Category distribution
                    if !stats.placeStats.visitsByCategory.isEmpty {
                        CategoryDistributionCard(stats: stats)
                    }

                    // Most visited places
                    if !stats.placeStats.mostVisitedPlaces.isEmpty {
                        SectionHeader(text: "Most Visited Places")
                        ForEach(Array(stats.placeStats.mostVisitedPlaces.prefix(5)), id: \.placeId) { place in
                            MostVisitedPlaceCard(place: place)
                        }
                    }

                    // Travel patterns
                    SectionHeader(text: "Travel Patterns")
                    TravelPatternsCard(stats: stats)

                    // Activity heatmap
                    if !stats.travelPatterns.weekdayActivity.isEmpty {
                        ActivityHeatmapCard(stats: stats)
                    }

                    // Geographic statistics
                    SectionHeader(text: "Geography")
                    GeographicStatsCard(stats: stats)

                    // Top countries
                    if !stats.geographicStats.topCountries.isEmpty {
                        TopCountriesCard(stats: stats)
                    }
                }
                .padding(16)
                .padding(.bottom, 80) // Add padding for floating tab bar
            }
        }
        .coordinateSpace(name: "scroll")
        .onPreferenceChange(ScrollOffsetPreferenceKey.self) { value in
            scrollOffset = value
        }
    }
}

/**
 * Period selector for Year/Month.
 */
private struct PeriodSelector: View {
    let selectedPeriod: StatsPeriod
    let onPeriodChange: (StatsPeriod) -> Void

    var body: some View {
        HStack(spacing: 8) {
            GlassButton(
                title: "Year",
                icon: selectedPeriod == .year ? "checkmark" : nil,
                variant: .filled,
                isSelected: selectedPeriod == .year,
                tint: .coolSteel
            ) {
                onPeriodChange(.year)
            }

            GlassButton(
                title: "Month",
                icon: selectedPeriod == .month ? "checkmark" : nil,
                variant: .filled,
                isSelected: selectedPeriod == .month,
                tint: .coolSteel
            ) {
                onPeriodChange(.month)
            }
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .glassBackground(material: .ultraThin, tint: .lightCyan, cornerRadius: 8)
    }
}

/**
 * Section header.
 */
private struct SectionHeader: View {
    let text: String

    var body: some View {
        Text(text)
            .font(.title2)
            .fontWeight(.bold)
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.top, 8)
    }
}

/**
 * Overview cards with key metrics.
 */
private struct OverviewCards: View {
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
 * Individual stat card.
 */
private struct StatCard: View {
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
 * Distance statistics card.
 */
private struct DistanceStatsCard: View {
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
                InfoRow(label: "Most Used", value: transportTypeName(mostUsed))
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
private struct TransportDistributionCard: View {
    let stats: ComprehensiveStatistics

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Distance by Transport Type")
                .font(.headline)

            let barData = stats.distanceStats.byTransportType.map { (type, meters) in
                BarData(
                    label: String(transportTypeName(type).prefix(4)),
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
private struct PlaceStatsCard: View {
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
private struct CategoryDistributionCard: View {
    let stats: ComprehensiveStatistics

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Visits by Category")
                .font(.headline)

            let pieData = stats.placeStats.visitsByCategory
                .filter { $0.key.name != "OTHER" || $0.value > 0 }
                .map { (category, count) in
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
private struct MostVisitedPlaceCard: View {
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
private struct TravelPatternsCard: View {
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
private struct ActivityHeatmapCard: View {
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
private struct GeographicStatsCard: View {
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
private struct TopCountriesCard: View {
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

/**
 * Info row for key-value pairs.
 */
private struct InfoRow: View {
    let label: String
    let value: String

    var body: some View {
        HStack {
            Text(label)
                .foregroundColor(.secondary)

            Spacer()

            Text(value)
                .fontWeight(.semibold)
        }
    }
}

/**
 * Empty stats view.
 */
private struct EmptyStatsView: View {
    var body: some View {
        VStack(spacing: 16) {
            Image(systemName: "chart.bar")
                .font(.system(size: 64))
                .foregroundColor(.secondary)

            Text("No statistics available")
                .font(.title2)
                .foregroundColor(.secondary)

            Text("Start tracking to see your travel statistics")
                .font(.body)
                .foregroundColor(.secondary)
        }
    }
}

/**
 * Error view with retry.
 */
private struct ErrorView: View {
    let error: String
    let onRetry: () -> Void

    var body: some View {
        VStack(spacing: 16) {
            Image(systemName: "exclamationmark.triangle")
                .font(.system(size: 64))
                .foregroundColor(.adaptiveWarning)

            Text(error)
                .font(.body)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)

            Button("Retry", action: onRetry)
                .buttonStyle(.borderedProminent)
        }
        .padding()
    }
}

// MARK: - ViewModel

/**
 * ViewModel bridging SwiftUI to Kotlin controller.
 */
class EnhancedStatsViewModel: ObservableObject {
    private let controller: EnhancedStatsController

    @Published var stats: ComprehensiveStatistics?
    @Published var isLoading: Bool = false
    @Published var error: String?
    @Published var selectedPeriod: StatsPeriod = .year

    init(controller: EnhancedStatsController) {
        self.controller = controller
        observeState()
    }

    func loadCurrentYear() {
        let calendar = Calendar.current
        let year = calendar.component(.year, from: Date())
        controller.loadPeriod(period: GetStatsUseCasePeriodYear(year: Int32(year)))
    }

    func loadPeriod(_ period: StatsPeriod) {
        selectedPeriod = period
        let calendar = Calendar.current
        let year = calendar.component(.year, from: Date())

        switch period {
        case .year:
            controller.loadPeriod(period: GetStatsUseCasePeriodYear(year: Int32(year)))
        case .month:
            let month = calendar.component(.month, from: Date())
            controller.loadPeriod(period: GetStatsUseCasePeriodMonth(year: Int32(year), month: Int32(month)))
        }
    }

    func refresh() {
        controller.refresh()
    }

    private func observeState() {
        // TODO: Implement StateFlow observation bridge
        // For now, manually trigger updates
    }
}

enum StatsPeriod {
    case year
    case month
}

// MARK: - Helper Functions

private func transportTypeName(_ type: TransportType) -> String {
    type.name.lowercased().capitalized
}

private func transportColor(_ type: TransportType) -> Color {
    switch type.name {
    case "WALK": return Color.adaptiveSuccess
    case "BIKE": return Color.adaptivePrimary
    case "CAR": return Color.adaptiveWarning
    case "TRAIN": return Color.eveningCategory
    case "PLANE": return Color.morningCategory
    case "BOAT": return Color.waterCategory
    default: return Color.neutralCategory
    }
}

private func categoryName(_ category: PlaceCategory) -> String {
    category.name.lowercased().capitalized
}

private func categoryColor(_ category: PlaceCategory) -> Color {
    switch category.name {
    case "HOME": return Color.adaptiveSuccess
    case "WORK": return Color.adaptivePrimary
    case "FOOD": return Color.morningCategory
    case "SHOPPING": return Color.adaptiveWarning
    case "FITNESS": return Color.eveningCategory
    case "ENTERTAINMENT": return Color.morningCategory
    case "TRAVEL": return Color.waterCategory
    case "HEALTHCARE": return Color.adaptiveWarning
    case "EDUCATION": return Color.adaptivePrimary
    case "RELIGIOUS": return Color.neutralCategory
    case "SOCIAL": return Color.morningCategory
    case "OUTDOOR": return Color.adaptiveSuccess
    case "SERVICE": return Color.neutralCategory
    default: return Color.neutralCategory
    }
}

private func dayOfWeekName(_ day: DayOfWeek) -> String {
    switch day.name {
    case "MONDAY": return "Mon"
    case "TUESDAY": return "Tue"
    case "WEDNESDAY": return "Wed"
    case "THURSDAY": return "Thu"
    case "FRIDAY": return "Fri"
    case "SATURDAY": return "Sat"
    case "SUNDAY": return "Sun"
    default: return day.name
    }
}

private func timeRangeForHour(_ hour: Int32) -> String {
    switch hour {
    case 0..<6: return "Night (12AM-6AM)"
    case 6..<12: return "Morning (6AM-12PM)"
    case 12..<18: return "Afternoon (12PM-6PM)"
    default: return "Evening (6PM-12AM)"
    }
}

// MARK: - Duration Extension

extension KotlinDuration {
    var hours: Int {
        Int(inWholeHours)
    }

    var minutes: Int {
        Int(inWholeMinutes)
    }
}
