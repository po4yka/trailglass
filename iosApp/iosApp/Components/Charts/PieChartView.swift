import SwiftUI

/**
 * SwiftUI pie chart component with Liquid Glass aesthetic.
 * Glass segments with category colors and interactive selection.
 */
struct PieChartView: View {
    let data: [PieData]
    let showLegend: Bool
    @State private var selectedIndex: Int?
    @State private var animatedValues: [Float]

    init(data: [PieData], showLegend: Bool = true) {
        self.data = data
        self.showLegend = showLegend
        _animatedValues = State(initialValue: Array(repeating: 0, count: data.count))
    }

    private var total: Float {
        animatedValues.reduce(0, +)
    }

    var body: some View {
        VStack(spacing: 16) {
            // Pie chart
            if total > 0 {
                ZStack {
                    // Glass background
                    Circle()
                        .fill(.ultraThinMaterial)
                        .overlay(
                            Circle()
                                .fill(Color.lightCyan.opacity(0.15))
                        )
                        .overlay(
                            Circle()
                                .strokeBorder(
                                    LinearGradient(
                                        colors: [
                                            Color.white.opacity(0.3),
                                            Color.white.opacity(0.1)
                                        ],
                                        startPoint: .topLeading,
                                        endPoint: .bottomTrailing
                                    ),
                                    lineWidth: 2
                                )
                        )
                        .shadow(color: Color.black.opacity(0.1), radius: 8, y: 4)

                    // Glass segments
                    ForEach(0..<data.count, id: \.self) { index in
                        GlassPieSlice(
                            startAngle: startAngle(for: index),
                            endAngle: endAngle(for: index),
                            color: data[index].color,
                            isSelected: selectedIndex == index
                        )
                        .onTapGesture {
                            withAnimation(MotionConfig.expressiveSpring) {
                                selectedIndex = selectedIndex == index ? nil : index
                            }
                        }
                    }
                }
                .frame(width: 200, height: 200)
            }

            // Legend with glass chips
            if showLegend {
                VStack(spacing: 8) {
                    ForEach(data.indices, id: \.self) { index in
                        GlassLegendItem(
                            color: data[index].color,
                            label: data[index].label,
                            value: percentage(for: index),
                            isSelected: selectedIndex == index,
                            onTap: {
                                withAnimation(MotionConfig.expressiveSpring) {
                                    selectedIndex = selectedIndex == index ? nil : index
                                }
                            }
                        )
                    }
                }
            }
        }
        .onAppear {
            animateValues()
        }
        .onChange(of: data.map { $0.value }) { _ in
            animateValues()
        }
    }

    private func animateValues() {
        for (index, pieData) in data.enumerated() {
            withAnimation(MotionConfig.staggeredAppear(index: index)) {
                animatedValues[index] = pieData.value
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
 * Glass pie slice with tinted glass effect.
 */
private struct GlassPieSlice: View {
    let startAngle: Angle
    let endAngle: Angle
    let color: Color
    let isSelected: Bool
    @Environment(\.colorScheme) var colorScheme

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

                let endX = center.x + innerRadius * cos(CGFloat(endAngle.radians))
                let endY = center.y + innerRadius * sin(CGFloat(endAngle.radians))
                path.addLine(to: CGPoint(x: endX, y: endY))

                path.addArc(
                    center: center,
                    radius: innerRadius,
                    startAngle: endAngle,
                    endAngle: startAngle,
                    clockwise: true
                )

                path.closeSubpath()
            }
            .fill(
                LinearGradient(
                    colors: [
                        color.opacity(0.6),
                        color.opacity(0.4)
                    ],
                    startPoint: .topLeading,
                    endPoint: .bottomTrailing
                )
            )
            .overlay(
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
                .stroke(
                    Color.white.opacity(colorScheme == .dark ? 0.3 : 0.5),
                    lineWidth: 1
                )
            )
            .scaleEffect(isSelected ? 1.05 : 1.0)
            .shadow(
                color: isSelected ? color.opacity(0.5) : Color.clear,
                radius: isSelected ? 12 : 0
            )
        }
    }
}

/**
 * Glass legend item with interactive selection.
 */
private struct GlassLegendItem: View {
    let color: Color
    let label: String
    let value: String
    let isSelected: Bool
    let onTap: () -> Void
    @Environment(\.colorScheme) var colorScheme

    var body: some View {
        Button(action: onTap) {
            HStack(spacing: 12) {
                // Glass color chip
                Circle()
                    .fill(
                        LinearGradient(
                            colors: [
                                color.opacity(0.6),
                                color.opacity(0.4)
                            ],
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        )
                    )
                    .overlay(
                        Circle()
                            .strokeBorder(
                                Color.white.opacity(colorScheme == .dark ? 0.3 : 0.5),
                                lineWidth: 1
                            )
                    )
                    .frame(width: 16, height: 16)
                    .shadow(color: color.opacity(0.3), radius: 4)

                Text(label)
                    .font(.caption)
                    .foregroundColor(.primary)

                Spacer()

                Text(value)
                    .font(.caption)
                    .fontWeight(.bold)
                    .foregroundColor(isSelected ? color : .primary)
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 8)
            .background(
                RoundedRectangle(cornerRadius: 8)
                    .fill(isSelected ? Color.blue.opacity(0.1) : Color.clear)
            )
            .overlay(
                RoundedRectangle(cornerRadius: 8)
                    .strokeBorder(
                        isSelected ? color.opacity(0.5) : Color.clear,
                        lineWidth: 1
                    )
            )
            .scaleEffect(isSelected ? 1.02 : 1.0)
        }
        .buttonStyle(.plain)
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
