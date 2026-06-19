package com.example.util

import android.content.Context
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest

@JsonClass(generateAdapter = true)
data class PdfBookmark(
    val id: String = java.util.UUID.randomUUID().toString(),
    val pdfHash: String,
    val name: String,
    val page: Int,
    val verticalOffset: Int = 0,
    val color: String = "#2196F3"
)

@JsonClass(generateAdapter = true)
data class PdfBookmarkList(val bookmarks: List<PdfBookmark>)

object PdfBookmarkManager {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val adapter = moshi.adapter(PdfBookmarkList::class.java)

    fun getHash(uriStr: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(uriStr.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    private fun getBookmarkFile(context: Context, uriStr: String): File {
        val dir = File(context.filesDir, "bookmarks")
        if (!dir.exists()) dir.mkdirs()
        return File(dir, "${getHash(uriStr)}.json")
    }

    suspend fun loadBookmarks(context: Context, uriStr: String): List<PdfBookmark> = withContext(Dispatchers.IO) {
        try {
            val file = getBookmarkFile(context, uriStr)
            if (file.exists()) {
                val json = file.readText()
                val list = adapter.fromJson(json)
                (list?.bookmarks ?: emptyList()).sortedWith(compareBy({ it.page }, { it.verticalOffset }))
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun saveBookmarks(context: Context, uriStr: String, bookmarks: List<PdfBookmark>) = withContext(Dispatchers.IO) {
        try {
            val file = getBookmarkFile(context, uriStr)
            val json = adapter.toJson(PdfBookmarkList(bookmarks))
            file.writeText(json)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
