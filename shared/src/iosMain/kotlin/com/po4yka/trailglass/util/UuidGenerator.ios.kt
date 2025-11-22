package com.po4yka.trailglass.util

import platform.Foundation.NSUUID

/**
 * iOS implementation of UUID generator.
 */
actual object UuidGenerator {
    actual fun randomUUID(): String = NSUUID().UUIDString()
}
