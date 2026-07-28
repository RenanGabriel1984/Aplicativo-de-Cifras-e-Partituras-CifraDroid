package com.example.ui.theme

import androidx.compose.ui.unit.dp

object VisualHierarchy {
    val opacity = mapOf(
        VisualPriority.PRIMARY to 1.0f,
        VisualPriority.SECONDARY to 0.7f,
        VisualPriority.TERTIARY to 0.4f,
        VisualPriority.BACKGROUND to 0.1f,
        VisualPriority.HIDDEN to 0.0f
    )
    val elevation = mapOf(
        VisualPriority.PRIMARY to 8.dp,
        VisualPriority.SECONDARY to 4.dp,
        VisualPriority.TERTIARY to 2.dp,
        VisualPriority.BACKGROUND to 0.dp,
        VisualPriority.HIDDEN to 0.dp
    )
    val scale = mapOf(
        VisualPriority.PRIMARY to 1.0f,
        VisualPriority.SECONDARY to 0.95f,
        VisualPriority.TERTIARY to 0.9f,
        VisualPriority.BACKGROUND to 0.85f,
        VisualPriority.HIDDEN to 0.8f
    )
}
