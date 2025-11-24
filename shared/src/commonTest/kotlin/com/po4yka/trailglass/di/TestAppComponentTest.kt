package com.po4yka.trailglass.di

import com.po4yka.trailglass.domain.model.LocationSample
import com.po4yka.trailglass.domain.model.LocationSource
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestAppComponentTest {
    @Test
    fun `createTestAppComponent provides working repositories`() =
        runTest {
            val deviceId = "component-device"
            val component = createTestAppComponent(userId = "component-user", deviceId = deviceId)
            val sample =
                LocationSample(
                    id = "sample-component",
                    timestamp = Clock.System.now(),
                    latitude = 0.0,
                    longitude = 0.0,
                    accuracy = 1.0,
                    source = LocationSource.GPS,
                    deviceId = deviceId,
                    userId = component.userId
                )

            val insertResult = component.locationRepository.insertSample(sample)
            assertTrue(insertResult.isSuccess(), "Sample should be inserted successfully")

            val retrieved = component.locationRepository.getSampleById(sample.id).getOrNull()
            assertEquals(sample.id, retrieved?.id)
            assertEquals(component.userId, retrieved?.userId)
        }
}
