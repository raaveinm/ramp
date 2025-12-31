package com.raaveinm.chirro.data.database

data class TrackInfo(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val uri: String,
    val cover: String = "none",
    val isFavourite: Boolean = false
) {
    companion object {
        val EMPTY = TrackInfo(
            id = -1,
            title = "",
            artist = "",
            album = "",
            duration = 0L,
            uri = ""
        )
    }
}