package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.Manuscript
import com.example.ui.MainViewModel
import androidx.compose.ui.res.stringResource
import com.example.R

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: MainViewModel,
    onNavigateToReader: (Int) -> Unit,
    onNavigateToPedalSettings: () -> Unit
) {
    val manuscripts by viewModel.allManuscripts.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    var isSearchActive by remember { mutableStateOf(false) }
    val displayList = if (searchQuery.isNotBlank()) searchResults else manuscripts

    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current

    val documentPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) {
                viewModel.importDocument(context, uri)
                android.widget.Toast.makeText(context, "Documento importado com sucesso!", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    )

    androidx.activity.compose.BackHandler(enabled = isSearchActive) {
        focusManager.clearFocus()
        isSearchActive = false
        viewModel.updateSearchQuery("")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    if (isSearchActive) {
                        TextField(
                            value = searchQuery,
                            onValueChange = viewModel::updateSearchQuery,
                            placeholder = { Text(stringResource(R.string.search)) },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(stringResource(R.string.app_name), fontWeight = FontWeight.Bold) 
                    }
                },
                navigationIcon = {
                    if (isSearchActive) {
                        IconButton(onClick = { 
                            focusManager.clearFocus()
                            isSearchActive = false 
                            viewModel.updateSearchQuery("")
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                        }
                    } else {
                        IconButton(onClick = { 
                            android.widget.Toast.makeText(context, "Menu (Em desenvolvimento)", android.widget.Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = stringResource(R.string.menu_desc))
                        }
                    }
                },
                actions = {
                    if (!isSearchActive) {
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search))
                        }
                    }
                    IconButton(onClick = onNavigateToPedalSettings) {
                        Icon(Icons.Default.BluetoothConnected, contentDescription = stringResource(R.string.pedal_config_desc))
                    }
                    IconButton(onClick = { 
                        android.widget.Toast.makeText(context, "Configurações (Em desenvolvimento)", android.widget.Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings_desc))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        },
        bottomBar = {
            BottomLibraryNav(
                onSearchClick = { isSearchActive = true },
                onNotImplementedClick = {
                    android.widget.Toast.makeText(context, "Em desenvolvimento", android.widget.Toast.LENGTH_SHORT).show()
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    documentPickerLauncher.launch(arrayOf("application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_manuscript_desc))
            }
        }
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 140.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            items(
                items = displayList,
                key = { it.id }
            ) { manuscript ->
                ManuscriptCard(
                    manuscript = manuscript,
                    onClick = { onNavigateToReader(manuscript.id) }
                )
            }
            
            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun BottomLibraryNav(
    onSearchClick: () -> Unit,
    onNotImplementedClick: () -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            selected = true,
            onClick = { },
            icon = { Icon(Icons.Default.LibraryMusic, contentDescription = stringResource(R.string.library)) },
            label = { Text(stringResource(R.string.library)) }
        )
        NavigationBarItem(
            selected = false,
            onClick = onNotImplementedClick,
            icon = { Icon(Icons.AutoMirrored.Filled.PlaylistPlay, contentDescription = stringResource(R.string.setlists)) },
            label = { Text(stringResource(R.string.setlists)) }
        )
        NavigationBarItem(
            selected = false,
            onClick = onSearchClick,
            icon = { Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search)) },
            label = { Text(stringResource(R.string.search)) }
        )
        NavigationBarItem(
            selected = false,
            onClick = onNotImplementedClick,
            icon = { Icon(Icons.Default.History, contentDescription = stringResource(R.string.history)) },
            label = { Text(stringResource(R.string.history)) }
        )
    }
}


@Composable
fun ManuscriptCard(
    manuscript: Manuscript,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.75f)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            AsyncImage(
                model = manuscript.coverUrl,
                contentDescription = manuscript.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            
            // Simulating an inner shadow/bevel
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.1f))
            )

            if (manuscript.isFavorite) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = stringResource(R.string.favorite_desc),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = manuscript.title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "${manuscript.category} · ${manuscript.era.ifEmpty { manuscript.composer }}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
