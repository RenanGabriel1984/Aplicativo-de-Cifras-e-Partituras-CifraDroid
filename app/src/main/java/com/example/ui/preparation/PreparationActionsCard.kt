package com.example.ui.preparation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.domain.preparation.PreparationAction
import com.example.ui.components.BadgeColor
import com.example.ui.components.DashboardBadge
import com.example.ui.components.DashboardCard
import com.example.ui.theme.AppSpacing

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PreparationActionsCard(
    actions: List<PreparationAction>,
    modifier: Modifier = Modifier
) {
    DashboardCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(AppSpacing.MD),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.SM)
        ) {
            Text("Preparação", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.SM),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.SM),
                modifier = Modifier.fillMaxWidth()
            ) {
                actions.forEach { action ->
                    val actionName = when (action) {
                        PreparationAction.CHANGE_KEY -> "Alterar Tom"
                        PreparationAction.CHANGE_CAPO -> "Capotraste"
                        PreparationAction.CHANGE_BPM -> "BPM"
                        PreparationAction.ADD_NOTE -> "Observações"
                        PreparationAction.MARK_ENTRY -> "Marcar Entrada"
                        PreparationAction.MARK_END -> "Marcar Final"
                        PreparationAction.MARK_REPEAT -> "Marcar Repetição"
                        PreparationAction.MARK_SOLO -> "Marcar Solo"
                        PreparationAction.MARK_DYNAMICS -> "Marcar Dinâmica"
                        PreparationAction.MARK_BREATH -> "Marcar Respiração"
                    }
                    DashboardBadge(text = actionName, badgeColor = BadgeColor.SURFACE_VARIANT, isPill = true)
                }
            }
        }
    }
}
