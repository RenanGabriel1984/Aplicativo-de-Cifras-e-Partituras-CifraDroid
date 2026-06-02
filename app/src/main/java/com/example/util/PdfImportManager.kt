package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import com.example.data.Manuscript
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object PdfImportManager {

    suspend fun importFromUri(context: Context, uri: Uri): Manuscript? = withContext(Dispatchers.IO) {
        kotlinx.coroutines.withTimeoutOrNull(15000L) {
            try {
                // First try to persist permission if it's a SAF Uri
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: Exception) {
                    // Ignore if not a SAF URI
                }

                // Extract file name
                var fileName = "Imported.pdf"
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1) fileName = cursor.getString(nameIndex)
                    }
                }
                if (fileName.isEmpty()) fileName = "Unknown_Document"
                
                val title = fileName.substringBeforeLast(".")

                // Copy to internal cache for physical guaranteed access
                val destinationFile = persistPdf(context, uri, fileName)
                    ?: return@withTimeoutOrNull null

                val coverPath = generateThumbnail(context, destinationFile)
                val extractedText = PdfTextExtractor.extractText(destinationFile)

                Manuscript(
                    title = title,
                    composer = "Desconhecido",
                    category = "Repertórios", // default fallback
                    coverUrl = coverPath,
                    localUri = destinationFile.absolutePath,
                    isFavorite = false,
                    lastUsedTimestamp = System.currentTimeMillis(),
                    extractedText = extractedText
                )
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

        private fun persistPdf(context: Context, uri: Uri, fileName: String): File? {
        return try {
            val extension = fileName.substringAfterLast(".", "pdf")
            val destinationFile = File(context.filesDir, "doc_${System.currentTimeMillis()}_$fileName")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destinationFile).use { output ->
                    input.copyTo(output)
                }
            }
            if (extension.equals("docx", ignoreCase = true) || extension.equals("doc", ignoreCase = true)) {
                val pdfFile = DocxToPdfConverter.convertToPdf(context, destinationFile)
                if (pdfFile != null) {
                    destinationFile.delete() // remove docx
                    return pdfFile
                }
            }
            destinationFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun generateThumbnail(context: Context, file: File): String {
        if (!file.name.endsWith(".pdf", ignoreCase = true)) return ""
        android.util.Log.d("PdfImport", "CAPA_INICIO: Gerando capa para ${file.name}")
        try {
            val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(pfd)
            var coverPath = ""
            if (renderer.pageCount > 0) {
                val page = renderer.openPage(0)
                try {
                    val width = (page.width * 0.5).toInt().coerceAtLeast(1)
                    val height = (page.height * 0.5).toInt().coerceAtLeast(1)
                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    
                    val canvas = android.graphics.Canvas(bitmap)
                    canvas.drawColor(android.graphics.Color.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    android.util.Log.d("PdfImport", "CAPA_GERADA: Capa renderizada com sucesso (${width}x${height})")

                    val coverFile = File(context.filesDir, "cover_${System.currentTimeMillis()}.png")
                    FileOutputStream(coverFile).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                    coverPath = "file://${coverFile.absolutePath}"
                    android.util.Log.d("PdfImport", "CAPA_SALVA: Capa salva em $coverPath")
                    bitmap.recycle()
                } catch (t: Throwable) {
                    android.util.Log.e("PdfImport", "CAPA_ERRO: Falha ao desenhar/salvar bitmap da thumbnail", t)
                } finally {
                    page.close()
                }
            }
            renderer.close()
            pfd.close()
            return coverPath
        } catch (t: Throwable) {
            android.util.Log.e("PdfImport", "CAPA_ERRO: Erro geral na geração da capa", t)
            return ""
        }
    }
}
