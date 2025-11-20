---
name: doc-maintainer
description: Maintains and updates project documentation including ARCHITECTURE.md, API docs, KDoc comments, and README files. Use when code changes require documentation updates.
allowed-tools:
  - Read
  - Write
  - Edit
  - Grep
  - Glob
---

# Documentation Maintainer

Keeps Trailglass documentation accurate, comprehensive, and up-to-date with code changes.

## Documentation Structure

### Core Documentation (`docs/`)
- `ARCHITECTURE.md` - System architecture, patterns, layer responsibilities
- `DEVELOPMENT.md` - Setup, building, debugging, troubleshooting
- `TESTING.md` - Testing strategy, coverage, best practices
- `ERROR_HANDLING.md` - Error types, retry mechanisms, patterns
- `LOCATION_TRACKING.md` - Platform location implementations
- `UI_IMPLEMENTATION.md` - UI architecture and design systems
- `dependency-injection.md` - kotlin-inject setup and usage

### Root Documentation
- `README.md` - Project overview, quick start, features
- `CLAUDE.md` - Claude Code instructions
- `GEMINI.md` - Gemini CLI instructions
- `AGENTS.md` - Universal AI agent instructions

### Module Documentation
- `shared/src/commonMain/kotlin/*/README.md` - Module-specific docs
- Component-level KDoc comments

## Documentation Types

### 1. Architecture Documentation

**When to Update:**
- New layers or components added
- Architecture patterns changed
- Data flow modified
- New design patterns introduced

**ARCHITECTURE.md Sections:**
```markdown
# Architecture

## Overview
High-level system architecture diagram and principles

## Project Structure
Directory layout with purpose of each module

## Layer Responsibilities
Detailed description of each architectural layer

## State Management
StateFlow patterns, immutability, unidirectional flow

## Dependency Injection
kotlin-inject setup, scoping, platform modules

## Navigation
Decompose patterns, navigation graphs

## Data Flow
Request/response flows, event handling

## Platform Integration
expect/actual patterns, platform-specific code

## Design Patterns
Repository, UseCase, Factory, Builder patterns used

## Best Practices
Guidelines and anti-patterns to avoid
```

**Example Update:**
```markdown
### Location Processing Pipeline (NEW)

The location processing pipeline transforms raw GPS points into meaningful visits:

1. **Raw Samples** → LocationSamplesRepository
2. **Clustering** → PlaceVisitDetector (DBSCAN algorithm)
3. **Geocoding** → ReverseGeocoder with cache
4. **Visit Creation** → PlaceVisitRepository
5. **Trip Assembly** → TripDetector

```kotlin
// Pipeline flow
LocationTracker → LocationSamples → PlaceVisitDetector →
  ReverseGeocoder → PlaceVisit → TripDetector → Trip
```
```

### 2. API Documentation (KDoc)

**When to Add:**
- Public classes and interfaces
- Non-obvious functions
- Complex algorithms
- Platform-specific code

**KDoc Format:**
```kotlin
/**
 * Detects place visits from location samples using DBSCAN clustering algorithm.
 *
 * Analyzes temporal and spatial patterns in location data to identify periods where
 * the user remained stationary at a specific location for a meaningful duration.
 *
 * @param samples Raw location samples to analyze, must be sorted by timestamp
 * @param minStayDuration Minimum time at location to qualify as visit (default: 5 minutes)
 * @param maxRadius Maximum radius in meters to cluster points (default: 100m)
 * @return List of detected place visits with center coordinates and time ranges
 * @throws IllegalArgumentException if samples list is empty
 *
 * @see PlaceVisit
 * @see LocationSample
 *
 * Example:
 * ```kotlin
 * val samples = locationRepository.getSamples(userId, startTime, endTime)
 * val visits = placeVisitDetector.detectVisits(
 *     samples = samples,
 *     minStayDuration = 10.minutes
 * )
 * ```
 */
suspend fun detectPlaceVisits(
    samples: List<LocationSample>,
    minStayDuration: Duration = 5.minutes,
    maxRadius: Double = 100.0
): List<PlaceVisit>
```

**Components:**
- Brief description (first line)
- Detailed explanation (paragraph)
- `@param` for each parameter with constraints
- `@return` with what's returned
- `@throws` for exceptions
- `@see` for related items
- Example usage in code block

### 3. Feature Documentation

**When to Add:**
- New feature implemented
- Feature behavior changes
- Platform-specific implementations

**Feature Doc Template:**
```markdown
# Feature Name

## Overview
Brief description of what this feature does

## Architecture
How it fits into the overall system

## Components
- **Component1**: Description and responsibility
- **Component2**: Description and responsibility

## Data Flow
Step-by-step flow diagram or list

## Usage
Code examples showing how to use the feature

## Platform Differences
Android vs iOS implementation details

## Configuration
Any settings or configuration needed

## Testing
How to test the feature

## Known Issues
Current limitations or bugs

## Future Enhancements
Planned improvements
```

**Example:**
```markdown
# Photo Attachment Feature

## Overview
Allows users to attach photos from their device gallery to place visits, with smart
time and location-based matching.

## Architecture
```
PhotoRepository → MediaStore/Photos Framework (platform) →
  PhotoMatchingAlgorithm → PhotoAttachmentRepository
```

## Components
- **PhotoRepository**: Accesses device photo library (platform-specific)
- **PhotoMatchingAlgorithm**: Matches photos to visits based on time + location
- **PhotoAttachmentRepository**: Stores photo-visit associations

## Platform Differences

**Android:** Uses MediaStore API with content resolvers
**iOS:** Uses Photos Framework with PHAsset

## Usage
```kotlin
// Get suggested photos for a visit
val suggestions = photoController.getSuggestionsForVisit(visitId)

// Attach photo
photoController.attachPhoto(photoId, visitId, caption = "Eiffel Tower")
```
```

### 4. README Updates

**When to Update:**
- Feature list changes
- Setup instructions change
- New dependencies added
- Build commands change

**README.md Sections:**
- Project overview
- Features (keep synchronized with implementation)
- Quick start commands
- Tech stack (verify versions match)
- Documentation links
- Implementation status

### 5. Module READMEs

**Location:** Alongside code in each module

**Purpose:** Explain module-specific details

**Example:** `shared/src/commonMain/kotlin/location/README.md`
```markdown
# Location Processing Module

## Purpose
Processes raw location samples into meaningful place visits and routes.

## Components
- `LocationTracker` (expect/actual) - Platform location tracking
- `PlaceVisitDetector` - DBSCAN clustering algorithm
- `RouteSegmentBuilder` - Generates routes between visits
- `ReverseGeocoder` (expect/actual) - Platform geocoding
- `DatabaseGeocodingCache` - Persistent geocoding cache

## Algorithms

### DBSCAN Clustering
Parameters:
- epsilon (ε): 100 meters
- minPoints: 3 samples
- timeWindow: 5 minutes

### Route Classification
Transport types determined by:
- Speed: <3 km/h = WALK, 3-30 = CAR, 30+ = TRAIN/PLANE
- Distance: <1km = WALK, 1-500km = CAR/TRAIN, 500+ = PLANE
- Pattern analysis: acceleration, stops, trajectory

## Usage
See examples in tests: `LocationProcessingTest.kt`
```

## Documentation Maintenance Process

### 1. Detect Changes Requiring Docs

**Code Change Indicators:**
- New public classes/interfaces
- Architecture modifications
- New features
- API changes
- Dependency updates

**Check:**
```bash
# Find classes missing KDoc
grep -r "class.*{" shared/src/commonMain/kotlin/ | grep -v "/**"

# Find changed files in PR
git diff --name-only main...HEAD

# Check if docs exist for modified modules
```

### 2. Update Relevant Documentation

**For New Feature:**
1. Add to README.md feature list
2. Create or update feature doc in `docs/`
3. Add KDoc to public APIs
4. Update ARCHITECTURE.md if needed
5. Add to AGENTS.md if AI-relevant

**For Bug Fix:**
1. Update Known Issues if applicable
2. Update examples if they were wrong

**For Refactoring:**
1. Update architecture docs if structure changed
2. Update code examples
3. Update KDoc references

### 3. Verify Documentation Accuracy

**Checklist:**
- [ ] Code examples compile and run
- [ ] File paths are correct
- [ ] Version numbers match
- [ ] Screenshots are current (if any)
- [ ] Links work
- [ ] No outdated information
- [ ] Consistent formatting

### 4. Review Documentation Quality

**Quality Standards:**
- Clear and concise writing
- Proper grammar and spelling
- Logical organization
- Appropriate detail level
- Helpful examples
- Up-to-date

## Documentation Anti-Patterns

### ❌ Avoid

**Stale Documentation:**
```markdown
# Old docs
Uses deprecated LocationManager class
```
**Fix:** Keep synchronized with code changes

**Missing Context:**
```kotlin
/**
 * Processes data.
 */
fun processData(data: Data)
```
**Fix:** Explain what processing means, what data, why

**Over-Documentation:**
```kotlin
/**
 * Gets the user ID.
 * @return the user ID as a String
 */
fun getUserId(): String  // Obvious from signature
```
**Fix:** Only document non-obvious things

**Outdated Examples:**
```kotlin
// Example using old API
val data = repository.getData()  // No longer exists
```
**Fix:** Test examples, keep current

### ✅ Do

**Clear KDoc:**
```kotlin
/**
 * Applies DBSCAN clustering to detect stationary periods in location data.
 *
 * Clusters are formed when at least [minPoints] samples are within [epsilon]
 * meters of each other. Only clusters with duration ≥ [minStayDuration] are
 * returned as place visits.
 *
 * @param samples Location samples sorted by timestamp
 * @param epsilon Maximum distance in meters between cluster points (default: 100m)
 * @param minPoints Minimum samples to form cluster (default: 3)
 * @param minStayDuration Minimum visit duration (default: 5 minutes)
 * @return Detected place visits with center coordinates and timestamps
 */
```

**Helpful Examples:**
```kotlin
// Complete, runnable example
val detector = PlaceVisitDetector()
val samples = repository.getSamples(userId, today)
val visits = detector.detectPlaceVisits(
    samples = samples,
    minStayDuration = 10.minutes
)
println("Found ${visits.size} visits")
```

**Current Architecture Docs:**
```markdown
## State Management (Updated 2025-11)

Controllers use StateFlow with immutable state updates:
- Never expose MutableStateFlow
- Update with `.update { it.copy(...) }`
- Include loading/error/data states
```

## Automation

### Documentation Checks

**Pre-commit Hook:**
```bash
#!/bin/bash
# Check for public APIs without KDoc
if git diff --cached --name-only | grep "\.kt$"; then
    echo "Checking for missing KDoc..."
    # Add KDoc validation here
fi
```

### Documentation Generation

**KDoc to HTML:**
```bash
# Generate API docs with Dokka
./gradlew dokkaHtml

# Output: build/dokka/html/index.html
```

## Common Documentation Tasks

### Adding New Feature Documentation

1. Read the feature code
2. Identify key components
3. Create/update feature doc in `docs/`
4. Add to README.md feature list
5. Add KDoc to public APIs
6. Add examples
7. Mention in AGENTS.md if relevant

### Updating After Refactoring

1. Search docs for old class/method names
2. Update all references
3. Update code examples
4. Check architecture diagrams
5. Verify file paths

### Documenting Platform-Specific Code

```kotlin
/**
 * Android implementation using FusedLocationProviderClient.
 *
 * Provides high-accuracy location updates optimized for battery life.
 * Requires location permissions before calling [startTracking].
 *
 * @see LocationTracker for the common interface
 */
actual class LocationTracker(private val context: Context) {
    // Implementation
}
```

## Documentation Review

Before committing docs:
- [ ] Spelling and grammar checked
- [ ] Code examples tested
- [ ] Links verified
- [ ] Formatting consistent
- [ ] No stale information
- [ ] Appropriate detail level

## Related Documentation

- `.cursor/rules/project-conventions.mdc` - Documentation conventions
- `AGENTS.md` - Documentation section
- `docs/` - All project documentation

---

*Use this skill to keep Trailglass documentation accurate, helpful, and synchronized with code.*
