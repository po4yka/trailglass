package com.po4yka.trailglass.data.remote.device

import com.po4yka.trailglass.data.remote.DeviceInfoProvider

/**
 * Expect/actual for platform-specific device info.
 * Actual implementations should implement DeviceInfoProvider interface.
 */
expect class PlatformDeviceInfoProvider : DeviceInfoProvider {
    override fun getDeviceId(): String
    override fun getDeviceName(): String
    override fun getPlatform(): String
    override fun getOsVersion(): String
    override fun getAppVersion(): String
}
