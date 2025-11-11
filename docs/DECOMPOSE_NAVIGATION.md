# Decompose Navigation Integration

## Overview

TrailGlass uses **Decompose 3.2.0** as its navigation library, providing lifecycle-aware navigation with state preservation, type-safe routing, and deep linking support throughout the application.

## Architecture

### Component Hierarchy

```
MainActivity (ComponentActivity)
    └── RootComponent (DefaultRootComponent)
        ├── StatsComponent
        │   └── StatsScreen
        ├── TimelineComponent
        │   └── TimelineScreen
        ├── MapComponent
        │   └── MapScreen
        └── SettingsComponent
            └── SettingsScreen
```

### Files Structure

```
composeApp/src/androidMain/kotlin/com/po4yka/trailglass/
├── MainActivity.kt                          # Activity with Decompose lifecycle integration
├── App.kt                                   # Root composable receiving RootComponent
└── ui/navigation/
    ├── RootComponent.kt                     # Main navigation component with child stack
    ├── Navigation.kt                        # Bottom navigation scaffold with Children API
    ├── StatsComponent.kt                    # Stats screen component wrapper
    ├── TimelineComponent.kt                 # Timeline screen component wrapper
    ├── MapComponent.kt                      # Map screen component wrapper
    └── SettingsComponent.kt                 # Settings screen component wrapper
```

## Implementation Details

### 1. Root Component (RootComponent.kt)

**Purpose**: Central navigation coordinator managing the app's navigation state.

**Key Features**:
- Uses `StackNavigation` with serializable configs
- Manages child stack with lifecycle-aware components
- Handles deep link navigation
- Integrates with kotlin-inject DI for controller injection

**Navigation Configs**:
```kotlin
@Serializable
sealed interface Config {
    @Serializable data object Stats : Config
    @Serializable data object Timeline : Config
    @Serializable data object Map : Config
    @Serializable data object Settings : Config
}
```

**Navigation Methods**:
- `navigateToScreen(config: Config)` - Navigate to a specific screen (replaces current)
- `handleDeepLink(path: String)` - Parse and navigate from deep link URLs

### 2. Child Components

Each screen has a dedicated component that:
- Implements `ComponentContext` for lifecycle integration
- Holds reference to the screen's controller
- Provides clean separation between navigation and business logic

**Example** (StatsComponent.kt):
```kotlin
interface StatsComponent {
    val statsController: StatsController
}

class DefaultStatsComponent(
    componentContext: ComponentContext,
    override val statsController: StatsController
) : StatsComponent, ComponentContext by componentContext
```

### 3. Main Scaffold (Navigation.kt)

**Purpose**: UI layer rendering the navigation structure.

**Implementation**:
- Uses `Children` API from Decompose extensions
- Subscribes to child stack changes with `subscribeAsState()`
- Renders active child component based on navigation state
- Bottom navigation bar with 4 tabs

**Bottom Navigation Tabs**:
1. **Stats** (BarChart icon) - Travel statistics and analytics
2. **Timeline** (ViewTimeline icon) - Daily timeline with visits/routes
3. **Map** (Map icon) - Interactive map view
4. **Settings** (Settings icon) - Tracking controls and permissions

### 4. MainActivity Integration

**Lifecycle Integration**:
```kotlin
val rootComponent: RootComponent = DefaultRootComponent(
    componentContext = defaultComponentContext(),
    appComponent = appComponent
)
```

**Deep Link Handling**:
- `onCreate()` - Handles initial deep link from launch intent
- `onNewIntent()` - Handles deep links when app is already running
- Extracts path from URI and delegates to RootComponent

### 5. App Entry Point (App.kt)

**Composable Function**:
```kotlin
@Composable
fun App(rootComponent: RootComponent) {
    TrailGlassTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            MainScaffold(
                rootComponent = rootComponent,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
```

## Deep Linking

### Supported URL Schemes

#### Custom Scheme (trailglass://)
- `trailglass://app/stats` → Stats screen
- `trailglass://app/timeline` → Timeline screen
- `trailglass://app/map` → Map screen
- `trailglass://app/settings` → Settings screen

#### HTTPS Scheme (https://trailglass.app)
- `https://trailglass.app/stats` → Stats screen
- `https://trailglass.app/timeline` → Timeline screen
- `https://trailglass.app/map` → Map screen
- `https://trailglass.app/settings` → Settings screen

### AndroidManifest Configuration

```xml
<!-- Deep linking support -->
<intent-filter android:autoVerify="true">
    <action android:name="android.intent.action.VIEW" />
    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />

    <!-- trailglass:// scheme -->
    <data android:scheme="trailglass" android:host="app" />
</intent-filter>

<!-- HTTPS deep links for web integration -->
<intent-filter android:autoVerify="true">
    <action android:name="android.intent.action.VIEW" />
    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />

    <!-- https://trailglass.app deep links -->
    <data android:scheme="https" android:host="trailglass.app" />
</intent-filter>
```

### Testing Deep Links

**ADB Command**:
```bash
# Test custom scheme
adb shell am start -W -a android.intent.action.VIEW -d "trailglass://app/timeline" com.po4yka.trailglass

# Test HTTPS scheme
adb shell am start -W -a android.intent.action.VIEW -d "https://trailglass.app/map" com.po4yka.trailglass
```

## State Preservation

### Automatic State Preservation

Decompose automatically preserves component state across:
- Configuration changes (rotation, theme change, etc.)
- Process death and recreation
- Back stack navigation

### How It Works

1. **ComponentContext** provides state preservation via `stateKeeper`
2. Each component's state is serialized using kotlinx.serialization
3. State is restored when component is recreated
4. Controllers maintain StateFlow which survives configuration changes

### Example

```kotlin
// State is preserved across configuration changes
@Composable
fun StatsScreen(controller: StatsController) {
    val state by controller.state.collectAsState()

    // State is automatically restored after rotation
    LaunchedEffect(Unit) {
        if (state.stats == null && !state.isLoading) {
            controller.loadPeriod(Period.Year(currentYear))
        }
    }
}
```

## Navigation Flow

### Bottom Navigation Flow

```
User clicks bottom nav item
    ↓
rootComponent.navigateToScreen(config)
    ↓
navigation.replaceAll(config)
    ↓
childStack updates with new active child
    ↓
UI recomposes via subscribeAsState()
    ↓
Children renders new screen
```

### Deep Link Flow

```
App receives Intent with deep link
    ↓
MainActivity.handleIntent(intent)
    ↓
Extract path from intent.data
    ↓
rootComponent.handleDeepLink(path)
    ↓
parseDeepLink(path) → Config
    ↓
navigateToScreen(config)
    ↓
Screen navigates as normal
```

## Dependency Injection Integration

### Controller Injection

Components receive controllers via kotlin-inject DI:

```kotlin
private fun createChild(
    config: RootComponent.Config,
    componentContext: ComponentContext
): RootComponent.Child = when (config) {
    is Config.Stats -> Child.Stats(
        component = DefaultStatsComponent(
            componentContext = componentContext,
            statsController = appComponent.statsController  // DI injection
        )
    )
    // ... other screens
}
```

### Scoping

- **AppComponent**: Singleton scope at application level
- **Controllers**: Singleton within AppComponent
- **Components**: Created per navigation, share controller instance

## Benefits

### ✅ Lifecycle Awareness
- Components respect Android lifecycle
- Automatic cleanup when component is destroyed
- No memory leaks from leaked subscriptions

### ✅ State Preservation
- Survives configuration changes (rotation, theme, etc.)
- Survives process death
- Automatic serialization/deserialization

### ✅ Type Safety
- Compile-time type checking for navigation
- No string-based routes
- Refactoring-friendly with sealed classes

### ✅ Deep Linking
- Built-in support for custom and HTTPS schemes
- Easy to extend with new routes
- Automatic navigation from external sources

### ✅ Testing
- Components are easily testable in isolation
- Navigation logic separated from UI
- Mock components for testing flows

### ✅ Performance
- Lightweight library (~300KB)
- No runtime reflection
- Efficient state management

## Comparison with Alternatives

### vs Navigation Compose

| Feature | Decompose | Navigation Compose |
|---------|-----------|-------------------|
| State preservation | ✅ Automatic | ⚠️ Manual with SavedStateHandle |
| Lifecycle awareness | ✅ Built-in | ⚠️ Requires ViewModel |
| Multiplatform | ✅ Yes (iOS, Desktop, Web) | ❌ Android only |
| Deep linking | ✅ Built-in | ✅ Built-in |
| Type safety | ✅ Sealed classes | ⚠️ String routes (or type-safe navigation) |
| Size | ✅ Lightweight | ⚠️ Heavier (part of Jetpack) |

### vs Voyager

| Feature | Decompose | Voyager |
|---------|-----------|---------|
| State preservation | ✅ Automatic | ✅ Automatic |
| Lifecycle awareness | ✅ Built-in | ✅ Built-in |
| Maturity | ✅ Stable, widely used | ⚠️ Newer library |
| Documentation | ✅ Comprehensive | ⚠️ Less comprehensive |
| Community | ✅ Large | ⚠️ Smaller |
| Architecture | ✅ Component-based | ✅ Screen-based |

## Future Enhancements

### Potential Additions

1. **Nested Navigation**
   - Detail screens with their own navigation stack
   - Modal dialogs as navigation destinations

2. **Animations**
   - Custom transition animations between screens
   - Shared element transitions

3. **Bottom Sheet Navigation**
   - Bottom sheet as navigation destination
   - Persistent bottom sheets

4. **Deep Link Parameters**
   - Parse query parameters from deep links
   - Navigate to specific items (e.g., visit ID, trip ID)

5. **Programmatic Navigation**
   - Navigate from outside composables
   - Navigate from background services

## Resources

- **Decompose GitHub**: https://github.com/arkivanov/Decompose
- **Decompose Documentation**: https://arkivanov.github.io/Decompose/
- **Sample Apps**: https://github.com/arkivanov/Decompose/tree/master/sample

## Related Documentation

- [UI Implementation](UI_IMPLEMENTATION.md) - UI screens and components
- [Architecture](ARCHITECTURE.md) - Overall system architecture
- [Dependency Injection](dependency-injection.md) - kotlin-inject DI setup
- [Testing](TESTING.md) - Testing strategy and examples

---

**Status**: ✅ Fully implemented and documented
**Version**: Decompose 3.2.0
**Last Updated**: 2025-11-17
