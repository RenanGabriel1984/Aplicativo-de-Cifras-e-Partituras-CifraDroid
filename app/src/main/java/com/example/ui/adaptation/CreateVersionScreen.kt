package com.example.ui.adaptation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateVersionScreen(
    title: String = "Santo",
    artist: String = "Músicas para Missa",
    key: String = "G",
    category: String = "Litúrgica",
    onCreateVersion: (String) -> Unit
) {
    var selectedType by remember { mutableStateOf("Minha Banda") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nova Versão") },
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
                CreateVersionActionBar(
                    onConfirm = { onCreateVersion(selectedType) },
                    modifier = Modifier.padding(24.dp),
                    enabled = selectedType.isNotEmpty()
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            VersionNameCard(
                title = title,
                artist = artist,
                key = key,
                category = category
            )
            
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = "Esta será sua primeira adaptação desta música.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
            
            AdaptationTypeSelector(
                selectedType = selectedType,
                onTypeSelected = { selectedType = it }
            )
            
            VersionSummaryCard(
                selectedName = selectedType
            )
        }
    }
}
