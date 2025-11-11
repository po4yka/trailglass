import SwiftUI

/**
 * SwiftUI pie chart component matching Android PieChart.
 */
struct PieChartView: View {
    let data: [PieData]
    let showLegend: Bool

    init(data: [PieData], showLegend: Bool = true) {
        self.data = data
        self.showLegend = showLegend
    }

    private var total: Float {
        data.map { $0.value }.reduce(0, +)
    }

    var body: some View {
        VStack(spacing: 16) {
            // Pie chart
            if total > 0 {
                ZStack {
                    ForEach(0..<data.count, id: \.self) { index in
                        PieSlice(
                            startAngle: startAngle(for: index),
                            endAngle: endAngle(for: index),
                            color: data[index].color
                        )
                    }
                }
                .frame(width: 200, height: 200)
            }

            // Legend
            if showLegend {
                VStack(spacing: 8) {
                    ForEach(data.indices, id: \.self) { index in
                        LegendItem(
                            color: data[index].color,
                            label: data[index].label,
                            value: percentage(for: index)
                        )
                    }
                }
            }
        }
    }

    private func startAngle(for index: Int) -> Angle {
        let sum = data.prefix(index).map { $0.value }.reduce(0, +)
        return .degrees(Double(sum / total) * 360 - 90)
    }

    private func endAngle(for index: Int) -> Angle {
        let sum = data.prefix(index + 1).map { $0.value }.reduce(0, +)
        return .degrees(Double(sum / total) * 360 - 90)
    }

    private func percentage(for index: Int) -> String {
        let percent = Int((data[index].value / total) * 100)
        return "\(percent)%"
    }
}

/**
 * Pie slice shape.
 */
private struct PieSlice: View {
    let startAngle: Angle
    let endAngle: Angle
    let color: Color

    var body: some View {
        GeometryReader { geometry in
            Path { path in
                let center = CGPoint(x: geometry.size.width / 2, y: geometry.size.height / 2)
                let radius = min(geometry.size.width, geometry.size.height) / 2
                let innerRadius = radius * 0.6

                path.addArc(
                    center: center,
                    radius: radius,
                    startAngle: startAngle,
                    endAngle: endAngle,
                    clockwise: false
                )

                path.addLine(to: CGPoint(
                    x: center.x + innerRadius * cos(CGFloat(endAngle.radians)),
                    y: center.y + innerRadius * sin(CGFloat(endAngle.radians))
                ))

                path.addArc(
                    center: center,
                    radius: innerRadius,
                    startAngle: endAngle,
                    endAngle: startAngle,
                    clockwise: true
                )

                path.closeSubpath()
            }
            .fill(color)
        }
    }
}

/**
 * Legend item.
 */
private struct LegendItem: View {
    let color: Color
    let label: String
    let value: String

    var body: some View {
        HStack(spacing: 8) {
            Circle()
                .fill(color)
                .frame(width: 12, height: 12)

            Text(label)
                .font(.caption)

            Spacer()

            Text(value)
                .font(.caption)
                .fontWeight(.bold)
        }
    }
}

/**
 * Pie data model.
 */
struct PieData {
    let label: String
    let value: Float
    let color: Color
}
