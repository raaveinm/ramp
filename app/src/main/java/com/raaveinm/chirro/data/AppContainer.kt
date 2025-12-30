package com.raaveinm.chirro.data

import android.content.Context
import com.raaveinm.chirro.data.database.TrackDatabase
import com.raaveinm.chirro.data.repository.TrackRepository
import com.raaveinm.chirro.data.repository.TrackRepositoryImpl

interface AppContainer {
    val trackRepository: TrackRepository
//    val mediaRetriever: RetrieveMedia
}

class DefaultAppContainer(private val context: Context): AppContainer {
    override val trackRepository: TrackRepository by lazy {
        TrackRepositoryImpl(context, TrackDatabase.getDatabase(context).trackDao())
    }
}
