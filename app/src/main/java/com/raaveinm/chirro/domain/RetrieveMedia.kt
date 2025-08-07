package com.raaveinm.chirro.domain

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.net.Uri
import android.provider.MediaStore
import com.raaveinm.chirro.data.TrackInfo

class RetrieveMedia(private val contentResolver: ContentResolver) {
    var mediaList: List<TrackInfo> = emptyList()
    val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    val resolver = this.contentResolver

    @SuppressLint("Recycle")
    fun retrieveMedia(): List<TrackInfo> {
        val cursor = resolver.query(
            uri,
            null,
            null,
            null,
            null
        )
        if (cursor?.moveToFirst() ?: return emptyList()){
            do {
                val title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
                val artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
                val album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))
                val duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                val trackUri = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                mediaList += TrackInfo(
                    title = title,
                    artist = artist,
                    uri = trackUri,
                    album = album,
                    duration = duration,
                )
            } while (cursor.moveToNext())
            cursor.close()
            return mediaList
        } else { return emptyList() }
    }

}