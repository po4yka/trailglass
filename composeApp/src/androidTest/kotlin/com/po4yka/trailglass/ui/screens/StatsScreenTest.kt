package com.po4yka.trailglass.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.po4yka.trailglass.feature.stats.GetStatsUseCase
import com.po4yka.trailglass.feature.stats.StatsController
import com.po4yka.trailglass.feature.stats.StatsPeriod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * UI tests for StatsScreen.
 */
class StatsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var controller: StatsController
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    @Before
    fun setup() {
        // Create a mock controller with test data
        // In a real implementation, you'd use a test repository
        val mockUseCase = object : GetStatsUseCase(null) {
            override suspend fun execute(
                userId: String,
                period: StatsPeriod
            ): GetStatsUseCase.StatsData {
                return GetStatsUseCase.StatsData(
                    totalCountries = 5,
                    totalCities = 12,
                    totalTrips = 3,
                    totalVisits = 25,
                    totalDays = 45,
                    topCountries = listOf(
                        "France" to 8,
                        "Spain" to 6,
                        "Italy" to 5
                    ),
                    topCities = listOf(
                        "Paris" to 4,
                        "Barcelona" to 3,
                        "Rome" to 3
                    )
                )
            }
        }

        controller = StatsController(mockUseCase, scope, "test_user")
    }

    @Test
    fun statsScreen_displaysOverviewCards() {
        // Given
        composeTestRule.setContent {
            StatsScreen(controller = controller)
        }

        // Wait for data to load
        composeTestRule.waitForIdle()

        // Then - verify overview cards are displayed
        composeTestRule.onNodeWithText("Countries").assertExists()
        composeTestRule.onNodeWithText("5").assertExists()

        composeTestRule.onNodeWithText("Cities").assertExists()
        composeTestRule.onNodeWithText("12").assertExists()

        composeTestRule.onNodeWithText("Trips").assertExists()
        composeTestRule.onNodeWithText("3").assertExists()

        composeTestRule.onNodeWithText("Days").assertExists()
        composeTestRule.onNodeWithText("45").assertExists()
    }

    @Test
    fun statsScreen_displaysTopCountries() {
        // Given
        composeTestRule.setContent {
            StatsScreen(controller = controller)
        }

        // Wait for data to load
        composeTestRule.waitForIdle()

        // Then
        composeTestRule.onNodeWithText("Top Countries").assertExists()
        composeTestRule.onNodeWithText("France").assertExists()
        composeTestRule.onNodeWithText("8 visits").assertExists()
        composeTestRule.onNodeWithText("Spain").assertExists()
        composeTestRule.onNodeWithText("Italy").assertExists()
    }

    @Test
    fun statsScreen_displaysTopCities() {
        // Given
        composeTestRule.setContent {
            StatsScreen(controller = controller)
        }

        // Wait for data to load
        composeTestRule.waitForIdle()

        // Then
        composeTestRule.onNodeWithText("Top Cities").assertExists()
        composeTestRule.onNodeWithText("Paris").assertExists()
        composeTestRule.onNodeWithText("4 visits").assertExists()
        composeTestRule.onNodeWithText("Barcelona").assertExists()
        composeTestRule.onNodeWithText("Rome").assertExists()
    }

    @Test
    fun statsScreen_periodFilterWorks() {
        // Given
        composeTestRule.setContent {
            StatsScreen(controller = controller)
        }

        // Wait for data to load
        composeTestRule.waitForIdle()

        // When - click on Month filter
        composeTestRule.onNodeWithText("Month").performClick()

        // Then - screen should still display data
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Countries").assertExists()
    }

    @Test
    fun statsScreen_scrollsToBottomContent() {
        // Given
        composeTestRule.setContent {
            StatsScreen(controller = controller)
        }

        // Wait for data to load
        composeTestRule.waitForIdle()

        // When - scroll to bottom
        composeTestRule.onNodeWithText("Top Cities")
            .performScrollTo()

        // Then - bottom content is visible
        composeTestRule.onNodeWithText("Top Cities").assertIsDisplayed()
    }
}
