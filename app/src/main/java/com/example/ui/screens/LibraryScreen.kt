package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.shadow
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

import com.example.util.PreferencesManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: MainViewModel,
    onNavigateToReader: (Int) -> Unit,
    onNavigateToPedalSettings: () -> Unit,
    onNavigateToMaestro: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToSetlists: () -> Unit
) {
    val manuscripts by viewModel.allManuscripts.collectAsStateWithLifecycle()
    val allSongCharts by viewModel.allSongCharts.collectAsStateWithLifecycle()
    val allRepertoires by viewModel.allRepertoires.collectAsStateWithLifecycle(emptyList())
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    val prefsManager = remember { PreferencesManager(context) }
    var favoriteSet by remember { mutableStateOf(prefsManager.getFavorites()) }
    var recentList by remember { mutableStateOf(prefsManager.getRecent()) }

    var isSearchActive by remember { mutableStateOf(false) }
    var showStats by remember { mutableStateOf(false) }
    var documentToDelete by remember { mutableStateOf<Manuscript?>(null) }
    val displayList = if (searchQuery.isNotBlank()) searchResults else manuscripts

    var showMenu by remember { mutableStateOf(false) }
    var backupJsonToRestore by remember { mutableStateOf<String?>(null) }
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current

    val backupPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) {
                coroutineScope.launch {
                    try {
                        context.contentResolver.openInputStream(uri)?.use { inputStream ->
                            val json = inputStream.bufferedReader().use { it.readText() }
                            backupJsonToRestore = json
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        android.widget.Toast.makeText(context, "Erro ao ler o arquivo de backup", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    )

    val documentPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) {
                viewModel.importDocument(context, uri)
                android.widget.Toast.makeText(context, "Documento importado com sucesso!", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    )

    var importResult by remember { mutableStateOf<com.example.util.ImportResult?>(null) }
    
    val importRepertoireLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) {
                coroutineScope.launch {
                    val result = com.example.util.RepertoireShareManager.importRepertoire(context, uri, allSongCharts)
                    if (result != null) {
                        importResult = result
                    } else {
                        android.widget.Toast.makeText(context, "Erro ao importar repertório ou formato inválido.", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
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
                        androidx.compose.foundation.layout.Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Default.Menu, contentDescription = stringResource(R.string.menu_desc))
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Exportar Backup") },
                                    onClick = {
                                        showMenu = false
                                        coroutineScope.launch {
                                            try {
                                                val json = viewModel.exportBackup()
                                                val backupDir = java.io.File(context.cacheDir, "backups")
                                                if (!backupDir.exists()) backupDir.mkdirs()
                                                val backupFile = java.io.File(backupDir, "cifradroid_backup.json")
                                                backupFile.writeText(json)
                                                
                                                val uri = androidx.core.content.FileProvider.getUriForFile(
                                                    context,
                                                    "${context.packageName}.fileprovider",
                                                    backupFile
                                                )
                                                
                                                val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                                    type = "application/json"
                                                    putExtra(android.content.Intent.EXTRA_STREAM, uri)
                                                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                }
                                                context.startActivity(android.content.Intent.createChooser(shareIntent, "Compartilhar Backup via..."))
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                                android.widget.Toast.makeText(context, "Erro ao exportar backup", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Importar Backup") },
                                    onClick = {
                                        showMenu = false
                                        backupPickerLauncher.launch(arrayOf("application/json", "*/*"))
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Importar Repertório") },
                                    onClick = {
                                        showMenu = false
                                        importRepertoireLauncher.launch(arrayOf("application/json", "*/*"))
                                    }
                                )
                            }
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
                    IconButton(onClick = onNavigateToSettings) {
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
            var context = androidx.compose.ui.platform.LocalContext.current
            BottomLibraryNav(
                onSearchClick = { isSearchActive = true },
                onStatsClick = { showStats = true },
                onSetlistsClick = onNavigateToSetlists,
                onMaestroClick = onNavigateToMaestro
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { 
                    documentPickerLauncher.launch(arrayOf("application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                icon = { Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_manuscript_desc)) },
                text = { Text("Importar Partitura") }
            )
        }
    ) { paddingValues ->
        val categories = listOf("Todos os Documentos", "Favoritos", "Recentemente Abertos")
        var selectedCategory by remember { mutableStateOf(categories[0]) }

        val filteredList = when (selectedCategory) {
            "Favoritos" -> displayList.filter { favoriteSet.contains(it.id.toString()) }
            "Recentemente Abertos" -> displayList.filter { recentList.contains(it.id) }.sortedBy { recentList.indexOf(it.id) }
            else -> displayList
        }

        if (displayList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Icon(Icons.Default.LibraryMusic, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Sua biblioteca está vazia.", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    Text("Importe uma partitura para começar.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 180.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp)
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                        Text("🎵 Meu Resumo Musical", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
                        
                        val totalMusicas by remember(allSongCharts) { androidx.compose.runtime.derivedStateOf { allSongCharts.size } }
                        val totalRepertorios by remember(allRepertoires) { androidx.compose.runtime.derivedStateOf { allRepertoires.size } }
                        val favSet = prefsManager.getFavoriteSongs()
                        val totalFavoritas by remember(favSet) { androidx.compose.runtime.derivedStateOf { favSet.size } }
                        
                        val mostPlayed = remember { prefsManager.getMostPlayedSongs(Int.MAX_VALUE) }
                        val topSongInfo = mostPlayed.firstOrNull()
                        val maisTocadaNome by remember(mostPlayed, allSongCharts) { 
                            androidx.compose.runtime.derivedStateOf { topSongInfo?.first?.let { id -> allSongCharts.find { it.id == id }?.title } ?: "Nenhuma" }
                        }
                        val maisTocadaCount = topSongInfo?.second ?: 0
                        
                        val recentOpened = remember { prefsManager.getRecentSongs(1).firstOrNull() }
                        val ultimaNome by remember(recentOpened, allSongCharts) { 
                            androidx.compose.runtime.derivedStateOf { recentOpened?.first?.let { id -> allSongCharts.find { it.id == id }?.title } ?: "Nenhuma" }
                        }
                        val ultimaTempo = remember(recentOpened) {
                            recentOpened?.second?.let { time ->
                                val diff = System.currentTimeMillis() - time
                                when {
                                    diff < 60_000 -> "Há menos de 1 m"
                                    diff < 3600_000 -> "Há ${diff / 60_000} m"
                                    diff < 86400_000 -> "Há ${diff / 3600_000} h"
                                    else -> "Há ${diff / 86400_000} dias"
                                }
                            } ?: "Nunca"
                        }
                        
                        val totalVisualizacoes = remember(mostPlayed) { mostPlayed.sumOf { it.second } }
                        val totalMusicasAbertas = mostPlayed.size
                        val mediaAcessos = remember(totalVisualizacoes, totalMusicasAbertas) { 
                            if (totalMusicasAbertas > 0) String.format("%.1f", totalVisualizacoes.toFloat() / totalMusicasAbertas) else "0.0" 
                        }

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            item {
                                Card(
                                    modifier = Modifier.width(160.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("Acervo", style = MaterialTheme.typography.labelMedium)
                                        Spacer(Modifier.height(4.dp))
                                        Text("$totalMusicas músicas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Text("$totalRepertorios repertórios", style = MaterialTheme.typography.bodySmall)
                                        Text("$totalFavoritas favoritas", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                            item {
                                Card(
                                    modifier = Modifier.width(160.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("Mais Tocada", style = MaterialTheme.typography.labelMedium)
                                        Spacer(Modifier.height(4.dp))
                                        Text(maisTocadaNome, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text("$maisTocadaCount aberturas", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                            item {
                                Card(
                                    modifier = Modifier.width(160.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("Última Aberta", style = MaterialTheme.typography.labelMedium)
                                        Spacer(Modifier.height(4.dp))
                                        Text(ultimaNome, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(ultimaTempo, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                            item {
                                Card(
                                    modifier = Modifier.width(160.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("Estatísticas", style = MaterialTheme.typography.labelMedium)
                                        Spacer(Modifier.height(4.dp))
                                        Text("$totalVisualizacoes views", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Text("$totalMusicasAbertas músicas vistas", style = MaterialTheme.typography.bodySmall)
                                        Text("Média: $mediaAcessos", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }

                        Text("⭐ Minhas Favoritas", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
                        
                        val favoriteSongs = allSongCharts.filter { favSet.contains(it.id.toString()) }
                        
                        if (favoriteSongs.isEmpty()) {
                            Text("Nenhuma música favorita.", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                            Text("Toque na estrela para adicionar músicas.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                                items(favoriteSongs, key = { "fav_${it.id}" }) { song ->
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.widthIn(max = 200.dp).clickable {
                                            prefsManager.addRecent(song.manuscriptId)
                                            recentList = prefsManager.getRecent()
                                            onNavigateToReader(song.manuscriptId)
                                        }
                                    ) {
                                        Text(song.title, modifier = Modifier.padding(12.dp), maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("🔥 Mais Tocadas", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
                        val mostPlayedSongs = prefsManager.getMostPlayedSongs(20).mapNotNull { mp -> allSongCharts.find { it.id == mp.first } }
                        
                        if (mostPlayedSongs.isEmpty()) {
                            Text("Nenhuma estatística disponível.", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                        } else {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                                items(mostPlayedSongs, key = { "mp_${it.id}" }) { song ->
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.widthIn(max = 200.dp).clickable {
                                            prefsManager.addRecent(song.manuscriptId)
                                            recentList = prefsManager.getRecent()
                                            onNavigateToReader(song.manuscriptId)
                                        }
                                    ) {
                                        Text(song.title, modifier = Modifier.padding(12.dp), maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        Text("🕒 Recentes", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
                        val recentSongsList = prefsManager.getRecentSongs(20).mapNotNull { rs -> allSongCharts.find { it.id == rs.first } }
                        
                        if (recentSongsList.isEmpty()) {
                            Text("Nenhuma música aberta recentemente.", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                        } else {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                                items(recentSongsList, key = { "rs_${it.id}" }) { song ->
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.widthIn(max = 200.dp).clickable {
                                            prefsManager.addRecent(song.manuscriptId)
                                            recentList = prefsManager.getRecent()
                                            onNavigateToReader(song.manuscriptId)
                                        }
                                    ) {
                                        Text(song.title, modifier = Modifier.padding(12.dp), maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(categories) { category ->
                            FilterChip(
                                selected = selectedCategory == category,
                                onClick = { selectedCategory = category },
                                label = { Text(category) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                    }
                }
                
                if (filteredList.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Box(modifier = Modifier.fillMaxWidth().padding(top = 64.dp), contentAlignment = Alignment.Center) {
                            Text("Nenhuma partitura nesta categoria.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    items(
                        items = filteredList,
                        key = { it.id }
                    ) { manuscript ->
                        val isFav = favoriteSet.contains(manuscript.id.toString())
                        ManuscriptCard(
                            manuscript = manuscript,
                            isFavorite = isFav,
                            onToggleFavorite = {
                                prefsManager.toggleFavorite(manuscript.id)
                                favoriteSet = prefsManager.getFavorites()
                            },
                            onClick = { 
                                prefsManager.addRecent(manuscript.id)
                                recentList = prefsManager.getRecent()
                                onNavigateToReader(manuscript.id) 
                            },
                            onDelete = { documentToDelete = manuscript }
                        )
                    }
                }
            }
        }
    }
    
    documentToDelete?.let { manuscript ->
        AlertDialog(
            onDismissRequest = { documentToDelete = null },
            title = { Text("Excluir este documento?") },
            text = { Text("A remoção da partitura \"${manuscript.title}\" será definitiva. O arquivo físico será apagado.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteDocument(context, manuscript)
                        documentToDelete = null
                        android.widget.Toast.makeText(context, "Documento apagado.", android.widget.Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { documentToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    backupJsonToRestore?.let { json ->
        AlertDialog(
            onDismissRequest = { backupJsonToRestore = null },
            title = { Text("Substituir dados atuais?") },
            text = { Text("Atenção: Restaurar um backup apaga TODAS as partituras, repertórios e configurações atuais, substituindo-os pelo conteúdo do backup.\nDeseja continuar?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                viewModel.importBackup(json)
                                android.widget.Toast.makeText(context, "Backup restaurado com sucesso!", android.widget.Toast.LENGTH_LONG).show()
                            } catch (e: Exception) {
                                e.printStackTrace()
                                android.widget.Toast.makeText(context, "Erro: Arquivo Invalido. Ocorreu um problema.", android.widget.Toast.LENGTH_LONG).show()
                            } finally {
                                backupJsonToRestore = null
                            }
                        }
                    }
                ) {
                    Text("Restaurar")
                }
            },
            dismissButton = {
                TextButton(onClick = { backupJsonToRestore = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
    
    importResult?.let { result ->
        AlertDialog(
            onDismissRequest = { importResult = null },
            title = { Text("Resumo da Importação") },
            text = {
                Column {
                    Text("Repertório: ${result.repertoireName}", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                    Text("Total de músicas: ${result.totalSongs}")
                    Text("Encontradas na biblioteca: ${result.foundSongs}")
                    
                    if (result.missingSongNames.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Ausentes: ${result.missingSongNames.size}", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                        Text("Músicas não encontradas:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        androidx.compose.foundation.lazy.LazyColumn(modifier = Modifier.heightIn(max = 120.dp)) {
                            items(result.missingSongNames.size) { index ->
                                Text("- ${result.missingSongNames[index]}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    coroutineScope.launch {
                        val repWithJson = result.repertoire.copy(
                            manuscriptIdsJson = com.example.util.RepertoireUtil.toJson(result.categories)
                        )
                        viewModel.insertImportedRepertoire(repWithJson, result.songsToInsert)
                        android.widget.Toast.makeText(context, "Repertório importado com sucesso!", android.widget.Toast.LENGTH_SHORT).show()
                        importResult = null
                    }
                }) {
                    Text("Importar")
                }
            },
            dismissButton = {
                TextButton(onClick = { importResult = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showStats) {
        AlertDialog(
            onDismissRequest = { showStats = false },
            title = { Text("Estatísticas e Histórico", style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Tempo Médio/Música", style = MaterialTheme.typography.labelMedium)
                            Text("3m 42s", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total em Repertórios", style = MaterialTheme.typography.labelMedium)
                            Text("${manuscripts.size} partituras", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    HorizontalDivider()
                    Text("Mais tocadas (Últimos 30 dias)", style = MaterialTheme.typography.titleSmall)
                    if (manuscripts.isNotEmpty()) {
                        manuscripts.take(3).forEachIndexed { index, ms ->
                            Text("${index + 1}. ${ms.title} (${ms.composer ?: "Desconhecido"})", style = MaterialTheme.typography.bodyMedium)
                        }
                    } else {
                        Text("Histórico vazio.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showStats = false }) { Text("Fechar") }
            }
        )
    }
}

@Composable
fun BottomLibraryNav(
    onSearchClick: () -> Unit,
    onStatsClick: () -> Unit,
    onSetlistsClick: () -> Unit,
    onMaestroClick: () -> Unit
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
            onClick = onSetlistsClick,
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
            onClick = onMaestroClick,
            icon = { Icon(Icons.Default.WifiTethering, contentDescription = "Maestro") },
            label = { Text("Maestro") }
        )
        NavigationBarItem(
            selected = false,
            onClick = onStatsClick,
            icon = { Icon(Icons.Default.History, contentDescription = stringResource(R.string.history)) },
            label = { Text("Estatísticas") }
        )
    }
}


@Composable
fun ManuscriptCard(
    manuscript: Manuscript,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .clickable { onClick() }
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.7f)
                .shadow(12.dp, RoundedCornerShape(8.dp), spotColor = Color.Black.copy(alpha = 0.5f))
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

            if (isFavorite) {
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
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
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
            
            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Mais opções")
                }
                
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(if (isFavorite) "Remover dos Favoritos" else "Adicionar aos Favoritos") },
                        onClick = {
                            expanded = false
                            onToggleFavorite()
                        },
                        leadingIcon = {
                            Icon(if (isFavorite) Icons.Default.StarOutline else Icons.Default.Star, contentDescription = null)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Excluir", color = MaterialTheme.colorScheme.error) },
                        onClick = {
                            expanded = false
                            onDelete()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        }
                    )
                }
            }
        }
    }
}
