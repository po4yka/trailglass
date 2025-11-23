package com.po4yka.trailglass.util

import java.util.UUID

/** Android implementation of UUID generator. */
actual object UuidGenerator {
    actual fun randomUUID(): String = UUID.randomUUID().toString()
}
