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
import com.example.domain.preparation.PreparationDestination
import com.example.ui.components.BadgeColor
import com.example.ui.components.DashboardBadge
import com.example.ui.components.DashboardCard
import com.example.ui.theme.AppSpacing

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PreparationDestinationCard(
    destinations: List<PreparationDestination>,
    modifier: Modifier = Modifier
) {
    DashboardCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(AppSpacing.MD),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.SM)
        ) {
            Text("Destino", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.SM),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.SM),
                modifier = Modifier.fillMaxWidth()
            ) {
                destinations.forEach { dest ->
                    val destName = when (dest) {
                        PreparationDestination.WORKSPACE -> "Workspace"
                        PreparationDestination.REPERTOIRE -> "Repertório"
                        PreparationDestination.FAVORITES -> "Favoritos"
                        PreparationDestination.MASS -> "Missa"
                        PreparationDestination.REHEARSAL -> "Ensaio"
                        PreparationDestination.RETREAT -> "Retiro"
                        PreparationDestination.CUSTOM -> "Customizado"
                    }
                    DashboardBadge(text = destName, badgeColor = BadgeColor.PRIMARY, isPill = false)
                }
            }
        }
    }
}
