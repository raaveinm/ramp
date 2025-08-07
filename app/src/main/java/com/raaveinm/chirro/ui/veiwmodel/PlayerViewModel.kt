package com.raaveinm.chirro.ui.veiwmodel

import android.app.Application
import android.content.ComponentName
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.raaveinm.chirro.data.TrackInfo
import com.raaveinm.chirro.data.TrackRepository
import com.raaveinm.chirro.domain.PlaybackService
import com.raaveinm.chirro.domain.toMediaItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PlayerViewModel(
    application: Application,
    private val trackRepository: TrackRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState = _uiState.asStateFlow()

    private val _allTracks = MutableStateFlow<List<TrackInfo>>(emptyList())
    val allTracks = _allTracks.asStateFlow()

    private var mediaController: MediaController? = null

    init {
        initializeController()
        observeAllTracks()
        viewModelScope.launch {
            while (true) {
                updateProgress()
                delay(500) // Update progress every 500ms
            }
        }
    }

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
            val trackMediaItem = track.toMediaItem()

            val allMediaItems = _allTracks.value.map { it.toMediaItem() }
            val startIndex = allMediaItems.indexOfFirst { it.mediaId == trackMediaItem.mediaId }

            if (startIndex != -1) {
                controller.setMediaItems(allMediaItems, startIndex, 0)
                controller.prepare()
                controller.play()
            }
        }
    }

    fun pause() {
        mediaController?.pause()
    }

    fun resume() {
        mediaController?.play()
    }

    fun skipNext() {
        mediaController?.seekToNextMediaItem()
    }

    fun skipPrevious() {
        mediaController?.seekToPreviousMediaItem()
    }

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
        mediaController?.removeListener(playerListener)
        MediaController.releaseFuture(mediaController?.let {
            MediaController.Builder(getApplication(),
                SessionToken(getApplication(),
                    ComponentName(
                        getApplication(),
                        PlaybackService::class.java
                    ))
            ).buildAsync()
        }!!)
        super.onCleared()
    }

    private fun MediaItem.toTrackInfo(): TrackInfo? {
        val trackId = this.mediaId.toIntOrNull() ?: return null
        return _allTracks.value.find { it.id == trackId }
    }
}