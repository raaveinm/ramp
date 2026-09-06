package com.raaveinm.chirro.domain

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.raaveinm.chirro.data.values.TrackInfo

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

fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}