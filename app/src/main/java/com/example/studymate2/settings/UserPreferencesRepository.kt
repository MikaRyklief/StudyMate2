package com.example.studymate2.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "user_settings"
)

data class UserSettings(
    val notificationsEnabled: Boolean = true,
    val reminderHour: Int = 18,
    val reminderMinute: Int = 0
)

class UserPreferencesRepository(private val dataStore: DataStore<Preferences>) {

    private object Keys {
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val REMINDER_HOUR = intPreferencesKey("reminder_hour")
        val REMINDER_MINUTE = intPreferencesKey("reminder_minute")
    }

    val settingsFlow: Flow<UserSettings> = dataStore.data.map { prefs ->
        UserSettings(
            notificationsEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: true,
            reminderHour = prefs[Keys.REMINDER_HOUR] ?: 18,
            reminderMinute = prefs[Keys.REMINDER_MINUTE] ?: 0
        )
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun setReminderTime(hour: Int, minute: Int) {
        dataStore.edit { prefs ->
            prefs[Keys.REMINDER_HOUR] = hour
            prefs[Keys.REMINDER_MINUTE] = minute
        }
    }
}
