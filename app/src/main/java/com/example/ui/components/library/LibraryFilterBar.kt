package com.example.ui.components.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.domain.library.LibraryFilter
import com.example.ui.components.BadgeColor
import com.example.ui.components.DashboardBadge
import com.example.ui.theme.AppSpacing

@Composable
fun LibraryFilterBar(
    currentFilter: LibraryFilter?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.SM),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DashboardBadge(text = "Filtros", badgeColor = BadgeColor.SURFACE_VARIANT, isPill = true)
        if (currentFilter != null) {
            DashboardBadge(text = "${currentFilter.name}: ${currentFilter.value}", badgeColor = BadgeColor.PRIMARY, isPill = true)
        }
    }
}
