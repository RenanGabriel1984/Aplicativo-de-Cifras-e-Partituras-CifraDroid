package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.AppColors
import com.example.ui.theme.AppShapes
import com.example.ui.theme.AppSpacing
import com.example.ui.theme.AppTypography
import com.example.util.ScoreMarkerType

@Composable
fun ConductorMarker(
    type: ScoreMarkerType,
    text: String,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (type) {
        ScoreMarkerType.SEGNO, ScoreMarkerType.DAL_SEGNO -> AppColors.Primary
        ScoreMarkerType.CODA, ScoreMarkerType.TO_CODA -> AppColors.Error
        ScoreMarkerType.FINE, ScoreMarkerType.AL_FINE -> AppColors.Amber
        ScoreMarkerType.DA_CAPO -> AppColors.Secondary
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = when (type) {
        ScoreMarkerType.SEGNO, ScoreMarkerType.DAL_SEGNO -> Color.White
        ScoreMarkerType.CODA, ScoreMarkerType.TO_CODA -> Color.White
        ScoreMarkerType.FINE, ScoreMarkerType.AL_FINE -> Color.Black
        ScoreMarkerType.DA_CAPO -> Color.White
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = modifier
            .clip(AppShapes.Small)
            .background(backgroundColor)
            .padding(horizontal = AppSpacing.SM, vertical = AppSpacing.XS),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = AppTypography.SectionLabel,
            color = contentColor
        )
    }
}
