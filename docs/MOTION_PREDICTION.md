# Motion Prediction Integration

## Overview

The Trailglass Android app now includes AndroidX Input Motion Prediction library v1.0.0, which reduces perceived latency in touch and stylus input by predicting future motion events.

## Purpose

Motion prediction improves the user experience for high-frequency touch interactions by:
- Predicting where the user's finger/stylus will be in the next frame
- Reducing perceived latency in drawing and sketching
- Providing smoother visual feedback during touch interactions

## Use Cases in Trailglass

Potential applications include:
1. Drawing route annotations on maps
2. Sketch-based journal entries
3. Free-form drawing interfaces
4. Any gesture-based interactions requiring low latency

## Dependencies

**Gradle Configuration:**
```kotlin
// In gradle/libs.versions.toml
androidx-input-motionprediction = "1.0.0"

// In composeApp/build.gradle.kts
implementation(libs.androidx.input.motionprediction)
```

**Minimum SDK:** API 23 (Android 6.0)

## Implementation

### Core Components

1. **MotionPredictionHelper** (`composeApp/src/main/kotlin/com/po4yka/trailglass/ui/util/MotionPredictionHelper.kt`)
   - Wrapper around `MotionEventPredictor`
   - Provides simple API for recording and predicting motion events

2. **DrawingView** (`composeApp/src/main/kotlin/com/po4yka/trailglass/ui/util/DrawingView.kt`)
   - Example Android View demonstrating motion prediction
   - Shows actual path + predicted continuation
   - Can be used for map annotations or journal sketches

3. **DrawingCanvas** (`composeApp/src/main/kotlin/com/po4yka/trailglass/ui/components/DrawingCanvas.kt`)
   - Compose wrapper around DrawingView
   - Easy integration into Compose UI

### Usage Example

```kotlin
// In your Compose UI
@Composable
fun MapAnnotationScreen() {
    var showDrawing by remember { mutableStateOf(false) }

    Box {
        // Your map view
        GoogleMapView()

        // Overlay drawing canvas when needed
        if (showDrawing) {
            DrawingCanvas(
                modifier = Modifier.fillMaxSize()
            )
        }

        // Button to toggle drawing mode
        FloatingActionButton(
            onClick = { showDrawing = !showDrawing }
        ) {
            Icon(Icons.Default.Draw, "Toggle Drawing")
        }
    }
}
```

### Direct API Usage

```kotlin
// Create predictor with a View reference
val predictor = MotionPredictionHelper(myView)

// In your touch event handler
fun onTouchEvent(event: MotionEvent): Boolean {
    // Record the actual motion event
    predictor.recordMotionEvent(event)

    // Get predicted position
    val predictedEvent = predictor.predict()

    // Use predicted coordinates for rendering
    predictedEvent?.let { predicted ->
        val predictedX = predicted.x
        val predictedY = predicted.y
        // Draw predicted continuation...
    }

    return true
}
```

## Technical Details

### How It Works

1. **Event Recording**: Each actual `MotionEvent` is recorded via `predictor.record(event)`
2. **Prediction**: Based on velocity and acceleration from previous events, the library predicts the next position
3. **System Integration**: Automatically uses Android's system prediction API when available (Android 11+)
4. **Multi-pointer Support**: Handles multiple simultaneous touch points
5. **Stylus Data**: Includes orientation and tilt predictions for stylus input

### Performance Considerations

- Prediction calculations are lightweight and optimized for real-time use
- Uses system-level prediction when available for best performance
- Minimal memory overhead
- No background processing required

## Testing

The motion prediction works best with:
- Physical devices (especially those with stylus support like Galaxy Note, Tab S series)
- High refresh rate displays (90Hz+, 120Hz+)
- Stylus input for most noticeable latency reduction

Testing in emulator will show the API working but latency improvements are less noticeable.

## Future Enhancements

Potential future uses:
- Map route annotation feature
- Journal entry sketching
- Photo annotation
- Gesture-based navigation shortcuts
- Custom drawing tools for trip documentation

## References

- [AndroidX Input Release Notes](https://developer.android.com/jetpack/androidx/releases/input)
- [Motion Prediction API Guide](https://developer.android.com/reference/androidx/input/motionprediction/MotionEventPredictor)
- Library: `androidx.input:input-motionprediction:1.0.0`
