package com.po4yka.trailglass.data.network

import com.po4yka.trailglass.domain.error.Result
import com.po4yka.trailglass.domain.error.TrailGlassError
import com.po4yka.trailglass.logging.logger
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.min
import kotlin.math.pow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Retry policy configuration for failed operations.
 */
data class RetryPolicy(
    /**
     * Maximum number of retry attempts.
     */
    val maxAttempts: Int = 3,

    /**
     * Initial delay before first retry.
     */
    val initialDelay: Duration = 1.seconds,

    /**
     * Maximum delay between retries.
     */
    val maxDelay: Duration = 30.seconds,

    /**
     * Multiplier for exponential backoff.
     */
    val multiplier: Double = 2.0,

    /**
     * Whether to retry on all errors or only specific types.
     */
    val retryOnAllErrors: Boolean = false,

    /**
     * Custom predicate to determine if an error should be retried.
     */
    val shouldRetry: ((TrailGlassError) -> Boolean)? = null
) {
    companion object {
        /**
         * Default retry policy with 3 attempts and exponential backoff.
         */
        val DEFAULT = RetryPolicy()

        /**
         * Aggressive retry policy for critical operations.
         */
        val AGGRESSIVE = RetryPolicy(
            maxAttempts = 5,
            initialDelay = 500.milliseconds,
            multiplier = 1.5
        )

        /**
         * Conservative retry policy for non-critical operations.
         */
        val CONSERVATIVE = RetryPolicy(
            maxAttempts = 2,
            initialDelay = 2.seconds,
            multiplier = 2.0
        )

        /**
         * No retry policy.
         */
        val NONE = RetryPolicy(maxAttempts = 0)

        /**
         * Network-specific retry policy.
         */
        val NETWORK = RetryPolicy(
            maxAttempts = 3,
            initialDelay = 2.seconds,
            shouldRetry = { error ->
                error is TrailGlassError.NetworkError.Timeout ||
                error is TrailGlassError.NetworkError.RequestFailed ||
                error is TrailGlassError.NetworkError.ServerError
            }
        )
    }

    /**
     * Calculate delay for a specific attempt.
     */
    fun getDelay(attempt: Int): Duration {
        val exponentialDelay = (initialDelay.inWholeMilliseconds *
                multiplier.pow(attempt.toDouble())).toLong().milliseconds
        return min(exponentialDelay.inWholeMilliseconds, maxDelay.inWholeMilliseconds).milliseconds
    }

    /**
     * Determine if an error should be retried.
     */
    fun shouldRetry(error: TrailGlassError): Boolean {
        return when {
            shouldRetry != null -> shouldRetry.invoke(error)
            retryOnAllErrors -> true
            else -> error.isRetryable()
        }
    }
}

/**
 * Retry state for tracking retry progress.
 */
data class RetryState(
    val attempt: Int = 0,
    val lastError: TrailGlassError? = null,
    val isRetrying: Boolean = false
)

/**
 * Execute an operation with retry logic.
 *
 * @param policy Retry policy to use
 * @param onRetry Optional callback invoked before each retry attempt
 * @param block Operation to execute
 * @return Result of the operation
 */
suspend fun <T> retryWithPolicy(
    policy: RetryPolicy = RetryPolicy.DEFAULT,
    onRetry: (suspend (RetryState) -> Unit)? = null,
    block: suspend () -> Result<T>
): Result<T> {
    val logger = logger("RetryPolicy")
    var currentAttempt = 0
    var lastError: TrailGlassError? = null

    while (currentAttempt <= policy.maxAttempts) {
        // First attempt or retry
        if (currentAttempt > 0) {
            val delay = policy.getDelay(currentAttempt - 1)
            logger.debug {
                "Retry attempt $currentAttempt/${policy.maxAttempts} after ${delay.inWholeMilliseconds}ms"
            }

            onRetry?.invoke(
                RetryState(
                    attempt = currentAttempt,
                    lastError = lastError,
                    isRetrying = true
                )
            )

            delay(delay)
        }

        when (val result = block()) {
            is Result.Success -> {
                if (currentAttempt > 0) {
                    logger.info { "Operation succeeded after $currentAttempt retries" }
                }
                return result
            }
            is Result.Error -> {
                lastError = result.error

                // Check if we should retry
                val shouldRetry = policy.shouldRetry(result.error) && currentAttempt < policy.maxAttempts

                if (!shouldRetry) {
                    logger.warn {
                        "Operation failed after $currentAttempt attempts: ${result.error.getTechnicalDetails()}"
                    }
                    return result
                }

                logger.debug {
                    "Operation failed (attempt ${currentAttempt + 1}): ${result.error.userMessage}"
                }
                currentAttempt++
            }
        }
    }

    // Should never reach here, but return last error if we do
    return Result.Error(lastError ?: TrailGlassError.Unknown("Retry loop exhausted"))
}

/**
 * Execute an operation with retry and network connectivity check.
 *
 * Automatically waits for network connection before retrying network errors.
 *
 * @param policy Retry policy to use
 * @param networkConnectivity Network connectivity monitor
 * @param onRetry Optional callback invoked before each retry attempt
 * @param block Operation to execute
 * @return Result of the operation
 */
suspend fun <T> retryWithNetwork(
    policy: RetryPolicy = RetryPolicy.NETWORK,
    networkConnectivity: NetworkConnectivity,
    onRetry: (suspend (RetryState) -> Unit)? = null,
    block: suspend () -> Result<T>
): Result<T> {
    val logger = logger("RetryPolicy")

    return retryWithPolicy(
        policy = policy,
        onRetry = { state ->
            onRetry?.invoke(state)

            // If last error was network-related, wait for connection
            if (state.lastError is TrailGlassError.NetworkError) {
                logger.debug { "Network error detected, waiting for network connection..." }

                // Wait for network connection with a timeout (max 30 seconds)
                val connected = withTimeoutOrNull(30.seconds) {
                    // Wait until we receive a 'true' value from the isConnected flow
                    networkConnectivity.isConnected.first { it }
                }

                if (connected == true) {
                    logger.info { "Network connection restored, proceeding with retry" }
                } else {
                    logger.warn { "Timeout waiting for network connection" }
                }
            }
        },
        block = block
    )
}

/**
 * Extension function to add retry capability to suspending functions.
 */
suspend fun <T> (suspend () -> T).withRetry(
    policy: RetryPolicy = RetryPolicy.DEFAULT,
    onRetry: ((RetryState) -> Unit)? = null
): Result<T> {
    return retryWithPolicy(
        policy = policy,
        onRetry = onRetry
    ) {
        try {
            Result.Success(this())
        } catch (e: Exception) {
            Result.Error(
                TrailGlassError.Unknown(
                    technicalMessage = e.message ?: "Unknown error",
                    cause = e
                )
            )
        }
    }
}
