package com.po4yka.trailglass.feature.settings

import app.cash.turbine.test
import com.po4yka.trailglass.domain.error.Result
import com.po4yka.trailglass.domain.error.TrailGlassError
import com.po4yka.trailglass.domain.model.*
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class SettingsControllerTest {

    private val testScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    @Test
    fun `should initialize and load settings`() = runTest {
        val settings = createDefaultSettings()
        val getUseCase = MockGetSettingsUseCase(settings)

        val controller = SettingsController(
            getSettingsUseCase = getUseCase,
            updateSettingsUseCase = MockUpdateSettingsUseCase(),
            exportSettingsUseCase = MockExportSettingsUseCase(),
            scope = testScope
        )

        controller.state.test {
            awaitItem().isLoading shouldBe true

            val loaded = awaitItem()
            loaded.isLoading shouldBe false
            loaded.settings shouldBe settings
        }
    }

    @Test
    fun `updateTrackingPreferences should update settings`() = runTest {
        val settings = createDefaultSettings()
        val getUseCase = MockGetSettingsUseCase(settings)
        val updateUseCase = MockUpdateSettingsUseCase()

        val controller = SettingsController(
            getSettingsUseCase = getUseCase,
            updateSettingsUseCase = updateUseCase,
            exportSettingsUseCase = MockExportSettingsUseCase(),
            scope = testScope
        )

        controller.state.test {
            awaitItem() // Loading
            awaitItem() // Loaded

            val newPrefs = TrackingPreferences(
                isEnabled = true,
                updateIntervalSeconds = 30,
                minimumDistance = 10.0
            )
            controller.updateTrackingPreferences(newPrefs)

            updateUseCase.called shouldBe true
            updateUseCase.lastSettings?.trackingPreferences shouldBe newPrefs
        }
    }

    @Test
    fun `updatePrivacySettings should update settings`() = runTest {
        val settings = createDefaultSettings()
        val updateUseCase = MockUpdateSettingsUseCase()

        val controller = SettingsController(
            getSettingsUseCase = MockGetSettingsUseCase(settings),
            updateSettingsUseCase = updateUseCase,
            exportSettingsUseCase = MockExportSettingsUseCase(),
            scope = testScope
        )

        controller.state.test {
            awaitItem() // Loading
            awaitItem() // Loaded

            val newPrivacy = PrivacySettings(
                shareLocation = false,
                anonymousAnalytics = true
            )
            controller.updatePrivacySettings(newPrivacy)

            updateUseCase.called shouldBe true
            updateUseCase.lastSettings?.privacySettings shouldBe newPrivacy
        }
    }

    @Test
    fun `updateUnitPreferences should update settings`() = runTest {
        val settings = createDefaultSettings()
        val updateUseCase = MockUpdateSettingsUseCase()

        val controller = SettingsController(
            getSettingsUseCase = MockGetSettingsUseCase(settings),
            updateSettingsUseCase = updateUseCase,
            exportSettingsUseCase = MockExportSettingsUseCase(),
            scope = testScope
        )

        controller.state.test {
            awaitItem()
            awaitItem()

            val newUnits = UnitPreferences(
                distanceUnit = DistanceUnit.KILOMETERS,
                temperatureUnit = TemperatureUnit.CELSIUS
            )
            controller.updateUnitPreferences(newUnits)

            updateUseCase.lastSettings?.unitPreferences shouldBe newUnits
        }
    }

    @Test
    fun `resetToDefaults should reset settings`() = runTest {
        val settings = createDefaultSettings()
        val updateUseCase = MockUpdateSettingsUseCase()

        val controller = SettingsController(
            getSettingsUseCase = MockGetSettingsUseCase(settings),
            updateSettingsUseCase = updateUseCase,
            exportSettingsUseCase = MockExportSettingsUseCase(),
            scope = testScope
        )

        controller.state.test {
            awaitItem()
            awaitItem()

            controller.resetToDefaults()

            val loading = awaitItem()
            loading.isLoading shouldBe true

            val reset = awaitItem()
            reset.isLoading shouldBe false
            reset.error shouldBe null

            updateUseCase.resetCalled shouldBe true
        }
    }

    @Test
    fun `resetToDefaults should handle errors`() = runTest {
        val settings = createDefaultSettings()
        val updateUseCase = MockUpdateSettingsUseCase(shouldThrowOnReset = true)

        val controller = SettingsController(
            getSettingsUseCase = MockGetSettingsUseCase(settings),
            updateSettingsUseCase = updateUseCase,
            exportSettingsUseCase = MockExportSettingsUseCase(),
            scope = testScope
        )

        controller.state.test {
            awaitItem()
            awaitItem()

            controller.resetToDefaults()

            awaitItem() // Loading
            val error = awaitItem()
            error.isLoading shouldBe false
            error.error shouldNotBe null
        }
    }

    @Test
    fun `importSettings should handle success`() = runTest {
        val settings = createDefaultSettings()
        val exportUseCase = MockExportSettingsUseCase(importResult = Result.Success(Unit))

        val controller = SettingsController(
            getSettingsUseCase = MockGetSettingsUseCase(settings),
            updateSettingsUseCase = MockUpdateSettingsUseCase(),
            exportSettingsUseCase = exportUseCase,
            scope = testScope
        )

        controller.state.test {
            awaitItem()
            awaitItem()

            controller.importSettings("{}")

            val loading = awaitItem()
            loading.isLoading shouldBe true

            val success = awaitItem()
            success.isLoading shouldBe false
            success.error shouldBe null
        }
    }

    @Test
    fun `importSettings should handle errors`() = runTest {
        val settings = createDefaultSettings()
        val error = TrailGlassError.Validation("Invalid format", "Invalid JSON")
        val exportUseCase = MockExportSettingsUseCase(importResult = Result.Error(error))

        val controller = SettingsController(
            getSettingsUseCase = MockGetSettingsUseCase(settings),
            updateSettingsUseCase = MockUpdateSettingsUseCase(),
            exportSettingsUseCase = exportUseCase,
            scope = testScope
        )

        controller.state.test {
            awaitItem()
            awaitItem()

            controller.importSettings("{}")

            awaitItem() // Loading
            val errorState = awaitItem()
            errorState.isLoading shouldBe false
            errorState.error shouldBe "Invalid JSON"
        }
    }

    @Test
    fun `clearError should clear error state`() = runTest {
        val settings = createDefaultSettings()
        val updateUseCase = MockUpdateSettingsUseCase(shouldThrowOnReset = true)

        val controller = SettingsController(
            getSettingsUseCase = MockGetSettingsUseCase(settings),
            updateSettingsUseCase = updateUseCase,
            exportSettingsUseCase = MockExportSettingsUseCase(),
            scope = testScope
        )

        controller.state.test {
            awaitItem()
            awaitItem()

            controller.resetToDefaults()
            awaitItem() // Loading
            awaitItem().error shouldNotBe null

            controller.clearError()

            val cleared = awaitItem()
            cleared.error shouldBe null
        }
    }

    private fun createDefaultSettings(): AppSettings {
        return AppSettings(
            trackingPreferences = TrackingPreferences(
                isEnabled = false,
                updateIntervalSeconds = 60,
                minimumDistance = 0.0
            ),
            privacySettings = PrivacySettings(
                shareLocation = false,
                anonymousAnalytics = false
            ),
            unitPreferences = UnitPreferences(
                distanceUnit = DistanceUnit.MILES,
                temperatureUnit = TemperatureUnit.FAHRENHEIT
            ),
            appearanceSettings = AppearanceSettings(
                theme = AppTheme.SYSTEM
            ),
            accountSettings = AccountSettings(
                email = "test@example.com",
                displayName = "Test User"
            )
        )
    }
}

class MockGetSettingsUseCase(
    private val settings: AppSettings
) : GetSettingsUseCase {
    private val flow = MutableStateFlow(settings)

    override fun execute(): Flow<AppSettings> = flow
}

class MockUpdateSettingsUseCase(
    private val shouldThrowOnReset: Boolean = false
) : UpdateSettingsUseCase {
    var called = false
    var resetCalled = false
    var lastSettings: AppSettings? = null

    override suspend fun execute(settings: AppSettings) {
        called = true
        lastSettings = settings
    }

    override suspend fun resetToDefaults() {
        resetCalled = true
        if (shouldThrowOnReset) {
            throw Exception("Reset failed")
        }
    }
}

class MockExportSettingsUseCase(
    private val exportJson: String = "{}",
    private val importResult: Result<Unit> = Result.Success(Unit)
) : ExportSettingsUseCase {
    override suspend fun exportSettings(): String = exportJson

    override suspend fun importSettings(json: String): Result<Unit> = importResult
}
