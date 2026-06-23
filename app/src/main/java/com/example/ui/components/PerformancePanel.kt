package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Manuscript
import com.example.data.Repertoire
import com.example.util.PerformanceSong
import com.example.util.RepertoireUtil
import kotlinx.coroutines.delay

@Composable
fun PerformanceSidebar(
    repertoire: Repertoire?,
    manuscripts: List<Manuscript>,
    currentSongId: Int,
    playedSongs: Set<Int>,
    onSongClick: (Int) -> Unit,
    onClose: () -> Unit,
    onFinish: () -> Unit,
    startTime: Long,
    elapsedTime: Long,
    isTimerRunning: Boolean,
    onTimerStart: () -> Unit,
    onTimerPause: () -> Unit,
    onTimerReset: () -> Unit
) {
    val performanceSongs = remember(repertoire, manuscripts) {
        if (repertoire == null) return@remember emptyList<PerformanceSong>()
        val cats = RepertoireUtil.getCategories(repertoire)
        val list = mutableListOf<PerformanceSong>()
        cats.forEach { cat ->
            cat.manuscriptIds.forEach { id ->
                val manuscript = manuscripts.find { it.id == id }
                val title = manuscript?.title ?: "Música $id"
                list.add(PerformanceSong(songChartId = id, title = title, category = cat.name))
            }
        }
        list
    }

    val currentIndex = remember(performanceSongs, currentSongId) {
        performanceSongs.indexOfFirst { it.songChartId == currentSongId }
    }
    val currentSong = performanceSongs.getOrNull(currentIndex)
    val nextSong = performanceSongs.getOrNull(currentIndex + 1)
    
    val progress = playedSongs.size
    val total = performanceSongs.size

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxHeight()
            .width(360.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = repertoire?.name?.uppercase() ?: "REPERTÓRIO",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Fechar Painel", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Current Song
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "EM EXECUÇÃO",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 2.sp),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = currentSong?.title ?: "---",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black, fontSize = 32.sp, lineHeight = 36.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Timer
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                PerformanceTimer(
                    startTime = startTime,
                    elapsedTime = elapsedTime,
                    isRunning = isTimerRunning,
                    onStart = onTimerStart,
                    onPause = onTimerPause,
                    onReset = onTimerReset
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Next Song
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(16.dp))
                    .clickable { nextSong?.let { onSongClick(it.songChartId) } }
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "PRÓXIMA",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 2.sp),
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = nextSong?.title ?: "Fim do Repertório",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Progress Indicator
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "PROGRESSO",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 2.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                val progressFloat = if (total > 0) progress.toFloat() / total else 0f
                LinearProgressIndicator(
                    progress = { progressFloat },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$progress DE $total MÚSICAS",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (progress >= total && total > 0) {
                Button(
                    onClick = onFinish,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Text("Concluir Repertório", color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PerformanceTimer(
    startTime: Long,
    elapsedTime: Long,
    isRunning: Boolean,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit
) {
    var currentTime by remember { mutableLongStateOf(elapsedTime) }

    LaunchedEffect(isRunning, startTime) {
        if (isRunning) {
            while (true) {
                currentTime = elapsedTime + (System.currentTimeMillis() - startTime)
                delay(100L)
            }
        } else {
            currentTime = elapsedTime
        }
    }

    val totalSeconds = currentTime / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val timeString = String.format("%02d:%02d", minutes, seconds)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = timeString,
            style = MaterialTheme.typography.displayLarge.copy(fontSize = 64.sp, fontWeight = FontWeight.Black, fontFeatureSettings = "tnum"),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isRunning) {
                FilledIconButton(
                    onClick = onPause,
                    modifier = Modifier.size(64.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Icon(Icons.Default.Stop, contentDescription = "Pausar", tint = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.size(32.dp))
                }
            } else {
                FilledIconButton(
                    onClick = onStart,
                    modifier = Modifier.size(64.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Iniciar", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(32.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            TextButton(onClick = onReset) {
                Text("ZERAR", fontWeight = FontWeight.Bold)
            }
        }
    }
}
