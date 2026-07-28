package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.domain.library.MusicLibraryState
import com.example.ui.components.library.*
import com.example.ui.theme.AppSpacing
import com.example.ui.layout.ResponsiveStageState

@Composable
fun MusicLibraryScreen(
    state: MusicLibraryState,
    responsiveState: ResponsiveStageState,
    modifier: Modifier = Modifier
) {
    Scaffold(modifier = modifier.fillMaxSize()) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = AppSpacing.MD),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.LG),
            contentPadding = PaddingValues(vertical = AppSpacing.LG)
        ) {
            // HEADER
            item {
                LibraryHeader(
                    title = "Minha Biblioteca",
                    totalSongs = state.statistics.totalSongs,
                    totalRepertoires = state.statistics.repertoires
                )
            }

            // COLEÇÕES
            item {
                Text(text = "Coleções", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(AppSpacing.SM))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.heightIn(max = 400.dp), // Restrict height for lazy grid inside lazy column
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.SM),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.SM),
                    userScrollEnabled = false // Disable scroll so it flows with the LazyColumn
                ) {
                    items(state.collections) { collection ->
                        LibraryCollectionCard(collection = collection)
                    }
                }
            }

            // FILTER & SORT BAR
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LibraryFilterBar(currentFilter = state.currentFilter)
                    LibrarySortButton(currentSort = state.currentSort, onClick = { /* TODO */ })
                }
            }

            // LISTA DE DOCUMENTOS MUSICAIS
            items(state.documents) { doc ->
                LibrarySongCard(song = doc, isFavorite = false, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}
