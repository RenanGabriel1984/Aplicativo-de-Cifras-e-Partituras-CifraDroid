package com.example.ui.adaptation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.domain.adaptation.MusicalAdaptation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdaptationWorkspaceScreen(
    adaptation: MusicalAdaptation,
    onEditKey: () -> Unit = {},
    onEditCapo: () -> Unit = {},
    onEditNotes: () -> Unit = {},
    onEditStructure: () -> Unit = {},
    onViewHistory: () -> Unit = {},
    onOpenEditor: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Workspace") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.background,
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                AdaptationWorkspaceActionBar(
                    onPlay = onOpenEditor,
                    modifier = Modifier.padding(24.dp)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            AdaptationHeader(
                versionName = adaptation.profile.name,
                songTitle = adaptation.adaptedDocument.metadata.title
            )
            
            AdaptationInfoCard(
                key = adaptation.adaptedDocument.metadata.key,
                capo = adaptation.adaptedDocument.metadata.capo,
                category = adaptation.adaptedDocument.metadata.category.name,
                onEditKey = onEditKey,
                onEditCapo = onEditCapo
            )
            
            AdaptationStructureCard(
                structure = adaptation.adaptedDocument.sections.map { it.semanticType.uppercase() },
                onEditStructure = onEditStructure
            )
            
            AdaptationNotesCard(
                notes = adaptation.profile.description,
                onEditNotes = onEditNotes
            )
            
            AdaptationHistoryCard(
                createdAt = adaptation.history.createdAt,
                updatedAt = adaptation.history.updatedAt,
                profileName = adaptation.profile.name,
                onViewHistory = onViewHistory
            )
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
