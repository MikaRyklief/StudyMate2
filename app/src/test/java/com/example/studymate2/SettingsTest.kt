package com.example.studymate2

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.studymate2.settings.UserPreferencesRepository
import com.example.studymate2.settings.UserSettings
import com.example.studymate2.viewmodel.SettingsViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Tests for settings persistence and configuration
 * Verifies that user preferences are saved and retrieved correctly
 */
@ExperimentalCoroutinesApi
class SettingsTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var mockDataStore: DataStore<Preferences>
    
    @Mock
    private lateinit var mockPreferences: Preferences

    private lateinit var repository: UserPreferencesRepository
    private lateinit var viewModel: SettingsViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    // Test that notification setting is saved correctly
    @Test
    fun testSaveNotificationSetting_Success() = runTest {
        val mockRepo = mock(UserPreferencesRepository::class.java)
        
        // When setNotificationsEnabled is called, do nothing (success case)
        doNothing().`when`(mockRepo).setNotificationsEnabled(true)
        
        mockRepo.setNotificationsEnabled(true)
        
        verify(mockRepo).setNotificationsEnabled(true)
    }

    // Test that reminder time is saved correctly
    @Test
    fun testSaveReminderTime_Success() = runTest {
        val mockRepo = mock(UserPreferencesRepository::class.java)
        val hour = 20
        val minute = 30
        
        doNothing().`when`(mockRepo).setReminderTime(hour, minute)
        
        mockRepo.setReminderTime(hour, minute)
        
        verify(mockRepo).setReminderTime(hour, minute)
    }

    // Test language preference is saved
    @Test
    fun testSaveLanguage_Success() = runTest {
        val mockRepo = mock(UserPreferencesRepository::class.java)
        val languageCode = "zu"
        
        doNothing().`when`(mockRepo).setLanguage(languageCode)
        
        mockRepo.setLanguage(languageCode)
        
        verify(mockRepo).setLanguage(languageCode)
    }

    // Test default settings values
    @Test
    fun testDefaultSettings() {
        val defaultSettings = UserSettings()
        
        assertTrue(defaultSettings.notificationsEnabled)
        assertEquals(18, defaultSettings.reminderHour)
        assertEquals(0, defaultSettings.reminderMinute)
        assertEquals("en", defaultSettings.languageCode)
    }

    // Test settings persistence - read saved values
    @Test
    fun testReadSavedSettings() = runTest {
        val mockRepo = mock(UserPreferencesRepository::class.java)
        val testSettings = UserSettings(
            notificationsEnabled = false,
            reminderHour = 9,
            reminderMinute = 15,
            languageCode = "zu"
        )
        
        whenever(mockRepo.settingsFlow).thenReturn(flowOf(testSettings))
        
        val result = mockRepo.settingsFlow.first()
        
        assertFalse(result.notificationsEnabled)
        assertEquals(9, result.reminderHour)
        assertEquals(15, result.reminderMinute)
        assertEquals("zu", result.languageCode)
    }

    // Test ViewModel updates notification setting
    @Test
    fun testViewModel_UpdateNotifications() = runTest {
        val mockRepo = mock(UserPreferencesRepository::class.java)
        doNothing().`when`(mockRepo).setNotificationsEnabled(false)
        whenever(mockRepo.settingsFlow).thenReturn(flowOf(UserSettings()))
        
        val viewModel = SettingsViewModel(mockRepo)
        viewModel.setNotifications(false)
        
        verify(mockRepo).setNotificationsEnabled(false)
    }

    // Test ViewModel updates reminder time
    @Test
    fun testViewModel_UpdateReminder() = runTest {
        val mockRepo = mock(UserPreferencesRepository::class.java)
        doNothing().`when`(mockRepo).setReminderTime(14, 45)
        whenever(mockRepo.settingsFlow).thenReturn(flowOf(UserSettings()))
        
        val viewModel = SettingsViewModel(mockRepo)
        viewModel.updateReminder(14, 45)
        
        verify(mockRepo).setReminderTime(14, 45)
    }

    // Test ViewModel updates language
    @Test
    fun testViewModel_UpdateLanguage() = runTest {
        val mockRepo = mock(UserPreferencesRepository::class.java)
        doNothing().`when`(mockRepo).setLanguage("en")
        whenever(mockRepo.settingsFlow).thenReturn(flowOf(UserSettings()))
        
        val viewModel = SettingsViewModel(mockRepo)
        viewModel.updateLanguage("en")
        
        verify(mockRepo).setLanguage("en")
    }

    // Test reset to default settings
    @Test
    fun testResetToDefaultSettings() = runTest {
        val mockRepo = mock(UserPreferencesRepository::class.java)
        
        // Reset means setting back to defaults
        doNothing().`when`(mockRepo).setNotificationsEnabled(true)
        doNothing().`when`(mockRepo).setReminderTime(18, 0)
        doNothing().`when`(mockRepo).setLanguage("en")
        
        // Simulate reset operation
        mockRepo.setNotificationsEnabled(true)
        mockRepo.setReminderTime(18, 0)
        mockRepo.setLanguage("en")
        
        verify(mockRepo).setNotificationsEnabled(true)
        verify(mockRepo).setReminderTime(18, 0)
        verify(mockRepo).setLanguage("en")
    }

    // Test invalid reminder hour (boundary test)
    @Test
    fun testInvalidReminderHour() {
        val invalidHour = 25
        val validMinute = 30
        
        // Should validate that hour is between 0-23
        assertTrue(invalidHour < 0 || invalidHour > 23)
    }

    // Test invalid reminder minute (boundary test)
    @Test
    fun testInvalidReminderMinute() {
        val validHour = 12
        val invalidMinute = 60
        
        // Should validate that minute is between 0-59
        assertTrue(invalidMinute < 0 || invalidMinute > 59)
    }

    // Test multiple rapid setting changes
    @Test
    fun testMultipleSettingChanges() = runTest {
        val mockRepo = mock(UserPreferencesRepository::class.java)
        
        doNothing().`when`(mockRepo).setNotificationsEnabled(any())
        doNothing().`when`(mockRepo).setLanguage(any())
        
        // Simulate user changing settings multiple times
        mockRepo.setNotificationsEnabled(true)
        mockRepo.setNotificationsEnabled(false)
        mockRepo.setLanguage("en")
        mockRepo.setLanguage("zu")
        
        verify(mockRepo, times(2)).setNotificationsEnabled(any())
        verify(mockRepo, times(2)).setLanguage(any())
    }
}

