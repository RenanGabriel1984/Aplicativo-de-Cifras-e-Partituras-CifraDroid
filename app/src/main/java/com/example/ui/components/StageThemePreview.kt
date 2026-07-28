package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AppSpacing
import com.example.ui.theme.AppTypography
import com.example.ui.theme.StageThemeType

@Composable
fun StageThemePreview(
    selectedTheme: StageThemeType,
    onThemeSelected: (StageThemeType) -> Unit,
    modifier: Modifier = Modifier
) {
    val themeOptions = listOf(
        ThemeOption(StageThemeType.CLASSIC, "Classic", "Partitura elegante"),
        ThemeOption(StageThemeType.STUDIO, "Studio", "Ambiente moderno"),
        ThemeOption(StageThemeType.PERFORMANCE, "Performance", "Palco ao vivo"),
        ThemeOption(StageThemeType.CONDUCTOR, "Conductor", "Orquestra"),
        ThemeOption(StageThemeType.LITURGICAL, "Liturgical", "Celebrações"),
        ThemeOption(StageThemeType.OLED, "OLED", "Baixo consumo")
    )

    Column(modifier = modifier) {
        Text(
            text = "Stage Themes",
            style = AppTypography.TitleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = AppSpacing.MD)
        )
        
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.MD),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.MD),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(themeOptions) { option ->
                StageThemeCard(
                    themeType = option.type,
                    title = option.title,
                    description = option.description,
                    isSelected = selectedTheme == option.type,
                    onClick = { onThemeSelected(option.type) }
                )
            }
        }
    }
}

private data class ThemeOption(
    val type: StageThemeType,
    val title: String,
    val description: String
)
