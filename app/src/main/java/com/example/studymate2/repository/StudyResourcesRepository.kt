package com.example.studymate2.repository

import com.example.studymate2.network.StudyApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StudyResourcesRepository(private val api: StudyApiService) {
    suspend fun fetchStudyResources(): List<StudyResource> = withContext(Dispatchers.IO) {
        try {
            val todos = api.fetchStudyTodos()
            val mapped = todos.map {
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
            if (mapped.isNotEmpty()) mapped else offlineFallback
        } catch (e: Exception) {
            offlineFallback
        }
    }

    private val offlineFallback = listOf(
        StudyResource(
            id = 1,
            title = "Build an exam cram outline",
            description = "List the top 3 topics for your next exam and assign 30-minute focus blocks this week.",
            completed = false
        ),
        StudyResource(
            id = 2,
            title = "Wellness reset",
            description = "Do a 5-minute stretch and hydrate before starting your next Pomodoro cycle.",
            completed = false
        ),
        StudyResource(
            id = 3,
            title = "Review your wins",
            description = "Check your completed tasks and badges to keep streak motivation high.",
            completed = true
        )
    )
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
