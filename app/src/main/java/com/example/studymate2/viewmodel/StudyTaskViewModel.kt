package com.example.studymate2.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.studymate2.StudyMateApp
import com.example.studymate2.data.StudyTask
import com.example.studymate2.data.StudyTaskRepository
import kotlinx.coroutines.launch

class StudyTaskViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: StudyTaskRepository =
        (application as StudyMateApp).taskRepository

    val allTasks = repository.allTasks.asLiveData()

    fun addTask(title: String, subject: String, dueDate: Long, durationMinutes: Int) {
        if (title.isBlank() || subject.isBlank() || durationMinutes <= 0) return
        val task = StudyTask(
            title = title.trim(),
            subject = subject.trim(),
            dueDate = dueDate,
            durationMinutes = durationMinutes
        )
        viewModelScope.launch {
            repository.insert(task)
        }
    }

    fun toggleTaskCompletion(task: StudyTask) {
        viewModelScope.launch {
            repository.update(task.copy(completed = !task.completed))
        }
    }

    fun deleteTask(task: StudyTask) {
        viewModelScope.launch {
            repository.delete(task)
        }
    }
}

class StudyTaskViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StudyTaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StudyTaskViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
