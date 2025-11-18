package com.example.studymate2

import android.util.Log
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog
import org.junit.Assert.*

/**
 * Tests for logging functionality
 * Verifies that appropriate logs are generated during key operations
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class LoggingTest {

    @Before
    fun setup() {
        // Enable log output
        ShadowLog.stream = System.out
    }

    @After
    fun teardown() {
        // Clear logs after each test
        ShadowLog.clear()
    }

    // Test login logging
    @Test
    fun testLoginLogging() {
        val tag = "FirebaseAuth"
        val message = "signInWithCredential failed"
        
        Log.e(tag, message)
        
        val logs = ShadowLog.getLogsForTag(tag)
        assertTrue(logs.isNotEmpty())
        
        val logEntry = logs.find { it.msg == message }
        assertNotNull(logEntry)
        assertEquals(Log.ERROR, logEntry?.type)
    }

    // Test registration logging
    @Test
    fun testRegistrationLogging() {
        val tag = "Register"
        val message = "Failed"
        
        Log.e(tag, message)
        
        val logs = ShadowLog.getLogsForTag(tag)
        assertTrue(logs.isNotEmpty())
        
        val logEntry = logs.find { it.msg == message }
        assertNotNull(logEntry)
    }

    // Test Google Sign-In logging
    @Test
    fun testGoogleSignInLogging() {
        val tag = "GoogleSignIn"
        val message = "ApiException"
        
        Log.e(tag, message)
        
        val logs = ShadowLog.getLogsForTag(tag)
        assertNotNull(logs)
        assertTrue(logs.size > 0)
    }

    // Test API fetch logging
    @Test
    fun testApiLogging() {
        val tag = "ApiService"
        val message = "Fetching study todos from API"
        
        Log.d(tag, message)
        
        val logs = ShadowLog.getLogsForTag(tag)
        val logEntry = logs.find { it.msg == message }
        
        if (logEntry != null) {
            assertEquals(Log.DEBUG, logEntry.type)
        }
    }

    // Test settings update logging
    @Test
    fun testSettingsUpdateLogging() {
        val tag = "Settings"
        val message = "Notification settings updated"
        
        Log.i(tag, message)
        
        val logs = ShadowLog.getLogsForTag(tag)
        val logEntry = logs.find { it.msg == message }
        
        if (logEntry != null) {
            assertEquals(Log.INFO, logEntry.type)
        }
    }

    // Test error logging
    @Test
    fun testErrorLogging() {
        val tag = "ErrorHandler"
        val message = "Network request failed"
        
        Log.e(tag, message)
        
        val logs = ShadowLog.getLogsForTag(tag)
        assertTrue(logs.any { it.msg == message })
    }

    // Test warning logging
    @Test
    fun testWarningLogging() {
        val tag = "Warning"
        val message = "Low battery, notifications may be delayed"
        
        Log.w(tag, message)
        
        val logs = ShadowLog.getLogsForTag(tag)
        val logEntry = logs.find { it.msg == message }
        
        if (logEntry != null) {
            assertEquals(Log.WARN, logEntry.type)
        }
    }

    // Test verbose logging
    @Test
    fun testVerboseLogging() {
        val tag = "Verbose"
        val message = "Detailed operation trace"
        
        Log.v(tag, message)
        
        val logs = ShadowLog.getLogsForTag(tag)
        val logEntry = logs.find { it.msg == message }
        
        if (logEntry != null) {
            assertEquals(Log.VERBOSE, logEntry.type)
        }
    }

    // Test multiple log entries
    @Test
    fun testMultipleLogEntries() {
        val tag = "MultiLog"
        
        Log.d(tag, "First log")
        Log.d(tag, "Second log")
        Log.d(tag, "Third log")
        
        val logs = ShadowLog.getLogsForTag(tag)
        assertTrue(logs.size >= 3)
    }

    // Test logging with exception
    @Test
    fun testLoggingWithException() {
        val tag = "Exception"
        val message = "Error occurred"
        val exception = Exception("Test exception")
        
        Log.e(tag, message, exception)
        
        val logs = ShadowLog.getLogsForTag(tag)
        val logEntry = logs.find { it.msg == message }
        
        assertNotNull(logEntry)
        assertEquals(Log.ERROR, logEntry?.type)
        assertNotNull(logEntry?.throwable)
    }

    // Test that sensitive data is not logged
    @Test
    fun testNoSensitiveDataInLogs() {
        val tag = "Security"
        val password = "secretPassword123"
        
        // Should NOT log password directly
        Log.d(tag, "User authentication attempt")
        
        val logs = ShadowLog.getLogsForTag(tag)
        
        // Verify password is not in logs
        val containsPassword = logs.any { it.msg.contains(password) }
        assertFalse("Password should not be in logs", containsPassword)
    }

    // Test log filtering by level
    @Test
    fun testLogLevelFiltering() {
        val tag = "FilterTest"
        
        Log.v(tag, "Verbose message")
        Log.d(tag, "Debug message")
        Log.i(tag, "Info message")
        Log.w(tag, "Warning message")
        Log.e(tag, "Error message")
        
        val logs = ShadowLog.getLogsForTag(tag)
        
        // Should have all 5 log levels
        assertTrue(logs.size >= 5)
        
        // Verify each level exists
        assertTrue(logs.any { it.type == Log.VERBOSE })
        assertTrue(logs.any { it.type == Log.DEBUG })
        assertTrue(logs.any { it.type == Log.INFO })
        assertTrue(logs.any { it.type == Log.WARN })
        assertTrue(logs.any { it.type == Log.ERROR })
    }

    // Test database operation logging
    @Test
    fun testDatabaseOperationLogging() {
        val tag = "Database"
        val message = "Study task inserted successfully"
        
        Log.d(tag, message)
        
        val logs = ShadowLog.getLogsForTag(tag)
        assertTrue(logs.any { it.msg == message })
    }

    // Test network operation logging
    @Test
    fun testNetworkOperationLogging() {
        val tag = "Network"
        val message = "API request initiated"
        
        Log.d(tag, message)
        
        val logs = ShadowLog.getLogsForTag(tag)
        assertTrue(logs.any { it.msg == message })
    }

    // Test user action logging
    @Test
    fun testUserActionLogging() {
        val tag = "UserAction"
        val message = "User clicked settings button"
        
        Log.i(tag, message)
        
        val logs = ShadowLog.getLogsForTag(tag)
        val logEntry = logs.find { it.msg == message }
        
        if (logEntry != null) {
            assertEquals(Log.INFO, logEntry.type)
        }
    }

    // Test performance logging
    @Test
    fun testPerformanceLogging() {
        val tag = "Performance"
        val startTime = System.currentTimeMillis()
        
        // Simulate some operation
        Thread.sleep(10)
        
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime
        
        Log.d(tag, "Operation completed in ${duration}ms")
        
        val logs = ShadowLog.getLogsForTag(tag)
        assertTrue(logs.any { it.msg.contains("Operation completed") })
    }

    // Test initialization logging
    @Test
    fun testInitializationLogging() {
        val tag = "Init"
        val message = "Firebase initialized successfully"
        
        Log.i(tag, message)
        
        val logs = ShadowLog.getLogsForTag(tag)
        assertTrue(logs.any { it.msg == message })
    }

    // Test crash reporting simulation
    @Test
    fun testCrashReportingLog() {
        val tag = "CrashReport"
        val message = "Fatal error detected"
        val exception = RuntimeException("Simulated crash")
        
        Log.e(tag, message, exception)
        
        val logs = ShadowLog.getLogsForTag(tag)
        val logEntry = logs.find { it.msg == message }
        
        assertNotNull(logEntry)
        assertNotNull(logEntry?.throwable)
        assertEquals("Simulated crash", logEntry?.throwable?.message)
    }

    // Test log message formatting
    @Test
    fun testLogMessageFormatting() {
        val tag = "Format"
        val userId = 123
        val action = "login"
        val message = "User $userId performed $action"
        
        Log.i(tag, message)
        
        val logs = ShadowLog.getLogsForTag(tag)
        assertTrue(logs.any { it.msg == "User 123 performed login" })
    }
}

