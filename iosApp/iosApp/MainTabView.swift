import SwiftUI
import Shared

/// Main tab navigation view for TrailGlass iOS app
/// Uses floating tab bar with Liquid Glass design
struct MainTabView: View {
    let appComponent: AppComponent
    @State private var selectedTab: Int = 0
    @State private var scrollOffset: CGFloat = 0

    init(appComponent: AppComponent) {
        self.appComponent = appComponent
    }

    private var tabs: [TabItem] {
        // HIG guidelines: max 5 items in tab bar
        // Using 4 main tabs: Stats, Timeline, Map, Trips
        // Settings moved to toolbar, Photos/Places accessible via nested navigation
        var baseTabs = [
            TabItem(title: String(localized: "screen.statistics"), icon: "chart.bar.fill", tag: 0),
            TabItem(title: String(localized: "screen.timeline"), icon: "clock.fill", tag: 1),
            TabItem(title: String(localized: "screen.map"), icon: "map.fill", tag: 2),
            TabItem(title: String(localized: "screen.trips"), icon: "suitcase.fill", tag: 3)
        ]

        #if DEBUG
        baseTabs.append(TabItem(title: String(localized: "screen.showcase"), icon: "sparkles", tag: 4))
        #endif

        return baseTabs
    }

    // State for presenting Settings sheet
    @State private var showSettingsSheet = false

    var body: some View {
        ZStack {
            // Tab content
            Group {
                switch selectedTab {
                case 0:
                    EnhancedStatsView(
                        controller: appComponent.enhancedStatsController,
                        onSettingsTapped: { showSettingsSheet = true }
                    )
                case 1:
                    EnhancedTimelineView(controller: appComponent.enhancedTimelineController, locationTrackingController: appComponent.locationTrackingController, appComponent: appComponent)
                case 2:
                    MapScreen(mapController: appComponent.mapController, appComponent: appComponent)
                case 3:
                    TripsView(appComponent: appComponent)
                #if DEBUG
                case 4:
                    ComponentShowcaseView()
                #endif
                default:
                    EnhancedStatsView(
                        controller: appComponent.enhancedStatsController,
                        onSettingsTapped: { showSettingsSheet = true }
                    )
                }
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)

            // Floating tab bar
            FloatingTabBar(
                selection: $selectedTab,
                scrollOffset: $scrollOffset,
                tabs: tabs
            )
        }
        .sheet(isPresented: $showSettingsSheet) {
            NavigationStack {
                EnhancedSettingsView(controller: appComponent.settingsController, appComponent: appComponent)
                    .toolbar {
                        ToolbarItem(placement: .topBarLeading) {
                            Button(String(localized: "action.done")) {
                                showSettingsSheet = false
                            }
                            .accessibilityLabel(String(localized: "accessibility.done"))
                            .accessibilityHint(String(localized: "accessibility.close_settings"))
                        }
                    }
            }
        }
    }
}
