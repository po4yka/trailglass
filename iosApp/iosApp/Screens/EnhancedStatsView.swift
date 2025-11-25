import SwiftUI
import Shared

/**
 * SwiftUI statistics screen matching Android EnhancedStatsScreen.
 * Shows comprehensive analytics and visualizations.
 */
struct EnhancedStatsView: View {
    @StateObject private var viewModel: EnhancedStatsViewModel
    @State private var scrollOffset: CGFloat = 0
    var onSettingsTapped: (() -> Void)?

    init(controller: EnhancedStatsController, onSettingsTapped: (() -> Void)? = nil) {
        _viewModel = StateObject(wrappedValue: EnhancedStatsViewModel(controller: controller))
        self.onSettingsTapped = onSettingsTapped
    }

    var body: some View {
        ZStack(alignment: .top) {
            // Background color to fill the entire screen
            Color(.systemBackground)
                .ignoresSafeArea()

            VStack(spacing: 0) {
                // Large flexible navigation bar with chart preview background
                LargeFlexibleNavigationBar(
                    title: String(localized: "screen.statistics"),
                    scrollOffset: scrollOffset,
                    actions: [
                        NavigationAction(icon: "arrow.clockwise") {
                            viewModel.refresh()
                        },
                        NavigationAction(icon: "gear") {
                            onSettingsTapped?()
                        }
                    ],
                    subtitle: {
                        Text(viewModel.selectedPeriod == .year ? String(localized: "stats.year_overview") : String(localized: "stats.month_overview"))
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
                    Text(String(localized: "stats.no_stats"))
                        .foregroundColor(.secondary)
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                }
            }
        }
        .ignoresSafeArea(edges: .top)
    }
}
