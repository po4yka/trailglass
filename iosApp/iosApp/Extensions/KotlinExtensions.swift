import Shared

// MARK: - Shared typealiases for Swift ergonomics

public typealias ConflictUiModel = SyncUiState.ConflictUiModel
public typealias ConflictResolutionChoice = SyncUiState.ConflictResolutionChoice
public typealias SyncStatusUiModel = SyncUiState.SyncStatusUiModel
public typealias EnhancedMapState = EnhancedMapController.EnhancedMapState
public typealias DayOfWeek = Kotlinx_datetimeDayOfWeek
public typealias LocalDate = Kotlinx_datetimeLocalDate
public typealias Instant = Kotlinx_datetimeInstant
public typealias KotlinInstant = Kotlinx_datetimeInstant
// TimelineZoomLevel is already correctly named in Shared module, no alias needed
public typealias GetTimelineUseCaseTimelineItemUI = GetTimelineForDayUseCase.TimelineItemUI
public typealias GetTimelineUseCaseTimelineItemUIDaySummaryUI = GetTimelineUseCase.TimelineItemUIDaySummaryUI
public typealias GetTimelineUseCaseTimelineItemUIWeekSummaryUI = GetTimelineUseCase.TimelineItemUIWeekSummaryUI
public typealias GetTimelineUseCaseTimelineItemUIMonthSummaryUI = GetTimelineUseCase.TimelineItemUIMonthSummaryUI
public typealias TripsControllerSortOption = TripsController.SortOption
public typealias TripStatistics = TripStatisticsCalculator.TripStatistics
public typealias RouteData = TripRoute
public typealias KotlinResult<T: AnyObject> = Result<T>
public typealias AuthState = AuthController.AuthState
public typealias RegionsState = RegionsController.RegionsState
public typealias RouteViewState = RouteViewController.RouteViewState
public typealias MapState = EnhancedMapController.EnhancedMapState
public typealias TripsState = TripsController.TripsState
public typealias ConflictResolutionState = ConflictResolutionController.ConflictResolutionState
public typealias DeviceManagementState = DeviceManagementController.DeviceManagementState
public typealias PlacesState = PlacesController.PlacesState
public typealias PhotoGalleryState = PhotoGalleryController.GalleryState
public typealias TrackingUIState = LocationTrackingController.LocationTrackingUIState
public typealias TripStatsState = TripStatisticsController.StatisticsState
public typealias RouteReplayState = RouteReplayController.ReplayState
public typealias EnhancedTimelineState = EnhancedTimelineController.EnhancedTimelineState

// Kotlin Duration is not exported; represent as seconds.
public typealias KotlinDuration = Int64

// Simple cancellable wrapper for Flow subscriptions
public class KotlinJob {
    private var task: Task<Void, Error>?

    init(task: Task<Void, Error>?) {
        self.task = task
    }

    public func cancel(cause: Error? = nil) {
        task?.cancel()
        task = nil
    }
}

// MARK: - Kotlinx_coroutines_coreStateFlow Extensions

extension Kotlinx_coroutines_coreStateFlow {
    func subscribe<T>(onValue: @escaping (T?) -> Void) -> KotlinJob {
        let task = Task {
            try await self.collect(
                collector: FlowCollector<T> { value in
                    onValue(value as? T)
                }
            )
        }
        return KotlinJob(task: task)
    }
}

// MARK: - KotlinDuration helpers (represented as seconds)

public extension KotlinDuration {
    var inWholeSeconds: Int64 { self }
    var inWholeMinutes: Int64 { self / 60 }
    var inWholeHours: Int64 { self / 3600 }
}

/// Flow collector helper
class FlowCollector<T>: Kotlinx_coroutines_coreFlowCollector {
    private let callback: (Any?) -> Void

    init(_ callback: @escaping (Any?) -> Void) {
        self.callback = callback
    }

    func emit(value: Any?, completionHandler: @escaping (Error?) -> Void) {
        callback(value)
        completionHandler(nil)
    }
}

// MARK: - TimelineFilter Extensions

extension TimelineFilter {
    convenience init(
        transportTypes: [TransportType] = [],
        placeCategories: [PlaceCategory] = [],
        showOnlyFavorites: Bool = false
    ) {
        self.init(
            transportTypes: Set(transportTypes),
            placeCategories: Set(placeCategories) as! Set<Shared.PlaceCategory>,
            countries: [],
            cities: [],
            searchQuery: nil,
            dateRange: nil,
            minDuration: nil,
            maxDuration: nil,
            showOnlyFavorites: showOnlyFavorites
        )
    }
}
