package com.example.util

import com.example.data.SongChart

object SongChartParser {
    
    fun parse(manuscriptId: Int, content: String): List<SongChart> {
        val charts = mutableListOf<SongChart>()
        val lines = content.split("\n")
        
        android.util.Log.d("SongChartParser", "Total de linhas recebidas: ${lines.size}")

        val titleIndices = mutableListOf<Int>()
        val detectedTitles = mutableListOf<String>()

        val chordRegex = Regex("^[A-G](?:#|b)?(?:m|min|maj|dim|aug|sus\\d?)?(?:\\d+)?(?:/[A-G](?:#|b)?)?$")

        for (i in lines.indices) {
            val line = lines[i].trim()
            if (line.isEmpty()) continue

            // 2. Comprimento entre 4 e 60
            if (line.length < 4 || line.length > 60) continue

            // 1. Majoritariamente em caixa alta
            val upperCount = line.count { it.isUpperCase() }
            val lowerCount = line.count { it.isLowerCase() }
            if (upperCount <= lowerCount) continue

            // 4. Não ser composta apenas por números
            val hasLetters = line.any { it.isLetter() }
            if (!hasLetters) continue

            // Separar palavras para validações seguintes
            val words = line.split(Regex("[\\s\\p{Punct}]+")).filter { it.isNotBlank() }
            if (words.isEmpty()) continue

            // 3. Não corresponder ao regex de acordes musicais (apenas cifras)
            val isOnlyChords = words.all { it.matches(chordRegex) }
            if (isOnlyChords) continue

            // 5. Pelo menos uma palavra com 4 ou mais letras
            val hasWordWith4Plus = words.any { word -> word.count { it.isLetter() } >= 4 }
            if (!hasWordWith4Plus) continue

            // 6. Isolada visualmente por linha vazia anterior ou posterior
            val isIsolatedBefore = (i == 0) || lines[i - 1].trim().isEmpty()
            val isIsolatedAfter = (i == lines.size - 1) || lines[i + 1].trim().isEmpty()
            if (!isIsolatedBefore && !isIsolatedAfter) continue

            titleIndices.add(i)
            detectedTitles.add(line)
            android.util.Log.d("SongChartParser", "Título detectado: $line")
        }

        android.util.Log.d("SongChartParser", "Títulos detectados (${detectedTitles.size}): $detectedTitles")

        if (titleIndices.isEmpty()) return emptyList()

        for (i in titleIndices.indices) {
            val startIndex = titleIndices[i]
            val endIndex = if (i + 1 < titleIndices.size) titleIndices[i + 1] else lines.size

            val blockLines = lines.subList(startIndex, endIndex)
            val title = blockLines.first().trim()
            val contentBlock = blockLines.joinToString("\n").trim()

            charts.add(
                SongChart(
                    manuscriptId = manuscriptId,
                    title = title,
                    originalKey = "C",
                    content = contentBlock,
                    sortOrder = i
                )
            )
        }

        android.util.Log.d("SongChartParser", "Total SongCharts gerados: ${charts.size}")

        return charts
    }
}
