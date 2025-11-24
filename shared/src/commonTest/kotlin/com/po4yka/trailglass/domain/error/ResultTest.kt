package com.po4yka.trailglass.domain.error

import kotlinx.coroutines.CancellationException
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ResultTest {

    @Test
    fun `resultOf rethrows CancellationException`() {
        assertFailsWith<CancellationException> {
            resultOf<Unit> {
                throw CancellationException("Cancelled")
            }
        }
    }

    @Test
    fun `resultOf catches other exceptions`() {
        val result = resultOf<Unit> {
            throw RuntimeException("Something went wrong")
        }
        assertTrue(result is Result.Error)
        assertTrue(result.error is TrailGlassError.Unknown)
    }
}
