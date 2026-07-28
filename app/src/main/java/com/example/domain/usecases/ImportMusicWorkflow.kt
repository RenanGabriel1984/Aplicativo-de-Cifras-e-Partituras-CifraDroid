package com.example.domain.usecases

import com.example.domain.document.MusicalDocument
import com.example.domain.identity.MusicalIdentityEngine
import com.example.domain.analysis.MusicalAnalysisEngine
import java.util.UUID

// Mock engines to allow compilation if needed, since the original code was lost
object PreparationRulesEngine {
    fun generateHints(document: MusicalDocument, analysis: Any): Any = Any()
}

object PreparationAdvisorEngine {
    fun generateAdvisor(document: MusicalDocument, analysis: Any, hints: Any): Any = Any()
}

object ImportMusicWorkflow {
    fun execute(document: MusicalDocument): ImportMusicResult {
        // 1. Cria MusicalIdentity
        val identity = MusicalIdentityEngine.createIdentity(
            id = UUID.randomUUID().toString(),
            title = document.metadata.title,
            artist = document.metadata.artist,
            category = document.metadata.category.name,
            originalKey = document.metadata.key,
            bpm = document.metadata.bpm,
            language = "Unknown"
        )
        
        // 2. Cria SongDocument (using the MusicalDocument provided)
        val songDocument = document
        
        // 3. Executa MusicalAnalysisEngine
        val analysis = MusicalAnalysisEngine.analyze(songDocument)
        
        // 4. Executa PreparationRulesEngine
        val preparationHints = PreparationRulesEngine.generateHints(songDocument, analysis)
        
        // 5. Executa PreparationAdvisorEngine
        val advisor = PreparationAdvisorEngine.generateAdvisor(songDocument, analysis, preparationHints)
        
        // 6. Retorna ImportMusicResult
        return ImportMusicResult(
            identity = identity,
            songDocument = songDocument,
            analysis = analysis,
            preparationHints = preparationHints,
            advisor = advisor
        )
    }
}
