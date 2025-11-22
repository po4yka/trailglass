import SwiftUI
import Combine

/// Base protocol for paginated view models
protocol PaginatedViewModel: ObservableObject {
    associatedtype Item: Identifiable

    var items: [Item] { get set }
    var isLoading: Bool { get set }
    var isLoadingMore: Bool { get set }
    var hasMorePages: Bool { get set }
    var error: String? { get set }
    var currentPage: Int { get set }

    func loadInitialData()
    func loadMoreIfNeeded(currentItem: Item?)
    func refresh()
}

extension PaginatedViewModel {
    /// Check if we should load more data when scrolling
    func shouldLoadMore(currentItem: Item?) -> Bool {
        guard let currentItem = currentItem else { return false }
        guard hasMorePages && !isLoadingMore else { return false }

        // Load more when we're 5 items from the end
        guard let index = items.firstIndex(where: { $0.id == currentItem.id }) else {
            return false
        }

        return index >= items.count - 5
    }
}

/// Pagination configuration
struct PaginationConfig {
    let pageSize: Int
    let prefetchThreshold: Int

    static let standard = PaginationConfig(
        pageSize: 20,
        prefetchThreshold: 5
    )

    static let large = PaginationConfig(
        pageSize: 50,
        prefetchThreshold: 10
    )
}

/// Pagination state
enum PaginationState {
    case idle
    case loading
    case loadingMore
    case refreshing
    case error(String)
}
