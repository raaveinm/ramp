package com.raaveinm.chirro.data

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.util.Log
import com.raaveinm.chirro.data.room.TrackInfo

class MediaResolve(private val contentResolver: ContentResolver) {
    val resolver: ContentResolver = this.contentResolver
    val uri: Uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

    fun resolve(): List<TrackInfo> {
        val cursor: Cursor? = resolver.query(uri, null, null, null, null)
        var trackList: MutableList<TrackInfo> = mutableListOf()
        when {
            cursor == null -> Log.e("chirroMediaResolve", "cursor is null")
            !cursor.moveToFirst() -> Log.e("chirroMediaResolve", "no media found")

            else -> {
                while (cursor.moveToNext()) {
                    val title: String =
                        cursor.getString(cursor.getColumnIndexOrThrow(android.provider.MediaStore.Audio.Media.TITLE))
                    val artist: String =
                        cursor.getString(cursor.getColumnIndexOrThrow(android.provider.MediaStore.Audio.Media.ARTIST))
                    val album: String =
                        cursor.getString(cursor.getColumnIndexOrThrow(android.provider.MediaStore.Audio.Media.ALBUM))
                    val duration: Long =
                        cursor.getLong(cursor.getColumnIndexOrThrow(android.provider.MediaStore.Audio.Media.DURATION))
                    val uri: String =
                        cursor.getString(cursor.getColumnIndexOrThrow(android.provider.MediaStore.Audio.Media.DATA))
                    //val artUri: String =
                      //  cursor.getString(cursor.getColumnIndexOrThrow(android.provider.MediaStore.Audio.Media.ALBUM_ART))

                    trackList.add(
                        TrackInfo(
                            title = title,
                            artist = artist,
                            album = album,
                            duration = duration,
                            uri = uri,
                            isFavorite = false,
                            included = true,
                            artUri = "default" // temporary plug
                        )
                    )
                }
            }
        }
        cursor?.close()
        Log.d("chirroMediaResolve", "resolve: ${trackList.count()}")
        return trackList
    }
}