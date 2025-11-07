import SwiftUI

/**
 * SwiftUI activity heatmap matching Android ActivityHeatmap.
 * Shows activity intensity by hour and day of week.
 */
struct ActivityHeatmapView: View {
    let data: [String: [Int: Int]] // day -> hour -> activity count
    let lowColor: Color
    let highColor: Color

    init(data: [String: [Int: Int]], lowColor: Color = Color(.systemGray5), highColor: Color = .blue) {
        self.data = data
        self.lowColor = lowColor
        self.highColor = highColor
    }

    private var maxActivity: Int {
        data.values.flatMap { $0.values }.max() ?? 1
    }

    private let days = ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"]
    private let hourLabels = ["6am", "12pm", "6pm", "12am"]

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Activity Heatmap")
                .font(.headline)

            VStack(spacing: 0) {
                // Hour labels
                HStack(spacing: 0) {
                    Spacer()
                        .frame(width: 36)

                    ForEach(0..<hourLabels.count, id: \.self) { index in
                        Text(hourLabels[index])
                            .font(.caption2)
                            .frame(maxWidth: .infinity, alignment: index == 0 ? .leading : .center)
                    }
                }
                .padding(.bottom, 4)

                // Heatmap grid
                VStack(spacing: 2) {
                    ForEach(days, id: \.self) { day in
                        HStack(spacing: 2) {
                            // Day label
                            Text(day)
                                .font(.caption2)
                                .frame(width: 32, alignment: .leading)

                            // Hour cells
                            ForEach(0..<24, id: \.self) { hour in
                                let activity = data[day]?[hour] ?? 0
                                let intensity = Float(activity) / Float(maxActivity)

                                Rectangle()
                                    .fill(colorForIntensity(intensity))
                                    .frame(height: 20)
                                    .cornerRadius(2)
                            }
                        }
                    }
                }

                // Legend
                HStack(spacing: 4) {
                    Text("Less")
                        .font(.caption2)

                    HStack(spacing: 2) {
                        ForEach(0..<5, id: \.self) { index in
                            Rectangle()
                                .fill(colorForIntensity(Float(index) / 4.0))
                                .frame(height: 12)
                                .cornerRadius(2)
                        }
                    }
                    .frame(maxWidth: .infinity)

                    Text("More")
                        .font(.caption2)
                }
                .padding(.top, 8)
            }
        }
    }

    private func colorForIntensity(_ intensity: Float) -> Color {
        let red = lowColor.components.red + (highColor.components.red - lowColor.components.red) * CGFloat(intensity)
        let green = lowColor.components.green + (highColor.components.green - lowColor.components.green) * CGFloat(intensity)
        let blue = lowColor.components.blue + (highColor.components.blue - lowColor.components.blue) * CGFloat(intensity)

        return Color(red: red, green: green, blue: blue)
    }
}

/**
 * Extension to get color components.
 */
extension Color {
    var components: (red: CGFloat, green: CGFloat, blue: CGFloat, opacity: CGFloat) {
        #if canImport(UIKit)
        typealias NativeColor = UIColor
        #elseif canImport(AppKit)
        typealias NativeColor = NSColor
        #endif

        var r: CGFloat = 0
        var g: CGFloat = 0
        var b: CGFloat = 0
        var o: CGFloat = 0

        guard NativeColor(self).getRed(&r, green: &g, blue: &b, alpha: &o) else {
            return (0, 0, 0, 0)
        }

        return (r, g, b, o)
    }
}
