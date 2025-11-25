import SwiftUI
import Shared

/**
 * Zoom level selector.
 */
struct ZoomLevelSelector: View {
    let currentZoom: TimelineZoomLevel
    let onZoomChanged: (TimelineZoomLevel) -> Void

    var body: some View {
        HStack(spacing: 8) {
            ForEach([TimelineZoomLevel.day, .week, .month, .year], id: \.self) { zoom in
                GlassButton(
                    title: zoom.displayName,
                    icon: currentZoom == zoom ? "checkmark" : nil,
                    variant: .filled,
                    isSelected: currentZoom == zoom,
                    tint: .coastalPath
                ) {
                    onZoomChanged(zoom)
                }
            }
        }
        .padding(8)
        .glassEffectTinted(.coastalPath, opacity: 0.6)
        .cornerRadius(8)
    }
}

/**
 * Date navigation bar.
 */
struct DateNavigationBar: View {
    let selectedDate: LocalDate
    let zoomLevel: TimelineZoomLevel
    let onPrevious: () -> Void
    let onNext: () -> Void
    let onToday: () -> Void

    var body: some View {
        HStack {
            Button(action: onPrevious) {
                Image(systemName: "chevron.left")
            }
            .padding(.leading, 8)

            Spacer()

            VStack(spacing: 4) {
                Text(formatDateForZoom(selectedDate, zoomLevel))
                    .font(.headline)

                Button(action: onToday) {
                    HStack(spacing: 4) {
                        Image(systemName: "calendar.badge.clock")
                            .font(.caption)
                        Text("Today")
                            .font(.caption)
                    }
                }
            }

            Spacer()

            Button(action: onNext) {
                Image(systemName: "chevron.right")
            }
            .padding(.trailing, 8)
        }
        .padding(.vertical, 8)
        .background(Color(.systemGray6))
    }

    private func formatDateForZoom(_ date: LocalDate, _ zoom: TimelineZoomLevel) -> String {
        switch zoom {
        case .day:
            return "\(date.year)-\(String(format: "%02d", date.monthNumber))-\(String(format: "%02d", date.dayOfMonth))"
        case .week:
            return "Week of \(date.year)-\(String(format: "%02d", date.monthNumber))-\(String(format: "%02d", date.dayOfMonth))"
        case .month:
            let monthNames = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"]
            return "\(monthNames[Int(date.monthNumber) - 1]) \(date.year)"
        case .year:
            return "\(date.year)"
        default:
            return "\(date.year)"
        }
    }
}

/**
 * Active filters chips.
 */
struct ActiveFiltersChips: View {
    let filter: TimelineFilter
    let onClearAll: () -> Void

    var body: some View {
        HStack {
            Text("Filters:")
                .font(.caption)
                .foregroundColor(.secondary)

            Text("\(filter.activeFilterCount) active")
                .font(.caption)
                .fontWeight(.bold)

            Spacer()

            Button("Clear All", action: onClearAll)
                .font(.caption)
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 8)
        .background(Color(.systemGray5))
    }
}

/**
 * Empty timeline view.
 */
struct EmptyTimelineView: View {
    var body: some View {
        VStack(spacing: 16) {
            Image(systemName: "note.text")
                .font(.system(size: 64))
                .foregroundColor(.secondary)

            Text("No timeline data")
                .font(.title2)
                .foregroundColor(.secondary)

            Text("Enable location tracking to see your timeline")
                .font(.body)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)
        }
        .padding()
    }
}

/**
 * Timeline filter sheet.
 */
struct TimelineFilterSheet: View {
    @State private var localFilter: TimelineFilter
    @State private var selectedCategories: Set<PlaceCategory>
    @State private var showOnlyFavorites: Bool
    let onFilterChanged: (TimelineFilter) -> Void
    let onDismiss: () -> Void

    init(currentFilter: TimelineFilter, onFilterChanged: @escaping (TimelineFilter) -> Void, onDismiss: @escaping () -> Void) {
        _localFilter = State(initialValue: currentFilter)
        // Initialize selected categories from the current filter
        // For now, start with empty set - we'll implement proper conversion later
        _selectedCategories = State(initialValue: [])
        _showOnlyFavorites = State(initialValue: currentFilter.showOnlyFavorites)
        self.onFilterChanged = onFilterChanged
        self.onDismiss = onDismiss
    }

    var body: some View {
        NavigationView {
            Form {
                Section("Transport Types") {
                    ForEach([TransportType.walk, .bike, .car, .train, .plane, .boat], id: \.name) { type in
                        Button(action: {
                            toggleTransportType(type)
                        }) {
                            HStack {
                                Image(systemName: transportIcon(type))
                                Text(transportName(type))
                                Spacer()
                                if localFilter.transportTypes.contains(where: { $0.name == type.name }) {
                                    Image(systemName: "checkmark")
                                        .foregroundColor(.blue)
                                }
                            }
                        }
                        .foregroundColor(.primary)
                    }
                }

                Section("Place Categories") {
                    ForEach(placeCategoryList(), id: \.rawValue) { category in
                        Toggle(category.displayName, isOn: Binding(
                            get: { selectedCategories.contains(category) },
                            set: { _ in toggleCategory(category) }
                        ))
                    }
                }

                Section("Options") {
                    Toggle("Show only favorites", isOn: $showOnlyFavorites)
                }

                Section {
                    Button("Reset", action: {
                        localFilter = TimelineFilter(transportTypes: [], placeCategories: [], showOnlyFavorites: false)
                        selectedCategories = []
                        showOnlyFavorites = false
                    })

                    Button("Apply") {
                        let updatedFilter = TimelineFilter(
                            transportTypes: Array(localFilter.transportTypes),
                            placeCategories: Array(selectedCategories),
                            showOnlyFavorites: showOnlyFavorites
                        )
                        onFilterChanged(updatedFilter)
                        onDismiss()
                    }
                    .fontWeight(.bold)
                }
            }
            .navigationTitle("Filter Timeline")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Done", action: onDismiss)
                }
            }
        }
    }

    private func toggleTransportType(_ type: TransportType) {
        var types = Array(localFilter.transportTypes)
        if types.contains(where: { $0.name == type.name }) {
            types = types.filter { $0.name != type.name }
        } else {
            types.append(type)
        }
        localFilter = TimelineFilter(
            transportTypes: types,
            placeCategories: [], // Categories managed separately via selectedCategories
            showOnlyFavorites: localFilter.showOnlyFavorites
        )
    }

    private func toggleCategory(_ category: PlaceCategory) {
        if selectedCategories.contains(category) {
            selectedCategories.remove(category)
        } else {
            selectedCategories.insert(category)
        }
    }

    private func placeCategoryList() -> [PlaceCategory] {
        // Return all categories except OTHER and UNKNOWN
        [.home, .work, .restaurant, .shopping, .entertainment, .travel]
    }
}

// MARK: - ViewModel

/**
 * ViewModel bridging SwiftUI to Kotlin controller.
 */
class EnhancedTimelineViewModel: ObservableObject {
    private let controller: EnhancedTimelineController
    private let locationTrackingController: LocationTrackingController
    private var stateObserver: KotlinJob?
    private var trackingObserver: KotlinJob?

    @Published var items: [GetTimelineUseCaseTimelineItemUI] = []
    @Published var zoomLevel: TimelineZoomLevel = .day
    @Published var selectedDate: LocalDate = LocalDate(year: 2025, monthNumber: 1, dayOfMonth: 1)
    @Published var filter: TimelineFilter = TimelineFilter(transportTypes: [], placeCategories: [], showOnlyFavorites: false)
    @Published var searchQuery: String = ""
    @Published var isLoading: Bool = false
    @Published var error: String?
    @Published var isTracking: Bool = false
    @Published var showPhotoPicker = false
    @Published var showNoteEditor = false
    @Published var showCheckIn = false

    init(controller: EnhancedTimelineController, locationTrackingController: LocationTrackingController) {
        self.controller = controller
        self.locationTrackingController = locationTrackingController
        observeState()
        observeTrackingState()
    }

    deinit {
        stateObserver?.cancel(cause: nil)
        trackingObserver?.cancel(cause: nil)
        controller.cleanup()
    }

    // FAB actions
    func toggleTracking() {
        if isTracking {
            locationTrackingController.stopTracking()
        } else {
            // Start tracking with passive mode (balanced battery usage)
            locationTrackingController.startTracking(mode: TrackingMode.passive)
        }
    }

    func addPhoto() {
        showPhotoPicker = true
    }

    func addNote() {
        showNoteEditor = true
    }

    func checkIn() {
        showCheckIn = true
    }

    func loadTimeline() {
        controller.jumpToToday()
    }

    func setZoomLevel(_ zoom: TimelineZoomLevel) {
        controller.setZoomLevel(zoomLevel: zoom)
    }

    func navigatePrevious() {
        controller.navigatePrevious()
    }

    func navigateNext() {
        controller.navigateNext()
    }

    func jumpToToday() {
        controller.jumpToToday()
    }

    func updateFilter(_ filter: TimelineFilter) {
        controller.updateFilter(filter: filter)
    }

    func clearFilters() {
        controller.clearFilters()
    }

    func search(_ query: String?) {
        controller.search(query: query)
    }

    func clearSearch() {
        searchQuery = ""
        controller.clearSearch()
    }

    func refresh() {
        controller.refresh()
    }

    private func observeState() {
        stateObserver = controller.state.subscribe { [weak self] (state: EnhancedTimelineState?) in
            guard let self = self, let state = state else { return }

            DispatchQueue.main.async {
                self.items = state.items as? [GetTimelineUseCaseTimelineItemUI] ?? []
                self.zoomLevel = state.zoomLevel
                self.selectedDate = state.selectedDate
                self.filter = state.filter
                self.searchQuery = state.searchQuery ?? ""
                self.isLoading = state.isLoading
                self.error = state.error
                // Note: isTracking is managed separately via observeTrackingState()
            }
        }
    }

    private func observeTrackingState() {
        // Observe tracking state from LocationTrackingController
        trackingObserver = locationTrackingController.uiState.subscribe { [weak self] (state: LocationTrackingUIState?) in
            guard let self = self, let state = state else { return }

            DispatchQueue.main.async {
                self.isTracking = state.trackingState.isTracking
            }
        }
    }
}

// MARK: - Helper Functions

// Helpers moved to SharedUIHelpers.swift

