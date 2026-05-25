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
    data class PdfDoc(val wrapper: PdfRendererWrapper) : DocumentContent()
    data class TextDoc(val text: String) : DocumentContent()
}

class PdfRendererWrapper(val file: File) {
    private var pfd: ParcelFileDescriptor? = null
    private var renderer: PdfRenderer? = null
    private val lock = Any()
    
    var pageCount: Int = 0
        private set
        
    init {
        pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        renderer = PdfRenderer(pfd!!)
        pageCount = renderer!!.pageCount
    }
    
    suspend fun renderPage(pageIndex: Int): Bitmap? = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        synchronized(lock) {
            if (pageIndex < 0 || pageIndex >= pageCount) return@synchronized null
            val r = renderer ?: return@synchronized null
            try {
                val page = r.openPage(pageIndex)
                try {
                    // Try high-resolution rendering, fallback to lower if OOM occurs
                    var bitmap: Bitmap? = null
                    val scaleMultipliers = listOf(2.0, 1.5, 1.0)
                    for (scale in scaleMultipliers) {
                        try {
                            bitmap = Bitmap.createBitmap(
                                (page.width * scale).toInt(),
                                (page.height * scale).toInt(),
                                Bitmap.Config.ARGB_8888
                            )
                            break
                        } catch (e: OutOfMemoryError) {
                            // Try next lower scale
                        }
                    }
                    if (bitmap == null) return@synchronized null // Cannot allocate even at lowest scale

                    val canvas = android.graphics.Canvas(bitmap)
                    canvas.drawColor(android.graphics.Color.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    bitmap
                } finally {
                    page.close()
                }
            } catch (t: Throwable) {
                t.printStackTrace()
                null
            }
        }
    }
    
    fun close() {
        synchronized(lock) {
            try { renderer?.close() } catch (e: Exception) {}
            try { pfd?.close() } catch (e: Exception) {}
            renderer = null
            pfd = null
        }
    }
}

object DocumentReader {
    fun loadDocument(context: Context, localUri: String?): DocumentContent? {
        if (localUri.isNullOrBlank()) return null
        val file = File(localUri)
        if (!file.exists()) return null

        return try {
            if (file.name.endsWith(".pdf", ignoreCase = true)) {
                DocumentContent.PdfDoc(PdfRendererWrapper(file))
            } else if (file.name.endsWith(".docx", ignoreCase = true) || file.name.endsWith(".doc", ignoreCase = true)) {
                loadDocx(file) // we'll try treating doc as docx for now, or just extracting strings simply
            } else {
                null
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
                                sb.append("\n\n")
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
                        String(buffer, 0, bytesRead, Charsets.US_ASCII).replace(Regex("[^\\x20-\\x7E\\r\\n]"), "")
                    } else ""
                }
            } catch (ex: Exception) { "" }
            return DocumentContent.TextDoc(text.trim())
        }
        return DocumentContent.TextDoc(sb.toString().trim())
    }
}
