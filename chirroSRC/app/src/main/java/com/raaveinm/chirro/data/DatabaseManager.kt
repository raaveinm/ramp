package com.raaveinm.chirro.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.count // Keep this import if you still use count elsewhere
import kotlinx.coroutines.flow.first // Import first
import kotlinx.coroutines.launch

class DatabaseManager {
    suspend fun getInitialTrackList(context: Context): List<TrackInfo> {
        val trackList = TrackDatabase.getDatabase(context).trackDao().getTracks().first()
        Log.d("chirroDatabaseManager", "getInitialTrackList: Got ${trackList.size} tracks")
        return trackList
    }

    suspend fun getDatabase(context: Context): Flow<List<TrackInfo>> {
        val trackListFlow = TrackDatabase.getDatabase(context).trackDao().getTracks()
        Log.d("chirroDatabaseManager", "getDatabase Flow requested")
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
            Log.d("chirroDatabaseManager", "fillDatabase: ${track.title} inserted")
        }
    }

    suspend fun updateDatabase(context: Context) {
        val mediaResolver = MediaResolve(context.contentResolver)
        val trackList: List<TrackInfo> = mediaResolver.resolve()
        val dao = TrackDatabase.getDatabase(context).trackDao()
        for (track in trackList) {
            dao.updateTrack(track)
            Log.d("chirroDatabaseManager", "updateDatabase: ${track.title} updated")
        }
    }

    suspend fun deleteDatabase(context: Context){
        TrackDatabase.getDatabase(context).trackDao().deleteAllTracks()
        Log.d("chirroDatabaseManager", "deleteDatabase: All tracks deleted")
    }

    suspend fun getTrackById(context: Context, trackId: Int): TrackInfo {
        val track: TrackInfo = TrackDatabase.getDatabase(context).trackDao().getTrackById(trackId)
        Log.d("chirroDatabaseManager", "getTrack: $track")
        return track
    }

    suspend fun updateTrackFavoriteStatus(context: Context, trackId: Int, isFavorite: Boolean) {
        val dao = TrackDatabase.getDatabase(context).trackDao()
        val track = dao.getTrackById(trackId)
        if (true) {
            val updatedTrack = track.copy(isFavorite = isFavorite)
            dao.updateTrack(updatedTrack)
            Log.d("chirroDatabaseManager", "updateTrackFavoriteStatus:" +
                    " Track $trackId favorite status updated to $isFavorite")
        } else {
            Log.w("chirroDatabaseManager", "updateTrackFavoriteStatus:" +
                    " Track $trackId not found")
        }
    }
}