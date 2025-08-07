package com.raaveinm.chirro.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TrackInfo(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo val title: String,
    @ColumnInfo val artist: String,
    @ColumnInfo val album: String,
    @ColumnInfo val duration: Long,
    @ColumnInfo val uri: String,
    @ColumnInfo val cover: String = "none",
    @ColumnInfo val isFavourite: Boolean = false
)
