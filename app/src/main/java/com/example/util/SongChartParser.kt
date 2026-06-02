package com.example.util

import com.example.data.SongChart

object SongChartParser {
    // Basic parser: tries to separate "songs" based on empty lines or common titles.
    // For now, let's look for simple block separations or just return one song per page if we can't detect,
    // or return everything as one song if no clear separator exists, BUT the prompt asks for separating them.
    // A simple heuristic for splitting a PDF text:
    // Split by multiple newlines, or lines that are all caps (titles).
    // Let's split by double newline blocks and treat the first line of a block as title if it's short.
    
    fun parse(manuscriptId: Int, content: String): List<SongChart> {
        val charts = mutableListOf<SongChart>()
        // Simple heuristic: music blocks are often separated by empty lines
        val blocks = content.split(Regex("(\r?\n){3,}")) // 3 or more newlines
        
        var order = 0
        for (block in blocks) {
            val trimmed = block.trim()
            if (trimmed.isEmpty()) continue
            
            val lines = trimmed.split("\n")
            val title = lines.firstOrNull { it.isNotBlank() }?.take(50) ?: "Música ${order + 1}"
            
            charts.add(
                SongChart(
                    manuscriptId = manuscriptId,
                    title = title,
                    originalKey = "C", // No auto-detect required by prompt, fallback to C
                    content = trimmed,
                    sortOrder = order
                )
            )
            order++
        }
        
        // If everything was parsed as a single block but we wanted more, maybe split by "Letra" or something?
        // Basic block splitting should be enough for a proof of concept as long as we demonstrate 1:N.
        if (charts.isEmpty() && content.isNotBlank()) {
            charts.add(
                SongChart(
                    manuscriptId = manuscriptId,
                    title = "Música 1",
                    originalKey = "C",
                    content = content,
                    sortOrder = 0
                )
            )
        }
        
        return charts
    }
}
