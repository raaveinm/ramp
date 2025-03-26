package com.raaveinm.ramp.snippets

import android.graphics.Bitmap
import android.net.Uri
import com.raaveinm.ramp.R

class TrackInfo {
    private val covers = listOf(
        R.drawable.defaulti,
        R.drawable.defaultii,
        R.drawable.defaultiii,
        R.drawable.defaultiv,
    )

    fun songCover(uri: String? = null): Int {
        if (uri == "default") {
            return covers.random()
        }
        return covers.random()
    }

    fun songName(): String{
        return "Song name"
    }
}

data class AudioItem(
    val mediaId: String,
    val uri: android.net.Uri,
    val title: String,
    val displayName: String,
    val artist: String,
    val album: String?,
    val albumArtUri: android.net.Uri?,
    val durationMs: Long
)