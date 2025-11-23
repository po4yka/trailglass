package com.po4yka.trailglass.resources

import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc

/**
 * Helper object for accessing Moko-Resources strings in common code.
 *
 * Moko-Resources provides a unified way to access resources (strings, images, fonts, etc.) across all platforms without
 * using expect/actual pattern.
 *
 * Usage examples:
 *
 * In common code:
 * ```kotlin
 * val appName = ResourceHelper.getString(SharedRes.strings.app_name)
 * ```
 *
 * In Compose (Android/Desktop/iOS):
 * ```kotlin
 * Text(text = stringResource(SharedRes.strings.app_name))
 * ```
 *
 * Platform-specific (Android):
 * ```kotlin
 * val text = SharedRes.strings.app_name.desc().toString(context)
 * ```
 *
 * Platform-specific (iOS):
 * ```kotlin
 * let text = SharedRes.strings.app_name.desc().localized()
 * ```
 */
object ResourceHelper {
    /**
     * Get a StringDesc for the given string resource. StringDesc can be converted to platform-specific string using
     * toString(context) or localized().
     *
     * @param resource The string resource to get
     * @return StringDesc that can be converted to platform-specific string
     */
    fun getString(resource: StringResource): StringDesc = resource.desc()

    /**
     * Get a StringDesc with formatted arguments.
     *
     * Note: For string formatting, use string resources with placeholders and pass arguments when converting to string
     * on the platform side.
     *
     * @param resource The string resource
     * @return StringDesc for the resource
     */
    fun getFormattedString(resource: StringResource): StringDesc = resource.desc()
}
