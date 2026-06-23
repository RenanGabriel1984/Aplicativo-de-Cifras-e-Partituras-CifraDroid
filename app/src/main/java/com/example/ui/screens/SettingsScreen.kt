package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPedalSettings: () -> Unit
) {
    val isStageMode by viewModel.isStageMode.collectAsStateWithLifecycle()
    val isChoirMode by viewModel.isChoirMode.collectAsStateWithLifecycle()
    val isVerticalScroll by viewModel.isVerticalScroll.collectAsStateWithLifecycle()
    val autoScrollSpeed by viewModel.autoScrollSpeed.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurações") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            
            // Geral
            Text(
                "Leitura e Visualização",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
            )
            
            ListItem(
                headlineContent = { Text("Modo de Rolagem Vertical") },
                supportingContent = { Text("Mudar o pager de páginas para rolagem vertical invés de horizontal") },
                trailingContent = {
                    Switch(checked = isVerticalScroll, onCheckedChange = { viewModel.setVerticalScroll(it) })
                }
            )

            ListItem(
                headlineContent = { Text("Desempenho (Performance Mode)") },
                supportingContent = { Text("Otimiza o uso de memória e CPU do leitor") },
                trailingContent = {
                    Switch(checked = isStageMode, onCheckedChange = { viewModel.setStageMode(it) })
                }
            )
            
            ListItem(
                headlineContent = { Text("Modo Coral") },
                supportingContent = { Text("Exibição otimizada para corais") },
                trailingContent = {
                    Switch(checked = isChoirMode, onCheckedChange = { viewModel.setChoirMode(it) })
                }
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            Text(
                "Palco & Performance",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
            )

            val context = androidx.compose.ui.platform.LocalContext.current
            val prefsManager = remember { com.example.util.PreferencesManager(context) }
            var extremeFocus by remember { mutableStateOf(prefsManager.isExtremeFocusModeEnabled()) }
            ListItem(
                headlineContent = { Text("Modo Foco Extremo") },
                supportingContent = { Text("Oculta menus, exibe apenas cifra/partitura e painel de palco") },
                trailingContent = {
                    Switch(checked = extremeFocus, onCheckedChange = {
                        extremeFocus = it
                        prefsManager.setExtremeFocusModeEnabled(it)
                    })
                }
            )

            var nextSongAlert by remember { mutableStateOf(prefsManager.isNextSongAlertEnabled()) }
            ListItem(
                headlineContent = { Text("Alerta de Próxima Música") },
                supportingContent = { Text("Tela fullscreen de alerta antes de avançar para a próxima música") },
                trailingContent = {
                    Switch(checked = nextSongAlert, onCheckedChange = {
                        nextSongAlert = it
                        prefsManager.setNextSongAlertEnabled(it)
                    })
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                "Navegação e Controles",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
            )
            
            ListItem(
                headlineContent = { Text("Velocidade Automática de Rolagem") },
                supportingContent = {
                    Slider(
                        value = autoScrollSpeed,
                        onValueChange = { viewModel.setAutoScrollSpeed(it) },
                        valueRange = 0f..5f,
                        steps = 5,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            )
            
            ListItem(
                headlineContent = { Text("Pedal Bluetooth") },
                supportingContent = { Text("Configurar o Page Turner ou pedal MIDI") },
                modifier = Modifier.clickable { onNavigateToPedalSettings() }
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            Text(
                "Cifras",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
            )

            ListItem(
                headlineContent = { Text("Tamanho da Fonte (Cifras)") },
                supportingContent = { Text("Configurado como Padrão (18sp)") }
            )
            
            ListItem(
                headlineContent = { Text("Tema da Cifra") },
                supportingContent = { Text("Cor: Vermelho Escuro | Fundo: Papel") }
            )
        }
    }
}
