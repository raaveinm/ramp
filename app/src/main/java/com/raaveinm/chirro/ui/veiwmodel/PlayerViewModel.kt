package com.raaveinm.chirro.ui.veiwmodel

import android.app.Activity
import android.app.Application
import android.content.ComponentName
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.annotation.OptIn
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.raaveinm.chirro.data.database.TrackInfo
import com.raaveinm.chirro.data.repository.TrackRepository
import com.raaveinm.chirro.domain.PlaybackService
import com.raaveinm.chirro.domain.toMediaItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class PlayerViewModel(
    application: Application,
    private val trackRepository: TrackRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState = _uiState.asStateFlow()
    val isPlaying: Boolean get() = _uiState.value.isPlaying
    private val _allTracks = MutableStateFlow<List<TrackInfo>>(emptyList())
    val allTracks = trackRepository.getAllTracks()
        .stateIn(
            scope = viewModelScope,
            started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(10000),
            initialValue = emptyList()
        )
    private var mediaController: MediaController? = null
    fun onFavoriteClicked(track: TrackInfo) {
        viewModelScope.launch {
            trackRepository.toggleFavorite(track)
        }
    }

    init {
        initializeController()
        observeAllTracks()

        viewModelScope.launch {
            while (true) {
                if (isPlaying) updateProgress()
                delay(100)
            }
        }
    }

    @OptIn(UnstableApi::class)
    private fun initializeController() {
        val sessionToken = SessionToken(
            getApplication(),
            ComponentName(getApplication(), PlaybackService::class.java)
        )
        val controllerFuture = MediaController.Builder(getApplication(), sessionToken).buildAsync()
        controllerFuture.addListener(
            {
                mediaController = controllerFuture.get()
                mediaController?.addListener(playerListener)
                updatePlayerState()
            },
            { it.run() }
        )
    }

    private fun observeAllTracks() {
        viewModelScope.launch {
            trackRepository.getAllTracks().collectLatest { tracks ->
                _allTracks.value = tracks
            }
        }
    }

    private val playerListener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            super.onEvents(player, events)
            if (events.containsAny(
                    Player.EVENT_PLAYBACK_STATE_CHANGED,
                    Player.EVENT_MEDIA_ITEM_TRANSITION,
                    Player.EVENT_IS_PLAYING_CHANGED
                )
            ) {
                updatePlayerState()
            }
        }
    }

    fun playTrack(track: TrackInfo) {
        mediaController?.let { controller ->
            val allMediaItems = _allTracks.value.map { it.toMediaItem() }
            val startIndex = allMediaItems.indexOfFirst { it.mediaId == track.id.toString() }

            if (startIndex != -1) {
                controller.setMediaItems(allMediaItems, startIndex, 0L)

                controller.prepare()
                controller.play()
            }
        }
    }

    fun pause() {
        mediaController?.pause()
        uiState.value.isPlaying = false
    }

    fun resume() {
        mediaController?.play()
        uiState.value.isPlaying = true
    }

    fun skipNext() = this.mediaController?.seekToNextMediaItem()
    fun skipPrevious() = this.mediaController?.seekToPreviousMediaItem()
    fun seekTo(position: Long) = this.mediaController?.seekTo(position)

    private fun updatePlayerState() {
        _uiState.value = _uiState.value.copy(
            isPlaying = mediaController?.isPlaying ?: false,
            currentTrack = mediaController?.currentMediaItem?.toTrackInfo(),
            totalDuration = mediaController?.duration?.coerceAtLeast(0) ?: 0L
        )
    }

    private fun updateProgress() {
        _uiState.value = _uiState.value.copy(
            currentPosition = mediaController?.currentPosition?.coerceAtLeast(0) ?: 0L
        )
    }

    override fun onCleared() {
        super.onCleared()
        mediaController?.removeListener(playerListener)
        mediaController?.release()
        mediaController = null
    }

    private fun MediaItem.toTrackInfo(): TrackInfo? {
        val trackId = this.mediaId.toIntOrNull() ?: return null
        return _allTracks.value.find { it.id == trackId.toLong() }
    }

    fun deleteTrack(track: TrackInfo, activity: Activity, launcher: ActivityResultLauncher<IntentSenderRequest>) {
        trackRepository.deleteTrack(track.id, activity, launcher)
    }
}
