# Diagnostics and Logging

This module provides in-app diagnostics and logging capabilities for TrailGlass.

## Components

### LogBuffer

Located at: `shared/commonMain/kotlin/logging/LogBuffer.kt`

Thread-safe in-memory circular buffer that stores up to 500 log entries.

**Features:**
- Stores log entries with timestamp, level, tag, and message
- Exposed as StateFlow for reactive UI updates
- Thread-safe access using Mutex
- Export logs as formatted text

**Usage:**
```kotlin
// Add a log entry
LogBuffer.add(LogLevel.INFO, "MyTag", "My message")

// Observe entries
LogBuffer.entries.collect { entries ->
    // Update UI
}

// Clear logs
LogBuffer.clear()

// Export as text
val exported = LogBuffer.export()
```

### LogBufferAppender

Located at: `shared/commonMain/kotlin/logging/LogBufferAppender.kt`

Extension functions to integrate LogBuffer with kotlin-logging.

**Usage:**
```kotlin
private val logger = logger()

// Log to both console and LogBuffer
logger.logToBuffer(LogLevel.INFO, "MyTag") { "Message" }
```

### DiagnosticsController

Located at: `shared/commonMain/kotlin/feature/diagnostics/DiagnosticsController.kt`

Collects and exposes diagnostic information about the app state.

**Provides:**
- Location tracking status (mode, accuracy, permissions)
- Database statistics (counts, size)
- Sync status (last sync, pending items, errors)
- System information (OS, device, battery, network)
- Permissions status

**Usage:**
```kotlin
@Inject
class MyController(
    private val diagnosticsController: DiagnosticsController
) {
    init {
        diagnosticsController.state.collect { state ->
            // Update UI with diagnostics
        }
    }

    fun refresh() {
        diagnosticsController.refreshAll()
    }

    fun export() {
        val report = diagnosticsController.exportDiagnostics()
        // Share report
    }
}
```

### PlatformDiagnostics

Platform-specific implementations that provide system information.

**Android:** `shared/androidMain/kotlin/feature/diagnostics/PlatformDiagnostics.android.kt`
**iOS:** `shared/iosMain/kotlin/feature/diagnostics/PlatformDiagnostics.ios.kt`

Provides:
- System info (app version, build number, OS, device)
- Battery info (level, optimization, low power mode)
- Permissions status
- Location info (accuracy, satellites)
- Database size

## UI Components

### Android

**LogViewerScreen** (`composeApp/src/main/kotlin/ui/screens/LogViewerScreen.kt`)
- Filter logs by level (All, DEBUG, INFO, WARNING, ERROR)
- Color-coded entries
- Auto-scroll to latest
- Export and share logs
- Clear logs with confirmation

**DiagnosticsScreen** (`composeApp/src/main/kotlin/ui/screens/DiagnosticsScreen.kt`)
- Cards for each diagnostic section
- Real-time updates
- Export diagnostics
- Refresh button

### iOS

**LogViewerView** (`iosApp/iosApp/Screens/LogViewerView.swift`)
- Same features as Android
- Native SwiftUI implementation

**DiagnosticsView** (`iosApp/iosApp/Screens/DiagnosticsView.swift`)
- Same features as Android
- Native SwiftUI implementation

## Navigation

Both screens are accessible from the Settings screen under the "Developer" section.

## Integration with DI

The DiagnosticsController is provided by the AppComponent:

```kotlin
abstract val diagnosticsController: DiagnosticsController
```

PlatformDiagnostics is provided by the PlatformModule:

```kotlin
fun platformDiagnostics(): PlatformDiagnostics
```

## Future Enhancements

1. Add search functionality to log viewer
2. Add log level statistics
3. Add ability to filter logs by tag
4. Add log persistence (save to file)
5. Add crash log collection
6. Add performance metrics to diagnostics
