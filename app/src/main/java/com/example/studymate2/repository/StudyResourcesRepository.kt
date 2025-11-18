package com.example.studymate2.repository

import com.example.studymate2.network.PublicApiEntry
import com.example.studymate2.network.StudyApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StudyResourcesRepository(private val api: StudyApiService) {
    suspend fun fetchStudyResources(): List<StudyResource> = withContext(Dispatchers.IO) {
        val apiEntries = runCatching { api.fetchEducationApis() }.getOrNull()?.entries.orEmpty()
        val mapped = apiEntries
            .filter { it.apiName.isNotBlank() || it.description.isNotBlank() }
            .take(12)
            .mapIndexed { index, entry -> entry.toStudyResource(index + 1) }

        if (mapped.isNotEmpty()) mapped else curatedResources
    }

    private fun PublicApiEntry.toStudyResource(id: Int): StudyResource {
        val sourceNote = link.takeIf { it.isNotBlank() }?.let { "Source: $it" }.orEmpty()
        val summary = listOf(description.trim(), sourceNote)
            .filter { it.isNotBlank() }
            .joinToString(separator = " • ")

        return StudyResource(
            id = id,
            title = apiName,
            description = summary.ifBlank { apiName },
            completed = auth.isNullOrBlank() && https
        )
    }

    private val curatedResources = listOf(
        StudyResource(
            id = 1,
            title = "Active recall toolkit",
            description = "Download the spaced-repetition flashcard deck template with guidance on 30/60/90 day review cycles.",
            completed = false
        ),
        StudyResource(
            id = 2,
            title = "Deep work starter plan",
            description = "A 4-session plan that pairs 50-minute focus blocks with short reflection prompts to improve retention.",
            completed = false
        ),
        StudyResource(
            id = 3,
            title = "Exam readiness checklist",
            description = "Printable checklist covering syllabus coverage, practice papers, formula sheet creation, and rest strategy.",
            completed = true
        ),
        StudyResource(
            id = 4,
            title = "Wellness micro-habits",
            description = "Evidence-based mini routines: 5-minute stretch, 2-minute breathing reset, and hydration reminders.",
            completed = true
        ),
        StudyResource(
            id = 5,
            title = "Group study playbook",
            description = "Step-by-step guide for running a 45-minute peer study session with rotating teaching segments.",
            completed = false
        )
    )
}

data class StudyResource(
    val id: Int,
    val title: String,
    val description: String,
    val completed: Boolean
)
