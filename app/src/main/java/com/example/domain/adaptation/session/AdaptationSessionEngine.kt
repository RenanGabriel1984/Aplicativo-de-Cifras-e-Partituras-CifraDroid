package com.example.domain.adaptation.session

import com.example.domain.adaptation.MusicalAdaptation
import com.example.domain.adaptation.MusicalAdaptationEngine
import java.util.UUID

object AdaptationSessionEngine {

    fun startSession(source: MusicalAdaptation): AdaptationSession {
        return AdaptationSession(
            sessionId = UUID.randomUUID().toString(),
            sourceAdaptation = source,
            currentRequest = null,
            preview = null,
            isConfirmed = false
        )
    }

    fun updateRequest(session: AdaptationSession, request: AdaptationRequest): AdaptationSession {
        val preview = generatePreview(session.sourceAdaptation, request)
        return session.copy(
            currentRequest = request,
            preview = preview
        )
    }

    private fun generatePreview(source: MusicalAdaptation, request: AdaptationRequest): AdaptationPreview {
        val originalKey = source.adaptedDocument.metadata.key
        val originalCapo = source.adaptedDocument.metadata.capo
        
        val targetKey = request.targetKey ?: originalKey
        val targetCapo = request.targetCapo ?: originalCapo
        
        val warnings = mutableListOf<String>()
        if (targetKey != originalKey) {
            warnings.add("A transposição de acordes será aplicada.")
        }
        if (targetCapo != originalCapo) {
            warnings.add("As cifras deverão considerar o novo capotraste.")
        }

        return AdaptationPreview(
            originalKey = originalKey,
            originalCapo = originalCapo,
            targetKey = targetKey,
            targetCapo = targetCapo,
            warnings = warnings
        )
    }

    fun confirmAdaptation(session: AdaptationSession, timestamp: Long, author: String): AdaptationSession {
        return session.copy(isConfirmed = true)
    }
    
    fun executeAdaptation(session: AdaptationSession, timestamp: Long, author: String): MusicalAdaptation {
        val request = session.currentRequest ?: return session.sourceAdaptation
        
        val duplicated = MusicalAdaptationEngine.duplicateAdaptation(
            newId = UUID.randomUUID().toString(),
            newName = request.profileName,
            sourceAdaptation = session.sourceAdaptation,
            timestamp = timestamp,
            author = author
        )
        
        val updatedMetadata = duplicated.adaptedDocument.metadata.copy(
            key = request.targetKey ?: duplicated.adaptedDocument.metadata.key,
            capo = request.targetCapo ?: duplicated.adaptedDocument.metadata.capo
        )
        val updatedDocument = duplicated.adaptedDocument.copy(metadata = updatedMetadata)
        
        val updatedProfile = duplicated.profile.copy(description = request.profileDescription)
        
        return duplicated.copy(
            adaptedDocument = updatedDocument,
            profile = updatedProfile
        )
    }
}
