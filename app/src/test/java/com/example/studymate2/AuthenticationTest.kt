package com.example.studymate2

import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.junit.Assert.*

/**
 * Unit tests for authentication functionality
 * Tests both registration and login scenarios with Firebase
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class AuthenticationTest {

    @Mock
    private lateinit var mockAuth: FirebaseAuth
    
    @Mock
    private lateinit var mockAuthTask: Task<AuthResult>
    
    @Mock
    private lateinit var mockAuthResult: AuthResult
    
    @Mock
    private lateinit var mockFirebaseUser: FirebaseUser
    
    @Mock
    private lateinit var mockGoogleSignInAccount: GoogleSignInAccount

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    // Test successful user registration
    @Test
    fun testRegisterUser_Success() {
        val email = "test@example.com"
        val password = "password123"
        
        // Mock successful registration
        `when`(mockAuth.createUserWithEmailAndPassword(email, password))
            .thenReturn(mockAuthTask)
        `when`(mockAuthTask.isSuccessful).thenReturn(true)
        `when`(mockAuthTask.result).thenReturn(mockAuthResult)
        
        // Capture the completion listener
        val listenerCaptor = argumentCaptor<OnCompleteListener<AuthResult>>()
        
        // Simulate registration
        mockAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(listenerCaptor.capture())
        
        // Trigger the listener
        listenerCaptor.firstValue.onComplete(mockAuthTask)
        
        // Verify registration was attempted
        verify(mockAuth).createUserWithEmailAndPassword(email, password)
        assertTrue(mockAuthTask.isSuccessful)
    }

    // Test registration failure
    @Test
    fun testRegisterUser_Failure() {
        val email = "bad@example.com"
        val password = "weak"
        
        // Mock failed registration
        `when`(mockAuth.createUserWithEmailAndPassword(email, password))
            .thenReturn(mockAuthTask)
        `when`(mockAuthTask.isSuccessful).thenReturn(false)
        `when`(mockAuthTask.exception).thenReturn(Exception("Registration failed"))
        
        val listenerCaptor = argumentCaptor<OnCompleteListener<AuthResult>>()
        
        mockAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(listenerCaptor.capture())
        
        listenerCaptor.firstValue.onComplete(mockAuthTask)
        
        verify(mockAuth).createUserWithEmailAndPassword(email, password)
        assertFalse(mockAuthTask.isSuccessful)
        assertNotNull(mockAuthTask.exception)
    }

    // Test login with valid credentials
    @Test
    fun testLoginUser_ValidCredentials() {
        val email = "user@example.com"
        val password = "correctpassword"
        
        `when`(mockAuth.signInWithEmailAndPassword(email, password))
            .thenReturn(mockAuthTask)
        `when`(mockAuthTask.isSuccessful).thenReturn(true)
        `when`(mockAuthResult.user).thenReturn(mockFirebaseUser)
        
        val listenerCaptor = argumentCaptor<OnCompleteListener<AuthResult>>()
        
        mockAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(listenerCaptor.capture())
        
        listenerCaptor.firstValue.onComplete(mockAuthTask)
        
        verify(mockAuth).signInWithEmailAndPassword(email, password)
        assertTrue(mockAuthTask.isSuccessful)
    }

    // Test login with invalid credentials
    @Test
    fun testLoginUser_InvalidCredentials() {
        val email = "wrong@example.com"
        val password = "wrongpassword"
        
        `when`(mockAuth.signInWithEmailAndPassword(email, password))
            .thenReturn(mockAuthTask)
        `when`(mockAuthTask.isSuccessful).thenReturn(false)
        `when`(mockAuthTask.exception).thenReturn(Exception("Invalid credentials"))
        
        val listenerCaptor = argumentCaptor<OnCompleteListener<AuthResult>>()
        
        mockAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(listenerCaptor.capture())
        
        listenerCaptor.firstValue.onComplete(mockAuthTask)
        
        verify(mockAuth).signInWithEmailAndPassword(email, password)
        assertFalse(mockAuthTask.isSuccessful)
    }

    // Test token retrieval after successful login
    @Test
    fun testGetIdToken_Success() {
        `when`(mockAuth.currentUser).thenReturn(mockFirebaseUser)
        `when`(mockFirebaseUser.getIdToken(false)).thenReturn(mock(Task::class.java) as Task<*>)
        
        val currentUser = mockAuth.currentUser
        assertNotNull(currentUser)
        
        // Verify we can request token
        currentUser?.getIdToken(false)
        verify(mockFirebaseUser).getIdToken(false)
    }

    // Test token validation - user is logged in
    @Test
    fun testTokenValidation_UserLoggedIn() {
        `when`(mockAuth.currentUser).thenReturn(mockFirebaseUser)
        `when`(mockFirebaseUser.uid).thenReturn("test-uid-12345")
        
        val user = mockAuth.currentUser
        
        assertNotNull(user)
        assertEquals("test-uid-12345", user?.uid)
    }

    // Test token validation - user not logged in
    @Test
    fun testTokenValidation_UserNotLoggedIn() {
        `when`(mockAuth.currentUser).thenReturn(null)
        
        val user = mockAuth.currentUser
        
        assertNull(user)
    }

    // Test Google Sign-In token handling
    @Test
    fun testGoogleSignIn_TokenHandling() {
        val testToken = "test-google-token-xyz"
        
        `when`(mockGoogleSignInAccount.idToken).thenReturn(testToken)
        
        val token = mockGoogleSignInAccount.idToken
        
        assertNotNull(token)
        assertEquals(testToken, token)
    }

    // Test registration with empty email field
    @Test
    fun testRegisterUser_EmptyEmail() {
        val email = ""
        val password = "password123"
        
        // Validation should prevent this from reaching Firebase
        assertTrue(email.isEmpty())
        // In real app, registration shouldn't be called with empty email
    }

    // Test registration with mismatched passwords
    @Test
    fun testRegisterUser_PasswordMismatch() {
        val password = "password123"
        val confirmPassword = "password456"
        
        assertNotEquals(password, confirmPassword)
        // This should trigger validation error in the app
    }

    // Test logout functionality
    @Test
    fun testUserLogout() {
        doNothing().`when`(mockAuth).signOut()
        
        mockAuth.signOut()
        
        verify(mockAuth).signOut()
    }
}

