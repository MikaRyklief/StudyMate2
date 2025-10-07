package com.example.studymate2.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromTaskType(value: TaskType?): String = value?.name ?: TaskType.ASSIGNMENT.name

    @TypeConverter
    fun toTaskType(value: String?): TaskType = value?.let {
        runCatching { TaskType.valueOf(it) }.getOrDefault(TaskType.ASSIGNMENT)
    } ?: TaskType.ASSIGNMENT
}
