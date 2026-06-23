package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Manuscript
import com.example.data.SongChart
import com.example.ui.MainViewModel
import com.example.util.RepertoireUtil
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Share

enum class RepertoireSortOrder(val label: String) {
    DEFAULT("Ordem do Repertório"),
    A_Z("A → Z"),
    Z_A("Z → A"),
    KEY("Tom Musical"),
    NEWEST("Mais Recentes")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRepertoireScreen(
    viewModel: MainViewModel,
    repertoireId: Int,
    onNavigateBack: () -> Unit,
    onNavigateToReader: (Int, Int) -> Unit
) {
    val repertoireFlow = viewModel.getRepertoire(repertoireId).collectAsStateWithLifecycle(initialValue = null)
    val repertoire = repertoireFlow.value
    
    val allSongCharts by viewModel.allSongCharts.collectAsStateWithLifecycle(emptyList())
    val allManuscripts by viewModel.allManuscripts.collectAsStateWithLifecycle(emptyList())
    
    if (repertoire == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    var categories by remember(repertoire.manuscriptIdsJson) { 
        mutableStateOf(RepertoireUtil.getCategories(repertoire)) 
    }
    
    val repertoireSongs by viewModel.getSongsForRepertoire(repertoireId).collectAsStateWithLifecycle(emptyList())
    var searchQuery by remember { mutableStateOf("") }
    var sortOrder by remember { mutableStateOf(RepertoireSortOrder.DEFAULT) }
    
    val processedCategories = remember(categories, allSongCharts, repertoireSongs, searchQuery, sortOrder) {
        categories.map { cat ->
            val filteredIds = cat.manuscriptIds.filter { songId ->
                val song = allSongCharts.find { it.id == songId } ?: return@filter false
                val repSong = repertoireSongs.find { it.songChartId == songId }
                val customKey = repSong?.customKey
                val originalKey = song.originalKey
                
                if (searchQuery.isBlank()) true
                else {
                    val q = searchQuery.lowercase()
                    song.title.lowercase().contains(q) ||
                    originalKey.lowercase().contains(q) ||
                    (customKey != null && customKey.lowercase().contains(q)) ||
                    cat.name.lowercase().contains(q)
                }
            }
            
            val sortedIds = when (sortOrder) {
                RepertoireSortOrder.DEFAULT -> filteredIds
                RepertoireSortOrder.A_Z -> filteredIds.sortedBy { id -> allSongCharts.find { it.id == id }?.title?.lowercase() }
                RepertoireSortOrder.Z_A -> filteredIds.sortedByDescending { id -> allSongCharts.find { it.id == id }?.title?.lowercase() }
                RepertoireSortOrder.KEY -> filteredIds.sortedBy { id -> 
                    val s = allSongCharts.find { it.id == id }
                    val rs = repertoireSongs.find { it.songChartId == id }
                    rs?.customKey ?: s?.originalKey ?: ""
                }
                RepertoireSortOrder.NEWEST -> filteredIds.sortedByDescending { it } // Assuming larger ID = newer
            }
            
            cat.copy(manuscriptIds = sortedIds.toMutableList())
        }.filter { it.manuscriptIds.isNotEmpty() || searchQuery.isBlank() } // Hide empty categories when searching
    }

    var showAddSongDialog by remember { mutableStateOf<Int?>(null) } // Category index
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    var collapsedGroups by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(emptySet<String>()) }

    val saveChanges = {
        val newJson = RepertoireUtil.toJson(categories)
        viewModel.insertRepertoire(repertoire.copy(manuscriptIdsJson = newJson))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(repertoire.name) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar") }
                },
                actions = {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val scope = androidx.compose.runtime.rememberCoroutineScope()
                    IconButton(onClick = {
                        scope.launch {
                            com.example.util.RepertoireShareManager.exportRepertoire(context, repertoire, repertoireSongs, categories, allSongCharts)
                        }
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Compartilhar Repertório")
                    }
                    TextButton(onClick = { showAddCategoryDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Novo Grupo do Repertório")
                        Spacer(Modifier.width(4.dp))
                        Text("Novo Grupo")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            val totalSongs = categories.sumOf { it.manuscriptIds.size }
            val totalGroups = categories.size
            Text("$totalSongs músicas • $totalGroups grupos", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp))
            
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Buscar...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Clear, "Limpar") }
                        }
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )
                
                var showSortMenu by remember { mutableStateOf(false) }
                Box(modifier = Modifier.padding(start = 8.dp)) {
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(Icons.Default.Sort, contentDescription = "Ordenar")
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        RepertoireSortOrder.values().forEach { order ->
                            DropdownMenuItem(
                                text = { Text(order.label) },
                                onClick = { sortOrder = order; showSortMenu = false },
                                trailingIcon = if (sortOrder == order) { { Icon(Icons.Default.Check, null) } } else null
                            )
                        }
                    }
                }
            }
            
            if (processedCategories.isEmpty() && categories.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Nenhuma música encontrada", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else if (categories.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.LibraryMusic,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Este repertório ainda não possui músicas",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Toque em Adicionar Música para começar.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(processedCategories.size) { catIdx ->
                        val category = processedCategories[catIdx]
                        val originalCatIdx = categories.indexOfFirst { it.name == category.name }
                        val isCollapsed = collapsedGroups.contains(category.name)
                        
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically, 
                                    horizontalArrangement = Arrangement.SpaceBetween, 
                                    modifier = Modifier.fillMaxWidth()
                                        .clickable {
                                            collapsedGroups = if (isCollapsed) collapsedGroups - category.name else collapsedGroups + category.name
                                        }
                                        .padding(start = 16.dp, end = 8.dp, top = 16.dp, bottom = 16.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Icon(
                                            if (isCollapsed) androidx.compose.material.icons.Icons.Default.KeyboardArrowRight else androidx.compose.material.icons.Icons.Default.ArrowDropDown, 
                                            contentDescription = "Expandir/Recolher",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Column {
                                            Text(category.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                                            Text("${category.manuscriptIds.size} músicas", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                        }
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (originalCatIdx > 0 && searchQuery.isBlank() && sortOrder == RepertoireSortOrder.DEFAULT) {
                                            IconButton(onClick = {
                                                val mCats = categories.toMutableList()
                                                val temp = mCats[originalCatIdx]
                                                mCats[originalCatIdx] = mCats[originalCatIdx - 1]
                                                mCats[originalCatIdx - 1] = temp
                                                categories = mCats
                                                saveChanges()
                                            }) { Icon(Icons.Default.ArrowUpward, contentDescription = "Subir Categoria") }
                                        }
                                        if (originalCatIdx != -1) {
                                            TextButton(onClick = { showAddSongDialog = originalCatIdx }) {
                                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                                Spacer(Modifier.width(4.dp))
                                                Text("Adicionar Música")
                                            }
                                        }
                                    }
                                }
                                
                                if (!isCollapsed) {
                                    Column(modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                                        if (category.manuscriptIds.isEmpty()) {
                                            Text("Nenhuma música nesta categoria.", color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
                                        } else {
                                            category.manuscriptIds.forEachIndexed { itemIdx, songId ->
                                                val song = allSongCharts.find { it.id == songId }
                                                val repSong = repertoireSongs.find { it.songChartId == songId }
                                                if (song != null) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onNavigateToReader(songId, repertoire.id) },
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text("${itemIdx + 1}.", modifier = Modifier.width(24.dp))
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text(song.title, fontWeight = FontWeight.Bold)
                                                            Text("Tom Original: ${song.originalKey}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                                            val customKey = repSong?.customKey
                                                            if (customKey != null && customKey != song.originalKey) {
                                                                Text("Tom Repertório: $customKey", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                                            }
                                                            Text("Categoria: ${category.name}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                                        }
                                                        
                                                        if (searchQuery.isBlank() && sortOrder == RepertoireSortOrder.DEFAULT && originalCatIdx != -1) {
                                                            val originalItemIdx = categories[originalCatIdx].manuscriptIds.indexOf(songId)
                                                            if (originalItemIdx > 0) {
                                                                IconButton(onClick = {
                                                                    val mCats = categories.toMutableList()
                                                                    val mItems = mCats[originalCatIdx].manuscriptIds.toMutableList()
                                                                    val temp = mItems[originalItemIdx]
                                                                    mItems[originalItemIdx] = mItems[originalItemIdx - 1]
                                                                    mItems[originalItemIdx - 1] = temp
                                                                    mCats[originalCatIdx] = mCats[originalCatIdx].copy(manuscriptIds = mItems)
                                                                    categories = mCats
                                                                    saveChanges()
                                                                }) { Icon(Icons.Default.ArrowUpward, "Subir Música") }
                                                            }
                                                        }
                                                        if (originalCatIdx != -1) {
                                                            IconButton(onClick = {
                                                                val originalItemIdx = categories[originalCatIdx].manuscriptIds.indexOf(songId)
                                                                if (originalItemIdx != -1) {
                                                                    val mCats = categories.toMutableList()
                                                                    val mItems = mCats[originalCatIdx].manuscriptIds.toMutableList()
                                                                    mItems.removeAt(originalItemIdx)
                                                                    mCats[originalCatIdx] = mCats[originalCatIdx].copy(manuscriptIds = mItems)
                                                                    categories = mCats
                                                                    saveChanges()
                                                                }
                                                            }) { Icon(Icons.Default.Delete, "Remover") }
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
                    
                    item { Spacer(modifier = Modifier.height(32.dp)) }
                }
            }
        }
        
        if (showAddCategoryDialog) {
            AlertDialog(
                onDismissRequest = { showAddCategoryDialog = false },
                title = { Text("Novo Grupo do Repertório") },
                text = {
                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = { newCategoryName = it },
                        label = { Text("Ex.: Entrada, Comunhão, Final, Louvor, Adoração") }
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (newCategoryName.isNotBlank()) {
                            val newCat = RepertoireUtil.RepertoireCategory(newCategoryName, mutableListOf())
                            categories = categories + newCat
                            saveChanges()
                            showAddCategoryDialog = false
                            newCategoryName = ""
                        }
                    }) { Text("Adicionar") }
                }
            )
        }
        
        if (showAddSongDialog != null) {
            AddSongLibraryDialog(
                allSongCharts = allSongCharts,
                allManuscripts = allManuscripts,
                repertoireSongs = repertoireSongs,
                onAddSong = { songId ->
                    val catIdx = showAddSongDialog!!
                    val mCats = categories.toMutableList()
                    val mItems = mCats[catIdx].manuscriptIds.toMutableList()
                    mItems.add(songId)
                    mCats[catIdx] = mCats[catIdx].copy(manuscriptIds = mItems)
                    categories = mCats
                    saveChanges()
                    showAddSongDialog = null
                },
                onDismiss = { showAddSongDialog = null }
            )
        }
    }
}

enum class LibrarySortOrder(val label: String) {
    A_Z("A → Z"),
    Z_A("Z → A"),
    NEWEST("Mais Recentes")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSongLibraryDialog(
    allSongCharts: List<SongChart>,
    allManuscripts: List<Manuscript>,
    repertoireSongs: List<com.example.data.RepertoireSong>,
    onAddSong: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var sortOrder by remember { mutableStateOf(LibrarySortOrder.NEWEST) }
    var showSortMenu by remember { mutableStateOf(false) }

    val manuscriptMap = remember(allManuscripts) {
        allManuscripts.associateBy { it.id }
    }
    
    val filteredSongs = remember(allSongCharts, searchQuery, sortOrder, manuscriptMap, repertoireSongs) {
        val q = searchQuery.lowercase().trim()
        var filtered = if (q.isEmpty()) {
            allSongCharts
        } else {
            allSongCharts.filter { song ->
                val manuscript = manuscriptMap[song.manuscriptId]
                val manuscriptCategory = manuscript?.category?.lowercase() ?: ""
                
                val repSong = repertoireSongs.find { it.songChartId == song.id }
                val customKey = repSong?.customKey ?: song.savedKey ?: ""
                
                song.title.lowercase().contains(q) ||
                song.originalKey.lowercase().contains(q) ||
                customKey.lowercase().contains(q) ||
                manuscriptCategory.contains(q)
            }
        }

        when (sortOrder) {
            LibrarySortOrder.A_Z -> filtered = filtered.sortedBy { it.title.lowercase() }
            LibrarySortOrder.Z_A -> filtered = filtered.sortedByDescending { it.title.lowercase() }
            LibrarySortOrder.NEWEST -> filtered = filtered.sortedByDescending { it.id }
        }
        filtered
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp),
            shape = MaterialTheme.shapes.extraLarge.copy(bottomStart = CornerSize(0.dp), bottomEnd = CornerSize(0.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar")
                    }
                    Text(
                        text = "Adicionar Música",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.Default.Sort, contentDescription = "Ordenar")
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            LibrarySortOrder.values().forEach { order ->
                                DropdownMenuItem(
                                    text = { Text(order.label) },
                                    onClick = {
                                        sortOrder = order
                                        showSortMenu = false
                                    },
                                    trailingIcon = if (sortOrder == order) {
                                        { Icon(Icons.Default.Check, null) }
                                    } else {
                                        null
                                    }
                                )
                            }
                        }
                    }
                }

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 8.dp),
                    placeholder = { Text("Buscar por título, tom ou grupo...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, "Limpar")
                            }
                        }
                    },
                    shape = MaterialTheme.shapes.extraLarge,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                )

                // Counter
                Text(
                    text = "${filteredSongs.size} músicas encontradas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )

                // List or Empty State
                if (filteredSongs.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.LibraryMusic,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Nenhuma música encontrada.",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
                    ) {
                        items(items = filteredSongs, key = { it.id }) { song ->
                            val manuscript = manuscriptMap[song.manuscriptId]
                            val repSong = repertoireSongs.find { it.songChartId == song.id }
                            val customKey = repSong?.customKey ?: song.savedKey
                            val keyText = if (customKey != null) "Tom: $customKey (Original: ${song.originalKey})" else "Tom: ${song.originalKey}"
                            val catText = manuscript?.category?.takeIf { it.isNotBlank() }
                            val subtitle = listOfNotNull(catText, keyText).joinToString(" • ")
                            
                            ListItem(
                                headlineContent = { Text(song.title, fontWeight = FontWeight.SemiBold) },
                                supportingContent = {
                                    if (subtitle.isNotEmpty()) {
                                        Text(subtitle)
                                    }
                                },
                                modifier = Modifier.clickable {
                                    onAddSong(song.id)
                                }
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
                        }
                    }
                }
            }
        }
    }
}
