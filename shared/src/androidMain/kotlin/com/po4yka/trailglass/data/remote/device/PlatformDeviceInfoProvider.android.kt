package com.po4yka.trailglass.data.remote.device

import android.content.Context
import android.os.Build
import android.provider.Settings
import com.po4yka.trailglass.data.remote.DeviceInfoProvider
import me.tatarka.inject.annotations.Inject
import java.util.UUID

/**
 * Android implementation of DeviceInfoProvider.
 */
@Inject
actual class PlatformDeviceInfoProvider(private val context: Context) : DeviceInfoProvider {

    private val deviceId: String by lazy {
        // Try to get existing device ID from SharedPreferences
        val prefs = context.getSharedPreferences("device_info", Context.MODE_PRIVATE)
        var id = prefs.getString(KEY_DEVICE_ID, null)

        if (id == null) {
            // Generate new device ID
            id = UUID.randomUUID().toString()
            prefs.edit().putString(KEY_DEVICE_ID, id).apply()
        }

        id
    }

    override fun getDeviceId(): String {
        return deviceId
    }

    override fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        return if (model.startsWith(manufacturer)) {
            model.capitalize()
        } else {
            "${manufacturer.capitalize()} $model"
        }
    }

    override fun getPlatform(): String {
        return "Android"
    }

    override fun getOsVersion(): String {
        return Build.VERSION.RELEASE
    }

    override fun getAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
    }

    private fun String.capitalize(): String {
        return this.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase() else it.toString()
        }
    }

    companion object {
        private const val KEY_DEVICE_ID = "device_id"
    }
}
