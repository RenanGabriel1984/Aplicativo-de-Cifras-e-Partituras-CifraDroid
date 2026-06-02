package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "song_charts",
    foreignKeys = [
        ForeignKey(
            entity = Manuscript::class,
            parentColumns = ["id"],
            childColumns = ["manuscriptId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["manuscriptId"], unique = false)]
)
data class SongChart(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val manuscriptId: Int,
    val title: String,
    val originalKey: String,
    val content: String,
    val savedKey: String? = null,
    val sortOrder: Int = 0
)
