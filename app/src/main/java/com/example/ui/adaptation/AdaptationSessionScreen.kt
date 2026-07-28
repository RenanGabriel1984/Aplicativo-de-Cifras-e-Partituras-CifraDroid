package com.example.ui.adaptation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.domain.adaptation.MusicalAdaptation
import com.example.domain.adaptation.AdaptationType
import com.example.domain.adaptation.session.AdaptationRequest
import com.example.domain.adaptation.session.AdaptationSessionEngine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdaptationSessionScreen(
    sourceAdaptation: MusicalAdaptation,
    onAdaptationCreated: (MusicalAdaptation) -> Unit,
    onBack: () -> Unit
) {
    var session by remember { mutableStateOf(AdaptationSessionEngine.startSession(sourceAdaptation)) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    
    var targetKey by remember { mutableStateOf(sourceAdaptation.adaptedDocument.metadata.key) }
    var targetCapo by remember { mutableStateOf(sourceAdaptation.adaptedDocument.metadata.capo) }
    var profileName by remember { mutableStateOf("Nova Versão") }
    
    LaunchedEffect(targetKey, targetCapo, profileName) {
        val request = AdaptationRequest(
            type = AdaptationType.KEY_CHANGE,
            targetKey = targetKey,
            targetCapo = targetCapo,
            profileName = profileName,
            profileDescription = "Adaptação personalizada"
        )
        session = AdaptationSessionEngine.updateRequest(session, request)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Adaptação Inteligente") },
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
                Button(
                    onClick = { showConfirmDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .height(56.dp),
                    shape = MaterialTheme.shapes.large
                ) {
                    Text("Gerar Interpretação", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            AdaptationSummaryCard(
                title = sourceAdaptation.adaptedDocument.metadata.title,
                originalKey = sourceAdaptation.adaptedDocument.metadata.key,
                originalCapo = sourceAdaptation.adaptedDocument.metadata.capo
            )
            
            AdaptationTargetCard(
                targetKey = targetKey,
                targetCapo = targetCapo,
                profileName = profileName,
                onKeyChange = { targetKey = it },
                onCapoChange = { targetCapo = it },
                onProfileNameChange = { profileName = it }
            )
            
            AdaptationPreviewCard(preview = session.preview)
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
    
    if (showConfirmDialog) {
        AdaptationConfirmationDialog(
            profileName = profileName,
            onConfirm = {
                showConfirmDialog = false
                session = AdaptationSessionEngine.confirmAdaptation(session, System.currentTimeMillis(), "Usuário")
                val newAdaptation = AdaptationSessionEngine.executeAdaptation(session, System.currentTimeMillis(), "Usuário")
                onAdaptationCreated(newAdaptation)
            },
            onDismiss = { showConfirmDialog = false }
        )
    }
}
