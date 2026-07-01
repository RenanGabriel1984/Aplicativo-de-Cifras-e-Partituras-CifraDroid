package com.example.util

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("digital_manuscript_prefs", Context.MODE_PRIVATE)

    private val HISTORY_SET_KEY = "history_song_set"

    fun recordSongOpened(songChartId: Int) {
        val historySet = prefs.getStringSet(HISTORY_SET_KEY, emptySet())?.toMutableSet() ?: mutableSetOf()
        val idStr = songChartId.toString()
        historySet.add(idStr)
        
        val currentCount = prefs.getInt("song_play_count_$idStr", 0)
        
        prefs.edit()
            .putStringSet(HISTORY_SET_KEY, historySet)
            .putInt("song_play_count_$idStr", currentCount + 1)
            .putLong("song_last_opened_$idStr", System.currentTimeMillis())
            .apply()
    }

    fun getMostPlayedSongs(limit: Int = 20): List<Pair<Int, Int>> {
        val historySet = prefs.getStringSet(HISTORY_SET_KEY, emptySet()) ?: emptySet()
        return historySet.mapNotNull {
            val id = it.toIntOrNull() ?: return@mapNotNull null
            val count = prefs.getInt("song_play_count_$it", 0)
            id to count
        }.sortedByDescending { it.second }.take(limit)
    }

    fun getRecentSongs(limit: Int = 20): List<Pair<Int, Long>> {
        val historySet = prefs.getStringSet(HISTORY_SET_KEY, emptySet()) ?: emptySet()
        return historySet.mapNotNull {
            val id = it.toIntOrNull() ?: return@mapNotNull null
            val timestamp = prefs.getLong("song_last_opened_$it", 0L)
            if (timestamp > 0L) id to timestamp else null
        }.sortedByDescending { it.second }.take(limit)
    }

    fun getPlayedPerformanceSongs(repertoireId: Int): Set<Int> {
        val set = prefs.getStringSet("performance_$repertoireId", emptySet()) ?: emptySet()
        return set.mapNotNull { it.toIntOrNull() }.toSet()
    }
    
    fun setPlayedPerformanceSongs(repertoireId: Int, played: Set<Int>) {
        val set = played.map { it.toString() }.toSet()
        prefs.edit().putStringSet("performance_$repertoireId", set).apply()
    }
    
    fun addPlayedPerformanceSong(repertoireId: Int, songId: Int) {
        val played = getPlayedPerformanceSongs(repertoireId).toMutableSet()
        played.add(songId)
        setPlayedPerformanceSongs(repertoireId, played)
    }

    fun clearPlayedPerformanceSongs(repertoireId: Int) {
        prefs.edit().remove("performance_$repertoireId").apply()
    }

    fun getPerformanceStartTime(repertoireId: Int): Long {
        return prefs.getLong("performance_time_$repertoireId", 0L)
    }

    fun setPerformanceStartTime(repertoireId: Int, timeMs: Long) {
        prefs.edit().putLong("performance_time_$repertoireId", timeMs).apply()
    }

    fun getPerformanceElapsedTime(repertoireId: Int): Long {
        return prefs.getLong("performance_elapsed_$repertoireId", 0L)
    }

    fun setPerformanceElapsedTime(repertoireId: Int, elapsedMs: Long) {
        prefs.edit().putLong("performance_elapsed_$repertoireId", elapsedMs).apply()
    }

    fun clearPerformanceTime(repertoireId: Int) {
        prefs.edit().remove("performance_time_$repertoireId").remove("performance_elapsed_$repertoireId").apply()
    }

    fun getPerformanceNote(songChartId: Int): String {
        return prefs.getString("performance_notes_$songChartId", "") ?: ""
    }

    fun setPerformanceNote(songChartId: Int, note: String) {
        prefs.edit().putString("performance_notes_$songChartId", note).apply()
    }

    fun isNextSongAlertEnabled(): Boolean {
        return prefs.getBoolean("show_next_song_alert_enabled", false)
    }

    fun setNextSongAlertEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("show_next_song_alert_enabled", enabled).apply()
    }

    fun isAutoConfirmMusicalInstructionsEnabled(): Boolean {
        return prefs.getBoolean("auto_confirm_musical_instructions", false)
    }

    fun setAutoConfirmMusicalInstructionsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("auto_confirm_musical_instructions", enabled).apply()
    }

    fun isSilentModeEnabled(): Boolean {
        return prefs.getBoolean("silent_mode_enabled", false)
    }

    fun setSilentModeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("silent_mode_enabled", enabled).apply()
    }

    fun isExtremeFocusModeEnabled(): Boolean {
        return prefs.getBoolean("extreme_focus_mode_enabled", false)
    }

    fun setExtremeFocusModeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("extreme_focus_mode_enabled", enabled).apply()
    }

    fun getFavoriteSongs(): Set<String> {
        return prefs.getStringSet("favorite_songs", emptySet()) ?: emptySet()
    }

    fun toggleFavoriteSong(songChartId: Int) {
        val favs = getFavoriteSongs().toMutableSet()
        val idStr = songChartId.toString()
        if (favs.contains(idStr)) {
            favs.remove(idStr)
        } else {
            favs.add(idStr)
        }
        prefs.edit().putStringSet("favorite_songs", favs).apply()
    }

    fun isFavoriteSong(songChartId: Int): Boolean {
        return getFavoriteSongs().contains(songChartId.toString())
    }

    fun getFavorites(): Set<String> {
        return prefs.getStringSet("favorites", emptySet()) ?: emptySet()
    }

    fun toggleFavorite(manuscriptId: Int) {
        val favs = getFavorites().toMutableSet()
        val idStr = manuscriptId.toString()
        if (favs.contains(idStr)) {
            favs.remove(idStr)
        } else {
            favs.add(idStr)
        }
        prefs.edit().putStringSet("favorites", favs).apply()
    }

    fun isFavorite(manuscriptId: Int): Boolean {
        return getFavorites().contains(manuscriptId.toString())
    }

    fun getRecent(): List<Int> {
        val recentStr = prefs.getString("recent_manuscripts", "") ?: ""
        if (recentStr.isBlank()) return emptyList()
        return recentStr.split(",").mapNotNull { it.toIntOrNull() }
    }

    fun addRecent(manuscriptId: Int) {
        val recents = getRecent().toMutableList()
        recents.remove(manuscriptId) // Remove se já existir (para mover pro topo)
        recents.add(0, manuscriptId) // Adiciona no topo
        if (recents.size > 10) {
            recents.removeAt(recents.size - 1)
        }
        prefs.edit().putString("recent_manuscripts", recents.joinToString(",")).apply()
    }

    fun getThemeMode(): Int {
        // 0=Claro, 1=Escuro, 2=Vermelho
        return prefs.getInt("theme_mode", if (prefs.getBoolean("dark_mode", true)) 1 else 0)
    }

    fun setThemeMode(mode: Int) {
        prefs.edit().putInt("theme_mode", mode).apply()
        // Backward compatibility
        prefs.edit().putBoolean("dark_mode", mode == 1 || mode == 2).apply()
    }

    fun getOrientationMode(): Int {
        // 0=Livre, 1=Retrato, 2=Paisagem
        return prefs.getInt("orientation_mode", 0)
    }

    fun setOrientationMode(mode: Int) {
        prefs.edit().putInt("orientation_mode", mode).apply()
    }

    fun isContinuousMode(): Boolean {
        return prefs.getBoolean("continuous_mode", false)
    }

    fun setContinuousMode(enabled: Boolean) {
        prefs.edit().putBoolean("continuous_mode", enabled).apply()
    }

    fun isSongPlayed(repertoireId: Int, songId: Int): Boolean {
        return prefs.getBoolean("played_${repertoireId}_$songId", false)
    }

    fun setSongPlayed(repertoireId: Int, songId: Int, played: Boolean) {
        prefs.edit().putBoolean("played_${repertoireId}_$songId", played).apply()
    }

    fun isCountdownEnabled(): Boolean {
        return prefs.getBoolean("countdown_enabled", true)
    }

    fun setCountdownEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("countdown_enabled", enabled).apply()
    }

    fun getChordFontSize(): Float {
        return prefs.getFloat("chord_font_size", 16f)
    }

    fun setChordFontSize(size: Float) {
        prefs.edit().putFloat("chord_font_size", size).apply()
    }

    fun getReadingProfile(): ReadingProfile {
        val name = prefs.getString("reading_profile", ReadingProfile.DEFAULT.name) ?: ReadingProfile.DEFAULT.name
        return try {
            ReadingProfile.valueOf(name)
        } catch (e: Exception) {
            ReadingProfile.DEFAULT
        }
    }

    fun setReadingProfile(profile: ReadingProfile) {
        prefs.edit().putString("reading_profile", profile.name).apply()
    }
}
