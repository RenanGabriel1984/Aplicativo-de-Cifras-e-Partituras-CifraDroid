package com.example.util

object ScoreRelationshipEngine {

    fun buildRelationships(markers: List<ScoreMarker>): List<ScoreRelationship> {
        val relationships = mutableListOf<ScoreRelationship>()

        for (marker in markers) {
            when (marker.type) {
                ScoreMarkerType.DA_CAPO -> {
                    relationships.add(
                        ScoreRelationship(
                            sourceMarkerId = marker.id,
                            targetMarkerId = null,
                            sourcePage = marker.page,
                            // Utilizando 0-index para alinhar com o mapeamento das páginas e ViewPager
                            targetPage = findDaCapoTarget(), 
                            relationshipType = RelationshipType.DA_CAPO_TO_START
                        )
                    )
                }
                ScoreMarkerType.DAL_SEGNO -> {
                    val target = findFirstSegno(markers)
                    relationships.add(
                        ScoreRelationship(
                            sourceMarkerId = marker.id,
                            targetMarkerId = target?.id,
                            sourcePage = marker.page,
                            targetPage = target?.page,
                            relationshipType = if (target != null) RelationshipType.DAL_SEGNO_TO_SEGNO else RelationshipType.UNRESOLVED
                        )
                    )
                }
                ScoreMarkerType.TO_CODA -> {
                    val target = findFirstCoda(markers)
                    relationships.add(
                        ScoreRelationship(
                            sourceMarkerId = marker.id,
                            targetMarkerId = target?.id,
                            sourcePage = marker.page,
                            targetPage = target?.page,
                            relationshipType = if (target != null) RelationshipType.TO_CODA_TO_CODA else RelationshipType.UNRESOLVED
                        )
                    )
                }
                ScoreMarkerType.AL_FINE -> {
                    val target = findFirstFine(markers)
                    relationships.add(
                        ScoreRelationship(
                            sourceMarkerId = marker.id,
                            targetMarkerId = target?.id,
                            sourcePage = marker.page,
                            targetPage = target?.page,
                            relationshipType = if (target != null) RelationshipType.AL_FINE_TO_FINE else RelationshipType.UNRESOLVED
                        )
                    )
                }
                else -> {
                    // Outros marcadores (como REPEAT, 1ST ENDING, etc.) serão implementados futuramente
                }
            }
        }

        return relationships.sortedBy { it.sourcePage }
    }

    fun findFirstSegno(markers: List<ScoreMarker>): ScoreMarker? {
        return markers.firstOrNull { it.type == ScoreMarkerType.SEGNO }
    }

    fun findFirstCoda(markers: List<ScoreMarker>): ScoreMarker? {
        return markers.firstOrNull { it.type == ScoreMarkerType.CODA }
    }

    fun findFirstFine(markers: List<ScoreMarker>): ScoreMarker? {
        return markers.firstOrNull { it.type == ScoreMarkerType.FINE }
    }

    fun findDaCapoTarget(): Int {
        return 0 // Representa o início do documento (0-indexed)
    }
}
