package com.raaveinm.chirro.domain.managment

import android.content.Context
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.raaveinm.chirro.data.DatabaseManager
import com.raaveinm.chirro.data.TrackInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class QueueManager(
    private val context: Context,
    private val databaseManager: DatabaseManager
) {

    private var trackList: List<TrackInfo> = emptyList()
    private var mediaItemList: List<MediaItem> = emptyList()

    @OptIn(UnstableApi::class)
    suspend fun prepareQueue() {
        Log.d("QueueManager", "Preparing queue from database...")
        trackList = withContext(Dispatchers.IO) {
            databaseManager.getDatabase(context) as List<TrackInfo>
        }

        mediaItemList = trackList.map { trackInfo ->
            buildMediaItem(trackInfo)
        }
        Log.d("QueueManager", "Queue prepared with ${mediaItemList.size} items.")
    }

    /**
     * Returns the list of MediaItems for the player.
     * Ensure prepareQueue() has been called successfully before this.
     */
    @OptIn(UnstableApi::class)
    fun getMediaItems(): List<MediaItem> {
        if (mediaItemList.isEmpty()) {
            Log.w("QueueManager", "Media queue is empty")
        }
        return mediaItemList
    }

    fun findIndexOfTrack(trackId: Int): Int =  trackList.indexOfFirst { it.id == trackId }
    fun getTrackInfoByIndex(index: Int) = trackList.getOrNull(index)

    @OptIn(UnstableApi::class)
    private fun buildMediaItem(trackInfo: TrackInfo): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setTitle(trackInfo.title)
            .setArtist(trackInfo.artist)
            .setAlbumTitle(trackInfo.album)
            // .setArtworkUri(trackInfo.artworkUri?.toUri())
            .build()

        return MediaItem.Builder()
            .setMediaId(trackInfo.id.toString())
            .setUri(trackInfo.uri.toUri())
            .setMediaMetadata(metadata)
            .build()
    }
}