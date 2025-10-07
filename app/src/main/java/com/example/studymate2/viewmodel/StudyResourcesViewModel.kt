package com.example.studymate2.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.studymate2.StudyMateApp
import com.example.studymate2.repository.StudyResource
import com.example.studymate2.repository.StudyResourcesRepository
import kotlinx.coroutines.launch

class StudyResourcesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: StudyResourcesRepository =
        (application as StudyMateApp).resourcesRepository

    private val _resources = MutableLiveData<List<StudyResource>>()
    val resources: LiveData<List<StudyResource>> = _resources

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    fun loadResources() {
        if (_isLoading.value == true) return
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                val tips = repository.fetchStudyResources()
                _resources.value = tips
            } catch (e: Exception) {
                _errorMessage.value = "Unable to load study tips. Please try again later."
            } finally {
                _isLoading.value = false
            }
        }
    }
}

class StudyResourcesViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StudyResourcesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StudyResourcesViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
