package com.raaveinm.chirro.ui.veiwmodel

import android.app.Activity
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.PowerManager
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
import com.raaveinm.chirro.data.datastore.SettingDataStoreRepository
import com.raaveinm.chirro.data.repository.TrackRepository
import com.raaveinm.chirro.data.values.Eggs
import com.raaveinm.chirro.data.values.TrackInfo
import com.raaveinm.chirro.domain.PlaybackService
import com.raaveinm.chirro.domain.toMediaItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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
    private val trackRepository: TrackRepository,
    private var settingsRepository: SettingDataStoreRepository
) : AndroidViewModel(application) {
    // Ui State
    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState = _uiState.asStateFlow()
    val isPlaying: Boolean get() = _uiState.value.isPlaying

    // Power Management
    private val context = getApplication<Application>()
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    private val _isPowerSaveMode = MutableStateFlow(powerManager.isPowerSaveMode)
    val isPowerSaveMode = _isPowerSaveMode.asStateFlow()
    private val powerModeReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == PowerManager.ACTION_POWER_SAVE_MODE_CHANGED) {
                _isPowerSaveMode.value = powerManager.isPowerSaveMode
            }
        }
    }

    // Track resolving
    private val _allTracks = MutableStateFlow<List<TrackInfo>>(emptyList())
    val allTracks = _allTracks.asStateFlow()
    private val _controllerReady = MutableStateFlow(false)
    private var mediaController: MediaController? = null

//    fun onFavoriteClicked(track: TrackInfo) {
//        viewModelScope.launch {
//            trackRepository.toggleFavorite(track)
//        }
//    }

    init {
        val filter = IntentFilter(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED)
        context.registerReceiver(powerModeReceiver, filter)

        initializeController()
        observeAllTracks()

        viewModelScope.launch {
            while (true) {
                if (isPlaying) updateProgress()
                delay(if (isPowerSaveMode.value) 300 else 100)
            }
        }
    }

    ///////////////////////////////////////////////
    // Update player queue
    ///////////////////////////////////////////////
    private fun updatePlayerQueue(tracks: List<TrackInfo>) {
        val controller = mediaController ?: return

        if (controller.shuffleModeEnabled) {
            controller.shuffleModeEnabled = false
        }

        if (controller.mediaItemCount == tracks.size) {
            var isSynced = true
            for (i in tracks.indices) {
                if (controller.getMediaItemAt(i).mediaId != tracks[i].id.toString()) {
                    isSynced = false
                    break
                }
            }
            if (isSynced) return
        }

        val currentMediaId = controller.currentMediaItem?.mediaId
        val newMediaItems = tracks.map { it.toMediaItem() }
        val currentIndex = if (currentMediaId != null) {
            tracks.indexOfFirst { it.id.toString() == currentMediaId }
        } else -1

        if (currentIndex != -1) {
            controller.setMediaItems(newMediaItems, currentIndex, controller.currentPosition)
        } else {
            controller.setMediaItems(newMediaItems)
        }
    }

    ///////////////////////////////////////////////
    // Init & Restore Session
    ///////////////////////////////////////////////

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
                mediaController?.shuffleModeEnabled = false
                mediaController?.addListener(playerListener)
                updatePlayerState()
                _controllerReady.value = true

                syncInitialState()
            },
            { it.run() }
        )
    }

    private fun syncInitialState() {
        viewModelScope.launch {
            val controller = mediaController ?: return@launch
            if (controller.mediaItemCount > 0) return@launch
            val tracks = _allTracks.value.takeIf { it.isNotEmpty() } ?: return@launch

            val prefs = settingsRepository.settingsPreferencesFlow.first()
            val savedTrackId = prefs.currentTrack?.id
            val shouldRestore = prefs.isSavedState && savedTrackId != null

            val mediaItems = tracks.map { it.toMediaItem() }

            if (shouldRestore) {
                val index = tracks.indexOfFirst { it.id == savedTrackId }
                if (index != -1) {
                    controller.setMediaItems(mediaItems, index, 0L)
                } else {
                    controller.setMediaItems(mediaItems)
                }
                controller.prepare()
                controller.pause()
            } else {
                controller.setMediaItems(mediaItems)
                controller.prepare()
            }
        }
    }

    private fun observeAllTracks() {
        viewModelScope.launch {
            trackRepository.getAllTracks()
                .collect { processedTracks ->
                    this@PlayerViewModel._allTracks.value = processedTracks

                    if (processedTracks.isNotEmpty()) {
                        updatePlayerQueue(processedTracks)
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
            val currentList = _allTracks.value
            val allMediaItems = currentList.map { it.toMediaItem() }
            val startIndex = currentList.indexOfFirst { it.id == track.id }

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
        try {
            context.unregisterReceiver(powerModeReceiver)
        } catch (e: IllegalArgumentException) {
            Log.e("PlayerViewModel", "Failed to unregister receiver $e")
        }
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
        if (keyWordsArtist[0] in trackArtist.lowercase()
            || keyWordsTitle[0] in trackName.lowercase()
            && keyWordsArtist.any { it in trackArtist.lowercase() }
        ) {
            return Eggs.ARC
        }
        return null
    }
}