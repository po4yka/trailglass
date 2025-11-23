# Paging 3 Integration for Timeline and Trips Screens

This document describes the Paging 3 integration implementation for the Timeline and Trips screens in the Trailglass Android app.

## Overview

Two new screens have been created with Paging 3 integration:

1. `EnhancedTimelineScreenPaging.kt` - Paginated timeline screen for place visits
2. `TripsScreenPaging.kt` - Paginated trips screen

These screens provide better performance for users with large location histories by loading data incrementally as the user scrolls.

## Implementation Details

### Dependencies

The following dependency was added to `composeApp/build.gradle.kts`:

```kotlin
implementation(libs.androidx.paging.compose)
```

### Architecture

The paging implementation follows this pattern:

1. **PagingSource** (already implemented in `shared/src/androidMain/kotlin/com/po4yka/trailglass/data/paging/`):
    - `PlaceVisitPagingSource` - Loads place visits from the database
    - `TripPagingSource` - Loads trips from the database

2. **Pager Configuration**:
    - Page size: 20 items
    - Prefetch distance: 5 items
    - Placeholders: Disabled for better UX

3. **Screen Integration**:
    - Screens receive `Database` and `userId` as parameters
    - Create a `Pager` with the appropriate `PagingSource`
    - Use `collectAsLazyPagingItems()` to collect paging data
    - Display items in a `LazyColumn` with proper state handling

### Features

Both paging screens include:

1. **Pull-to-Refresh**: Using Material's `ExperimentalMaterialApi.pullRefresh`
2. **Loading States**:
    - Initial loading with centered progress indicator
    - Append loading with smaller progress indicator at bottom
    - Refresh loading with pull-to-refresh indicator
3. **Error Handling**:
    - Error view for initial load failures
    - Retry button for append errors
    - Uses existing `ErrorView` component
4. **Empty State**: Custom empty state views when no data exists
5. **Animations**: All animations use `MotionConfig.standardSpring()` for consistency with Material 3 Expressive design
6. **Item Animations**: Fade in/out animations when items appear/disappear

### Motion Configuration

All animations use the app's `MotionConfig` object for consistency:

```kotlin
AnimatedVisibility(
    visible = true,
    enter = fadeIn(animationSpec = MotionConfig.standardSpring()),
    exit = fadeOut(animationSpec = MotionConfig.standardSpring())
)
```

Shape morphing for trip cards also uses Material 3 Expressive springs:

```kotlin
spring(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessMedium
)
```

## Usage

### EnhancedTimelineScreenPaging

```kotlin
EnhancedTimelineScreenPaging(
    database = appComponent.database,
    userId = appComponent.userId,
    trackingController = appComponent.locationTrackingController,
    onAddPhoto = { /* ... */ },
    onAddNote = { /* ... */ },
    onCheckIn = { /* ... */ }
)
```

### TripsScreenPaging

```kotlin
TripsScreenPaging(
    database = appComponent.database,
    userId = appComponent.userId,
    onTripClick = { trip -> /* navigate to trip detail */ },
    onCreateTrip = { /* show create trip dialog */ }
)
```

## Integration with Existing Code

To use these paging screens in your navigation:

1. Access the `Database` instance from `AppComponent`:
   ```kotlin
   val database = appComponent.database
   ```

2. Pass it to the paging screen along with other required parameters

3. The screens will handle all paging logic internally

## Differences from Original Screens

### EnhancedTimelineScreen vs EnhancedTimelineScreenPaging

- **Original**: Loads all timeline items at once using `GetTimelineUseCase`
- **Paging**: Loads place visits incrementally using `PlaceVisitPagingSource`
- **Removed Features** (simplified for paging implementation):
    - Zoom levels (Day/Week/Month/Year)
    - Date navigation
    - Filtering by category/transport
    - Search functionality
    - Route segments and summary cards

### TripsScreen vs TripsScreenPaging

- **Original**: Accepts a `List<Trip>` parameter
- **Paging**: Directly queries database using `TripPagingSource`
- **Preserved Features**:
    - Ongoing/completed trip grouping
    - Shape morphing animations
    - Auto-detection badges
    - Tags display

## Performance Benefits

1. **Reduced Memory Usage**: Only loaded items are kept in memory
2. **Faster Initial Load**: First page loads quickly
3. **Smooth Scrolling**: Items load ahead of user scroll position
4. **Better Large Dataset Handling**: Scales well with thousands of items

## Future Enhancements

Potential improvements for the paging screens:

1. **Add Filtering**: Implement filters using `PagingSource.invalidate()`
2. **Add Search**: Create a new `PagingSource` that applies search query
3. **Add Zoom Levels**: Create different `PagingSource` implementations for each zoom level
4. **Add Sorting**: Pass sort parameter to `PagingSource`
5. **Header/Footer Support**: Add sticky headers for date grouping
6. **Placeholder Support**: Enable placeholders for smoother scroll experience

## Testing

To test the paging implementation:

1. **Build the app**: `./gradlew :composeApp:assembleDebug`
2. **Test with small dataset**: Verify empty state and single page
3. **Test with large dataset**: Import test data and verify:
    - Initial page loads correctly
    - Scrolling loads more items
    - Pull-to-refresh works
    - Error states display correctly
4. **Test animations**: Verify all transitions use MotionConfig springs

## Notes

- The original `EnhancedTimelineScreen.kt` and `TripsScreen.kt` are preserved for backward compatibility
- These paging screens are alternative implementations that can be used when performance is a concern
- Choose the appropriate screen based on your use case:
    - Use original screens for feature-rich experience with filters/search
    - Use paging screens for better performance with large datasets
