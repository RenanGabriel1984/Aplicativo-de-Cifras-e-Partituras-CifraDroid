package com.example.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.data.Manuscript
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object DocumentImporter {
    suspend fun importDocument(context: Context, uri: Uri): Manuscript? = withContext(Dispatchers.IO) {
        try {
            var fileName = "Documento"
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (displayNameIndex >= 0) {
                        fileName = cursor.getString(displayNameIndex)
                    }
                }
            }

            // Remove extension for title
            val title = fileName.substringBeforeLast(".")
            
            // Create a local copy
            val destinationFile = File(context.filesDir, "imported_${System.currentTimeMillis()}_$fileName")
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(destinationFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            
            // Extract cover/thumbnail and optional text
            var coverPath = ""
            var textExtracted = ""
            if (destinationFile.name.endsWith(".pdf", ignoreCase = true)) {
                try {
                    val pfd = android.os.ParcelFileDescriptor.open(destinationFile, android.os.ParcelFileDescriptor.MODE_READ_ONLY)
                    val renderer = android.graphics.pdf.PdfRenderer(pfd)
                    if (renderer.pageCount > 0) {
                        val page = renderer.openPage(0)
                        val bitmap = android.graphics.Bitmap.createBitmap((page.width * 0.5).toInt().coerceAtLeast(1), (page.height * 0.5).toInt().coerceAtLeast(1), android.graphics.Bitmap.Config.ARGB_8888)
                        val canvas = android.graphics.Canvas(bitmap)
                        canvas.drawColor(android.graphics.Color.WHITE)
                        page.render(bitmap, null, null, android.graphics.pdf.PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        
                        val coverFile = File(context.filesDir, "cover_${System.currentTimeMillis()}.png")
                        FileOutputStream(coverFile).use { out ->
                            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
                        }
                        coverPath = coverFile.absolutePath
                        page.close()
                    }
                    renderer.close()
                    pfd.close()
                } catch (e: Exception) { e.printStackTrace() }
            } else {
                // Try reading text fallback (already handled partially in DocumentReader, we'll just extract a chunk for indexing)
                try {
                    textExtracted = destinationFile.readText(Charsets.UTF_8).take(1000)
                } catch(e:Exception){}
            }
            
            return@withContext Manuscript(
                title = title,
                composer = "Desconhecido",
                category = "Entrada", // default fallback
                coverUrl = coverPath,
                localUri = destinationFile.absolutePath,
                isFavorite = false,
                lastUsedTimestamp = System.currentTimeMillis(),
                extractedText = textExtracted
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
