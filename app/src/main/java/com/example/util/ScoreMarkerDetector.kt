package com.example.util

import java.util.UUID

object ScoreMarkerDetector {
    
    private val markerDictionary = mapOf(
        "D.C." to ScoreMarkerType.DA_CAPO,
        "DA CAPO" to ScoreMarkerType.DA_CAPO,
        "D.S." to ScoreMarkerType.DAL_SEGNO,
        "DAL SEGNO" to ScoreMarkerType.DAL_SEGNO,
        "SEGNO" to ScoreMarkerType.SEGNO,
        "CODA" to ScoreMarkerType.CODA,
        "TO CODA" to ScoreMarkerType.TO_CODA,
        "FINE" to ScoreMarkerType.FINE,
        "AL FINE" to ScoreMarkerType.AL_FINE,
        "1ª CASA" to ScoreMarkerType.FIRST_ENDING,
        "1A CASA" to ScoreMarkerType.FIRST_ENDING,
        "2ª CASA" to ScoreMarkerType.SECOND_ENDING,
        "2A CASA" to ScoreMarkerType.SECOND_ENDING,
        "1ST ENDING" to ScoreMarkerType.FIRST_ENDING,
        "2ND ENDING" to ScoreMarkerType.SECOND_ENDING,
        "REPEAT" to ScoreMarkerType.REPEAT_START
    )

    fun detectFromPages(pagesText: List<PageText>): List<ScoreMarker> {
        val markers = mutableListOf<ScoreMarker>()
        
        pagesText.forEach { pageTextObj ->
            if (pageTextObj.text.isNotBlank()) {
                val lines = pageTextObj.text.lines()
                for (line in lines) {
                    val upperLine = line.uppercase()
                    for ((key, type) in markerDictionary) {
                        if (upperLine.contains(key)) {
                            markers.add(
                                ScoreMarker(
                                    id = UUID.randomUUID().toString(),
                                    type = type,
                                    page = pageTextObj.page,
                                    text = key
                                )
                            )
                        }
                    }
                }
            }
        }
        return markers
    }
}
