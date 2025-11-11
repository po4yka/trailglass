# Photo Integration

TrailGlass photo integration allows users to attach photos from their device to place visits, creating visual memories of their travels.

## Overview

```
Photo Library (Device)
      ↓
  PhotoPicker (Platform-specific)
      ↓
Photo Metadata Extraction
      ↓
   Photo Database
      ↓
Photo-Visit Matching (Time + Location)
      ↓
Photo Attachments
```

## Architecture

**Platform-Agnostic Layer** (commonMain):
- Domain models (Photo, PhotoAttachment)
- Photo repository interface
- Use cases (Get, Suggest, Attach)
- Photo controller

**Android Layer**:
- MediaStore integration
- ActivityResultContracts for photo picking
- Permission handling (READ_MEDIA_IMAGES / READ_EXTERNAL_STORAGE)
- Coil for image loading

**iOS Layer**:
- PHPicker for photo selection
- Photos framework (PHAsset)
- Permission handling (NSPhotoLibraryUsageDescription)

## Domain Models

### Photo

```kotlin
data class Photo(
    val id: String,                    // Unique identifier
    val uri: String,                   // Platform URI (content:// or localIdentifier)
    val timestamp: Instant,            // When photo was taken
    val latitude: Double?,             // GPS location (if available)
    val longitude: Double?,
    val width: Int?,                   // Dimensions
    val height: Int?,
    val sizeBytes: Long?,              // File size
    val mimeType: String?,             // e.g., "image/jpeg"
    val userId: String,                // Owner
    val addedAt: Instant               // When added to TrailGlass
)
```

### PhotoAttachment

```kotlin
data class PhotoAttachment(
    val id: String,
    val photoId: String,
    val placeVisitId: String,
    val attachedAt: Instant,
    val caption: String?               // Optional caption
)
```

## Database Schema

### Photos Table

```sql
CREATE TABLE photos (
    id TEXT PRIMARY KEY,
    uri TEXT NOT NULL,
    timestamp INTEGER NOT NULL,
    latitude REAL,
    longitude REAL,
    width INTEGER,
    height INTEGER,
    size_bytes INTEGER,
    mime_type TEXT,
    user_id TEXT NOT NULL,
    added_at INTEGER NOT NULL
);

-- Indices
CREATE INDEX photos_user_timestamp_idx ON photos(user_id, timestamp DESC);
CREATE INDEX photos_location_idx ON photos(latitude, longitude);
CREATE INDEX photos_timestamp_idx ON photos(timestamp DESC);
```

### Photo Attachments Table

```sql
CREATE TABLE photo_attachments (
    id TEXT PRIMARY KEY,
    photo_id TEXT NOT NULL,
    place_visit_id TEXT NOT NULL,
    attached_at INTEGER NOT NULL,
    caption TEXT,
    FOREIGN KEY (photo_id) REFERENCES photos(id) ON DELETE CASCADE,
    FOREIGN KEY (place_visit_id) REFERENCES place_visits(id) ON DELETE CASCADE
);

-- Indices
CREATE INDEX photo_attachments_visit_idx ON photo_attachments(place_visit_id);
CREATE INDEX photo_attachments_photo_idx ON photo_attachments(photo_id);
```

## Use Cases

### 1. GetPhotosForDayUseCase

Get all photos taken on a specific day.

```kotlin
val useCase = GetPhotosForDayUseCase(photoRepository)
val photos = useCase.execute(date = LocalDate(2025, 11, 17), userId = "user1")
```

### 2. SuggestPhotosForVisitUseCase

Suggest photos that might belong to a place visit using time and location matching.

**Scoring Algorithm**:
- **Time Score** (0.0 - 1.0):
  - 1.0 if photo timestamp is within visit time range
  - Decays based on time difference (30 min = 0.5 score)

- **Location Score** (0.0 - 1.0):
  - 1.0 if photo location is within 500m of visit center
  - Decays based on distance (1km = 0.5 score)

**Total Score** = Time Score + Location Score (0.0 - 2.0)

```kotlin
val useCase = SuggestPhotosForVisitUseCase(
    photoRepository,
    suggestionRadiusMeters = 500.0
)

val suggested = useCase.execute(visit = placeVisit, userId = "user1")
// Returns photos sorted by relevance (best match first)
```

### 3. AttachPhotoToVisitUseCase

Attach a photo to a place visit with optional caption.

```kotlin
val useCase = AttachPhotoToVisitUseCase(photoRepository)

when (val result = useCase.execute(photoId, visitId, caption = "Eiffel Tower!")) {
    is AttachPhotoToVisitUseCase.Result.Success -> { /* Success */ }
    is AttachPhotoToVisitUseCase.Result.AlreadyAttached -> { /* Already attached */ }
    is AttachPhotoToVisitUseCase.Result.Error -> { /* Handle error */ }
}
```

## Controller

**PhotoController** manages photo state with StateFlow:

```kotlin
data class PhotoState(
    val selectedDate: LocalDate?,
    val photos: List<Photo>,
    val suggestedPhotos: List<Photo>,
    val selectedVisit: PlaceVisit?,
    val isLoading: Boolean,
    val error: String?
)

// Usage
controller.loadPhotosForDay(date)
controller.loadSuggestionsForVisit(visit)
controller.attachPhotoToVisit(photoId, caption)
```

## Platform Implementation

### Android

#### Photo Picking

Uses **ActivityResultContracts.PickMultipleVisualMedia** (Android 11+):

```kotlin
// In Activity
val pickMedia = registerForActivityResult(
    ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
) { uris ->
    // Extract metadata from URIs
    val photoPicker = AndroidPhotoPicker(context, userId)
    val photos = photoPicker.extractPhotosFromUris(uris)

    // Save to database
    photos.forEach { photo ->
        photoRepository.insertPhoto(photo)
    }
}

// Launch picker
pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
```

#### Metadata Extraction

**AndroidPhotoPicker** uses MediaStore to extract:
- Date taken (DATE_TAKEN)
- GPS location (LATITUDE, LONGITUDE)
- Dimensions (WIDTH, HEIGHT)
- File size (SIZE)
- MIME type (MIME_TYPE)

```kotlin
suspend fun extractPhotoFromUri(uri: Uri): Photo? {
    val metadata = extractMetadataFromUri(uri) // MediaStore query
    return Photo(
        id = "photo_${UUID.randomUUID()}",
        uri = uri.toString(),
        timestamp = metadata.timestamp,
        // ... other fields
    )
}
```

#### Permissions

**Android 13+**: `READ_MEDIA_IMAGES`
**Android 12 and below**: `READ_EXTERNAL_STORAGE`

```kotlin
val permissions = PhotoPermissions(activity)

if (!permissions.hasPhotoPermissions()) {
    permissions.requestPhotoPermissions { granted ->
        if (granted) {
            // Launch photo picker
        }
    }
}
```

**AndroidManifest.xml**:
```xml
<!-- Android 13+ -->
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />

<!-- Android 12 and below -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"
    android:maxSdkVersion="32" />
```

### iOS

#### Photo Picking

Uses **PHPickerViewController** (iOS 14+):

```swift
import PhotosUI

var configuration = PHPickerConfiguration(photoLibrary: .shared())
configuration.filter = .images
configuration.selectionLimit = 10

let picker = PHPickerViewController(configuration: configuration)
picker.delegate = self

present(picker, animated: true)

// Delegate
func picker(_ picker: PHPickerViewController, didFinishPicking results: [PHPickerResult]) {
    let identifiers = results.compactMap(\.assetIdentifier)

    // Fetch PHAssets
    let fetchResult = PHAsset.fetchAssets(
        withLocalIdentifiers: identifiers,
        options: nil
    )

    // Extract metadata
    let photoPicker = IOSPhotoPicker(userId: userId)
    let photos = photoPicker.extractPhotosFromAssets(fetchResult)

    // Save to database
    photos.forEach { photo in
        photoRepository.insertPhoto(photo)
    }
}
```

#### Metadata Extraction

**IOSPhotoPicker** uses PHAsset to extract:
- Creation date (creationDate)
- GPS location (location.coordinate)
- Dimensions (pixelWidth, pixelHeight)
- Media subtype (for MIME type inference)

```kotlin
suspend fun extractPhotoFromAsset(asset: PHAsset): Photo? {
    val metadata = extractMetadataFromAsset(asset)
    return Photo(
        id = "photo_${UUID.randomUUID()}",
        uri = asset.localIdentifier,
        timestamp = metadata.timestamp,
        // ... other fields
    )
}
```

#### Permissions

Requires **NSPhotoLibraryUsageDescription** in Info.plist:

```xml
<key>NSPhotoLibraryUsageDescription</key>
<string>TrailGlass needs access to your photos to attach them to your travel memories.</string>
```

**Permission Request**:
```kotlin
val photoPicker = IOSPhotoPicker(userId)
val hasPermission = photoPicker.hasPermissions()

if (!hasPermission) {
    photoPicker.requestPermissions()
}
```

## UI Components (Android)

### PhotoGrid

Lazy grid of photos with Coil image loading:

```kotlin
@Composable
fun PhotoGrid(
    photos: List<Photo>,
    onPhotoClick: (Photo) -> Unit,
    columns: Int = 3
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns)
    ) {
        items(photos) { photo ->
            SubcomposeAsyncImage(
                model = Uri.parse(photo.uri),
                contentDescription = "Photo",
                contentScale = ContentScale.Crop
            )
        }
    }
}
```

**Features**:
- Lazy loading (virtualized grid)
- Loading indicators
- Error placeholders
- Empty state
- Clickable items

### Usage in Screens

**Timeline Screen** with photos:
```kotlin
@Composable
fun TimelineScreen(
    controller: TimelineController,
    photoController: PhotoController
) {
    val timelineState by controller.state.collectAsState()
    val photoState by photoController.state.collectAsState()

    Column {
        // Timeline items
        TimelineContent(timelineState.items)

        // Photos for the day
        if (photoState.photos.isNotEmpty()) {
            Text("Photos", style = MaterialTheme.typography.titleMedium)
            PhotoGrid(
                photos = photoState.photos,
                onPhotoClick = { photo -> /* Show detail */ }
            )
        }
    }
}
```

**Visit Detail** with photo suggestions:
```kotlin
@Composable
fun VisitDetailScreen(
    visit: PlaceVisit,
    photoController: PhotoController
) {
    val photoState by photoController.state.collectAsState()

    LaunchedEffect(visit) {
        photoController.loadSuggestionsForVisit(visit)
    }

    Column {
        Text("Visit: ${visit.city}")

        if (photoState.suggestedPhotos.isNotEmpty()) {
            Text("Suggested Photos")
            PhotoGrid(
                photos = photoState.suggestedPhotos,
                onPhotoClick = { photo ->
                    photoController.attachPhotoToVisit(photo.id)
                }
            )
        }
    }
}
```

## Photo Suggestion Examples

### Example 1: Perfect Match

**Visit**:
- Time: 2025-11-17 14:00 - 16:00
- Location: Eiffel Tower (48.8584°N, 2.2945°E)

**Photo**:
- Timestamp: 2025-11-17 15:30
- Location: 48.8586°N, 2.2947°E (20m away)

**Score**: 2.0 (1.0 time + 1.0 location) ✅ **Perfect match**

### Example 2: Time Match Only

**Visit**:
- Time: 2025-11-17 14:00 - 16:00
- Location: Eiffel Tower

**Photo**:
- Timestamp: 2025-11-17 15:30
- Location: None (no GPS)

**Score**: 1.0 (1.0 time + 0.0 location) ⚠️ **Suggested but uncertain**

### Example 3: Near Miss

**Visit**:
- Time: 2025-11-17 14:00 - 16:00
- Location: Eiffel Tower

**Photo**:
- Timestamp: 2025-11-17 16:10 (10 min after visit ended)
- Location: 48.8586°N, 2.2947°E (20m away)

**Score**: 1.67 (0.67 time + 1.0 location) ⚠️ **Good match, likely related**

## Error Handling

Photo integration implements comprehensive error handling following the patterns in [ERROR_HANDLING.md](ERROR_HANDLING.md).

### Photo Errors

All photo errors use the `TrailGlassError.PhotoError` sealed class:

```kotlin
// Permission denied
TrailGlassError.PhotoError.PermissionDenied(
    userMessage = "Photo library access is required to attach photos to your travels.",
    technicalMessage = "User denied photo library permission"
)

// Photo not found
TrailGlassError.PhotoError.PhotoNotFound(
    userMessage = "The photo you selected could not be found.",
    technicalMessage = "Photo not found in MediaStore/PHAsset"
)

// Load failed
TrailGlassError.PhotoError.LoadFailed(
    userMessage = "Unable to load photo. Please try again.",
    technicalMessage = "Failed to load photo from URI: IOException"
)

// Invalid photo
TrailGlassError.PhotoError.InvalidPhoto(
    userMessage = "This photo is not supported. Please select a different one.",
    technicalMessage = "Unsupported image format or corrupted file"
)

// Attachment failed
TrailGlassError.PhotoError.AttachmentFailed(
    userMessage = "Unable to attach photo. Please try again.",
    technicalMessage = "Failed to save photo attachment to database"
)
```

### Result Pattern

All photo operations return `Result<T>`:

```kotlin
// Attach photo to visit
when (val result = useCase.execute(photoId, visitId, caption)) {
    is AttachPhotoToVisitUseCase.Result.Success -> {
        showSuccess("Photo attached successfully")
    }
    is AttachPhotoToVisitUseCase.Result.AlreadyAttached -> {
        showWarning("This photo is already attached to this visit")
    }
    is AttachPhotoToVisitUseCase.Result.Error -> {
        when (result.error) {
            is TrailGlassError.PhotoError.PhotoNotFound -> {
                showError("Photo no longer exists")
            }
            is TrailGlassError.PhotoError.AttachmentFailed -> {
                showError(result.error.userMessage)
                // Offer retry
                showRetryButton()
            }
            else -> {
                showError(result.error.userMessage)
            }
        }
    }
}
```

### Permission Handling

Request permissions with proper error handling:

```kotlin
// Check permissions first
val permissions = PhotoPermissions(activity)

if (!permissions.hasPhotoPermissions()) {
    if (permissions.shouldShowPhotoRationale()) {
        // Show rationale dialog
        showPhotoRationaleDialog {
            permissions.requestPhotoPermissions { granted ->
                when (granted) {
                    true -> launchPhotoPicker()
                    false -> {
                        val error = TrailGlassError.PhotoError.PermissionDenied()
                        error.logToAnalytics(errorAnalytics)
                        showError(error.userMessage)
                    }
                }
            }
        }
    } else {
        permissions.requestPhotoPermissions { granted ->
            if (granted) launchPhotoPicker()
        }
    }
} else {
    launchPhotoPicker()
}
```

### Metadata Extraction Errors

Handle metadata extraction failures gracefully:

```kotlin
suspend fun extractPhotoFromUri(uri: Uri): Result<Photo> {
    return ErrorMapper.mapToResultSuspend {
        try {
            val metadata = extractMetadataFromUri(uri)

            Photo(
                id = "photo_${UUID.randomUUID()}",
                uri = uri.toString(),
                timestamp = metadata.timestamp ?: Clock.System.now(),
                latitude = metadata.latitude,  // May be null
                longitude = metadata.longitude,  // May be null
                width = metadata.width,
                height = metadata.height,
                sizeBytes = metadata.sizeBytes,
                mimeType = metadata.mimeType ?: "image/jpeg",
                userId = userId,
                addedAt = Clock.System.now()
            )
        } catch (e: Exception) {
            val error = ErrorMapper.mapPhotoException(e, PhotoContext.LOAD)
            error.logToAnalytics(
                errorAnalytics,
                context = mapOf("operation" to "extractMetadata", "uri" to uri.toString())
            )
            throw error
        }
    }
}
```

### Retry Logic

Photo operations support retry with backoff:

```kotlin
// Retry photo load with conservative policy
val photo = retryWithPolicy(
    policy = RetryPolicy.CONSERVATIVE,
    onRetry = { state ->
        logger.warn { "Retrying photo load (attempt ${state.attempt})" }
        showRetryingIndicator()
    }
) {
    photoPicker.extractPhotoFromUri(uri)
}
```

### Error Analytics

Photo errors are logged with context:

```kotlin
try {
    val photo = photoRepository.insertPhoto(photo)
} catch (e: Exception) {
    val error = ErrorMapper.mapPhotoException(e, PhotoContext.ATTACH)
    error.logToAnalytics(
        errorAnalytics,
        context = mapOf(
            "operation" to "insertPhoto",
            "photoId" to photo.id,
            "hasLocation" to (photo.latitude != null).toString()
        ),
        severity = ErrorSeverity.ERROR
    )
    return Result.Error(error)
}
```

### UI Error Handling

Show errors in photo grid:

```kotlin
@Composable
fun PhotoGrid(
    photos: List<Photo>,
    onPhotoClick: (Photo) -> Unit,
    error: String? = null,
    onRetry: () -> Unit = {}
) {
    when {
        error != null -> {
            ErrorView(
                message = error,
                icon = Icons.Default.Photo,
                onRetry = onRetry
            )
        }
        photos.isEmpty() -> {
            EmptyPhotoView(
                message = "No photos yet. Add photos from your library."
            )
        }
        else -> {
            LazyVerticalGrid(columns = GridCells.Fixed(3)) {
                items(photos) { photo ->
                    SubcomposeAsyncImage(
                        model = Uri.parse(photo.uri),
                        contentDescription = "Photo",
                        error = {
                            // Show placeholder for load errors
                            PhotoErrorPlaceholder()
                        },
                        loading = {
                            CircularProgressIndicator()
                        }
                    )
                }
            }
        }
    }
}
```

## Best Practices

### Performance

1. **Lazy Loading**: Use LazyVerticalGrid for photo lists
2. **Image Caching**: Coil automatically caches images
3. **Pagination**: Load photos in batches (e.g., by month)
4. **Thumbnails**: Use Coil's size() for thumbnail generation

### Privacy

1. **Permission Rationale**: Explain why photo access is needed
2. **Minimal Access**: Only request when needed
3. **User Control**: Allow photo detachment
4. **No Upload Without Consent**: Photos stay local unless explicitly synced

### UX

1. **Automatic Suggestions**: Show photo suggestions immediately
2. **Quick Attach**: One-tap to attach suggested photos
3. **Visual Feedback**: Show loading states during attachment
4. **Undo**: Allow removing attachments

## Testing

### Unit Tests

```kotlin
@Test
fun testPhotoSuggestion() = runTest {
    val visit = PlaceVisit(
        startTime = Instant.parse("2025-11-17T14:00:00Z"),
        endTime = Instant.parse("2025-11-17T16:00:00Z"),
        centerLatitude = 48.8584,
        centerLongitude = 2.2945
    )

    val photo = Photo(
        timestamp = Instant.parse("2025-11-17T15:30:00Z"),
        latitude = 48.8586,
        longitude = 2.2947
    )

    val useCase = SuggestPhotosForVisitUseCase(mockRepository)
    val suggestions = useCase.execute(visit, "user1")

    assertTrue(suggestions.contains(photo))
}
```

### Integration Tests

```kotlin
@Test
fun testPhotoAttachment() = runTest {
    // Insert photo
    photoRepository.insertPhoto(photo)

    // Attach to visit
    val attachment = PhotoAttachment(
        id = "attach1",
        photoId = photo.id,
        placeVisitId = visit.id,
        attachedAt = Clock.System.now()
    )
    photoRepository.attachPhotoToVisit(attachment)

    // Verify
    val photos = photoRepository.getPhotosForVisit(visit.id)
    assertEquals(1, photos.size)
    assertEquals(photo.id, photos[0].id)
}
```

## Future Enhancements

- [ ] Photo editing (crop, rotate, filters)
- [ ] Photo sharing (export with location data)
- [ ] Cloud photo storage
- [ ] Photo albums for trips
- [ ] Face detection for tagging people
- [ ] Automatic photo import from camera roll
- [ ] Photo slideshows
- [ ] Photo search by location/date
- [ ] Photo quality analysis (blur detection)
- [ ] RAW photo support

## Resources

- [Android Photo Picker](https://developer.android.com/training/data-storage/shared/photopicker)
- [MediaStore](https://developer.android.com/reference/android/provider/MediaStore)
- [PHPicker (iOS)](https://developer.apple.com/documentation/photokit/phpicker)
- [Photos Framework](https://developer.apple.com/documentation/photokit)
- [Coil Image Loading](https://coil-kt.github.io/coil/)

---

**Related Documentation**:
- [Error Handling](ERROR_HANDLING.md) - Comprehensive error handling guide
- [Architecture](ARCHITECTURE.md) - System architecture overview
- [UI Implementation](UI_IMPLEMENTATION.md) - Material 3 UI components
- [Testing](TESTING.md) - Testing strategy and coverage
