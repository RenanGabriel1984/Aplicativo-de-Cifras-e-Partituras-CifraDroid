package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.Xml
import kotlinx.coroutines.sync.withLock
import org.xmlpull.v1.XmlPullParser
import java.io.File
import java.util.zip.ZipFile

sealed class DocumentContent {
    data class PdfDoc(val engine: PdfReaderEngine) : DocumentContent()
    data class HtmlDoc(val html: String) : DocumentContent()
    data class TextDoc(val text: String) : DocumentContent()
}

object DocumentReader {
    fun loadDocument(context: Context, localUri: String?): DocumentContent? {
        if (localUri.isNullOrBlank()) return null
        val file = File(localUri)
        if (!file.exists()) return null

        return try {
            if (file.name.endsWith(".pdf", ignoreCase = true)) {
                DocumentContent.PdfDoc(PdfReaderEngine(file))
            } else if (file.name.endsWith(".docx", ignoreCase = true) || file.name.endsWith(".doc", ignoreCase = true)) {
                val html = DocxToHtmlConverter.convertToHtml(file)
                DocumentContent.HtmlDoc(html)
            } else {
                // Fallback to purely reading string
                DocumentContent.TextDoc(file.readText())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
