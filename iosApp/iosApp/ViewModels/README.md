# iOS Pagination Implementation

This directory contains ViewModels that implement manual pagination for iOS SwiftUI views, providing platform parity with Android's Paging 3 library.

## Architecture

### Why Manual Pagination?

Android uses Paging 3, which is an Android-specific library. iOS requires a custom implementation using SwiftUI best practices:

1. **Pull-to-refresh** - Using `.refreshable` modifier
2. **Load more on scroll** - Detecting when user approaches end of list
3. **Loading states** - Showing indicators during data fetching
4. **Error handling** - Graceful error recovery with retry

### Key Components

#### 1. PaginatedViewModel.swift

Base protocol and utilities for pagination:

- `PaginatedViewModel` protocol defines common pagination interface
- `PaginationConfig` configures page size and prefetch threshold
- `PaginationState` enum tracks loading states

#### 2. PaginatedPlaceVisitsViewModel.swift

ViewModel for paginated place visits:

- Uses `PlaceVisitRepository.getVisitsByUser(userId, limit, offset)`
- Implements offset-based pagination
- Prefetches next page when user is 5 items from end
- Supports pull-to-refresh

#### 3. PaginatedTripsViewModel.swift

ViewModel for trips:

- Uses `TripRepository.getTripsForUser(userId)`
- Currently loads all trips at once (repository doesn't support pagination yet)
- Groups trips into ongoing and completed
- Supports pull-to-refresh

## Usage

### Timeline View

```swift
import SwiftUI
import shared

struct MyTimelineView: View {
    let repository: PlaceVisitRepository
    let userId: String

    var body: some View {
        PaginatedTimelineView(
            repository: repository,
            userId: userId
        )
    }
}
```

### Trips View

```swift
import SwiftUI
import shared

struct MyTripsView: View {
    let repository: TripRepository
    let userId: String

    var body: some View {
        PaginatedTripsView(
            repository: repository,
            userId: userId
        )
    }
}
```

## How Pagination Works

### Place Visits (Offset-based)

1. **Initial Load**: Loads first page (20 items by default)
2. **Scroll Detection**: As user scrolls, `onAppear` on list items triggers
3. **Prefetch**: When user is 5 items from end, loads next page
4. **Append Data**: New items are appended to existing list
5. **End Detection**: When page returns fewer items than page size, stops loading

```
Page 0: offset=0,  limit=20 → 20 items
Page 1: offset=20, limit=20 → 20 items
Page 2: offset=40, limit=20 → 15 items (last page)
```

### Trips (All-at-once)

Currently loads all trips in a single call because `TripRepository` doesn't have pagination methods yet. When pagination is added to the shared repository, the ViewModel can be updated to use offset-based pagination.

## State Management

### Loading States

- `isLoading` - Initial load in progress
- `isLoadingMore` - Next page load in progress
- `hasMorePages` - More data available
- `error` - Error message if load failed

### Pull-to-Refresh

Uses SwiftUI's `.refreshable` modifier:

```swift
.refreshable {
    viewModel.refresh()
}
```

This resets pagination state and loads from page 0.

## Platform Parity

### Android (Paging 3)
- Automatic pagination with `PagingSource`
- RecyclerView integration
- Built-in loading states

### iOS (Manual Implementation)
- Manual pagination with ViewModels
- LazyVStack integration
- Custom loading state management

### Shared Features
- Same data from shared repositories
- Same page size (20 items)
- Same prefetch threshold (5 items from end)
- Pull-to-refresh on both platforms
- Loading indicators
- Error handling

## Configuration

Default pagination config:

```swift
PaginationConfig(
    pageSize: 20,        // Items per page
    prefetchThreshold: 5  // Items from end to trigger load
)
```

Large pagination config:

```swift
PaginationConfig(
    pageSize: 50,
    prefetchThreshold: 10
)
```

## Future Improvements

1. **Add pagination to TripRepository** - Update shared Kotlin code to support offset/limit
2. **Caching** - Cache loaded pages to avoid re-fetching
3. **Bidirectional pagination** - Support loading previous pages
4. **Search integration** - Pagination with search/filter
5. **Combine with Controllers** - Integrate with existing controller-based architecture

## Example: Adding Pagination to New Data Type

```swift
class PaginatedItemsViewModel: ObservableObject {
    private let repository: MyRepository
    private let config: PaginationConfig

    @Published var items: [MyItem] = []
    @Published var isLoading = false
    @Published var isLoadingMore = false
    @Published var hasMorePages = true
    @Published var error: String?
    @Published private(set) var currentPage = 0

    init(repository: MyRepository, config: PaginationConfig = .standard) {
        self.repository = repository
        self.config = config
    }

    func loadInitialData() {
        // Load page 0
    }

    func loadMoreIfNeeded(currentItem: MyItem?) {
        // Check if near end and load next page
    }

    func refresh() {
        // Reset and reload
    }
}
```

## Testing

To test pagination:

1. Run app with limited data (< 20 items) - should show all items
2. Run app with many items (> 50 items) - should paginate
3. Scroll to end - should load more automatically
4. Pull down - should refresh and reset to page 0
5. Test offline - should show error and allow retry

## Troubleshooting

### Items not loading
- Check that `onAppear` is called on list items
- Verify `hasMorePages` is true
- Check repository is returning correct count

### Duplicate items
- Ensure page offset calculation is correct
- Check that items aren't being added twice

### Performance issues
- Reduce page size if loading is slow
- Increase prefetch threshold if scrolling stutters
- Use `LazyVStack` instead of regular `VStack`
