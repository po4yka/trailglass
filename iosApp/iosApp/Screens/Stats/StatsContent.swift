import SwiftUI

/**
 * Main content layout for the statistics screen.
 */
struct StatsContent: View {
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
