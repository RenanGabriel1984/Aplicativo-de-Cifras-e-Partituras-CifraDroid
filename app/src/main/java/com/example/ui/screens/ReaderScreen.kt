package com.example.ui.screens

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.ui.text.style.TextAlign
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.util.readerGestures
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

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
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
    val prefsManager = remember { com.example.util.PreferencesManager(context) }
    var themeMode by remember { mutableIntStateOf(prefsManager.getThemeMode()) }
    var orientationMode by remember { mutableIntStateOf(prefsManager.getOrientationMode()) }
    var isContinuousMode by remember { mutableStateOf(prefsManager.isContinuousMode()) }
    var isCountdownEnabled by remember { mutableStateOf(prefsManager.isCountdownEnabled()) }
    var isTouchLocked by remember { mutableStateOf(false) }
    
    val isDarkMode = themeMode == 1 || themeMode == 2
    val stageBackgroundColor = when (themeMode) {
        0 -> Color.White
        1 -> Color.Black
        2 -> Color(0xFF1A0000)
        else -> Color.White
    }

    DisposableEffect(orientationMode) {
        val activity = context.findActivity()
        activity?.requestedOrientation = when (orientationMode) {
            1 -> android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            2 -> android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            else -> android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
        onDispose {
            activity?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

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

    var currentTool by remember { mutableStateOf(com.example.ui.components.AnnotationTool.NONE) }
    var annotations by remember { mutableStateOf(emptyList<com.example.util.PdfAnnotation>()) }
    var bookmarks by remember { mutableStateOf(emptyList<com.example.util.PdfBookmark>()) }
    var undoStack by remember { mutableStateOf(emptyList<List<com.example.util.PdfAnnotation>>()) }
    var redoStack by remember { mutableStateOf(emptyList<List<com.example.util.PdfAnnotation>>()) }
    var textInputRequested by remember { mutableStateOf<androidx.compose.ui.geometry.Offset?>(null) }
    var currentTextInput by remember { mutableStateOf("") }
    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }

    LaunchedEffect(manuscript?.localUri) {
        val uri = manuscript?.localUri
        if (!uri.isNullOrBlank()) {
            uiState.isLoading = true
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                uiState.localDocument = com.example.util.DocumentReader.loadDocument(context, uri)
                annotations = com.example.util.PdfAnnotationManager.loadAnnotations(context, uri)
                bookmarks = com.example.util.PdfBookmarkManager.loadBookmarks(context, uri)
            }
            uiState.isLoading = false
        }
    }

    val coroutineScope = rememberCoroutineScope()
    val currentDocument = uiState.localDocument
    
    val persistAnnotations = { newList: List<com.example.util.PdfAnnotation> ->
        undoStack = undoStack + listOf(annotations)
        redoStack = emptyList()
        annotations = newList
        val uri = manuscript?.localUri
        if (!uri.isNullOrBlank()) {
            coroutineScope.launch {
                com.example.util.PdfAnnotationManager.saveAnnotations(context, uri, newList)
            }
        }
    }

    val handleAnnotationAdded = { ann: com.example.util.PdfAnnotation ->
        persistAnnotations(annotations + ann)
    }

    val handleErase = { x: Float, y: Float, requestedPage: Int ->
        val toErase = annotations.filter { ann ->
            ann.page == requestedPage && 
            when (ann.type) {
                "PEN" -> ann.points?.any { p -> kotlin.math.hypot(p.x - x, p.y - y) < 0.05f } == true
                "HIGHLIGHT" -> x >= ann.x && x <= ann.x + ann.width && y >= ann.y && y <= ann.y + ann.height
                "TEXT" -> x >= ann.x && x <= ann.x + 0.3f && y >= ann.y - 0.05f && y <= ann.y + 0.05f
                else -> false
            }
        }.map { it.id }.toSet()
        if (toErase.isNotEmpty()) {
            persistAnnotations(annotations.filterNot { toErase.contains(it.id) })
        }
    }

    val handleTextRequest = { x: Float, y: Float ->
        textInputRequested = androidx.compose.ui.geometry.Offset(x, y)
        currentTextInput = ""
    }

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
    
    var topBarHeight by remember { mutableStateOf(0.dp) }
    var bottomBarHeight by remember { mutableStateOf(0.dp) }
    val density = androidx.compose.ui.platform.LocalDensity.current

    var isExtremeFocusMode by remember { mutableStateOf(prefsManager.isExtremeFocusModeEnabled()) }
    var isNextSongAlertEnabled by remember { mutableStateOf(prefsManager.isNextSongAlertEnabled()) }
    var isAutoConfirmMusicalInstructions by remember { mutableStateOf(prefsManager.isAutoConfirmMusicalInstructionsEnabled()) }
    var isSilentMode by remember { mutableStateOf(prefsManager.isSilentModeEnabled()) }
    
    var pendingNextSongId by remember { mutableStateOf<Int?>(null) }
    var nextSongCountdown by remember { mutableIntStateOf(10) }

    val performanceSession by com.example.util.PerformanceSessionManager.session.collectAsStateWithLifecycle()
    
    LaunchedEffect(performanceSession?.isRunning) {
        var lastTick = System.currentTimeMillis()
        while (performanceSession?.isRunning == true) {
            kotlinx.coroutines.delay(1000)
            val now = System.currentTimeMillis()
            com.example.util.PerformanceSessionManager.updateTime((performanceSession?.elapsedTime ?: 0L) + (now - lastTick))
            lastTick = now
        }
    }
    
    LaunchedEffect(performanceSession?.isRunning) {
        if (performanceSession?.isRunning == true) {
            isExtremeFocusMode = true
            uiState.showHud = true
            viewModel.setStageMode(true)
            isNextSongAlertEnabled = true
            isAutoConfirmMusicalInstructions = true
        }
    }

    LaunchedEffect(uiState.localDocument) {
        val docName = (uiState.localDocument as? com.example.util.DocumentContent.PdfDoc)?.engine?.file?.name ?: "Música"
        if (performanceSession?.isRunning == true && performanceSession?.currentSong != docName) {
            com.example.util.PerformanceSessionManager.updateCurrentSong(docName)
            com.example.util.PerformanceSessionManager.incrementSongsPlayed()
        }
    }

    var showPerformancePanel by remember { mutableStateOf(false) }
    val allManuscripts by viewModel.allManuscripts.collectAsStateWithLifecycle(emptyList())
    var playedPerformanceSongs by remember(repertoireId) { 
        mutableStateOf(if (repertoireId != null) prefsManager.getPlayedPerformanceSongs(repertoireId) else emptySet()) 
    }
    var performanceStartTime by remember(repertoireId) {
        mutableLongStateOf(if (repertoireId != null) prefsManager.getPerformanceStartTime(repertoireId) else 0L)
    }
    var performanceElapsedTime by remember(repertoireId) {
        mutableLongStateOf(if (repertoireId != null) prefsManager.getPerformanceElapsedTime(repertoireId) else 0L)
    }
    var isPerformanceTimerRunning by remember { mutableStateOf(false) }

    var showMarkersPanel by remember { mutableStateOf(false) }
    val detectedMarkers by produceState<List<com.example.util.ScoreMarker>>(initialValue = emptyList(), uiState.localDocument) {
        val document = uiState.localDocument
        if (document is com.example.util.DocumentContent.PdfDoc) {
            value = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                val pagesText = com.example.util.PdfTextExtractor.extractTextByPage(context, document.engine.file)
                com.example.util.ScoreMarkerDetector.detectFromPages(pagesText)
            }
        } else {
            value = emptyList()
        }
    }

    val scoreRelationships by remember {
        derivedStateOf { com.example.util.ScoreRelationshipEngine.buildRelationships(detectedMarkers) }
    }

    val musicalStructure by produceState(
        initialValue = com.example.util.MusicalStructure(emptyList()),
        pageCount, detectedMarkers, scoreRelationships
    ) {
        value = com.example.util.MusicalStructureEngine.analyzeStructure(
            pageCount = pageCount,
            markers = detectedMarkers,
            relationships = scoreRelationships,
            timeline = null
        )
    }

    var showStructureBottomSheet by remember { mutableStateOf(false) }

    var musicalTimeline by remember(manuscriptId) { mutableStateOf(com.example.util.MusicalTimeline()) }
    
    val musicalSemantics by remember(musicalStructure, detectedMarkers, musicalTimeline) {
        derivedStateOf {
            com.example.util.MusicalSemanticsEngine.inferSemantics(
                structure = musicalStructure,
                markers = detectedMarkers,
                timeline = musicalTimeline
            )
        }
    }

    var ultimaPaginaNotificada by remember { mutableStateOf<Int?>(null) }
    var currentMusicalAction by remember { mutableStateOf<com.example.util.MusicalExecutionAction?>(null) }
    var autoConfirmCountdown by remember { mutableIntStateOf(0) }

    LaunchedEffect(currentMusicalAction) {
        if (currentMusicalAction != null && isAutoConfirmMusicalInstructions) {
            autoConfirmCountdown = 3
            while (autoConfirmCountdown > 0) {
                kotlinx.coroutines.delay(1000)
                autoConfirmCountdown--
            }
            val action = currentMusicalAction
            if (action != null) {
                val targetPage = action.targetPage
                if (targetPage >= 0 && targetPage < pageCount) {
                    musicalTimeline = com.example.util.MusicalTimelineEngine.executeRelationship(musicalTimeline, action.relationship)
                    if (performanceSession?.isRunning == true) { com.example.util.PerformanceSessionManager.incrementRelationships() }
                    pagerState.animateScrollToPage(targetPage)
                } else {
                    musicalTimeline = com.example.util.MusicalTimelineEngine.executeRelationship(musicalTimeline, action.relationship)
                    if (performanceSession?.isRunning == true) { com.example.util.PerformanceSessionManager.incrementRelationships() }
                }
                currentMusicalAction = null
            }
        }
    }

    LaunchedEffect(pagerState.currentPage, detectedMarkers) {
        val markersInPage = detectedMarkers.filter { it.page == pagerState.currentPage }
        musicalTimeline = com.example.util.MusicalTimelineEngine.updatePage(
            timeline = musicalTimeline,
            page = pagerState.currentPage,
            markersInPage = markersInPage
        )
        if (performanceSession?.isRunning == true) {
            com.example.util.PerformanceSessionManager.updateCurrentPass("Passagem ${musicalTimeline.currentPass}")
        }
    }

    LaunchedEffect(pagerState.currentPage, scoreRelationships, musicalTimeline) {
        if (ultimaPaginaNotificada != pagerState.currentPage) {
            val relationship = scoreRelationships.firstOrNull { it.sourcePage == pagerState.currentPage }
            if (relationship != null) {
                if (relationship.relationshipType == com.example.util.RelationshipType.UNRESOLVED) {
                    val alreadyExecuted = musicalTimeline.executedRelationships.any { 
                        it.relationshipId == relationship.sourceMarkerId && it.pass == musicalTimeline.currentPass 
                    }
                    if (!alreadyExecuted) {
                        android.widget.Toast.makeText(context, "Destino musical não encontrado.", android.widget.Toast.LENGTH_SHORT).show()
                        musicalTimeline = com.example.util.MusicalTimelineEngine.executeRelationship(musicalTimeline, relationship)
                    }
                } else {
                    val action = com.example.util.MusicalExecutionEngine.evaluateCurrentPage(
                        currentPage = pagerState.currentPage,
                        relationships = scoreRelationships,
                        timeline = musicalTimeline
                    )
                    if (action != null) {
                        currentMusicalAction = action
                    } else {
                        currentMusicalAction = null
                    }
                }
            } else {
                currentMusicalAction = null
            }
            ultimaPaginaNotificada = pagerState.currentPage
        }
    }

    // Retomar logic
    var showResumeDialog by remember { mutableStateOf(false) }
    var showFinishDialog by remember { mutableStateOf(false) }
    LaunchedEffect(repertoireId) {
        if (repertoireId != null) {
            val played = prefsManager.getPlayedPerformanceSongs(repertoireId)
            val allIds = com.example.util.RepertoireUtil.getFlatManuscriptIds(repertoire!!)
            if (played.isNotEmpty() && played.size < allIds.size) {
                showResumeDialog = true
            }
        }
    }

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
            val ids = com.example.util.RepertoireUtil.getFlatManuscriptIds(repertoire!!)
            val currentIndex = ids.indexOf(manuscriptId)

            if (repertoireId != null) {
                prefsManager.addPlayedPerformanceSong(repertoireId, manuscriptId)
                playedPerformanceSongs = prefsManager.getPlayedPerformanceSongs(repertoireId)
                if (playedPerformanceSongs.size >= ids.size && !showFinishDialog) {
                    showFinishDialog = true
                }
            }

            if (currentIndex in 0 until ids.size - 1) {
                val nextId = ids[currentIndex + 1]
                if (isNextSongAlertEnabled) {
                    pendingNextSongId = nextId
                    nextSongCountdown = 10
                } else {
                    onNavigateToManuscript(nextId)
                }
            }
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
            val ids = com.example.util.RepertoireUtil.getFlatManuscriptIds(repertoire!!)
            val currentIndex = ids.indexOf(manuscriptId)
            if (currentIndex > 0) {
                onNavigateToManuscript(ids[currentIndex - 1])
            }
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
    
    var showGoToDialog by remember { mutableStateOf(false) }
    var showBookmarkAddDialog by remember { mutableStateOf(false) }
    var showBookmarksSheet by remember { mutableStateOf(false) }
    var showThumbnails by remember { mutableStateOf(false) }
    
    
    androidx.activity.compose.BackHandler(
        enabled = uiState.showHud || uiState.showMusicList || uiState.selectedSongChartId != null || uiState.showImportDiagnostic
    ) {
        if (uiState.showImportDiagnostic) {
            uiState.showImportDiagnostic = false
        } else if (uiState.selectedSongChartId != null) {
            uiState.selectedSongChartId = null
        } else if (uiState.showMusicList) {
            uiState.showMusicList = false
        } else if (uiState.showHud) {
            uiState.showHud = false
        }
    }
    
    // Auto-hide HUD natively
    LaunchedEffect(uiState.showHud, hudInteractionTime, pagerState.currentPage) {
        if (uiState.showHud) {
            kotlinx.coroutines.delay(20000)
            uiState.showHud = false
        }
    }
    
        val readingContext by remember(musicalStructure, musicalTimeline, pagerState.currentPage, detectedMarkers) {
            derivedStateOf {
                com.example.util.ReadingContextEngine.inferContext(
                    structure = musicalStructure,
                    timeline = musicalTimeline,
                    currentPage = pagerState.currentPage,
                    markers = detectedMarkers
                )
            }
        }

        val dashboardState by remember(musicalStructure, musicalTimeline, readingContext, performanceSession, pagerState.currentPage, detectedMarkers, repertoire) {
            derivedStateOf {
                val repertoireTotal = repertoire?.let { com.example.util.RepertoireUtil.getFlatManuscriptIds(it).size } ?: 0
                com.example.util.StageDashboardEngine.produceState(
                    structure = musicalStructure,
                    timeline = musicalTimeline,
                    readingContext = readingContext,
                    session = performanceSession,
                    currentPage = pagerState.currentPage,
                    markers = detectedMarkers,
                    repertoireTotal = repertoireTotal
                )
            }
        }

        val readingProfile by remember { mutableStateOf(prefsManager.getReadingProfile()) }
        val dashboardPresentation by remember(dashboardState, musicalStructure, performanceSession, readingProfile) {
            derivedStateOf {
                com.example.util.ReadingProfileEngine.producePresentation(
                    profile = readingProfile,
                    state = dashboardState
                )
            }
        }

        val flowContext by remember(performanceSession, dashboardState, musicalTimeline, pagerState.currentPage, pagerState.isScrollInProgress, isExtremeFocusMode, isAutoConfirmMusicalInstructions) {
            derivedStateOf {
                com.example.util.FlowModeEngine.produceContext(
                    session = performanceSession,
                    dashboardState = dashboardState,
                    timeline = musicalTimeline,
                    currentPage = pagerState.currentPage,
                    isScrollInProgress = pagerState.isScrollInProgress,
                    focusMode = isExtremeFocusMode,
                    autoCue = isAutoConfirmMusicalInstructions
                )
            }
        }

        val semanticReadingState by remember(pagerState.currentPage, musicalSemantics) {
            derivedStateOf {
                com.example.util.SemanticReadingEngine.produceState(
                    currentPage = pagerState.currentPage,
                    semantics = musicalSemantics
                )
            }
        }

        val adaptiveGuidanceState by remember(pagerState.currentPage, musicalStructure, musicalSemantics, musicalTimeline, scoreRelationships, detectedMarkers) {
            derivedStateOf {
                com.example.util.AdaptiveGuidanceEngine.produceGuidance(
                    currentPage = pagerState.currentPage,
                    structure = musicalStructure,
                    semantics = musicalSemantics,
                    timeline = musicalTimeline,
                    relationships = scoreRelationships,
                    markers = detectedMarkers
                )
            }
        }

    val pagerModifier = Modifier
        .fillMaxSize()
        .readerGestures(
            isTouchLocked, isStageMode, uiState, pagerState.currentPage, musicalStructure,
            onTap = { offset ->
                if (isTouchLocked || !flowContext.allowGestures) return@readerGestures
                
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
            },
            onGesture = { gesture ->
                if (isTouchLocked || !flowContext.allowGestures) return@readerGestures
                when (gesture) {
                    com.example.util.ReaderGesture.DOUBLE_TAP -> {
                        val nextSection = musicalStructure.sections.firstOrNull { it.startPage > pagerState.currentPage }
                        if (nextSection != null) {
                            coroutineScope.launch { pagerState.animateScrollToPage(nextSection.startPage) }
                        }
                    }
                    com.example.util.ReaderGesture.LONG_PRESS -> {
                        showBookmarkAddDialog = true
                    }
                    com.example.util.ReaderGesture.SWIPE_UP -> {
                        uiState.showHud = true
                    }
                    com.example.util.ReaderGesture.SWIPE_DOWN -> {
                        uiState.showHud = false
                    }
                    com.example.util.ReaderGesture.SWIPE_LEFT -> {
                        showMarkersPanel = true
                    }
                    com.example.util.ReaderGesture.SWIPE_RIGHT -> {
                        uiState.showMusicList = true
                    }
                    com.example.util.ReaderGesture.TWO_FINGER_TAP -> {
                        isExtremeFocusMode = !isExtremeFocusMode
                    }
                    com.example.util.ReaderGesture.THREE_FINGER_TAP -> {
                        showPerformancePanel = true
                    }
                }
            }
        )
    
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
            Box(modifier = Modifier.fillMaxSize().background(stageBackgroundColor).windowInsetsPadding(WindowInsets.safeDrawing), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(stageBackgroundColor) // Stage mode background
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
            Column(modifier = Modifier.fillMaxSize().background(if (uiState.showMusicList) stageBackgroundColor else Color.Transparent)) {
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
                            repertoireId = repertoireId,
                            viewModel = viewModel,
                            onBack = { uiState.selectedSongChartId = null },
                            onNavigateToSongChart = { uiState.selectedSongChartId = it },
                            isStageMode = isStageMode,
                            showHud = uiState.showHud,
                            isTouchLocked = isTouchLocked,
                            onToggleHud = toggleHud,
                            isDarkMode = isDarkMode
                        )
                    } else if (uiState.showMusicList) {
                        SongListScreen(
                            manuscriptId = manuscriptId,
                            repertoireId = repertoireId,
                            topBarHeight = topBarHeight,
                            bottomBarHeight = bottomBarHeight,
                            viewModel = viewModel,
                            onSongChartSelected = { selectedId ->
                                uiState.selectedSongChartId = selectedId
                                uiState.showHud = false
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
                                PageContent(
                                    page = page, 
                                    localDocument = uiState.localDocument, 
                                    defaultPages = defaultPages, 
                                    isChoirMode = isChoirMode,
                                    annotations = annotations,
                                    currentTool = currentTool,
                                    onAnnotationAdded = handleAnnotationAdded,
                                    onEraseRequested = { x, y -> handleErase(x, y, page) },
                                    onTextRequested = handleTextRequest
                                )
                            }
                        } else {
                            HorizontalPager(
                                state = pagerState,
                                modifier = pagerModifier,
                                beyondViewportPageCount = 1
                            ) { page ->
                                PageContent(
                                    page = page, 
                                    localDocument = uiState.localDocument, 
                                    defaultPages = defaultPages, 
                                    isChoirMode = isChoirMode,
                                    annotations = annotations,
                                    currentTool = currentTool,
                                    onAnnotationAdded = handleAnnotationAdded,
                                    onEraseRequested = { x, y -> handleErase(x, y, page) },
                                    onTextRequested = handleTextRequest
                                )
                            }
                        }
                        
                        if (liturgicalTheme != LiturgicalTheme.CLASSIC) {
                            Box(modifier = Modifier.fillMaxSize().background(liturgicalTheme.color))
                        }
                    }
                } // End inner Box
            } // End Column

            if (isTouchLocked) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    androidx.compose.material3.FilledTonalIconButton(
                        onClick = { isTouchLocked = false },
                        colors = androidx.compose.material3.IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Desbloquear Tela"
                        )
                    }
                }
            }

            // HUD
            AnimatedVisibility(
                visible = uiState.showHud && !isExtremeFocusMode,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Top Bar
                    Surface(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        modifier = Modifier.align(Alignment.TopCenter).onGloballyPositioned { coordinates ->
                            topBarHeight = with(density) { coordinates.size.height.toDp() }
                        }
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
                                        val ids = com.example.util.RepertoireUtil.getFlatManuscriptIds(repertoire!!)
                                        val idx = ids.indexOf(manuscriptId) + 1
                                        val total = ids.size
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
                            var editNoteDialog by remember { mutableStateOf(false) }
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (repertoire != null) {
                                    IconButton(onClick = { editNoteDialog = true }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Editar Nota de Palco")
                                    }
                                    IconButton(onClick = { showPerformancePanel = !showPerformancePanel }) {
                                        Icon(Icons.Default.Menu, contentDescription = "Painel de Execução", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                                Box {
                                    IconButton(onClick = { showMenu = true }) {
                                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings_desc), tint = MaterialTheme.colorScheme.primary)
                                    }
                                    DropdownMenu(
                                        expanded = showMenu,
                                        onDismissRequest = { showMenu = false }
                                    ) {
                                    DropdownMenuItem(
                                        text = { Text(if (performanceSession?.isRunning == true) "Encerrar Sessão" else "Iniciar Sessão de Performance") },
                                        onClick = {
                                            if (performanceSession?.isRunning == true) {
                                                com.example.util.PerformanceSessionManager.stopSession()
                                            } else {
                                                val docName = (currentDocument as? com.example.util.DocumentContent.PdfDoc)?.engine?.file?.name ?: "Música"
                                                com.example.util.PerformanceSessionManager.startSession(docName)
                                            }
                                            showMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(if (uiState.showMusicList) "Alternar para PDF" else "Alternar para Músicas") },
                                        onClick = {
                                            uiState.showMusicList = !uiState.showMusicList
                                            uiState.selectedSongChartId = null
                                            showMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(if (isVerticalScroll) "Mudar para rolagem horizontal" else "Mudar para rolagem vertical") },
                                        onClick = {
                                            viewModel.setVerticalScroll(!isVerticalScroll)
                                            showMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { 
                                            Text(when(themeMode) {
                                                0 -> "Tema: Claro"
                                                1 -> "Tema: Escuro"
                                                else -> "Tema: Vermelho"
                                            })
                                        },
                                        onClick = {
                                            themeMode = (themeMode + 1) % 3
                                            prefsManager.setThemeMode(themeMode)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { 
                                            Text(when(orientationMode) {
                                                0 -> "Girar Tela: Livre"
                                                1 -> "Girar Tela: Retrato"
                                                else -> "Girar Tela: Paisagem"
                                            })
                                        },
                                        onClick = {
                                            orientationMode = (orientationMode + 1) % 3
                                            prefsManager.setOrientationMode(orientationMode)
                                            // Doesn't close menu immediately to allow multiple clicks if desired
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Repertório Contínuo: ${if(isContinuousMode) "ON" else "OFF"}") },
                                        onClick = {
                                            isContinuousMode = !isContinuousMode
                                            prefsManager.setContinuousMode(isContinuousMode)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Timer (10s) na Próxima Música: ${if(isCountdownEnabled) "ON" else "OFF"}") },
                                        onClick = {
                                            isCountdownEnabled = !isCountdownEnabled
                                            prefsManager.setCountdownEnabled(isCountdownEnabled)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(if (isStageMode) "Sair do Modo Palco" else "Modo Palco (Ocultar Menus)") },
                                        onClick = {
                                            viewModel.setStageMode(!isStageMode)
                                            uiState.showHud = !isStageMode // Se entrou no modo palco, oculta. (Inverted since state hasn't updated yet)
                                            if (!isStageMode) { uiState.showHud = false }
                                            showMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Travar Tela") },
                                        onClick = {
                                            isTouchLocked = true
                                            uiState.showHud = false
                                            showMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Miniaturas") },
                                        onClick = {
                                            showThumbnails = true
                                            showMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Ir para Página") },
                                        onClick = {
                                            showGoToDialog = true
                                            showMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Adicionar Marcador") },
                                        onClick = {
                                            showBookmarkAddDialog = true
                                            showMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Marcadores") },
                                        onClick = {
                                            showBookmarksSheet = true
                                            showMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Debug: Marcadores Musicais") },
                                        onClick = {
                                            showMarkersPanel = true
                                            showMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Estrutura Musical") },
                                        onClick = {
                                            showStructureBottomSheet = true
                                            showMenu = false
                                        }
                                    )
                                }
                                
                                if (showBookmarkAddDialog && currentDocument is com.example.util.DocumentContent.PdfDoc) {
                                    var bookmarkName by remember { mutableStateOf("") }
                                    var bookmarkColor by remember { mutableStateOf("#2196F3") }
                                    val colorOptions = listOf(
                                        "#2196F3" to "Azul",
                                        "#4CAF50" to "Verde",
                                        "#FFEB3B" to "Amarelo",
                                        "#F44336" to "Vermelho",
                                        "#9C27B0" to "Roxo"
                                    )
                                    AlertDialog(
                                        onDismissRequest = { showBookmarkAddDialog = false },
                                        title = { Text("Adicionar Marcador") },
                                        text = {
                                            Column {
                                                OutlinedTextField(
                                                    value = bookmarkName,
                                                    onValueChange = { bookmarkName = it },
                                                    label = { Text("Nome do Marcador") },
                                                    singleLine = true,
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                Spacer(modifier = Modifier.height(16.dp))
                                                Text("Cor do Marcador", style = MaterialTheme.typography.labelMedium)
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                                    colorOptions.forEach { (colorHex, _) ->
                                                        Box(
                                                            modifier = Modifier
                                                                .size(40.dp)
                                                                .background(Color(android.graphics.Color.parseColor(colorHex)), shape = androidx.compose.foundation.shape.CircleShape)
                                                                .then(if (bookmarkColor == colorHex) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, shape = androidx.compose.foundation.shape.CircleShape) else Modifier.border(1.dp, Color.Gray, shape = androidx.compose.foundation.shape.CircleShape))
                                                                .clickable { bookmarkColor = colorHex }
                                                        )
                                                    }
                                                }
                                            }
                                        },
                                        confirmButton = {
                                            TextButton(onClick = {
                                                val uri = manuscript?.localUri
                                                if (bookmarkName.isNotBlank() && uri != null) {
                                                    val hash = com.example.util.PdfBookmarkManager.getHash(uri)
                                                    val newBookmark = com.example.util.PdfBookmark(
                                                        pdfHash = hash,
                                                        name = bookmarkName,
                                                        page = pagerState.currentPage,
                                                        verticalOffset = 0,
                                                        color = bookmarkColor
                                                    )
                                                    bookmarks = (bookmarks + newBookmark).sortedWith(compareBy({ it.page }, { it.verticalOffset }))
                                                    coroutineScope.launch {
                                                        com.example.util.PdfBookmarkManager.saveBookmarks(context, uri, bookmarks)
                                                    }
                                                }
                                                showBookmarkAddDialog = false
                                            }) { Text("Salvar") }
                                        },
                                        dismissButton = {
                                            TextButton(onClick = { showBookmarkAddDialog = false }) { Text("Cancelar") }
                                        }
                                    )
                                }

                                if (showGoToDialog) {
                                    var pageInput by remember { mutableStateOf("") }
                                    AlertDialog(
                                        onDismissRequest = { showGoToDialog = false },
                                        title = { Text("Ir para Página") },
                                        text = {
                                            OutlinedTextField(
                                                value = pageInput,
                                                onValueChange = { pageInput = it },
                                                label = { Text("Página (1 a $pageCount)") },
                                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                                            )
                                        },
                                        confirmButton = {
                                            TextButton(onClick = {
                                                val p = pageInput.toIntOrNull()
                                                if (p != null && p in 1..pageCount) {
                                                    coroutineScope.launch {
                                                        pagerState.scrollToPage(p - 1)
                                                    }
                                                    showGoToDialog = false
                                                }
                                            }) {
                                                Text("Ir")
                                            }
                                        },
                                        dismissButton = {
                                            TextButton(onClick = { showGoToDialog = false }) { Text("Cancelar") }
                                        }
                                    )
                                }
                            }
                        }
                    }
                    
                    if (showThumbnails) {
                        ModalBottomSheet(
                            onDismissRequest = { showThumbnails = false },
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 8.dp
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
                                Text(
                                    "Miniaturas das Páginas", 
                                    style = MaterialTheme.typography.titleMedium, 
                                    modifier = Modifier.padding(16.dp)
                                )
                                androidx.compose.foundation.lazy.LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(pageCount) { index ->
                                        Surface(
                                            modifier = Modifier
                                                .width(100.dp)
                                                .aspectRatio(0.7f)
                                                .clickable {
                                                    coroutineScope.launch { pagerState.scrollToPage(index) }
                                                    showThumbnails = false
                                                },
                                            shape = RoundedCornerShape(8.dp),
                                            border = androidx.compose.foundation.BorderStroke(
                                                width = if (pagerState.currentPage == index) 2.dp else 1.dp,
                                                color = if (pagerState.currentPage == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                            )
                                        ) {
                                            PageContent(index, uiState.localDocument, defaultPages)
                                            Box(modifier = Modifier.fillMaxSize().padding(4.dp), contentAlignment = Alignment.BottomCenter) {
                                                Surface(color = Color.Black.copy(alpha = 0.7f), shape = RoundedCornerShape(4.dp)) {
                                                    Text("${index + 1}", color = Color.White, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                                }
                                            }
                                        }
                                    }
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
                            .onGloballyPositioned { coordinates ->
                                bottomBarHeight = with(density) { coordinates.size.height.toDp() } + 32.dp
                            }
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
                                
                                Surface(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    onClick = { showGoToDialog = true }
                                ) {
                                    Text(
                                        text = "Página ${pagerState.currentPage + 1} de $pageCount (${((pagerState.currentPage + 1).toFloat() / pageCount * 100).toInt()}%)",
                                        style = MaterialTheme.typography.labelMedium,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                                
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

            val currentBookmarkIndex by remember(bookmarks, pagerState.currentPage) {
                derivedStateOf {
                    val currPage = pagerState.currentPage
                    bookmarks.indexOfLast { it.page <= currPage }
                }
            }
            val currentBookmark = if (currentBookmarkIndex >= 0) bookmarks[currentBookmarkIndex] else null
            val prevBookmark = if (currentBookmarkIndex > 0) bookmarks[currentBookmarkIndex - 1] else null
            val nextBookmark = if (currentBookmarkIndex >= 0 && currentBookmarkIndex < bookmarks.size - 1) bookmarks[currentBookmarkIndex + 1] else if (currentBookmarkIndex < 0 && bookmarks.isNotEmpty()) bookmarks[0] else null

            if (bookmarks.isNotEmpty() && !uiState.showMusicList) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Indicador Visual
                    if (currentBookmark != null) {
                        Surface(
                            modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f)
                        ) {
                            Text(
                                text = "[${currentBookmark.name}]",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(android.graphics.Color.parseColor(currentBookmark.color)),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Mini Painel Rápido (only when controls are visible)
                    if (uiState.showHud) {
                        Surface(
                            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 90.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.9f),
                            shadowElevation = 4.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        prevBookmark?.let { b -> coroutineScope.launch { pagerState.animateScrollToPage(b.page) } }
                                    },
                                    enabled = prevBookmark != null
                                ) {
                                    Icon(Icons.Default.SkipPrevious, contentDescription = "Marcador Anterior")
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 16.dp)) {
                                    Text(
                                        text = currentBookmark?.name ?: "---",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (currentBookmark != null) Color(android.graphics.Color.parseColor(currentBookmark.color)) else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        nextBookmark?.let { b -> coroutineScope.launch { pagerState.animateScrollToPage(b.page) } }
                                    },
                                    enabled = nextBookmark != null
                                ) {
                                    Icon(Icons.Default.SkipNext, contentDescription = "Próximo Marcador")
                                }
                            }
                        }
                    }
                }
            }

            if (isStageMode && !uiState.showHud && !isTouchLocked && !uiState.showMusicList) {
                Box(modifier = Modifier.fillMaxSize()) {
                    var performanceNote by remember(manuscriptId) { mutableStateOf(prefsManager.getPerformanceNote(manuscriptId)) }
                    if (performanceNote.isNotBlank()) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 16.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.9f)
                        ) {
                            Text(
                                text = "Nota: $performanceNote",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }

                    // Floating fast navigation
                    val ids = repertoire?.let { com.example.util.RepertoireUtil.getFlatManuscriptIds(it) }
                    val currentIdx = ids?.indexOf(manuscriptId) ?: -1
                    val hasPrev = currentIdx > 0
                    val hasNext = ids != null && currentIdx < ids.size - 1

                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = 32.dp, end = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.95f),
                        shadowElevation = 8.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { 
                                    if (hasPrev) onNavigateToManuscript(ids!![currentIdx - 1])
                                },
                                enabled = hasPrev
                            ) {
                                Icon(Icons.Default.SkipPrevious, contentDescription = "Música Anterior", tint = if (hasPrev) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                            }
                            
                            val isPlayed = playedPerformanceSongs.contains(manuscriptId)
                            IconButton(onClick = { 
                                if (repertoireId != null) {
                                    val newSet = if (isPlayed) playedPerformanceSongs - manuscriptId else playedPerformanceSongs + manuscriptId
                                    prefsManager.setPlayedPerformanceSongs(repertoireId, newSet)
                                    playedPerformanceSongs = newSet
                                }
                            }) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.CheckCircle, 
                                    contentDescription = "Marcar Executada",
                                    tint = if (isPlayed) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            IconButton(
                                onClick = { 
                                    if (hasNext) onNavigateToManuscript(ids!![currentIdx + 1])
                                },
                                enabled = hasNext
                            ) {
                                Icon(Icons.Default.SkipNext, contentDescription = "Próxima Música", tint = if (hasNext) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                            }
                        }
                    }
                }
            }

        if (uiState.localDocument is com.example.util.DocumentContent.PdfDoc && !isStageMode && !uiState.showMusicList) {
            Box(modifier = Modifier.fillMaxSize()) {
                Surface(
                    modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.9f),
                    shadowElevation = 4.dp
                ) {
                    Column(modifier = Modifier.padding(4.dp)) {
                        val activeColor = MaterialTheme.colorScheme.primary
                        val idleColor = MaterialTheme.colorScheme.onSurface
                        IconButton(onClick = { currentTool = if (currentTool == com.example.ui.components.AnnotationTool.PEN) com.example.ui.components.AnnotationTool.NONE else com.example.ui.components.AnnotationTool.PEN }) {
                            Icon(Icons.Default.Edit, contentDescription = "Caneta", tint = if (currentTool == com.example.ui.components.AnnotationTool.PEN) activeColor else idleColor)
                        }
                        IconButton(onClick = { currentTool = if (currentTool == com.example.ui.components.AnnotationTool.HIGHLIGHT) com.example.ui.components.AnnotationTool.NONE else com.example.ui.components.AnnotationTool.HIGHLIGHT }) {
                            Icon(Icons.Default.Create, contentDescription = "Marca Texto", tint = if (currentTool == com.example.ui.components.AnnotationTool.HIGHLIGHT) activeColor else idleColor)
                        }
                        IconButton(onClick = { currentTool = if (currentTool == com.example.ui.components.AnnotationTool.TEXT) com.example.ui.components.AnnotationTool.NONE else com.example.ui.components.AnnotationTool.TEXT }) {
                            Icon(Icons.Default.Title, contentDescription = "Texto", tint = if (currentTool == com.example.ui.components.AnnotationTool.TEXT) activeColor else idleColor)
                        }
                        IconButton(onClick = { currentTool = if (currentTool == com.example.ui.components.AnnotationTool.ERASER) com.example.ui.components.AnnotationTool.NONE else com.example.ui.components.AnnotationTool.ERASER }) {
                            Icon(Icons.Default.Clear, contentDescription = "Borracha", tint = if (currentTool == com.example.ui.components.AnnotationTool.ERASER) activeColor else idleColor)
                        }
                        if (undoStack.isNotEmpty()) {
                            IconButton(onClick = {
                                val prev = undoStack.last()
                                redoStack = redoStack + listOf(annotations)
                                undoStack = undoStack.dropLast(1)
                                annotations = prev
                                val uri = manuscript?.localUri
                                if (!uri.isNullOrBlank()) {
                                    coroutineScope.launch { com.example.util.PdfAnnotationManager.saveAnnotations(context, uri, prev) }
                                }
                            }) {
                                Icon(androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Desfazer")
                            }
                        }
                        if (redoStack.isNotEmpty()) {
                            IconButton(onClick = {
                                val next = redoStack.last()
                                undoStack = undoStack + listOf(annotations)
                                redoStack = redoStack.dropLast(1)
                                annotations = next
                                val uri = manuscript?.localUri
                                if (!uri.isNullOrBlank()) {
                                    coroutineScope.launch { com.example.util.PdfAnnotationManager.saveAnnotations(context, uri, next) }
                                }
                            }) {
                                Icon(Icons.Default.ArrowForward, contentDescription = "Refazer")
                            }
                        }
                    }
                }
            }
        }

        if (textInputRequested != null) {
            AlertDialog(
                onDismissRequest = { textInputRequested = null; currentTextInput = "" },
                title = { Text("Adicionar Texto") },
                text = {
                    androidx.compose.material3.OutlinedTextField(
                        value = currentTextInput,
                        onValueChange = { currentTextInput = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (currentTextInput.isNotBlank()) {
                            val x = textInputRequested!!.x
                            val y = textInputRequested!!.y
                            val ann = com.example.util.PdfAnnotation(
                                page = pagerState.currentPage,
                                type = "TEXT",
                                x = x,
                                y = y,
                                text = currentTextInput,
                                color = "#FF0000"
                            )
                            handleAnnotationAdded(ann)
                        }
                        textInputRequested = null
                        currentTextInput = ""
                    }) {
                        Text("Salvar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { textInputRequested = null; currentTextInput = "" }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        if (showBookmarksSheet) {
            @OptIn(ExperimentalMaterial3Api::class)
            ModalBottomSheet(
                onDismissRequest = { showBookmarksSheet = false }
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("Marcadores", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))
                    if (bookmarks.isEmpty()) {
                        Text("Nenhum marcador criado.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        androidx.compose.foundation.lazy.LazyColumn {
                            items(bookmarks.size) { index ->
                                val bookmark = bookmarks[index]
                                androidx.compose.material3.ListItem(
                                    headlineContent = { Text(bookmark.name) },
                                    supportingContent = { Text("Página ${bookmark.page + 1}") },
                                    leadingContent = {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .background(Color(android.graphics.Color.parseColor(bookmark.color)), shape = androidx.compose.foundation.shape.CircleShape)
                                        )
                                    },
                                    modifier = Modifier.clickable {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(bookmark.page)
                                            // Se no futuro houver suporte melhorado, lidar com verticalOffset aqui
                                        }
                                        showBookmarksSheet = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        } // This closes if (showBookmarksSheet)

        if (showStructureBottomSheet) {
            @OptIn(ExperimentalMaterial3Api::class)
            ModalBottomSheet(
                onDismissRequest = { showStructureBottomSheet = false }
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("Estrutura Musical", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    if (musicalStructure.sections.isEmpty()) {
                        Text("Nenhuma estrutura identificada.")
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxWidth()) {
                            items(musicalStructure.sections) { section ->
                                ListItem(
                                    headlineContent = { Text(section.name.uppercase(), fontWeight = FontWeight.Bold) },
                                    supportingContent = { Text("Página ${section.startPage + 1}") }
                                )
                                HorizontalDivider()
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }

        if (showMarkersPanel) {
            @OptIn(ExperimentalMaterial3Api::class)
            ModalBottomSheet(
                onDismissRequest = { showMarkersPanel = false }
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("Marcadores Musicais Detectados", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))
                    if (detectedMarkers.isEmpty()) {
                        Text("Nenhum marcador detectado neste documento.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        androidx.compose.foundation.lazy.LazyColumn {
                            items(detectedMarkers.size) { index ->
                                val marker = detectedMarkers[index]
                                androidx.compose.material3.ListItem(
                                    headlineContent = { Text(marker.type.name, style = MaterialTheme.typography.titleMedium) },
                                    supportingContent = { Text("Página ${marker.page + 1}\nTexto encontrado: ${marker.text}") }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }

        if (showFinishDialog) {
            AlertDialog(
                onDismissRequest = { showFinishDialog = false },
                title = { Text("Repertório concluído") },
                text = {
                    val totalSec = performanceElapsedTime / 1000
                    Text("Tempo total:\n${totalSec / 60} minutos\n\nMúsicas:\n${playedPerformanceSongs.size}\n\nExecutadas:\n${playedPerformanceSongs.size}")
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (repertoireId != null) {
                            prefsManager.clearPlayedPerformanceSongs(repertoireId)
                            prefsManager.clearPerformanceTime(repertoireId)
                        }
                        showFinishDialog = false
                    }) { Text("Concluir") }
                }
            )
        }

        if (showResumeDialog) {
            AlertDialog(
                onDismissRequest = { showResumeDialog = false },
                title = { Text("Retomar Repertório") },
                text = { Text("Deseja continuar de onde parou?") },
                confirmButton = {
                    TextButton(onClick = {
                        showResumeDialog = false
                    }) { Text("Continuar") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        if (repertoireId != null) {
                            prefsManager.clearPlayedPerformanceSongs(repertoireId)
                            prefsManager.clearPerformanceTime(repertoireId)
                            playedPerformanceSongs = emptySet()
                            performanceElapsedTime = 0L
                            performanceStartTime = 0L
                        }
                        showResumeDialog = false
                    }) { Text("Recomeçar") }
                }
            )
        }

        if (!isSilentMode) {
            val upcomingMarker = remember(pagerState.currentPage, detectedMarkers, musicalTimeline) {
                val nextPages = (pagerState.currentPage + 1)..(pagerState.currentPage + 3)
                detectedMarkers.firstOrNull { marker ->
                    marker.page in nextPages && 
                    marker.type in listOf(
                        com.example.util.ScoreMarkerType.DAL_SEGNO, 
                        com.example.util.ScoreMarkerType.DA_CAPO, 
                        com.example.util.ScoreMarkerType.TO_CODA, 
                        com.example.util.ScoreMarkerType.FINE, 
                        com.example.util.ScoreMarkerType.AL_FINE, 
                        com.example.util.ScoreMarkerType.SEGNO, 
                        com.example.util.ScoreMarkerType.CODA
                    ) &&
                    !musicalTimeline.visitedMarkers.contains(marker.id)
                }
            }

            if (currentMusicalAction != null) {
                val action = currentMusicalAction!!
                val title = when (action.relationship.relationshipType) {
                    com.example.util.RelationshipType.DA_CAPO_TO_START -> "D.C."
                    com.example.util.RelationshipType.DAL_SEGNO_TO_SEGNO -> "D.S."
                    com.example.util.RelationshipType.TO_CODA_TO_CODA -> "To Coda"
                    com.example.util.RelationshipType.AL_FINE_TO_FINE -> "Fine"
                    else -> "Instrução Musical"
                }

                if (isAutoConfirmMusicalInstructions) {
                    PerformanceCueOverlay(
                        title = title,
                        description = "Executando em $autoConfirmCountdown",
                        progress = if (autoConfirmCountdown > 0) autoConfirmCountdown / 3f else 0f
                    )
                } else {
                    PerformanceCueOverlay(
                        title = title,
                        description = "Executando...",
                        onExecute = {
                            val targetPage = action.targetPage
                            if (targetPage < 0 || targetPage >= pageCount) {
                                android.widget.Toast.makeText(context, "Destino fora do documento.", android.widget.Toast.LENGTH_SHORT).show()
                                musicalTimeline = com.example.util.MusicalTimelineEngine.executeRelationship(musicalTimeline, action.relationship)
                                if (performanceSession?.isRunning == true) { com.example.util.PerformanceSessionManager.incrementRelationships() }
                            } else {
                                musicalTimeline = com.example.util.MusicalTimelineEngine.executeRelationship(musicalTimeline, action.relationship)
                                if (performanceSession?.isRunning == true) { com.example.util.PerformanceSessionManager.incrementRelationships() }
                                val msg = when (action.relationship.relationshipType) {
                                    com.example.util.RelationshipType.DA_CAPO_TO_START -> "D.C. → início"
                                    com.example.util.RelationshipType.DAL_SEGNO_TO_SEGNO -> "D.S. → página ${targetPage + 1}"
                                    com.example.util.RelationshipType.TO_CODA_TO_CODA -> "To Coda → página ${targetPage + 1}"
                                    com.example.util.RelationshipType.AL_FINE_TO_FINE -> "Fine → página ${targetPage + 1}"
                                    else -> "Salto executado"
                                }
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(targetPage)
                                    snackbarHostState.showSnackbar(msg)
                                }
                            }
                            currentMusicalAction = null 
                        },
                        onDismiss = { currentMusicalAction = null }
                    )
                }
            } else if (upcomingMarker != null && flowContext.showMarkers) {
                val distance = upcomingMarker.page - pagerState.currentPage
                val typeStr = when (upcomingMarker.type) {
                    com.example.util.ScoreMarkerType.DA_CAPO -> "Da Capo"
                    com.example.util.ScoreMarkerType.DAL_SEGNO -> "Dal Segno"
                    com.example.util.ScoreMarkerType.TO_CODA -> "To Coda"
                    com.example.util.ScoreMarkerType.FINE, com.example.util.ScoreMarkerType.AL_FINE -> "Fine"
                    com.example.util.ScoreMarkerType.SEGNO -> "Segno"
                    com.example.util.ScoreMarkerType.CODA -> "Coda"
                    else -> "Instrução"
                }
                PerformanceCueOverlay(
                    title = "Prepare-se",
                    description = "$typeStr em $distance página" + if (distance > 1) "s" else ""
                )
            }
        }

        AnimatedVisibility(
            visible = showPerformancePanel,
            enter = slideInHorizontally(initialOffsetX = { -it }),
            exit = slideOutHorizontally(targetOffsetX = { -it }),
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            com.example.ui.components.PerformanceSidebar(
                repertoire = repertoire,
                manuscripts = allManuscripts,
                currentSongId = manuscriptId,
                playedSongs = playedPerformanceSongs,
                onSongClick = { id -> 
                    showPerformancePanel = false
                    onNavigateToManuscript(id) 
                },
                onClose = { showPerformancePanel = false },
                onFinish = {
                    showPerformancePanel = false
                    showFinishDialog = true
                },
                startTime = performanceStartTime,
                elapsedTime = performanceElapsedTime,
                isTimerRunning = isPerformanceTimerRunning,
                onTimerStart = {
                    if (repertoireId != null) {
                        performanceStartTime = System.currentTimeMillis()
                        prefsManager.setPerformanceStartTime(repertoireId, performanceStartTime)
                        isPerformanceTimerRunning = true
                    }
                },
                onTimerPause = {
                    if (repertoireId != null && isPerformanceTimerRunning) {
                        val elapsedNow = performanceElapsedTime + (System.currentTimeMillis() - performanceStartTime)
                        performanceElapsedTime = elapsedNow
                        prefsManager.setPerformanceElapsedTime(repertoireId, elapsedNow)
                        isPerformanceTimerRunning = false
                    }
                },
                onTimerReset = {
                    if (repertoireId != null) {
                        isPerformanceTimerRunning = false
                        performanceElapsedTime = 0L
                        performanceStartTime = 0L
                        prefsManager.clearPerformanceTime(repertoireId)
                    }
                }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            if (performanceSession?.isRunning == true) {
                com.example.ui.screens.PerformanceDashboard(
                    state = dashboardState,
                    presentation = dashboardPresentation,
                    flowContext = flowContext,
                    semanticState = semanticReadingState,
                    adaptiveGuidance = adaptiveGuidanceState
                )
            } else {
                AnimatedVisibility(
                    visible = flowContext.showHud,
                    enter = fadeIn() + androidx.compose.animation.slideInVertically { -it },
                    exit = fadeOut() + androidx.compose.animation.slideOutVertically { -it }
                ) {
                    Box(modifier = Modifier.graphicsLayer(alpha = flowContext.opacity)) {
                        ReadingHud(
                            currentPage = pagerState.currentPage,
                            pageCount = pageCount,
                            musicalStructure = musicalStructure,
                            musicalTimeline = musicalTimeline,
                            scoreRelationships = scoreRelationships,
                            detectedMarkers = detectedMarkers,
                            isPerformanceMode = repertoireId != null,
                            isFocusMode = isExtremeFocusMode,
                            isScrollInProgress = pagerState.isScrollInProgress,
                            readingContext = readingContext
                        )
                    }
                }
            }
        }

        PerformanceSessionReport(
            session = performanceSession,
            onClose = { com.example.util.PerformanceSessionManager.clearSession() }
        )

        androidx.compose.material3.SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp)
        )

        }
        }
        }
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
    repertoireId: Int? = null,
    topBarHeight: androidx.compose.ui.unit.Dp,
    bottomBarHeight: androidx.compose.ui.unit.Dp,
    viewModel: MainViewModel,
    onSongChartSelected: (Int) -> Unit
) {
    val songCharts by viewModel.getSongCharts(manuscriptId).collectAsStateWithLifecycle(emptyList())
    val repertoireSongs by if (repertoireId != null) viewModel.getSongsForRepertoire(repertoireId).collectAsStateWithLifecycle(emptyList()) else remember { mutableStateOf(emptyList()) }
    
    val context = LocalContext.current
    val prefsManager = remember { com.example.util.PreferencesManager(context) }
    var updateTrigger by remember { mutableIntStateOf(0) }
    
    var selectedFilter by remember { mutableStateOf("Todos") }
    val displayCharts = remember(songCharts, selectedFilter, updateTrigger) {
        when (selectedFilter) {
            "Favoritas" -> songCharts.filter { prefsManager.isFavoriteSong(it.id) }
            "Mais Tocadas" -> {
                val mostPlayed = prefsManager.getMostPlayedSongs(20)
                val sorted = mostPlayed.mapNotNull { mp -> songCharts.find { it.id == mp.first } }
                sorted
            }
            "Recentes" -> {
                val recent = prefsManager.getRecentSongs(20)
                val sorted = recent.mapNotNull { r -> songCharts.find { it.id == r.first } }
                sorted
            }
            else -> songCharts
        }
    }

    androidx.compose.material3.Scaffold(
        containerColor = Color.Transparent
    ) { innerPadding ->
        if (songCharts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("Nenhuma música encontrada.", color = MaterialTheme.colorScheme.onSurface)
            }
            return@Scaffold
        }

        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = topBarHeight + 16.dp, 
                bottom = bottomBarHeight + 16.dp, 
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                androidx.compose.foundation.lazy.LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        androidx.compose.material3.FilterChip(
                            selected = selectedFilter == "Todos",
                            onClick = { selectedFilter = "Todos" },
                            label = { Text("Todas as músicas") }
                        )
                    }
                    item {
                        androidx.compose.material3.FilterChip(
                            selected = selectedFilter == "Favoritas",
                            onClick = { selectedFilter = "Favoritas" },
                            label = { Text("⭐ Favoritas") }
                        )
                    }
                    item {
                        androidx.compose.material3.FilterChip(
                            selected = selectedFilter == "Mais Tocadas",
                            onClick = { selectedFilter = "Mais Tocadas" },
                            label = { Text("🔥 Mais Tocadas") }
                        )
                    }
                    item {
                        androidx.compose.material3.FilterChip(
                            selected = selectedFilter == "Recentes",
                            onClick = { selectedFilter = "Recentes" },
                            label = { Text("🕒 Recentes") }
                        )
                    }
                }
                
                if (selectedFilter == "Favoritas" || selectedFilter == "Mais Tocadas" || selectedFilter == "Recentes") {
                    Text(
                        text = "${displayCharts.size} músicas",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }

            if (selectedFilter == "Favoritas" && displayCharts.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("⭐", style = MaterialTheme.typography.displayMedium)
                            Text("Nenhuma música favorita.", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                            Text("Toque na estrela para adicionar músicas.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else if (selectedFilter == "Recentes" && displayCharts.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("🕒", style = MaterialTheme.typography.displayMedium)
                            Text("Nenhuma música aberta recentemente.", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            } else if (selectedFilter == "Mais Tocadas" && displayCharts.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("🔥", style = MaterialTheme.typography.displayMedium)
                            Text("Nenhuma estatística disponível.", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            } else if (displayCharts.isNotEmpty() || selectedFilter == "Todos") {
                items(
                    count = displayCharts.size,
                    key = { index -> displayCharts[index].id }
                ) { index ->
                val chart = displayCharts[index]
                val repertoireSong = repertoireSongs.find { it.songChartId == chart.id }
            
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
                    if (repertoireId != null) {
                        // Dummy read of updateTrigger to cause recomposition when state changes
                        updateTrigger
                        val isPlayed = prefsManager.isSongPlayed(repertoireId, chart.id)
                        androidx.compose.material3.Checkbox(
                            checked = isPlayed,
                            onCheckedChange = { checked ->
                                prefsManager.setSongPlayed(repertoireId, chart.id, checked)
                                updateTrigger++
                            },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                    Text(
                        text = chart.title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    )
                    
                    // Trigger recomposition correctly
                    updateTrigger
                    val isFav = prefsManager.isFavoriteSong(chart.id)
                    IconButton(
                        onClick = { 
                            prefsManager.toggleFavoriteSong(chart.id)
                            updateTrigger++
                        },
                        modifier = Modifier.padding(end = 8.dp).size(48.dp)
                    ) {
                        Icon(
                            imageVector = if (isFav) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = if (isFav) "Desfavoritar" else "Favoritar",
                            tint = if (isFav) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    val key = if (repertoireId != null) { repertoireSong?.customKey ?: chart.originalKey } else { chart.savedKey ?: chart.originalKey }
                    if (key.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val isTransposed = if (repertoireId != null) { repertoireSong?.customKey != null && repertoireSong.customKey != chart.originalKey } else { chart.savedKey != null && chart.savedKey != chart.originalKey }
                            if (isTransposed) {
                                Text(
                                    "● Transposto", 
                                    color = MaterialTheme.colorScheme.primary, 
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            }
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
    }
    }
}

@Composable
fun PageContent(
    page: Int, 
    localDocument: com.example.util.DocumentContent?, 
    defaultPages: List<String>, 
    isChoirMode: Boolean = false,
    annotations: List<com.example.util.PdfAnnotation> = emptyList(),
    currentTool: com.example.ui.components.AnnotationTool = com.example.ui.components.AnnotationTool.NONE,
    onAnnotationAdded: (com.example.util.PdfAnnotation) -> Unit = {},
    onEraseRequested: (Float, Float) -> Unit = { _, _ -> },
    onTextRequested: (Float, Float) -> Unit = { _, _ -> }
) {
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
                        localDocument.engine.renderPage(page)
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
                    Box {
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
                        com.example.ui.components.PdfAnnotationOverlay(
                            page = page,
                            annotations = annotations,
                            currentTool = currentTool,
                            onAnnotationAdded = onAnnotationAdded,
                            onEraseRequested = onEraseRequested,
                            onTextRequested = onTextRequested,
                            modifier = Modifier
                                .matchParentSize()
                                .padding(horizontal = 8.dp, vertical = 24.dp)
                                .graphicsLayer {
                                    val totalScale = animatedViewportScale.value
                                    scaleX = totalScale
                                    scaleY = totalScale
                                }
                        )
                    }
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
