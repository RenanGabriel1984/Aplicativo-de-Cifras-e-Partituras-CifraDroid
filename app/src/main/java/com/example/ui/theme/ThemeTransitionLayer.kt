package com.example.ui.theme

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

@Composable
fun ThemeTransitionLayer(
    themeState: ThemeTransitionState,
    modifier: Modifier = Modifier,
    ambientContext: AmbientContext? = null,
    content: @Composable () -> Unit
) {
    AnimatedContent(
        targetState = themeState.currentTheme,
        transitionSpec = {
            val duration = 600
            
            val enter = when (targetState) {
                StageThemeType.CLASSIC -> fadeIn(tween(duration))
                StageThemeType.STUDIO -> fadeIn(tween(duration))
                StageThemeType.PERFORMANCE -> fadeIn(tween(duration)) + scaleIn(tween(duration), initialScale = 0.95f)
                StageThemeType.CONDUCTOR -> fadeIn(tween(duration)) + slideInVertically(tween(duration)) { -it / 20 }
                StageThemeType.LITURGICAL -> fadeIn(tween(duration))
                StageThemeType.OLED -> fadeIn(snap())
            }
            
            val exit = when (initialState) {
                StageThemeType.CLASSIC -> fadeOut(tween(duration))
                StageThemeType.STUDIO -> fadeOut(tween(duration))
                StageThemeType.PERFORMANCE -> fadeOut(tween(duration)) + scaleOut(tween(duration), targetScale = 1.05f)
                StageThemeType.CONDUCTOR -> fadeOut(tween(duration)) + slideOutVertically(tween(duration)) { it / 20 }
                StageThemeType.LITURGICAL -> fadeOut(tween(duration))
                StageThemeType.OLED -> fadeOut(snap())
            }
            
            enter togetherWith exit
        },
        label = "ThemeTransitionLayer",
        modifier = modifier
    ) { targetTheme ->
        Box(
            modifier = Modifier.graphicsLayer {
                if (themeState.isTransitioning) {
                    val progress = themeState.progress
                    when (targetTheme) {
                        StageThemeType.PERFORMANCE -> {
                            val scale = 1f + (0.02f * (1f - progress))
                            scaleX = scale
                            scaleY = scale
                        }
                        StageThemeType.CONDUCTOR -> {
                            translationY = 50f * (1f - progress)
                        }
                        StageThemeType.LITURGICAL -> {
                            alpha = 0.5f + (0.5f * progress)
                        }
                        StageThemeType.STUDIO -> {
                            alpha = progress
                        }
                        StageThemeType.CLASSIC -> {
                            alpha = progress
                        }
                        StageThemeType.OLED -> {
                            alpha = 1f
                        }
                    }
                }
            }.then(
                if (themeState.isTransitioning && targetTheme == StageThemeType.STUDIO) {
                    Modifier.blur((10f * (1f - themeState.progress)).dp)
                } else {
                    Modifier
                }
            )
        ) {
            content()
        }
    }
}
