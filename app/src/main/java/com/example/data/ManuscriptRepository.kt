package com.example.data

import kotlinx.coroutines.flow.Flow

class ManuscriptRepository(private val manuscriptDao: ManuscriptDao, private val repertoireDao: RepertoireDao) {
    val allManuscripts: Flow<List<Manuscript>> = manuscriptDao.getAllManuscripts()
    val favoriteManuscripts: Flow<List<Manuscript>> = manuscriptDao.getFavorites()

    fun getByCategory(category: String) = manuscriptDao.getByCategory(category)
    fun search(query: String) = manuscriptDao.searchManuscripts(query)
    fun getById(id: Int) = manuscriptDao.getManuscriptById(id)

    suspend fun insertDefaultData(initialData: List<Manuscript>) {
        manuscriptDao.insertAll(initialData)
    }

    suspend fun insert(manuscript: Manuscript) {
        manuscriptDao.insertManuscript(manuscript)
    }

    suspend fun updateLastUsed(id: Int, timestamp: Long) {
        manuscriptDao.updateLastUsed(id, timestamp)
    }

    suspend fun toggleFavorite(manuscript: Manuscript) {
        manuscriptDao.updateManuscript(manuscript.copy(isFavorite = !manuscript.isFavorite))
    }

    fun getRepertoire(id: Int): Flow<Repertoire> = repertoireDao.getRepertoireById(id)
}
