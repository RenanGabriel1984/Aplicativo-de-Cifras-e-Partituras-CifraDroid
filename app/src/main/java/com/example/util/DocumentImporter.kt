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
            
            return@withContext Manuscript(
                title = title,
                composer = "Importado",
                category = if (fileName.endsWith(".pdf", true)) "PDF" else "DOC",
                coverUrl = "", // Empty indicates local file
                localUri = destinationFile.absolutePath,
                isFavorite = false,
                lastUsedTimestamp = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
