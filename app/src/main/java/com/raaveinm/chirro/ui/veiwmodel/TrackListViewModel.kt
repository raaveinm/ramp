package com.raaveinm.chirro.ui.veiwmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.raaveinm.chirro.data.room.DatabaseManager
import com.raaveinm.chirro.data.room.TrackInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TrackListViewModel(application: Application) : AndroidViewModel(application) {

    private val _trackList = MutableStateFlow<List<TrackInfo>>(emptyList())
    val trackList: StateFlow<List<TrackInfo>> = _trackList.asStateFlow()

    init { fetchTrackList() }

    private fun fetchTrackList() {
        viewModelScope.launch {
            _trackList.value = DatabaseManager().getInitialTrackList(getApplication())
        }
    }
}