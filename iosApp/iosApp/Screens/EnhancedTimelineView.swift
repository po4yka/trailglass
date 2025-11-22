import SwiftUI

/**
 * SwiftUI timeline screen matching Android EnhancedTimelineScreen.
 * Shows timeline with zoom levels, filtering, and search.
 */
struct EnhancedTimelineView: View {
    @StateObject private var viewModel: EnhancedTimelineViewModel
    @State private var showFilterSheet = false
    @State private var showSearchBar = false

    init(controller: EnhancedTimelineController) {
        _viewModel = StateObject(wrappedValue: EnhancedTimelineViewModel(controller: controller))
    }

    var body: some View {
        NavigationView {
            ZStack {
                VStack(spacing: 0) {
                    // Zoom level selector
                    ZoomLevelSelector(
                        currentZoom: viewModel.zoomLevel,
                        onZoomChanged: { viewModel.setZoomLevel($0) }
                    )

                    // Date navigation
                    DateNavigationBar(
                        selectedDate: viewModel.selectedDate,
                        zoomLevel: viewModel.zoomLevel,
                        onPrevious: { viewModel.navigatePrevious() },
                        onNext: { viewModel.navigateNext() },
                        onToday: { viewModel.jumpToToday() }
                    )

                    // Active filters indicator
                    if viewModel.filter.isActive {
                        ActiveFiltersChips(
                            filter: viewModel.filter,
                            onClearAll: { viewModel.clearFilters() }
                        )
                    }

                    // Content
                    if viewModel.isLoading {
                        Spacer()
                        ProgressView()
                        Spacer()
                    } else if let error = viewModel.error {
                        Spacer()
                        ErrorView(error: error) {
                            viewModel.refresh()
                        }
                        Spacer()
                    } else if !viewModel.items.isEmpty {
                        TimelineContent(
                            items: viewModel.items,
                            zoomLevel: viewModel.zoomLevel
                        )
                    } else {
                        Spacer()
                        EmptyTimelineView()
                        Spacer()
                    }
                }
            }
            .navigationTitle(showSearchBar ? "" : "Timeline")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    if showSearchBar {
                        Button("Done") {
                            showSearchBar = false
                            viewModel.clearSearch()
                        }
                    } else {
                        HStack(spacing: 4) {
                            Button(action: { showSearchBar = true }) {
                                Image(systemName: "magnifyingglass")
                            }

                            Button(action: { showFilterSheet = true }) {
                                ZStack(alignment: .topTrailing) {
                                    Image(systemName: "line.3.horizontal.decrease.circle")
                                    if viewModel.filter.activeFilterCount > 0 {
                                        Circle()
                                            .fill(Color.adaptivePrimary)
                                            .frame(width: 16, height: 16)
                                            .overlay(
                                                Text("\(viewModel.filter.activeFilterCount)")
                                                    .font(.caption2)
                                                    .foregroundColor(.white)
                                            )
                                            .offset(x: 8, y: -8)
                                    }
                                }
                            }

                            Button(action: { viewModel.refresh() }) {
                                Image(systemName: "arrow.clockwise")
                            }
                        }
                    }
                }
            }
            .searchable(text: $viewModel.searchQuery, isPresented: $showSearchBar, prompt: "Search timeline...")
            .sheet(isPresented: $showFilterSheet) {
                TimelineFilterSheet(
                    currentFilter: viewModel.filter,
                    onFilterChanged: { viewModel.updateFilter($0) },
                    onDismiss: { showFilterSheet = false }
                )
            }
        }
        .onAppear {
            viewModel.loadTimeline()
        }
    }
}

/**
 * Zoom level selector.
 */
private struct ZoomLevelSelector: View {
    let currentZoom: TimelineZoomLevel
    let onZoomChanged: (TimelineZoomLevel) -> Void

    var body: some View {
        HStack(spacing: 8) {
            ForEach([TimelineZoomLevel.day, .week, .month, .year], id: \.self) { zoom in
                Button(action: { onZoomChanged(zoom) }) {
                    HStack {
                        if currentZoom == zoom {
                            Image(systemName: "checkmark")
                                .font(.caption)
                        }
                        Text(zoom.displayName)
                    }
                    .padding(.horizontal, 12)
                    .padding(.vertical, 6)
                    .background(currentZoom == zoom ? Color.adaptivePrimary : Color(.systemGray5))
                    .foregroundColor(currentZoom == zoom ? .white : .primary)
                    .cornerRadius(8)
                }
            }
        }
        .padding(8)
        .frame(maxWidth: .infinity)
        .background(Color(.systemGray6))
    }
}

/**
 * Date navigation bar.
 */
private struct DateNavigationBar: View {
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
        @unknown default:
            return "\(date.year)"
        }
    }
}

/**
 * Active filters chips.
 */
private struct ActiveFiltersChips: View {
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
 * Timeline content with items.
 */
private struct TimelineContent: View {
    let items: [GetTimelineUseCaseTimelineItemUI]
    let zoomLevel: TimelineZoomLevel

    var body: some View {
        ScrollView {
            LazyVStack(spacing: 12) {
                ForEach(Array(items.enumerated()), id: \.offset) { _, item in
                    TimelineItemView(item: item)
                }
            }
            .padding(16)
        }
    }
}

/**
 * Individual timeline item view.
 */
private struct TimelineItemView: View {
    let item: GetTimelineUseCaseTimelineItemUI

    var body: some View {
        Group {
            if let dayStart = item as? GetTimelineUseCaseTimelineItemUIDayStartUI {
                DayMarkerCard(text: "Day Start", icon: "sun.max")
            } else if let dayEnd = item as? GetTimelineUseCaseTimelineItemUIDayEndUI {
                DayMarkerCard(text: "Day End", icon: "moon.stars")
            } else if let visit = item as? GetTimelineUseCaseTimelineItemUIVisitUI {
                EnhancedVisitCard(visit: visit.placeVisit)
            } else if let route = item as? GetTimelineUseCaseTimelineItemUIRouteUI {
                EnhancedRouteCard(route: route.routeSegment)
            } else if let daySummary = item as? GetTimelineUseCaseTimelineItemUIDaySummaryUI {
                DaySummaryCard(summary: daySummary)
            } else if let weekSummary = item as? GetTimelineUseCaseTimelineItemUIWeekSummaryUI {
                WeekSummaryCard(summary: weekSummary)
            } else if let monthSummary = item as? GetTimelineUseCaseTimelineItemUIMonthSummaryUI {
                MonthSummaryCard(summary: monthSummary)
            }
        }
    }
}

/**
 * Day marker card.
 */
private struct DayMarkerCard: View {
    let text: String
    let icon: String

    var body: some View {
        HStack(spacing: 8) {
            Image(systemName: icon)
                .foregroundColor(.adaptivePrimary)
            Text(text)
                .font(.body)
                .fontWeight(.medium)
        }
        .padding(12)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color(.systemGray6))
        .cornerRadius(8)
    }
}

/**
 * Enhanced visit card.
 */
private struct EnhancedVisitCard: View {
    let visit: PlaceVisit

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            // Header
            HStack(alignment: .top, spacing: 12) {
                Image(systemName: categoryIcon(visit.category))
                    .font(.title2)
                    .foregroundColor(.blue)

                VStack(alignment: .leading, spacing: 4) {
                    HStack(spacing: 8) {
                        Text(visit.displayName)
                            .font(.headline)

                        if visit.isFavorite {
                            Image(systemName: "star.fill")
                                .foregroundColor(.yellow)
                                .font(.caption)
                        }
                    }

                    if let city = visit.city, let country = visit.country {
                        Text("\(city), \(country)")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                }

                Spacer()
            }

            // Address (if no POI name)
            if let address = visit.approximateAddress, visit.userLabel == nil, visit.poiName == nil {
                Text(address)
                    .font(.caption)
                    .foregroundColor(.secondary)
            }

            // User notes
            if let notes = visit.userNotes {
                HStack(spacing: 8) {
                    Image(systemName: "note.text")
                        .font(.caption)
                    Text(notes)
                        .font(.caption)
                }
                .padding(8)
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(Color(.systemGray6))
                .cornerRadius(4)
            }

            // Metadata chips
            HStack(spacing: 8) {
                HStack(spacing: 4) {
                    Image(systemName: "clock")
                        .font(.caption)
                    Text(formatDuration(visit.duration))
                        .font(.caption)
                }
                .padding(.horizontal, 8)
                .padding(.vertical, 4)
                .background(Color(.systemGray5))
                .cornerRadius(4)

                if visit.category.name != "OTHER" {
                    Text(categoryName(visit.category))
                        .font(.caption)
                        .padding(.horizontal, 8)
                        .padding(.vertical, 4)
                        .background(Color(.systemGray5))
                        .cornerRadius(4)
                }
            }
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color.blue.opacity(0.1))
        .cornerRadius(12)
    }
}

/**
 * Enhanced route card.
 */
private struct EnhancedRouteCard: View {
    let route: RouteSegment

    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: transportIcon(route.transportType))
                .font(.title2)
                .foregroundColor(.green)

            VStack(alignment: .leading, spacing: 4) {
                Text(transportName(route.transportType))
                    .font(.headline)

                HStack(spacing: 8) {
                    Text("\(Int(route.distanceMeters / 1000)) km")
                        .font(.caption)

                    let duration = route.endTime.timeIntervalSince1970 - route.startTime.timeIntervalSince1970
                    if duration > 0 {
                        Text("• \(Int(duration / 60)) min")
                            .font(.caption)
                    }
                }
                .foregroundColor(.secondary)
            }

            Spacer()

            // Confidence indicator
            if route.confidence < 0.7 {
                Image(systemName: "questionmark.circle")
                    .foregroundColor(.secondary)
            }
        }
        .padding(16)
        .background(Color.green.opacity(0.1))
        .cornerRadius(12)
    }
}

/**
 * Day summary card.
 */
private struct DaySummaryCard: View {
    let summary: GetTimelineUseCaseTimelineItemUIDaySummaryUI

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack(spacing: 8) {
                Image(systemName: "calendar")
                    .foregroundColor(.purple)
                Text(formatDate(summary.date))
                    .font(.headline)
            }

            HStack(spacing: 16) {
                SummaryStatItem(
                    icon: "mappin.and.ellipse",
                    label: "Places",
                    value: "\(summary.totalVisits)"
                )
                SummaryStatItem(
                    icon: "ruler",
                    label: "Distance",
                    value: "\(Int(summary.totalDistanceMeters / 1000)) km"
                )
                SummaryStatItem(
                    icon: "figure.walk",
                    label: "Routes",
                    value: "\(summary.totalRoutes)"
                )
            }
            .frame(maxWidth: .infinity)
        }
        .padding(16)
        .background(Color.purple.opacity(0.1))
        .cornerRadius(12)
    }
}

/**
 * Week summary card.
 */
private struct WeekSummaryCard: View {
    let summary: GetTimelineUseCaseTimelineItemUIWeekSummaryUI

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            VStack(alignment: .leading, spacing: 4) {
                HStack(spacing: 8) {
                    Image(systemName: "calendar.badge.clock")
                        .foregroundColor(.purple)
                    Text("Week Summary")
                        .font(.headline)
                }
                Text("\(formatDate(summary.weekStart)) - \(formatDate(summary.weekEnd))")
                    .font(.caption)
                    .foregroundColor(.secondary)
            }

            HStack(spacing: 16) {
                SummaryStatItem(
                    icon: "mappin.and.ellipse",
                    label: "Places",
                    value: "\(summary.totalVisits)"
                )
                SummaryStatItem(
                    icon: "ruler",
                    label: "Distance",
                    value: "\(Int(summary.totalDistanceMeters / 1000)) km"
                )
                SummaryStatItem(
                    icon: "calendar",
                    label: "Days Active",
                    value: "\(summary.activeDays)"
                )
            }
            .frame(maxWidth: .infinity)
        }
        .padding(16)
        .background(Color.purple.opacity(0.1))
        .cornerRadius(12)
    }
}

/**
 * Month summary card.
 */
private struct MonthSummaryCard: View {
    let summary: GetTimelineUseCaseTimelineItemUIMonthSummaryUI

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack(spacing: 8) {
                Image(systemName: "calendar.circle")
                    .foregroundColor(.purple)
                Text("\(summary.month.name) \(summary.year)")
                    .font(.headline)
            }

            HStack(spacing: 16) {
                SummaryStatItem(
                    icon: "mappin.and.ellipse",
                    label: "Places",
                    value: "\(summary.totalVisits)"
                )
                SummaryStatItem(
                    icon: "ruler",
                    label: "Distance",
                    value: "\(Int(summary.totalDistanceMeters / 1000)) km"
                )
                SummaryStatItem(
                    icon: "calendar.badge.clock",
                    label: "Weeks",
                    value: "\(summary.activeWeeks)"
                )
            }
            .frame(maxWidth: .infinity)

            if !summary.topCategories.isEmpty {
                Divider()

                Text("Top Categories")
                    .font(.caption)
                    .foregroundColor(.secondary)

                HStack(spacing: 8) {
                    ForEach(Array(summary.topCategories.prefix(3)), id: \.name) { category in
                        HStack(spacing: 4) {
                            Image(systemName: categoryIcon(category))
                                .font(.caption2)
                            Text(categoryName(category))
                                .font(.caption2)
                        }
                        .padding(.horizontal, 8)
                        .padding(.vertical, 4)
                        .background(Color(.systemGray5))
                        .cornerRadius(4)
                    }
                }
            }
        }
        .padding(16)
        .background(Color.purple.opacity(0.1))
        .cornerRadius(12)
    }
}

/**
 * Summary stat item.
 */
private struct SummaryStatItem: View {
    let icon: String
    let label: String
    let value: String

    var body: some View {
        VStack(spacing: 4) {
            Image(systemName: icon)
                .font(.title3)
                .foregroundColor(.blue)
            Text(value)
                .font(.headline)
            Text(label)
                .font(.caption)
                .foregroundColor(.secondary)
        }
    }
}

/**
 * Empty timeline view.
 */
private struct EmptyTimelineView: View {
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
private struct TimelineFilterSheet: View {
    @State private var localFilter: TimelineFilter
    let onFilterChanged: (TimelineFilter) -> Void
    let onDismiss: () -> Void

    init(currentFilter: TimelineFilter, onFilterChanged: @escaping (TimelineFilter) -> Void, onDismiss: @escaping () -> Void) {
        _localFilter = State(initialValue: currentFilter)
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
                    ForEach(placeCategoryList(), id: \.name) { category in
                        Button(action: {
                            toggleCategory(category)
                        }) {
                            HStack {
                                Image(systemName: categoryIcon(category))
                                Text(categoryName(category))
                                Spacer()
                                if localFilter.placeCategories.contains(where: { $0.name == category.name }) {
                                    Image(systemName: "checkmark")
                                        .foregroundColor(.blue)
                                }
                            }
                        }
                        .foregroundColor(.primary)
                    }
                }

                Section("Options") {
                    Toggle("Show only favorites", isOn: $localFilter.showOnlyFavorites)
                }

                Section {
                    Button("Reset", action: {
                        localFilter = TimelineFilter(
                            transportTypes: [],
                            placeCategories: [],
                            showOnlyFavorites: false
                        )
                    })

                    Button("Apply") {
                        onFilterChanged(localFilter)
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
        var types = localFilter.transportTypes
        if types.contains(where: { $0.name == type.name }) {
            types = types.filter { $0.name != type.name }
        } else {
            types.append(type)
        }
        localFilter = TimelineFilter(
            transportTypes: types,
            placeCategories: localFilter.placeCategories,
            showOnlyFavorites: localFilter.showOnlyFavorites
        )
    }

    private func toggleCategory(_ category: PlaceCategory) {
        var categories = localFilter.placeCategories
        if categories.contains(where: { $0.name == category.name }) {
            categories = categories.filter { $0.name != category.name }
        } else {
            categories.append(category)
        }
        localFilter = TimelineFilter(
            transportTypes: localFilter.transportTypes,
            placeCategories: categories,
            showOnlyFavorites: localFilter.showOnlyFavorites
        )
    }

    private func placeCategoryList() -> [PlaceCategory] {
        // Return all categories except OTHER
        [.home, .work, .food, .shopping, .fitness, .entertainment, .travel, .healthcare, .education, .religious, .social, .outdoor, .service]
    }
}

// MARK: - ViewModel

/**
 * ViewModel bridging SwiftUI to Kotlin controller.
 */
class EnhancedTimelineViewModel: ObservableObject {
    private let controller: EnhancedTimelineController

    @Published var items: [GetTimelineUseCaseTimelineItemUI] = []
    @Published var zoomLevel: TimelineZoomLevel = .day
    @Published var selectedDate: LocalDate = LocalDate(year: 2025, monthNumber: 1, dayOfMonth: 1)
    @Published var filter: TimelineFilter = TimelineFilter(transportTypes: [], placeCategories: [], showOnlyFavorites: false)
    @Published var searchQuery: String = ""
    @Published var isLoading: Bool = false
    @Published var error: String?

    init(controller: EnhancedTimelineController) {
        self.controller = controller
        observeState()
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
        // TODO: Implement StateFlow observation bridge
        // For now, manually trigger updates
    }
}

// MARK: - Helper Functions

private func categoryIcon(_ category: PlaceCategory) -> String {
    switch category.name {
    case "HOME": return "house"
    case "WORK": return "briefcase"
    case "FOOD": return "fork.knife"
    case "SHOPPING": return "cart"
    case "FITNESS": return "figure.run"
    case "ENTERTAINMENT": return "film"
    case "TRAVEL": return "airplane"
    case "HEALTHCARE": return "cross.case"
    case "EDUCATION": return "book"
    case "RELIGIOUS": return "building.columns"
    case "SOCIAL": return "person.2"
    case "OUTDOOR": return "tree"
    case "SERVICE": return "hammer"
    default: return "mappin"
    }
}

private func categoryName(_ category: PlaceCategory) -> String {
    category.name.lowercased().capitalized
}

private func transportIcon(_ type: TransportType) -> String {
    switch type.name {
    case "WALK": return "figure.walk"
    case "BIKE": return "bicycle"
    case "CAR": return "car"
    case "TRAIN": return "tram"
    case "PLANE": return "airplane"
    case "BOAT": return "ferry"
    default: return "questionmark.circle"
    }
}

private func transportName(_ type: TransportType) -> String {
    type.name.lowercased().capitalized
}

private func formatDuration(_ duration: KotlinDuration) -> String {
    let hours = duration.inWholeHours
    let minutes = duration.inWholeMinutes % 60

    if hours > 0 && minutes > 0 {
        return "\(hours)h \(minutes)m"
    } else if hours > 0 {
        return "\(hours)h"
    } else {
        return "\(minutes)m"
    }
}

private func formatDate(_ date: LocalDate) -> String {
    "\(date.year)-\(String(format: "%02d", date.monthNumber))-\(String(format: "%02d", date.dayOfMonth))"
}
