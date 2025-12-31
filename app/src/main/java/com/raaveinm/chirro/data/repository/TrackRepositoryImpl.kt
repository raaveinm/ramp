package com.raaveinm.chirro.data.repository

import android.content.ContentUris
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import com.raaveinm.chirro.data.database.TrackDao
import com.raaveinm.chirro.data.database.TrackInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class TrackRepositoryImpl(
    private val context: Context,
    private val trackDao: TrackDao
) : TrackRepository {
    override fun getAllTracks(): Flow<List<TrackInfo>> {
        val favFlow = trackDao.getFavoriteIds()
        val mediaStoreFlow = callbackFlow {
            val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean) {
                    trySend(fetchTracksFromMediaStore())
                }
            }
        context.contentResolver.registerContentObserver(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            true, observer
        )

        trySend(fetchTracksFromMediaStore())
        awaitClose { context.contentResolver.unregisterContentObserver(observer) }

    }.flowOn(Dispatchers.IO)

    return mediaStoreFlow.combine(favFlow) { localFiles, favoriteIds ->
        localFiles.map { track ->
            track.copy(isFavourite = favoriteIds.contains(track.id))
        }
    }.flowOn(Dispatchers.Default)
}

    private fun fetchTracksFromMediaStore(): List<TrackInfo> {
        val mediaList = ArrayList<TrackInfo>()

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

        try {
            val cursor = context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                "${MediaStore.Audio.Media.TITLE} ASC"
            )

            cursor?.use { c ->
                val idColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val albumIdColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

                while (c.moveToNext()) {
                    val id = c.getLong(idColumn)
                    val title = c.getString(titleColumn) ?: "Unknown"
                    val artist = c.getString(artistColumn) ?: "Unknown Artist"
                    val album = c.getString(albumColumn) ?: "Unknown Album"
                    val duration = c.getLong(durationColumn)
                    val albumId = c.getLong(albumIdColumn)

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    ).toString()

                    val coverUri = ContentUris.withAppendedId(
                        Uri.parse("content://media/external/audio/albumart"),
                        albumId
                    ).toString()

                    mediaList.add(
                        TrackInfo(
                            id = id,
                            title = title,
                            artist = artist,
                            album = album,
                            duration = duration,
                            uri = contentUri,
                            cover = coverUri,
                            isFavourite = false
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return mediaList
    }

    override suspend fun getTrackById(id: Int): TrackInfo = withContext(Dispatchers.IO) {
        val track = fetchTrackFromMediaStore(id) ?: throw Exception("Track not found")
        val isFav = trackDao.isTrackFavorite(id.toLong())
        track.copy(isFavourite = isFav)
    }

    override suspend fun toggleFavorite(track: TrackInfo) {
        withContext(Dispatchers.IO) {
            val isCurFav = trackDao.isTrackFavorite(track.id)
            val favEntity = com.raaveinm.chirro.data.database.FavTrackInfo(
                id = track.id,
                uri = track.uri
            )

            if (isCurFav)
                trackDao.deleteTrack(favEntity)
            else
                trackDao.insertTrack(favEntity)
        }
    }

    private fun fetchTrackFromMediaStore(trackId: Int): TrackInfo? {
        val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val resolver = context.contentResolver
        val selection = "${MediaStore.Audio.Media._ID} = ?"
        val selectionArgs = arrayOf(trackId.toString())

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA
        )

        val cursor = resolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            null
        )

        cursor?.use { c ->
            if (c.moveToFirst()) {
                val idColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val dataColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

                val id = c.getLong(idColumn)
                val title = c.getString(titleColumn)
                val artist = c.getString(artistColumn)
                val album = c.getString(albumColumn)
                val duration = c.getLong(durationColumn)
                val trackUri = c.getString(dataColumn)

                return TrackInfo(
                    id = id,
                    title = title ?: "Unknown",
                    artist = artist ?: "Unknown Artist",
                    album = album ?: "Unknown Album",
                    duration = duration,
                    uri = trackUri ?: "",
                    isFavourite = false
                )
            }
        }
        return null
    }
}
