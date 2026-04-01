package com.raaveinm.chirro.ui.veiwmodel

import android.app.Activity
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
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
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import com.raaveinm.chirro.data.datastore.SettingDataStoreRepository
import com.raaveinm.chirro.data.repository.TrackRepository
import com.raaveinm.chirro.data.values.Eggs
import com.raaveinm.chirro.data.values.TrackInfo
import com.raaveinm.chirro.domain.PlaybackService
import com.raaveinm.chirro.domain.toMediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.collections.emptyList

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
 * @property updateProgress
 */

@FlowPreview
class PlayerViewModel(
    application: Application,
    private val trackRepository: TrackRepository,
    private var settingsRepository: SettingDataStoreRepository
) : AndroidViewModel(application) {
    // Ui State
    private val _uiState = MutableStateFlow(PlayerUiState())
    private val _progressionUiState = MutableStateFlow(ProgressBarUiState())
    val uiState = _uiState.asStateFlow()
    val progressionUiState = _progressionUiState.asStateFlow()
    val isPlaying: Boolean get() = _uiState.value.isPlaying
    val dynamicColor: Flow<Boolean> get() = settingsRepository.uiSettingsFlow.map { it.backgroundDynamicColor }
    val backgroundImage: Flow<Boolean> get() = settingsRepository.uiSettingsFlow.map { it.backgroundImage }
    val opacityValue: Flow<Float> get() = settingsRepository.uiSettingsFlow.map { (it.backgroundImageOpacity).toFloat()/100 }
    private val _sleepTimerEndTimeMs = MutableStateFlow<Long?>(null)
    private val _sleepTimerRemainingSeconds = MutableStateFlow<Long?>(null)
    val sleepTimerRemainingSeconds = _sleepTimerRemainingSeconds.asStateFlow()
    private var sleepTimerJob: Job? = null

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
    private val _isPlayerScreenActive = MutableStateFlow(false)

    fun setPlayerScreenVisibility(isVisible: Boolean) {
        _isPlayerScreenActive.value = isVisible
        if (isVisible) updateProgress()
    }

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
            combine(
                uiState.map { it.isPlaying }.distinctUntilChanged(),
                _isPlayerScreenActive
            ) { isPlaying, isActive ->
                isPlaying && isActive
            }.collectLatest { shouldUpdate ->
                while (shouldUpdate) {
                    updateProgress()
                    val delayMs = if (isPowerSaveMode.value) 300L else 100L
                    delay(delayMs)
                }
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
                updateCurrentTrack()
                updatePlayState()
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

            val prefs = settingsRepository.playbackStateFlow.first()
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
                        updateCurrentTrack()
                    }
                }
        }
    }


    private val playerListener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            super.onEvents(player, events)

            if (events.contains(Player.EVENT_MEDIA_ITEM_TRANSITION))
                updateCurrentTrack()
            if (events.containsAny(
                    Player.EVENT_PLAYBACK_STATE_CHANGED,
                    Player.EVENT_IS_PLAYING_CHANGED
                )
            ) {
                updateCurrentTrack()
                updatePlayState()
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
        updatePlayState()
    }

    fun resume() {
        mediaController?.play()
        updatePlayState()
    }

    fun skipNext() = this.mediaController?.seekToNextMediaItem()
    fun skipPrevious() = this.mediaController?.seekToPreviousMediaItem()
    fun seekTo(position: Long) {
        mediaController?.seekTo(position)
        updateProgress()
    }

    private fun updateCurrentTrack() {
        _uiState.value = _uiState.value.copy(
            currentTrack = mediaController?.currentMediaItem?.toTrackInfo()
        )
        _progressionUiState.value = _progressionUiState.value.copy(
            totalDuration = mediaController?.duration?.coerceAtLeast(0) ?: 0L
        )
    }

    private fun updatePlayState() {
        _uiState.value = _uiState.value.copy(
            isPlaying = mediaController?.isPlaying ?: false
        )
    }

    private fun updateProgress() {
        _progressionUiState.value = _progressionUiState.value.copy(
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
        sleepTimerJob?.cancel()
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
    private val _searchQuery = MutableStateFlow("")
    private val _isSearching = MutableStateFlow(false)
    private val _isLoading = MutableStateFlow(false)

    @OptIn(FlowPreview::class, FlowPreview::class)
    val searchUiState: StateFlow<SearchBarUiState> = combine(
        _searchQuery,
        _isSearching,
        _isLoading,
        _searchQuery
            .debounce(400L)
            .distinctUntilChanged()
            .combine(_allTracks) { query, tracks ->
                if (query.isBlank()) {
                    emptyList()
                } else {
                    val keyWords = query.trim().split("\\s+".toRegex())

                    tracks.filter { track ->
                        keyWords.all { keyWord ->
                            track.title.contains(keyWord, ignoreCase = true) ||
                                    track.artist.contains(keyWord, ignoreCase = true) ||
                                    track.album.contains(keyWord, ignoreCase = true)
                        }
                    }
                }
            }
            .onEach { _isLoading.value = false }
            .flowOn(Dispatchers.Default)
    ) { query, isSearching, isLoading, results ->
        SearchBarUiState(
            currentText = query,
            isSearching = isSearching,
            searchResults = results,
            isLoading = isLoading
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SearchBarUiState()
    )

    // Text field handling
    fun onTextChanged(text: String) {
        _searchQuery.value = text
        _isLoading.value = true
    }

    fun onSearchToggle() {
        _isSearching.value = !_isSearching.value
    }

    fun clearSearch() {
        _searchQuery.value = ""
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

    ///////////////////////////////////////////////
    // Alarm Manager (Sleep Timer)
    ///////////////////////////////////////////////
    @OptIn(UnstableApi::class)
    fun startSleepTimer(seconds: Long) {
        val bundle = Bundle().apply {
            putLong(PlaybackService.EXTRA_SECONDS, seconds)
        }

        val future = mediaController?.sendCustomCommand(
            SessionCommand(PlaybackService.START_SLEEP_TIMER, Bundle.EMPTY),
            bundle
        )

        future?.addListener({
            val result = future.get()
            if (result.resultCode == SessionResult.RESULT_SUCCESS) {
                val endTime = result.extras.getLong(PlaybackService.EXTRA_END_TIME_MS, -1L)
                if (endTime != -1L) {
                    _sleepTimerEndTimeMs.value = endTime
                    startSleepTimerTicker()
                }
            }
        }, MoreExecutors.directExecutor())
    }

    private fun startSleepTimerTicker() {
        sleepTimerJob?.cancel()
        sleepTimerJob = viewModelScope.launch {
            while (_sleepTimerEndTimeMs.value != null) {
                val remaining = ((_sleepTimerEndTimeMs.value ?: 0L) - System.currentTimeMillis()) / 1000
                if (remaining <= 0) {
                    _sleepTimerRemainingSeconds.value = null
                    _sleepTimerEndTimeMs.value = null
                    break
                }
                _sleepTimerRemainingSeconds.value = remaining
                delay(1000)
            }
        }
    }

    @OptIn(UnstableApi::class)
    fun stopSleepTimer() {
        mediaController?.sendCustomCommand(
            SessionCommand(PlaybackService.STOP_SLEEP_TIMER, Bundle.EMPTY),
            Bundle.EMPTY
        )
        _sleepTimerEndTimeMs.value = null
        _sleepTimerRemainingSeconds.value = null
        sleepTimerJob?.cancel()
    }
}