package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.ui.theme.StageThemeStyle
import com.example.ui.theme.StageThemeType
import com.example.ui.theme.StageThemePalette

@Composable
fun ThemePreviewCanvas(
    themeStyle: StageThemeStyle,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(themeStyle.backgroundColor)
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Mini Dashboard
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(32.dp)
                    .background(
                        themeStyle.surfaceColor.copy(alpha = themeStyle.glassOpacity),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(8.dp)
                            .background(themeStyle.accentColor, RoundedCornerShape(4.dp))
                    )
                    Box(
                        modifier = Modifier
                            .width(30.dp)
                            .height(14.dp)
                            .background(themeStyle.badgeColor, RoundedCornerShape(8.dp))
                    )
                }
            }

            // Mini Conductor
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(themeStyle.surfaceColor, RoundedCornerShape(2.dp))
                )
                Box(
                    modifier = Modifier
                        .weight(2f)
                        .fillMaxHeight()
                        .background(themeStyle.accentColor, RoundedCornerShape(2.dp))
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(themeStyle.badgeColor, RoundedCornerShape(2.dp))
                )
            }

            // Progress
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(themeStyle.badgeColor, RoundedCornerShape(2.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .fillMaxHeight()
                        .background(themeStyle.progressColor, RoundedCornerShape(2.dp))
                )
            }
        }
    }
}
