# Dependency Injection Usage Examples

This document provides practical examples of using kotlin-inject in the TrailGlass project.

## Table of Contents

1. [Android Examples](#android-examples)
2. [iOS Examples](#ios-examples)
3. [Common Patterns](#common-patterns)
4. [Testing](#testing)

---

## Android Examples

### 1. Application Class

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

**AndroidManifest.xml:**
```xml
<application
    android:name=".TrailGlassApplication"
    ...>
    ...
</application>
```

### 2. Activity with DI

```kotlin
class MainActivity : ComponentActivity() {
    private val appComponent by lazy {
        (application as TrailGlassApplication).appComponent
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Access any dependency
        val timelineController = appComponent.timelineController
        val locationRepository = appComponent.locationRepository

        setContent {
            App(appComponent)
        }
    }
}
```

### 3. Composable with DI

```kotlin
@Composable
fun TimelineScreen() {
    val context = LocalContext.current
    val appComponent = remember {
        (context.applicationContext as TrailGlassApplication).appComponent
    }

    val timelineController = appComponent.timelineController
    val timelineState by timelineController.state.collectAsState()

    LaunchedEffect(Unit) {
        val today = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
        timelineController.loadDay(today)
    }

    TimelineContent(
        items = timelineState.items,
        isLoading = timelineState.isLoading,
        error = timelineState.error
    )
}
```

### 4. ViewModel with DI

```kotlin
class TimelineViewModel(
    private val timelineController: TimelineController
) : ViewModel() {

    val state = timelineController.state
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            TimelineController.TimelineState()
        )

    fun loadDay(date: LocalDate) {
        timelineController.loadDay(date)
    }
}

// In Activity
class TimelineActivity : ComponentActivity() {
    private val appComponent by lazy {
        (application as TrailGlassApplication).appComponent
    }

    private val viewModel by viewModels<TimelineViewModel> {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return TimelineViewModel(appComponent.timelineController) as T
            }
        }
    }
}
```

### 5. Service with DI

```kotlin
class LocationTrackingService : Service() {
    private val appComponent by lazy {
        (application as TrailGlassApplication).appComponent
    }

    private val locationTrackingController by lazy {
        appComponent.locationTrackingController
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        lifecycleScope.launch {
            locationTrackingController.startTracking()
        }
        return START_STICKY
    }
}
```

### 6. Repository Usage

```kotlin
class DataSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val appComponent =
        (context.applicationContext as TrailGlassApplication).appComponent

    override suspend fun doWork(): Result {
        val locationRepository = appComponent.locationRepository

        return locationRepository
            .getUnprocessedSamples(appComponent.userId, limit = 100)
            .map { samples ->
                // Process samples
                Result.success()
            }
            .getOrElse {
                Result.retry()
            }
    }
}
```

---

## iOS Examples

### 1. AppDelegate

```swift
import Shared

@main
class AppDelegate: UIResponder, UIApplicationDelegate {
    lazy var appComponent: AppComponent = {
        return CreateKt.createIOSAppComponent()
    }()

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {
        // Component is ready to use
        return true
    }
}
```

### 2. ViewController with DI

```swift
class TimelineViewController: UIViewController {
    private lazy var timelineController: TimelineController = {
        let appDelegate = UIApplication.shared.delegate as! AppDelegate
        return appDelegate.appComponent.timelineController
    }()

    override func viewDidLoad() {
        super.viewDidLoad()

        // Observe state
        observeTimeline()

        // Load data
        let today = Clock.companion.System.now()
            .toLocalDateTime(timeZone: TimeZone.companion.currentSystemDefault())
            .date
        timelineController.loadDay(date: today)
    }

    private func observeTimeline() {
        // Using Combine or async/await to observe StateFlow
        Task {
            for await state in timelineController.state {
                updateUI(with: state)
            }
        }
    }
}
```

### 3. SwiftUI View with DI

```swift
struct TimelineView: View {
    @StateObject private var viewModel: TimelineViewModel

    init() {
        let appDelegate = UIApplication.shared.delegate as! AppDelegate
        let controller = appDelegate.appComponent.timelineController
        _viewModel = StateObject(wrappedValue: TimelineViewModel(controller: controller))
    }

    var body: some View {
        List(viewModel.items) { item in
            TimelineItemRow(item: item)
        }
        .onAppear {
            viewModel.loadToday()
        }
    }
}

class TimelineViewModel: ObservableObject {
    @Published var items: [TimelineItemUI] = []

    private let controller: TimelineController

    init(controller: TimelineController) {
        self.controller = controller
        observeState()
    }

    func observeState() {
        Task {
            for await state in controller.state {
                await MainActor.run {
                    self.items = state.items
                }
            }
        }
    }

    func loadToday() {
        let today = Clock.companion.System.now()
            .toLocalDateTime(timeZone: TimeZone.companion.currentSystemDefault())
            .date
        controller.loadDay(date: today)
    }
}
```

### 4. Background Task with DI

```swift
class LocationProcessingTask: BGProcessingTask {
    private lazy var locationProcessor: LocationProcessor = {
        let appDelegate = UIApplication.shared.delegate as! AppDelegate
        return appDelegate.appComponent.locationProcessor
    }()

    func execute() async throws {
        let repository = getAppComponent().locationRepository

        let result = repository.getUnprocessedSamples(
            userId: getAppComponent().userId,
            limit: 100
        )

        // Process samples
    }

    private func getAppComponent() -> AppComponent {
        let appDelegate = UIApplication.shared.delegate as! AppDelegate
        return appDelegate.appComponent
    }
}
```

---

## Common Patterns

### 1. Accessing DI in Tests

```kotlin
@Test
fun testTimelineController() = runTest {
    // Create test component
    val testPlatformModule = object : PlatformModule {
        override val databaseDriverFactory = TestDatabaseDriverFactory()
        override val applicationScope = TestScope()
        override val userId = "test_user"
        override val deviceId = "test_device"
    }

    val component = AppComponent::class.create(testPlatformModule)

    // Use controller
    val timelineController = component.timelineController
    timelineController.loadDay(LocalDate(2025, 1, 1))

    // Assert state
    assertEquals(false, timelineController.state.value.isLoading)
}
```

### 2. Scoping Dependencies

All dependencies in TrailGlass use `@AppScope`, meaning they're singletons:

```kotlin
// These are the same instance throughout the app
val repo1 = appComponent.locationRepository
val repo2 = appComponent.locationRepository
assert(repo1 === repo2) // true
```

### 3. Lazy vs Eager Initialization

Dependencies are created lazily:

```kotlin
// Component creation doesn't create all dependencies
val component = createAndroidAppComponent(context)

// Dependencies are created on first access
val controller = component.timelineController // Created here

// Subsequent accesses use the same instance
val sameController = component.timelineController // Same instance
```

### 4. Custom Providers

If you need custom configuration:

```kotlin
interface MyModule {
    @AppScope
    @Provides
    fun provideCustomProcessor(
        reverseGeocoder: ReverseGeocoder
    ): PlaceVisitProcessor {
        return PlaceVisitProcessor(
            reverseGeocoder = reverseGeocoder,
            minDurationThreshold = 15.minutes, // Custom
            spatialThresholdMeters = 150.0     // Custom
        )
    }
}
```

---

## Testing

### Unit Test with Mock Component

```kotlin
class TimelineControllerTest {
    private lateinit var component: AppComponent
    private lateinit var timelineController: TimelineController

    @Before
    fun setup() {
        val mockPlatformModule = MockPlatformModule()
        component = AppComponent::class.create(mockPlatformModule)
        timelineController = component.timelineController
    }

    @Test
    fun `load day updates state`() = runTest {
        val date = LocalDate(2025, 1, 1)

        timelineController.loadDay(date)
        advanceUntilIdle()

        assertEquals(date, timelineController.state.value.selectedDate)
        assertEquals(false, timelineController.state.value.isLoading)
    }
}
```

### Integration Test with Real Component

```kotlin
@Test
fun testFullDataFlow() = runTest {
    val component = createTestAppComponent()

    // Insert sample
    val sample = LocationSample(/*...*/)
    component.locationRepository.insertSample(sample)

    // Process
    val result = component.locationProcessor.processLocationData(
        samples = listOf(sample),
        userId = "test"
    )

    // Verify
    assertTrue(result.visits.isNotEmpty())
}
```

---

## Best Practices

1. **Access component once**: Store the reference, don't repeatedly access it
2. **Use lazy initialization**: Let DI handle creation timing
3. **Don't create dependencies manually**: Always use the component
4. **Test with mock components**: Create test-specific modules
5. **Keep scopes minimal**: Only use @AppScope for true singletons

## Troubleshooting

### "lateinit property appComponent has not been initialized"

Make sure your Application class is declared in AndroidManifest.xml:

```xml
<application android:name=".TrailGlassApplication" ...>
```

### "Cannot access applicationContext"

In Compose, use `LocalContext.current.applicationContext`:

```kotlin
val appComponent = remember {
    (LocalContext.current.applicationContext as TrailGlassApplication).appComponent
}
```

### iOS "Thread 1: EXC_BAD_ACCESS"

Ensure you're accessing the component on the main thread:

```swift
Task { @MainActor in
    let component = (UIApplication.shared.delegate as! AppDelegate).appComponent
}
```
