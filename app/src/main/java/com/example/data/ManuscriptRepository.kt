package com.example.data

import kotlinx.coroutines.flow.Flow

class ManuscriptRepository(private val manuscriptDao: ManuscriptDao) {
    val allManuscripts: Flow<List<Manuscript>> = manuscriptDao.getAllManuscripts()
    val favoriteManuscripts: Flow<List<Manuscript>> = manuscriptDao.getFavorites()

    fun getByCategory(category: String) = manuscriptDao.getByCategory(category)
    fun search(query: String) = manuscriptDao.searchManuscripts(query)
    fun getById(id: Int) = manuscriptDao.getManuscriptById(id)

    suspend fun insertDefaultData(initialData: List<Manuscript>) {
        manuscriptDao.insertAll(initialData)
    }

    suspend fun toggleFavorite(manuscript: Manuscript) {
        manuscriptDao.updateManuscript(manuscript.copy(isFavorite = !manuscript.isFavorite))
    }
}
