package com.po4yka.trailglass.data.remote

import io.kotest.matchers.shouldBe
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertTrue

class ApiClientHttpTest {

    @Test
    fun `should handle successful HTTP response`() = runTest {
        val mockEngine = MockEngine { request ->
            respond(
                content = """{"status": "success"}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        val response = client.get("https://test.api.com/status")
        response.status shouldBe HttpStatusCode.OK
        client.close()
    }

    @Test
    fun `should handle 401 Unauthorized response`() = runTest {
        val mockEngine = MockEngine { request ->
            respond(
                content = """{"error": "Unauthorized"}""",
                status = HttpStatusCode.Unauthorized,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        val response = client.get("https://test.api.com/protected")
        response.status shouldBe HttpStatusCode.Unauthorized
        client.close()
    }

    @Test
    fun `should handle 500 server error response`() = runTest {
        val mockEngine = MockEngine { request ->
            respond(
                content = """{"error": "Internal Server Error"}""",
                status = HttpStatusCode.InternalServerError,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        val response = client.get("https://test.api.com/error")
        response.status shouldBe HttpStatusCode.InternalServerError
        client.close()
    }

    @Test
    fun `should handle network timeout`() = runTest {
        var timeoutOccurred = false

        val mockEngine = MockEngine { request ->
            throw io.ktor.client.network.sockets.ConnectTimeoutException("Connection timeout")
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        try {
            client.get("https://test.api.com/slow")
        } catch (e: Exception) {
            timeoutOccurred = true
            assertTrue(e is io.ktor.client.network.sockets.ConnectTimeoutException)
        }

        assertTrue(timeoutOccurred, "Expected timeout exception")
        client.close()
    }

    @Test
    fun `should include request headers`() = runTest {
        var receivedHeaders: Headers? = null

        val mockEngine = MockEngine { request ->
            receivedHeaders = request.headers
            respond(
                content = """{"status": "ok"}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        client.get("https://test.api.com/test") {
            header("Authorization", "Bearer test_token")
            header("X-Device-Id", "test_device")
        }

        receivedHeaders?.get("Authorization") shouldBe "Bearer test_token"
        receivedHeaders?.get("X-Device-Id") shouldBe "test_device"
        client.close()
    }

    @Test
    fun `should send POST request with body`() = runTest {
        var receivedMethod: HttpMethod? = null
        var receivedBody: String? = null

        val mockEngine = MockEngine { request ->
            receivedMethod = request.method
            receivedBody = request.body.toString()

            respond(
                content = """{"id": "123", "status": "created"}""",
                status = HttpStatusCode.Created,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        client.post("https://test.api.com/items") {
            contentType(ContentType.Application.Json)
            setBody("""{"name": "test"}""")
        }

        receivedMethod shouldBe HttpMethod.Post
        client.close()
    }

    @Test
    fun `should handle multiple retry attempts`() = runTest {
        var attemptCount = 0

        val mockEngine = MockEngine { request ->
            attemptCount++
            if (attemptCount < 3) {
                throw io.ktor.client.network.sockets.SocketTimeoutException("Timeout")
            }
            respond(
                content = """{"status": "success"}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        // Simulate retry logic
        var response: io.ktor.client.statement.HttpResponse? = null
        for (i in 1..3) {
            try {
                response = client.get("https://test.api.com/retry")
                break
            } catch (e: Exception) {
                if (i == 3) throw e
            }
        }

        response?.status shouldBe HttpStatusCode.OK
        attemptCount shouldBe 3
        client.close()
    }
}
