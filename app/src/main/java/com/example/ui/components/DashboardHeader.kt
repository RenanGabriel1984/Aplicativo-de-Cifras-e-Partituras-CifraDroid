package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.ui.theme.AppTypography

@Composable
fun DashboardHeader(
    title: String,
    subtitle: String,
    metric: String? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = AppTypography.DashboardTitle
            )
            Text(
                text = subtitle,
                style = AppTypography.DashboardSubtitle
            )
        }
        if (metric != null) {
            Text(
                text = metric,
                style = AppTypography.PerformanceMetric
            )
        }
    }
}
