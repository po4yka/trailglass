import SwiftUI

/**
 * SwiftUI bar chart component matching Android BarChart.
 */
struct BarChartView: View {
    let data: [BarData]
    let showValues: Bool
    let maxBarHeight: CGFloat

    init(data: [BarData], showValues: Bool = true, maxBarHeight: CGFloat = 200) {
        self.data = data
        self.showValues = showValues
        self.maxBarHeight = maxBarHeight
    }

    private var maxValue: Float {
        data.map { $0.value }.max() ?? 1
    }

    var body: some View {
        VStack(spacing: 16) {
            // Bars
            HStack(alignment: .bottom, spacing: 8) {
                ForEach(data.indices, id: \.self) { index in
                    BarItem(
                        barData: data[index],
                        maxValue: maxValue,
                        maxHeight: maxBarHeight,
                        showValue: showValues
                    )
                }
            }
        }
    }
}

/**
 * Individual bar item.
 */
private struct BarItem: View {
    let barData: BarData
    let maxValue: Float
    let maxHeight: CGFloat
    let showValue: Bool

    private var barHeight: CGFloat {
        CGFloat(barData.value / maxValue) * maxHeight
    }

    var body: some View {
        VStack(spacing: 4) {
            // Value text
            if showValue {
                Text(barData.formattedValue ?? "\(Int(barData.value))")
                    .font(.caption2)
                    .fontWeight(.bold)
            }

            // Bar
            Rectangle()
                .fill(barData.color ?? Color.adaptivePrimary)
                .frame(width: 32, height: barHeight)
                .cornerRadius(4)

            // Label
            Text(barData.label)
                .font(.caption2)
                .lineLimit(2)
                .multilineTextAlignment(.center)
                .frame(width: 40)
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
