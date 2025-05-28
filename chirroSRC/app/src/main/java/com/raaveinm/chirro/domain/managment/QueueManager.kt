package com.raaveinm.chirro.domain.managment

import android.content.Context
import android.net.Uri
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
import java.io.File

class QueueManager(
    private val context: Context,
    private val databaseManager: DatabaseManager
) {

    private var trackList: List<TrackInfo> = emptyList()
    private var mediaItemList: List<MediaItem> = emptyList()

    @OptIn(UnstableApi::class)
    suspend fun prepareQueue() {
        trackList = withContext(Dispatchers.IO) {
            databaseManager.getInitialTrackList(context)
        }

        mediaItemList = trackList.mapNotNull { trackInfo ->
            try {
                val fileUri = try {
                    trackInfo.uri.toUri()
                } catch (e: Exception) {
                    Log.w("QueueManager", "Could not parse URI string:" +
                            " ${trackInfo.uri}, $e.")
                    val file = File(trackInfo.uri)
                    if (file.exists()) file.toUri() else null
                }

                if (fileUri != null) {
                    buildMediaItem(trackInfo, fileUri)
                } else {
                    Log.e("QueueManager", "Could not create valid URI for track:" +
                            " ${trackInfo.title} - Path: ${trackInfo.uri}")
                    null
                }
            } catch (e: Exception) {
                Log.e("QueueManager", "Error processing track: " +
                        "${trackInfo.title} - ${e.message}")
                null
            }
        }
    }

    @OptIn(UnstableApi::class)
    fun getMediaItems(): List<MediaItem> = mediaItemList


//    fun findIndexOfTrack(trackId: Int): Int =  trackList.indexOfFirst { it.id == trackId }
//    fun getTrackInfoByIndex(index: Int): TrackInfo? = trackList.getOrNull(index)

    @OptIn(UnstableApi::class)
    private fun buildMediaItem(trackInfo: TrackInfo, mediaUri: Uri): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setTitle(trackInfo.title)
            .setArtist(trackInfo.artist)
            .setAlbumTitle(trackInfo.album)
            .setArtworkUri(try {
                trackInfo.artUri.toUri()
            } catch (_: Exception) { null })
            .setExtras(android.os.Bundle().apply {
                putInt("databaseId", trackInfo.id)
                putBoolean("isFavorite", trackInfo.isFavorite)
            })
            .build()

        return MediaItem.Builder()
            .setMediaId(trackInfo.id.toString())
            .setUri(mediaUri)
            .setMediaMetadata(metadata)
            .build()
    }
}