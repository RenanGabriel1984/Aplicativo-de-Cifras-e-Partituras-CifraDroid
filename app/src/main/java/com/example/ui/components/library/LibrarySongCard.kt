package com.example.ui.components.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.domain.models.SongDocument
import com.example.ui.components.DashboardBadge
import com.example.ui.components.DashboardCard
import com.example.ui.components.BadgeColor
import com.example.ui.theme.AppSpacing

@Composable
fun LibrarySongCard(
    song: SongDocument,
    isFavorite: Boolean = false,
    modifier: Modifier = Modifier
) {
    DashboardCard(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(AppSpacing.MD),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.SM)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.SM), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MusicNote, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(text = song.metadata.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                }
                if (isFavorite) {
                    Icon(Icons.Default.Favorite, contentDescription = "Favorito", tint = MaterialTheme.colorScheme.error)
                }
            }
            Text(text = song.metadata.artist, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.SM)) {
                DashboardBadge(text = "Tom: ${song.metadata.key}", badgeColor = BadgeColor.PRIMARY, isPill = true)
                DashboardBadge(text = song.metadata.category.name, badgeColor = BadgeColor.SURFACE_VARIANT, isPill = true)
            }
        }
    }
}
