package com.raaveinm.chirro.ui.veiwmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raaveinm.chirro.data.values.OrderMediaQueue
import com.raaveinm.chirro.data.datastore.SettingDataStoreRepository
import com.raaveinm.chirro.data.values.TrackInfo
import com.raaveinm.chirro.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingDataStoreRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    val settingsFlow = combine(
        settingsRepository.settingsFlow,
        settingsRepository.playbackStateFlow,
        settingsRepository.uiSettingsFlow) { settings, playback, uiSettings ->
        SettingsUiState(
            trackPrimaryOrder = settings.trackPrimaryOrder,
            trackSecondaryOrder = settings.trackSecondaryOrder,
            trackSortAscending = settings.trackSortAscending,
            currentTheme = uiSettings.currentTheme,
            isSavedState = playback.isSavedState,
            isShuffleMode = settings.isShuffleMode,
            backgroundDynamicColor = uiSettings.backgroundDynamicColor,
            backgroundImage = uiSettings.backgroundImage,
        )
    }

    init {
        viewModelScope.launch {
            settingsFlow.collect {
                _uiState.value = _uiState.value.copy(
                    trackPrimaryOrder = it.trackPrimaryOrder,
                    trackSecondaryOrder = it.trackSecondaryOrder,
                    trackSortAscending = it.trackSortAscending,
                    currentTheme = it.currentTheme,
                    isSavedState = it.isSavedState,
                    isShuffleMode = it.isShuffleMode,
                    backgroundDynamicColor = it.backgroundDynamicColor,
                    backgroundImage = it.backgroundImage
                )
            }
        }
    }

    ///////////////////////////////////////////////
    // Track Order
    ///////////////////////////////////////////////
    fun setShuffleMode(shuffleMode: Boolean) {
        viewModelScope.launch {
            settingsRepository.setShuffleMode(shuffleMode)
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
}