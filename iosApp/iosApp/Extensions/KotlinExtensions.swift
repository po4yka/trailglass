import Foundation
import Shared

// MARK: - Type-safe Notification Names

extension NSNotification.Name {
    static let showRegionOnMap = NSNotification.Name("ShowRegionOnMap")
    static let navigateToMap = NSNotification.Name("NavigateToMap")
    static let regionEntered = NSNotification.Name("RegionEntered")
    static let regionExited = NSNotification.Name("RegionExited")
    static let syncCompleted = NSNotification.Name("SyncCompleted")
    static let authStateChanged = NSNotification.Name("AuthStateChanged")
}

// MARK: - Date to Kotlin Instant Conversion

extension Date {
    /// Converts Swift Date to Kotlin Instant
    var kotlinInstant: Kotlinx_datetimeInstant {
        Kotlinx_datetimeInstant.companion.fromEpochMilliseconds(
            epochMilliseconds: Int64(timeIntervalSince1970 * 1000)
        )
    }
}

extension Kotlinx_datetimeInstant {
    /// Converts Kotlin Instant to Swift Date
    var swiftDate: Date {
        Date(timeIntervalSince1970: TimeInterval(epochSeconds))
    }
}

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
// SettingsState is a top-level type in Shared module, no alias needed
public typealias StatsState = StatsController.StatsState
public typealias EnhancedStatsState = EnhancedStatsController.EnhancedStatsState
public typealias KotlinStatsPeriod = GetStatsUseCase.Period
public typealias KotlinStatsPeriodYear = GetStatsUseCase.PeriodYear
public typealias KotlinStatsPeriodMonth = GetStatsUseCase.PeriodMonth
public typealias KotlinStatsPeriodCustom = GetStatsUseCase.PeriodCustom

// Kotlin Duration is not exported; represent as seconds.
public typealias KotlinDuration = Int64

// Simple cancellable wrapper for Flow subscriptions
public class KotlinJob {
    private var task: Task<Void, Never>?

    init(task: Task<Void, Never>?) {
        self.task = task
    }

    public func cancel(cause: Error? = nil) {
        task?.cancel()
        task = nil
    }

    deinit {
        task?.cancel()
    }
}

// MARK: - Kotlinx_coroutines_coreStateFlow Extensions

extension Kotlinx_coroutines_coreStateFlow {
    /// Subscribe to StateFlow updates with automatic main thread dispatch
    /// - Parameter onValue: Closure called on main thread with each new value
    /// - Returns: KotlinJob that can be cancelled to stop observation
    func subscribe<T>(onValue: @escaping (T?) -> Void) -> KotlinJob {
        let task = Task {
            do {
                try await self.collect(
                    collector: FlowCollector<T> { value in
                        onValue(value as? T)
                    }
                )
            } catch {
                // Task was cancelled or collection failed - this is expected on cleanup
            }
        }
        return KotlinJob(task: task)
    }

    /// Subscribe to StateFlow with MainActor-isolated callback for UI updates
    /// - Parameter onValue: Closure called on MainActor with each new value
    /// - Returns: KotlinJob that can be cancelled to stop observation
    @MainActor
    func subscribeOnMain<T>(onValue: @escaping @MainActor (T?) -> Void) -> KotlinJob {
        let task = Task { @MainActor in
            do {
                try await self.collect(
                    collector: MainActorFlowCollector<T> { value in
                        onValue(value as? T)
                    }
                )
            } catch {
                // Task was cancelled or collection failed - this is expected on cleanup
            }
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

/// Flow collector helper for background collection
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

/// Flow collector that dispatches to MainActor for UI updates
@MainActor
class MainActorFlowCollector<T>: Kotlinx_coroutines_coreFlowCollector {
    private let callback: @MainActor (Any?) -> Void

    init(_ callback: @escaping @MainActor (Any?) -> Void) {
        self.callback = callback
    }

    nonisolated func emit(value: Any?, completionHandler: @escaping (Error?) -> Void) {
        Task { @MainActor in
            self.callback(value)
            completionHandler(nil)
        }
    }
}

// MARK: - TimelineFilter Extensions

extension TimelineFilter {
    convenience init(
        transportTypes: [TransportType] = [],
        placeCategories: [PlaceCategory] = [],
        showOnlyFavorites: Bool = false
    ) {
        // Safe conversion using Set initializers
        let transportSet = Set(transportTypes)
        let categorySet = Set(placeCategories)
        let emptyStringSet = Set<String>()

        self.init(
            transportTypes: transportSet,
            placeCategories: categorySet,
            countries: emptyStringSet,
            cities: emptyStringSet,
            searchQuery: nil,
            dateRange: nil,
            minDuration: nil,
            maxDuration: nil,
            showOnlyFavorites: showOnlyFavorites
        )
    }
}

// MARK: - Data to Kotlin ByteArray Conversion

extension Data {
    /// Converts Swift Data to Kotlin ByteArray
    func toKotlinByteArray() -> KotlinByteArray {
        let kotlinData = KotlinByteArray(size: Int32(self.count))
        self.withUnsafeBytes { bytes in
            for (index, byte) in bytes.enumerated() {
                kotlinData.set(index: Int32(index), value: Int8(bitPattern: byte))
            }
        }
        return kotlinData
    }
}

// MARK: - FrequentPlace Identifiable Conformance

extension FrequentPlace: Identifiable {}

