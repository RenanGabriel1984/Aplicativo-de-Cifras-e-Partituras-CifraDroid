package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import androidx.compose.ui.res.stringResource
import com.example.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PedalSettingsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val isConnected by viewModel.pedalManager.isConnected.collectAsStateWithLifecycle()
    val deviceName by viewModel.pedalManager.deviceName.collectAsStateWithLifecycle()
    val isVerticalScroll by viewModel.isVerticalScroll.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.pedal_setup)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                    navigationIconContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Status Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isConnected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = if (isConnected) Icons.Default.BluetoothConnected else Icons.Default.Bluetooth,
                        contentDescription = stringResource(R.string.bluetooth_status),
                        modifier = Modifier.size(32.dp),
                        tint = if (isConnected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Column {
                        val finalDeviceName = if (deviceName.contains("M-VAVE") || deviceName.contains("Cube Turner")) deviceName else "M-VAVE / Cube Turner"
                        Text(
                            text = if (isConnected) stringResource(R.string.connected, finalDeviceName) else "M-VAVE / Cube Turner Desconectado",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isConnected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isConnected) stringResource(R.string.pedal_detected) else "Conecte via Bluetooth nas configurações do Android. Os comandos HID (Setas, PageUp/Down) serão reconhecidos automaticamente.",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isConnected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Text(stringResource(R.string.key_mapping), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

            // Mapping Configurations
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedCard(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.left_pedal), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(if (isVerticalScroll) stringResource(R.string.scroll_up) else stringResource(R.string.previous_page), style = MaterialTheme.typography.bodyLarge)
                    }
                }
                OutlinedCard(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.right_pedal), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(if (isVerticalScroll) stringResource(R.string.scroll_down) else stringResource(R.string.next_page), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            Text(stringResource(R.string.operation_mode), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

            // Operation Mode Segments
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val modes = listOf(
                    true to "Modo Portrait (Rolagem Vertical Contínua)",
                    false to "Modo Landscape (Troca Horizontal de Páginas)"
                )
                modes.forEach { (isVertical, modeName) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isVerticalScroll == isVertical) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceContainer,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { viewModel.setVerticalScroll(isVertical) }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isVerticalScroll == isVertical,
                            onClick = { viewModel.setVerticalScroll(isVertical) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(modeName, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.press_pedals_to_test),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
