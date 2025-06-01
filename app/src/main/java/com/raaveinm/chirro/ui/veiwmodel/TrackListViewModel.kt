package com.raaveinm.chirro.ui.veiwmodel

import androidx.lifecycle.ViewModel
import com.raaveinm.chirro.data.TrackInfo

class TrackListViewModel : ViewModel() {
    private val _trackList = mutableListOf<TrackInfo>()
    val trackList: List<TrackInfo> = _trackList

}