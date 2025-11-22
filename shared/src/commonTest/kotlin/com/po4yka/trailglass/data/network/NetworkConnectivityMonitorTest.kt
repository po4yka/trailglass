package com.po4yka.trailglass.data.network

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class NetworkConnectivityMonitorTest {
    @Test
    fun `initial state should be connected`() =
        runTest {
            val monitor = MockNetworkConnectivityMonitor()

            monitor.isConnected() shouldBe true
            monitor.networkState.value.shouldBeInstanceOf<NetworkState.Connected>()
        }

    @Test
    fun `should detect disconnection`() =
        runTest {
            val monitor = MockNetworkConnectivityMonitor()

            monitor.networkState.test {
                // Initial state
                awaitItem().shouldBeInstanceOf<NetworkState.Connected>()

                // Simulate disconnect
                monitor.simulateDisconnect()

                // Should emit disconnected state
                awaitItem().shouldBeInstanceOf<NetworkState.Disconnected>()
                monitor.isConnected() shouldBe false
            }
        }

    @Test
    fun `should detect reconnection`() =
        runTest {
            val monitor = MockNetworkConnectivityMonitor()

            monitor.networkState.test {
                awaitItem() // Initial connected

                // Disconnect
                monitor.simulateDisconnect()
                awaitItem().shouldBeInstanceOf<NetworkState.Disconnected>()

                // Reconnect
                monitor.simulateConnect()
                awaitItem().shouldBeInstanceOf<NetworkState.Connected>()
                monitor.isConnected() shouldBe true
            }
        }

    @Test
    fun `should detect network type changes`() =
        runTest {
            val monitor = MockNetworkConnectivityMonitor()

            monitor.networkInfo.test {
                // Initial WiFi
                awaitItem().type shouldBe NetworkType.WIFI

                // Switch to cellular
                monitor.setNetworkType(NetworkType.CELLULAR)
                awaitItem().type shouldBe NetworkType.CELLULAR

                // Switch to ethernet
                monitor.setNetworkType(NetworkType.ETHERNET)
                awaitItem().type shouldBe NetworkType.ETHERNET
            }
        }

    @Test
    fun `should detect metered connection`() =
        runTest {
            val monitor = MockNetworkConnectivityMonitor()

            monitor.networkInfo.test {
                // Initial not metered
                awaitItem().isMetered shouldBe false

                // Switch to metered
                monitor.setMetered(true)
                awaitItem().isMetered shouldBe true
                monitor.isMetered() shouldBe true
            }
        }

    @Test
    fun `should detect limited connectivity`() =
        runTest {
            val monitor = MockNetworkConnectivityMonitor()

            monitor.networkState.test {
                awaitItem() // Initial connected

                // Simulate limited connection
                monitor.simulateLimitedConnection("No internet access")
                val limited = awaitItem()
                limited.shouldBeInstanceOf<NetworkState.Limited>()
                limited.reason shouldBe "No internet access"
            }
        }

    @Test
    fun `allowsSync extension should work correctly`() {
        NetworkState.Connected.allowsSync() shouldBe true
        NetworkState.Disconnected.allowsSync() shouldBe false
        NetworkState.Limited("test").allowsSync() shouldBe false
    }

    @Test
    fun `monitoring lifecycle should work`() {
        val monitor = MockNetworkConnectivityMonitor()

        monitor.isMonitoring() shouldBe false

        monitor.startMonitoring()
        monitor.isMonitoring() shouldBe true

        monitor.stopMonitoring()
        monitor.isMonitoring() shouldBe false
    }

    @Test
    fun `cellular connection should be metered by default`() =
        runTest {
            val monitor = MockNetworkConnectivityMonitor()

            monitor.simulateConnect(type = NetworkType.CELLULAR, isMetered = true)

            monitor.networkInfo.value.type shouldBe NetworkType.CELLULAR
            monitor.networkInfo.value.isMetered shouldBe true
            monitor.isMetered() shouldBe true
        }

    @Test
    fun `wifi connection should not be metered`() =
        runTest {
            val monitor = MockNetworkConnectivityMonitor()

            monitor.simulateConnect(type = NetworkType.WIFI, isMetered = false)

            monitor.networkInfo.value.type shouldBe NetworkType.WIFI
            monitor.networkInfo.value.isMetered shouldBe false
            monitor.isMetered() shouldBe false
        }
}
