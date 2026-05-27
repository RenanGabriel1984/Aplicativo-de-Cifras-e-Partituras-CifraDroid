package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.util.SessionNetworkManager
import com.example.util.SessionRole
import com.example.util.SessionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaestroScreen(
    onNavigateBack: () -> Unit
) {
    val role by SessionManager.currentRole.collectAsStateWithLifecycle()
    val connStatus by SessionNetworkManager.connectionStatus.collectAsStateWithLifecycle()
    val pin by SessionNetworkManager.sessionPin.collectAsStateWithLifecycle()
    val clientsCount by SessionNetworkManager.connectedClientsCount.collectAsStateWithLifecycle()

    var inputPin by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hub Maestro") },
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Icon(Icons.Default.WifiTethering, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
            
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Status: $connStatus", style = MaterialTheme.typography.titleMedium)
                    if (role == SessionRole.LEADER && pin.isNotEmpty()) {
                        Text("Sua Sessão (PIN): $pin", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("Participantes conectados: $clientsCount", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            if (role == SessionRole.STANDALONE) {
                Button(
                    onClick = { SessionNetworkManager.startServer() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Criar Sessão (Maestro)")
                }

                HorizontalDivider()

                OutlinedTextField(
                    value = inputPin,
                    onValueChange = { inputPin = it },
                    label = { Text("PIN da Sessão") },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = { SessionNetworkManager.connectViaPin(inputPin) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = inputPin.length >= 4,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("Conectar (Músico)")
                }
            } else {
                Button(
                    onClick = { SessionNetworkManager.stopAll() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Desconectar")
                }
            }
        }
    }
}
