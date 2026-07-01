package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.util.PerformanceSession

@Composable
fun PerformanceSessionOverlay(
    session: PerformanceSession?,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = session != null && session.isRunning,
        enter = fadeIn() + slideInVertically { it },
        exit = fadeOut() + slideOutVertically { it },
        modifier = modifier
    ) {
        if (session != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "🔴 SESSÃO ATIVA",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = session.currentSong.ifEmpty { "Repertório" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (session.currentPass.isNotEmpty()) {
                            Text(
                                text = session.currentPass,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        val minutes = (session.elapsedTime / 1000) / 60
                        val seconds = (session.elapsedTime / 1000) % 60
                        Text(
                            text = String.format("%02d:%02d", minutes, seconds),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${session.songsPlayed} músicas • ${session.executedRelationships} saltos",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PerformanceSessionReport(
    session: PerformanceSession?,
    onClose: () -> Unit
) {
    if (session != null && session.isFinished) {
        AlertDialog(
            onDismissRequest = onClose,
            title = { 
                Text(
                    text = "Relatório de Performance",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                ) 
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val minutes = (session.elapsedTime / 1000) / 60
                    val seconds = (session.elapsedTime / 1000) % 60
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Tempo Total:", fontWeight = FontWeight.SemiBold)
                        Text(String.format("%02d:%02d", minutes, seconds))
                    }
                    HorizontalDivider()
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Músicas Tocadas:", fontWeight = FontWeight.SemiBold)
                        Text("${session.songsPlayed}")
                    }
                    HorizontalDivider()
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Saltos Executados:", fontWeight = FontWeight.SemiBold)
                        Text("${session.executedRelationships}")
                    }
                    if (session.currentPass.isNotEmpty()) {
                        HorizontalDivider()
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Última Passagem:", fontWeight = FontWeight.SemiBold)
                            Text(session.currentPass)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = onClose) {
                    Text("Concluir")
                }
            }
        )
    }
}
