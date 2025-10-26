package com.po4yka.trailglass.feature.permission

import app.cash.turbine.test
import com.po4yka.trailglass.domain.permission.*
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class PermissionFlowControllerTest {

    private val testScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    @Test
    fun `should initialize with empty state`() = runTest {
        val controller = createController()

        controller.state.test {
            val state = awaitItem()
            state.currentRequest shouldBe null
            state.isRequesting shouldBe false
            state.showRationaleDialog shouldBe false
            state.showDeniedDialog shouldBe false
            state.showPermanentlyDeniedDialog shouldBe false
        }
    }

    @Test
    fun `startPermissionFlow should complete immediately when already granted`() = runTest {
        val manager = MockPermissionManager(
            initialState = PermissionState.Granted
        )
        val controller = createController(permissionManager = manager)

        controller.state.test {
            awaitItem() // Initial

            controller.startPermissionFlow(PermissionType.LOCATION_FINE)

            val result = awaitItem()
            result.lastResult.shouldBeInstanceOf<PermissionResult.Granted>()
            result.currentRequest shouldBe null
        }
    }

    @Test
    fun `startPermissionFlow should show rationale when permission was previously denied`() = runTest {
        val manager = MockPermissionManager(
            initialState = PermissionState.Denied,
            shouldShowRationale = true
        )
        val controller = createController(permissionManager = manager)

        controller.state.test {
            awaitItem()

            controller.startPermissionFlow(PermissionType.LOCATION_FINE)

            val state = awaitItem()
            state.showRationaleDialog shouldBe true
            state.currentRequest?.permissionType shouldBe PermissionType.LOCATION_FINE
            state.currentRequest?.shouldShowRationale shouldBe true
        }
    }

    @Test
    fun `startPermissionFlow should show settings dialog when permanently denied`() = runTest {
        val manager = MockPermissionManager(
            initialState = PermissionState.PermanentlyDenied
        )
        val controller = createController(permissionManager = manager)

        controller.state.test {
            awaitItem()

            controller.startPermissionFlow(PermissionType.LOCATION_FINE)

            val state = awaitItem()
            state.showPermanentlyDeniedDialog shouldBe true
            state.settingsInstructions shouldBe null // Will be set
            state.currentRequest?.canRequest shouldBe false
        }
    }

    @Test
    fun `onRationaleAccepted should request permission`() = runTest {
        val manager = MockPermissionManager(
            initialState = PermissionState.Denied,
            shouldShowRationale = true,
            requestResult = PermissionResult.Granted
        )
        val controller = createController(permissionManager = manager)

        controller.state.test {
            awaitItem()

            controller.startPermissionFlow(PermissionType.LOCATION_FINE)
            awaitItem() // Rationale shown

            controller.onRationaleAccepted()

            awaitItem().showRationaleDialog shouldBe false
            val requesting = awaitItem()
            requesting.isRequesting shouldBe true

            val completed = awaitItem()
            completed.isRequesting shouldBe false
            completed.lastResult.shouldBeInstanceOf<PermissionResult.Granted>()
        }
    }

    @Test
    fun `onRationaleDenied should cancel flow`() = runTest {
        val manager = MockPermissionManager(
            initialState = PermissionState.Denied,
            shouldShowRationale = true
        )
        val controller = createController(permissionManager = manager)

        controller.state.test {
            awaitItem()

            controller.startPermissionFlow(PermissionType.LOCATION_FINE)
            awaitItem() // Rationale shown

            controller.onRationaleDenied()

            val state = awaitItem()
            state.showRationaleDialog shouldBe false
            state.lastResult.shouldBeInstanceOf<PermissionResult.Cancelled>()
            state.currentRequest shouldBe null
        }
    }

    @Test
    fun `requestPermission should show denied dialog on denial`() = runTest {
        val manager = MockPermissionManager(
            initialState = PermissionState.NotDetermined,
            requestResult = PermissionResult.Denied
        )
        val controller = createController(permissionManager = manager)

        controller.state.test {
            awaitItem()

            controller.startPermissionFlow(PermissionType.CAMERA)

            awaitItem() // Requesting
            val denied = awaitItem()
            denied.isRequesting shouldBe false
            denied.showDeniedDialog shouldBe true
            denied.lastResult.shouldBeInstanceOf<PermissionResult.Denied>()
        }
    }

    @Test
    fun `onRetryPermission should restart flow`() = runTest {
        val manager = MockPermissionManager(
            initialState = PermissionState.NotDetermined,
            requestResult = PermissionResult.Denied
        )
        val controller = createController(permissionManager = manager)

        controller.state.test {
            awaitItem()

            controller.startPermissionFlow(PermissionType.CAMERA)
            awaitItem() // Requesting
            awaitItem() // Denied dialog shown

            controller.onRetryPermission()

            val dismissed = awaitItem()
            dismissed.showDeniedDialog shouldBe false

            // Should restart the flow
            awaitItem() // Requesting again
        }
    }

    @Test
    fun `onOpenSettingsClicked should call openAppSettings`() = runTest {
        val manager = MockPermissionManager(
            initialState = PermissionState.PermanentlyDenied
        )
        val controller = createController(permissionManager = manager)

        controller.state.test {
            awaitItem()

            controller.startPermissionFlow(PermissionType.LOCATION_FINE)
            awaitItem() // Settings dialog shown

            controller.onOpenSettingsClicked()

            val state = awaitItem()
            state.showPermanentlyDeniedDialog shouldBe false
            state.currentRequest shouldBe null

            manager.openSettingsCalled shouldBe true
        }
    }

    @Test
    fun `onContinueWithoutPermission should dismiss dialogs`() = runTest {
        val manager = MockPermissionManager(
            initialState = PermissionState.Denied,
            shouldShowRationale = true
        )
        val controller = createController(permissionManager = manager)

        controller.state.test {
            awaitItem()

            controller.startPermissionFlow(PermissionType.PHOTO_LIBRARY)
            awaitItem() // Rationale shown

            controller.onContinueWithoutPermission()

            val state = awaitItem()
            state.showRationaleDialog shouldBe false
            state.showDeniedDialog shouldBe false
            state.showPermanentlyDeniedDialog shouldBe false
            state.lastResult.shouldBeInstanceOf<PermissionResult.Denied>()
        }
    }

    @Test
    fun `isPermissionGranted should return correct state`() = runTest {
        val manager = MockPermissionManager(
            initialState = PermissionState.Granted
        )
        val controller = createController(permissionManager = manager)

        val isGranted = controller.isPermissionGranted(PermissionType.LOCATION_FINE)
        isGranted shouldBe true
    }

    @Test
    fun `getPermissionState should return current state`() = runTest {
        val manager = MockPermissionManager(
            initialState = PermissionState.Denied
        )
        val controller = createController(permissionManager = manager)

        val state = controller.getPermissionState(PermissionType.CAMERA)
        state.shouldBeInstanceOf<PermissionState.Denied>()
    }

    @Test
    fun `clearError should clear error state`() = runTest {
        val manager = MockPermissionManager(
            shouldThrow = true
        )
        val controller = createController(permissionManager = manager)

        controller.state.test {
            awaitItem()

            controller.startPermissionFlow(PermissionType.LOCATION_FINE)

            val error = awaitItem()
            error.error shouldBe null // Will be set

            controller.clearError()

            val cleared = awaitItem()
            cleared.error shouldBe null
        }
    }

    @Test
    fun `permission flow should handle all permission types`() = runTest {
        val permissionTypes = listOf(
            PermissionType.LOCATION_FINE,
            PermissionType.LOCATION_COARSE,
            PermissionType.LOCATION_BACKGROUND,
            PermissionType.CAMERA,
            PermissionType.PHOTO_LIBRARY,
            PermissionType.NOTIFICATIONS
        )

        val rationaleProvider = PermissionRationaleProvider()

        permissionTypes.forEach { permissionType ->
            val rationale = rationaleProvider.getRationale(permissionType)
            rationale.permissionType shouldBe permissionType
            rationale.title.isNotEmpty() shouldBe true
            rationale.description.isNotEmpty() shouldBe true
        }
    }

    @Test
    fun `rationale provider should provide settings instructions`() = runTest {
        val provider = PermissionRationaleProvider()

        val instructions = provider.getSettingsInstructions(PermissionType.LOCATION_FINE)

        instructions.permissionType shouldBe PermissionType.LOCATION_FINE
        instructions.steps.isNotEmpty() shouldBe true
        instructions.quickAction.isNotEmpty() shouldBe true
    }

    private fun createController(
        permissionManager: PermissionManager = MockPermissionManager(),
        rationaleProvider: PermissionRationaleProvider = PermissionRationaleProvider()
    ): PermissionFlowController {
        return PermissionFlowController(
            permissionManager = permissionManager,
            rationaleProvider = rationaleProvider,
            coroutineScope = testScope
        )
    }
}

// Mock PermissionManager for testing
class MockPermissionManager(
    private val initialState: PermissionState = PermissionState.NotDetermined,
    private val shouldShowRationale: Boolean = false,
    private val requestResult: PermissionResult = PermissionResult.Granted,
    private val shouldThrow: Boolean = false
) : PermissionManager() {

    var openSettingsCalled = false

    override suspend fun checkPermission(permissionType: PermissionType): PermissionState {
        if (shouldThrow) throw Exception("Test error")
        return initialState
    }

    override suspend fun requestPermission(permissionType: PermissionType): PermissionResult {
        if (shouldThrow) throw Exception("Test error")
        return requestResult
    }

    override suspend fun shouldShowRationale(permissionType: PermissionType): Boolean {
        return shouldShowRationale
    }

    override suspend fun openAppSettings() {
        openSettingsCalled = true
    }

    override fun observePermission(permissionType: PermissionType): StateFlow<PermissionState> {
        return MutableStateFlow(initialState)
    }
}
