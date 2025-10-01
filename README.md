# Trailglass

> Your journeys, captured as they unfold.

Trailglass is a privacy-respectful travel logging app for iOS and Android.  
It quietly logs your location in the background, builds a living timeline of your trips, and lets you attach photos and journal entries to any moment along the way.

The code is open for **non-commercial use** under the **Non-Commercial Open Software License (NC-OSL)** by Nikita Pochaev (po4yka).

---

## Features

- **Background travel logging**
  - Lightweight background location tracking.
  - Automatic grouping of raw points into trips and daily segments.
  - Offline-friendly storage with sync conflict handling.

- **Timeline & map**
  - Chronological timeline of your days and trips.
  - Map view with reconstructed paths and key stops.
  - Detail screens for each trip/day with stats.

- **Photos & journal entries**
  - Attach photos to locations and time segments.
  - Write short notes or full journal entries for each day or trip.
  - Rich metadata: time, place, weather (planned) and other context.

- **Cross-device sync**
  - Sync between iOS and Android via a shared cloud backend.
  - Conflict-aware merge strategy designed for intermittent connectivity.
  - Prepared for end-to-end encryption for sensitive data (planned).

- **Native experience on both platforms**
  - Android: Material 3 Expressive, modern navigation, system dark mode, dynamic color.
  - iOS: Liquid Glass-inspired visuals, blur, depth, and native gesture patterns.

---

## Tech Stack (High-Level)

- **Core / Shared**
  - Kotlin Multiplatform (KMP) for shared domain, data, and sync logic.
  - Coroutines and Flow for async and reactive streams.
  - Kotlinx.serialization for data models and persistence formats.

- **Android**
  - Kotlin, Android Gradle Plugin 8+, Kotlin 2.x toolchain.
  - Jetpack Compose with Material 3 and Compose BOM.
  - Navigation Compose (Nav-3 style) for in-app navigation.
  - Room / SQLDelight (exact choice TBD) for offline storage.
  - WorkManager for background jobs (sync, maintenance).
  - DataStore for user preferences and feature flags.

- **iOS**
  - Native iOS app (Swift / SwiftUI).
  - Liquid Glass-style UI components (blur, translucency, depth).
  - Shared KMP layer bridged into Swift for domain and data.
  - Native background location APIs and scheduling.

- **Cloud / Backend (TBD, draft)**
  - A cloud sync service (e.g. Ktor server or similar) acting as:
    - API for pushing/pulling encrypted deltas.
    - Storage for user timelines, trips, and attachments metadata.
  - File/object storage for photos and large assets.
