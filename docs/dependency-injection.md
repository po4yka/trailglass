# Dependency Injection with kotlin-inject

TrailGlass uses [kotlin-inject](https://github.com/evant/kotlin-inject) for compile-time dependency injection across Android and iOS platforms.

## Overview

kotlin-inject provides:
- **Compile-time safety**: All dependency graphs are validated at compile time
- **Multiplatform support**: Works seamlessly with Kotlin Multiplatform Mobile (KMM)
- **Zero runtime overhead**: No reflection, pure code generation via KSP
- **Type-safe**: Leverages Kotlin's type system for dependency resolution

## Architecture

### Component Hierarchy

```
AppComponent (Application-level)
    ├── DataModule (Repositories)
    ├── LocationModule (Location processing)
    └── PlatformModule (Platform-specific)
        ├── AndroidPlatformModule (Android)
        └── IOSPlatformModule (iOS)
```

### Scopes

- **@AppScope**: Singleton scope, dependencies created once per application instance
  - Repositories
  - Database
  - Location processors
  - Controllers

## Usage

### Android

#### 1. Create the component in your Application class

```kotlin
class TrailGlassApplication : Application() {
    val appComponent: AppComponent by lazy {
        createAndroidAppComponent(applicationContext)
    }

    override fun onCreate() {
        super.onCreate()
        // Component is ready to use
    }
}
```

#### 2. Access dependencies

```kotlin
class MainActivity : ComponentActivity() {
    private val appComponent by lazy {
        (application as TrailGlassApplication).appComponent
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Access controllers
        val timelineController = appComponent.timelineController
        val statsController = appComponent.statsController

        setContent {
            // Use in Composables
        }
    }
}
```

### iOS

#### 1. Create the component in AppDelegate

```swift
import Shared

class AppDelegate: UIResponder, UIApplicationDelegate {
    lazy var appComponent: AppComponent = {
        return CreateKt.createIOSAppComponent()
    }()

    func application(_ application: UIApplication,
                     didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        // Component is ready to use
        return true
    }
}
```

#### 2. Access dependencies

```swift
class TimelineViewController: UIViewController {
    private lazy var timelineController: TimelineController = {
        let appDelegate = UIApplication.shared.delegate as! AppDelegate
        return appDelegate.appComponent.timelineController
    }()

    override func viewDidLoad() {
        super.viewDidLoad()
        // Use controller
    }
}
```

## Adding New Dependencies

### 1. Add @Inject to constructor

```kotlin
@Inject
class MyNewService(
    private val repository: MyRepository,
    private val processor: MyProcessor
) {
    // Implementation
}
```

### 2. Provide the dependency in a module (if needed)

If the class has an interface:

```kotlin
interface MyModule {
    @AppScope
    @Provides
    fun provideMyService(impl: MyNewServiceImpl): MyService = impl
}
```

Then include the module in AppComponent:

```kotlin
@AppScope
@Component
abstract class AppComponent(
    @Component val platformModule: PlatformModule
) : DataModule, LocationModule, MyModule {
    // ...
}
```

### 3. Access via component

```kotlin
val myService = appComponent.myService
```

## Module Organization

### DataModule
Provides all repository implementations:
- LocationRepository
- PlaceVisitRepository
- RouteSegmentRepository
- TripRepository
- PhotoRepository
- GeocodingCacheRepository

### LocationModule
Provides location processing components:
- ReverseGeocoder (platform-specific)
- CachedReverseGeocoder
- PlaceVisitProcessor
- RouteSegmentBuilder
- TripDetector
- TripDayAggregator
- LocationProcessor

### PlatformModule
Provides platform-specific dependencies:
- DatabaseDriverFactory
- CoroutineScope (application-level)
- User ID
- Device ID

## Testing

### Unit Tests

For unit testing, create a test component:

```kotlin
@Component
abstract class TestAppComponent : DataModule, LocationModule {
    @Provides
    fun provideTestDatabase(): Database = createInMemoryDatabase()

    @Provides
    fun provideTestScope(): CoroutineScope = TestScope()

    // Mock other dependencies as needed
}
```

### Integration Tests

Use the real component with test implementations:

```kotlin
@Test
fun testRepositoryIntegration() = runTest {
    val testModule = TestPlatformModule()
    val component = AppComponent::class.create(testModule)

    // Test with real dependencies
    val repository = component.locationRepository
    // ...
}
```

## Best Practices

1. **Prefer constructor injection**: Use @Inject on constructors, not properties
2. **Keep scopes minimal**: Only use @AppScope for true singletons
3. **Separate concerns**: Use modules to group related dependencies
4. **Platform abstraction**: Keep platform-specific code in PlatformModule
5. **Avoid circular dependencies**: Design your dependency graph to be acyclic

## Migration from Manual DI

If you have existing manual DI code:

1. Add @Inject to constructor
2. Remove manual factory methods
3. Add provider function to appropriate module if needed
4. Update creation sites to use component

Example:

```kotlin
// Before
val repository = LocationRepositoryImpl(database)

// After
val repository = appComponent.locationRepository
```

## Troubleshooting

### "Cannot find create function"

Make sure:
1. KSP plugin is applied in build.gradle.kts
2. Project has been built (Run `./gradlew build`)
3. Generated code is in build/generated/ksp/

### "Unsatisfied dependencies"

Check that:
1. All dependencies have @Inject or @Provides
2. Module is included in component
3. Scopes match between provider and consumer

### "Cyclic dependency"

Restructure your classes to break the cycle:
1. Extract interface
2. Use lazy initialization
3. Refactor to remove circular dependency

## Further Reading

- [kotlin-inject Documentation](https://github.com/evant/kotlin-inject)
- [KSP Documentation](https://kotlinlang.org/docs/ksp-overview.html)
- [Dependency Inversion Principle](../architecture-review.md)
