package com.example.ui.document

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.domain.document.DocumentSection
import com.example.ui.components.DashboardCard
import com.example.ui.components.DashboardDivider
import com.example.ui.theme.AppSpacing

@Composable
fun SectionPreviewCard(
    section: DocumentSection,
    modifier: Modifier = Modifier
) {
    DashboardCard(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(AppSpacing.MD),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.SM)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = section.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = section.semanticType,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            DashboardDivider()
            if (section.chords.isNotBlank()) {
                Text(
                    text = section.chords,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            if (section.lyrics.isNotBlank()) {
                Text(
                    text = section.lyrics,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
