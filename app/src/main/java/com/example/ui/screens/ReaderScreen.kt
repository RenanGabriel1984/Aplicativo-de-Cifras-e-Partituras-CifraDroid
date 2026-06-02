package com.example.ui.screens

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.DataProvider
import com.example.ui.MainViewModel
import kotlinx.coroutines.launch

import androidx.compose.ui.platform.LocalContext
import coil.imageLoader
import coil.request.ImageRequest
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.withStyle
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.example.R

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.MainActivity

enum class LiturgicalTheme(val color: androidx.compose.ui.graphics.Color, val label: String) {
    CLASSIC(androidx.compose.ui.graphics.Color.Transparent, "Clássico"),
    ADVENT(androidx.compose.ui.graphics.Color(0xFF6A0DAD).copy(alpha = 0.05f), "Advento"),
    LENT(androidx.compose.ui.graphics.Color(0xFF4B0082).copy(alpha = 0.05f), "Quaresma"),
    EASTER(androidx.compose.ui.graphics.Color(0xFFFFD700).copy(alpha = 0.05f), "Páscoa"),
    PENTECOST(androidx.compose.ui.graphics.Color(0xFFFF0000).copy(alpha = 0.05f), "Pentecostes")
}

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReaderScreen(
    viewModel: MainViewModel,
    manuscriptId: Int,
    repertoireId: Int? = null,
    onNavigateBack: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToManuscript: (Int) -> Unit = {}
) {
    val manuscript by viewModel.getById(manuscriptId).collectAsStateWithLifecycle(initialValue = null)
    val repertoire by if (repertoireId != null) viewModel.getRepertoire(repertoireId).collectAsStateWithLifecycle(initialValue = null) else remember { mutableStateOf(null) }
    
    val isVerticalScroll by viewModel.isVerticalScroll.collectAsStateWithLifecycle()
    val uiState = com.example.ui.state.rememberReaderUiState()
    val isStageMode by viewModel.isStageMode.collectAsStateWithLifecycle()
    val isChoirMode by viewModel.isChoirMode.collectAsStateWithLifecycle()
    val liturgicalTheme by viewModel.liturgicalTheme.collectAsStateWithLifecycle()
    val autoScrollSpeed by viewModel.autoScrollSpeed.collectAsStateWithLifecycle()
    
    val currentRole by com.example.util.SessionManager.currentRole.collectAsStateWithLifecycle()
    val syncEvent by com.example.util.SessionManager.syncEvents.collectAsStateWithLifecycle()
    
    val context = LocalContext.current

    DisposableEffect(Unit) {
        val window = context.findActivity()?.window
        if (window != null) {
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            if (window != null) {
                val controller = WindowCompat.getInsetsController(window, window.decorView)
                controller.show(WindowInsetsCompat.Type.systemBars())
                window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }

    LaunchedEffect(manuscript?.localUri) {
        val uri = manuscript?.localUri
        if (!uri.isNullOrBlank()) {
            uiState.isLoading = true
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                uiState.localDocument = com.example.util.DocumentReader.loadDocument(context, uri)
            }
            uiState.isLoading = false
        }
    }

    val currentDocument = uiState.localDocument
    DisposableEffect(currentDocument) {
        onDispose {
            if (currentDocument is com.example.util.DocumentContent.PdfDoc) {
                currentDocument.engine.close()
            }
        }
    }

    val defaultPages = com.example.data.DataProvider.readerPages
    val pageCount = when (uiState.localDocument) {
        is com.example.util.DocumentContent.PdfDoc -> (uiState.localDocument as com.example.util.DocumentContent.PdfDoc).engine.pageCount
        else -> defaultPages.size
    }
    val pagerState = rememberPagerState(pageCount = { pageCount })
    val coroutineScope = rememberCoroutineScope()
    
    val view = androidx.compose.ui.platform.LocalView.current
    val window = (context as? android.app.Activity)?.window

    DisposableEffect(window) {
        if (window != null) {
            val insetsController = androidx.core.view.WindowCompat.getInsetsController(window, window.decorView)
            insetsController.systemBarsBehavior = androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            insetsController.hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())
        }
        onDispose {
            if (window != null) {
                val insetsController = androidx.core.view.WindowCompat.getInsetsController(window, window.decorView)
                insetsController.show(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    val attemptNextPageOrNextSong = {
        view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
        if (uiState.isPerformanceMode) {
            // Let the built-in system handle it when in Performance Mode by interacting with the list. 
            // Wait, we can animate the scroll state. But I don't have perfScrollState here.
        }
        if (!uiState.isPerformanceMode && pagerState.currentPage < pageCount - 1) {
            coroutineScope.launch { 
                pagerState.animateScrollToPage(
                    pagerState.currentPage + 1,
                    animationSpec = androidx.compose.animation.core.tween(500, easing = androidx.compose.animation.core.FastOutSlowInEasing)
                ) 
            }
        } else if (repertoire != null) {
            try {
                val arr = org.json.JSONArray(repertoire!!.manuscriptIdsJson)
                val ids = List(arr.length()) { i -> arr.getInt(i) }
                val currentIndex = ids.indexOf(manuscriptId)
                if (currentIndex in 0 until ids.size - 1) {
                    onNavigateToManuscript(ids[currentIndex + 1])
                }
            } catch (e: Exception) { }
        }
    }
    
    val attemptPrevPageOrPrevSong = {
        view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
        if (uiState.isPerformanceMode) {
            // System will handle the scroll since it's focused, or we can handle it
        }
        if (!uiState.isPerformanceMode && pagerState.currentPage > 0) {
            coroutineScope.launch { 
                pagerState.animateScrollToPage(
                    pagerState.currentPage - 1,
                    animationSpec = androidx.compose.animation.core.tween(500, easing = androidx.compose.animation.core.FastOutSlowInEasing)
                ) 
            }
        } else if (repertoire != null) {
            try {
                val arr = org.json.JSONArray(repertoire!!.manuscriptIdsJson)
                val ids = List(arr.length()) { i -> arr.getInt(i) }
                val currentIndex = ids.indexOf(manuscriptId)
                if (currentIndex > 0) {
                    onNavigateToManuscript(ids[currentIndex - 1])
                }
            } catch (e: Exception) { }
        }
    }
    
    // Auto-scroll loop
    LaunchedEffect(autoScrollSpeed) {
        if (autoScrollSpeed > 0f) {
            while (true) {
                kotlinx.coroutines.delay((5000 / autoScrollSpeed).toLong().coerceAtLeast(500L))
                attemptNextPageOrNextSong()
            }
        }
    }

    // Follower sync
    LaunchedEffect(syncEvent, currentRole) {
        if (currentRole == com.example.util.SessionRole.FOLLOWER && syncEvent != null) {
            if (syncEvent!!.manuscriptId == manuscriptId && syncEvent!!.pageIndex != pagerState.currentPage) {
                pagerState.animateScrollToPage(syncEvent!!.pageIndex)
            } else if (syncEvent!!.manuscriptId != manuscriptId) {
                onNavigateToManuscript(syncEvent!!.manuscriptId)
            }
        }
    }
    
    // Leader broadcast
    LaunchedEffect(pagerState.currentPage) {
        if (currentRole == com.example.util.SessionRole.LEADER) {
            com.example.util.SessionManager.broadcastPageChange(manuscriptId, pagerState.currentPage)
        }
    }

    val focusRequester = remember { FocusRequester() }
    val toggleHud = { uiState.showHud = !uiState.showHud }
    var lastKeystrokeTime by remember { mutableLongStateOf(0L) }
    var hudInteractionTime by remember { mutableLongStateOf(0L) }
    
    androidx.activity.compose.BackHandler(enabled = uiState.showHud) {
        uiState.showHud = false
    }
    
    // Auto-hide HUD natively
    LaunchedEffect(uiState.showHud, hudInteractionTime, pagerState.currentPage) {
        if (uiState.showHud) {
            kotlinx.coroutines.delay(20000)
            uiState.showHud = false
        }
    }
    
    val pagerModifier = Modifier
        .fillMaxSize()
        .pointerInput(isStageMode) {
            detectTapGestures(
                onTap = { offset ->
                    if (!isStageMode) {
                        val width = size.width
                        val x = offset.x
                        val leftBoundary = width * 0.3f
                        val rightBoundary = width * 0.7f
                        if (x < leftBoundary) {
                            attemptPrevPageOrPrevSong()
                        } else if (x > rightBoundary) {
                            attemptNextPageOrNextSong()
                        } else {
                            toggleHud()
                            hudInteractionTime = System.currentTimeMillis()
                        }
                    }
                }
            )
        }
    
    // Auto-preload neighbor pages for zero-latency pedal turns
    LaunchedEffect(pagerState.currentPage) {
        val prev = pagerState.currentPage - 1
        val next = pagerState.currentPage + 1
        
        if (uiState.localDocument == null) {
            if (prev >= 0 && prev < defaultPages.size) {
                val request = ImageRequest.Builder(context).data(defaultPages[prev]).build()
                context.imageLoader.enqueue(request)
            }
            if (next >= 0 && next < defaultPages.size) {
                val request = ImageRequest.Builder(context).data(defaultPages[next]).build()
                context.imageLoader.enqueue(request)
            }
        }
    }

    val currentManuscript = manuscript
    var showTransposePanel by remember { mutableStateOf(false) }
    
    val storedPref by viewModel.getPreferredKey(manuscriptId).collectAsStateWithLifecycle(null)
    val defaultKey = if (currentManuscript?.keySignature.isNullOrBlank()) {
        if (currentManuscript?.tone.isNullOrBlank()) "C" else currentManuscript!!.tone
    } else currentManuscript!!.keySignature
    
    val currentKey = storedPref?.preferredKey?.takeIf { it.isNotBlank() } ?: defaultKey

    if (currentManuscript != null) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(100)
            try {
                focusRequester.requestFocus()
            } catch (e: Exception) {
                // Ignore if not focused immediately
            }
        }
        
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black).windowInsetsPadding(WindowInsets.safeDrawing), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black) // Deep black for reader
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                .focusRequester(focusRequester)
                .focusable()
                .onKeyEvent { keyEvent ->
                    if (uiState.isPerformanceMode) {
                        return@onKeyEvent false
                    }
                    when (keyEvent.key) {
                        Key.DirectionLeft, Key.PageUp, Key.DirectionUp -> {
                            if (keyEvent.type == KeyEventType.KeyUp) {
                                val now = System.currentTimeMillis()
                                if (now - lastKeystrokeTime > 300) {
                                    lastKeystrokeTime = now
                                    attemptPrevPageOrPrevSong()
                                }
                            }
                            true
                        }
                        Key.DirectionRight, Key.PageDown, Key.DirectionDown, Key.Spacebar, Key.Enter -> {
                            if (keyEvent.type == KeyEventType.KeyUp) {
                                val now = System.currentTimeMillis()
                                if (now - lastKeystrokeTime > 300) {
                                    lastKeystrokeTime = now
                                    attemptNextPageOrNextSong()
                                }
                            }
                            true
                        }
                        else -> false
                    }
                }
        ) {
            Column(modifier = Modifier.fillMaxSize().background(if (uiState.isPerformanceMode) Color.Black else Color.Transparent)) {
                Box(modifier = Modifier.weight(1f)) {
                    if (uiState.isPerformanceMode) {
                        val pdfContent by viewModel.getPdfText(manuscriptId).collectAsStateWithLifecycle(null)
                        val rawText = pdfContent?.content ?: currentManuscript?.extractedText ?: ""
                        if (rawText.isBlank()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Este PDF não possui texto selecionável e não pode utilizar o Modo Performance.", 
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(32.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        } else {
                            val prefs = context.getSharedPreferences("performance_prefs", Context.MODE_PRIVATE)
                            var fontSizeMultiplier by remember { mutableFloatStateOf(prefs.getFloat("font_size_$manuscriptId", 1.5f)) }
                            val transposedText = remember(rawText, defaultKey, currentKey) {
                                val stepsForText = com.example.util.ChordTransposer.getStepsBetween(defaultKey, currentKey)
                                val isFlatsText = currentKey.contains("b") || currentKey == "F"
                                if (stepsForText == 0) rawText else com.example.util.ChordTransposer.transposeText(rawText, stepsForText, isFlatsText)
                            }
                            
                            val perfScrollState = androidx.compose.foundation.lazy.rememberLazyListState()
                            
                            // Auto scroll for performance mode
                            LaunchedEffect(autoScrollSpeed) {
                                if (autoScrollSpeed > 0f) {
                                    while (true) {
                                        kotlinx.coroutines.delay((1000 / autoScrollSpeed).toLong().coerceAtLeast(100L))
                                        perfScrollState.animateScrollBy(50f)
                                    }
                                }
                            }
                            
                            Box(modifier = Modifier.fillMaxSize().pointerInput(isStageMode) {
                                detectTapGestures(
                                    onTap = { offset ->
                                        if (!isStageMode) {
                                            val width = size.width
                                            val x = offset.x
                                            val leftBoundary = width * 0.3f
                                            val rightBoundary = width * 0.7f
                                            val viewportHeight = size.height
                                            coroutineScope.launch {
                                                if (x < leftBoundary) {
                                                    perfScrollState.animateScrollBy(-(viewportHeight * 0.8).toFloat())
                                                } else if (x > rightBoundary) {
                                                    perfScrollState.animateScrollBy((viewportHeight * 0.8).toFloat())
                                                } else {
                                                    toggleHud()
                                                    hudInteractionTime = System.currentTimeMillis()
                                                }
                                            }
                                        }
                                    }
                                )
                            }) {
                                val lines = remember(transposedText) { transposedText.split("\n") }
                                androidx.compose.foundation.lazy.LazyColumn(state = perfScrollState, modifier = Modifier.fillMaxSize().padding(16.dp)) {
                                    item {
                                        Column(modifier = Modifier.fillMaxWidth().background(Color.DarkGray).padding(8.dp)) {
                                            Text("TELEMETRIA (Diagnóstico)", color=Color.Yellow, fontWeight=androidx.compose.ui.text.font.FontWeight.Bold)
                                            Text("Caracteres Extraídos: ${rawText.length}", color=Color.White, fontSize=12.sp)
                                            Text("Linhas: ${lines.size}", color=Color.White, fontSize=12.sp)
                                            Text("Primeiras 10 linhas originais (s/ transp):", color=Color.Cyan, fontSize=12.sp)
                                            val top10 = rawText.lines().take(10).joinToString("\n")
                                            Text(top10, color=Color.White, fontSize=12.sp, fontFamily=androidx.compose.ui.text.font.FontFamily.Monospace)
                                        }
                                    }
                                    
                                    if (uiState.showHud) {
                                        item {
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("Tamanho da Fonte:", color = Color.White)
                                                Row {
                                                    listOf(1.0f, 1.25f, 1.5f, 1.75f, 2.0f).forEach { mult ->
                                                        val isSelected = fontSizeMultiplier == mult
                                                        Surface(
                                                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.DarkGray,
                                                            shape = RoundedCornerShape(8.dp),
                                                            modifier = Modifier.padding(end = 4.dp).clickable {
                                                                fontSizeMultiplier = mult
                                                                prefs.edit().putFloat("font_size_$manuscriptId", mult).apply()
                                                            }
                                                        ) {
                                                            Text("${(mult * 100).toInt()}%", color = if (isSelected) MaterialTheme.colorScheme.onPrimary else Color.White, modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.labelSmall)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    
                                    val chordColor = androidx.compose.ui.graphics.Color(0xFF64B5F6) // Light Blue 300
                                    
                                    items(lines.size) { idx ->
                                        val lineText = lines[idx]
                                        val annotatedString = remember(lineText) {
                                            androidx.compose.ui.text.buildAnnotatedString {
                                                var lastIndex = 0
                                                val chordRegex = Regex("\\b[A-G](?:#|b)?(?:m|maj|min|aug|dim)?(?:[0-9])?(?:sus[24])?(?:/[A-G](?:#|b)?)?\\b")
                                                for (match in chordRegex.findAll(lineText)) {
                                                    append(lineText.substring(lastIndex, match.range.first))
                                                    withStyle(style = androidx.compose.ui.text.SpanStyle(color = chordColor, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)) {
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
                                            style = androidx.compose.ui.text.TextStyle(
                                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                                fontSize = (14 * fontSizeMultiplier).sp,
                                                lineHeight = (22 * fontSizeMultiplier).sp
                                            ),
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Pager allows smooth transitions and intelligent 'beyondBoundsPageCount' preloads 1 page ahead/behind
                        if (isVerticalScroll) {
                            VerticalPager(
                                state = pagerState,
                                modifier = pagerModifier,
                                beyondViewportPageCount = 1
                            ) { page ->
                                PageContent(page, uiState.localDocument, defaultPages, isChoirMode)
                            }
                        } else {
                            HorizontalPager(
                                state = pagerState,
                                modifier = pagerModifier,
                                beyondViewportPageCount = 1
                            ) { page ->
                                PageContent(page, uiState.localDocument, defaultPages, isChoirMode)
                            }
                        }
                        
                        if (liturgicalTheme != LiturgicalTheme.CLASSIC) {
                            Box(modifier = Modifier.fillMaxSize().background(liturgicalTheme.color))
                        }
                    }
                } // End inner Box
            } // End Column

            // HUD
            AnimatedVisibility(
                visible = uiState.showHud,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Top Bar
                    Surface(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        modifier = Modifier.align(Alignment.TopCenter)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 24.dp) 
                        ) {
                            IconButton(onClick = onNavigateBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = MaterialTheme.colorScheme.primary)
                            }
                            
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                Text(
                                    text = repertoire?.name ?: currentManuscript?.title ?: "",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (repertoire != null) {
                                        val idx = (try {
                                            val arr = org.json.JSONArray(repertoire!!.manuscriptIdsJson)
                                            val ids = List(arr.length()) { i -> arr.getInt(i) }
                                            ids.indexOf(manuscriptId)
                                        } catch(e: Exception) { -1 }) + 1
                                        val total = (try { org.json.JSONArray(repertoire!!.manuscriptIdsJson).length() } catch(e: Exception){ 0 })
                                        "${currentManuscript?.title ?: ""} - $idx de $total"
                                    } else {
                                        currentManuscript?.composer ?: ""
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                
                                val networkStatus by com.example.util.SessionNetworkManager.connectionStatus.collectAsStateWithLifecycle()
                                val serverIpAndPort by com.example.util.SessionNetworkManager.serverIpAndPort.collectAsStateWithLifecycle()
                                val clientsCount by com.example.util.SessionNetworkManager.connectedClientsCount.collectAsStateWithLifecycle()
                                if (networkStatus != "Desconectado") {
                                    val statusDot = when {
                                        networkStatus.contains("Conectado") || networkStatus.contains("Sessão Aberta") -> "🟢"
                                        networkStatus.contains("Reconectando") -> "🟡"
                                        else -> "🔴"
                                    }
                                    val connectedColor = if (networkStatus.contains("Conectado") || networkStatus.contains("Sessão Aberta")) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error

                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.padding(top = 4.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
                                            Text(statusDot, modifier = Modifier.padding(end = 6.dp), style = MaterialTheme.typography.labelSmall)
                                            Text(
                                                text = "$networkStatus ${if(serverIpAndPort != null) "| $serverIpAndPort" else ""} ${if(clientsCount > 0) "| $clientsCount devs" else ""}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = connectedColor
                                            )
                                        }
                                    }
                                }
                                
                                if (syncEvent?.note != null && currentRole == com.example.util.SessionRole.FOLLOWER) {
                                    Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(8.dp), modifier = Modifier.padding(top = 16.dp).fillMaxWidth()) {
                                        Text("Aviso Maestro: ${syncEvent!!.note}", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    }
                                }
                            }
                            
                            var showMenu by remember { mutableStateOf(false) }
                            
                            Box {
                                IconButton(onClick = { showMenu = true }) {
                                    Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings_desc), tint = MaterialTheme.colorScheme.primary)
                                }
                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(if (isVerticalScroll) "Mudar para rolagem horizontal" else "Mudar para rolagem vertical") },
                                        onClick = {
                                            viewModel.setVerticalScroll(!isVerticalScroll)
                                            showMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(if (uiState.isPerformanceMode) "Desativar Modo Performance" else "Modo Performance") },
                                        onClick = {
                                            uiState.isPerformanceMode = !uiState.isPerformanceMode
                                            uiState.showHud = false
                                            showMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Transposição...") },
                                        onClick = {
                                            showTransposePanel = true
                                            showMenu = false
                                            uiState.showHud = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Configurações Gerais...") },
                                        onClick = {
                                            showMenu = false
                                            onNavigateToSettings()
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Bottom Bar
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.95f),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 32.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                IconButton(onClick = { 
                                    attemptPrevPageOrPrevSong()
                                }) {
                                    Icon(Icons.Default.ChevronLeft, contentDescription = stringResource(R.string.previous))
                                }
                                
                                Text(
                                    text = "${pagerState.currentPage + 1} / $pageCount",
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                
                                IconButton(onClick = { 
                                    attemptNextPageOrNextSong()
                                }) {
                                    Icon(Icons.Default.ChevronRight, contentDescription = stringResource(R.string.next))
                                }
                            }
                        }
                    }
                }
            }
            
            if (showTransposePanel) {
                var localSelectedKey by remember { mutableStateOf(currentKey) }

                @OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
                androidx.compose.material3.ModalBottomSheet(
                    onDismissRequest = { showTransposePanel = false },
                    sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally, 
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 24.dp)
                    ) {
                        Text("Transposição", style = MaterialTheme.typography.titleLarge, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Tom Original", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(defaultKey, style = MaterialTheme.typography.headlineMedium)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Tom Selecionado", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(localSelectedKey, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                            Button(onClick = {
                                localSelectedKey = com.example.util.ChordTransposer.transposeText(localSelectedKey, -1, useFlats = true)
                            }) {
                                Text("-1")
                            }
                            Button(onClick = {
                                localSelectedKey = com.example.util.ChordTransposer.transposeText(localSelectedKey, 1, useFlats = false)
                            }) {
                                Text("+1")
                            }
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                            TextButton(onClick = { 
                                showTransposePanel = false
                            }) {
                                Text("Cancelar")
                            }
                            Button(onClick = {
                                viewModel.savePreferredKey(manuscriptId, localSelectedKey)
                                showTransposePanel = false
                            }) {
                                Text("Aplicar")
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        } // Close Box
        } // Close else if (!isLoading)
    } else {
        // Loading State
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun PageContent(page: Int, localDocument: com.example.util.DocumentContent?, defaultPages: List<String>, isChoirMode: Boolean = false) {
    val scrollState = androidx.compose.foundation.rememberScrollState()
    
    // Smart Reading Mode: dynamic scale based on scroll offset without recomposing
    val animatedViewportScale = remember { 
        derivedStateOf {
            if (scrollState.maxValue <= 0) return@derivedStateOf 1f
            val value = scrollState.value.toFloat()
            val max = scrollState.maxValue.toFloat()
            
            val threshold = max * 0.1f
            if (threshold <= 0) return@derivedStateOf 1f
            
            when {
                value < threshold -> 1f + (0.15f * (1f - (value / threshold)))
                value > max - threshold -> 1f + (0.15f * ((value - (max - threshold)) / threshold))
                else -> 1f
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Transparent).verticalScroll(scrollState)) {
        if (localDocument is com.example.util.DocumentContent.PdfDoc) {
            var bitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
            var hasError by remember { mutableStateOf(false) }
            var errorReport by remember { mutableStateOf("") }
            var retryKey by remember { mutableIntStateOf(0) }
            
            LaunchedEffect(page, retryKey, localDocument) {
                hasError = false
                bitmap = null
                try {
                    val result = kotlinx.coroutines.withTimeoutOrNull(5000L) {
                        localDocument.engine.renderPage(page, scale = 1.2f)
                    }
                    if (result == null) {
                        hasError = true
                        errorReport = "Timeout (5000ms) atingido antes do término do renderPage."
                    } else {
                        bitmap = result
                    }
                } catch (e: Exception) {
                    hasError = true
                    errorReport = e.message ?: e.stackTraceToString()
                }
            }
            
            if (bitmap != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Image(
                        bitmap = bitmap!!.asImageBitmap(),
                        contentDescription = stringResource(R.string.page_desc, page + 1),
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 24.dp)
                            .graphicsLayer {
                                val totalScale = animatedViewportScale.value
                                scaleX = totalScale
                                scaleY = totalScale
                            }
                    )
                }
            } else if (hasError) {
                Box(modifier = Modifier.fillMaxWidth().height(400.dp).background(Color.White).padding(8.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Info, contentDescription = "Error", tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Erro ao carregar renderização da pág. ${page + 1}", color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        val scrState = androidx.compose.foundation.rememberScrollState()
                        Text(
                            text = errorReport,
                            color = Color.Red,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = false)
                                .verticalScroll(scrState)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { retryKey++ }) {
                            Text("Tentar Novamente")
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Carregando página...", color = Color.Gray)
                    }
                }
            }
        } else {
            AsyncImage(
                model = defaultPages.getOrNull(page),
                contentDescription = stringResource(R.string.page_desc, page + 1),
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
