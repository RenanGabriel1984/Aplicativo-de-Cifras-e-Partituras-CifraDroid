package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DataProvider
import com.example.data.Manuscript
import com.example.data.ManuscriptRepository
import com.example.util.PedalManager
import com.example.ui.screens.LiturgicalTheme
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class MainViewModel(private val repository: ManuscriptRepository, val pedalManager: PedalManager) : ViewModel() {

    init {
        // Initialize with default data if empty
        viewModelScope.launch {
            val manuscripts = repository.allManuscripts.first()
            if (manuscripts.isEmpty()) {
                repository.insertDefaultData(DataProvider.initialManuscripts)
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

    private val _isStageMode = MutableStateFlow(false)
    val isStageMode = _isStageMode.asStateFlow()

    fun setStageMode(enabled: Boolean) {
        _isStageMode.value = enabled
    }

    private val _isChoirMode = MutableStateFlow(false)
    val isChoirMode = _isChoirMode.asStateFlow()

    fun setChoirMode(enabled: Boolean) {
        _isChoirMode.value = enabled
    }

    private val _liturgicalTheme = MutableStateFlow(LiturgicalTheme.CLASSIC)
    val liturgicalTheme = _liturgicalTheme.asStateFlow()

    fun setLiturgicalTheme(theme: LiturgicalTheme) {
        _liturgicalTheme.value = theme
    }
    
    private val _autoScrollSpeed = MutableStateFlow(0f)
    val autoScrollSpeed = _autoScrollSpeed.asStateFlow()

    fun setAutoScrollSpeed(speed: Float) {
        _autoScrollSpeed.value = speed
    }

    fun importDocument(context: android.content.Context, uri: android.net.Uri) {
        viewModelScope.launch {
            val manuscript = com.example.util.PdfImportManager.importFromUri(context, uri)
            if (manuscript != null) {
                val id = repository.insert(manuscript)
                if (manuscript.extractedText.isNotEmpty()) {
                    repository.savePdfText(com.example.data.PdfTextContent(id.toInt(), manuscript.extractedText))
                }
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

    fun getPdfText(manuscriptId: Int) = repository.getPdfText(manuscriptId)

    suspend fun getTransposedContent(manuscriptId: Int, steps: Int, useFlats: Boolean): String {
        val pdfText = repository.getPdfText(manuscriptId).first()
        val content = pdfText?.content ?: ""
        if (steps == 0) return content
        return com.example.util.ChordTransposer.transposeText(content, steps, useFlats)
    }

    fun getPreferredKey(manuscriptId: Int) = repository.getPreferredKey(manuscriptId)

    fun savePreferredKey(manuscriptId: Int, preferredKey: String) {
        viewModelScope.launch {
            repository.savePreferredKey(com.example.data.TranspositionPreference(manuscriptId, preferredKey))
        }
    }

    fun deleteDocument(context: android.content.Context, manuscript: Manuscript) {
        viewModelScope.launch {
            // Remove physical file
            val localUri = manuscript.localUri
            if (!localUri.isNullOrBlank()) {
                val file = java.io.File(localUri)
                if (file.exists()) {
                    file.delete()
                }
            }
            
            // Remove cover file if local
            val coverUrl = manuscript.coverUrl
            if (coverUrl.startsWith("file://")) {
                val coverFile = java.io.File(coverUrl.removePrefix("file://"))
                if (coverFile.exists()) {
                    coverFile.delete()
                }
            }
            
            // Remove from Coil cache
            if (coverUrl.isNotEmpty()) {
                val imageLoader = coil.Coil.imageLoader(context)
                imageLoader.diskCache?.remove(coverUrl)
                imageLoader.memoryCache?.remove(coil.memory.MemoryCache.Key(coverUrl))
            }

            // Remove from Room DB
            repository.delete(manuscript)
        }
    }
}
