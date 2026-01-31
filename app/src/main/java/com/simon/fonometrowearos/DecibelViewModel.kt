package com.simon.fonometrowearos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DecibelViewModel(private val repository: DecibelRepository) : ViewModel() {

    val decibelFlow: StateFlow<Float> = repository.decibelFlow

    fun startListening() {
        viewModelScope.launch {
            repository.startListening()
        }
    }

    fun stopListening() {
        repository.stopListening()
    }

    override fun onCleared() {
        super.onCleared()
        stopListening()
    }
}

class DecibelViewModelFactory(private val repository: DecibelRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DecibelViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DecibelViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
