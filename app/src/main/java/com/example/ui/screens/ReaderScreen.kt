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
        if (pagerState.currentPage < pageCount - 1) {
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
        if (pagerState.currentPage > 0) {
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
                    if (uiState.showMusicList || uiState.selectedSongChartId != null) {
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
            Column(modifier = Modifier.fillMaxSize().background(if (uiState.showMusicList) Color.Black else Color.Transparent)) {
                Box(modifier = Modifier.weight(1f)) {
                    if (uiState.showImportDiagnostic) {
                        DiagnosticScreen(
                            manuscriptId = manuscriptId,
                            viewModel = viewModel,
                            onBack = { uiState.showImportDiagnostic = false }
                        )
                    } else if (uiState.selectedSongChartId != null) {
                        SongChartScreen(
                            songChartId = uiState.selectedSongChartId!!,
                            viewModel = viewModel,
                            onBack = { uiState.selectedSongChartId = null }
                        )
                    } else if (uiState.showMusicList) {
                        SongListScreen(
                            manuscriptId = manuscriptId,
                            viewModel = viewModel,
                            onSongChartSelected = { selectedId ->
                                uiState.selectedSongChartId = selectedId
                            }
                        )
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
                                        text = { Text("Configurações Gerais...") },
                                        onClick = {
                                            showMenu = false
                                            onNavigateToSettings()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Diagnóstico da Importação") },
                                        onClick = {
                                            showMenu = false
                                            uiState.showImportDiagnostic = true
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
                            @OptIn(ExperimentalMaterial3Api::class)
                            SingleChoiceSegmentedButtonRow(modifier = Modifier.padding(top = 8.dp, start=16.dp, end=16.dp)) {
                                SegmentedButton(
                                    selected = !uiState.showMusicList,
                                    onClick = { 
                                        uiState.showMusicList = false
                                        uiState.selectedSongChartId = null
                                    },
                                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                                ) {
                                    Text("PDF")
                                }
                                SegmentedButton(
                                    selected = uiState.showMusicList,
                                    onClick = { uiState.showMusicList = true },
                                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                                ) {
                                    Text("MÚSICAS")
                                }
                            }
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
fun SongListScreen(
    manuscriptId: Int,
    viewModel: MainViewModel,
    onSongChartSelected: (Int) -> Unit
) {
    val songCharts by viewModel.getSongCharts(manuscriptId).collectAsStateWithLifecycle(emptyList())

    if (songCharts.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Nenhuma música encontrada.", color = MaterialTheme.colorScheme.onSurface)
        }
        return
    }

    androidx.compose.foundation.lazy.LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            count = songCharts.size,
            key = { index -> songCharts[index].id }
        ) { index ->
            val chart = songCharts[index]
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSongChartSelected(chart.id) },
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = chart.title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val key = chart.savedKey ?: chart.originalKey
                    if (key.isNotBlank()) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "Tom: $key",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
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
