package com.example.util

import android.content.Context
import java.io.File

sealed class DocumentContent {
    data class PdfDoc(val engine: PdfReaderEngine) : DocumentContent()
}

object DocumentReader {
    fun loadDocument(context: Context, localUri: String?): DocumentContent? {
        if (localUri.isNullOrBlank()) return null
        val file = File(localUri)
        if (!file.exists()) return null

        return try {
            if (file.name.endsWith(".pdf", ignoreCase = true)) {
                DocumentContent.PdfDoc(PdfReaderEngine(file))
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
