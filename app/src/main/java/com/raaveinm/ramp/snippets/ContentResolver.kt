package com.raaveinm.ramp.snippets

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.core.net.toUri

class ContentResolver(
    private val context: Context
) {
    private var mCursor: Cursor? = null

    private val projection: Array<String> = arrayOf(
        MediaStore.Audio.AudioColumns._ID,
        MediaStore.Audio.AudioColumns.DISPLAY_NAME,
        MediaStore.Audio.AudioColumns.ARTIST,
        MediaStore.Audio.AudioColumns.DATA,
        MediaStore.Audio.AudioColumns.DURATION,
        MediaStore.Audio.AudioColumns.TITLE,
        MediaStore.Audio.AudioColumns.ALBUM_ID,
        MediaStore.Audio.AudioColumns.ALBUM,
    )

    private var selectionClause: String? = "${MediaStore.Audio.AudioColumns.IS_MUSIC} = ?"
    private var selectionArg = arrayOf("1")
    private val sortOrder = "${MediaStore.Audio.AudioColumns.DISPLAY_NAME} ASC"

    @WorkerThread
    fun getAudioData(): List<AudioItem> {
        return getCursorData()
    }

    private fun getCursorData(): List<AudioItem> {
        val audioList = mutableListOf<AudioItem>()
        mCursor = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selectionClause,
            selectionArg,
            sortOrder
        )

        mCursor?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns._ID)
            val displayNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DISPLAY_NAME)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ARTIST)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DATA)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DURATION)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.TITLE)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ALBUM_ID)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ALBUM)

            if (cursor.count == 0) {
                Log.i("ContentResolver", "No audio files found.")
            } else {
                Log.i("ContentResolver", "Found ${cursor.count} audio files. Processing...")
                while (cursor.moveToNext()) {
                    try {
                        val id = cursor.getLong(idColumn)
                        val displayName = cursor.getString(displayNameColumn) ?: "Unknown File"
                        val artist = cursor.getString(artistColumn)?.takeIf {
                            it.isNotBlank() && it != "<unknown>" } ?: "Unknown Artist"
                        val data = cursor.getString(dataColumn)
                        val duration = cursor.getLong(durationColumn)
                        val title = cursor.getString(titleColumn)?.takeIf {
                            it.isNotBlank() } ?: displayName
                        val albumId = cursor.getLong(albumIdColumn)
                        val album = cursor.getString(albumColumn)?: "Unknown Album"
                        val albumArtUri: Uri? = ContentUris.withAppendedId(
                            "content://media/external/audio/albumart".toUri(), albumId
                        )

                        val uri = ContentUris.withAppendedId(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            id
                        )

                        val audioItem = AudioItem(
                            mediaId = id.toString(),
                            uri = uri,
                            title = title,
                            displayName = displayName,
                            artist = artist,
                            durationMs = duration,
                            //dataPath = data,
                            //albumId = albumId,
                            album = album,
                            albumArtUri = albumArtUri
                        )
                        audioList.add(audioItem)

                    } catch (e: Exception) {
                        val errorRowId = try { cursor.getLong(idColumn) } catch (_: Exception) { -1L }
                        Log.e("ContentResolver", "Error processing row with ID: $errorRowId", e)
                    }
                }
                Log.i("ContentResolver", "Finished processing. ${audioList.size} items added.")
            }
        } ?: run {
            Log.e("ContentResolver", "MediaStore query returned null cursor.")
        }
        return audioList
    }
}