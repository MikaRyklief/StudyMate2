package com.example.studymate2.data

import kotlinx.coroutines.flow.Flow

class StudyTaskRepository(private val dao: StudyTaskDao) {
    val allTasks: Flow<List<StudyTask>> = dao.getAllTasks()

    suspend fun insert(task: StudyTask) = dao.insertTask(task)
    suspend fun update(task: StudyTask) = dao.updateTask(task)
    suspend fun delete(task: StudyTask) = dao.deleteTask(task)

    suspend fun getPendingTasks(): List<StudyTask> = dao.getPendingTasks()
}
