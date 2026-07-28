package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

enum class StageThemeType {
    CLASSIC, STUDIO, PERFORMANCE, CONDUCTOR, LITURGICAL, OLED
}

object StageThemePalette {
    val Classic = StageThemeStyle(
        backgroundColor = Color(0xFFFDFBF7),
        surfaceColor = Color(0xFFF5F3ED),
        glassOpacity = 0.85f,
        blur = 12.dp,
        accentColor = Color(0xFF5A4A42),
        badgeColor = Color(0xFFE8E2D9),
        progressColor = Color(0xFF5A4A42),
        cueColor = Color(0xFFB71C1C),
        shadowLevel = 4.dp,
        motionProfile = AppMotion.Smooth
    )

    val Studio = StageThemeStyle(
        backgroundColor = Color(0xFF1E1E1E),
        surfaceColor = Color(0xFF2C2C2C),
        glassOpacity = 0.7f,
        blur = 16.dp,
        accentColor = Color(0xFF2196F3),
        badgeColor = Color(0xFF3A3A3A),
        progressColor = Color(0xFF2196F3),
        cueColor = Color(0xFFE53935),
        shadowLevel = 8.dp,
        motionProfile = AppMotion.Smooth
    )

    val Performance = StageThemeStyle(
        backgroundColor = Color(0xFF000000),
        surfaceColor = Color(0xFF121212),
        glassOpacity = 0.6f,
        blur = 20.dp,
        accentColor = Color(0xFF00E5FF),
        badgeColor = Color(0xFF1E1E1E),
        progressColor = Color(0xFFFFB300),
        cueColor = Color(0xFFFFB300),
        shadowLevel = 12.dp,
        motionProfile = AppMotion.Smooth
    )

    val Conductor = StageThemeStyle(
        backgroundColor = Color(0xFF2E0C15),
        surfaceColor = Color(0xFF3F131D),
        glassOpacity = 0.75f,
        blur = 16.dp,
        accentColor = Color(0xFFFFD54F),
        badgeColor = Color(0xFF5A1C28),
        progressColor = Color(0xFFFFD54F),
        cueColor = Color(0xFFFDFBF7),
        shadowLevel = 8.dp,
        motionProfile = AppMotion.Smooth
    )

    val Liturgical = StageThemeStyle(
        backgroundColor = Color(0xFF4A1423),
        surfaceColor = Color(0xFF5C1C2D),
        glassOpacity = 0.8f,
        blur = 16.dp,
        accentColor = Color(0xFFF3E5AB),
        badgeColor = Color(0xFF70263A),
        progressColor = Color(0xFFF3E5AB),
        cueColor = Color(0xFFFFFFFF),
        shadowLevel = 6.dp,
        motionProfile = AppMotion.Smooth
    )

    val Oled = StageThemeStyle(
        backgroundColor = Color(0xFF000000),
        surfaceColor = Color(0xFF0A0A0A),
        glassOpacity = 0.5f,
        blur = 24.dp,
        accentColor = Color(0xFFFFFFFF),
        badgeColor = Color(0xFF1A1A1A),
        progressColor = Color(0xFFFFFFFF),
        cueColor = Color(0xFFE53935),
        shadowLevel = 0.dp,
        motionProfile = AppMotion.Smooth
    )

    fun getStyle(type: StageThemeType): StageThemeStyle {
        return when (type) {
            StageThemeType.CLASSIC -> Classic
            StageThemeType.STUDIO -> Studio
            StageThemeType.PERFORMANCE -> Performance
            StageThemeType.CONDUCTOR -> Conductor
            StageThemeType.LITURGICAL -> Liturgical
            StageThemeType.OLED -> Oled
        }
    }
}
