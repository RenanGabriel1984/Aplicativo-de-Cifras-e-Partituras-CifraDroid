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
import androidx.compose.material.icons.filled.Send
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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.res.stringResource
import com.example.R

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.MainActivity

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
        is com.example.util.DocumentContent.TextDoc -> 1
        is com.example.util.DocumentContent.PdfDoc -> (localDocument as com.example.util.DocumentContent.PdfDoc).engine.pageCount
        else -> defaultPages.size
    }
    val pagerState = rememberPagerState(pageCount = { pageCount })
    val coroutineScope = rememberCoroutineScope()
    
    val attemptNextPageOrNextSong = {
        if (pagerState.currentPage < pageCount - 1) {
            coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
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
        if (pagerState.currentPage > 0) {
            coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
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
                            
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = repertoire?.name ?: currentManuscript.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
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
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                                var showMaestroPanel by remember { mutableStateOf(false) }

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
                                        modifier = Modifier.padding(top = 4.dp).clickable { showMaestroPanel = true }
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

                                if (showMaestroPanel) {
                                    AlertDialog(
                                        onDismissRequest = { showMaestroPanel = false },
                                        title = { Text("Painel Maestro", style = MaterialTheme.typography.titleMedium) },
                                        text = {
                                            Column {
                                                Text("Status: $networkStatus")
                                                if (serverIpAndPort != null) Text("IP Sessão: $serverIpAndPort")
                                                Text("Músicos Conectados: $clientsCount", modifier = Modifier.padding(top = 8.dp))
                                                
                                                if (currentRole == com.example.util.SessionRole.LEADER) {
                                                    var currentNote by remember { mutableStateOf("") }
                                                    OutlinedTextField(
                                                        value = currentNote,
                                                        onValueChange = { currentNote = it },
                                                        label = { Text("Nota para integrantes (Ex: Tom G, Capo 2)") },
                                                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                                                        trailingIcon = {
                                                            IconButton(onClick = {
                                                                com.example.util.SessionManager.broadcastPageChange(manuscriptId, pagerState.currentPage, currentNote)
                                                            }) { Icon(Icons.Default.Send, contentDescription = "Enviar") }
                                                        }
                                                    )
                                                }
                                                
                                                if (syncEvent?.note != null) {
                                                    Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(8.dp), modifier = Modifier.padding(top = 16.dp).fillMaxWidth()) {
                                                        Text("Aviso Maestro: ${syncEvent!!.note}", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onPrimaryContainer)
                                                    }
                                                }
                                            }
                                        },
                                        confirmButton = {
                                            TextButton(onClick = { showMaestroPanel = false }) { Text("Fechar") }
                                        }
                                    )
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
                                        text = { Text("Ser Maestro (Leader)") },
                                        onClick = {
                                            com.example.util.SessionNetworkManager.startServer()
                                            showMenu = false
                                        }
                                    )
                                    var showFollowerDialog by remember { mutableStateOf(false) }
                                    DropdownMenuItem(
                                        text = { Text("Seguir Maestro") },
                                        onClick = {
                                            showFollowerDialog = true
                                            showMenu = false
                                        }
                                    )
                                    if (showFollowerDialog) {
                                        var ipInput by remember { mutableStateOf("") }
                                        AlertDialog(
                                            onDismissRequest = { showFollowerDialog = false },
                                            title = { Text("Conectar ao Maestro", style = MaterialTheme.typography.titleMedium) },
                                            text = {
                                                OutlinedTextField(
                                                    value = ipInput,
                                                    onValueChange = { ipInput = it },
                                                    label = { Text("IP:Porta (ex: 192.168.1.10:8080)") },
                                                    singleLine = true,
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                            },
                                            confirmButton = {
                                                TextButton(onClick = {
                                                    com.example.util.SessionNetworkManager.connectAsFollower(ipInput)
                                                    showFollowerDialog = false
                                                }) { Text("Conectar") }
                                            },
                                            dismissButton = {
                                                TextButton(onClick = { showFollowerDialog = false }) { Text("Cancelar") }
                                            }
                                        )
                                    }
                                    DropdownMenuItem(
                                        text = { Text("Modo Individual") },
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
    val bgColor = if (localDocument is com.example.util.DocumentContent.TextDoc || localDocument is com.example.util.DocumentContent.HtmlDoc) Color(0xFFFCFAF2) else Color.Transparent
    Box(modifier = Modifier
        .fillMaxSize()
        .background(bgColor)
    ) {
        if (localDocument is com.example.util.DocumentContent.TextDoc) {
            val scrollState = rememberScrollState()
            Text(
                text = localDocument.text,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                style = if (isChoirMode) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.bodyLarge,
                color = Color.Black
            )
        } else if (localDocument is com.example.util.DocumentContent.HtmlDoc) {
            androidx.compose.ui.viewinterop.AndroidView(
                factory = { context ->
                    android.webkit.WebView(context).apply {
                        settings.javaScriptEnabled = false
                        setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    }
                },
                update = { webView ->
                    val extraScale = if (isChoirMode) "body { font-size: 150% !important; }" else ""
                    val modifiedHtml = localDocument.html.replace("</style>", "$extraScale</style>")
                    webView.loadDataWithBaseURL(null, modifiedHtml, "text/html", "utf-8", null)
                },
                modifier = Modifier.fillMaxSize()
            )
        } else if (localDocument is com.example.util.DocumentContent.PdfDoc) {
            var bitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
            var hasError by remember { mutableStateOf(false) }
            var retryKey by remember { mutableIntStateOf(0) }
            
            LaunchedEffect(page, retryKey) {
                hasError = false
                bitmap = null
                val result = kotlinx.coroutines.withTimeoutOrNull(5000L) {
                    localDocument.engine.renderPage(page)
                }
                if (result == null) {
                    hasError = true
                } else {
                    bitmap = result
                }
            }
            
            if (bitmap != null) {
                androidx.compose.foundation.Image(
                    bitmap = bitmap!!.asImageBitmap(),
                    contentDescription = stringResource(R.string.page_desc, page + 1),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize().padding(16.dp).shadow(12.dp)
                )
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
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
