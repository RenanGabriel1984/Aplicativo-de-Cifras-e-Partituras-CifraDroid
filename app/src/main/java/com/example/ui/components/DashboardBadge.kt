package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import com.example.ui.theme.AppColors
import com.example.ui.theme.AppShapes
import com.example.ui.theme.AppSpacing
import com.example.ui.theme.AppTypography
import com.example.ui.theme.LocalStageTheme

enum class BadgeColor {
    PRIMARY, AMBER, ERROR, SURFACE_VARIANT
}

@Composable
fun DashboardBadge(
    text: String,
    badgeColor: BadgeColor = BadgeColor.PRIMARY,
    isPill: Boolean = true,
    modifier: Modifier = Modifier
) {
    val theme = LocalStageTheme.current

    val targetBackgroundColor = when (badgeColor) {
        BadgeColor.PRIMARY -> theme.accentColor
        BadgeColor.AMBER -> AppColors.Amber
        BadgeColor.ERROR -> AppColors.Error
        BadgeColor.SURFACE_VARIANT -> theme.badgeColor
    }
    
    val targetContentColor = when (badgeColor) {
        BadgeColor.PRIMARY -> Color.White
        BadgeColor.AMBER -> Color.Black
        BadgeColor.ERROR -> Color.White
        BadgeColor.SURFACE_VARIANT -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val backgroundColor by animateColorAsState(targetValue = targetBackgroundColor, label = "badge_bg")
    val contentColor by animateColorAsState(targetValue = targetContentColor, label = "badge_content")
    
    val shape: Shape = if (isPill) AppShapes.Pill else AppShapes.Small

    Box(
        modifier = modifier
            .background(backgroundColor, shape)
            .padding(horizontal = AppSpacing.MD, vertical = AppSpacing.XS),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = AppTypography.SectionLabel,
            color = contentColor
        )
    }
}
