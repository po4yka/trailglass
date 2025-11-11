# TrailGlass Dependencies

Complete guide to all dependencies used in the TrailGlass application.

---

## Core Dependencies

### Kotlin & Multiplatform
- **kotlin**: 2.2.21 - Modern programming language for Android, iOS, and shared code
- **kotlinMultiplatform**: Enables code sharing between Android and iOS
- **Coroutines**: 1.10.2 - Asynchronous programming and concurrency
- **kotlinx.datetime**: 0.7.1 - Multiplatform date/time library

### Build Tools
- **Android Gradle Plugin (AGP)**: 8.13 - Android build system
- **KSP**: 2.2.21-1.0.29 - Kotlin Symbol Processing for code generation
- **Gradle**: 8.11 - Build automation

---

## UI & Compose

### Jetpack Compose
- **Compose Multiplatform**: 1.9.1 - Declarative UI framework for multiplatform
- **Material 3**: Latest Material Design components
- **Compose Compiler**: Kotlin compiler plugin for Compose

### Navigation
- **Decompose**: 3.2.0 - Lifecycle-aware navigation library
  - Core: Multiplatform navigation with state preservation
  - Extensions Compose: Compose integration for Decompose
  - **Why**: Type-safe navigation, lifecycle management, deep linking support

### Image Loading
- **Coil**: 2.7.0 - Image loading library for Android
  - `coil-compose`: Compose integration
  - **Usage**: `PhotoGrid.kt` for photo display
  - **Why**: Efficient async image loading with caching

### Maps
- **Google Maps Compose**: 6.2.0 - Compose wrapper for Google Maps
- **Play Services Maps**: 19.0.0 - Google Maps SDK
  - **Usage**: `MapView.kt` for map visualization
  - **Requires**: `MAPS_API_KEY` in `local.properties`

---

## Data & Persistence

### Database
- **SQLDelight**: 2.1.0 - Type-safe multiplatform SQL database
  - Runtime core
  - Coroutines extensions
  - Android driver
  - iOS native driver
  - SQLite driver (for tests)
  - **Why**: Type-safe SQL, multiplatform support, compile-time verification

### Preferences
- **DataStore**: 1.1.1 - Modern Android preferences library
  - `datastore-preferences`: Android implementation
  - `datastore-preferences-core`: Shared core (KMP compatible)
  - **Why**: Type-safe preferences, Flow-based, replaces SharedPreferences

### Dependency Injection
- **kotlin-inject**: 0.7.2 - Compile-time dependency injection
  - Runtime: Core runtime library
  - Compiler: KSP processor for code generation
  - **Why**: Compile-time safety, no reflection, multiplatform support

---

## Networking

### HTTP Client
- **Ktor Client**: 3.0.1 - Multiplatform HTTP client
  - `ktor-client-core`: Core HTTP client
  - `ktor-client-okhttp`: Android engine (OkHttp-based)
  - `ktor-client-darwin`: iOS engine (Darwin-based)
  - `ktor-client-content-negotiation`: Content negotiation plugin
  - `ktor-serialization-kotlinx-json`: JSON serialization
  - `ktor-client-logging`: Logging plugin
  - **Usage**: Future backend sync, API calls, geocoding
  - **Why**: Multiplatform, coroutines-first, extensible

### Serialization
- **kotlinx.serialization**: 1.7.3 - Multiplatform serialization library
  - `kotlinx-serialization-json`: JSON support
  - **Usage**: Decompose navigation configs, API responses, data transfer
  - **Why**: Type-safe, multiplatform, compile-time code generation

---

## Background Work

### WorkManager
- **androidx.work**: 2.10.0 - Background task scheduler
  - `work-runtime-ktx`: Kotlin extensions
  - **Usage**: Periodic location processing, background sync
  - **Why**: Battery-efficient, OS-integrated, handles device states

---

## Security

### Secrets Management
- **Secrets Gradle Plugin**: 2.0.1 - API key management
  - **Configuration**: `local.properties` for API keys
  - **Template**: `local.defaults.properties`
  - **Why**: Keeps secrets out of version control

---

## Testing

### Unit Testing
- **JUnit**: 4.13.2 - Java testing framework
- **kotlin-test**: Multiplatform test library
- **MockK**: 1.14.6 - Kotlin mocking library
- **Turbine**: 1.2.1 - Flow testing utilities
- **Kotest**: 6.0.3 - Kotlin assertion library
- **kotlinx-coroutines-test**: Coroutine testing utilities

### UI Testing
- **Compose UI Test**: 1.9.1 - Compose testing framework
- **Espresso**: 3.7.0 - Android UI testing
- **androidx.test**: AndroidX test libraries

### Code Coverage
- **Kover**: 0.9.3 - Kotlin code coverage tool
  - Target: 75%+ coverage
  - Excludes: Generated code, DI components

---

## Logging

- **kotlin-logging**: 7.0.13 - Multiplatform logging facade
- **slf4j-android**: 2.0.17 - SLF4J Android binding

---

## Dependency Configuration

### Version Catalog (gradle/libs.versions.toml)

All dependencies are managed through Gradle version catalogs for centralized version management.

```toml
[versions]
kotlin = "2.2.21"
compose = "1.9.1"
decompose = "3.2.0"
coil = "2.7.0"
ktor = "3.0.1"
# ... etc
```

### Source Set Dependencies

**commonMain** (shared code):
- SQLDelight, Coroutines, DateTime
- Ktor client core
- kotlinx.serialization
- kotlin-logging
- kotlin-inject

**androidMain** (Android-specific):
- Compose UI, Coil, Maps
- Ktor OkHttp engine
- DataStore, WorkManager
- SQLDelight Android driver

**iosMain** (iOS-specific):
- Ktor Darwin engine
- SQLDelight native driver

---

## API Key Setup

### Google Maps API Key

1. **Get API Key**:
   - Go to [Google Cloud Console](https://console.cloud.google.com/google/maps-apis)
   - Create a new project or select existing
   - Enable "Maps SDK for Android"
   - Create credentials → API key
   - Restrict key to Android apps (package: `com.po4yka.trailglass`)

2. **Add to Project**:
   ```bash
   # Copy template
   cp local.defaults.properties local.properties

   # Edit local.properties and add your API key
   MAPS_API_KEY=AIzaSy...your_actual_key_here
   ```

3. **Secrets Plugin** will automatically:
   - Read `local.properties`
   - Generate `BuildConfig.MAPS_API_KEY`
   - Keep secrets out of version control

### Future API Keys

When adding new API keys (geocoding, weather, etc.):

1. Add to `local.defaults.properties` as template
2. Add actual key to `local.properties`
3. Access in code via generated BuildConfig

---

## Dependency Updates

### Checking for Updates

```bash
# Check for dependency updates
./gradlew dependencyUpdates

# View dependency tree
./gradlew :shared:dependencies
./gradlew :composeApp:dependencies
```

### Update Policy

- **Major versions**: Review breaking changes, update documentation
- **Minor versions**: Update when stable, test thoroughly
- **Patch versions**: Update regularly for bug fixes
- **Security patches**: Update immediately

### Compatibility Matrix

| Kotlin | Compose MP | Decompose | Ktor | SQLDelight |
|--------|------------|-----------|------|------------|
| 2.2.21 | 1.9.1      | 3.2.0     | 3.0.1| 2.1.0      |

**Compatibility Notes**:
- Kotlin version must match Compose Multiplatform requirements
- KSP version must match Kotlin version (x.y.z-w format)
- Decompose requires kotlinx.serialization
- Ktor and SQLDelight are independent

---

## Migration Notes

### From Previous Setup

If migrating from earlier TrailGlass versions:

**Added Dependencies**:
- ✅ Coil 2.7.0 (was missing, used but not declared)
- ✅ Google Maps Compose 6.2.0 (was missing)
- ✅ kotlinx.serialization 1.7.3 (needed for Decompose)
- ✅ Ktor Client 3.0.1 (infrastructure for future features)
- ✅ DataStore 1.1.1 (modern preferences)
- ✅ WorkManager 2.10.0 (background tasks)
- ✅ Secrets Plugin 2.0.1 (API key management)

**Breaking Changes**: None - all additions are backwards compatible

---

## Troubleshooting

### Build Issues

**Problem**: "Cannot find Coil/Maps"
```bash
# Solution: Sync Gradle
./gradlew --refresh-dependencies
```

**Problem**: "MAPS_API_KEY not found"
```bash
# Solution: Create local.properties
cp local.defaults.properties local.properties
# Edit and add your API key
```

**Problem**: "KSP version mismatch"
```bash
# Solution: Update KSP to match Kotlin version
# In libs.versions.toml: ksp = "2.2.21-x.y.z"
```

### Runtime Issues

**Problem**: Ktor client crashes
```kotlin
// Solution: Ensure platform engine is included
// Android: implementation(libs.ktor.client.okhttp)
// iOS: implementation(libs.ktor.client.darwin)
```

**Problem**: Maps not displaying
- Check `MAPS_API_KEY` in local.properties
- Verify API key restrictions in Google Cloud Console
- Enable "Maps SDK for Android" in Google Cloud

---

## Resources

### Official Documentation
- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Decompose](https://arkivanov.github.io/Decompose/)
- [Ktor](https://ktor.io/)
- [SQLDelight](https://cashapp.github.io/sqldelight/)
- [Coil](https://coil-kt.github.io/coil/)

### Migration Guides
- [DataStore Migration](https://developer.android.com/topic/libraries/architecture/datastore)
- [Ktor 3.0 Migration](https://ktor.io/docs/migration-to-30.html)
- [kotlinx.serialization Guide](https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/serialization-guide.md)

---

**Related Documentation**:
- [Architecture](ARCHITECTURE.md) - System architecture
- [Development](DEVELOPMENT.md) - Setup and development guide
- [Decompose Navigation](DECOMPOSE_NAVIGATION.md) - Navigation implementation

---

**Last Updated**: 2025-11-17
**Kotlin Version**: 2.2.21
**Gradle Version**: 8.11
