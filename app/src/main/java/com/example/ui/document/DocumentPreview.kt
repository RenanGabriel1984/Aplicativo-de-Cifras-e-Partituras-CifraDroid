package com.example.ui.document

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.domain.document.MusicalDocument
import com.example.ui.theme.AppSpacing

@Composable
fun DocumentPreview(
    document: MusicalDocument,
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
            item {
                DocumentHeader(document = document)
            }
            
            item {
                Text(
                    text = "Seções",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            items(document.sections.sortedBy { it.order }) { section ->
                SectionPreviewCard(section = section)
            }
        }
    }
}
