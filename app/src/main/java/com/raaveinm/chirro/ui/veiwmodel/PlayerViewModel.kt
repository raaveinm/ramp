package com.raaveinm.chirro.ui.veiwmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.ComponentName
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.raaveinm.chirro.data.DatabaseManager
import com.raaveinm.chirro.data.TrackInfo
import com.raaveinm.chirro.domain.PlayerService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult

data class PlayerUiState(
    val currentTrack: TrackInfo? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val totalDuration: Long = 0L,
    val isFavorite: Boolean = false
)

@UnstableApi
class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var mediaControllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    private val sessionToken = SessionToken(
        application,
        ComponentName(application, PlayerService::class.java)
    )

    private val playerObserverScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var progressJob: Job? = null

    private val databaseManager = DatabaseManager()


    init { initializeMediaController() }

    @OptIn(UnstableApi::class)
    private fun initializeMediaController() {
        Log.d("PlayerViewModel", "Initializing MediaController...")
        mediaControllerFuture = MediaController.Builder(getApplication(), sessionToken).buildAsync()
        mediaControllerFuture?.addListener({
            try {
                mediaController = mediaControllerFuture?.get() // Blocks until ready
                Log.i("PlayerViewModel", "MediaController connected: $mediaController")
                mediaController?.addListener(PlayerListener())
                updateStateFromController()
                startProgressUpdater()
            } catch (e: Exception) {
                Log.e("PlayerViewModel", "Error connecting MediaController: ${e.message}", e)
            }
        }, MoreExecutors.directExecutor())
    }

    @OptIn(UnstableApi::class)
    private fun updateStateFromController() {
        mediaController?.let { controller ->
            val currentMediaItem = controller.currentMediaItem
            val metadata = currentMediaItem?.mediaMetadata
            val isPlaying = controller.isPlaying
            val position = controller.currentPosition
            val duration = controller.duration.coerceAtLeast(0L)

            val trackId = metadata?.extras?.getInt("databaseId", -1) ?: -1
            val isFavorite = metadata?.extras?.getBoolean("isFavorite", false) == true


            _uiState.value = _uiState.value.copy(
                currentTrack = if (metadata != null) TrackInfo(
                    id = trackId,
                    title = metadata.title?.toString() ?: "Unknown",
                    artist = metadata.artist?.toString() ?: "Unknown",
                    album = metadata.albumTitle?.toString() ?: "Unknown",
                    duration = duration,
                    uri = currentMediaItem.requestMetadata.mediaUri?.toString() ?: "",
                    artUri = metadata.artworkUri?.toString() ?: "",
                    isFavorite = isFavorite,
                    included = true
                ) else null,
                isPlaying = isPlaying,
                currentPosition = position,
                totalDuration = duration,
                isFavorite = isFavorite
            )
            Log.d("PlayerViewModel", "UI State updated: isPlaying=$isPlaying, track=${metadata?.title}, pos=$position, dur=$duration, fav=$isFavorite")

        } ?: run {
            _uiState.value = PlayerUiState()
            Log.d("PlayerViewModel", "MediaController null, resetting UI state.")
        }
    }

    // --- UI Actions ---

    @OptIn(UnstableApi::class)
    fun playPause() {
        Log.d("PlayerViewModel", "playPause triggered")
        mediaController?.let {
            if (it.isPlaying) {
                it.pause()
                Log.i("PlayerViewModel", "Sent PAUSE command")
            } else {
                it.play()
                Log.i("PlayerViewModel", "Sent PLAY command")

            }
        } ?: Log.w("PlayerViewModel", "playPause called but MediaController is null")
    }

    @OptIn(UnstableApi::class)
    fun skipNext() {
        Log.d("PlayerViewModel", "skipNext triggered")
        if (mediaController?.hasNextMediaItem() == true) {
            mediaController?.seekToNextMediaItem()
            Log.i("PlayerViewModel", "Sent SKIP_NEXT command")
        } else {
            Log.w("PlayerViewModel", "skipNext called but no next item available")
        }
    }

    @OptIn(UnstableApi::class)
    fun skipPrevious() {
        Log.d("PlayerViewModel", "skipPrevious triggered")
        // Optional: Seek to beginning if position > threshold, else previous
        val currentPosition = mediaController?.currentPosition ?: 0
        if (currentPosition > 3000 && mediaController?.hasPreviousMediaItem() == true) { // Seek to 0 if played > 3s
            mediaController?.seekTo(0)
            Log.i("PlayerViewModel", "Sent SEEK_TO 0 command (instead of previous)")
        } else if (mediaController?.hasPreviousMediaItem() == true) {
            mediaController?.seekToPreviousMediaItem()
            Log.i("PlayerViewModel", "Sent SKIP_PREVIOUS command")
        } else {
            Log.w("PlayerViewModel", "skipPrevious called but no previous item available")
        }
    }

    @OptIn(UnstableApi::class)
    fun seekTo(positionPercent: Float) {
        mediaController?.let {
            val duration = it.duration
            if (duration > 0) {
                val newPosition = (duration * positionPercent).toLong()
                it.seekTo(newPosition)
                Log.i("PlayerViewModel", "Sent SEEK_TO command: " +
                        "$newPosition ms (${(positionPercent*100).toInt()}%)")
                _uiState.value = _uiState.value.copy(currentPosition = newPosition)
            } else {
                Log.w("PlayerViewModel", "seekTo called but duration is unknown or zero")
            }
        } ?: Log.w("PlayerViewModel", "seekTo called but MediaController is null")
    }

    @OptIn(UnstableApi::class)
    fun toggleFavorite() {
        val currentTrackId = _uiState.value.currentTrack?.id
        if (currentTrackId != null && currentTrackId != -1) {
            Log.d("PlayerViewModel", "toggleFavorite triggered for track ID: $currentTrackId")

            val args = Bundle().apply { putInt(PlayerService.EXTRA_TRACK_ID, currentTrackId) }
            val command = SessionCommand(PlayerService.ACTION_TOGGLE_FAVORITE, args)
//            val futureResult = mediaController?.sendCustomCommand(command, args)
//
//            futureResult?.addListener({
//                try {
//                    val result = futureResult.get()
//                    if (result.resultCode == SessionResult.RESULT_SUCCESS) {
//                        Log.i("PlayerViewModel", "TOGGLE_FAVORITE command success for track $currentTrackId")
//                    } else {
//                        Log.w("PlayerViewModel", "TOGGLE_FAVORITE command failed for track $currentTrackId with code: ${result.resultCode}")
//                    }
//                } catch (e: Exception) {
//                    Log.e("PlayerViewModel", "Error sending TOGGLE_FAVORITE command for track $currentTrackId", e)
//                }
//            }, MoreExecutors.directExecutor())

            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val currentStatus = _uiState.value.isFavorite
                    databaseManager.updateTrackFavoriteStatus(getApplication(), currentTrackId, !currentStatus)
                    // Manually update UI state - requires service state to be in sync or might cause flicker
                    withContext(Dispatchers.Main) {
                       _uiState.value = _uiState.value.copy(isFavorite = !currentStatus)
                    }
                    Log.i("PlayerViewModel", "Favorite status toggled directly in DB for track $currentTrackId")

                } catch (e: Exception) {
                     Log.e("PlayerViewModel", "Error toggling favorite directly in DB for track $currentTrackId", e)
                }
            }
        } else {
            Log.w("PlayerViewModel", "toggleFavorite called but current track ID is invalid: $currentTrackId")
        }
    }


    // --- Player Event Listener ---
    private inner class PlayerListener : Player.Listener {
        @OptIn(UnstableApi::class)
        override fun onEvents(player: Player, events: Player.Events) {
            if (events.containsAny(
                    Player.EVENT_PLAYBACK_STATE_CHANGED,
                    Player.EVENT_MEDIA_METADATA_CHANGED,
                    Player.EVENT_IS_PLAYING_CHANGED,
                    Player.EVENT_MEDIA_ITEM_TRANSITION)
            ) {
                Log.d("PlayerViewModel", "Player event received, updating UI state.")
                updateStateFromController()
            }
            if (events.contains(Player.EVENT_IS_PLAYING_CHANGED)) {
                if (player.isPlaying) {
                    startProgressUpdater()
                } else {
                    stopProgressUpdater()
                }
            }
        }
    }

    // --- Progress Updater ---
    @OptIn(UnstableApi::class)
    private fun startProgressUpdater() {
        stopProgressUpdater()
        progressJob = playerObserverScope.launch {
            while (isActive) {
                mediaController?.let {
                    if (it.isPlaying) {
                        val currentPosition = it.currentPosition
                        if (_uiState.value.currentPosition != currentPosition) {
                            _uiState.value = _uiState.value.copy(currentPosition = currentPosition)
                        }
                    }
                }
                delay(500)
            }
        }
        Log.d("PlayerViewModel", "Progress updater started.")
    }

    @OptIn(UnstableApi::class)
    private fun stopProgressUpdater() {
        progressJob?.cancel()
        progressJob = null
        Log.d("PlayerViewModel", "Progress updater stopped.")
    }

    @SuppressLint("ImplicitSamInstance")
    @OptIn(UnstableApi::class)
    override fun onCleared() {
        super.onCleared()
        Log.w("PlayerViewModel", "onCleared called - releasing MediaController")
        stopProgressUpdater()
        playerObserverScope.cancel()
        mediaController?.removeListener(PlayerListener())
        mediaControllerFuture?.let { future ->
            if (future.isDone && !future.isCancelled) {
                try {
                    MediaController.releaseFuture(future)
                } catch (e: Exception) {
                    Log.e("PlayerViewModel", "Error releasing MediaController future", e)
                }
            } else if (!future.isDone) {
                future.cancel(false)
                Log.w("PlayerViewModel", "Cancelled MediaController future onCleared")
            }
        }
        mediaController = null
        Log.w("PlayerViewModel", "MediaController released.")
    }
}