package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AppShapes
import com.example.ui.theme.AppSpacing
import com.example.ui.theme.AppTypography
import com.example.ui.theme.StageThemePalette
import com.example.ui.theme.StageThemeType

@Composable
fun StageThemeCard(
    themeType: StageThemeType,
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeStyle = StageThemePalette.getStyle(themeType)

    val targetBorderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val targetElevation = if (isSelected) 8.dp else 2.dp

    val borderColor by animateColorAsState(targetValue = targetBorderColor, label = "border")
    val elevation by animateDpAsState(targetValue = targetElevation, label = "elevation")

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = AppShapes.Medium,
        border = BorderStroke(2.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            ThemePreviewCanvas(
                themeStyle = themeStyle,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(AppShapes.Medium)
            )

            Column(
                modifier = Modifier.padding(AppSpacing.MD)
            ) {
                Text(
                    text = title,
                    style = AppTypography.TitleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(AppSpacing.XS))
                Text(
                    text = description,
                    style = AppTypography.BodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
