package com.raaveinm.chirro.ui.veiwmodel

import android.app.Application
import android.content.ComponentName
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.raaveinm.chirro.data.TrackDao
import com.raaveinm.chirro.data.TrackInfo
import com.raaveinm.chirro.domain.PlayerService
import com.raaveinm.chirro.domain.managment.PlayListManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class TrackState(
    val id: Int = 0,
    val title: String = "Unknown Title",
    val artist: String = "Unknown Artist",
    val album: String = "Unknown Album",
    val duration: Long = 0L,
    val uri: String? = null,
    val artUri: String? = null,
    val isFavorite: Boolean = false,
    val included: Boolean = true
)

class PlayerViewModel(application: Application, private val trackDao: TrackDao) : AndroidViewModel(application) {

}