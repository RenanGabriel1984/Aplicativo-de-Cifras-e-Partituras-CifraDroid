package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ManuscriptRepository(
    private val manuscriptDao: ManuscriptDao, 
    private val repertoireDao: RepertoireDao,
    private val transpositionDao: TranspositionDao,
    private val pdfTextContentDao: PdfTextContentDao,
    private val songChartDao: SongChartDao,
    private val repertoireSongDao: RepertoireSongDao,
    private val repertoireCategoryDao: RepertoireCategoryDao
) {
    val allManuscripts: Flow<List<Manuscript>> = manuscriptDao.getAllManuscripts()
    val favoriteManuscripts: Flow<List<Manuscript>> = manuscriptDao.getFavorites()

    fun getByCategory(category: String) = manuscriptDao.getByCategory(category)
    fun search(query: String) = manuscriptDao.searchManuscripts(query)
    fun getById(id: Int) = manuscriptDao.getManuscriptById(id)

    suspend fun insertDefaultData(initialData: List<Manuscript>) {
        manuscriptDao.insertAll(initialData)
    }

    suspend fun insert(manuscript: Manuscript): Long {
        return manuscriptDao.insertManuscript(manuscript)
    }

    suspend fun updateLastUsed(id: Int, timestamp: Long) {
        manuscriptDao.updateLastUsed(id, timestamp)
    }

    suspend fun toggleFavorite(manuscript: Manuscript) {
        manuscriptDao.updateManuscript(manuscript.copy(isFavorite = !manuscript.isFavorite))
    }

    fun getAllSongCharts() = songChartDao.getAllSongCharts()

    fun getSongCharts(manuscriptId: Int) = songChartDao.getByManuscriptId(manuscriptId)

    fun getSongChartById(songChartId: Int) = songChartDao.getById(songChartId)

    suspend fun saveSongCharts(songCharts: List<SongChart>) {
        songChartDao.insertAll(songCharts)
    }

    suspend fun updateSongChartKey(songChartId: Int, newKey: String?) {
        songChartDao.updateSavedKey(songChartId, newKey)
    }

    suspend fun updateRepertoireSongKey(repertoireSongId: Int, newKey: String?) {
        repertoireSongDao.updateCustomKey(repertoireSongId, newKey)
    }

    fun getRepertoireSongById(id: Int): Flow<RepertoireSong?> = repertoireSongDao.getById(id)

    fun getSongsForRepertoire(repertoireId: Int): Flow<List<RepertoireSong>> = repertoireSongDao.getSongsForRepertoire(repertoireId)

    fun findRepertoireSong(repertoireId: Int, songChartId: Int): Flow<RepertoireSong?> = repertoireSongDao.findRepertoireSong(repertoireId, songChartId)

    suspend fun delete(manuscript: Manuscript) {
        pdfTextContentDao.deletePdfText(manuscript.id)
        manuscriptDao.deleteManuscript(manuscript)
    }

    fun getAllRepertoires(): Flow<List<Repertoire>> = repertoireDao.getAllRepertoires()
    
    suspend fun insertImportedRepertoire(repertoire: Repertoire, importedSongs: List<RepertoireSong>): Long {
        val id = repertoireDao.insert(repertoire)
        val repId = if (repertoire.id == 0) id.toInt() else repertoire.id
        
        val customKeyMap = importedSongs.associate { it.songChartId to it.customKey }
        val categoriesFromJson = com.example.util.RepertoireUtil.getCategories(repertoire)

        repertoireCategoryDao.deleteCategoriesForRepertoire(repId)
        repertoireSongDao.deleteSongsForRepertoire(repId)

        categoriesFromJson.forEachIndexed { catIdx, cat ->
            val catId = repertoireCategoryDao.insert(RepertoireCategory(
                repertoireId = repId,
                name = cat.name,
                position = catIdx
            ))

            cat.manuscriptIds.forEachIndexed { songIdx, songChartId ->
                 repertoireSongDao.insert(RepertoireSong(
                     repertoireId = repId,
                     songChartId = songChartId,
                     categoryId = catId.toInt(),
                     position = songIdx,
                     customKey = customKeyMap[songChartId]
                 ))
            }
        }
        return id
    }

    suspend fun insertRepertoire(repertoire: Repertoire): Long {
        val id = repertoireDao.insert(repertoire)
        val repId = if (repertoire.id == 0) id.toInt() else repertoire.id
        
        val categoriesFromJson = com.example.util.RepertoireUtil.getCategories(repertoire)

        val oldSongs = repertoireSongDao.getSongsForRepertoireSync(repId)
        val customKeyMap = oldSongs.associate { it.songChartId to it.customKey }

        repertoireCategoryDao.deleteCategoriesForRepertoire(repId)
        repertoireSongDao.deleteSongsForRepertoire(repId)

        categoriesFromJson.forEachIndexed { catIdx, cat ->
            val catId = repertoireCategoryDao.insert(RepertoireCategory(
                repertoireId = repId,
                name = cat.name,
                position = catIdx
            ))

            cat.manuscriptIds.forEachIndexed { songIdx, songChartId ->
                 repertoireSongDao.insert(RepertoireSong(
                     repertoireId = repId,
                     songChartId = songChartId,
                     categoryId = catId.toInt(),
                     position = songIdx,
                     customKey = customKeyMap[songChartId]
                 ))
            }
        }
        return id
    }
    suspend fun deleteRepertoire(id: Int) { repertoireDao.delete(id) }

    fun getRepertoire(id: Int): Flow<Repertoire> = repertoireDao.getRepertoireById(id)

    fun getPreferredKey(manuscriptId: Int) = transpositionDao.getPreference(manuscriptId)

    suspend fun savePreferredKey(preference: TranspositionPreference) {
        transpositionDao.savePreference(preference)
    }

    fun getPdfText(manuscriptId: Int) = pdfTextContentDao.getPdfText(manuscriptId)

    suspend fun savePdfText(pdfTextContent: PdfTextContent) {
        pdfTextContentDao.savePdfText(pdfTextContent)
    }
    
    suspend fun deletePdfText(manuscriptId: Int) {
        pdfTextContentDao.deletePdfText(manuscriptId)
    }

    suspend fun exportBackup(): String {
        try {
            val manuscripts = manuscriptDao.getAllManuscripts().first()
            val songCharts = songChartDao.getAllSongCharts().first()
            val repertoires = repertoireDao.getAllRepertoires().first()
            val categories = repertoireCategoryDao.getAll().first()
            val songs = repertoireSongDao.getAll().first()

            val root = org.json.JSONObject()
            
            val manuscriptsArr = org.json.JSONArray()
            for (m in manuscripts) {
                val obj = org.json.JSONObject()
                obj.put("id", m.id)
                obj.put("title", m.title)
                obj.put("composer", m.composer)
                obj.put("category", m.category)
                obj.put("coverUrl", m.coverUrl)
                obj.put("isFavorite", m.isFavorite)
                obj.put("lastUsedTimestamp", m.lastUsedTimestamp)
                obj.put("keySignature", m.keySignature)
                obj.put("era", m.era)
                obj.put("localUri", m.localUri ?: org.json.JSONObject.NULL)
                obj.put("extractedText", m.extractedText)
                obj.put("capo", m.capo)
                obj.put("tone", m.tone)
                obj.put("liturgicalNotes", m.liturgicalNotes)
                manuscriptsArr.put(obj)
            }
            root.put("manuscripts", manuscriptsArr)
            
            val songChartsArr = org.json.JSONArray()
            for (s in songCharts) {
                val obj = org.json.JSONObject()
                obj.put("id", s.id)
                obj.put("manuscriptId", s.manuscriptId)
                obj.put("title", s.title)
                obj.put("originalKey", s.originalKey)
                obj.put("content", s.content)
                obj.put("savedKey", s.savedKey ?: org.json.JSONObject.NULL)
                obj.put("sortOrder", s.sortOrder)
                songChartsArr.put(obj)
            }
            root.put("songCharts", songChartsArr)

            val repertoiresArr = org.json.JSONArray()
            for (r in repertoires) {
                val obj = org.json.JSONObject()
                obj.put("id", r.id)
                obj.put("name", r.name)
                obj.put("manuscriptIdsJson", r.manuscriptIdsJson)
                repertoiresArr.put(obj)
            }
            root.put("repertoires", repertoiresArr)

            val categoriesArr = org.json.JSONArray()
            for (c in categories) {
                val obj = org.json.JSONObject()
                obj.put("id", c.id)
                obj.put("repertoireId", c.repertoireId)
                obj.put("name", c.name)
                obj.put("position", c.position)
                categoriesArr.put(obj)
            }
            root.put("categories", categoriesArr)
            
            val songsArr = org.json.JSONArray()
            for (s in songs) {
                val obj = org.json.JSONObject()
                obj.put("id", s.id)
                obj.put("repertoireId", s.repertoireId)
                obj.put("categoryId", s.categoryId ?: org.json.JSONObject.NULL)
                obj.put("songChartId", s.songChartId)
                obj.put("position", s.position)
                obj.put("customKey", s.customKey ?: org.json.JSONObject.NULL)
                songsArr.put(obj)
            }
            root.put("songs", songsArr)

            return root.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    suspend fun importBackup(jsonString: String) {
        try {
            val root = org.json.JSONObject(jsonString)
            
            val mArr = root.getJSONArray("manuscripts")
            val manuscriptsList = mutableListOf<Manuscript>()
            for (i in 0 until mArr.length()) {
                val obj = mArr.getJSONObject(i)
                manuscriptsList.add(
                    Manuscript(
                        id = obj.getInt("id"),
                        title = obj.getString("title"),
                        composer = obj.getString("composer"),
                        category = obj.getString("category"),
                        coverUrl = obj.getString("coverUrl"),
                        isFavorite = obj.getBoolean("isFavorite"),
                        lastUsedTimestamp = obj.getLong("lastUsedTimestamp"),
                        keySignature = obj.getString("keySignature"),
                        era = obj.getString("era"),
                        localUri = if (obj.isNull("localUri")) null else obj.getString("localUri"),
                        extractedText = obj.getString("extractedText"),
                        capo = obj.getInt("capo"),
                        tone = obj.getString("tone"),
                        liturgicalNotes = obj.getString("liturgicalNotes")
                    )
                )
            }
            
            val scArr = root.getJSONArray("songCharts")
            val songChartsList = mutableListOf<SongChart>()
            for (i in 0 until scArr.length()) {
                val obj = scArr.getJSONObject(i)
                songChartsList.add(
                    SongChart(
                        id = obj.getInt("id"),
                        manuscriptId = obj.getInt("manuscriptId"),
                        title = obj.getString("title"),
                        originalKey = obj.getString("originalKey"),
                        content = obj.getString("content"),
                        savedKey = if (obj.isNull("savedKey")) null else obj.getString("savedKey"),
                        sortOrder = obj.getInt("sortOrder")
                    )
                )
            }

            val rArr = root.getJSONArray("repertoires")
            val repertoiresList = mutableListOf<Repertoire>()
            for (i in 0 until rArr.length()) {
                val obj = rArr.getJSONObject(i)
                repertoiresList.add(
                    Repertoire(
                        id = obj.getInt("id"),
                        name = obj.getString("name"),
                        manuscriptIdsJson = obj.getString("manuscriptIdsJson")
                    )
                )
            }

            val cArr = root.getJSONArray("categories")
            val categoriesList = mutableListOf<RepertoireCategory>()
            for (i in 0 until cArr.length()) {
                val obj = cArr.getJSONObject(i)
                categoriesList.add(
                    RepertoireCategory(
                        id = obj.getInt("id"),
                        repertoireId = obj.getInt("repertoireId"),
                        name = obj.getString("name"),
                        position = obj.getInt("position")
                    )
                )
            }

            val sArr = root.getJSONArray("songs")
            val songsList = mutableListOf<RepertoireSong>()
            for (i in 0 until sArr.length()) {
                val obj = sArr.getJSONObject(i)
                songsList.add(
                    RepertoireSong(
                        id = obj.getInt("id"),
                        repertoireId = obj.getInt("repertoireId"),
                        categoryId = if (obj.isNull("categoryId")) null else obj.getInt("categoryId"),
                        songChartId = obj.getInt("songChartId"),
                        position = obj.getInt("position"),
                        customKey = if (obj.isNull("customKey")) null else obj.getString("customKey")
                    )
                )
            }

            // If we got here without exception, JSON is valid. Delete existing and insert new.
            manuscriptDao.deleteAll()
            songChartDao.deleteAll()
            repertoireDao.deleteAll()
            repertoireCategoryDao.deleteAll()
            repertoireSongDao.deleteAll()

            manuscriptDao.insertAll(manuscriptsList)
            songChartDao.insertAll(songChartsList)
            repertoireDao.insertAll(repertoiresList)
            repertoireCategoryDao.insertAll(categoriesList)
            repertoireSongDao.insertAll(songsList)
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("Invalid backup file format", e)
        }
    }
}
