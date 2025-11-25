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
    private var stateObserver: KotlinJob?

    @Published var stats: ComprehensiveStatistics?
    @Published var isLoading: Bool = false
    @Published var error: String?
    @Published var selectedPeriod: StatsPeriod = .year

    init(controller: EnhancedStatsController) {
        self.controller = controller
        observeState()
    }

    deinit {
        stateObserver?.cancel(cause: nil)
        controller.cleanup()
    }

    func loadCurrentYear() {
        let currentYear = Calendar.current.component(.year, from: Date())
        let kotlinPeriod = KotlinStatsPeriodYear(year: Int32(currentYear))
        controller.loadPeriod(period: kotlinPeriod)
        selectedPeriod = .year
    }

    func loadPeriod(_ period: StatsPeriod) {
        selectedPeriod = period

        let kotlinPeriod: KotlinStatsPeriod
        switch period {
        case .year:
            let currentYear = Calendar.current.component(.year, from: Date())
            kotlinPeriod = KotlinStatsPeriodYear(year: Int32(currentYear))
        case .month:
            let components = Calendar.current.dateComponents([.year, .month], from: Date())
            let year = Int32(components.year ?? Calendar.current.component(.year, from: Date()))
            let month = Int32(components.month ?? Calendar.current.component(.month, from: Date()))
            kotlinPeriod = KotlinStatsPeriodMonth(year: year, month: month)
        }

        controller.loadPeriod(period: kotlinPeriod)
    }

    func refresh() {
        controller.refresh()
    }

    private func observeState() {
        stateObserver = controller.state.subscribe { [weak self] (state: EnhancedStatsState?) in
            guard let self = self, let state = state else { return }

            Task { @MainActor in
                self.isLoading = state.isLoading
                self.error = state.error
                self.stats = state.stats
            }
        }
    }
}

enum StatsPeriod {
    case year
    case month
}

// MARK: - Helper Functions

// Helpers moved to SharedUIHelpers.swift

