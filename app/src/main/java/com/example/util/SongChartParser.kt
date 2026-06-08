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

    private fun removeAccents(str: String): String {
        val normalized = java.text.Normalizer.normalize(str, java.text.Normalizer.Form.NFD)
        return Regex("\\p{InCombiningDiacriticalMarks}+").replace(normalized, "")
    }

    private fun isLiturgicalHeader(line: String): Boolean {
        val upperLine = removeAccents(line.trim().uppercase())
        val startsWithKeyword = liturgicalKeywords.any { keyword -> upperLine.startsWith(keyword) }
        
        if (!startsWithKeyword) {
            return false
        }

        val words = line.trim().split(Regex("\\s+"))
        if (words.size > 6) {
            android.util.Log.d("SongChartParser", "HEADER REJEITADO -> $line")
            return false
        }

        if (line.contains(Regex("[,.!?;:]"))) {
            android.util.Log.d("SongChartParser", "HEADER REJEITADO -> $line")
            return false
        }

        android.util.Log.d("SongChartParser", "HEADER DETECTADO -> $line")
        return true
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
        for (line in lines) {
            val words = line.split(Regex("[\\s()]+")).filter { it.isNotBlank() }
            chordCount += words.count { it.matches(chordRegex) }
        }
        
        return usefulLines >= 5 || chordCount >= 3
    }

    fun parse(manuscriptId: Int, content: String): List<SongChart> {
        val charts = mutableListOf<SongChart>()
        val lines = content.split("\n")
        
        android.util.Log.d("SongChartParser", "Total de linhas recebidas: ${lines.size}")

        var currentTitle: String? = null
        val currentContent = StringBuilder()
        var sortOrder = 0

        val detectedHeaders = mutableListOf<String>()

        for (i in lines.indices) {
            val line = lines[i].trim()
            if (line.isEmpty() && currentContent.isEmpty()) continue

            if (isLiturgicalHeader(line)) {
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
