package com.example.ui.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.ui.screens.LiturgicalTheme
import com.example.util.DocumentContent

@Stable
class ReaderUiState(
    showHudInitial: Boolean = false,
    isStageModeInitial: Boolean = false,
    isChoirModeInitial: Boolean = false,
    autoScrollSpeedInitial: Float = 0f,
    liturgicalThemeInitial: LiturgicalTheme = LiturgicalTheme.CLASSIC
) {
    var showHud by mutableStateOf(showHudInitial)
    var isStageMode by mutableStateOf(isStageModeInitial)
    var isChoirMode by mutableStateOf(isChoirModeInitial)
    var autoScrollSpeed by mutableFloatStateOf(autoScrollSpeedInitial)
    var liturgicalTheme by mutableStateOf(liturgicalThemeInitial)
    var showMusicList by mutableStateOf(false)
    var selectedSongChartId by mutableStateOf<Int?>(null)
    
    var isLoading by mutableStateOf(false)
    var localDocument by mutableStateOf<DocumentContent?>(null)

    companion object {
        val Saver: Saver<ReaderUiState, *> = mapSaver(
            save = {
                mapOf(
                    "showHud" to it.showHud,
                    "isStageMode" to it.isStageMode,
                    "isChoirMode" to it.isChoirMode,
                    "autoScrollSpeed" to it.autoScrollSpeed,
                    "liturgicalTheme" to it.liturgicalTheme.name,
                    "showMusicList" to it.showMusicList
                )
            },
            restore = {
                ReaderUiState(
                    showHudInitial = it["showHud"] as Boolean,
                    isStageModeInitial = it["isStageMode"] as Boolean,
                    isChoirModeInitial = it["isChoirMode"] as Boolean,
                    autoScrollSpeedInitial = it["autoScrollSpeed"] as Float,
                    liturgicalThemeInitial = LiturgicalTheme.valueOf(it["liturgicalTheme"] as String)
                ).apply {
                    showMusicList = it["showMusicList"] as? Boolean ?: false
                }
            }
        )
    }
}

@Composable
fun rememberReaderUiState(): ReaderUiState {
    return rememberSaveable(saver = ReaderUiState.Saver) {
        ReaderUiState()
    }
}
