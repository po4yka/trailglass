# Map Visualization

TrailGlass map visualization displays your travel history with place visit markers and route polylines on interactive maps.

## Overview

```
Location Data (Visits + Routes)
      ↓
  GetMapDataUseCase
      ↓
MapDisplayData (Markers + Routes + Region)
      ↓
MapController (StateFlow)
      ↓
Platform Map Component
      ├── Android: Google Maps Compose
      └── iOS: MapKit (MKMapView)
```

## Architecture

**Shared Layer** (commonMain):
- Domain models (Coordinate, MapRegion, MapMarker, MapRoute)
- GetMapDataUseCase (converts visits/routes to map data)
- MapController (state management)
- Color scheme for transport types

**Android Layer**:
- Google Maps Compose integration
- MapView composable
- MapScreen with marker info cards
- Camera position management

**iOS Layer**:
- MapKit (MKMapView) wrapper
- SwiftUI TrailGlassMapView
- Annotation and overlay rendering
- Coordinator pattern for delegation

## Domain Models

### Coordinate

```kotlin
data class Coordinate(
    val latitude: Double,   // -90 to 90
    val longitude: Double   // -180 to 180
)
```

### MapRegion

Defines a geographic bounding box:

```kotlin
data class MapRegion(
    val center: Coordinate,
    val latitudeDelta: Double,    // North-South span
    val longitudeDelta: Double    // East-West span
)

// Computed properties
val northEast: Coordinate
val southWest: Coordinate
```

### MapMarker

Represents a place visit on the map:

```kotlin
data class MapMarker(
    val id: String,
    val coordinate: Coordinate,
    val title: String?,          // e.g., "Paris"
    val snippet: String?,        // e.g., "Eiffel Tower, Avenue Anatole France"
    val placeVisitId: String
)
```

### MapRoute

Represents a route segment:

```kotlin
data class MapRoute(
    val id: String,
    val coordinates: List<Coordinate>,  // Simplified path
    val transportType: TransportType,
    val color: Int?,                     // ARGB color
    val routeSegmentId: String
)
```

### MapDisplayData

Complete data for map rendering:

```kotlin
data class MapDisplayData(
    val markers: List<MapMarker>,
    val routes: List<MapRoute>,
    val region: MapRegion?        // Bounding region
)
```

### CameraPosition

Camera position and zoom:

```kotlin
data class CameraPosition(
    val target: Coordinate,
    val zoom: Float = 15f,        // 1 (world) to 20 (building)
    val tilt: Float = 0f,         // Camera angle
    val bearing: Float = 0f       // Rotation
)
```

## Use Cases

### GetMapDataUseCase

Converts place visits and route segments into map display data.

**Features**:
- Loads visits and routes for a time range
- Converts to map markers and routes
- Calculates bounding region (with 20% padding)
- Assigns colors based on transport type

**Usage**:
```kotlin
val useCase = GetMapDataUseCase(
    placeVisitRepository,
    routeSegmentRepository
)

val mapData = useCase.execute(
    userId = "user1",
    startTime = thirtyDaysAgo,
    endTime = now
)

// mapData.markers: List of place visit markers
// mapData.routes: List of route polylines
// mapData.region: Bounding box to fit all data
```

**Transport Colors** (ARGB):
- WALK: Green (#4CAF50)
- BIKE: Blue (#2196F3)
- CAR: Red (#F44336)
- TRAIN: Purple (#9C27B0)
- PLANE: Orange (#FF9800)
- BOAT: Cyan (#00BCD4)
- UNKNOWN: Gray (#9E9E9E)

## Controller

### MapController

Manages map state with StateFlow:

```kotlin
data class MapState(
    val mapData: MapDisplayData,
    val cameraPosition: CameraPosition?,
    val selectedMarker: MapMarker?,
    val selectedRoute: MapRoute?,
    val isLoading: Boolean,
    val error: String?
)

// Methods
fun loadMapData(startTime: Instant, endTime: Instant)
fun selectMarker(marker: MapMarker)
fun deselectMarker()
fun updateCameraPosition(position: CameraPosition)
fun moveCameraTo(coordinate: Coordinate, zoom: Float = 15f)
fun fitToData()  // Fit map to show all markers/routes
```

**Zoom Calculation**:
```kotlin
delta > 10.0  -> zoom 5   // Country level
delta > 5.0   -> zoom 7   // Large region
delta > 1.0   -> zoom 9   // City level
delta > 0.5   -> zoom 11  // District
delta > 0.1   -> zoom 13  // Neighborhood
delta > 0.05  -> zoom 15  // Street level
else          -> zoom 17  // Building level
```

## Android Implementation

### Dependencies

Add to `build.gradle.kts`:

```kotlin
dependencies {
    // Google Maps Compose
    implementation("com.google.maps.android:maps-compose:4.3.0")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
}
```

### API Key Setup

**1. Get API Key**:
- Go to [Google Cloud Console](https://console.cloud.google.com/)
- Enable Maps SDK for Android
- Create API key

**2. Add to AndroidManifest.xml**:
```xml
<application>
    <meta-data
        android:name="com.google.android.geo.API_KEY"
        android:value="YOUR_API_KEY_HERE" />
</application>
```

### MapView Composable

```kotlin
@Composable
fun MapView(
    controller: MapController,
    onMarkerClick: (MapMarker) -> Unit = {},
    onRouteClick: (MapRoute) -> Unit = {}
) {
    val state by controller.state.collectAsState()

    Box {
        GoogleMap(
            cameraPositionState = cameraPositionState,
            onMapClick = { controller.deselectMarker() }
        ) {
            // Routes (polylines)
            state.mapData.routes.forEach { route ->
                Polyline(
                    points = route.coordinates.map { LatLng(it.latitude, it.longitude) },
                    color = Color(route.color ?: 0xFF2196F3),
                    width = getRouteWidth(route.transportType)
                )
            }

            // Markers
            state.mapData.markers.forEach { marker ->
                Marker(
                    state = MarkerState(
                        position = LatLng(marker.coordinate.latitude, marker.coordinate.longitude)
                    ),
                    title = marker.title,
                    snippet = marker.snippet,
                    onClick = {
                        controller.selectMarker(marker)
                        onMarkerClick(marker)
                        true
                    }
                )
            }
        }

        // Fit to data button
        FloatingActionButton(
            onClick = { controller.fitToData() }
        ) {
            Icon(Icons.Default.MyLocation, contentDescription = "Fit to data")
        }
    }
}
```

**Route Widths** (by transport type):
- WALK: 8dp
- BIKE: 10dp
- CAR: 12dp
- TRAIN: 14dp
- PLANE: 16dp

### MapScreen

```kotlin
@Composable
fun MapScreen(controller: MapController) {
    val state by controller.state.collectAsState()

    // Load last 30 days
    LaunchedEffect(Unit) {
        val now = Clock.System.now()
        val thirtyDaysAgo = now.minus(30.days)
        controller.loadMapData(thirtyDaysAgo, now)
    }

    Column {
        MapView(controller = controller, modifier = Modifier.weight(1f))

        // Selected marker info card
        state.selectedMarker?.let { marker ->
            MarkerInfoCard(
                marker = marker,
                onClose = { controller.deselectMarker() }
            )
        }
    }
}
```

**MarkerInfoCard Features**:
- Displays marker title and snippet
- "Details" button (navigate to visit)
- "Photos" button (add/view photos)
- Close button

### Camera Control

```kotlin
// Move to specific location
controller.moveCameraTo(
    coordinate = Coordinate(48.8584, 2.2945), // Eiffel Tower
    zoom = 15f
)

// Fit all data
controller.fitToData()

// Manual camera update
controller.updateCameraPosition(
    CameraPosition(
        target = coordinate,
        zoom = 12f,
        tilt = 45f,
        bearing = 90f
    )
)
```

## iOS Implementation

### MapKit Integration

**1. Add MapKit to Info.plist** (if needed for location):
```xml
<key>NSLocationWhenInUseUsageDescription</key>
<string>Show your location on the map</string>
```

**2. Create SwiftUI Map Wrapper**:

```swift
struct TrailGlassMapView: UIViewRepresentable {
    let markers: [MapMarker]
    let routes: [MapRoute]
    let region: MapRegion?
    let onMarkerTap: ((MapMarker) -> Void)?

    func makeUIView(context: Context) -> MKMapView {
        let mapView = MKMapView()
        mapView.delegate = context.coordinator
        mapView.showsUserLocation = true
        return mapView
    }

    func updateUIView(_ mapView: MKMapView, context: Context) {
        // Update region
        if let region = region {
            let mkRegion = MKCoordinateRegion(
                center: CLLocationCoordinate2D(
                    latitude: region.center.latitude,
                    longitude: region.center.longitude
                ),
                span: MKCoordinateSpan(
                    latitudeDelta: region.latitudeDelta,
                    longitudeDelta: region.longitudeDelta
                )
            )
            mapView.setRegion(mkRegion, animated: true)
        }

        // Update annotations
        updateAnnotations(mapView: mapView, markers: markers)

        // Update overlays
        updateOverlays(mapView: mapView, routes: routes)
    }

    func makeCoordinator() -> Coordinator {
        Coordinator(onMarkerTap: onMarkerTap)
    }
}
```

**3. Coordinator for Delegation**:

```swift
class Coordinator: NSObject, MKMapViewDelegate {
    let onMarkerTap: ((MapMarker) -> Void)?

    // Customize markers
    func mapView(_ mapView: MKMapView, viewFor annotation: MKAnnotation) -> MKAnnotationView? {
        guard !(annotation is MKUserLocation) else { return nil }

        let annotationView = MKMarkerAnnotationView(
            annotation: annotation,
            reuseIdentifier: "MarkerAnnotation"
        )
        annotationView.canShowCallout = true
        annotationView.markerTintColor = .systemTeal
        return annotationView
    }

    // Customize routes
    func mapView(_ mapView: MKMapView, rendererFor overlay: MKOverlay) -> MKOverlayRenderer {
        if let polyline = overlay as? MKPolyline {
            let renderer = MKPolylineRenderer(polyline: polyline)
            renderer.strokeColor = getColorForTransport(polyline.title ?? "UNKNOWN")
            renderer.lineWidth = getWidthForTransport(polyline.title ?? "UNKNOWN")
            return renderer
        }
        return MKOverlayRenderer(overlay: overlay)
    }
}
```

**4. SwiftUI Screen**:

```swift
struct MapScreenView: View {
    @StateObject private var viewModel = MapViewModel()

    var body: some View {
        ZStack {
            TrailGlassMapView(
                markers: viewModel.markers,
                routes: viewModel.routes,
                region: viewModel.region,
                onMarkerTap: { marker in
                    viewModel.selectMarker(marker)
                }
            )

            if let selectedMarker = viewModel.selectedMarker {
                VStack {
                    Spacer()
                    MarkerInfoCard(
                        marker: selectedMarker,
                        onClose: { viewModel.deselectMarker() }
                    )
                }
            }
        }
    }
}
```

## Features

### Markers

**Display**:
- Pin for each place visit
- Title: City or POI name
- Snippet: Address
- Tap to select and show info card

**Info Card**:
- Visit name and address
- "Details" button → Visit detail screen
- "Photos" button → Photo attachment
- Close button

### Routes

**Display**:
- Polyline between visits
- Color-coded by transport type
- Line width based on transport type
- Simplified path (Douglas-Peucker)

**Colors**:
- Green: Walking
- Blue: Cycling
- Red: Driving
- Purple: Train
- Orange: Flying
- Cyan: Boat

### Camera Controls

**Auto-fit**:
- Automatically calculates bounding region
- Adds 20% padding
- Minimum delta of 0.01 degrees

**Manual Control**:
- Zoom (1-20)
- Tilt (0-90 degrees)
- Bearing (0-360 degrees)
- Pan to coordinate

**Fit to Data Button**:
- FAB in bottom-right corner
- Adjusts camera to show all markers and routes
- Animated transition

## Usage Examples

### Load Map for Trip

```kotlin
val trip = /* Trip from database */

controller.loadMapData(
    startTime = trip.startTime,
    endTime = trip.endTime ?: Clock.System.now()
)
```

### Show Single Visit

```kotlin
val visit = /* PlaceVisit from database */

// Create single marker
val marker = MapMarker(
    id = "marker_${visit.id}",
    coordinate = Coordinate(visit.centerLatitude, visit.centerLongitude),
    title = visit.city,
    snippet = visit.approximateAddress,
    placeVisitId = visit.id
)

// Move camera to visit
controller.moveCameraTo(
    coordinate = marker.coordinate,
    zoom = 16f
)
```

### Show Route

```kotlin
val route = /* RouteSegment from database */

controller.moveCameraTo(
    coordinate = route.simplifiedPath.first(),
    zoom = 12f
)
```

## Performance

### Optimization Strategies

**1. Path Simplification**:
- Use Douglas-Peucker algorithm (already applied)
- Reduces polyline points by 60-80%
- Preserves route shape

**2. Marker Clustering** (future):
- Cluster nearby markers at low zoom levels
- Expand to individual markers when zoomed in
- Reduces marker count for better performance

**3. Visible Region Loading** (future):
- Only load markers/routes in visible map region
- Update when user pans/zooms
- Reduces data transfer and rendering

**4. Level of Detail** (future):
- Show simplified routes at low zoom
- Show full detail at high zoom
- Adaptive rendering

### Current Performance

**Rendering**:
- Handles 100+ markers smoothly
- Handles 50+ routes (simplified paths)
- Google Maps Compose uses GPU acceleration

**Memory**:
- Markers: ~200 bytes each
- Routes: ~100 bytes per coordinate
- Typical trip (30 days): ~50 markers + 40 routes = ~15KB

## Error Handling

Map visualization implements comprehensive error handling following the patterns in [ERROR_HANDLING.md](ERROR_HANDLING.md).

### Map Loading Errors

Handle map data loading errors:

```kotlin
@Composable
fun MapScreen(controller: MapController) {
    val state by controller.state.collectAsState()

    when {
        state.isLoading -> {
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
        state.error != null -> {
            ErrorView(
                message = state.error!!,
                icon = Icons.Default.Map,
                onRetry = { controller.loadMapData(startTime, endTime) }
            )
        }
        state.mapData.markers.isEmpty() && state.mapData.routes.isEmpty() -> {
            EmptyMapView(
                message = "No location data yet. Start tracking to see your travels on the map."
            )
        }
        else -> {
            MapView(controller = controller)
        }
    }
}
```

### Network Errors

Handle geocoding and map tile loading errors:

```kotlin
// Controller handling
fun loadMapData(startTime: Instant, endTime: Instant) {
    viewModelScope.launch {
        _state.update { it.copy(isLoading = true, error = null) }

        when (val result = getMapDataUseCase.execute(userId, startTime, endTime)) {
            is Result.Success -> {
                _state.update {
                    it.copy(
                        mapData = result.data,
                        isLoading = false,
                        error = null
                    )
                }

                // Auto-fit map to data
                result.data.region?.let { region ->
                    fitToRegion(region)
                }
            }
            is Result.Error -> {
                val errorMessage = when (result.error) {
                    is TrailGlassError.NetworkError.NoConnection -> {
                        "No internet connection. Showing cached map data."
                    }
                    is TrailGlassError.DatabaseError -> {
                        "Unable to load map data. Please try again."
                    }
                    else -> result.error.userMessage
                }

                result.error.logToAnalytics(
                    errorAnalytics,
                    context = mapOf("operation" to "loadMapData")
                )

                _state.update {
                    it.copy(isLoading = false, error = errorMessage)
                }
            }
        }
    }
}
```

### Offline Mode

Map visualization works offline with cached tiles:

```kotlin
// Check connectivity before loading
val isOnline = networkConnectivity.isNetworkAvailable()

if (!isOnline) {
    // Show offline banner
    showOfflineBanner("You're offline. Map tiles may not load.")

    // Load markers/routes from local database (works offline)
    val mapData = getMapDataUseCase.execute(userId, startTime, endTime)

    // Map tiles may be cached by Google Maps/MapKit
}
```

### Camera Position Errors

Handle invalid camera positions gracefully:

```kotlin
fun moveCameraTo(coordinate: Coordinate, zoom: Float) {
    try {
        // Validate coordinate
        if (coordinate.latitude !in -90.0..90.0 ||
            coordinate.longitude !in -180.0..180.0
        ) {
            logger.warn { "Invalid coordinate: $coordinate" }
            return
        }

        // Validate zoom
        val validZoom = zoom.coerceIn(1f, 20f)

        _state.update {
            it.copy(
                cameraPosition = CameraPosition(
                    target = coordinate,
                    zoom = validZoom
                )
            )
        }
    } catch (e: Exception) {
        logger.error(e) { "Failed to move camera" }
        // Don't crash - just keep current position
    }
}
```

### Marker Click Errors

Handle marker click errors safely:

```kotlin
Marker(
    state = MarkerState(position = latLng),
    title = marker.title,
    onClick = {
        try {
            controller.selectMarker(marker)
            onMarkerClick(marker)
            true
        } catch (e: Exception) {
            logger.error(e) { "Failed to handle marker click" }
            false  // Return false to not consume the click
        }
    }
)
```

### Map API Errors

Handle Google Maps API errors (Android):

```kotlin
// In AndroidManifest.xml, add API key with error handling
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="${MAPS_API_KEY}" />

// Check if Google Play Services is available
fun checkGooglePlayServices(context: Context): Boolean {
    val availability = GoogleApiAvailability.getInstance()
    val resultCode = availability.isGooglePlayServicesAvailable(context)

    return when (resultCode) {
        ConnectionResult.SUCCESS -> true
        else -> {
            // Show error dialog
            if (availability.isUserResolvableError(resultCode)) {
                availability.getErrorDialog(activity, resultCode, REQUEST_CODE)?.show()
            } else {
                showError("Google Play Services is required for maps")
            }
            false
        }
    }
}
```

### Error Analytics

Map errors are logged with context:

```kotlin
try {
    val mapData = getMapDataUseCase.execute(userId, startTime, endTime)
} catch (e: Exception) {
    val error = ErrorMapper.mapException(e)
    error.logToAnalytics(
        errorAnalytics,
        context = mapOf(
            "operation" to "loadMapData",
            "startTime" to startTime.toString(),
            "endTime" to endTime.toString(),
            "markerCount" to mapData.markers.size.toString(),
            "routeCount" to mapData.routes.size.toString()
        ),
        severity = ErrorSeverity.ERROR
    )
    throw error
}
```

### Retry Logic

Implement retry for map data loading:

```kotlin
// Retry with exponential backoff
val mapData = retryWithPolicy(
    policy = RetryPolicy.DEFAULT,
    onRetry = { state ->
        logger.info { "Retrying map data load (attempt ${state.attempt})" }
        showRetryingIndicator()
    }
) {
    getMapDataUseCase.execute(userId, startTime, endTime)
}
```

## Best Practices

### UX

1. **Always fit to data on load**: Show all content immediately
2. **Provide manual controls**: Let users explore
3. **Show selected marker info**: Contextual information
4. **Use clear visual hierarchy**: Markers above routes
5. **Provide legend** (future): Explain route colors

### Performance

1. **Limit data range**: Default to last 30 days
2. **Use simplified paths**: Already done via PathSimplifier
3. **Lazy load images** (for photo markers): Future enhancement
4. **Cache map tiles**: Handled by Google Maps/MapKit
5. **Debounce region changes** (for future dynamic loading)

### Privacy

1. **User location**: Only show if user enables
2. **Public maps**: Don't include sensitive visit names
3. **Photo markers** (future): Respect privacy settings

## Testing

### Unit Tests

```kotlin
@Test
fun testMapDataConversion() = runTest {
    val visit = PlaceVisit(/* ... */)
    val route = RouteSegment(/* ... */)

    val useCase = GetMapDataUseCase(mockVisitRepo, mockRouteRepo)
    val mapData = useCase.execute("user1", start, end)

    assertTrue(mapData.markers.isNotEmpty())
    assertTrue(mapData.routes.isNotEmpty())
    assertNotNull(mapData.region)
}

@Test
fun testBoundingRegion() {
    val markers = listOf(
        MapMarker(/* Paris */),
        MapMarker(/* London */),
        MapMarker(/* Berlin */)
    )

    val region = calculateBoundingRegion(markers, emptyList())

    // Verify region contains all markers
    assertTrue(region.northEast.latitude >= 51.5074) // London
    assertTrue(region.southWest.latitude <= 48.8566) // Paris
}
```

### Integration Tests

```kotlin
@Test
fun testMapScreen() {
    composeTestRule.setContent {
        MapScreen(mockController)
    }

    // Verify map is displayed
    composeTestRule.onNodeWithContentDescription("Map").assertExists()

    // Verify fit button
    composeTestRule.onNodeWithContentDescription("Fit to data").assertExists()
}
```

## Future Enhancements

- [ ] Marker clustering (for better performance)
- [ ] Heat maps (density visualization)
- [ ] Route animation (playback trips)
- [ ] 3D terrain view
- [ ] Offline maps support
- [ ] Custom map styles (dark mode, satellite)
- [ ] Photo markers (show photos on map)
- [ ] Search on map
- [ ] Measure distance tool
- [ ] Export map as image
- [ ] Share location/trip
- [ ] Real-time location tracking on map
- [ ] AR navigation (future)

## Resources

- [Google Maps Compose](https://developers.google.com/maps/documentation/android-sdk/maps-compose)
- [Google Maps Android API](https://developers.google.com/maps/documentation/android-sdk)
- [MapKit (iOS)](https://developer.apple.com/documentation/mapkit)
- [MKMapView](https://developer.apple.com/documentation/mapkit/mkmapview)

---

**Related Documentation**:
- [Error Handling](ERROR_HANDLING.md) - Comprehensive error handling guide
- [Architecture](ARCHITECTURE.md) - System architecture overview
- [Location Tracking](LOCATION_TRACKING.md) - Platform location tracking
- [UI Implementation](UI_IMPLEMENTATION.md) - Material 3 UI components
- [Testing](TESTING.md) - Testing strategy and coverage
