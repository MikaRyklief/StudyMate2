package com.example.studymate2.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_tasks")
data class StudyTask(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val externalId: String? = null,
    val title: String,
    val subject: String,
    val dueDate: Long,
    val durationMinutes: Int,
    val taskType: TaskType = TaskType.ASSIGNMENT,
    val completed: Boolean = false
)

enum class TaskType(@androidx.annotation.StringRes val labelRes: Int) {
    ASSIGNMENT(com.example.studymate2.R.string.task_type_assignment),
    EXAM(com.example.studymate2.R.string.task_type_exam),
    REVISION(com.example.studymate2.R.string.task_type_revision)
}
