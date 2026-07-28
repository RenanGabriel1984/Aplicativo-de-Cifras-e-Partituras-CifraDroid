package com.example.ui.preparation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.domain.preparation.PreparationState
import com.example.ui.theme.AppSpacing
import com.example.ui.layout.ResponsiveStageState

@Composable
fun PreparationWorkspaceScreen(
    state: PreparationState,
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
            item {
                PreparationMetadataCard(document = state.document)
            }
            item {
                PreparationStructureCard(document = state.document)
            }
            item {
                PreparationActionsCard(actions = state.availableActions)
            }
            item {
                PreparationDestinationCard(destinations = state.destinations)
            }
            item {
                PreparationSummaryCard(summary = state.summary)
            }
        }
    }
}
