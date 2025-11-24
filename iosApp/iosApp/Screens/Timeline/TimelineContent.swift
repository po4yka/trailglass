import SwiftUI
import Shared

/**
 * Timeline content with items.
 */
struct TimelineContent: View {
    let items: [GetTimelineUseCaseTimelineItemUI]
    let zoomLevel: TimelineZoomLevel
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

            LazyVStack(spacing: 12) {
                ForEach(Array(items.enumerated()), id: \.offset) { _, item in
                    TimelineItemView(item: item)
                }
            }
            .padding(16)
            .padding(.bottom, 80) // Add padding for floating tab bar
        }
        .coordinateSpace(name: "scroll")
        .onPreferenceChange(ScrollOffsetPreferenceKey.self) { value in
            scrollOffset = value
        }
    }
}

/**
 * Individual timeline item view.
 */
struct TimelineItemView: View {
    let item: GetTimelineUseCaseTimelineItemUI

    var body: some View {
        Group {
            if let dayStart = item as? GetTimelineUseCaseTimelineItemUIDayStartUI {
                DayMarkerCard(text: "Day Start", icon: "sun.max")
            } else if let dayEnd = item as? GetTimelineUseCaseTimelineItemUIDayEndUI {
                DayMarkerCard(text: "Day End", icon: "moon.stars")
            } else if let visit = item as? GetTimelineUseCaseTimelineItemUIVisitUI {
                VisitGlassCard(visit: visit.placeVisit)
            } else if let route = item as? GetTimelineUseCaseTimelineItemUIRouteUI {
                RouteGlassCard(route: route.routeSegment)
            } else if let daySummary = item as? GetTimelineUseCaseTimelineItemUIDaySummaryUI {
                DaySummaryCard(summary: daySummary)
            } else if let weekSummary = item as? GetTimelineUseCaseTimelineItemUIWeekSummaryUI {
                WeekSummaryCard(summary: weekSummary)
            } else if let monthSummary = item as? GetTimelineUseCaseTimelineItemUIMonthSummaryUI {
                MonthSummaryCard(summary: monthSummary)
            }
        }
    }
}
