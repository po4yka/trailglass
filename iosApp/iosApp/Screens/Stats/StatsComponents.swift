import SwiftUI

/**
 * Utility components and helpers for the statistics screen.
 */

/**
 * Period selector for Year/Month.
 */
struct PeriodSelector: View {
    let selectedPeriod: StatsPeriod
    let onPeriodChange: (StatsPeriod) -> Void

    var body: some View {
        HStack(spacing: 8) {
            GlassButton(
                title: "Year",
                icon: selectedPeriod == .year ? "checkmark" : nil,
                variant: .filled,
                isSelected: selectedPeriod == .year,
                tint: .coolSteel
            ) {
                onPeriodChange(.year)
            }

            GlassButton(
                title: "Month",
                icon: selectedPeriod == .month ? "checkmark" : nil,
                variant: .filled,
                isSelected: selectedPeriod == .month,
                tint: .coolSteel
            ) {
                onPeriodChange(.month)
            }
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .glassBackground(material: .ultraThin, tint: .lightCyan, cornerRadius: 8)
    }
}

/**
 * Section header.
 */
struct SectionHeader: View {
    let text: String

    var body: some View {
        Text(text)
            .font(.title2)
            .fontWeight(.bold)
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.top, 8)
    }
}

/**
 * Info row for key-value pairs.
 */
struct InfoRow: View {
    let label: String
    let value: String

    var body: some View {
        HStack {
            Text(label)
                .foregroundColor(.secondary)

            Spacer()

            Text(value)
                .fontWeight(.semibold)
        }
    }
}

/**
 * Empty stats view.
 */
struct EmptyStatsView: View {
    var body: some View {
        VStack(spacing: 16) {
            Image(systemName: "chart.bar")
                .font(.system(size: 64))
                .foregroundColor(.secondary)

            Text("No statistics available")
                .font(.title2)
                .foregroundColor(.secondary)

            Text("Start tracking to see your travel statistics")
                .font(.body)
                .foregroundColor(.secondary)
        }
    }
}

/**
 * Error view with retry.
 */
struct ErrorView: View {
    let error: String
    let onRetry: () -> Void

    var body: some View {
        VStack(spacing: 16) {
            Image(systemName: "exclamationmark.triangle")
                .font(.system(size: 64))
                .foregroundColor(.adaptiveWarning)

            Text(error)
                .font(.body)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)

            Button("Retry", action: onRetry)
                .buttonStyle(.borderedProminent)
        }
        .padding()
    }
}

// MARK: - ViewModel

/**
 * ViewModel bridging SwiftUI to Kotlin controller.
 */
class EnhancedStatsViewModel: ObservableObject {
    private let controller: EnhancedStatsController

    @Published var stats: ComprehensiveStatistics?
    @Published var isLoading: Bool = false
    @Published var error: String?
    @Published var selectedPeriod: StatsPeriod = .year

    init(controller: EnhancedStatsController) {
        self.controller = controller
        observeState()
    }

    func loadCurrentYear() {
        let calendar = Calendar.current
        let year = calendar.component(.year, from: Date())
        controller.loadPeriod(period: GetStatsUseCasePeriodYear(year: Int32(year)))
    }

    func loadPeriod(_ period: StatsPeriod) {
        selectedPeriod = period
        let calendar = Calendar.current
        let year = calendar.component(.year, from: Date())

        switch period {
        case .year:
            controller.loadPeriod(period: GetStatsUseCasePeriodYear(year: Int32(year)))
        case .month:
            let month = calendar.component(.month, from: Date())
            controller.loadPeriod(period: GetStatsUseCasePeriodMonth(year: Int32(year), month: Int32(month)))
        }
    }

    func refresh() {
        controller.refresh()
    }

    private func observeState() {
        // TODO: Implement StateFlow observation bridge
        // For now, manually trigger updates
    }
}

enum StatsPeriod {
    case year
    case month
}

// MARK: - Helper Functions

func transportTypeName(_ type: TransportType) -> String {
    type.name.lowercased().capitalized
}

func transportColor(_ type: TransportType) -> Color {
    switch type.name {
    case "WALK": return Color.adaptiveSuccess
    case "BIKE": return Color.adaptivePrimary
    case "CAR": return Color.adaptiveWarning
    case "TRAIN": return Color.eveningCategory
    case "PLANE": return Color.morningCategory
    case "BOAT": return Color.waterCategory
    default: return Color.neutralCategory
    }
}

func categoryName(_ category: PlaceCategory) -> String {
    category.name.lowercased().capitalized
}

func categoryColor(_ category: PlaceCategory) -> Color {
    switch category.name {
    case "HOME": return Color.adaptiveSuccess
    case "WORK": return Color.adaptivePrimary
    case "FOOD": return Color.morningCategory
    case "SHOPPING": return Color.adaptiveWarning
    case "FITNESS": return Color.eveningCategory
    case "ENTERTAINMENT": return Color.morningCategory
    case "TRAVEL": return Color.waterCategory
    case "HEALTHCARE": return Color.adaptiveWarning
    case "EDUCATION": return Color.adaptivePrimary
    case "RELIGIOUS": return Color.neutralCategory
    case "SOCIAL": return Color.morningCategory
    case "OUTDOOR": return Color.adaptiveSuccess
    case "SERVICE": return Color.neutralCategory
    default: return Color.neutralCategory
    }
}

func dayOfWeekName(_ day: DayOfWeek) -> String {
    switch day.name {
    case "MONDAY": return "Mon"
    case "TUESDAY": return "Tue"
    case "WEDNESDAY": return "Wed"
    case "THURSDAY": return "Thu"
    case "FRIDAY": return "Fri"
    case "SATURDAY": return "Sat"
    case "SUNDAY": return "Sun"
    default: return day.name
    }
}

func timeRangeForHour(_ hour: Int32) -> String {
    switch hour {
    case 0..<6: return "Night (12AM-6AM)"
    case 6..<12: return "Morning (6AM-12PM)"
    case 12..<18: return "Afternoon (12PM-6PM)"
    default: return "Evening (6PM-12AM)"
    }
}

// MARK: - Duration Extension

extension KotlinDuration {
    var hours: Int {
        Int(inWholeHours)
    }

    var minutes: Int {
        Int(inWholeMinutes)
    }
}
