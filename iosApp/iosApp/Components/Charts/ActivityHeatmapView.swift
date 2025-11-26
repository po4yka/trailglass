import SwiftUI

/**
 * SwiftUI activity heatmap with Liquid Glass aesthetic.
 * Glass cells with gradient intensity and interactive selection.
 */
struct ActivityHeatmapView: View {
    let data: [String: [Int: Int]] // day -> hour -> activity count
    let lowColor: Color
    let highColor: Color
    @State private var selectedCell: CellPosition?
    @State private var animatedIntensities: [String: [Int: Float]] = [:]

    init(data: [String: [Int: Int]], lowColor: Color = Color.lightCyan, highColor: Color = Color.blueSlate) {
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
        VStack(alignment: .leading, spacing: 12) {
            // Glass header
            HStack(spacing: 8) {
                Image(systemName: "chart.bar.xaxis")
                    .foregroundColor(.coolSteel)
                Text("Activity Heatmap")
                    .font(.headline)
            }

                // Fixed Day Labels and Scrollable Grid
                HStack(alignment: .top, spacing: 0) {
                    // Fixed Day Labels Column
                    VStack(spacing: 3) {
                        ForEach(Array(days.enumerated()), id: \.offset) { _, day in
                            Text(day)
                                .font(.caption2)
                                .fontWeight(.semibold)
                                .foregroundColor(.blueSlate)
                                .frame(width: 36, height: 18, alignment: .leading)
                        }
                    }
                    .padding(.top, 20) // Offset for header row

                    // Scrollable Content
                    ScrollView(.horizontal, showsIndicators: false) {
                        VStack(alignment: .leading, spacing: 0) {
                            // Hour labels (top)
                            HStack(spacing: 2) {
                                ForEach(0..<24, id: \.self) { hour in
                                    let label: String = {
                                        switch hour {
                                        case 0: return "12am"
                                        case 6: return "6am"
                                        case 12: return "12pm"
                                        case 18: return "6pm"
                                        default: return ""
                                        }
                                    }()

                                    Text(label)
                                        .font(.caption2)
                                        .fontWeight(.medium)
                                        .foregroundColor(.coolSteel)
                                        .frame(width: 32, alignment: .center)
                                }
                            }
                            .padding(.bottom, 2)

                            // Heatmap grid
                            VStack(spacing: 3) {
                                ForEach(Array(days.enumerated()), id: \.offset) { _, day in
                                    HStack(spacing: 2) {
                                        ForEach(0..<24, id: \.self) { hour in
                                            let activity = data[day]?[hour] ?? 0
                                            let animatedIntensity = animatedIntensities[day]?[hour] ?? 0
                                            let isSelected = selectedCell?.day == day && selectedCell?.hour == hour

                                            GlassHeatmapCell(
                                                intensity: animatedIntensity,
                                                lowColor: lowColor,
                                                highColor: highColor,
                                                isSelected: isSelected,
                                                activityCount: activity
                                            )
                                            .frame(width: 32) // Fixed width for accessibility
                                            .onTapGesture {
                                                withAnimation(MotionConfig.quickSpring) {
                                                    if isSelected {
                                                        selectedCell = nil
                                                    } else {
                                                        selectedCell = CellPosition(day: day, hour: hour)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Glass legend
                HStack(spacing: 8) {
                    Text("Less")
                        .font(.caption2)
                        .foregroundColor(.secondary)

                    HStack(spacing: 3) {
                        ForEach(0..<5, id: \.self) { index in
                            RoundedRectangle(cornerRadius: 3)
                                .fill(
                                    LinearGradient(
                                        colors: [
                                            colorForIntensity(Float(index) / 4.0).opacity(0.7),
                                            colorForIntensity(Float(index) / 4.0).opacity(0.5)
                                        ],
                                        startPoint: .topLeading,
                                        endPoint: .bottomTrailing
                                    )
                                )
                                .overlay(
                                    RoundedRectangle(cornerRadius: 3)
                                        .strokeBorder(Color.white.opacity(0.3), lineWidth: 0.5)
                                )
                                .frame(height: 14)
                        }
                    }
                    .frame(maxWidth: .infinity)

                    Text("More")
                        .font(.caption2)
                        .foregroundColor(.secondary)
                }
                .padding(.top, 12)
            }
        }
        .onAppear {
            animateCells()
        }
        .onChange(of: data) { _ in
            animateCells()
        }
    }

    private func animateCells() {
        for (dayIndex, day) in days.enumerated() {
            for hour in 0..<24 {
                let activity = data[day]?[hour] ?? 0
                let intensity = Float(activity) / Float(maxActivity)
                let delay = Double(dayIndex * 24 + hour) * 0.01

                DispatchQueue.main.asyncAfter(deadline: .now() + delay) {
                    withAnimation(MotionConfig.quickSpring) {
                        if animatedIntensities[day] == nil {
                            animatedIntensities[day] = [:]
                        }
                        animatedIntensities[day]?[hour] = intensity
                    }
                }
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
 * Cell position for selection tracking.
 */
private struct CellPosition: Equatable {
    let day: String
    let hour: Int
}

/**
 * Glass heatmap cell.
 */
private struct GlassHeatmapCell: View {
    let intensity: Float
    let lowColor: Color
    let highColor: Color
    let isSelected: Bool
    let activityCount: Int
    @Environment(\.colorScheme) var colorScheme

    private var cellColor: Color {
        let red = lowColor.components.red + (highColor.components.red - lowColor.components.red) * CGFloat(intensity)
        let green = lowColor.components.green + (highColor.components.green - lowColor.components.green) * CGFloat(intensity)
        let blue = lowColor.components.blue + (highColor.components.blue - lowColor.components.blue) * CGFloat(intensity)
        return Color(red: red, green: green, blue: blue)
    }

    var body: some View {
        RoundedRectangle(cornerRadius: 3)
            .fill(
                LinearGradient(
                    colors: [
                        cellColor.opacity(0.7),
                        cellColor.opacity(0.5)
                    ],
                    startPoint: .topLeading,
                    endPoint: .bottomTrailing
                )
            )
            .overlay(
                RoundedRectangle(cornerRadius: 3)
                    .fill(.ultraThinMaterial)
                    .opacity(intensity > 0 ? 0.2 : 0.05)
            )
            .overlay(
                RoundedRectangle(cornerRadius: 3)
                    .strokeBorder(
                        isSelected ? cellColor : Color.white.opacity(colorScheme == .dark ? 0.2 : 0.3),
                        lineWidth: isSelected ? 2 : 0.5
                    )
            )
            .frame(height: 18)
            .scaleEffect(isSelected ? 1.15 : 1.0)
            .shadow(
                color: isSelected ? cellColor.opacity(0.5) : Color.clear,
                radius: isSelected ? 6 : 0
            )
            .overlay(
                Group {
                    if isSelected && activityCount > 0 {
                        Text("\(activityCount)")
                            .font(.system(size: 8, weight: .bold))
                            .foregroundColor(.white)
                            .shadow(color: Color.black.opacity(0.5), radius: 1)
                    }
                }
            )
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
