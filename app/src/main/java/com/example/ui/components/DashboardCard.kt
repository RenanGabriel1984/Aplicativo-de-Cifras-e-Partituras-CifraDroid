package com.example.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.ui.theme.AppGlass
import com.example.ui.theme.AppShapes
import com.example.ui.theme.AppSpacing
import com.example.ui.theme.LocalStageTheme

@Composable
fun DashboardCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val theme = LocalStageTheme.current
    
    Box(
        modifier = modifier
            .then(
                AppGlass.glassCard(
                    color = theme.surfaceColor,
                    shape = AppShapes.Large,
                    alpha = theme.glassOpacity
                )
            )
            .animateContentSize(
                animationSpec = androidx.compose.animation.core.spring(
                    dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
                    stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
                )
            )
            .padding(AppSpacing.MD)
    ) {
        content()
    }
}
