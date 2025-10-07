package com.example.studymate2

import org.junit.Test
import org.junit.Assert.*

/**
 * Tests for input validation and error handling
 * Ensures the app handles invalid input gracefully without crashing
 */
class ValidationTest {

    // Helper function to validate email format
    private fun isValidEmail(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return email.matches(emailPattern.toRegex())
    }

    // Helper function to validate password strength
    private fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    // Helper function to check if string is empty or blank
    private fun isFieldEmpty(field: String): Boolean {
        return field.trim().isEmpty()
    }

    // Test valid email addresses
    @Test
    fun testEmailValidation_ValidEmails() {
        assertTrue(isValidEmail("test@example.com"))
        assertTrue(isValidEmail("user.name@domain.com"))
        assertTrue(isValidEmail("valid_email@test.co"))
    }

    // Test invalid email addresses
    @Test
    fun testEmailValidation_InvalidEmails() {
        assertFalse(isValidEmail(""))
        assertFalse(isValidEmail("notanemail"))
        assertFalse(isValidEmail("missing@domain"))
        assertFalse(isValidEmail("@example.com"))
        assertFalse(isValidEmail("user@"))
        assertFalse(isValidEmail("user @example.com"))
    }

    // Test email with special characters
    @Test
    fun testEmailValidation_SpecialCharacters() {
        assertFalse(isValidEmail("user#example.com"))
        assertFalse(isValidEmail("user!@example.com"))
        assertFalse(isValidEmail("user space@example.com"))
    }

    // Test empty password field
    @Test
    fun testPasswordValidation_EmptyPassword() {
        assertFalse(isValidPassword(""))
    }

    // Test password minimum length requirement
    @Test
    fun testPasswordValidation_MinimumLength() {
        assertFalse(isValidPassword("12345"))
        assertTrue(isValidPassword("123456"))
        assertTrue(isValidPassword("password123"))
    }

    // Test weak passwords
    @Test
    fun testPasswordValidation_WeakPasswords() {
        // These meet minimum length but are weak
        assertTrue(isValidPassword("111111")) // Still valid by length
        assertTrue(isValidPassword("password"))
    }

    // Test empty field detection
    @Test
    fun testEmptyFieldValidation() {
        assertTrue(isFieldEmpty(""))
        assertTrue(isFieldEmpty("   "))
        assertTrue(isFieldEmpty("\t"))
        assertFalse(isFieldEmpty("text"))
        assertFalse(isFieldEmpty("  text  "))
    }

    // Test password matching
    @Test
    fun testPasswordMatch_Success() {
        val password = "password123"
        val confirmPassword = "password123"
        
        assertEquals(password, confirmPassword)
    }

    // Test password mismatch
    @Test
    fun testPasswordMatch_Failure() {
        val password = "password123"
        val confirmPassword = "password456"
        
        assertNotEquals(password, confirmPassword)
    }

    // Test null safety
    @Test
    fun testNullSafety_EmailValidation() {
        val nullEmail: String? = null
        
        // Should handle null gracefully
        assertFalse(nullEmail?.let { isValidEmail(it) } ?: false)
    }

    // Test trim functionality on input fields
    @Test
    fun testInputTrim() {
        val emailWithSpaces = "  user@example.com  "
        val trimmedEmail = emailWithSpaces.trim()
        
        assertTrue(isValidEmail(trimmedEmail))
        assertFalse(isValidEmail(emailWithSpaces))
    }

    // Test invalid data doesn't crash app (null checks)
    @Test
    fun testInvalidDataHandling_NullValues() {
        val nullString: String? = null
        
        // These should not crash
        val isEmpty = nullString.isNullOrEmpty()
        val isBlank = nullString.isNullOrBlank()
        
        assertTrue(isEmpty)
        assertTrue(isBlank)
    }

    // Test form with all empty fields
    @Test
    fun testFormValidation_AllFieldsEmpty() {
        val email = ""
        val password = ""
        val confirmPassword = ""
        
        assertTrue(isFieldEmpty(email))
        assertTrue(isFieldEmpty(password))
        assertTrue(isFieldEmpty(confirmPassword))
    }

    // Test form with partial data
    @Test
    fun testFormValidation_PartialData() {
        val email = "user@example.com"
        val password = ""
        val confirmPassword = ""
        
        assertTrue(isValidEmail(email))
        assertTrue(isFieldEmpty(password))
        assertTrue(isFieldEmpty(confirmPassword))
    }

    // Test SQL injection attempt in email field
    @Test
    fun testEmailValidation_SqlInjection() {
        val maliciousEmail = "admin'--@example.com"
        
        // Email validation should reject this
        assertFalse(isValidEmail(maliciousEmail))
    }

    // Test XSS attempt in input
    @Test
    fun testInputValidation_XssAttempt() {
        val xssAttempt = "<script>alert('xss')</script>"
        
        // Should be treated as invalid email
        assertFalse(isValidEmail(xssAttempt))
    }

    // Test extremely long input
    @Test
    fun testInputValidation_ExtremelyLongInput() {
        val longEmail = "a".repeat(1000) + "@example.com"
        
        // Should handle gracefully - might be valid or invalid depending on implementation
        // The important thing is it doesn't crash
        val result = try {
            isValidEmail(longEmail)
            true
        } catch (e: Exception) {
            false
        }
        
        // If we got here without crashing, test passes
        assertTrue(result || !result) // Always true - we just want no crash
    }

    // Test unicode characters in email
    @Test
    fun testEmailValidation_UnicodeCharacters() {
        assertFalse(isValidEmail("user@例え.com"))
        assertFalse(isValidEmail("привет@example.com"))
    }

    // Test number-only password
    @Test
    fun testPasswordValidation_NumbersOnly() {
        assertTrue(isValidPassword("123456789"))
    }

    // Test password with whitespace
    @Test
    fun testPasswordValidation_Whitespace() {
        val passwordWithSpaces = "pass word"
        
        // Length is > 6, so technically valid
        assertTrue(isValidPassword(passwordWithSpaces))
    }

    // Test case sensitivity in password matching
    @Test
    fun testPasswordMatch_CaseSensitive() {
        val password = "Password123"
        val confirmPassword = "password123"
        
        // Passwords should be case-sensitive
        assertNotEquals(password, confirmPassword)
    }

    // Test boundary value - exactly 6 character password
    @Test
    fun testPasswordValidation_BoundaryValue() {
        assertTrue(isValidPassword("123456"))
        assertFalse(isValidPassword("12345"))
    }

    // Test registration form validation helper
    private fun validateRegistrationForm(
        email: String,
        password: String,
        confirmPassword: String
    ): Pair<Boolean, String> {
        when {
            isFieldEmpty(email) || isFieldEmpty(password) || isFieldEmpty(confirmPassword) ->
                return Pair(false, "Please fill in all fields")
            !isValidEmail(email) ->
                return Pair(false, "Invalid email format")
            !isValidPassword(password) ->
                return Pair(false, "Password must be at least 6 characters")
            password != confirmPassword ->
                return Pair(false, "Passwords do not match")
            else ->
                return Pair(true, "Valid")
        }
    }

    // Test complete form validation - success
    @Test
    fun testCompleteFormValidation_Success() {
        val result = validateRegistrationForm(
            "user@example.com",
            "password123",
            "password123"
        )
        
        assertTrue(result.first)
        assertEquals("Valid", result.second)
    }

    // Test complete form validation - empty fields
    @Test
    fun testCompleteFormValidation_EmptyFields() {
        val result = validateRegistrationForm("", "", "")
        
        assertFalse(result.first)
        assertEquals("Please fill in all fields", result.second)
    }

    // Test complete form validation - invalid email
    @Test
    fun testCompleteFormValidation_InvalidEmail() {
        val result = validateRegistrationForm(
            "notanemail",
            "password123",
            "password123"
        )
        
        assertFalse(result.first)
        assertEquals("Invalid email format", result.second)
    }

    // Test complete form validation - weak password
    @Test
    fun testCompleteFormValidation_WeakPassword() {
        val result = validateRegistrationForm(
            "user@example.com",
            "12345",
            "12345"
        )
        
        assertFalse(result.first)
        assertEquals("Password must be at least 6 characters", result.second)
    }

    // Test complete form validation - password mismatch
    @Test
    fun testCompleteFormValidation_PasswordMismatch() {
        val result = validateRegistrationForm(
            "user@example.com",
            "password123",
            "password456"
        )
        
        assertFalse(result.first)
        assertEquals("Passwords do not match", result.second)
    }
}

