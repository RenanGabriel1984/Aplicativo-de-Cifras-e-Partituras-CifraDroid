package com.example.util

import java.io.File
import java.util.zip.ZipFile
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element

object DocxToHtmlConverter {
    fun convertToHtml(file: File): String {
        return try {
            val zipFile = ZipFile(file)
            val docEntry = zipFile.getEntry("word/document.xml") ?: return ""
            val inputStream = zipFile.getInputStream(docEntry)
            
            val factory = DocumentBuilderFactory.newInstance()
            val builder = factory.newDocumentBuilder()
            val document = builder.parse(inputStream)
            
            val htmlBuilder = StringBuilder()
            // Add a style block suitable for a dark/light responsive reader or specifically formatted sheet music
            htmlBuilder.append("<html><head>")
            htmlBuilder.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=5.0\">")
            htmlBuilder.append("<style>")
            htmlBuilder.append("body { font-family: sans-serif; padding: 16px; margin: 0; background-color: transparent; }")
            htmlBuilder.append("p { margin: 4px 0; }")
            htmlBuilder.append("</style></head><body>")
            
            val body = document.getElementsByTagName("w:body").item(0) as? Element
            if (body != null) {
                val paragraphs = body.getElementsByTagName("w:p")
                for (i in 0 until paragraphs.length) {
                    val p = paragraphs.item(i) as Element
                    
                    val pPrList = p.getElementsByTagName("w:pPr")
                    var align = "left"
                    if (pPrList.length > 0) {
                        val pPr = pPrList.item(0) as Element
                        val jcList = pPr.getElementsByTagName("w:jc")
                        if (jcList.length > 0) {
                            val jc = jcList.item(0) as Element
                            val valAttr = jc.getAttribute("w:val")
                            if (valAttr == "center") align = "center"
                            else if (valAttr == "right") align = "right"
                            else if (valAttr == "both") align = "justify"
                        }
                    }
                    htmlBuilder.append("<p style=\"text-align: $align;\">")
                    
                    val runs = p.getElementsByTagName("w:r")
                    for (j in 0 until runs.length) {
                        val r = runs.item(j) as Element
                        
                        var isBold = false
                        var isItalic = false
                        var color = ""
                        var fontSize = ""
                        
                        val rPrList = r.getElementsByTagName("w:rPr")
                        if (rPrList.length > 0) {
                            val rPr = rPrList.item(0) as Element
                            if (rPr.getElementsByTagName("w:b").length > 0) isBold = true
                            if (rPr.getElementsByTagName("w:i").length > 0) isItalic = true
                            
                            val colorList = rPr.getElementsByTagName("w:color")
                            if (colorList.length > 0) {
                                val c = (colorList.item(0) as Element).getAttribute("w:val")
                                if (c.isNotEmpty() && c != "auto") {
                                    color = "#$c"
                                }
                            }
                            
                            val szList = rPr.getElementsByTagName("w:sz")
                            if (szList.length > 0) {
                                val sz = (szList.item(0) as Element).getAttribute("w:val")
                                if (sz.isNotEmpty()) {
                                    fontSize = "${sz.toInt() / 2}pt" // docx sizes are in half-points
                                }
                            }
                        }
                        
                        var style = ""
                        if (color.isNotEmpty()) style += "color: $color; "
                        if (fontSize.isNotEmpty()) style += "font-size: $fontSize; "
                        
                        var textContent = ""
                        val tList = r.getElementsByTagName("w:t")
                        if (tList.length > 0) {
                            val textNode = tList.item(0)
                            textContent = textNode.textContent
                        }
                        
                        // Handle line breaks (w:br)
                        val brList = r.getElementsByTagName("w:br")
                        val hasBr = brList.length > 0
                        
                        if (textContent.isNotEmpty() || hasBr) {
                            var tag = "span"
                            if (style.isNotEmpty()) {
                                htmlBuilder.append("<span style=\"$style\">")
                            } else {
                                htmlBuilder.append("<span>")
                            }
                            if (isBold) htmlBuilder.append("<b>")
                            if (isItalic) htmlBuilder.append("<i>")
                            
                            if (textContent.isNotEmpty()) {
                                // Preserve spaces for chords
                                htmlBuilder.append(textContent.replace(" ", "&nbsp;").replace("<", "&lt;").replace(">", "&gt;"))
                            }
                            
                            if (isItalic) htmlBuilder.append("</i>")
                            if (isBold) htmlBuilder.append("</b>")
                            htmlBuilder.append("</span>")
                        }
                        
                        if (hasBr) {
                            for (k in 0 until brList.length) {
                                htmlBuilder.append("<br/>")
                            }
                        }
                    }
                    htmlBuilder.append("</p>")
                }
            }
            
            htmlBuilder.append("</body></html>")
            zipFile.close()
            htmlBuilder.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            "<html><body><p>Erro ao ler formatação DOCX.</p><pre>${e.message}</pre></body></html>"
        }
    }
}
