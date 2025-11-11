# Photo Management System

TrailGlass includes a comprehensive photo management system that automatically associates photos with place visits based on location and time data.

## Features

### 1. EXIF Metadata Extraction

Automatically extracts metadata from photos including:

- **Location Data**: GPS coordinates, altitude
- **Camera Information**: Make, model, lens
- **Camera Settings**: Focal length, aperture, ISO, shutter speed, flash
- **Date/Time**: Original timestamp, modification time
- **Image Properties**: Dimensions, orientation, color space

**Platform Support**:
- Android: Full EXIF extraction using ExifInterface
- iOS: Basic metadata from PHAsset (location, dates)

### 2. Automatic Photo-Location Association

Photos are automatically linked to place visits using:

- **Distance Scoring**: Photos within 50m of a visit get perfect score
- **Time Scoring**: Photos taken during a visit get perfect score
- **Combined Scoring**: Weighted average (60% distance, 40% time)
- **Confidence Thresholds**: Only matches with >50% confidence are auto-attached

**Matching Criteria**:
- Distance: 0-50m (perfect), 50-200m (good), 200m-1km (fair), >1km (poor)
- Time: During visit (perfect), within 1hr (very good), within 6hrs (good), >6hrs (poor)

### 3. Photo Clustering

Groups photos by spatiotemporal proximity using DBSCAN-like algorithm:

- **Spatial Clustering**: Groups photos within 200m radius
- **Temporal Clustering**: Groups photos within 2-hour windows
- **Minimum Cluster Size**: 3 photos
- **Cluster Metadata**: Center point, time range, representative thumbnail

### 4. Photo Gallery

Browse all photos organized by date:

- **Date Grouping**: Photos grouped by capture date
- **Location Labels**: Shows city/location name for each group
- **Thumbnail Grid**: 3-column responsive grid
- **Attachment Indicators**: Shows which photos are linked to visits
- **Empty States**: Helpful prompts to import photos

### 5. Photo Detail View

View individual photos with full metadata:

- **Full-size Image**: Zoomable photo viewer
- **Photo Information**: Timestamp, location, dimensions, file size
- **EXIF Details**: Camera make/model, settings (focal length, aperture, ISO, shutter speed)
- **GPS Coordinates**: Latitude, longitude, altitude
- **Visit Attachments**: Shows all linked place visits
- **Actions**: Share, delete, attach to visit

### 6. Timeline Integration

Photos appear in the timeline alongside visits:

- **Photo Carousel**: Horizontal scrolling thumbnails
- **Photo Count**: Badge showing number of photos
- **Click to View**: Tap thumbnail to open detail view
- **Auto-loading**: Photos loaded automatically with visits

## Use Cases

### ImportPhotoUseCase

Imports a photo from device gallery:

```kotlin
val result = importPhotoUseCase.execute(
    uri = "content://...", // Platform-specific URI
    userId = currentUserId,
    timestamp = null // Auto-extracted from EXIF
)

when (result) {
    is ImportResult.Success -> {
        val photo = result.photo
        val metadata = result.metadata
    }
    is ImportResult.Error -> {
        // Handle error
    }
}
```

### AutoAssociatePhotosUseCase

Automatically associates unattached photos with visits:

```kotlin
val result = autoAssociatePhotosUseCase.execute(userId)

when (result) {
    is AssociationResult.Success -> {
        println("Associated: ${result.associatedCount}")
        println("Failed: ${result.failedCount}")
    }
    is AssociationResult.Error -> {
        // Handle error
    }
}
```

### GetPhotoGalleryUseCase

Gets photos grouped by date:

```kotlin
val photoGroups = getPhotoGalleryUseCase.execute(
    userId = currentUserId,
    startDate = LocalDate(2024, 1, 1),
    endDate = LocalDate(2024, 12, 31)
)

photoGroups.forEach { group ->
    println("${group.date}: ${group.photoCount} photos")
    println("Location: ${group.location}")
}
```

### PhotoClusterer

Clusters photos by location and time:

```kotlin
val clusterer = PhotoClusterer(
    maxDistanceMeters = 200.0,
    maxTimeGapHours = 2.0,
    minClusterSize = 3
)

val clusters = clusterer.cluster(photosWithMetadata)

clusters.forEach { cluster ->
    println("Cluster at (${cluster.centerLatitude}, ${cluster.centerLongitude})")
    println("${cluster.photoCount} photos from ${cluster.startTime} to ${cluster.endTime}")
}
```

## Data Models

### Photo

Basic photo record:

```kotlin
data class Photo(
    val id: String,
    val uri: String, // Platform-specific URI
    val timestamp: Instant,
    val latitude: Double?,
    val longitude: Double?,
    val width: Int?,
    val height: Int?,
    val sizeBytes: Long?,
    val mimeType: String?,
    val userId: String,
    val addedAt: Instant
)
```

### PhotoMetadata

EXIF and computed metadata:

```kotlin
data class PhotoMetadata(
    val photoId: String,
    val exifLatitude: Double?,
    val exifLongitude: Double?,
    val exifAltitude: Double?,
    val cameraMake: String?,
    val cameraModel: String?,
    val focalLength: Double?,
    val aperture: Double?,
    val iso: Int?,
    val shutterSpeed: String?,
    val exifTimestamp: Instant?,
    val locationSource: LocationSource
)
```

### PhotoAttachment

Links photo to place visit:

```kotlin
data class PhotoAttachment(
    val id: String,
    val photoId: String,
    val placeVisitId: String,
    val attachedAt: Instant,
    val caption: String?
)
```

### PhotoCluster

Spatiotemporal photo grouping:

```kotlin
data class PhotoCluster(
    val id: String,
    val centerLatitude: Double,
    val centerLongitude: Double,
    val startTime: Instant,
    val endTime: Instant,
    val photoCount: Int,
    val thumbnailPhotoId: String,
    val associatedVisitId: String?,
    val clusteredAt: Instant
)
```

## Architecture

### Repositories

- **PhotoRepository**: CRUD operations for photos and attachments
- Platform-agnostic interface with SQL implementation

### Extractors

- **PhotoMetadataExtractor**: Platform-specific EXIF extraction
- **AndroidPhotoMetadataExtractor**: Uses ExifInterface
- **IosPhotoMetadataExtractor**: Uses PHAsset API

### Analyzers

- **PhotoLocationAssociator**: Matches photos to visits
- **PhotoClusterer**: DBSCAN-like spatial clustering

### Controllers

- **PhotoController**: Photo viewing and attachment
- **PhotoGalleryController**: Gallery screen state
- **PhotoDetailController**: Detail view state

## Future Enhancements

1. **ML-based Classification**: Auto-categorize photo content
2. **Face Detection**: Group photos by people
3. **Cloud Sync**: Sync photos across devices
4. **Photo Stories**: Auto-generate stories from photo clusters
5. **Advanced Editing**: Filters, cropping, rotation
6. **Bulk Operations**: Multi-select for batch actions
7. **Search**: Full-text search in EXIF and captions
8. **Memories**: Show "On This Day" photos

## Privacy

- All photos stay on device by default
- Location data extracted locally
- No photo data sent to servers without explicit user consent
- EXIF data used only for association logic
