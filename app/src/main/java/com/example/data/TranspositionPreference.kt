package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transposition_preferences")
data class TranspositionPreference(
    @PrimaryKey val manuscriptId: Int,
    val preferredKey: String
)
