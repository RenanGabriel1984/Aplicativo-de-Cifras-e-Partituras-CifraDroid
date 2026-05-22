package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.ManuscriptRepository
import com.example.util.PedalManager

class MainViewModelFactory(
    private val repository: ManuscriptRepository,
    private val pedalManager: PedalManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository, pedalManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
