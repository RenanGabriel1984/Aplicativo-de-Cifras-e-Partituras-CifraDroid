package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.Repertoire
import com.example.data.RepertoireSong
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@JsonClass(generateAdapter = true)
data class SharedRepertoire(
    val version: Int = 1,
    val name: String,
    val categories: List<SharedRepertoireCategory>
)

@JsonClass(generateAdapter = true)
data class SharedRepertoireCategory(
    val name: String,
    val songs: List<SharedRepertoireSong>
)

@JsonClass(generateAdapter = true)
data class SharedRepertoireSong(
    val title: String,
    val originalKey: String,
    val customKey: String?,
    val lineCount: Int? = null,
    val firstLine: String? = null
)

data class ImportResult(
    val repertoireName: String,
    val totalSongs: Int,
    val foundSongs: Int,
    val missingSongNames: List<String>,
    val repertoire: Repertoire,
    val categories: List<RepertoireUtil.RepertoireCategory>,
    val songsToInsert: List<RepertoireSong>
)

object RepertoireShareManager {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val adapter = moshi.adapter(SharedRepertoire::class.java)

    suspend fun exportRepertoire(
        context: Context,
        repertoire: Repertoire,
        repertoireSongs: List<RepertoireSong>,
        categories: List<RepertoireUtil.RepertoireCategory>,
        allSongCharts: List<com.example.data.SongChart>
    ) = withContext(Dispatchers.IO) {
        
        val sharedCategories = categories.map { cat ->
            SharedRepertoireCategory(
                name = cat.name,
                songs = cat.manuscriptIds.mapNotNull { id ->
                    val songChart = allSongCharts.find { it.id == id } ?: return@mapNotNull null
                    val validLines = songChart.content.lines().filter { it.isNotBlank() }
                    SharedRepertoireSong(
                        title = songChart.title,
                        originalKey = songChart.originalKey,
                        customKey = repertoireSongs.find { it.songChartId == id }?.customKey,
                        lineCount = validLines.size,
                        firstLine = validLines.firstOrNull() ?: ""
                    )
                }
            )
        }

        val sharedRepertoire = SharedRepertoire(
            name = repertoire.name,
            categories = sharedCategories
        )
        
        val jsonString = adapter.toJson(sharedRepertoire)
        
        val cacheDir = File(context.cacheDir, "repertoires")
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        val file = File(cacheDir, "repertorio_${System.currentTimeMillis()}.json")
        FileOutputStream(file).use { it.write(jsonString.toByteArray()) }
        
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Repertório: ${repertoire.name}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        withContext(Dispatchers.Main) {
            context.startActivity(Intent.createChooser(intent, "Compartilhar Repertório").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        }
    }
    
    suspend fun importRepertoire(
        context: Context,
        uri: Uri,
        allSongCharts: List<com.example.data.SongChart>
    ): ImportResult? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            
            val sharedRepertoire = adapter.fromJson(jsonString) ?: return@withContext null
            
            if (sharedRepertoire.version != 1) return@withContext null
            
            val mappedCategories = mutableListOf<RepertoireUtil.RepertoireCategory>()
            val foundSongs = mutableListOf<RepertoireSong>()
            val missingSongNames = mutableListOf<String>()
            var totalSongs = 0
            
            for (cat in sharedRepertoire.categories) {
                val catIds = mutableListOf<Int>()
                for (song in cat.songs) {
                    totalSongs++
                    
                    var matchedChartId: Int? = null
                    val matchingTitles = allSongCharts.filter { it.title.equals(song.title, ignoreCase = true) }
                    
                    if (matchingTitles.size == 1) {
                        matchedChartId = matchingTitles.first().id
                    } else if (matchingTitles.size > 1) {
                        val matchingKey = matchingTitles.filter { it.originalKey == song.originalKey }
                        if (matchingKey.size == 1) {
                            matchedChartId = matchingKey.first().id
                        } else if (matchingKey.size > 1) {
                            var matchingLines = matchingKey
                            if (song.lineCount != null) {
                                val withSameCount = matchingKey.filter { 
                                    it.content.lines().filter { l -> l.isNotBlank() }.size == song.lineCount 
                                }
                                if (withSameCount.size == 1) {
                                    matchedChartId = withSameCount.first().id
                                } else if (withSameCount.size > 1) {
                                    matchingLines = withSameCount
                                }
                            }
                            
                            if (matchedChartId == null && song.firstLine != null) {
                                val withSameFirstLine = matchingLines.filter { 
                                    (it.content.lines().firstOrNull { l -> l.isNotBlank() } ?: "") == song.firstLine 
                                }
                                if (withSameFirstLine.size == 1) {
                                    matchedChartId = withSameFirstLine.first().id
                                } // If size > 1, it remains ambiguous and is ignored
                            }
                        }
                    }
                    
                    if (matchedChartId != null) {
                        catIds.add(matchedChartId)
                        foundSongs.add(
                            RepertoireSong(
                                repertoireId = 0,
                                categoryId = null,
                                songChartId = matchedChartId,
                                position = 0,
                                customKey = song.customKey
                            )
                        )
                    } else {
                        missingSongNames.add(song.title)
                    }
                }
                mappedCategories.add(RepertoireUtil.RepertoireCategory(cat.name, catIds))
            }
            
            val newRepertoire = Repertoire(name = sharedRepertoire.name, manuscriptIdsJson = "[]")
            
            ImportResult(
                repertoireName = sharedRepertoire.name,
                totalSongs = totalSongs,
                foundSongs = foundSongs.size,
                missingSongNames = missingSongNames,
                repertoire = newRepertoire,
                categories = mappedCategories,
                songsToInsert = foundSongs
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
