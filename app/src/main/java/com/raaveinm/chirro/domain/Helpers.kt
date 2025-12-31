package com.raaveinm.chirro.domain

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
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

fun checkPermission(activity: Activity, launcher: ActivityResultLauncher<String>) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.READ_MEDIA_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            launcher.launch(Manifest.permission.READ_MEDIA_AUDIO)
        }
    } else {
        if (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            launcher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }
}
