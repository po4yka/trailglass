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
        let kotlinPeriod = GetStatsUseCasePeriodYear(year: Int32(currentYear))
        controller.loadPeriod(period: kotlinPeriod)
        selectedPeriod = .year
    }

    func loadPeriod(_ period: StatsPeriod) {
        selectedPeriod = period

        let kotlinPeriod: GetStatsUseCasePeriod
        switch period {
        case .year:
            let currentYear = Calendar.current.component(.year, from: Date())
            kotlinPeriod = GetStatsUseCasePeriodYear(year: Int32(currentYear))
        case .month:
            let components = Calendar.current.dateComponents([.year, .month], from: Date())
            let year = Int32(components.year ?? Calendar.current.component(.year, from: Date()))
            let month = Int32(components.month ?? Calendar.current.component(.month, from: Date()))
            kotlinPeriod = GetStatsUseCasePeriodMonth(year: Int32(year), month: Int32(month))
        }

        controller.loadPeriod(period: kotlinPeriod)
    }

    func refresh() {
        controller.refresh()
    }

    private func observeState() {
        // Assuming the controller has a state with isLoading, error, and stats properties
        // This may need adjustment based on the actual Kotlin controller interface
        stateObserver = controller.state.subscribe { [weak self] (state: Any?) in
            guard let self = self, let state = state else { return }

            DispatchQueue.main.async {
                // Extract properties from the state object
                // This implementation assumes the state has these properties
                if let loading = (state as? NSObject)?.value(forKey: "isLoading") as? Bool {
                    self.isLoading = loading
                }
                if let error = (state as? NSObject)?.value(forKey: "error") as? String {
                    self.error = error
                }
                if let stats = (state as? NSObject)?.value(forKey: "stats") as? ComprehensiveStatistics {
                    self.stats = stats
                }
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

