package com.example.studymate2

import com.example.studymate2.network.StudyApiService
import com.example.studymate2.network.StudyTodoResponse
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

/**
 * Tests for RESTful API integration
 * Uses MockWebServer to simulate API responses
 */
class ApiServiceTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: StudyApiService

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        
        // Create API service pointing to mock server
        apiService = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(StudyApiService::class.java)
    }

    @After
    fun teardown() {
        mockWebServer.shutdown()
    }

    // Test successful API response
    @Test
    fun testFetchStudyTodos_Success() = runTest {
        val mockResponse = """
            [
                {"id": 1, "title": "Study Math", "completed": false},
                {"id": 2, "title": "Complete Assignment", "completed": true}
            ]
        """.trimIndent()
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(mockResponse)
        )
        
        val result = apiService.fetchStudyTodos(1)
        
        assertNotNull(result)
        assertEquals(2, result.size)
        assertEquals("Study Math", result[0].title)
        assertFalse(result[0].completed)
        assertEquals("Complete Assignment", result[1].title)
        assertTrue(result[1].completed)
    }

    // Test empty API response
    @Test
    fun testFetchStudyTodos_EmptyResponse() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("[]")
        )
        
        val result = apiService.fetchStudyTodos(1)
        
        assertNotNull(result)
        assertTrue(result.isEmpty())
    }

    // Test API error response (500 server error)
    @Test
    fun testFetchStudyTodos_ServerError() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error")
        )
        
        try {
            apiService.fetchStudyTodos(1)
            fail("Expected exception not thrown")
        } catch (e: Exception) {
            // Expected behavior - API call should fail
            assertTrue(true)
        }
    }

    // Test API timeout
    @Test(timeout = 5000)
    fun testFetchStudyTodos_Timeout() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setSocketPolicy(SocketPolicy.NO_RESPONSE)
        )
        
        try {
            apiService.fetchStudyTodos(1)
            fail("Expected timeout exception")
        } catch (e: Exception) {
            // Timeout should occur
            assertTrue(true)
        }
    }

    // Test malformed JSON response
    @Test
    fun testFetchStudyTodos_MalformedJson() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("{invalid json}")
        )
        
        try {
            apiService.fetchStudyTodos(1)
            fail("Should throw exception for malformed JSON")
        } catch (e: Exception) {
            // Expected - parsing should fail
            assertTrue(true)
        }
    }

    // Test 404 Not Found response
    @Test
    fun testFetchStudyTodos_NotFound() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(404)
                .setBody("Not Found")
        )
        
        try {
            apiService.fetchStudyTodos(999)
            fail("Should throw exception for 404")
        } catch (e: Exception) {
            assertTrue(true)
        }
    }

    // Test correct HTTP request formation
    @Test
    fun testRequestFormatting() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("[]")
        )
        
        apiService.fetchStudyTodos(5)
        
        val request = mockWebServer.takeRequest()
        
        assertEquals("GET", request.method)
        assertTrue(request.path?.contains("todos") == true)
        assertTrue(request.path?.contains("userId=5") == true)
    }

    // Test data parsing from API
    @Test
    fun testDataParsing() = runTest {
        val jsonResponse = """
            [
                {
                    "id": 100,
                    "title": "Test Task",
                    "completed": false
                }
            ]
        """.trimIndent()
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(jsonResponse)
        )
        
        val result = apiService.fetchStudyTodos(1)
        
        assertEquals(1, result.size)
        
        val todo = result[0]
        assertEquals(100, todo.id)
        assertEquals("Test Task", todo.title)
        assertFalse(todo.completed)
    }

    // Test network connectivity issue
    @Test
    fun testNetworkError() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setSocketPolicy(SocketPolicy.DISCONNECT_AT_START)
        )
        
        try {
            apiService.fetchStudyTodos(1)
            fail("Should fail due to network error")
        } catch (e: Exception) {
            // Network error expected
            assertTrue(true)
        }
    }

    // Test multiple API calls
    @Test
    fun testMultipleApiCalls() = runTest {
        // Queue multiple responses
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("[{\"id\": 1, \"title\": \"Task 1\", \"completed\": false}]")
        )
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("[{\"id\": 2, \"title\": \"Task 2\", \"completed\": true}]")
        )
        
        val result1 = apiService.fetchStudyTodos(1)
        val result2 = apiService.fetchStudyTodos(2)
        
        assertEquals(1, result1.size)
        assertEquals(1, result2.size)
        assertEquals("Task 1", result1[0].title)
        assertEquals("Task 2", result2[0].title)
    }

    // Test response with special characters
    @Test
    fun testSpecialCharactersInResponse() = runTest {
        val jsonResponse = """
            [
                {
                    "id": 1,
                    "title": "Study: Math & Physics (Advanced)",
                    "completed": false
                }
            ]
        """.trimIndent()
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(jsonResponse)
        )
        
        val result = apiService.fetchStudyTodos(1)
        
        assertEquals("Study: Math & Physics (Advanced)", result[0].title)
    }

    // Test large response handling
    @Test
    fun testLargeResponse() = runTest {
        val todos = (1..100).map {
            """{"id": $it, "title": "Task $it", "completed": false}"""
        }.joinToString(",", "[", "]")
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(todos)
        )
        
        val result = apiService.fetchStudyTodos(1)
        
        assertEquals(100, result.size)
    }
}

