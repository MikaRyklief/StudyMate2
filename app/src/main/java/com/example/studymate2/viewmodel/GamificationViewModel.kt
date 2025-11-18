package com.example.studymate2.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.studymate2.StudyMateApp
import com.example.studymate2.data.StudyTask
import com.example.studymate2.repository.GamificationRepository
import kotlinx.coroutines.launch

class GamificationViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: GamificationRepository =
        (application as StudyMateApp).gamificationRepository

    val profile = repository.profile.asLiveData()

    fun onTaskCompleted(task: StudyTask) {
        viewModelScope.launch {
            repository.recordTaskCompletion(task)
        }
    }

    fun onFocusSessionFinished(minutes: Int) {
        viewModelScope.launch {
            repository.recordFocusSession(minutes)
        }
    }
}

class GamificationViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GamificationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GamificationViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}