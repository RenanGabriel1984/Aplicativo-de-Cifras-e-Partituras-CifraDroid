package com.example.ui.preparation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.domain.document.MusicalDocument
import com.example.ui.components.BadgeColor
import com.example.ui.components.DashboardBadge
import com.example.ui.components.DashboardHeader
import com.example.ui.theme.AppSpacing

@Composable
fun PreparationHeader(
    document: MusicalDocument,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(AppSpacing.SM)) {
        DashboardHeader(
            title = document.metadata.title,
            subtitle = document.metadata.artist,
            metric = "${document.metadata.bpm} BPM"
        )
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.SM)) {
            DashboardBadge(text = document.metadata.category.name, badgeColor = BadgeColor.SURFACE_VARIANT, isPill = true)
            DashboardBadge(text = "Tom: ${document.metadata.key}", badgeColor = BadgeColor.PRIMARY, isPill = true)
            if (document.metadata.capo > 0) {
                DashboardBadge(text = "Capo: ${document.metadata.capo}", badgeColor = BadgeColor.AMBER, isPill = true)
            }
        }
    }
}
