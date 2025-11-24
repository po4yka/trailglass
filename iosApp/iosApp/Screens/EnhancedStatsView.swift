import SwiftUI
import Shared

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
