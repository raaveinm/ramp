package com.raaveinm.chirro.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity (tableName = "trackList")
data class TrackInfo (
    @PrimaryKey (autoGenerate = true) val id: Int = 0,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val uri: String,
    val artUri: String,
    val included: Boolean
)