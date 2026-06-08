package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticScreen(
    manuscriptId: Int,
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val manuscript by viewModel.getById(manuscriptId).collectAsStateWithLifecycle(null)
    val songCharts by viewModel.getSongCharts(manuscriptId).collectAsStateWithLifecycle(emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Diagnóstico da Importação") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            manuscript?.let { ms ->
                Text("=== RESULTADO DO PARSER ===", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                
                Text("Total de caracteres extraídos: ${ms.extractedText.length}", style = MaterialTheme.typography.bodyLarge)
                val lines = ms.extractedText.split("\n")
                Text("Total de linhas: ${lines.size}", style = MaterialTheme.typography.bodyLarge)
                Text("Cabeçalhos litúrgicos detectados: ${songCharts.size}", style = MaterialTheme.typography.bodyLarge)
                Text("Quantidade de blocos gerados: ${songCharts.size}", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Segmentação realizada:", style = MaterialTheme.typography.titleMedium)
                Text(if (songCharts.size > 1) "SIM" else "NÃO", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(16.dp))

                Text("Cabeçalhos litúrgicos detectados:", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                songCharts.forEachIndexed { index, chart ->
                    Text("${index + 1}. ${chart.title}", style = MaterialTheme.typography.bodyLarge)
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                val showExtractedText = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Text("=== TEXTO EXTRAÍDO ===", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = { showExtractedText.value = !showExtractedText.value }) {
                        Text(if (showExtractedText.value) "Ocultar" else "Mostrar")
                    }
                }
                
                if (showExtractedText.value) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        val maxLines = minOf(300, lines.size)
                        items(maxLines) { index ->
                            Text("[${index.toString().padStart(3, '0')}] ${lines[index]}", style = MaterialTheme.typography.bodyMedium, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                        }
                    }
                }
            } ?: run {
                Text("Carregando ou manuscrito não encontrado...")
            }
        }
    }
}
