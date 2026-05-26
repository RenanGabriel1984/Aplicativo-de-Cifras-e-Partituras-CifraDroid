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
                loadDocx(file) // we'll try treating doc as docx for now, or just extracting strings simply
            } else {
                // Fallback to purely reading string
                DocumentContent.TextDoc(file.readText())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun loadDocx(file: File): DocumentContent.TextDoc {
        val sb = java.lang.StringBuilder()
        try {
            val zip = ZipFile(file)
            val docEntry = zip.getEntry("word/document.xml")
            if (docEntry != null) {
                val inputStream = zip.getInputStream(docEntry)
                val parser = Xml.newPullParser()
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
                parser.setInput(inputStream, null)

                var eventType = parser.eventType
                var inText = false
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    when (eventType) {
                        XmlPullParser.START_TAG -> {
                            val name = parser.name
                            if (name == "w:t") {
                                inText = true
                            } else if (name == "w:p") {
                                sb.append("\n")
                            } else if (name == "w:tab") {
                                sb.append("\t")
                            } else if (name == "w:br") {
                                sb.append("\n")
                            }
                        }
                        XmlPullParser.TEXT -> {
                            if (inText) {
                                sb.append(parser.text)
                            }
                        }
                        XmlPullParser.END_TAG -> {
                            if (parser.name == "w:t") {
                                inText = false
                            }
                        }
                    }
                    eventType = parser.next()
                }
                inputStream.close()
            }
            zip.close()
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback for .doc or failed docx: try reading purely human readable strings
            // Limit to first 2MB to prevent OOM
            val text = try {
                file.inputStream().use { stream ->
                    val buffer = ByteArray(2 * 1024 * 1024)
                    val bytesRead = stream.read(buffer)
                    if (bytesRead > 0) {
                        String(buffer, 0, bytesRead, Charsets.UTF_8).replace(Regex("[^\\p{L}\\p{N}\\p{P}\\p{Z}\\n\\r]"), "")
                    } else ""
                }
            } catch (ex: Exception) { "" }
            return DocumentContent.TextDoc(text.trim())
        }
        return DocumentContent.TextDoc(sb.toString().trim())
    }
}
