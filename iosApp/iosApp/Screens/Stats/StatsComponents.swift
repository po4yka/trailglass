import SwiftUI
import Shared

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
        .glassEffectTinted(.coastalPath, opacity: 0.6)
        .cornerRadius(8)
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

// ErrorView and InfoRow moved to SharedComponents.swift


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

    // func loadCurrentYear() {
    //     // TODO: Fix Kotlin period types
    // }

    func loadPeriod(_ period: StatsPeriod) {
        selectedPeriod = period
        let calendar = Calendar.current
        let year = calendar.component(.year, from: Date())

        // TODO: Fix Kotlin period loading
        // switch period {
        // case .year:
        //     controller.loadPeriod(period: GetStatsUseCasePeriodYear(year: Int32(year)))
        // case .month:
        //     let month = calendar.component(.month, from: Date())
        //     controller.loadPeriod(period: GetStatsUseCasePeriodMonth(year: Int32(year), month: Int32(month)))
        // }
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

// Helpers moved to SharedUIHelpers.swift

