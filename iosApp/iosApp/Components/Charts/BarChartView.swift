import SwiftUI

/**
 * SwiftUI bar chart component with Liquid Glass aesthetic.
 * Glass bars with transport/category colors and interactive effects.
 */
struct BarChartView: View {
    let data: [BarData]
    let showValues: Bool
    let maxBarHeight: CGFloat
    @State private var animatedHeights: [CGFloat]
    @State private var hoveredIndex: Int? = nil

    init(data: [BarData], showValues: Bool = true, maxBarHeight: CGFloat = 200) {
        self.data = data
        self.showValues = showValues
        self.maxBarHeight = maxBarHeight
        _animatedHeights = State(initialValue: Array(repeating: 0, count: data.count))
    }

    private var maxValue: Float {
        data.map { $0.value }.max() ?? 1
    }

    var body: some View {
        VStack(spacing: 16) {
            // Glass grid lines
            ZStack(alignment: .bottom) {
                // Grid lines background
                VStack(spacing: 0) {
                    ForEach(0..<5) { index in
                        HStack {
                            Spacer()
                        }
                        .frame(height: 1)
                        .background(Color.white.opacity(0.1))
                        if index < 4 {
                            Spacer()
                        }
                    }
                }
                .frame(height: maxBarHeight)

                // Bars
                HStack(alignment: .bottom, spacing: 12) {
                    ForEach(data.indices, id: \.self) { index in
                        GlassBarItem(
                            barData: data[index],
                            maxValue: maxValue,
                            maxHeight: maxBarHeight,
                            animatedHeight: animatedHeights[index],
                            showValue: showValues,
                            isHovered: hoveredIndex == index,
                            onHover: {
                                withAnimation(MotionConfig.quickSpring) {
                                    hoveredIndex = index
                                }
                            },
                            onHoverEnd: {
                                withAnimation(MotionConfig.quickSpring) {
                                    hoveredIndex = nil
                                }
                            }
                        )
                    }
                }
            }
        }
        .onAppear {
            animateBars()
        }
        .onChange(of: data.map { $0.value }) { _ in
            animateBars()
        }
    }

    private func animateBars() {
        for (index, barData) in data.enumerated() {
            withAnimation(MotionConfig.staggeredAppear(index: index, baseDelay: 0.08)) {
                animatedHeights[index] = CGFloat(barData.value / maxValue) * maxBarHeight
            }
        }
    }
}

/**
 * Glass bar item with interactive effects.
 */
private struct GlassBarItem: View {
    let barData: BarData
    let maxValue: Float
    let maxHeight: CGFloat
    let animatedHeight: CGFloat
    let showValue: Bool
    let isHovered: Bool
    let onHover: () -> Void
    let onHoverEnd: () -> Void
    @Environment(\.colorScheme) var colorScheme

    private var barColor: Color {
        barData.color ?? .coolSteel
    }

    var body: some View {
        VStack(spacing: 6) {
            // Value label with glass background
            if showValue {
                Text(barData.formattedValue ?? "\(Int(barData.value))")
                    .font(.caption2)
                    .fontWeight(.bold)
                    .foregroundColor(isHovered ? barColor : .primary)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 4)
                    .background(
                        RoundedRectangle(cornerRadius: 6)
                            .fill(.ultraThinMaterial)
                            .overlay(
                                RoundedRectangle(cornerRadius: 6)
                                    .fill(barColor.opacity(isHovered ? 0.2 : 0.1))
                            )
                    )
                    .opacity(animatedHeight > 0 ? 1 : 0)
                    .scaleEffect(isHovered ? 1.05 : 1.0)
            }

            // Glass bar
            RoundedRectangle(cornerRadius: 8)
                .fill(
                    LinearGradient(
                        colors: [
                            barColor.opacity(0.6),
                            barColor.opacity(0.4)
                        ],
                        startPoint: .top,
                        endPoint: .bottom
                    )
                )
                .overlay(
                    RoundedRectangle(cornerRadius: 8)
                        .fill(.ultraThinMaterial)
                        .opacity(0.3)
                )
                .overlay(
                    RoundedRectangle(cornerRadius: 8)
                        .strokeBorder(
                            LinearGradient(
                                colors: [
                                    Color.white.opacity(colorScheme == .dark ? 0.4 : 0.6),
                                    Color.white.opacity(colorScheme == .dark ? 0.1 : 0.2)
                                ],
                                startPoint: .top,
                                endPoint: .bottom
                            ),
                            lineWidth: 1.5
                        )
                )
                .frame(width: 40, height: max(animatedHeight, 4))
                .shadow(
                    color: isHovered ? barColor.opacity(0.4) : Color.black.opacity(0.1),
                    radius: isHovered ? 8 : 4,
                    y: 2
                )
                .scaleEffect(x: isHovered ? 1.08 : 1.0, y: 1.0)
                .onTapGesture {
                    onHover()
                    DispatchQueue.main.asyncAfter(deadline: .now() + 0.3) {
                        onHoverEnd()
                    }
                }

            // Label
            Text(barData.label)
                .font(.caption2)
                .foregroundColor(isHovered ? barColor : .secondary)
                .lineLimit(2)
                .multilineTextAlignment(.center)
                .frame(width: 48)
                .fontWeight(isHovered ? .semibold : .regular)
        }
    }
}

/**
 * Bar data model.
 */
struct BarData {
    let label: String
    let value: Float
    let formattedValue: String?
    let color: Color?

    init(label: String, value: Float, formattedValue: String? = nil, color: Color? = nil) {
        self.label = label
        self.value = value
        self.formattedValue = formattedValue
        self.color = color
    }
}
