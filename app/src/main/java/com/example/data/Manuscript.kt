package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "manuscripts")
data class Manuscript(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val composer: String,
    val category: String,
    val coverUrl: String,
    val isFavorite: Boolean = false,
    val lastUsedTimestamp: Long = 0L,
    val keySignature: String = "",
    val era: String = ""
)
