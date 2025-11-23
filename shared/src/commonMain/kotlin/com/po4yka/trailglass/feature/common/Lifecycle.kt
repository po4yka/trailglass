package com.po4yka.trailglass.feature.common

/**
 * Interface for components that require lifecycle management and cleanup.
 *
 * Components implementing this interface have resources that must be released when they are no longer needed (e.g.,
 * coroutines, observers, platform resources).
 *
 * Similar to java.io.Closeable but available in KMP common code.
 */
interface Lifecycle {
    /**
     * Cleanup and release all resources held by this component.
     *
     * This should:
     * - Cancel all running coroutines
     * - Unsubscribe from all flows/observers
     * - Release platform-specific resources
     * - Clear references to prevent memory leaks
     *
     * After cleanup, this component should not be used again.
     */
    fun cleanup()
}
