package com.example.ui.preparation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.domain.document.MusicalDocument
import com.example.ui.components.DashboardCard
import com.example.ui.document.SectionPreviewCard
import com.example.ui.theme.AppSpacing

@Composable
fun PreparationStructureCard(
    document: MusicalDocument,
    modifier: Modifier = Modifier
) {
    DashboardCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(AppSpacing.MD),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.SM)
        ) {
            Text("Estrutura", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            document.sections.sortedBy { it.order }.forEach { section ->
                SectionPreviewCard(section = section, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}
