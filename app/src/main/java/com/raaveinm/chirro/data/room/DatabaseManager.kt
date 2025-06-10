package com.raaveinm.chirro.data.room

import android.content.Context
import com.raaveinm.chirro.data.MediaResolve
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DatabaseManager {
    suspend fun getInitialTrackList(context: Context): List<TrackInfo> {
        val trackList = TrackDatabase.getDatabase(context).trackDao().getTracks().first()
        return trackList
    }

    fun getDatabase(context: Context): Flow<List<TrackInfo>> {
        val trackListFlow = TrackDatabase.getDatabase(context).trackDao().getTracks()
        return trackListFlow
    }

    fun databaseManager(context: Context) {
        CoroutineScope(Dispatchers.IO).launch { deleteDatabase(context) }
        CoroutineScope(Dispatchers.IO).launch { delay(100L); fillDatabase(context) }
    }

    suspend fun fillDatabase(context: Context){
        val mediaResolver = MediaResolve(context.contentResolver)
        val trackList: List<TrackInfo> = mediaResolver.resolve()
        val dao = TrackDatabase.getDatabase(context).trackDao()
        for (track in trackList){
            dao.insertTrack(track)
        }
    }

//    suspend fun updateDatabase(context: Context) {
//        val mediaResolver = MediaResolve(context.contentResolver)
//        val trackList: List<TrackInfo> = mediaResolver.resolve()
//        val dao = TrackDatabase.getDatabase(context).trackDao()
//        for (track in trackList) {
//            dao.updateTrack(track)
//        }
//    }

    suspend fun deleteDatabase(context: Context){
        TrackDatabase.getDatabase(context).trackDao().deleteAllTracks()
    }

    suspend fun getTrackById(context: Context, trackId: Int): TrackInfo {
        val track: TrackInfo = TrackDatabase.getDatabase(context).trackDao().getTrackById(trackId)
        return track
    }

    suspend fun updateTrackFavoriteStatus(context: Context, trackId: Int, isFavorite: Boolean) {
        val dao = TrackDatabase.getDatabase(context).trackDao()
        val track = dao.getTrackById(trackId)
        val updatedTrack = track.copy(isFavorite = isFavorite)
        dao.updateTrack(updatedTrack)
    }
}