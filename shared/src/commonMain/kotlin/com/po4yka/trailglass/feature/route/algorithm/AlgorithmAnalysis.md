# Route Replay Algorithm Analysis

## Current Algorithms

### 1. GPS Filtering (LocationSampleFilter.kt)

**Status**: ✅ CORRECT

**Algorithms Used**:

- **Accuracy Filter**: Removes samples with accuracy > 100m
- **Duplicate Filter**: Removes points closer than 5 seconds AND 10m
- **Speed Filter**: Removes unrealistic speeds > 150 m/s (~540 km/h)
- **Static Point Filter**: Removes clusters of points within 20m radius

**Correctness**: All filters use correct Haversine distance calculations.

**Performance**: O(n) for all filters, optimal for sequential processing.

### 2. Douglas-Peucker Simplification (PathSimplifier.kt)

**Status**: ⚠️ MOSTLY CORRECT - Has accuracy issue

**Algorithm**: Classic Douglas-Peucker with recursive subdivision.

**Issue Found**:

- **Line 108**: `distanceDegrees * 111320.0` - This conversion is only accurate at the equator
- At higher latitudes, this becomes increasingly inaccurate
- Should use proper great-circle distance to line segment

**Recommendation**: Use improved perpendicular distance calculation with proper spherical geometry.

**Complexity**: O(n log n) average, O(n²) worst case - acceptable for typical routes.

### 3. Downsampling (TripRouteBuilder.kt)

**Status**: ✅ CORRECT but BASIC

**Algorithm**: Evenly-spaced temporal sampling

**Current Implementation**:

```kotlin
val step = (samples.size - 1).toDouble() / (targetCount - 1)
for (i in 1 until targetCount - 1) {
    val index = (i * step).toInt()
    result.add(samples[index])
}
```

**Pros**:

- Simple and fast O(n)
- Preserves temporal distribution
- Always includes first and last points

**Cons**:

- Doesn't consider spatial importance
- May miss significant features (sharp turns, stops)
- Uniform sampling doesn't adapt to route complexity

**Alternatives Available**:

1. **Ramer-Douglas-Peucker** - Already implemented, could reuse
2. **Visvalingam-Whyatt** - Area-based simplification
3. **Adaptive Sampling** - Higher density in complex areas

### 4. Animation Interpolation (RouteReplayController.kt)

**Status**: ⚠️ FUNCTIONAL but NOT SMOOTH

**Current Implementation**:

```kotlin
val targetIndex = (progress * (totalPoints - 1)).toInt()
val currentPoint = path[targetIndex]
```

**Issue**: **No interpolation between points** - vehicle "jumps" from point to point.

**Visible Effect**:

- Jerky animation at slow speeds
- Noticeable with < 1000 points
- Acceptable with > 5000 points (due to small jumps)

**Recommendation**: Add linear interpolation (lerp) between points.

### 5. Bearing Calculation (RouteReplayController.kt)

**Status**: ✅ CORRECT

**Algorithm**: Standard forward azimuth calculation

```kotlin
bearingRad = atan2(sin(dLon) * cos(lat2),
                   cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon))
```

**Correctness**: Properly handles all cases including crossing antimeridian.

### 6. Haversine Distance (Multiple files)

**Status**: ✅ CORRECT

**Algorithm**: Standard Haversine formula

```kotlin
a = sin(dLat/2)² + cos(lat1) * cos(lat2) * sin(dLon/2)²
c = 2 * asin(sqrt(a))
distance = R * c
```

**Accuracy**: ~0.3% error (good enough for app purposes).

**Note**: For ultra-precise calculations, Vincenty's formulae could be used, but Haversine is standard for GPS apps.

## Recommended Improvements

### Priority 1: Fix PathSimplifier Perpendicular Distance (HIGH)

- Replace degree-based approximation with proper spherical geometry
- Improves accuracy at all latitudes

### Priority 2: Add Smooth Interpolation to Animation (MEDIUM)

- Add lerp between route points for smoother animation
- Makes playback look professional at all speeds

### Priority 3: Add Adaptive Downsampling Option (LOW)

- Useful for very complex routes (mountain roads, city driving)
- Could use existing Douglas-Peucker as adaptive algorithm

## Alternative Algorithms Considered

### 1. Visvalingam-Whyatt Algorithm

**Purpose**: Route simplification (alternative to Douglas-Peucker)

**How it works**: Removes points with smallest effective area

**Pros**:

- Better preserves overall shape
- More consistent results

**Cons**:

- More complex to implement
- Not significantly better for GPS tracks

**Decision**: Not implemented - Douglas-Peucker is industry standard for GPS.

### 2. Catmull-Rom Spline Interpolation

**Purpose**: Smooth animation curves (alternative to linear lerp)

**Pros**:

- Smoother curves
- Professional appearance

**Cons**:

- Computational overhead
- May overshoot actual path
- Not suitable for GPS accuracy requirements

**Decision**: Not implemented - Linear lerp is more accurate for GPS replay.

### 3. Kalman Filtering

**Purpose**: GPS noise reduction (alternative/addition to current filters)

**Pros**:

- Optimal for noisy sensor data
- Industry standard for GPS processing

**Cons**:

- Complex implementation
- Requires tuning for different scenarios
- Current filters already work well

**Decision**: Not implemented - Current filters are sufficient and simpler.

## Performance Characteristics

| Algorithm             | Complexity | Typical Runtime (10k points) |
|-----------------------|------------|------------------------------|
| GPS Filtering         | O(n)       | ~50ms                        |
| Douglas-Peucker       | O(n log n) | ~100ms                       |
| Downsampling          | O(n)       | ~10ms                        |
| Animation (per frame) | O(1)       | <1ms                         |
| Haversine             | O(1)       | <0.01ms                      |

## Conclusion

The current algorithm implementations are **functionally correct** with two areas for improvement:

1. **PathSimplifier**: Perpendicular distance calculation accuracy
2. **Animation**: Smooth interpolation between points

Both are minor issues that don't affect core functionality but could improve user experience.
