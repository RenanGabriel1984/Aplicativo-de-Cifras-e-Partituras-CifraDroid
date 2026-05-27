package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DataProvider
import com.example.data.Manuscript
import com.example.data.ManuscriptRepository
import com.example.util.PedalManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class MainViewModel(private val repository: ManuscriptRepository, val pedalManager: PedalManager) : ViewModel() {

    init {
        // Initialize with default data if empty
        viewModelScope.launch {
            repository.allManuscripts.collect { manuscripts ->
                if (manuscripts.isEmpty()) {
                    repository.insertDefaultData(DataProvider.initialManuscripts)
                }
            }
        }
    }

    val allManuscripts: StateFlow<List<Manuscript>> = repository.allManuscripts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val favoriteManuscripts: StateFlow<List<Manuscript>> = repository.favoriteManuscripts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val searchResults: StateFlow<List<Manuscript>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                flowOf(emptyList())
            } else {
                repository.search(query)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavorite(manuscript: Manuscript) {
        viewModelScope.launch {
            repository.toggleFavorite(manuscript)
        }
    }

    private val _isVerticalScroll = MutableStateFlow(false)
    val isVerticalScroll = _isVerticalScroll.asStateFlow()

    fun setVerticalScroll(enabled: Boolean) {
        _isVerticalScroll.value = enabled
    }

    fun importDocument(context: android.content.Context, uri: android.net.Uri) {
        viewModelScope.launch {
            val manuscript = com.example.util.PdfImportManager.importFromUri(context, uri)
            if (manuscript != null) {
                repository.insert(manuscript)
            }
        }
    }

    fun getById(id: Int): kotlinx.coroutines.flow.Flow<Manuscript> {
        viewModelScope.launch {
            repository.updateLastUsed(id, System.currentTimeMillis())
        }
        return repository.getById(id)
    }

    fun getRepertoire(id: Int) = repository.getRepertoire(id)
}
