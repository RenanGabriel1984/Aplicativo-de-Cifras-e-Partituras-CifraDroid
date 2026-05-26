package com.example.util

import java.io.File

object PdfTextExtractor {
    fun extractText(file: File): String {
        // PDFBox/iText or similar is needed for deep PDF extraction.
        // For Android native, text extraction is not built into PdfRenderer.
        // We will do a generic binary text scrape as a lightweight fallback for indexing.
        try {
            val length = minOf(file.length(), 2 * 1024 * 1024).toInt()
            val stream = file.inputStream()
            val buffer = ByteArray(length)
            val bytesRead = stream.read(buffer)
            stream.close()
            if (bytesRead > 0) {
                // Return only printable characters and limit length
                val text = String(buffer, 0, bytesRead, Charsets.UTF_8)
                    .replace(Regex("[^\\p{L}\\p{N} ]"), " ")
                return text.take(2000)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }
}
