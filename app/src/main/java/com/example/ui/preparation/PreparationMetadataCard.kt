package com.example.ui.preparation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.domain.document.MusicalDocument
import com.example.ui.components.DashboardCard
import com.example.ui.theme.AppSpacing

@Composable
fun PreparationMetadataCard(
    document: MusicalDocument,
    modifier: Modifier = Modifier
) {
    DashboardCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(AppSpacing.MD)) {
            PreparationHeader(document = document)
        }
    }
}
