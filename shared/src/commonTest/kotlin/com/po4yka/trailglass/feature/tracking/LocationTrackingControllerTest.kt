package com.po4yka.trailglass.feature.tracking

import app.cash.turbine.test
import com.po4yka.trailglass.location.tracking.LocationTracker
import com.po4yka.trailglass.location.tracking.TrackingMode
import com.po4yka.trailglass.location.tracking.TrackingState
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class LocationTrackingControllerTest {

    private val testScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    @Test
    fun `should initialize with default state`() = runTest {
        val tracker = MockLocationTracker()
        val startUseCase = MockStartTrackingUseCase()
        val stopUseCase = MockStopTrackingUseCase()

        val controller = LocationTrackingController(
            locationTracker = tracker,
            startTrackingUseCase = startUseCase,
            stopTrackingUseCase = stopUseCase,
            coroutineScope = testScope
        )

        controller.uiState.test {
            val state = awaitItem()
            state.trackingState shouldBe TrackingState()
            state.isProcessing shouldBe false
            state.error shouldBe null
        }
    }

    @Test
    fun `should update state when tracking state changes`() = runTest {
        val tracker = MockLocationTracker()
        val controller = LocationTrackingController(
            locationTracker = tracker,
            startTrackingUseCase = MockStartTrackingUseCase(),
            stopTrackingUseCase = MockStopTrackingUseCase(),
            coroutineScope = testScope
        )

        controller.uiState.test {
            awaitItem() // Initial state

            // Simulate tracking state change
            tracker.updateTrackingState(TrackingState(isTracking = true))

            val state = awaitItem()
            state.trackingState.isTracking shouldBe true
        }
    }

    @Test
    fun `startTracking should succeed with permissions`() = runTest {
        val tracker = MockLocationTracker(hasPermissions = true)
        val startUseCase = MockStartTrackingUseCase()
        val controller = LocationTrackingController(
            locationTracker = tracker,
            startTrackingUseCase = startUseCase,
            stopTrackingUseCase = MockStopTrackingUseCase(),
            coroutineScope = testScope
        )

        controller.uiState.test {
            awaitItem() // Initial

            controller.startTracking(TrackingMode.ACTIVE)

            awaitItem().isProcessing shouldBe true
            val final = awaitItem()
            final.isProcessing shouldBe false
            final.error shouldBe null

            startUseCase.called shouldBe true
        }
    }

    @Test
    fun `startTracking should handle permission denied`() = runTest {
        val tracker = MockLocationTracker(hasPermissions = false)
        val startUseCase = MockStartTrackingUseCase(
            result = StartTrackingUseCase.Result.PermissionDenied
        )
        val controller = LocationTrackingController(
            locationTracker = tracker,
            startTrackingUseCase = startUseCase,
            stopTrackingUseCase = MockStopTrackingUseCase(),
            coroutineScope = testScope
        )

        controller.uiState.test {
            awaitItem()

            controller.startTracking(TrackingMode.ACTIVE)

            awaitItem() // Processing
            val final = awaitItem()
            final.isProcessing shouldBe false
            final.error shouldNotBe null
            final.hasPermissions shouldBe false
        }
    }

    @Test
    fun `startTracking should handle errors`() = runTest {
        val startUseCase = MockStartTrackingUseCase(
            result = StartTrackingUseCase.Result.Error("Test error")
        )
        val controller = LocationTrackingController(
            locationTracker = MockLocationTracker(),
            startTrackingUseCase = startUseCase,
            stopTrackingUseCase = MockStopTrackingUseCase(),
            coroutineScope = testScope
        )

        controller.uiState.test {
            awaitItem()

            controller.startTracking(TrackingMode.ACTIVE)

            awaitItem() // Processing
            val final = awaitItem()
            final.isProcessing shouldBe false
            final.error shouldBe "Test error"
        }
    }

    @Test
    fun `stopTracking should execute use case`() = runTest {
        val stopUseCase = MockStopTrackingUseCase()
        val controller = LocationTrackingController(
            locationTracker = MockLocationTracker(),
            startTrackingUseCase = MockStartTrackingUseCase(),
            stopTrackingUseCase = stopUseCase,
            coroutineScope = testScope
        )

        controller.uiState.test {
            awaitItem()

            controller.stopTracking()

            awaitItem().isProcessing shouldBe true
            awaitItem().isProcessing shouldBe false

            stopUseCase.called shouldBe true
        }
    }

    @Test
    fun `checkPermissions should update state`() = runTest {
        val tracker = MockLocationTracker(hasPermissions = true)
        val controller = LocationTrackingController(
            locationTracker = tracker,
            startTrackingUseCase = MockStartTrackingUseCase(),
            stopTrackingUseCase = MockStopTrackingUseCase(),
            coroutineScope = testScope
        )

        controller.uiState.test {
            awaitItem()

            controller.checkPermissions()

            val state = awaitItem()
            state.hasPermissions shouldBe true
        }
    }

    @Test
    fun `requestPermissions should update state when granted`() = runTest {
        val tracker = MockLocationTracker(willGrantPermissions = true)
        val controller = LocationTrackingController(
            locationTracker = tracker,
            startTrackingUseCase = MockStartTrackingUseCase(),
            stopTrackingUseCase = MockStopTrackingUseCase(),
            coroutineScope = testScope
        )

        controller.uiState.test {
            awaitItem()

            controller.requestPermissions()

            val state = awaitItem()
            state.hasPermissions shouldBe true
        }
    }

    @Test
    fun `clearError should clear error state`() = runTest {
        val startUseCase = MockStartTrackingUseCase(
            result = StartTrackingUseCase.Result.Error("Test error")
        )
        val controller = LocationTrackingController(
            locationTracker = MockLocationTracker(),
            startTrackingUseCase = startUseCase,
            stopTrackingUseCase = MockStopTrackingUseCase(),
            coroutineScope = testScope
        )

        controller.uiState.test {
            awaitItem()

            controller.startTracking(TrackingMode.ACTIVE)
            awaitItem() // Processing
            awaitItem().error shouldNotBe null

            controller.clearError()

            val state = awaitItem()
            state.error shouldBe null
        }
    }
}

// Mock implementations

class MockLocationTracker(
    private val hasPermissions: Boolean = false,
    private val willGrantPermissions: Boolean = false
) : LocationTracker {
    private val _trackingState = MutableStateFlow(TrackingState())
    override val trackingState = _trackingState

    fun updateTrackingState(state: TrackingState) {
        _trackingState.value = state
    }

    override suspend fun hasPermissions(): Boolean = hasPermissions

    override suspend fun requestPermissions(): Boolean = willGrantPermissions

    override suspend fun startTracking(mode: TrackingMode) {}

    override suspend fun stopTracking() {}

    override suspend fun pauseTracking() {}

    override suspend fun resumeTracking() {}
}

class MockStartTrackingUseCase(
    private val result: StartTrackingUseCase.Result = StartTrackingUseCase.Result.Success
) : StartTrackingUseCase {
    var called = false

    override suspend fun execute(mode: TrackingMode): StartTrackingUseCase.Result {
        called = true
        return result
    }
}

class MockStopTrackingUseCase : StopTrackingUseCase {
    var called = false

    override suspend fun execute() {
        called = true
    }
}
