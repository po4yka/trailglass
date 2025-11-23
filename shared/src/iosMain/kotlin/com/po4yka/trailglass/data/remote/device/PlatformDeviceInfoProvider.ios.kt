package com.po4yka.trailglass.data.remote.device

import com.po4yka.trailglass.data.remote.DeviceInfoProvider
import me.tatarka.inject.annotations.Inject
import platform.Foundation.NSBundle
import platform.Foundation.NSUserDefaults
import platform.UIKit.UIDevice
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/** iOS implementation of DeviceInfoProvider. */
@Inject
actual class PlatformDeviceInfoProvider : DeviceInfoProvider {
    private val cachedDeviceId: String by lazy {
        val userDefaults = NSUserDefaults.standardUserDefaults
        var id = userDefaults.stringForKey(KEY_DEVICE_ID)

        if (id == null) {
            @OptIn(ExperimentalUuidApi::class)
            id = Uuid.random().toString()
            userDefaults.setObject(id, KEY_DEVICE_ID)
            userDefaults.synchronize()
        }

        id!!
    }

    actual override fun getDeviceId(): String = cachedDeviceId

    actual override fun getDeviceName(): String = UIDevice.currentDevice.name

    actual override fun getPlatform(): String = "iOS"

    actual override fun getOsVersion(): String = UIDevice.currentDevice.systemVersion

    actual override fun getAppVersion(): String {
        val bundle = NSBundle.mainBundle
        val version = bundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String
        return version ?: "1.0.0"
    }

    companion object {
        private const val KEY_DEVICE_ID = "device_id"
    }
}
