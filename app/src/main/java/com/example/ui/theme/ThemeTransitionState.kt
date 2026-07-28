package com.example.ui.theme

data class ThemeTransitionState(
    val isTransitioning: Boolean,
    val previousTheme: StageThemeType?,
    val currentTheme: StageThemeType,
    val progress: Float
)
