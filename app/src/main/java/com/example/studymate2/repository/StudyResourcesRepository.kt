package com.example.studymate2.repository

import com.example.studymate2.network.StudyApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StudyResourcesRepository(private val api: StudyApiService) {
    suspend fun fetchStudyResources(): List<StudyResource> = withContext(Dispatchers.IO) {
        val todos = api.fetchStudyTodos()
        todos.map {
            StudyResource(
                id = it.id,
                title = it.title.capitalizeFirstLetter(),
                completed = it.completed,
                description = if (it.completed) {
                    "Completed task suggestion: review notes and consolidate knowledge."
                } else {
                    "Suggested focus: spend 25 minutes on this topic and take a 5-minute break."
                }
            )
        }
    }
}

data class StudyResource(
    val id: Int,
    val title: String,
    val description: String,
    val completed: Boolean
)

private fun String.capitalizeFirstLetter(): String = replaceFirstChar { char ->
    if (char.isLowerCase()) char.titlecase() else char.toString()
}
