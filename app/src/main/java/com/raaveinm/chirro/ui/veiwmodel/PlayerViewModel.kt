package com.raaveinm.chirro.ui.veiwmodel

import android.app.Activity
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.raaveinm.chirro.data.database.TrackInfo
import com.raaveinm.chirro.data.repository.TrackRepository
import com.raaveinm.chirro.domain.Eggs
import com.raaveinm.chirro.domain.PlaybackService
import com.raaveinm.chirro.domain.toMediaItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Main Screen UI management and media queue controls
 * @param application
 * @param trackRepository
 *
 * @property _uiState
 * @property uiState
 * @property isPlaying
 * @property _allTracks
 * @property allTracks
 * @property mediaController
 * @property playerListener
 * @property updatePlayerState
 * @property updateProgress
 */

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
//    fun onFavoriteClicked(track: TrackInfo) {
//        viewModelScope.launch {
//            trackRepository.toggleFavorite(track)
//        }
//    }

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
            trackRepository.getAllTracks().collect { tracks ->
                _allTracks.value = tracks
                if (tracks.isNotEmpty()) {
                    updatePlayerState()
                }
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
        updatePlayerState()
    }

    fun resume() {
        mediaController?.play()
        updatePlayerState()
    }

    fun skipNext() = this.mediaController?.seekToNextMediaItem()
    fun skipPrevious() = this.mediaController?.seekToPreviousMediaItem()
    fun seekTo(position: Long) {
        mediaController?.seekTo(position)
        updateProgress()
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
        super.onCleared()
        mediaController?.removeListener(playerListener)
        mediaController?.release()
        mediaController = null
    }

    private fun MediaItem.toTrackInfo(): TrackInfo? {
        val trackId = this.mediaId.toIntOrNull() ?: return null
        return _allTracks.value.find { it.id == trackId.toLong() }
    }

    fun deleteTrack(
        track: TrackInfo,
        activity: Activity,
        launcher: ActivityResultLauncher<IntentSenderRequest>
    ): Boolean {
        mediaController?.let { controller ->
            val trackList = track.id.toString()
            for (index in 0 until controller.mediaItemCount) {
                if (controller.getMediaItemAt(index).mediaId == trackList) {
                    controller.removeMediaItem(index)
                    break
                }
            }
        }
        return trackRepository.deleteTrack(track.id, activity, launcher)
    }

    fun shareTrack(context: Context, track: TrackInfo?) {
        if (track == null) {
            Log.e("shareTrack", "Track is null")
            return
        }

        val shareTrackIntent = Intent(Intent.ACTION_SEND).apply {
            type = "audio/*"
            putExtra(Intent.EXTRA_STREAM, track.uri.toUri())
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooserIntent = Intent.createChooser(shareTrackIntent, "Share track")
        if (context !is Activity)
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooserIntent)
    }

    ///////////////////////////////////////////////
    // Search functionality
    ///////////////////////////////////////////////
    private val _searchUiState = MutableStateFlow(SearchBarUiState())
    val searchUiState = _searchUiState.asStateFlow()

    // Text field handling
    fun onTextChanged(text: String) {
        _searchUiState.value = _searchUiState.value.copy(currentText = text)
        filterTracks(text)
    }

    fun onSearchToggle() {
        _searchUiState.value = _searchUiState.value.copy(
            isSearching = !_searchUiState.value.isSearching)
    }

    fun clearSearch() {
        _searchUiState.value = _searchUiState.value.copy(currentText = "")
        filterTracks("")
    }

    // Filtered tracklist
    private fun filterTracks(query: String) {
        if (query.isBlank()) {
            _searchUiState.value = _searchUiState.value.copy(searchResults = emptyList())
            return
        }

        val keyWords = query.trim().split("\\s+".toRegex())

        val filteredTracks = _allTracks.value.filter { track ->
            keyWords.all { keyWord ->
                track.title.contains(keyWord, ignoreCase = true) ||
                        track.artist.contains(keyWord, ignoreCase = true) ||
                        track.album.contains(keyWord, ignoreCase = true)
            }
        }

        _searchUiState.value = _searchUiState.value.copy(searchResults = filteredTracks)
    }

    ///////////////////////////////////////////////
    // Easter Eggs
    ///////////////////////////////////////////////
    fun backgroundEasterEgg(): Eggs? {
        val trackName = _uiState.value.currentTrack?.title ?: return null
        val trackArtist = _uiState.value.currentTrack?.artist ?: return null
        val keyWordsTitle = listOf("arc")
        val keyWordsArtist = listOf("embark", "arc")
        trackName.lowercase()
        if (keyWordsArtist[0] in trackArtist.lowercase() || keyWordsTitle[0] in trackName.lowercase() && keyWordsArtist.any { it in trackArtist.lowercase() }) {
            return Eggs.ARC
        }
        return null
    }
}
