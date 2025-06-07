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
import com.raaveinm.chirro.data.room.DatabaseManager
import com.raaveinm.chirro.data.room.TrackInfo
import com.raaveinm.chirro.domain.PlayerService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.SessionCommand
import com.raaveinm.chirro.domain.usecase.Commands

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
        mediaControllerFuture = MediaController.Builder(getApplication(), sessionToken).buildAsync()
        mediaControllerFuture?.addListener({
            try {
                mediaController = mediaControllerFuture?.get()
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
        } ?: run {
            _uiState.value = PlayerUiState()
        }
    }

    // --- UI Actions ---

    @OptIn(UnstableApi::class)
    fun playPause() {
        Log.d("PlayerViewModel", "playPause triggered")
        mediaController?.let {
            if (it.isPlaying) {
                it.pause()
            } else {
                it.play()
            }
        } ?: Log.w("PlayerViewModel", "playPause called but MediaController is null")
    }

    @OptIn(UnstableApi::class)
    fun skipNext() {
        if (mediaController?.hasNextMediaItem() == true) mediaController?.seekToNextMediaItem()
    }

    @OptIn(UnstableApi::class)
    fun skipPrevious() {
        val currentPosition = mediaController?.currentPosition ?: 0
        if (currentPosition > 3000 && mediaController?.hasPreviousMediaItem() == true) {
            mediaController?.seekTo(0)
        } else if (mediaController?.hasPreviousMediaItem() == true) {
            mediaController?.seekToPreviousMediaItem()
        }
    }

    @OptIn(UnstableApi::class)
    fun seekTo(positionPercent: Float) {
        mediaController?.let {
            val duration = it.duration
            if (duration > 0) {
                val newPosition = (duration * positionPercent).toLong()
                it.seekTo(newPosition)
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
            val args = Bundle().apply { putInt(Commands.EXTRA_TRACK_ID, currentTrackId) }
            val command = SessionCommand(Commands.ACTION_TOGGLE_FAVORITE, args)
            val futureResult = mediaController?.sendCustomCommand(command, args)

           futureResult?.addListener({  },MoreExecutors.directExecutor())

            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val currentStatus = _uiState.value.isFavorite
                    databaseManager.updateTrackFavoriteStatus(getApplication(), currentTrackId, !currentStatus)
                    withContext(Dispatchers.Main) {
                       _uiState.value = _uiState.value.copy(isFavorite = !currentStatus)
                    }
                } catch (e: Exception) {
                     Log.e("PlayerViewModel", "Error toggling favorite directly in DB for track $currentTrackId", e)
                }
            }
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
    }

    @OptIn(UnstableApi::class)
    private fun stopProgressUpdater() {
        progressJob?.cancel()
        progressJob = null
    }

    @SuppressLint("ImplicitSamInstance")
    @OptIn(UnstableApi::class)
    override fun onCleared() {
        super.onCleared()
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
            }
        }
        mediaController = null
        Log.w("PlayerViewModel", "MediaController released.")
    }
}