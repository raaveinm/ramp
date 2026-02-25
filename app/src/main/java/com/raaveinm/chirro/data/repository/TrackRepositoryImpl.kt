package com.raaveinm.chirro.data.repository

import android.app.Activity
import android.app.RecoverableSecurityException
import android.content.ContentUris
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.core.net.toUri
import com.raaveinm.chirro.data.database.TrackDao
import com.raaveinm.chirro.data.values.TrackInfo
import com.raaveinm.chirro.data.values.OrderMediaQueue
import com.raaveinm.chirro.data.datastore.SettingDataStoreRepository
import com.raaveinm.chirro.domain.shuffled
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.withContext

class TrackRepositoryImpl(
    private val context: Context,
    private val trackDao: TrackDao,
    private val settingsRepository: SettingDataStoreRepository
) : TrackRepository {

    private val repoScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val sharedTracksFlow: SharedFlow<List<TrackInfo>> = createSharedTracksFlow()

    ///////////////////////////////////////////////
    // Fetching favourites and combine with all songs
    ///////////////////////////////////////////////
    private fun createSharedTracksFlow(): SharedFlow<List<TrackInfo>> {
        val sortSettingsFlow = settingsRepository.settingsPreferencesFlow
            .map { Triple(it.trackPrimaryOrder, it.trackSecondaryOrder, it.trackSortAscending) }
            .distinctUntilChanged()

        val shuffleModeFlow = settingsRepository.settingsPreferencesFlow
            .map { it.isShuffleMode }
            .distinctUntilChanged()

        val favFlow = trackDao.getFavoriteIds()

        val mediaStoreTrigger = callbackFlow {
            val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean) { trySend(Unit) }
            }
            context.contentResolver.registerContentObserver(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, true, observer
            )
            trySend(Unit)
            awaitClose { context.contentResolver.unregisterContentObserver(observer) }
        }

        val fetchedTracksFlow = combine(mediaStoreTrigger, sortSettingsFlow) { _, sortParams ->
            fetchTracksFromMediaStore(
                primaryOrder = sortParams.first,
                secondaryOrder = sortParams.second,
                isAsc = sortParams.third
            )
        }.flowOn(Dispatchers.IO)
        return combine(fetchedTracksFlow, favFlow, shuffleModeFlow) { tracks, favoriteIds, isShuffle ->
            val processedFiles = if (isShuffle) tracks.shuffled() else tracks
            processedFiles.map { track ->
                track.copy(isFavourite = favoriteIds.contains(track.id))
            }
        }.flowOn(Dispatchers.IO).shareIn(
            scope = repoScope,
            started = SharingStarted.Lazily,
            replay = 1
        )
    }

    override fun getAllTracks(): Flow<List<TrackInfo>> {
        return sharedTracksFlow
    }

    ///////////////////////////////////////////////
    // Fetching all songs from MediaStore
    ///////////////////////////////////////////////
    private fun fetchTracksFromMediaStore(
        primaryOrder: OrderMediaQueue,
        secondaryOrder: OrderMediaQueue,
        isAsc: Boolean,
        limit: Int = Int.MAX_VALUE,
        offset: Int = 0
    ): List<TrackInfo> {
        val mediaList = ArrayList<TrackInfo>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DATE_ADDED
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = getSortOrder(primaryOrder, secondaryOrder, isAsc)
        Log.d("MediaStore", "SortOrder: $sortOrder")

        val finalSortOrder = if (limit != Int.MAX_VALUE)
            "$sortOrder LIMIT $limit OFFSET $offset" else sortOrder


        try {
            val cursor = context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                finalSortOrder
            )

            cursor?.use { c ->
                val idColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val albumIdColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val dateAddedColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)

                while (c.moveToNext()) {
                    val id = c.getLong(idColumn)
                    val title = c.getString(titleColumn) ?: ""
                    val artist = c.getString(artistColumn) ?: ""
                    val album = c.getString(albumColumn) ?: ""
                    val duration = c.getLong(durationColumn)
                    val albumId = c.getLong(albumIdColumn)
                    val dateAdded = c.getLong(dateAddedColumn)

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    ).toString()

                    val coverUri = ContentUris.withAppendedId(
                        "content://media/external/audio/albumart".toUri(),
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
                            isFavourite = false,
                            date = dateAdded
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return mediaList
    }

    override suspend fun getTracksPaged(page: Int, pageSize: Int): List<TrackInfo> {
        val prefs = settingsRepository.settingsPreferencesFlow.first()

        val offset = page * pageSize

        return if (prefs.isShuffleMode) {
            val allTracks = fetchTracksFromMediaStore(prefs.trackPrimaryOrder, prefs.trackSecondaryOrder, prefs.trackSortAscending)
            allTracks.shuffled().drop(offset).take(pageSize)

        } else {

            val rawTracks = fetchTracksFromMediaStore(
                primaryOrder = prefs.trackPrimaryOrder,
                secondaryOrder = prefs.trackSecondaryOrder,
                isAsc = prefs.trackSortAscending,
                limit = pageSize,
                offset = offset
            )

            val favIds = trackDao.getFavoriteIds().first()
            rawTracks.map { track ->
                track.copy(isFavourite = favIds.contains(track.id))
            }
        }
    }

    ///////////////////////////////////////////////
    // Fetching track by id from playlist
    ///////////////////////////////////////////////
    override suspend fun getTrackById(id: Int): TrackInfo = withContext(Dispatchers.IO) {
        val track = fetchTrackFromMediaStore(id) ?: throw Exception("Track not found")
        val isFav = trackDao.isTrackFavorite(id.toLong())
        track.copy(isFavourite = isFav)
    }

    ///////////////////////////////////////////////
    // Toggle favourite
    ///////////////////////////////////////////////
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

    ///////////////////////////////////////////////
    // Fetching track by id from MediaStore
    ///////////////////////////////////////////////
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
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DATE_ADDED
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
                val dateAddedColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)

                val id = c.getLong(idColumn)
                val title = c.getString(titleColumn)
                val artist = c.getString(artistColumn)
                val album = c.getString(albumColumn)
                val duration = c.getLong(durationColumn)
                val trackUri = c.getString(dataColumn)
                val dateAdded = c.getLong(dateAddedColumn)

                return TrackInfo(
                    id = id,
                    title = title ?: "",
                    artist = artist ?: "",
                    album = album ?: "",
                    duration = duration,
                    uri = trackUri ?: "",
                    isFavourite = false,
                    date = dateAdded
                )
            }
        }
        return null
    }

    ///////////////////////////////////////////////
    // Delete track from device storage
    ///////////////////////////////////////////////
    override fun deleteTrack(
        trackId: Long,
        activity: Activity,
        launcher: ActivityResultLauncher<IntentSenderRequest>
    ) : Boolean {
        val uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, trackId)
        val context = activity.applicationContext

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val deleteRequest = MediaStore.createDeleteRequest(context.contentResolver, listOf(uri))
            launcher.launch(IntentSenderRequest.Builder(deleteRequest).build())
            return false
        } else {
            try {
                val rows = context.contentResolver.delete(uri, null, null)
                return rows > 0
            } catch (e: SecurityException) {
                val intentSender = when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                        (e as? RecoverableSecurityException)?.userAction?.actionIntent?.intentSender
                    }
                    else -> null
                }

                intentSender?.let { sender ->
                    launcher.launch(IntentSenderRequest.Builder(sender).build())
                }
                return false
            }
        }
    }
}

private fun getSortOrder(
    primary: OrderMediaQueue,
    secondary: OrderMediaQueue,
    isAsc: Boolean
): String {
    fun OrderMediaQueue.toSql(): String {
        return when (this) {
            OrderMediaQueue.ALBUM -> MediaStore.Audio.Media.ALBUM
            OrderMediaQueue.ARTIST -> MediaStore.Audio.Media.ARTIST
            OrderMediaQueue.TITLE -> MediaStore.Audio.Media.TITLE
            OrderMediaQueue.DURATION -> MediaStore.Audio.Media.DURATION
            OrderMediaQueue.ID -> MediaStore.Audio.Media._ID
            OrderMediaQueue.TRACK -> MediaStore.Audio.Media.TRACK
            OrderMediaQueue.DATE_ADDED -> MediaStore.Audio.Media.DATE_ADDED
            else -> MediaStore.Audio.Media.TRACK
        }
    }

    val asc = if (isAsc) "ASC" else "DESC"
    if (secondary == OrderMediaQueue.ALBUM)
        return "${primary.toSql()} $asc, ${secondary.toSql()} $asc, ${MediaStore.Audio.Media.TRACK} $asc"
    return "${primary.toSql()} $asc, ${secondary.toSql()} $asc"
}