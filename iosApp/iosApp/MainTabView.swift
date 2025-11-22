import SwiftUI
import shared

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
        var baseTabs = [
            TabItem(title: "Stats", icon: "chart.bar.fill", tag: 0),
            TabItem(title: "Timeline", icon: "clock.fill", tag: 1),
            TabItem(title: "Map", icon: "map.fill", tag: 2),
            TabItem(title: "Trips", icon: "suitcase.fill", tag: 3),
            TabItem(title: "Places", icon: "mappin.circle.fill", tag: 4),
            TabItem(title: "Photos", icon: "photo.fill", tag: 5),
            TabItem(title: "Settings", icon: "gear", tag: 6)
        ]

        #if DEBUG
        baseTabs.append(TabItem(title: "Showcase", icon: "sparkles", tag: 7))
        #endif

        return baseTabs
    }

    var body: some View {
        ZStack {
            // Tab content
            Group {
                switch selectedTab {
                case 0:
                    EnhancedStatsView(controller: appComponent.enhancedStatsController)
                case 1:
                    EnhancedTimelineView(controller: appComponent.enhancedTimelineController)
                case 2:
                    MapScreen(mapController: appComponent.mapController)
                case 3:
                    TripsView(appComponent: appComponent)
                case 4:
                    SimplePlacesView(placesController: appComponent.placesController)
                case 5:
                    PhotoGalleryView(appComponent: appComponent)
                case 6:
                    EnhancedSettingsView(controller: appComponent.settingsController, appComponent: appComponent)
                #if DEBUG
                case 7:
                    ComponentShowcaseView()
                #endif
                default:
                    EnhancedStatsView(controller: appComponent.enhancedStatsController)
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
    }
}
