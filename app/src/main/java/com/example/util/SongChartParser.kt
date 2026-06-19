package com.example.util

import com.example.data.SongChart
import java.text.Normalizer

object SongChartParser {
    
    private val liturgicalKeywords = listOf(
        "ENTRADA", "ATO PENITENCIAL", "KYRIE", "GLORIA", "SALMO", "SEQUENCIA", "ALELUIA",
        "ACLAMACAO", "OFERTORIO", "SANTO", "ORACAO EUCARISTICA", "DOXOLOGIA",
        "CORDEIRO", "COMUNHAO", "VENI CREATOR SPIRITUS", "RITO PARA APAGAR O CIRIO", "FINAL"
    )

    private val chordRegex = Regex("^[A-G](?:#|b)?(?:m|min|maj|M|dim|aug|sus\\d?)?(?:\\d+)?(?:/[A-G](?:#|b)?)?$")

    private val structuralMarkers = setOf(
        "REFRAO", "PONTE", "INTRO", "INTRODUCAO", "SOLO", "FINAL", "CODA", 
        "INTERLUDIO", "BIS", "REPETE", "REFRAO FINAL", "CORO", "ESTROFE", "VERSO", "VERSE", "CHORUS", "BRIDGE", "INSTRUMENTAL"
    )

    private fun removeAccents(str: String): String {
        val normalized = java.text.Normalizer.normalize(str, java.text.Normalizer.Form.NFD)
        return Regex("\\p{InCombiningDiacriticalMarks}+").replace(normalized, "")
    }

    private fun isSongTitle(line: String, emptyLinesCount: Int, isFirst: Boolean): Boolean {
        if (line.isBlank()) return false
        val trimmed = line.trim()
        val upperLine = removeAccents(trimmed.uppercase())
        val words = trimmed.split(Regex("\\s+"))

        val cleanedWords = words.map { removeAccents(it.uppercase().replace(Regex("[^A-Z0-9]"), "")) }.filter { it.isNotEmpty() }
        if (cleanedWords.isNotEmpty()) {
            val wordsWithoutNumbers = cleanedWords.filter { word -> !word.all { c -> c.isDigit() } }
            val joinedWords = wordsWithoutNumbers.joinToString(" ")
            
            if (structuralMarkers.contains(joinedWords)) {
                android.util.Log.d("SongChartParser", "DESCARTADO (Título Estrutural): '$trimmed'")
                return false
            }
        }

        val startsWithKeyword = liturgicalKeywords.any { keyword -> upperLine.startsWith(keyword) }
        if (startsWithKeyword && words.size <= 8 && !trimmed.contains(Regex("[,.!?;:]"))) {
            android.util.Log.d("SongChartParser", "DETECTADO (Palavra Litúrgica): '$trimmed'")
            return true
        }

        if (words.size > 8) return false
        val chordCount = words.count { it.matches(chordRegex) }
        if (chordCount > 0 && chordCount.toDouble() / words.size > 0.3) {
            android.util.Log.d("SongChartParser", "DESCARTADO (Muitos Acordes): '$trimmed'")
            return false
        }
        if (trimmed.contains(Regex("[,.!?;:]"))) {
            android.util.Log.d("SongChartParser", "DESCARTADO (Pontuação): '$trimmed'")
            return false
        }
        if (trimmed.contains(Regex("[œ»≤≥═║≠|&=♩♪♫♬♭♯𝄞𝄢♮𝄆𝄇\\p{So}\\p{Sm}│┃┄┅┆┇┈┉┊┋┌┍┎┏┐┑┒┓└┕┖┗┘┙┚┛├┝┞┟┠┡┢┣┤┥┦┧┨┩┪┫┬┭┮┯┰┱┲┳┴┵┶┷┸┹┺┻┼┽┾┿╀╁╂╃╄╅╆╇╈╉╊╋╌╍╎╏═║╒╓╔╕╖╗╘╙╚╛╜╝╞╟╠╡╢╣╤╥╦╧╨╩╪╫╬━]"))) {
            android.util.Log.d("SongChartParser", "DESCARTADO (Caracteres Gráficos): '$trimmed'")
            return false
        }
        if (trimmed.count { it.isLetter() } < 2) return false
        if (trimmed.firstOrNull { it.isLetter() }?.isLowerCase() == true) return false

        val letters = trimmed.filter { it.isLetter() }
        val isAllUpperCase = letters.isNotEmpty() && letters.all { it.isUpperCase() }
        val startsWithNumber = trimmed.matches(Regex("^[0-9]+.*"))
        val precededByEmptySpace = emptyLinesCount >= 2 || isFirst

        val isTitle = isAllUpperCase || startsWithNumber || precededByEmptySpace
        
        if (isTitle) {
            android.util.Log.d("SongChartParser", "DETECTADO (Padrão de Título): '$trimmed'")
        }
        
        return isTitle
    }

    private fun detectOriginalKey(content: String): String {
        val lines = content.split("\n").take(20)
        val validRootChords = setOf(
            "C", "C#", "Db", "D", "D#", "Eb", "E", "F", "F#", "Gb", "G", "G#", "Ab", "A", "A#", "Bb", "B",
            "Cm", "C#m", "Dbm", "Dm", "D#m", "Ebm", "Em", "Fm", "F#m", "Gbm", "Gm", "G#m", "Abm", "Am", "A#m", "Bbm", "Bm"
        )
        
        val chordFrequencies = mutableMapOf<String, Int>()
        var firstValidChord: String? = null

        for (line in lines) {
            val words = line.split(Regex("[\\s()]+")).filter { it.isNotBlank() }
            for (word in words) {
                if (word.matches(chordRegex)) {
                    // Extract root chord (e.g. C#m7 -> C#m, Cmaj7 -> C, F# -> F#)
                    val baseChordMatch = Regex("^[A-G](?:#|b)?(?:m)?").find(word)
                    if (baseChordMatch != null) {
                        val root = baseChordMatch.value
                        if (validRootChords.contains(root)) {
                            if (firstValidChord == null) {
                                firstValidChord = root
                            }
                            chordFrequencies[root] = chordFrequencies.getOrDefault(root, 0) + 1
                        }
                    }
                }
            }
        }

        if (chordFrequencies.isEmpty()) return "?"

        val mostFrequent = chordFrequencies.maxByOrNull { it.value }?.key
        return mostFrequent ?: firstValidChord ?: "?"
    }

    private fun isValidBlock(content: String): Boolean {
        val lines = content.split("\n")
        val usefulLines = lines.count { it.trim().isNotEmpty() }
        
        var chordCount = 0
        var alphaCount = 0
        var totalChars = 0
        var graphicChars = 0
        var realWordCount = 0

        val graphicRegex = Regex("[œ»≤≥═║≠|=&♩♪♫♬♭♯𝄞𝄢♮𝄆𝄇\\p{So}\\p{Sm}│┃┄┅┆┇┈┉┊┋┌┍┎┏┐┑┒┓└┕┖┗┘┙┚┛├┝┞┟┠┡┢┣┤┥┦┧┨┩┪┫┬┭┮┯┰┱┲┳┴┵┶┷┸┹┺┻┼┽┾┿╀╁╂╃╄╅╆╇╈╉╊╋╌╍╎╏═║╒╓╔╕╖╗╘╙╚╛╜╝╞╟╠╡╢╣╤╥╦╧╨╩╪╫╬━]")

        for (line in lines) {
            val words = line.split(Regex("[\\s()]+")).filter { it.isNotBlank() }
            val chordsInLine = words.count { it.matches(chordRegex) }
            chordCount += chordsInLine
            
            val nonChordWords = words.filter { !it.matches(chordRegex) }
            realWordCount += nonChordWords.count { removeAccents(it).matches(Regex("[a-zA-Z]{3,}")) }
            
            var lineGraphicChars = 0
            var lineAlphaCount = 0
            var lineTotalChars = 0
            val lineSymbolsCount = line.count { !it.isLetterOrDigit() && !it.isWhitespace() }

            // FAIL FAST 1: sequências longas de símbolos
            if (line.contains(Regex("([^A-Za-z0-9\\s])\\1{4,}"))) {
                 android.util.Log.d("SongChartParser", "DESCARTADO: Linha contém sequência longa de símbolos")
                 return false
            }

            for (char in line) {
                if (!char.isWhitespace()) {
                    totalChars++
                    lineTotalChars++
                    if (char.isLetter()) {
                        alphaCount++
                        lineAlphaCount++
                    } else if (graphicRegex.matches(char.toString())) {
                        graphicChars++
                        lineGraphicChars++
                    }
                }
            }
            
            // FAIL FAST 2: Densidade gráfica excessiva na linha
            if (lineTotalChars > 0 && lineGraphicChars.toDouble() / lineTotalChars > 0.3) {
                android.util.Log.d("SongChartParser", "DESCARTADO: Linha com densidade gráfica excessiva (${lineGraphicChars.toDouble() / lineTotalChars})")
                return false
            }
            
            // FAIL FAST 3: Padrões típicos de pauta/MuseScore
            if (lineTotalChars > 10 && lineSymbolsCount.toDouble() / lineTotalChars > 0.5 && lineAlphaCount == 0 && chordsInLine == 0) {
                 android.util.Log.d("SongChartParser", "DESCARTADO: Linha suspeita (excesso de símbolos)")
                 return false
            }
        }
        
        if (totalChars > 0) {
            val alphaRatio = alphaCount.toDouble() / totalChars.toDouble()
            val graphicRatio = graphicChars.toDouble() / totalChars.toDouble()

            if (graphicRatio > 0.1 || graphicChars > 20) {
                android.util.Log.d("SongChartParser", "DESCARTADO: Excesso de caracteres gráficos ($graphicRatio, $graphicChars)")
                return false
            }

            if (alphaRatio < 0.3) {
                 android.util.Log.d("SongChartParser", "DESCARTADO: Densidade alfabética muito baixa ($alphaRatio)")
                 return false
            }
        }
        
        if (realWordCount < 1) {
             android.util.Log.d("SongChartParser", "DESCARTADO: Poucas palavras reais ($realWordCount)")
             return false
        }
        
        if (chordCount < 1) {
             android.util.Log.d("SongChartParser", "DESCARTADO: Poucos acordes ($chordCount)")
             return false
        }
        
        return usefulLines >= 3
    }

    fun parse(manuscriptId: Int, content: String): List<SongChart> {
        val charts = mutableListOf<SongChart>()
        val lines = content.split("\n")
        
        android.util.Log.d("SongChartParser", "Total de linhas recebidas: ${lines.size}")

        var currentTitle: String? = null
        val currentContent = StringBuilder()
        var sortOrder = 0

        val detectedHeaders = mutableListOf<String>()
        var emptyLinesCount = 0

        for (i in lines.indices) {
            val line = lines[i].trim()
            if (line.isEmpty()) {
                emptyLinesCount++
                if (currentContent.isNotEmpty()) currentContent.appendLine(lines[i])
                continue
            }

            val isFirst = currentTitle == null && currentContent.isEmpty()
            val isPotentialHeader = isSongTitle(line, emptyLinesCount, isFirst)
            val strongKeyword = liturgicalKeywords.any { removeAccents(line.uppercase()).startsWith(it) }
            val isHeader = isPotentialHeader && (strongKeyword || emptyLinesCount > 1 || currentTitle == null)

            if (isHeader) {
                // Fechar o bloco anterior se existir
                if (currentTitle != null) {
                    val contentBlock = currentContent.toString().trim()
                    if (isValidBlock(contentBlock)) {
                        val detectedKey = detectOriginalKey(contentBlock)
                        android.util.Log.d("SongChartParser", "TOM DETECTADO -> $currentTitle = $detectedKey")
                        charts.add(
                            SongChart(
                                manuscriptId = manuscriptId,
                                title = currentTitle!!,
                                originalKey = detectedKey,
                                content = contentBlock,
                                sortOrder = sortOrder++
                            )
                        )
                    } else {
                        android.util.Log.d("SongChartParser", "BLOCO DESCARTADO (invalido): $currentTitle")
                    }
                }
                
                detectedHeaders.add(line)
                
                // Iniciar novo bloco
                currentTitle = line
                currentContent.clear()
                currentContent.appendLine(lines[i]) // Preserva formatação original da linha
            } else {
                // Ignorar as linhas (como cabeçalhos de metadados) até encontrar a primeira música
                if (currentTitle != null) {
                    currentContent.appendLine(lines[i]) // Preserva espaços para acordes/letra
                }
            }
            emptyLinesCount = 0
        }

        // Salvar o último bloco
        if (currentTitle != null) {
            val finalContent = currentContent.toString().trim()
            if (finalContent.isNotEmpty() && isValidBlock(finalContent)) {
                val detectedKey = detectOriginalKey(finalContent)
                android.util.Log.d("SongChartParser", "TOM DETECTADO -> $currentTitle = $detectedKey")
                charts.add(
                    SongChart(
                        manuscriptId = manuscriptId,
                        title = currentTitle!!,
                        originalKey = detectedKey,
                        content = finalContent,
                        sortOrder = sortOrder
                    )
                )
            } else {
                android.util.Log.d("SongChartParser", "BLOCO DESCARTADO (invalido): $currentTitle")
            }
        }

        android.util.Log.d("SongChartParser", "Total de cabeçalhos litúrgicos detectados: ${detectedHeaders.size}")
        android.util.Log.d("SongChartParser", "Total SongCharts gerados: ${charts.size}")

        return charts
    }
}
