package com.raaveinm.chirro.data.database

data class TrackInfo(
    val id: Int,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val uri: String,
    val cover: String = "none",
    val isFavourite: Boolean = false
)