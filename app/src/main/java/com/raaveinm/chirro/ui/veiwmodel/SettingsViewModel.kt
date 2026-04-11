package com.raaveinm.chirro.ui.veiwmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raaveinm.chirro.data.datastore.SettingDataStoreRepository
import com.raaveinm.chirro.data.values.EqualizerPreferences
import com.raaveinm.chirro.data.values.OrderMediaQueue
import com.raaveinm.chirro.data.values.TrackInfo
import com.raaveinm.chirro.domain.jni.AudioCore
import com.raaveinm.chirro.ui.theme.AppTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingDataStoreRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    private val _localAlphaOverride = MutableStateFlow<Float?>(null)
    private val _localEqualizerOverride = MutableStateFlow<EqualizerPreferences?>(null)
    val alphaState: StateFlow<BackgroundAlphaUiState> = combine(
        settingsRepository.uiSettingsFlow,
        _localAlphaOverride
    ) { uiSettings, localAlpha ->
        val dsAlpha = (uiSettings.backgroundImageOpacity).toFloat() / 100f
        BackgroundAlphaUiState(backgroundAlpha = localAlpha ?: dsAlpha)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BackgroundAlphaUiState(0.3f)
    )

    private var alphaSaveJob: Job? = null
    private var equalizerSaveJob: Job? = null

    val settingsFlow = combine(
        settingsRepository.settingsFlow,
        settingsRepository.playbackStateFlow,
        settingsRepository.uiSettingsFlow,
        _localEqualizerOverride
    ) { settings, playback, uiSettings, localEq ->
        SettingsUiState(
            trackPrimaryOrder = settings.trackPrimaryOrder,
            trackSecondaryOrder = settings.trackSecondaryOrder,
            trackSortAscending = settings.trackSortAscending,
            currentTheme = uiSettings.currentTheme,
            isSavedState = playback.isSavedState,
            isShuffleMode = settings.isShuffleMode,
            backgroundDynamicColor = uiSettings.backgroundDynamicColor,
            backgroundImage = uiSettings.backgroundImage,
            equalizerPreferences = localEq ?: playback.equalizerPreferences ?: EqualizerPreferences.NORMAL
        )
    }
//    var haptic: HapticFeedback
//        get() = LocalHapticFeedback.current
//        set(value) =

    init {
        viewModelScope.launch {
            settingsFlow.collect {
                _uiState.value = it
                applyEqualizer(it.equalizerPreferences)
            }
        }
    }

    ///////////////////////////////////////////////
    // Track Order
    ///////////////////////////////////////////////
    fun setShuffleMode(shuffleMode: Boolean) {
        viewModelScope.launch {
            settingsRepository.setShuffleMode(shuffleMode)
//            haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
        }
    }

    fun setTrackPrimaryOrder(id: Int) {
        val mediaQueue = when (id) {
            0 -> OrderMediaQueue.DEFAULT
            1 -> OrderMediaQueue.ID
            2 -> OrderMediaQueue.TITLE
            3 -> OrderMediaQueue.ARTIST
            4 -> OrderMediaQueue.ALBUM
            5 -> OrderMediaQueue.DURATION
            6 -> OrderMediaQueue.TRACK
            7 -> OrderMediaQueue.DATE_ADDED
            else -> OrderMediaQueue.DEFAULT
        }
        viewModelScope.launch {
            if (_uiState.value.trackSecondaryOrder == mediaQueue)
                settingsRepository.updateSecondaryOrder(_uiState.value.trackPrimaryOrder)
            settingsRepository.updatePrimaryOrder(mediaQueue)
        }
    }

    fun setTrackSecondaryOrder(id: Int) {
        val mediaQueue = when (id) {
            0 -> OrderMediaQueue.DEFAULT
            1 -> OrderMediaQueue.ID
            2 -> OrderMediaQueue.TITLE
            3 -> OrderMediaQueue.ARTIST
            4 -> OrderMediaQueue.ALBUM
            5 -> OrderMediaQueue.DURATION
            6 -> OrderMediaQueue.TRACK
            7 -> OrderMediaQueue.DATE_ADDED
            else -> OrderMediaQueue.DEFAULT
        }

        viewModelScope.launch {
            if (_uiState.value.trackPrimaryOrder == mediaQueue)
                settingsRepository.updatePrimaryOrder(_uiState.value.trackSecondaryOrder)
            settingsRepository.updateSecondaryOrder(mediaQueue)
        }
    }

    fun setTrackSortAscending(ascending: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateSortAscending(ascending)
        }
    }

    ///////////////////////////////////////////////
    // Theme
    ///////////////////////////////////////////////
    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            settingsRepository.updateTheme(theme)
        }
    }

    ///////////////////////////////////////////////
    // Application behavior
    ///////////////////////////////////////////////

    fun setSavedState(state: Boolean, trackInfo: TrackInfo? = null) {
        viewModelScope.launch {
            settingsRepository.setSavedState(state, trackInfo)
        }
    }

    fun setBackgroundDynamicColor(state: Boolean) {
        viewModelScope.launch {
            settingsRepository.setBackgroundDynamicColor(state)
        }
    }

    fun setBackgroundImage(state: Boolean) {
        viewModelScope.launch {
            settingsRepository.setBackgroundImage(state)
        }
    }

    fun setBackgroundAlpha(alpha: Float) {
        _localAlphaOverride.value = alpha
        alphaSaveJob?.cancel()
        alphaSaveJob = viewModelScope.launch {
            delay(300)
            settingsRepository.setBackgroundImgOpacity((alpha * 100).toInt())
        }
    }

    fun setEqualizer(equalizerPreferences: EqualizerPreferences) {
        _localEqualizerOverride.value = equalizerPreferences
        equalizerSaveJob?.cancel()
        equalizerSaveJob = viewModelScope.launch {
            delay(200)
            settingsRepository.updateEqualizer(equalizerPreferences)
            _localEqualizerOverride.value = null
        }
    }

    fun setEqualizerBand(index: Int, gain: Float) {
        val current = _uiState.value.equalizerPreferences
        val updated = when (index) {
            0 -> current.copy(id = "custom", subBass = gain)
            1 -> current.copy(id = "custom", bass = gain)
            2 -> current.copy(id = "custom", lowMid = gain)
            3 -> current.copy(id = "custom", mid = gain)
            4 -> current.copy(id = "custom", highMid = gain)
            5 -> current.copy(id = "custom", presence = gain)
            6 -> current.copy(id = "custom", brilliance = gain)
            7 -> current.copy(id = "custom", air = gain)
            else -> current
        }

        _localEqualizerOverride.value = updated
        equalizerSaveJob?.cancel()
        equalizerSaveJob = viewModelScope.launch {
            delay(500)
            settingsRepository.updateEqualizer(updated)
            _localEqualizerOverride.value = null
        }
    }

    private fun applyEqualizer(prefs: EqualizerPreferences) {
        prefs.toGainsList().forEachIndexed { index, gain ->
            AudioCore.setEqualizerBand(index, gain)
        }
    }
}

private fun EqualizerPreferences.toGainsList(): List<Float> {
    return listOf(subBass, bass, lowMid, mid, highMid, presence, brilliance, air)
}