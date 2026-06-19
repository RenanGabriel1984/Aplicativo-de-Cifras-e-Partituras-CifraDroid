package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "repertoire_categories",
    foreignKeys = [
        ForeignKey(
            entity = Repertoire::class,
            parentColumns = ["id"],
            childColumns = ["repertoireId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    // Remove individual indices parameter in older Room if it complains, but here it's fine
    indices = [Index("repertoireId")]
)
data class RepertoireCategory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val repertoireId: Int,
    val name: String,
    val position: Int
)
