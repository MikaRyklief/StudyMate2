package com.example.studymate2.data

data class StudyBlock(
    val taskId: Int,
    val title: String,
    val subject: String,
    val taskType: TaskType,
    val startTimeMillis: Long,
    val durationMinutes: Int
) {
    val endTimeMillis: Long get() = startTimeMillis + durationMinutes * 60_000L
}
