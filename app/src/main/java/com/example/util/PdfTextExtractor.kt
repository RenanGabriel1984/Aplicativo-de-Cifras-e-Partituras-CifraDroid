package com.example.util

import android.content.Context
import android.util.Log
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.io.File

object PdfTextExtractor {
    fun extractText(context: Context, file: File): String {
        return try {
            PDFBoxResourceLoader.init(context.applicationContext)
            var text = ""
            PDDocument.load(file).use { document ->
                val stripper = PDFTextStripper()
                stripper.sortByPosition = true
                text = stripper.getText(document) ?: ""
            }
            
            Log.d("PdfTextExtractor", "PDF_TEXT_EXTRACTION_SUCCESS: Extraídos ${text.length} caracteres.")
            Log.d("PdfTextExtractor", "PDF_TEXT_EXTRACTION_SUCCESS: Primeiras 10 linhas:\n${text.lines().take(10).joinToString("\n")}")
            
            text
        } catch (e: Exception) {
            Log.e("PdfTextExtractor", "Erro na extração de texto do PDF", e)
            ""
        }
    }
}
