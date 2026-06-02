package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongChartScreen(
    songChartId: Int,
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val songChart by viewModel.getSongChartById(songChartId).collectAsStateWithLifecycle(null)

    if (songChart == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Cifra não disponível.", color = MaterialTheme.colorScheme.onSurface)
        }
        return
    }

    val chart = songChart!!
    val currentKey = chart.savedKey ?: chart.originalKey

    val transposedText = remember(chart.content, chart.originalKey, currentKey) {
        val stepsForText = ChordTransposer.getStepsBetween(chart.originalKey, currentKey)
        val isFlatsText = currentKey.contains("b") || currentKey == "F"
        if (stepsForText == 0) chart.content else ChordTransposer.transposeText(chart.content, stepsForText, isFlatsText)
    }

    val lines = remember(transposedText) { transposedText.split("\n") }
    val chordColor = Color(0xFF64B5F6) // Light Blue 300
    
    val scrollState = androidx.compose.foundation.lazy.rememberLazyListState()

    Column(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Box(modifier = Modifier.fillMaxWidth().background(Color(0xFF1E1E1E)).padding(horizontal = 4.dp, vertical = 8.dp)) {
             Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                 Row(verticalAlignment = Alignment.CenterVertically) {
                     IconButton(onClick = onBack) {
                         Icon(Icons.Default.ChevronLeft, contentDescription = "Voltar", tint = Color.White)
                     }
                     Text(chart.title, color = Color.White, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                 }
                 Row(verticalAlignment = Alignment.CenterVertically) {
                     IconButton(onClick = { 
                         val nextKey = ChordTransposer.transposeText(currentKey, -1, useFlats = true)
                         viewModel.updateSongChartKey(chart.id, nextKey)
                     }) {
                         Text("-", color = Color.White, fontSize = 24.sp)
                     }
                     Text(currentKey, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                     IconButton(onClick = { 
                         val nextKey = ChordTransposer.transposeText(currentKey, 1, useFlats = false)
                         viewModel.updateSongChartKey(chart.id, nextKey)
                     }) {
                         Text("+", color = Color.White, fontSize = 24.sp)
                     }
                 }
             }
        }

        LazyColumn(state = scrollState, modifier = Modifier.fillMaxSize().padding(16.dp)) {
            items(lines.size) { idx ->
                val lineText = lines[idx]
                val annotatedString = remember(lineText) {
                    buildAnnotatedString {
                        var lastIndex = 0
                        val chordRegex = Regex("\\b[A-G](?:#|b)?(?:m|maj|min|aug|dim)?(?:[0-9])?(?:sus[24])?(?:/[A-G](?:#|b)?)?\\b")
                        for (match in chordRegex.findAll(lineText)) {
                            append(lineText.substring(lastIndex, match.range.first))
                            withStyle(style = SpanStyle(color = chordColor, fontWeight = FontWeight.Bold)) {
                                append(match.value)
                            }
                            lastIndex = match.range.last + 1
                        }
                        append(lineText.substring(lastIndex))
                    }
                }
                Text(
                    text = annotatedString,
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}
