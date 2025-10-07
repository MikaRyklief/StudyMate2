package com.example.studymate2.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_tasks")
data class StudyTask(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val subject: String,
    val dueDate: Long,
    val durationMinutes: Int,
    val completed: Boolean = false
)
