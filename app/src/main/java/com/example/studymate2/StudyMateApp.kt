package com.example.studymate2

import android.app.Application
import com.example.studymate2.data.StudyDatabase
import com.example.studymate2.data.StudyTaskRepository
import com.example.studymate2.network.StudyApiService
import com.example.studymate2.repository.StudyResourcesRepository
import com.example.studymate2.notification.StudyNotificationScheduler
import com.example.studymate2.repository.GamificationRepository
import com.example.studymate2.settings.UserPreferencesRepository
import com.example.studymate2.settings.userPreferencesDataStore
import com.example.studymate2.util.LocaleManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestoreSettings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class StudyMateApp : Application() {
    val database: StudyDatabase by lazy { StudyDatabase.getDatabase(this) }
    val taskRepository: StudyTaskRepository by lazy { StudyTaskRepository(database.studyTaskDao()) }
    val gamificationRepository: GamificationRepository by lazy {
        GamificationRepository(database.gamificationDao())
    }
    val apiService: StudyApiService by lazy { StudyApiService.create() }
    val resourcesRepository: StudyResourcesRepository by lazy {
        StudyResourcesRepository(apiService)
    }

    override fun onCreate() {
        super.onCreate()
        StudyNotificationScheduler.createChannels(this)

        FirebaseFirestore.getInstance().firestoreSettings = firestoreSettings {
            isPersistenceEnabled = true
        }

        runBlocking {
            val repository = UserPreferencesRepository(userPreferencesDataStore)
            val settings = repository.settingsFlow.first()
            LocaleManager.apply(settings.languageCode)
            if (settings.notificationsEnabled) {
                StudyNotificationScheduler.scheduleDailyReminder(
                    this@StudyMateApp,
                    settings.reminderHour,
                    settings.reminderMinute,
                    settings.notificationsEnabled
                )
            }
            StudyNotificationScheduler.scheduleWellnessNudges(
                this@StudyMateApp,
                settings.wellnessNudgesEnabled
            )
        }
    }
}
