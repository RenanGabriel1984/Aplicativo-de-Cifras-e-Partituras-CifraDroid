package com.example.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

val LocalStageTheme = staticCompositionLocalOf { StageThemePalette.Classic }

@Composable
fun StageThemeRenderer(
    themeType: StageThemeType? = null,
    ambientContext: AmbientContext? = null,
    content: @Composable () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefsManager = androidx.compose.runtime.remember { com.example.util.PreferencesManager(context) }
    
    var resolvedThemeType by androidx.compose.runtime.remember { 
        androidx.compose.runtime.mutableStateOf(themeType ?: prefsManager.getStageTheme()) 
    }

    androidx.compose.runtime.LaunchedEffect(themeType) {
        resolvedThemeType = themeType ?: prefsManager.getStageTheme()
    }

    val engineState = rememberThemeTransitionEngine(resolvedThemeType)

    val targetStyle = StageThemePalette.getStyle(engineState.currentTheme)

    val animationSpecColor = if (engineState.currentTheme == StageThemeType.OLED) snap<androidx.compose.ui.graphics.Color>() else tween(600)
    val animationSpecDp = if (engineState.currentTheme == StageThemeType.OLED) snap<androidx.compose.ui.unit.Dp>() else tween(600)

    val backgroundColor by animateColorAsState(targetValue = targetStyle.backgroundColor, animationSpec = animationSpecColor, label = "bg")
    val surfaceColor by animateColorAsState(targetValue = targetStyle.surfaceColor, animationSpec = animationSpecColor, label = "surface")
    val accentColor by animateColorAsState(targetValue = targetStyle.accentColor, animationSpec = animationSpecColor, label = "accent")
    val badgeColor by animateColorAsState(targetValue = targetStyle.badgeColor, animationSpec = animationSpecColor, label = "badge")
    val progressColor by animateColorAsState(targetValue = targetStyle.progressColor, animationSpec = animationSpecColor, label = "progress")
    val cueColor by animateColorAsState(targetValue = targetStyle.cueColor, animationSpec = animationSpecColor, label = "cue")
    
    val glassOpacity by animateFloatAsState(targetValue = targetStyle.glassOpacity, animationSpec = if (engineState.currentTheme == StageThemeType.OLED) snap() else tween(600), label = "glass")
    val blur by animateDpAsState(targetValue = targetStyle.blur, animationSpec = animationSpecDp, label = "blur")
    val shadowLevel by animateDpAsState(targetValue = targetStyle.shadowLevel, animationSpec = animationSpecDp, label = "shadow")

    val animatedStyle = StageThemeStyle(
        backgroundColor = backgroundColor,
        surfaceColor = surfaceColor,
        glassOpacity = glassOpacity,
        blur = blur,
        accentColor = accentColor,
        badgeColor = badgeColor,
        progressColor = progressColor,
        cueColor = cueColor,
        shadowLevel = shadowLevel,
        motionProfile = targetStyle.motionProfile
    )

    CompositionLocalProvider(LocalStageTheme provides animatedStyle) {
        ThemeTransitionLayer(
            themeState = engineState,
            ambientContext = ambientContext,
            content = content
        )
    }
}
