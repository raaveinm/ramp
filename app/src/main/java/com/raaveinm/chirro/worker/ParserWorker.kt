package com.raaveinm.chirro.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.raaveinm.chirro.ChirroApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ParserWorker(context: Context, workerParams: WorkerParameters)
    : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            return@withContext try {
                val appContainer = (applicationContext as ChirroApplication).container
                val mediaRetriever = appContainer.mediaRetriever
                val trackRepository = appContainer.trackRepository
                val mediaList = mediaRetriever.retrieveMedia()
                if (mediaList.isNotEmpty()) {
                    trackRepository.insertTracks(mediaList)
                } else { Result.failure() }
                Result.success()
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure()
            }
        }
    }
}