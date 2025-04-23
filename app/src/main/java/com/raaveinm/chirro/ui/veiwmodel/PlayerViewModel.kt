package com.raaveinm.chirro.ui.veiwmodel

import androidx.lifecycle.ViewModel
import com.raaveinm.chirro.data.TrackDao
import com.raaveinm.chirro.data.TrackInfo
import com.raaveinm.chirro.domain.managment.PlayListManager
import kotlinx.coroutines.flow.Flow


data class TrackState(
    val id: Int = 0,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val uri: String,
    val artUri: String,
    val isFavorite: Boolean,
    val included: Boolean
)

class PlayerViewModel(private val trackDao: TrackDao) : ViewModel() {
    private val playListManager = PlayListManager(trackDao)
    val trackList: Flow<List<TrackInfo>> = playListManager.getAllTracks()
    val favouriteTracks: Flow<List<TrackInfo>> = playListManager.getFavouriteTracks()


}