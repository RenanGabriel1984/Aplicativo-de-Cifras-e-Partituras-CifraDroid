package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.util.ChordTransposer

import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.foundation.gestures.animateScrollBy

@Composable
fun ContinuousChartItem(
    songChartId: Int,
    repertoireId: Int,
    viewModel: MainViewModel,
    chordColor: Color,
    textColor: Color,
    fontSizeSp: Float
) {
    val songChart by viewModel.getSongChartById(songChartId).collectAsStateWithLifecycle(null)
    val repertoireSong by viewModel.findRepertoireSong(repertoireId, songChartId).collectAsStateWithLifecycle(null)

    if (songChart == null) return

    val chart = songChart!!
    val currentKey = repertoireSong?.customKey ?: chart.originalKey

    val transposedText = remember(chart.content, chart.originalKey, currentKey) {
        val stepsForText = ChordTransposer.getStepsBetween(chart.originalKey, currentKey)
        val isFlatsText = currentKey.contains("b") || currentKey == "F"
        if (stepsForText == 0) chart.content else ChordTransposer.transposeText(chart.content, stepsForText, isFlatsText)
    }

    val lines = remember(transposedText) { transposedText.split("\n") }

    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
        Text(
            text = chart.title,
            color = textColor,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Tom: $currentKey",
            color = MaterialTheme.colorScheme.primary,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        for(lineText in lines) {
            val annotatedString = remember(lineText) {
                androidx.compose.ui.text.buildAnnotatedString {
                    val tokenRegex = Regex("(\\s+)|([^\\s]+)")
                    for (match in tokenRegex.findAll(lineText)) {
                        val token = match.value
                        if (token.isBlank()) {
                            append(token)
                        } else if (ChordTransposer.isChord(token)) {
                            withStyle(androidx.compose.ui.text.SpanStyle(color = chordColor, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)) {
                                append(token)
                            }
                        } else {
                            append(token)
                        }
                    }
                }
            }
            Text(
                text = annotatedString,
                color = textColor,
                fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
                fontSize = fontSizeSp.sp,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongChartScreen(
    songChartId: Int,
    repertoireId: Int? = null,
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onNavigateToSongChart: (Int) -> Unit = {},
    isStageMode: Boolean = false,
    showHud: Boolean = true,
    isTouchLocked: Boolean = false,
    onToggleHud: () -> Unit = {},
    isDarkMode: Boolean = false
) {
    val songChart by viewModel.getSongChartById(songChartId).collectAsStateWithLifecycle(null)
    val repertoire by if (repertoireId != null) viewModel.getRepertoire(repertoireId).collectAsStateWithLifecycle(null) else remember { mutableStateOf(null) }
    val repertoireSong by if (repertoireId != null) viewModel.findRepertoireSong(repertoireId, songChartId).collectAsStateWithLifecycle(null) else remember { mutableStateOf(null) }

    if (songChart == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Cifra não disponível.", color = MaterialTheme.colorScheme.onSurface)
        }
        return
    }

    val chart = songChart!!
    val currentKey = if (repertoireId != null) {
        repertoireSong?.customKey ?: chart.originalKey
    } else {
        chart.savedKey ?: chart.originalKey
    }

    val transposedText = remember(chart.content, chart.originalKey, currentKey) {
        val stepsForText = ChordTransposer.getStepsBetween(chart.originalKey, currentKey)
        val isFlatsText = currentKey.contains("b") || currentKey == "F"
        if (stepsForText == 0) chart.content else ChordTransposer.transposeText(chart.content, stepsForText, isFlatsText)
    }

    val lines = remember(transposedText) { transposedText.split("\n") }
    val chordColor = if (isDarkMode) Color(0xFFFF6B6B) else Color(0xFF8B0000) // Light Red for dark mode, Dark Red for Light mode
    val textColor = if (isDarkMode) Color.LightGray else Color.Black
    val surfaceColor = if (isDarkMode) Color.Black else Color.White
    val topBarColor = if (isDarkMode) Color(0xFF1A1A1A) else Color(0xFFF5F5F5)
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefsManager = remember { com.example.util.PreferencesManager(context) }
    
    LaunchedEffect(songChartId) {
        prefsManager.recordSongOpened(songChartId)
    }

    var fontSizeSp by remember { mutableStateOf(prefsManager.getChordFontSize()) }
    
    val scrollState = androidx.compose.foundation.lazy.rememberLazyListState()

    var autoScrollSpeed by remember { mutableIntStateOf(0) }
    var scrollStartTime by remember { mutableLongStateOf(0L) }
    var scrollDurationSeconds by remember { mutableLongStateOf(0L) }

    LaunchedEffect(autoScrollSpeed) {
        if (autoScrollSpeed > 0) {
            scrollStartTime = System.currentTimeMillis()
            val pixelsPerSecond = when (autoScrollSpeed) {
                1 -> 15f
                2 -> 30f
                3 -> 45f
                else -> 60f
            }
            // Animate scroll smooth interval
            while(true) {
                // To keep timer updated continuously we will check elapsed frequently
                scrollDurationSeconds = (System.currentTimeMillis() - scrollStartTime) / 1000L
                scrollState.animateScrollBy(
                    value = pixelsPerSecond / 10f, 
                    animationSpec = androidx.compose.animation.core.tween(100, easing = androidx.compose.animation.core.LinearEasing)
                )
            }
        } else {
            scrollDurationSeconds = 0L
        }
    }

    val categoryName = remember(repertoire, songChartId) {
        if (repertoire != null) {
            val cats = com.example.util.RepertoireUtil.getCategories(repertoire!!)
            cats.find { it.manuscriptIds.contains(songChartId) }?.name ?: "Música"
        } else {
            "Música"
        }
    }

    var countdownNextChartId by remember { mutableStateOf<Int?>(null) }
    var countdownRemaining by remember { mutableIntStateOf(10) }

    LaunchedEffect(countdownNextChartId) {
        if (countdownNextChartId != null) {
            countdownRemaining = 10
            while(countdownRemaining > 0) {
                kotlinx.coroutines.delay(1000)
                countdownRemaining--
            }
            onNavigateToSongChart(countdownNextChartId!!)
            countdownNextChartId = null
        }
    }

    if (countdownNextChartId != null) {
        val nextChart = viewModel.getSongChartById(countdownNextChartId!!).collectAsStateWithLifecycle(null).value
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { countdownNextChartId = null },
            title = { Text("Próxima Música") },
            text = { Text("Iniciando ${nextChart?.title ?: "..."} em $countdownRemaining segundos...") },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    onNavigateToSongChart(countdownNextChartId!!)
                    countdownNextChartId = null
                }) { Text("Pular") }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { countdownNextChartId = null }) { Text("Cancelar") }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(surfaceColor)
                .windowInsetsPadding(androidx.compose.foundation.layout.WindowInsets.safeDrawing)
                .clickable(enabled = isStageMode && !isTouchLocked) { onToggleHud() }
        ) {
            if (!isTouchLocked && (!isStageMode || showHud)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(topBarColor)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Voltar Button
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.TextButton(
                            onClick = onBack,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Voltar", tint = MaterialTheme.colorScheme.primary)
                            Text("Voltar", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            androidx.compose.material3.TextButton(
                                onClick = {
                                    if (fontSizeSp > 10f) {
                                        fontSizeSp -= 2f
                                        prefsManager.setChordFontSize(fontSizeSp)
                                    }
                                },
                                contentPadding = PaddingValues(4.dp)
                            ) { Text("A-", fontSize = 14.sp) }
                            androidx.compose.material3.TextButton(
                                onClick = {
                                    if (fontSizeSp < 40f) {
                                        fontSizeSp += 2f
                                        prefsManager.setChordFontSize(fontSizeSp)
                                    }
                                },
                                contentPadding = PaddingValues(4.dp)
                            ) { Text("A+", fontSize = 16.sp) }
                        }
                    }
                }
            }
            
            Column(modifier = Modifier.padding(16.dp)) {
                // Title
                Text(
                    text = chart.title, 
                    color = textColor, 
                    style = MaterialTheme.typography.titleLarge
                )
                
                if (repertoire != null) {
                    val ids = com.example.util.RepertoireUtil.getFlatManuscriptIds(repertoire!!)
                    val idx = ids.indexOf(songChartId)
                    if (idx != -1) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$categoryName • ${idx + 1} de ${ids.size}",
                            color = if (isDarkMode) Color.Gray else Color.DarkGray,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Transposition Controls
                Row(
                    verticalAlignment = Alignment.CenterVertically, 
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val isTransposed = currentKey != chart.originalKey
                
                val updateKey: (String?) -> Unit = { newKey ->
                    if (repertoireId != null) {
                        repertoireSong?.let {
                            viewModel.updateRepertoireSongKey(it.id, newKey)
                        }
                    } else {
                        viewModel.updateSongChartKey(chart.id, newKey)
                    }
                }

                Column {
                    if (isTransposed) {
                        Text("Original: ${chart.originalKey}", color = if (isDarkMode) Color.Gray else Color.DarkGray, fontSize = 12.sp)
                        Text("Atual: $currentKey", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    } else {
                        Text("Tom: $currentKey", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isTransposed) {
                        androidx.compose.material3.TextButton(onClick = { 
                            updateKey(null)
                            android.widget.Toast.makeText(context, "Tom original restaurado", android.widget.Toast.LENGTH_SHORT).show()
                        }) {
                            Text("Restaurar", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    
                    IconButton(onClick = { 
                        val nextKey = com.example.util.ChordTransposer.transposeText(currentKey, -1, useFlats = true)
                        updateKey(nextKey)
                        android.widget.Toast.makeText(context, "Tom salvo", android.widget.Toast.LENGTH_SHORT).show()
                    }) {
                        Text("-", color = textColor, fontSize = 28.sp)
                    }
                    
                    IconButton(onClick = { 
                        val nextKey = com.example.util.ChordTransposer.transposeText(currentKey, 1, useFlats = false)
                        updateKey(nextKey)
                        android.widget.Toast.makeText(context, "Tom salvo", android.widget.Toast.LENGTH_SHORT).show()
                    }) {
                        Text("+", color = textColor, fontSize = 24.sp)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                var menuExpanded by remember { mutableStateOf(false) }
                Box {
                    androidx.compose.material3.TextButton(
                        onClick = { menuExpanded = true },
                        colors = if (autoScrollSpeed > 0) androidx.compose.material3.ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary) else androidx.compose.material3.ButtonDefaults.textButtonColors(contentColor = textColor)
                    ) {
                        Icon(if (autoScrollSpeed > 0) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (autoScrollSpeed > 0) "Rolagem: ON" else "Iniciar Rolagem")
                    }
                    
                    androidx.compose.material3.DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        listOf(0 to "Parar", 1 to "Muito Lenta", 2 to "Lenta", 3 to "Média", 4 to "Rápida").forEach { (speedValue, label) ->
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text(label, color = if (autoScrollSpeed == speedValue) MaterialTheme.colorScheme.primary else Color.Unspecified) },
                                onClick = {
                                    autoScrollSpeed = speedValue
                                    menuExpanded = false
                                }
                            )
                        }
                    }
                }

                if (autoScrollSpeed > 0) {
                    val minutes = scrollDurationSeconds / 60
                    val seconds = scrollDurationSeconds % 60
                    Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(8.dp)) {
                        Text(
                            text = String.format("%02d:%02d", minutes, seconds),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        val isContinuousMode = prefsManager.isContinuousMode()
        
        LazyColumn(state = scrollState, modifier = Modifier.weight(1f).padding(horizontal = 16.dp).padding(top = 8.dp)) {
            if (isContinuousMode && repertoire != null) {
                val ids = com.example.util.RepertoireUtil.getFlatManuscriptIds(repertoire!!)
                items(ids.size) { i ->
                    ContinuousChartItem(
                        songChartId = ids[i],
                        repertoireId = repertoireId!!,
                        viewModel = viewModel,
                        chordColor = chordColor,
                        textColor = textColor,
                        fontSizeSp = fontSizeSp
                    )
                }
            } else {
                items(lines.size) { idx ->
                    val lineText = lines[idx]
                    val annotatedString = remember(lineText) {
                        buildAnnotatedString {
                            val tokenRegex = Regex("(\\s+)|([^\\s]+)")
                            for (match in tokenRegex.findAll(lineText)) {
                                val token = match.value
                                if (token.trim().isNotEmpty() && ChordTransposer.isChord(token)) {
                                    withStyle(style = SpanStyle(color = chordColor, fontWeight = FontWeight.Bold)) {
                                        append(token)
                                    }
                                } else {
                                    append(token)
                                }
                            }
                        }
                    }
                    Text(
                        text = annotatedString,
                        color = textColor,
                        fontFamily = FontFamily.SansSerif,
                        fontSize = fontSizeSp.sp,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }

        if (repertoire != null && !isTouchLocked && (!isStageMode || showHud)) {
            val ids = com.example.util.RepertoireUtil.getFlatManuscriptIds(repertoire!!)
            val idx = ids.indexOf(songChartId)
            if (idx != -1) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (idx > 0) {
                            androidx.compose.material3.TextButton(onClick = { onNavigateToSongChart(ids[idx - 1]) }) {
                                Icon(Icons.Default.ChevronLeft, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Música Anterior", style = MaterialTheme.typography.labelLarge)
                            }
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }

                        if (idx < ids.size - 1) {
                            androidx.compose.material3.TextButton(onClick = { 
                                if (prefsManager.isCountdownEnabled()) {
                                    countdownNextChartId = ids[idx + 1]
                                } else {
                                    onNavigateToSongChart(ids[idx + 1])
                                }
                            }) {
                                Text("Próxima Música", style = MaterialTheme.typography.labelLarge)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(androidx.compose.material.icons.Icons.Default.ChevronRight, contentDescription = null)
                            }
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }
                    }
                }
            }
        }
    }
}
}
