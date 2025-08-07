package com.raaveinm.chirro.data

import android.content.Context
import com.raaveinm.chirro.domain.RetrieveMedia

interface AppContainer {
    val trackRepository: TrackRepository
    val mediaRetriever: RetrieveMedia
}

class DefaultAppContainer(private val context: Context): AppContainer {
    override val trackRepository: TrackRepository by lazy {
        OfflineTrackRepository(TrackDatabase.getDatabase(context).trackDao())
    }
    override val mediaRetriever: RetrieveMedia by lazy {
        RetrieveMedia(context.contentResolver)
    }
}