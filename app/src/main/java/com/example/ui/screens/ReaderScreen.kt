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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    onNavigateToManuscript: (Int) -> Unit = {}
) {
    val manuscript by viewModel.getById(manuscriptId).collectAsStateWithLifecycle(initialValue = null)
    val repertoire by if (repertoireId != null) viewModel.getRepertoire(repertoireId).collectAsStateWithLifecycle(initialValue = null) else remember { mutableStateOf(null) }
    
    val isVerticalScroll by viewModel.isVerticalScroll.collectAsStateWithLifecycle()
    var showHud by remember { mutableStateOf(false) }
    var isStageMode by remember { mutableStateOf(false) }
    var isChoirMode by remember { mutableStateOf(false) }
    var autoScrollSpeed by remember { mutableFloatStateOf(0f) }
    var liturgicalTheme by remember { mutableStateOf(LiturgicalTheme.CLASSIC) }
    
    val currentRole by com.example.util.SessionManager.currentRole.collectAsStateWithLifecycle()
    val syncEvent by com.example.util.SessionManager.syncEvents.collectAsStateWithLifecycle()
    
    val context = LocalContext.current
    var localDocument by remember { mutableStateOf<com.example.util.DocumentContent?>(null) }
    var isLoading by remember { mutableStateOf(false) }

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
            isLoading = true
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                localDocument = com.example.util.DocumentReader.loadDocument(context, uri)
            }
            isLoading = false
        }
    }

    DisposableEffect(localDocument) {
        onDispose {
            if (localDocument is com.example.util.DocumentContent.PdfDoc) {
                (localDocument as com.example.util.DocumentContent.PdfDoc).engine.close()
            }
        }
    }

    val defaultPages = com.example.data.DataProvider.readerPages
    val pageCount = when (localDocument) {
        is com.example.util.DocumentContent.PdfDoc -> (localDocument as com.example.util.DocumentContent.PdfDoc).engine.pageCount
        else -> defaultPages.size
    }
    val pagerState = rememberPagerState(pageCount = { pageCount })
    val coroutineScope = rememberCoroutineScope()
    
    val view = androidx.compose.ui.platform.LocalView.current

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
    val toggleHud = { showHud = !showHud }
    var lastKeystrokeTime by remember { mutableLongStateOf(0L) }
    
    // Auto-hide HUD natively
    LaunchedEffect(showHud, pagerState.currentPage) {
        if (showHud) {
            kotlinx.coroutines.delay(4000)
            showHud = false
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
                        // 1f, 1.5f, 1f hit zones mapped to width
                        val leftBoundary = width * (1f / 3.5f)
                        val rightBoundary = width * (2.5f / 3.5f)
                        if (x < leftBoundary) {
                            attemptPrevPageOrPrevSong()
                        } else if (x > rightBoundary) {
                            attemptNextPageOrNextSong()
                        }
                    }
                },
                onLongPress = { toggleHud() }
            )
        }
    
    // Auto-preload neighbor pages for zero-latency pedal turns
    LaunchedEffect(pagerState.currentPage) {
        val prev = pagerState.currentPage - 1
        val next = pagerState.currentPage + 1
        
        if (localDocument == null) {
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
        
        if (isLoading) {
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
            // Pager allows smooth transitions and intelligent 'beyondBoundsPageCount' preloads 1 page ahead/behind
            if (isVerticalScroll) {
                VerticalPager(
                    state = pagerState,
                    modifier = pagerModifier,
                    beyondViewportPageCount = 1
                ) { page ->
                    PageContent(page, localDocument, defaultPages, isChoirMode)
                }
            } else {
                HorizontalPager(
                    state = pagerState,
                    modifier = pagerModifier,
                    beyondViewportPageCount = 1
                ) { page ->
                    PageContent(page, localDocument, defaultPages, isChoirMode)
                }
            }
            
            if (liturgicalTheme != LiturgicalTheme.CLASSIC) {
                Box(modifier = Modifier.fillMaxSize().background(liturgicalTheme.color))
            }

            // HUD
            AnimatedVisibility(
                visible = showHud,
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
                                    text = repertoire?.name ?: currentManuscript.title,
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
                                        "${currentManuscript.title} - $idx de $total"
                                    } else {
                                        currentManuscript.composer
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
                                        text = { Text(if (isStageMode) "Desativar Modo Palco" else "Ativar Modo Palco") },
                                        onClick = {
                                            isStageMode = !isStageMode
                                            showHud = false
                                            showMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(if (isChoirMode) "Desativar Modo Coral" else "Ativar Modo Coral") },
                                        onClick = {
                                            isChoirMode = !isChoirMode
                                            showHud = false
                                            showMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Tema Litúrgico: ${liturgicalTheme.label}") },
                                        onClick = {
                                            val values = LiturgicalTheme.values()
                                            liturgicalTheme = values[(liturgicalTheme.ordinal + 1) % values.size]
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Desconectar Sessão") },
                                        onClick = {
                                            com.example.util.SessionNetworkManager.stopAll()
                                            showMenu = false
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
                            
                            Slider(
                                value = autoScrollSpeed,
                                onValueChange = { autoScrollSpeed = it },
                                valueRange = 0f..5f,
                                steps = 5,
                                modifier = Modifier.padding(horizontal = 32.dp).fillMaxWidth(0.5f)
                            )
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
fun PageContent(page: Int, localDocument: com.example.util.DocumentContent?, defaultPages: List<String>, isChoirMode: Boolean = false) {
    val scrollState = androidx.compose.foundation.rememberScrollState()
    
    // Smart Reading Mode: dynamic scale based on scroll offset without recomposing
    val animatedViewportScale = remember { androidx.compose.animation.core.Animatable(1f) }
    
    LaunchedEffect(scrollState) {
        androidx.compose.runtime.snapshotFlow {
            if (scrollState.maxValue <= 0) return@snapshotFlow 1f
            val value = scrollState.value.toFloat()
            val max = scrollState.maxValue.toFloat()
            
            val threshold = max * 0.1f
            if (threshold <= 0) return@snapshotFlow 1f
            
            when {
                value < threshold -> 1f + (0.15f * (1f - (value / threshold)))
                value > max - threshold -> 1f + (0.15f * ((value - (max - threshold)) / threshold))
                else -> 1f
            }
        }.collect { targetScale ->
            animatedViewportScale.animateTo(
                targetValue = targetScale,
                animationSpec = androidx.compose.animation.core.spring(
                    dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
                    stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                )
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Transparent).verticalScroll(scrollState)) {
        if (localDocument is com.example.util.DocumentContent.PdfDoc) {
            var bitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
            var hasError by remember { mutableStateOf(false) }
            var retryKey by remember { mutableIntStateOf(0) }
            
            LaunchedEffect(page, retryKey) {
                hasError = false
                bitmap = null
                val result = kotlinx.coroutines.withTimeoutOrNull(5000L) {
                    localDocument.engine.renderPage(page, scale = 1.5f)
                }
                if (result == null) {
                    hasError = true
                } else {
                    bitmap = result
                }
            }
            
            if (bitmap != null) {
                var scale by remember { mutableFloatStateOf(1f) }
                var offsetX by remember { mutableStateOf(0f) }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(1f, 3f)
                                val maxX = ((size.width * scale - size.width) / 2).coerceAtLeast(0f)
                                if (scale > 1f) {
                                    offsetX = (offsetX + pan.x).coerceIn(-maxX, maxX)
                                } else {
                                    offsetX = 0f
                                }
                            }
                        }
                ) {
                    androidx.compose.foundation.Image(
                        bitmap = bitmap!!.asImageBitmap(),
                        contentDescription = stringResource(R.string.page_desc, page + 1),
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                val totalScale = scale * animatedViewportScale.value
                                scaleX = totalScale
                                scaleY = totalScale
                                translationX = offsetX
                            }
                    )
                }
            } else if (hasError) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Erro ao carregar renderização", color = Color.Red)
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
