package com.raaveinm.chirro.domain

import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.raaveinm.chirro.data.database.TrackInfo


fun TrackInfo.toMediaItem(): MediaItem {
    return MediaItem.Builder()
        .setMediaId(this.id.toString())
        .setUri(this.uri)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(this.title)
                .setArtist(this.artist)
                .setAlbumTitle(this.album)
                .setArtworkUri(this.cover.toUri())
                .setIsPlayable(true)
                .setIsBrowsable(false)
                .build()
        )
        .build()
}
