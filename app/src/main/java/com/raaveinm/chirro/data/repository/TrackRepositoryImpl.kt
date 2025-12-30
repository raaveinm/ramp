package com.raaveinm.chirro.data.repository

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
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
                    trySend(Unit)
                }
            }

            context.contentResolver.registerContentObserver(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                true,
                observer
            )

            trySend(Unit)
            awaitClose { context.contentResolver.unregisterContentObserver(observer) }
        }

        return mediaStoreFlow.combine(favFlow) { _, favoriteIds ->
            val localFiles = fetchTracksFromMediaStore()
            localFiles.map { track ->
                track.copy(isFavourite = favoriteIds.contains(track.id.toLong()))
            }
        }.flowOn(Dispatchers.IO)
    }

    private fun fetchTracksFromMediaStore(): List<TrackInfo> {
        val mediaList = ArrayList<TrackInfo>()
        val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val resolver = context.contentResolver

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
            null,
            null,
            null
        )

        cursor?.use { c ->
            val idColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

            while (c.moveToNext()) {
                val id = c.getLong(idColumn)
                val title = c.getString(titleColumn)
                val artist = c.getString(artistColumn)
                val album = c.getString(albumColumn)
                val duration = c.getLong(durationColumn)
                val trackUri = c.getString(dataColumn)

                mediaList.add(
                    TrackInfo(
                        id = id.toInt(),
                        title = title ?: "Unknown",
                        artist = artist ?: "Unknown Artist",
                        album = album ?: "Unknown Album",
                        duration = duration,
                        uri = trackUri ?: "",
                        isFavourite = false
                    )
                )
            }
        }
        return mediaList
    }

    override suspend fun getTrackById(id: Int): TrackInfo = withContext(Dispatchers.IO) {
        val track = fetchTrackFromMediaStore(id) ?: throw Exception("Track not found")
        val isFav = trackDao.isTrackFavorite(id)
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
                    id = id.toInt(),
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
