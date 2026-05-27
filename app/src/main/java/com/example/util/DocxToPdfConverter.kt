package com.example.util

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.StaticLayout
import android.text.TextPaint
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipFile
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element

object DocxToPdfConverter {

    private fun extractRichTextFromDocx(file: File): android.text.SpannableStringBuilder {
        val builder = android.text.SpannableStringBuilder()
        try {
            val zipFile = ZipFile(file)
            val docEntry = zipFile.getEntry("word/document.xml") ?: return builder
            val inputStream = zipFile.getInputStream(docEntry)
            
            val factory = DocumentBuilderFactory.newInstance()
            val parser = factory.newDocumentBuilder()
            val document = parser.parse(inputStream)
            
            val bodyList = document.getElementsByTagName("w:body")
            if (bodyList.length > 0) {
                val body = bodyList.item(0) as Element
                val paragraphs = body.getElementsByTagName("w:p")
                for (i in 0 until paragraphs.length) {
                    val p = paragraphs.item(i) as Element
                    
                    val pPrList = p.getElementsByTagName("w:pPr")
                    var align = "left"
                    if (pPrList.length > 0) {
                        val pPr = pPrList.item(0) as Element
                        val jcList = pPr.getElementsByTagName("w:jc")
                        if (jcList.length > 0) {
                            align = (jcList.item(0) as Element).getAttribute("w:val")
                        }
                    }
                    
                    val pStart = builder.length
                    
                    val runs = p.getElementsByTagName("w:r")
                    for (j in 0 until runs.length) {
                        val r = runs.item(j) as Element
                        
                        var isBold = false
                        var isItalic = false
                        var hexColor = "#000000"
                        var textSize = -1f
                        
                        val rPrList = r.getElementsByTagName("w:rPr")
                        if (rPrList.length > 0) {
                            val rPr = rPrList.item(0) as Element
                            if (rPr.getElementsByTagName("w:b").length > 0) isBold = true
                            if (rPr.getElementsByTagName("w:i").length > 0) isItalic = true
                            
                            val colorList = rPr.getElementsByTagName("w:color")
                            if (colorList.length > 0) {
                                val colorVal = (colorList.item(0) as Element).getAttribute("w:val")
                                if (colorVal.isNotEmpty() && colorVal != "auto") {
                                    hexColor = "#$colorVal"
                                }
                            }
                            
                            val sizeList = rPr.getElementsByTagName("w:sz")
                            if (sizeList.length > 0) {
                                val szVal = (sizeList.item(0) as Element).getAttribute("w:val").toFloatOrNull()
                                if (szVal != null) {
                                    textSize = szVal / 2f // Word sizes are in half-points
                                }
                            }
                        }
                        
                        val children = r.childNodes
                        for (k in 0 until children.length) {
                            val node = children.item(k)
                            val nodeName = node.nodeName
                            
                            val startOpt = builder.length
                            var didAppend = false
                            
                            if (nodeName == "w:t") {
                                builder.append(node.textContent)
                                didAppend = true
                            } else if (nodeName == "w:tab") {
                                builder.append("    ")
                                didAppend = true
                            } else if (nodeName == "w:br") {
                                builder.append("\n")
                                didAppend = true
                            }
                            
                            if (didAppend) {
                                val endOpt = builder.length
                                val flags = android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                
                                if (isBold || isItalic) {
                                    val style = if (isBold && isItalic) android.graphics.Typeface.BOLD_ITALIC
                                                else if (isBold) android.graphics.Typeface.BOLD
                                                else android.graphics.Typeface.ITALIC
                                    builder.setSpan(android.text.style.StyleSpan(style), startOpt, endOpt, flags)
                                }
                                
                                try {
                                    val colorInt = android.graphics.Color.parseColor(hexColor)
                                    if (colorInt != android.graphics.Color.BLACK) {
                                        builder.setSpan(android.text.style.ForegroundColorSpan(colorInt), startOpt, endOpt, flags)
                                    }
                                } catch (_: Exception) {}
                                
                                if (textSize > 0) {
                                    builder.setSpan(android.text.style.AbsoluteSizeSpan(textSize.toInt(), true), startOpt, endOpt, flags)
                                }
                            }
                        }
                    }
                    builder.append("\n")
                    
                    val pEnd = builder.length
                    val alignEnum = when (align) {
                        "center" -> android.text.Layout.Alignment.ALIGN_CENTER
                        "right" -> android.text.Layout.Alignment.ALIGN_OPPOSITE
                        else -> android.text.Layout.Alignment.ALIGN_NORMAL
                    }
                    builder.setSpan(android.text.style.AlignmentSpan.Standard(alignEnum), pStart, pEnd, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
            zipFile.close()
        } catch (e: Exception) {
            e.printStackTrace()
            builder.append("Erro ao ler DOCX.")
        }
        return builder
    }

    fun convertToPdf(context: Context, docxFile: File): File? {
        try {
            val richText = extractRichTextFromDocx(docxFile)
            
            val pdfDocument = PdfDocument()
            val paint = TextPaint().apply {
                color = Color.BLACK
                textSize = 14f // Base size
                typeface = Typeface.MONOSPACE
            }
            
            val pageWidth = 595 // A4
            val pageHeight = 842 // A4
            val padding = 40
            val maxWidth = pageWidth - 2 * padding
            
            val staticLayout = StaticLayout.Builder.obtain(richText, 0, richText.length, paint, maxWidth)
                .setAlignment(android.text.Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(0f, 1.2f)
                .build()
            
            var yOffset = 0
            var pageNumber = 1
            val contentHeight = pageHeight - 2 * padding
            
            while (yOffset < staticLayout.height) {
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas
                
                canvas.drawColor(Color.WHITE)
                canvas.translate(padding.toFloat(), padding.toFloat() - yOffset.toFloat())
                
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
