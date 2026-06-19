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
data class AnnotationPoint(val x: Float, val y: Float)

@JsonClass(generateAdapter = true)
data class PdfAnnotation(
    val id: String = java.util.UUID.randomUUID().toString(),
    val page: Int,
    val type: String, // "PEN", "HIGHLIGHT", "TEXT"
    val x: Float = 0f,
    val y: Float = 0f,
    val width: Float = 0f,
    val height: Float = 0f,
    val color: String? = null,
    val text: String? = null,
    val points: List<AnnotationPoint>? = null
)

@JsonClass(generateAdapter = true)
data class PdfAnnotationList(val annotations: List<PdfAnnotation>)

object PdfAnnotationManager {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val adapter = moshi.adapter(PdfAnnotationList::class.java)

    private fun getHash(uriStr: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(uriStr.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    private fun getAnnotationFile(context: Context, uriStr: String): File {
        val dir = File(context.filesDir, "annotations")
        if (!dir.exists()) dir.mkdirs()
        return File(dir, "${getHash(uriStr)}.json")
    }

    suspend fun loadAnnotations(context: Context, uriStr: String): List<PdfAnnotation> = withContext(Dispatchers.IO) {
        try {
            val file = getAnnotationFile(context, uriStr)
            if (file.exists()) {
                val json = file.readText()
                val list = adapter.fromJson(json)
                list?.annotations ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun saveAnnotations(context: Context, uriStr: String, annotations: List<PdfAnnotation>) = withContext(Dispatchers.IO) {
        try {
            val file = getAnnotationFile(context, uriStr)
            val json = adapter.toJson(PdfAnnotationList(annotations))
            file.writeText(json)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
