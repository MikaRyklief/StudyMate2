package com.example.studymate2

import android.app.Application
import com.example.studymate2.data.StudyDatabase
import com.example.studymate2.data.StudyTaskRepository
import com.example.studymate2.network.StudyApiService
import com.example.studymate2.repository.StudyResourcesRepository

class StudyMateApp : Application() {
    val database: StudyDatabase by lazy { StudyDatabase.getDatabase(this) }
    val taskRepository: StudyTaskRepository by lazy { StudyTaskRepository(database.studyTaskDao()) }
    val apiService: StudyApiService by lazy { StudyApiService.create() }
    val resourcesRepository: StudyResourcesRepository by lazy {
        StudyResourcesRepository(apiService)
    }
}
