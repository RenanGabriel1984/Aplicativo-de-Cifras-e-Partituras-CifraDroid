package com.example.ui.components.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.domain.library.LibraryCollection
import com.example.ui.components.DashboardCard
import com.example.ui.theme.AppSpacing

@Composable
fun LibraryCollectionCard(
    collection: LibraryCollection,
    modifier: Modifier = Modifier
) {
    DashboardCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(AppSpacing.MD),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.MD),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Folder, contentDescription = collection.title, tint = MaterialTheme.colorScheme.primary)
            Text(text = collection.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
