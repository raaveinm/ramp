package com.raaveinm.chirro.data.repository

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import com.raaveinm.chirro.data.database.TrackDao
import com.raaveinm.chirro.data.database.TrackInfo
import kotlinx.coroutines.flow.Flow

class TrackRepositoryImpl(
    private val context: Context,
    private val trackDao: TrackDao
) : TrackRepository {

    private val contentObserver = object :
        ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            // TODO: Implement behavior when media content changes
        }
    }

    init {
        context.contentResolver.registerContentObserver(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            true,
            contentObserver
        )
    }

    override fun getAllTracks(): Flow<List<TrackInfo>> {
        TODO("Not yet implemented")
    }

    override suspend fun getTrackById(id: Int): TrackInfo {
        TODO("Query MediaStore\n" +
                "Query FavoritesDao (List of favorite IDs)\n" +
                "Map to TrackInfo, setting isFavourite = true if ID exists in Dao")
    }
}
