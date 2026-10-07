package com.campusevent.qa

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Level 2 - Integration Testing with OkHttp MockWebServer.
 *
 * CRITICAL QA PRINCIPLE:
 * - Deterministic network testing running completely offline.
 * - Simulates accurate HTTP statuses (200, 404, 500), empty arrays, and malformed JSON.
 * - Always starts server in @Before and shuts down safely in @After to prevent port leaks.
 */
class MockWebServerEventTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: EventApiService

    @Before
    fun startServer() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        apiService = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(EventApiService::class.java)
    }

    @After
    fun stopServer() {
        mockWebServer.shutdown()
    }

    @Test
    fun `TEST 1 pemanggilan API sukses HTTP 200 mengembalikan data event yang valid`() = runBlocking {
        val mockJson = """
            [
              {
                "eventId": "EVT-001",
                "title": "Workshop UI/UX Design",
                "category": "Design",
                "speaker": "Sabrina Putri",
                "location": "Lab Komputer 2",
                "date": "2026-10-15",
                "quota": 50,
                "registeredCount": 32
              }
            ]
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(mockJson)
        )

        val result = apiService.getEvents()

        assertThat(result).hasSize(1)
        val event = result[0]
        assertThat(event.eventId).isEqualTo("EVT-001")
        assertThat(event.title).isEqualTo("Workshop UI/UX Design")
        assertThat(event.category).isEqualTo("Design")
        assertThat(event.speaker).isEqualTo("Sabrina Putri")
        assertThat(event.quota).isEqualTo(50)
        assertThat(event.registeredCount).isEqualTo(32)
        assertThat(event.remainingQuota).isEqualTo(18)
    }

    @Test
    fun `TEST 2 pemanggilan API HTTP 404 Not Found melempar HttpException dan tidak dianggap berhasil`() = runBlocking {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(404)
                .setBody("""{"error": "Resource Not Found"}""")
        )

        var caughtException: Exception? = null
        try {
            apiService.getEvents()
        } catch (e: Exception) {
            caughtException = e
        }

        assertThat(caughtException).isNotNull()
        assertThat(caughtException).isInstanceOf(HttpException::class.java)
        val httpException = caughtException as HttpException
        assertThat(httpException.code()).isEqualTo(404)
    }

    @Test
    fun `TEST 3 pemanggilan API HTTP 500 Internal Server Error ditangani sebagai server error`() = runBlocking {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(500)
                .setBody("""{"error": "Internal Server Error"}""")
        )

        var caughtException: Exception? = null
        try {
            apiService.getEvents()
        } catch (e: Exception) {
            caughtException = e
        }

        assertThat(caughtException).isNotNull()
        assertThat(caughtException).isInstanceOf(HttpException::class.java)
        val httpException = caughtException as HttpException
        assertThat(httpException.code()).isEqualTo(500)
    }

    @Test
    fun `TEST 4 pemanggilan API mengembalikan JSON array kosong mengembalikan list size 0`() = runBlocking {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("[]")
        )

        val result = apiService.getEvents()

        assertThat(result).isNotNull()
        assertThat(result).isEmpty()
        assertThat(result).hasSize(0)
    }

    @Test
    fun `TEST 5 pemanggilan API dengan malformed JSON melempar exception parsing dan tertangani`() = runBlocking {
        val corruptedJson = """[ {"eventId": "EVT-999", "title": "Corrupted Data" """

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(corruptedJson)
        )

        var caughtException: Exception? = null
        try {
            apiService.getEvents()
        } catch (e: Exception) {
            caughtException = e
        }

        assertThat(caughtException).isNotNull()
        assertThat(caughtException is com.google.gson.JsonSyntaxException || caughtException is java.io.EOFException).isTrue()
    }
}
