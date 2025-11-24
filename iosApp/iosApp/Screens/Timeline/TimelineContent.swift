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
        // Timeline item rendering commented out - requires rebuilt Kotlin framework
        Text("Timeline item: \(item.id)")
            .foregroundColor(.secondary)
            .padding()
            .glassEffectTinted(.coastalPath, opacity: 0.6)
            .cornerRadius(8)
    }
}
