package com.raaveinm.chirro.data

import android.content.Context
import com.raaveinm.chirro.data.database.TrackDatabase
import com.raaveinm.chirro.data.repository.TrackRepository
import com.raaveinm.chirro.data.repository.TrackRepositoryImpl
import com.raaveinm.chirro.domain.RetrieveMedia

interface AppContainer {
    val trackRepository: TrackRepository
    val mediaRetriever: RetrieveMedia
}

class DefaultAppContainer(private val context: Context): AppContainer {
    override val trackRepository: TrackRepository by lazy {
        TrackRepositoryImpl(context, TrackDatabase.getDatabase(context).trackDao())
    }
    override val mediaRetriever: RetrieveMedia by lazy {
        RetrieveMedia(context.contentResolver)
    }
}
