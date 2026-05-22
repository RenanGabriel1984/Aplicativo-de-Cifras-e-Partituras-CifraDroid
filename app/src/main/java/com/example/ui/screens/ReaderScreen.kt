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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.res.stringResource
import com.example.R

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReaderScreen(
    viewModel: MainViewModel,
    manuscriptId: Int,
    onNavigateBack: () -> Unit
) {
    val manuscript by viewModel.getById(manuscriptId).collectAsStateWithLifecycle(initialValue = null)
    val isVerticalScroll by viewModel.isVerticalScroll.collectAsStateWithLifecycle()
    var showHud by remember { mutableStateOf(false) }
    val pages = DataProvider.readerPages
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()
    
    val toggleHud = { showHud = !showHud }
    val focusRequester = remember { FocusRequester() }
    val context = LocalContext.current
    
    val pagerModifier = Modifier
        .fillMaxSize()
        .pointerInput(Unit) {
            detectTapGestures(
                onTap = { offset ->
                    val width = size.width
                    val x = offset.x
                    // 1f, 1.5f, 1f hit zones mapped to width
                    val leftBoundary = width * (1f / 3.5f)
                    val rightBoundary = width * (2.5f / 3.5f)
                    if (x < leftBoundary) {
                        coroutineScope.launch {
                            if (pagerState.currentPage > 0) pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    } else if (x > rightBoundary) {
                        coroutineScope.launch {
                            if (pagerState.currentPage < pages.size - 1) pagerState.animateScrollToPage(pagerState.currentPage + 1)
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
        
        if (prev >= 0 && prev < pages.size) {
            val request = ImageRequest.Builder(context).data(pages[prev]).build()
            context.imageLoader.enqueue(request)
        }
        if (next >= 0 && next < pages.size) {
            val request = ImageRequest.Builder(context).data(pages[next]).build()
            context.imageLoader.enqueue(request)
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
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF131313)) // Deep black for reader
                .focusRequester(focusRequester)
                .focusable()
                .onKeyEvent { keyEvent ->
                    when (keyEvent.key) {
                        Key.DirectionLeft, Key.PageUp, Key.DirectionUp -> {
                            if (keyEvent.type == KeyEventType.KeyUp) {
                                coroutineScope.launch {
                                    if (!pagerState.isScrollInProgress && pagerState.currentPage > 0) {
                                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                    }
                                }
                            }
                            true
                        }
                        Key.DirectionRight, Key.PageDown, Key.DirectionDown, Key.Spacebar, Key.Enter -> {
                            if (keyEvent.type == KeyEventType.KeyUp) {
                                coroutineScope.launch {
                                    if (!pagerState.isScrollInProgress && pagerState.currentPage < pages.size - 1) {
                                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                    }
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
                    modifier = pagerModifier
                ) { page ->
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFFCFAF2))
                    ) {
                        AsyncImage(
                            model = pages[page],
                            contentDescription = stringResource(R.string.page_desc, page + 1),
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            } else {
                HorizontalPager(
                    state = pagerState,
                    modifier = pagerModifier
                ) { page ->
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFFCFAF2))
                    ) {
                        AsyncImage(
                            model = pages[page],
                            contentDescription = stringResource(R.string.page_desc, page + 1),
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
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
                                    text = currentManuscript.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = currentManuscript.composer,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
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
                                }
                            }
                        }
                    }

                    // Bottom Bar
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.95f),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 32.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            IconButton(onClick = { 
                                coroutineScope.launch {
                                    if (pagerState.currentPage > 0) pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                }
                            }) {
                                Icon(Icons.Default.ChevronLeft, contentDescription = stringResource(R.string.previous))
                            }
                            
                            Text(
                                text = "${pagerState.currentPage + 1} / ${pages.size}",
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            
                            IconButton(onClick = { 
                                coroutineScope.launch {
                                    if (pagerState.currentPage < pages.size - 1) pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            }) {
                                Icon(Icons.Default.ChevronRight, contentDescription = stringResource(R.string.next))
                            }
                        }
                    }
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
