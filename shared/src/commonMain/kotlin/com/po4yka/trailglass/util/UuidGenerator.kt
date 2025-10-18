package com.po4yka.trailglass.util

/**
 * Platform-agnostic UUID generator.
 */
expect object UuidGenerator {
    /**
     * Generate a random UUID string.
     */
    fun randomUUID(): String
}
