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

    fun extractTextByPage(context: Context, file: File): List<PageText> {
        return try {
            PDFBoxResourceLoader.init(context.applicationContext)
            val pages = mutableListOf<PageText>()
            PDDocument.load(file).use { document ->
                val totalPages = document.numberOfPages
                val stripper = PDFTextStripper()
                stripper.sortByPosition = true
                
                for (i in 1..totalPages) {
                    stripper.startPage = i
                    stripper.endPage = i
                    val pageText = stripper.getText(document) ?: ""
                    // Pages in model are usually 0-indexed, but PDFTextStripper uses 1-indexed. Let's make page class 0-indexed as requested by standard indexing.
                    pages.add(PageText(page = i - 1, text = pageText))
                }
            }
            pages
        } catch (e: Exception) {
            Log.e("PdfTextExtractor", "Erro na extração de texto por página", e)
            emptyList()
        }
    }
}
