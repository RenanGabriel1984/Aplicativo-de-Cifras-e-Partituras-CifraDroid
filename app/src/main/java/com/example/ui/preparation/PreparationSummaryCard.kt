package com.example.ui.preparation

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.domain.preparation.PreparationSummary
import com.example.ui.components.BadgeColor
import com.example.ui.components.DashboardBadge
import com.example.ui.components.DashboardCard
import com.example.ui.components.DashboardProgress
import com.example.ui.theme.AppSpacing

@Composable
fun PreparationSummaryCard(
    summary: PreparationSummary,
    modifier: Modifier = Modifier
) {
    DashboardCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(AppSpacing.MD),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.SM)
        ) {
            Text("Resumo", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            
            AnimatedContent(targetState = summary, label = "summary_anim") { state ->
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.SM)) {
                    if (state.ready) {
                        DashboardBadge(text = "Documento preparado", badgeColor = BadgeColor.PRIMARY, isPill = true)
                    } else {
                        DashboardBadge(text = "${state.missingFields} Campos pendentes", badgeColor = BadgeColor.AMBER, isPill = true)
                    }
                    
                    if (state.estimatedPerformanceReady) {
                        DashboardBadge(text = "Pronto para tocar", badgeColor = BadgeColor.PRIMARY, isPill = true)
                        DashboardProgress(progress = 1.0f)
                    } else {
                        DashboardProgress(progress = 0.5f)
                    }
                }
            }
        }
    }
}
