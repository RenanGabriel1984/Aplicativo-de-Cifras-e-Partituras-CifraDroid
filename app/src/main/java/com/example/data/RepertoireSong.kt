package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "repertoire_songs",
    foreignKeys = [
        ForeignKey(
            entity = Repertoire::class,
            parentColumns = ["id"],
            childColumns = ["repertoireId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = RepertoireCategory::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SongChart::class,
            parentColumns = ["id"],
            childColumns = ["songChartId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("repertoireId"), Index("categoryId"), Index("songChartId")]
)
data class RepertoireSong(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val repertoireId: Int,
    val categoryId: Int?,
    val songChartId: Int,
    val position: Int,
    val customKey: String? = null
)
