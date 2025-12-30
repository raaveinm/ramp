package com.raaveinm.chirro.data.repository

import com.raaveinm.chirro.data.database.TrackInfo
import kotlinx.coroutines.flow.Flow

interface TrackRepository {
    fun getAllTracks(): Flow<List<TrackInfo>>
    suspend fun getTrackById(id: Int): TrackInfo
    suspend fun toggleFavorite(track: TrackInfo)
}