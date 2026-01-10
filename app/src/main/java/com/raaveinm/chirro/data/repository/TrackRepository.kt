package com.raaveinm.chirro.data.repository

import android.app.Activity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.raaveinm.chirro.data.database.TrackInfo
import kotlinx.coroutines.flow.Flow

interface TrackRepository {
    fun getAllTracks(): Flow<List<TrackInfo>>
    suspend fun getTrackById(id: Int): TrackInfo
    suspend fun toggleFavorite(track: TrackInfo)
    fun deleteTrack(trackId: Long, activity: Activity, launcher: ActivityResultLauncher<IntentSenderRequest>) : Boolean
}
