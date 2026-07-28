package com.example.domain.adaptation

import com.example.domain.document.MusicalDocument

object MusicalAdaptationEngine {

    fun createAdaptation(
        id: String,
        identityId: String,
        originalDocument: MusicalDocument,
        profileName: String,
        profileDescription: String,
        timestamp: Long,
        author: String
    ): MusicalAdaptation {
        val profile = AdaptationProfile(
            id = "${id}_profile",
            name = profileName,
            description = profileDescription,
            rules = emptyList()
        )
        
        val history = AdaptationHistory(
            createdAt = timestamp,
            updatedAt = timestamp,
            modifiedBy = author
        )

        return MusicalAdaptation(
            id = id,
            identityId = identityId,
            originalDocumentId = originalDocument.id,
            profile = profile,
            adaptedDocument = originalDocument,
            history = history
        )
    }

    fun duplicateAdaptation(
        newId: String,
        newName: String,
        sourceAdaptation: MusicalAdaptation,
        timestamp: Long,
        author: String
    ): MusicalAdaptation {
        val newProfile = sourceAdaptation.profile.copy(
            id = "${newId}_profile",
            name = newName
        )
        
        val newHistory = AdaptationHistory(
            createdAt = timestamp,
            updatedAt = timestamp,
            modifiedBy = author
        )

        return sourceAdaptation.copy(
            id = newId,
            profile = newProfile,
            history = newHistory
        )
    }

    fun renameAdaptation(
        adaptation: MusicalAdaptation,
        newName: String,
        timestamp: Long,
        author: String
    ): MusicalAdaptation {
        val updatedProfile = adaptation.profile.copy(name = newName)
        val updatedHistory = adaptation.history.copy(
            updatedAt = timestamp,
            modifiedBy = author
        )
        return adaptation.copy(
            profile = updatedProfile,
            history = updatedHistory
        )
    }

    fun applyRules(
        adaptation: MusicalAdaptation,
        newRules: List<AdaptationRule>,
        timestamp: Long,
        author: String
    ): MusicalAdaptation {
        val updatedProfile = adaptation.profile.copy(
            rules = adaptation.profile.rules + newRules
        )
        val updatedHistory = adaptation.history.copy(
            updatedAt = timestamp,
            modifiedBy = author
        )
        
        return adaptation.copy(
            profile = updatedProfile,
            history = updatedHistory
        )
    }

    fun listAdaptations(adaptations: List<MusicalAdaptation>, identityId: String): List<MusicalAdaptation> {
        return adaptations.filter { it.identityId == identityId }
    }
}
