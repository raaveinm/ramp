package com.raaveinm.chirro.domain.managment

import com.raaveinm.chirro.data.room.TrackDao
import com.raaveinm.chirro.data.room.TrackInfo
import kotlinx.coroutines.flow.Flow

class PlayListManager(private val trackDao: TrackDao) {
    fun getAllTracks(): Flow<List<TrackInfo>> = trackDao.getTracks()
    fun getFavouriteTracks(): Flow<List<TrackInfo>> = trackDao.getFavouriteTracks()
}