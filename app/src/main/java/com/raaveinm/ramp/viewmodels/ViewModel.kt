package com.raaveinm.ramp.viewmodels

import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.raaveinm.ramp.services.ChirroPlayer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private const val TAG = "PlayerViewModel"

sealed class PlayerUiState {
    object Initializing : PlayerUiState()
    data class Ready(
        val playerState: PlayerState,
        val currentPosition: Long,
        val totalDuration: Long,
        val artwork: Bitmap? = null
    ) : PlayerUiState()
    data class Error(val message: String) : PlayerUiState()
}

data class PlayerState(
    val isPlaying: Boolean = false,
    val currentMediaMetadata: MediaMetadata? = null,
    val playWhenReady: Boolean = true,
    val playbackState: Int = Player.STATE_IDLE
)

class PlayerViewModel : ViewModel() {

    // --- State Flows ---
    private val _uiState = MutableStateFlow<PlayerUiState>(PlayerUiState.Initializing)
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    // --- Media Controller ---
    private var mediaControllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    // --- Position Tracking ---
    private var positionTrackingJob: Job? = null

    // --- Initialization ---
    fun initializeController(context: Context) {
        if (mediaController != null || mediaControllerFuture != null) {
            Log.d(TAG, "Controller already initialized or initializing.")
            return
        }
        Log.d(TAG, "Initializing MediaController...")
        val sessionToken = SessionToken(context, ComponentName(context, ChirroPlayer::class.java))
        mediaControllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        mediaControllerFuture?.addListener(
            {
                try {
                    val controller = mediaControllerFuture?.get()
                    if (controller != null) {
                        Log.i(TAG, "MediaController Connected!")
                        mediaController = controller
                        controller.addListener(playerListener)
                        updatePlayerState(controller)
                        startPositionTracking()
                        _uiState.value = PlayerUiState.Ready(
                            playerState = PlayerState(
                                isPlaying = controller.isPlaying,
                                currentMediaMetadata = controller.mediaMetadata,
                                playWhenReady = controller.playWhenReady,
                                playbackState = controller.playbackState
                            ),
                            currentPosition = controller.currentPosition,
                            totalDuration = controller.duration.coerceAtLeast(0L)
                        )
                    } else {
                        Log.e(TAG, "MediaController connection failed (controller is null)")
                        _uiState.value = PlayerUiState.Error("Failed to connect to media service")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "MediaController connection failed", e)
                    _uiState.value = PlayerUiState.Error("Failed to connect: ${e.message}")
                }
            },
            MoreExecutors.directExecutor()
        )
    }

    // --- Playback Controls ---
    fun playPause() {
        mediaController?.let {
            if (it.isPlaying) {
                it.pause()
                Log.d(TAG, "Sending PAUSE command")
            } else {
                if (it.playbackState == Player.STATE_IDLE || it.playbackState == Player.STATE_ENDED) {
                    it.prepare()
                    it.seekToDefaultPosition()
                }
                it.play()
                Log.d(TAG, "Sending PLAY command")
            }
        } ?: Log.w(TAG, "playPause called but controller is null")
    }

    fun skipNext() {
        if (mediaController?.hasNextMediaItem() == true) {
            mediaController?.seekToNextMediaItem()
            Log.d(TAG, "Sending SKIP_NEXT command")
        } else {
            Log.w(TAG, "skipNext called but no next item or controller is null")
        }
    }

    fun skipPrevious() {
        if (mediaController?.hasPreviousMediaItem() == true) {
            mediaController?.seekToPreviousMediaItem()
            Log.d(TAG, "Sending SKIP_PREVIOUS command")
        } else {
            Log.w(TAG, "skipPrevious called but no previous item or controller is null")
        }
    }

    fun seekTo(positionMs: Long) {
        mediaController?.seekTo(positionMs)
        Log.d(TAG, "Sending SEEK_TO $positionMs command")
    }

    fun playMediaId(mediaId: String) {
        mediaController?.let { controller ->
            var itemFound = false
            for (i in 0 until controller.mediaItemCount) {
                if (controller.getMediaItemAt(i).mediaId == mediaId) {
                    Log.d(TAG, "Found mediaId $mediaId at index $i. Seeking and playing.")
                    controller.seekTo(i, 0L)
                    controller.playWhenReady = true
                    controller.prepare()
                    controller.play()
                    itemFound = true
                    break
                }
            }
            if (!itemFound) {
                Log.w(TAG, "MediaId $mediaId not found in current playlist.")
            }
        } ?: Log.w(TAG, "playMediaId called but controller is null")
    }


    // --- Player Listener ---
    private val playerListener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            if (events.containsAny(
                    Player.EVENT_PLAYBACK_STATE_CHANGED,
                    Player.EVENT_MEDIA_METADATA_CHANGED,
                    Player.EVENT_IS_PLAYING_CHANGED,
                    Player.EVENT_PLAY_WHEN_READY_CHANGED,
                    Player.EVENT_MEDIA_ITEM_TRANSITION
                )
            ) {
                updatePlayerState(player)
            }
            if (events.contains(Player.EVENT_PLAYER_ERROR)) {
                handlePlayerError(player.playerError)
            }
            if (events.contains(Player.EVENT_IS_PLAYING_CHANGED)) {
                if (player.isPlaying) {
                    startPositionTracking()
                } else {
                    stopPositionTracking()
                    updatePosition(player.currentPosition, player.duration)
                }
            }
            if (events.contains(Player.EVENT_TIMELINE_CHANGED) || events.contains(Player.EVENT_MEDIA_ITEM_TRANSITION)) {
                updatePosition(player.currentPosition, player.duration)
            }
        }
    }

    private fun updatePlayerState(player: Player) {
        val newState = PlayerState(
            isPlaying = player.isPlaying,
            currentMediaMetadata = player.mediaMetadata,
            playWhenReady = player.playWhenReady,
            playbackState = player.playbackState
        )
        val currentUiState = _uiState.value
        if (currentUiState is PlayerUiState.Ready) {
            _uiState.value = currentUiState.copy(
                playerState = newState,
                // Keep position from tracking, duration updated here
                totalDuration = player.duration.coerceAtLeast(0L)
            )
        } else if (currentUiState is PlayerUiState.Initializing && player.playbackState != Player.STATE_IDLE) {
            // Transition from Initializing to Ready once player has state
            _uiState.value = PlayerUiState.Ready(
                playerState = newState,
                currentPosition = player.currentPosition,
                totalDuration = player.duration.coerceAtLeast(0L)
            )
        }
        Log.d(TAG, "State Updated: isPlaying=${newState.isPlaying}, state=${newState.playbackState}, title=${newState.currentMediaMetadata?.title}")
    }

    private fun updatePosition(position: Long, duration: Long) {
        val currentUiState = _uiState.value
        if (currentUiState is PlayerUiState.Ready) {
            if (kotlin.math.abs(currentUiState.currentPosition - position) > 500 || currentUiState.totalDuration != duration) {
                _uiState.value = currentUiState.copy(
                    currentPosition = position.coerceAtLeast(0L),
                    totalDuration = duration.coerceAtLeast(0L)
                )
            }
        }
    }

    private fun handlePlayerError(error: PlaybackException?) {
        val errorMessage = error?.message ?: "An unknown error occurred"
        Log.e(TAG, "Player Error: ${error?.errorCodeName} - $errorMessage", error)
        _uiState.value = PlayerUiState.Error(errorMessage)
        stopPositionTracking()
    }

    // --- Position Tracking ---
    private fun startPositionTracking() {
        stopPositionTracking()
        positionTrackingJob = viewModelScope.launch {
            while (isActive) {
                mediaController?.let {
                    if (it.isPlaying) {
                        updatePosition(it.currentPosition, it.duration)
                    }
                }
                delay(1000L)
            }
        }
        Log.d(TAG, "Started position tracking.")
    }

    private fun stopPositionTracking() {
        positionTrackingJob?.cancel()
        positionTrackingJob = null
        Log.d(TAG, "Stopped position tracking.")
    }

    // --- Cleanup ---
    override fun onCleared() {
        super.onCleared()
        stopPositionTracking()
        mediaController?.removeListener(playerListener)
        mediaControllerFuture?.let { MediaController.releaseFuture(it) }
        mediaController = null
        mediaControllerFuture = null
        Log.d(TAG, "ViewModel Cleared, Controller Released")
    }
}