package com.raaveinm.chirro.ui.veiwmodel

import android.media.session.MediaController
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.media3.common.Player
import com.google.common.util.concurrent.ListenableFuture
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

    var playerState by mutableStateOf<Player?>(null)
    var currentTrack by mutableStateOf<TrackState?>(null)
    var currentPosition by mutableLongStateOf(0L)
    var totalDuration by mutableLongStateOf(0L)
    val isPlaying by mutableStateOf(false)
    val isFavorite by mutableStateOf(false)
    val isRandom by mutableStateOf(false)
    val isRepeating by mutableStateOf(false)

    private var mediaControllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

}