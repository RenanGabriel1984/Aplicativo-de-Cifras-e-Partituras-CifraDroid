package com.example.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

object AppGlass {
    fun glassBackground(color: Color, shape: Shape, alpha: Float = 0.82f): Modifier {
        return Modifier
            .clip(shape)
            .background(color.copy(alpha = alpha))
    }

    fun glassCard(color: Color, shape: Shape, alpha: Float = 0.82f): Modifier {
        return Modifier
            .clip(shape)
            .background(color.copy(alpha = alpha))
            .border(1.dp, color.copy(alpha = 0.2f), shape)
    }

    fun glassPerformance(color: Color, shape: Shape, alpha: Float = 0.82f): Modifier {
        return Modifier
            .clip(shape)
            .background(color.copy(alpha = alpha))
            .border(2.dp, color.copy(alpha = 0.5f), shape)
    }
}
