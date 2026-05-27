package com.example.util

import android.content.Context
import android.graphics.Color
import android.graphics.pdf.PdfDocument
import android.text.Html
import android.text.StaticLayout
import android.text.TextPaint
import java.io.File
import java.io.FileOutputStream

object DocxToPdfConverter {
    fun convertToPdf(context: Context, docxFile: File): File? {
        try {
            val html = DocxToHtmlConverter.convertToHtml(docxFile)
            val spanned = Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT)
            
            val pdfDocument = PdfDocument()
            val paint = TextPaint().apply {
                color = Color.BLACK
                textSize = 24f
            }
            
            val pageWidth = 595 // A4
            val pageHeight = 842 // A4
            val padding = 40
            val maxWidth = pageWidth - 2 * padding
            
            val staticLayout = StaticLayout.Builder.obtain(spanned, 0, spanned.length, paint, maxWidth).build()
            
            // How many pages?
            var yOffset = 0
            var pageNumber = 1
            val contentHeight = pageHeight - 2 * padding
            
            while (yOffset < staticLayout.height) {
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas
                
                canvas.drawColor(Color.WHITE)
                canvas.translate(padding.toFloat(), padding.toFloat() - yOffset)
                
                // Draw only the portion visible on this page. StaticLayout doesn't support drawing a slice directly, 
                // but setting a clip rect works.
                canvas.save()
                canvas.clipRect(0, yOffset, maxWidth, yOffset + contentHeight)
                staticLayout.draw(canvas)
                canvas.restore()
                
                pdfDocument.finishPage(page)
                yOffset += contentHeight
                pageNumber++
            }
            
            val pdfFile = File(context.filesDir, "converted_${System.currentTimeMillis()}.pdf")
            FileOutputStream(pdfFile).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()
            return pdfFile
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
