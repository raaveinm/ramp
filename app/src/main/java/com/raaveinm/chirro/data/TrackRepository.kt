package com.raaveinm.chirro.data

import kotlinx.coroutines.flow.Flow

// Interface remains the same
interface TrackRepository {
    fun getAllTracks(): Flow<List<TrackInfo>>
    suspend fun insertTracks(tracks: List<TrackInfo>)
    suspend fun getTrackById(id: Int): TrackInfo
}

// Implementation that uses Room
class OfflineTrackRepository(private val trackDao: TrackDao) : TrackRepository {
    override fun getAllTracks(): Flow<List<TrackInfo>> = trackDao.getAllTracks()
    override suspend fun insertTracks(tracks: List<TrackInfo>) {
        tracks.forEach { trackDao.insertTrack(it) }
    }
    override suspend fun getTrackById(id: Int): TrackInfo = trackDao.getTrackById(id)
}