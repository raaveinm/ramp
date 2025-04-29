package com.raaveinm.chirro.domain.managment

import android.content.Context
import androidx.annotation.OptIn
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import com.raaveinm.chirro.data.DatabaseManager
import com.raaveinm.chirro.data.TrackInfo
import com.raaveinm.chirro.domain.PlayerService
import androidx.media3.common.util.Log
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class QueueManager (private val context: Context) {
    private var trackId: Int by mutableIntStateOf(1)
    var currentTrack: TrackInfo? by mutableStateOf(null)
        private set

    suspend fun loadInitialTrack() {
        withContext(Dispatchers.IO) {
             currentTrack = DatabaseManager().getTrackById(context = context, trackId = trackId)
       }
    }

    @OptIn(UnstableApi::class)
    fun setupPlayerWithTrack(track: TrackInfo?) {
         if (track == null) {
            Log.e("QueueManager", "Cannot start player, track is null")
            return
        }
        if (PlayerService().getPlayer() != null) {
             val metadata = MediaMetadata.Builder()
                .setTitle(track.title)
                .setArtist(track.artist)
                .build()

            val mediaItem = MediaItem.Builder()
                .setMediaMetadata(metadata)
                .setUri(track.uri.toUri())
                .build()
            PlayerService().getPlayer()?.setMediaItem(mediaItem)
            PlayerService().getPlayer()?.prepare()
            PlayerService().getPlayer()?.play()
        }
    }

    fun nextItem() {
        trackId++
        PlayerService().getPlayer()?.stop()
       // lifecycleScope.launch { loadInitialTrack(); setupPlayerWithTrack(currentTrack) }
    }

    fun previousItem() {
        if (trackId > 1) {
            trackId--
            PlayerService().getPlayer()?.stop()
          // lifecycleScope.launch { loadInitialTrack(); setupPlayerWithTrack(currentTrack) }
        }
    }
}