package com.raaveinm.chirro.ui.veiwmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PlaylistViewModel : ViewModel() {
    private val _playlistUiState = MutableStateFlow(PlayerUiState())
    val playlistUiState: StateFlow<PlayerUiState> = _playlistUiState.asStateFlow()

}