package com.example.studymate2.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyTaskDao {
    @Query("SELECT * FROM study_tasks ORDER BY dueDate ASC")
    fun getAllTasks(): Flow<List<StudyTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: StudyTask)

    @Update
    suspend fun updateTask(task: StudyTask)

    @Delete
    suspend fun deleteTask(task: StudyTask)
}
