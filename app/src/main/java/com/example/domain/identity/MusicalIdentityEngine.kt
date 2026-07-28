package com.example.domain.identity

data class MusicalIdentityStatistics(
    val versionCount: Int,
    val sources: Set<MusicalSource>,
    val hasPrimaryVersion: Boolean
)

object MusicalIdentityEngine {

    fun createIdentity(
        id: String,
        title: String,
        artist: String,
        category: String,
        originalKey: String,
        bpm: Int,
        language: String,
        liturgicalSeason: String? = null,
        ministry: String? = null,
        initialVersion: MusicalVersion? = null
    ): MusicalIdentity {
        val versions = initialVersion?.let { listOf(it) } ?: emptyList()
        return MusicalIdentity(
            id = id,
            title = title,
            artist = artist,
            category = category,
            originalKey = originalKey,
            bpm = bpm,
            language = language,
            liturgicalSeason = liturgicalSeason,
            ministry = ministry,
            versions = versions,
            primaryVersion = initialVersion
        )
    }

    fun createVersion(
        id: String,
        origin: MusicalOrigin,
        createdAt: Long,
        documentId: String,
        description: String
    ): MusicalVersion {
        return MusicalVersion(
            id = id,
            origin = origin,
            createdAt = createdAt,
            documentId = documentId,
            description = description
        )
    }

    fun addVersion(identity: MusicalIdentity, version: MusicalVersion, setAsPrimary: Boolean = false): MusicalIdentity {
        val newVersions = identity.versions + version
        val newPrimary = if (setAsPrimary || identity.primaryVersion == null) version else identity.primaryVersion
        return identity.copy(
            versions = newVersions,
            primaryVersion = newPrimary
        )
    }

    fun removeVersion(identity: MusicalIdentity, versionId: String): MusicalIdentity {
        val newVersions = identity.versions.filter { it.id != versionId }
        val newPrimary = if (identity.primaryVersion?.id == versionId) newVersions.firstOrNull() else identity.primaryVersion
        return identity.copy(
            versions = newVersions,
            primaryVersion = newPrimary
        )
    }

    fun findPrimaryVersion(identity: MusicalIdentity): MusicalVersion? {
        return identity.primaryVersion ?: identity.versions.firstOrNull()
    }

    fun changePrimaryVersion(identity: MusicalIdentity, versionId: String): MusicalIdentity {
        val newPrimary = identity.versions.find { it.id == versionId } ?: identity.primaryVersion
        return identity.copy(primaryVersion = newPrimary)
    }

    fun calculateIdentityStatistics(identity: MusicalIdentity): MusicalIdentityStatistics {
        return MusicalIdentityStatistics(
            versionCount = identity.versions.size,
            sources = identity.versions.map { it.origin.source }.toSet(),
            hasPrimaryVersion = identity.primaryVersion != null
        )
    }
}
