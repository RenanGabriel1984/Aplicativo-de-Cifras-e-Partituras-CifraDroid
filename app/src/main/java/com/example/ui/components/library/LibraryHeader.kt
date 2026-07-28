package com.example.ui.components.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.ui.components.DashboardHeader
import com.example.ui.theme.AppSpacing

@Composable
fun LibraryHeader(
    title: String,
    totalSongs: Int,
    totalRepertoires: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(AppSpacing.SM)) {
        DashboardHeader(
            title = title,
            subtitle = "$totalSongs músicas",
            metric = "$totalRepertoires repertórios"
        )
    }
}
