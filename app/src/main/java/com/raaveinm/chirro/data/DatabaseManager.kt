package com.raaveinm.chirro.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.launch

class DatabaseManager {
    fun databaseManager(context: Context) {
        CoroutineScope(Dispatchers.IO).launch { deleteDatabase(context) }
        CoroutineScope(Dispatchers.IO).launch { delay(100L); fillDatabase(context) }
        CoroutineScope(Dispatchers.IO).launch { delay(500L); getDatabase(context) }
    }

    suspend fun fillDatabase(context: Context){

        var trackList: List<TrackInfo> = MediaResolve(context.contentResolver).resolve()
        for (track in trackList){
            TrackDatabase.getDatabase(context).trackDao().insertTrack(track)
            Log.d("chirroDatabaseManager", "fillDatabase: ${track.title} inserted")
        }
    }

    suspend fun updateDatabase(context: Context) {
        var trackList: List<TrackInfo> = MediaResolve(context.contentResolver).resolve()
        for (track in trackList) {
            TrackDatabase.getDatabase(context).trackDao().updateTrack(track)
            Log.d("chirroDatabaseManager", "fillDatabase: ${track.title} inserted")
        }
    }

    suspend fun deleteDatabase(context: Context){
        var trackList: List<TrackInfo> = MediaResolve(context.contentResolver).resolve()
        for (track in trackList){
            TrackDatabase.getDatabase(context).trackDao().deleteTrack(track)
        }
    }

    suspend fun getDatabase(context: Context): Flow<List<TrackInfo>> {
        val trackList: Flow<List<TrackInfo>> = TrackDatabase.getDatabase(context).trackDao().getTracks()
        Log.d("chirroDatabaseManager", "getDatabase: $trackList, ")
        Log.d("chirroDatabaseManager", "getDatabase: ${trackList.count()}")
        return trackList
    }

    suspend fun getTrackById(context: Context, trackId: Int): TrackInfo {
        val track: TrackInfo = TrackDatabase.getDatabase(context).trackDao().getTrackById(trackId)
        Log.d("chirroDatabaseManager", "getTrack: $track")
        return track
    }
}